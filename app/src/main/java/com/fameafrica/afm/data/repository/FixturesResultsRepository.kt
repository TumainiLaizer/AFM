package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.FixturesResultsDao
import com.fameafrica.afm.data.database.dao.MonthlyStatistics
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import com.fameafrica.afm.data.database.entities.FixturesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FixturesResultsRepository @Inject constructor(
    private val fixturesResultsDaoProvider: Provider<FixturesResultsDao>,
    private val teamsRepository: TeamsRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val cupGroupStandingsRepository: CupGroupStandingsRepository
) {

    private val fixturesResultsDao: FixturesResultsDao?
        get() = try {
            fixturesResultsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    fun getAllResults(): Flow<List<FixturesResultsEntity>> = fixturesResultsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getResultByFixtureId(fixtureId: Int): FixturesResultsEntity? =
        fixturesResultsDao?.getByFixtureId(fixtureId)

    suspend fun insertResult(result: FixturesResultsEntity) {
        fixturesResultsDao?.insert(result)
    }

    suspend fun insertResultsBatch(results: List<FixturesResultsEntity>) {
        fixturesResultsDao?.insertAll(results)
    }

    fun getResultsByTeam(teamId: Int): Flow<List<FixturesResultsEntity>> =
        fixturesResultsDao?.getResultsByTeam(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    /**
     * Get recent results for a team with a specific limit.
     */
    fun getRecentResultsByTeam(teamId: Int, limit: Int): Flow<List<FixturesResultsEntity>> =
        getResultsByTeam(teamId).map { it.take(limit) }

    fun getCupResults(cupName: String, season: String): Flow<List<FixturesResultsEntity>> =
        fixturesResultsDao?.getCupResults(cupName, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getTeamForm(teamId: Int): TeamForm {
        val recentResults = fixturesResultsDao?.getResultsByTeam(teamId)?.firstOrNull()?.take(5) ?: emptyList()
        var wins = 0
        var draws = 0
        var losses = 0
        var gf = 0
        var ga = 0
        
        val formString = recentResults.joinToString("") { result ->
            val isHome = result.homeTeamId == teamId
            val scoreFor = if (isHome) result.homeScore else result.awayScore
            val scoreAgainst = if (isHome) result.awayScore else result.homeScore
            
            gf += scoreFor
            ga += scoreAgainst
            
            when {
                scoreFor > scoreAgainst -> {
                    wins++
                    "W"
                }
                scoreFor == scoreAgainst -> {
                    draws++
                    "D"
                }
                else -> {
                    losses++
                    "L"
                }
            }
        }.reversed()

        return TeamForm(
            formString = formString,
            played = recentResults.size,
            wins = wins,
            draws = draws,
            losses = losses,
            goalsFor = gf,
            goalsAgainst = ga,
            goalDifference = gf - ga,
            points = (wins * 3) + draws
        )
    }

    fun getMonthlyStats(year: String): Flow<List<MonthlyStatistics>> =
        fixturesResultsDao?.getMonthlyStats(year) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getResultsDashboard(): ResultsDashboard {
        val allResults = fixturesResultsDao?.getAll()?.firstOrNull() ?: emptyList()
        return ResultsDashboard(
            totalMatches = allResults.size,
            totalGoals = allResults.sumOf { it.homeScore + it.awayScore },
            homeWins = allResults.count { it.homeScore > it.awayScore },
            awayWins = allResults.count { it.awayScore > it.homeScore },
            draws = allResults.count { it.homeScore == it.awayScore }
        )
    }

    suspend fun getTeamRecordVsOpponent(teamId: Int, opponentId: Int): TeamRecord {
        val h2h = fixturesResultsDao?.getHeadToHead(teamId, opponentId)?.firstOrNull() ?: emptyList()
        var wins = 0
        var draws = 0
        var losses = 0
        h2h.forEach { result ->
            val isHome = result.homeTeamId == teamId
            val scoreFor = if (isHome) result.homeScore else result.awayScore
            val scoreAgainst = if (isHome) result.awayScore else result.homeScore
            
            when {
                scoreFor > scoreAgainst -> wins++
                scoreFor == scoreAgainst -> draws++
                else -> losses++
            }
        }
        return TeamRecord(wins, draws, losses)
    }

    suspend fun processMatchResult(
        fixture: FixturesEntity,
        homeScore: Int,
        awayScore: Int,
        manOfMatchId: Int? = null,
        manOfMatchTeamId: Int? = null
    ): FixturesResultsEntity {
        val result = FixturesResultsEntity(
            fixtureId = fixture.id,
            matchDate = fixture.matchDate,
            homeTeamId = fixture.homeTeamId,
            homeTeam = fixture.homeTeam,
            awayTeamId = fixture.awayTeamId,
            awayTeam = fixture.awayTeam,
            homeScore = homeScore,
            awayScore = awayScore,
            matchType = fixture.matchType,
            season = fixture.season,
            leagueName = fixture.league,
            cupName = fixture.cupName,
            stadium = fixture.stadium,
            manOfMatchId = manOfMatchId,
            manOfMatchTeamId = manOfMatchTeamId
        )
        
        insertResult(result)
        
        if (fixture.league != null) {
            leagueStandingsRepository.updateStandingsAfterMatch(result)
        }
        
        if (fixture.cupName != null && fixture.round.contains("Group")) {
            cupGroupStandingsRepository.updateGroupStandingsAfterMatch(result)
        }

        // Update abilities
        teamsRepository.recalculateTeamAbilities(fixture.homeTeamId)
        teamsRepository.recalculateTeamAbilities(fixture.awayTeamId)
        
        return result
    }
}

data class ResultsDashboard(
    val totalMatches: Int,
    val totalGoals: Int,
    val homeWins: Int,
    val awayWins: Int,
    val draws: Int
)

data class TeamRecord(val wins: Int, val draws: Int, val losses: Int)
