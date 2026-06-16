package com.fameafrica.afm.data.repository

import android.util.Log
import com.fameafrica.afm.data.database.dao.PreseasonScheduleDao
import com.fameafrica.afm.data.database.dao.PreseasonStats
import com.fameafrica.afm.data.database.entities.PreseasonScheduleEntity
import com.fameafrica.afm.data.database.entities.PreseasonStatus
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PreseasonScheduleRepository @Inject constructor(
    private val preseasonScheduleDaoProvider: Provider<PreseasonScheduleDao>,
    private val teamsRepository: TeamsRepository,
    private val fixturesRepository: FixturesRepository,
    private val gameDateManager: GameDateManager
) {

    private val preseasonScheduleDao: PreseasonScheduleDao?
        get() = try {
            preseasonScheduleDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============
    fun getAllPreseasonMatches(): Flow<List<PreseasonScheduleEntity>> = preseasonScheduleDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun getPreseasonMatchById(id: Int): PreseasonScheduleEntity? = preseasonScheduleDao?.getById(id)
    suspend fun insertPreseasonMatch(match: PreseasonScheduleEntity) {
        preseasonScheduleDao?.insert(match)
    }
    suspend fun updatePreseasonMatch(match: PreseasonScheduleEntity) {
        preseasonScheduleDao?.update(match)
    }
    suspend fun deletePreseasonMatch(match: PreseasonScheduleEntity) {
        preseasonScheduleDao?.delete(match)
    }
    suspend fun deleteBySeason(season: String) {
        preseasonScheduleDao?.deleteBySeason(season)
    }

    // ============ TEAM-BASED ============
    fun getTeamPreseasonSchedule(teamId: Int, season: String): Flow<List<PreseasonScheduleEntity>> =
        preseasonScheduleDao?.getTeamPreseasonSchedule(teamId, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getUpcomingPreseasonMatches(teamId: Int, season: String): Flow<List<PreseasonScheduleEntity>> =
        preseasonScheduleDao?.getUpcomingPreseasonMatches(teamId, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getCompletedPreseasonMatches(teamId: Int, season: String): Flow<List<PreseasonScheduleEntity>> =
        preseasonScheduleDao?.getCompletedPreseasonMatches(teamId, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ AI LOGIC: PRESEASON ============

    /**
     * AI teams schedule friendlies among themselves and occasionally invite the user.
     */
    suspend fun processAIPreseason(season: String, currentWeek: Int, userTeamId: Int) {
        // Preseason is usually weeks 1 to 10
        if (currentWeek > 10) return

        val allTeams = teamsRepository.getAllTeams().firstOrNull() ?: return
        val teamsWithPlayers = allTeams.filter { (it.avgAttackingAbility ?: 0.0) > 0 }
        
        val aiTeams = teamsWithPlayers.filter { it.id != userTeamId }.shuffled()
        
        for (team in aiTeams.take(10)) {
            val existing = preseasonScheduleDao?.getTeamPreseasonSchedule(team.id, season)?.firstOrNull() ?: emptyList()
            if (existing.size >= 4) continue // Already has enough friendlies

            // Decide to invite User or another AI
            val inviteUser = Random.nextInt(100) < 5 
            val userTeam = if (inviteUser) teamsRepository.getTeamById(userTeamId) else null
            
            val opponent = if (inviteUser && userTeam != null) {
                userTeam
            } else {
                aiTeams.find { it.id != team.id && !existing.any { e -> e.opponentId == it.id } }
            }

            if (opponent == null) continue

            val matchDate = gameDateManager.formatGameDateForDb(currentWeek + 1) + " 16:00"
            
            if (inviteUser) {
                // User receives invitation (logic handled in GameManager/Dashboard)
                requestPreseasonFriendly(team.id, userTeamId, season, matchDate)
                Log.d("AFM_AI", "AI Preseason: ${team.name} invited User (${userTeam?.name}) for a friendly")
            } else {
                // AI vs AI auto-schedule
                val match = PreseasonScheduleEntity(
                    teamId = team.id,
                    teamName = team.name,
                    season = season,
                    matchDate = matchDate,
                    opponentId = opponent.id,
                    opponent = opponent.name,
                    location = "Home",
                    stadium = team.homeStadium,
                    status = PreseasonStatus.SCHEDULED.value,
                    isUserTeam = false,
                    tourLocation = "African Tour"
                )
                preseasonScheduleDao?.insert(match)
                fixturesRepository.createFriendlyFixture(
                    homeTeamId = team.id,
                    homeTeamName = team.name,
                    awayTeamId = opponent.id,
                    awayTeamName = opponent.name,
                    matchDate = matchDate,
                    season = season,
                    stadium = team.homeStadium
                )
                Log.d("AFM_AI", "AI Preseason: ${team.name} vs ${opponent.name} scheduled")
            }
        }
    }

    // ============ USER TEAM (MANAGER'S TEAM) ============
    
    suspend fun generateCustomPreseasonTour(
        userTeamId: Int,
        userTeamName: String,
        season: String,
        selectedTeams: List<TeamsEntity>,
        tourCountry: String
    ): List<PreseasonScheduleEntity> {
        preseasonScheduleDao?.deleteBySeason(season)

        val matches = mutableListOf<PreseasonScheduleEntity>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val seasonYear = season.split("/").first().toInt()
        
        calendar.set(seasonYear, Calendar.JULY, 1, 15, 0)

        selectedTeams.forEachIndexed { index, opponent ->
            val match = PreseasonScheduleEntity(
                teamId = userTeamId,
                teamName = userTeamName,
                season = season,
                matchDate = dateFormat.format(calendar.time),
                opponentId = opponent.id,
                opponent = opponent.name,
                location = "Away",
                stadium = opponent.homeStadium ?: "National Stadium",
                status = PreseasonStatus.SCHEDULED.value,
                isUserTeam = true,
                tourLocation = tourCountry
            )
            matches.add(match)
            calendar.add(Calendar.DAY_OF_YEAR, 4)
        }

        preseasonScheduleDao?.insertAll(matches)

        matches.forEach { match ->
            fixturesRepository.createFriendlyFixture(
                homeTeamId = match.opponentId,
                homeTeamName = match.opponent,
                awayTeamId = match.teamId,
                awayTeamName = match.teamName,
                matchDate = match.matchDate,
                season = season,
                stadium = match.stadium ?: "FAME Africa Stadium"
            )
        }

        return matches
    }

    suspend fun requestPreseasonFriendly(
        requestingTeamId: Int,
        userTeamId: Int,
        season: String,
        proposedDate: String
    ) {
        val requestingTeam = teamsRepository.getTeamById(requestingTeamId) ?: return
        val userTeam = teamsRepository.getTeamById(userTeamId) ?: return

        val match = PreseasonScheduleEntity(
            teamId = userTeamId,
            teamName = userTeam.name,
            season = season,
            matchDate = proposedDate,
            opponentId = requestingTeamId,
            opponent = requestingTeam.name,
            location = "Home",
            stadium = userTeam.homeStadium,
            status = PreseasonStatus.SCHEDULED.value,
            isUserTeam = true,
            tourLocation = "Custom"
        )
        preseasonScheduleDao?.insert(match)
        
        fixturesRepository.createFriendlyFixture(
            homeTeamId = userTeam.id,
            homeTeamName = userTeam.name,
            awayTeamId = requestingTeam.id,
            awayTeamName = requestingTeam.name,
            matchDate = proposedDate,
            season = season,
            stadium = match.stadium ?: "FAME Africa Stadium"
        )
    }

    // ============ MATCH MANAGEMENT ============
    suspend fun completePreseasonMatch(
        matchId: Int,
        homeScore: Int,
        opponentScore: Int
    ): PreseasonScheduleEntity? {
        val match = preseasonScheduleDao?.getById(matchId) ?: return null
        val updated = match.copy(
            status = PreseasonStatus.COMPLETED.value,
            homeScore = homeScore,
            opponentScore = opponentScore
        )
        preseasonScheduleDao?.update(updated)
        return updated
    }

    suspend fun cancelPreseasonMatch(matchId: Int): PreseasonScheduleEntity? {
        val match = preseasonScheduleDao?.getById(matchId) ?: return null
        val updated = match.copy(status = PreseasonStatus.CANCELLED.value)
        preseasonScheduleDao?.update(updated)
        return updated
    }

    // ============ UTILITY ============
    private suspend fun getTeamStadium(teamId: Int): String? {
        return teamsRepository.getTeamById(teamId)?.homeStadium
    }

    suspend fun getPreseasonStats(teamId: Int, season: String): PreseasonStats? =
        preseasonScheduleDao?.getPreseasonStats(teamId, season)

    // ============ DASHBOARD ============
    suspend fun getPreseasonDashboard(teamId: Int, season: String): PreseasonDashboard {
        val allMatches = preseasonScheduleDao?.getTeamPreseasonSchedule(teamId, season)
            ?.firstOrNull() ?: emptyList()

        val scheduled = allMatches.filter { it.status == PreseasonStatus.SCHEDULED.value }
        val completed = allMatches.filter { it.status == PreseasonStatus.COMPLETED.value }

        val wins = completed.count { it.didWin }
        val draws = completed.count { it.isDraw }
        val losses = completed.count { it.didLose }
        val goalsFor = completed.sumOf { it.homeScore ?: 0 }
        val goalsAgainst = completed.sumOf { it.opponentScore ?: 0 }

        return PreseasonDashboard(
            totalMatches = allMatches.size,
            scheduled = scheduled.size,
            completed = completed.size,
            wins = wins,
            draws = draws,
            losses = losses,
            goalsFor = goalsFor,
            goalsAgainst = goalsAgainst,
            goalDifference = goalsFor - goalsAgainst,
            upcomingMatches = scheduled.sortedBy { it.matchDate },
            recentResults = completed.sortedByDescending { it.matchDate }.take(5)
        )
    }
}

data class PreseasonDashboard(
    val totalMatches: Int,
    val scheduled: Int,
    val completed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val upcomingMatches: List<PreseasonScheduleEntity>,
    val recentResults: List<PreseasonScheduleEntity>
)
