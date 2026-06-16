package com.fameafrica.afm.data.repository

import android.util.Log
import com.fameafrica.afm.data.database.dao.BracketWithCupDetails
import com.fameafrica.afm.data.database.dao.BracketWithDetails
import com.fameafrica.afm.data.database.dao.CupBracketsDao
import com.fameafrica.afm.data.database.dao.CupPerformerStats
import com.fameafrica.afm.data.database.dao.CupStatistics
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CupBracketsRepository @Inject constructor(
    private val cupBracketsDaoProvider: Provider<CupBracketsDao>,
    private val cupsRepository: CupsRepository,
    private val teamsRepository: TeamsRepository,
    private val fixturesRepository: FixturesRepository,
    private val leaguesRepository: LeaguesRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository
) {

    private val cupBracketsDao: CupBracketsDao?
        get() = try {
            cupBracketsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllBrackets(): Flow<List<CupBracketsEntity>> = cupBracketsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getBracketById(id: Int): CupBracketsEntity? = cupBracketsDao?.getById(id)

    suspend fun getBracketByFixtureId(fixtureId: Int): CupBracketsEntity? =
        cupBracketsDao?.getByFixtureId(fixtureId)

    suspend fun insertBracket(bracket: CupBracketsEntity) {
        cupBracketsDao?.insert(bracket)
    }

    suspend fun insertAllBrackets(brackets: List<CupBracketsEntity>) {
        cupBracketsDao?.insertAll(brackets)
    }

    suspend fun updateBracket(bracket: CupBracketsEntity) {
        cupBracketsDao?.update(bracket)
    }

    suspend fun deleteBracket(bracket: CupBracketsEntity) {
        cupBracketsDao?.delete(bracket)
    }

    suspend fun deleteByCupAndSeason(cupName: String, season: Int) {
        cupBracketsDao?.deleteByCupAndSeason(cupName, season)
    }

    // ============ CUP-BASED ============

    fun getBracketsByCupAndSeason(cupName: String, season: Int): Flow<List<CupBracketsEntity>> =
        cupBracketsDao?.getBracketsByCupAndSeason(cupName, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getBracketsByRound(cupName: String, season: Int, round: String): Flow<List<CupBracketsEntity>> =
        cupBracketsDao?.getBracketsByRound(cupName, season, round) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTeamBrackets(cupName: String, season: Int, teamName: String): Flow<List<CupBracketsEntity>> =
        cupBracketsDao?.getTeamBrackets(cupName, season, teamName) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getFinalBracket(cupName: String, season: Int): CupBracketsEntity? =
        cupBracketsDao?.getFinalBracket(cupName, season)

    // ============ HELPER ============
    
    private fun formatSeasonString(year: Int): String = "$year/${(year + 1) % 100}"

    // ============ BRACKET GENERATION ============

    /**
     * Get teams to involve in a cup based on rules.
     */
    suspend fun getQualifiedTeamsForCup(cup: CupsEntity, previousYear: Int? = null): List<String> {
        val allTeams = teamsRepository.getAllTeams().firstOrNull() ?: return emptyList()

        return when (cup.name) {
            "Muungano Cup" -> {
                // Rule: Top 4 teams from Tanzania Premier League and Top 4 Teams from Zanzibar Premier League
                val tplTop4 = if (previousYear != null) {
                    leagueStandingsRepository.getStandings("Tanzania Premier League", previousYear)
                        .firstOrNull()?.sortedBy { it.position }?.take(4)?.map { it.teamName }
                } else null
                
                val zplTop4 = if (previousYear != null) {
                    leagueStandingsRepository.getStandings("Zanzibar Premier League", previousYear)
                        .firstOrNull()?.sortedBy { it.position }?.take(4)?.map { it.teamName }
                } else null

                val finalTpl = tplTop4 ?: allTeams.filter { it.league == "Tanzania Premier League" }
                    .sortedByDescending { it.eloRating }.take(4).map { it.name }
                
                val finalZpl = zplTop4 ?: allTeams.filter { it.league == "Zanzibar Premier League" }
                    .sortedByDescending { it.eloRating }.take(4).map { it.name }
                
                (finalTpl + finalZpl).shuffled()
            }
            "CRDB Federations Cup" -> {
                // Rule: 64 clubs from the top 4 divisions in Tanzania.
                allTeams.filter { team ->
                    team.league in listOf(
                        "Tanzania Premier League", 
                        "Tanzania Championship League", 
                        "Tanzania First League", 
                        "Tanzania Regional Champions League"
                    )
                }.sortedByDescending { it.reputation }.take(cup.teamsInvolved).map { it.name }
            }
            "PBZ Cup" -> {
                allTeams.filter { team ->
                    team.league == "Zanzibar Premier League" || team.league == "Zanzibar Championship League"
                }.sortedByDescending { it.reputation }.take(cup.teamsInvolved).map { it.name }
            }
            "FAME Africa™ Cup" -> {
                allTeams.filter { team ->
                    team.league == "Tanzania Regional Division League 2"
                }.sortedByDescending { it.reputation }.take(cup.teamsInvolved).map { it.name }
            }
            else -> {
                if (cup.isDomesticCup) {
                    val countryTeams = allTeams.filter { team ->
                        leaguesRepository.getLeagueByName(team.league ?: "")?.countryId == cup.countryId
                    }
                    countryTeams.sortedByDescending { it.eloRating }.take(cup.teamsInvolved).map { it.name }
                } else {
                    allTeams.filter { it.cupQualification == cup.name }
                        .sortedByDescending { it.eloRating }
                        .take(cup.teamsInvolved)
                        .map { it.name }
                }
            }
        }
    }

    /**
     * Generate knockout bracket for a cup and IMMEDIATELY generate fixtures for the first round.
     */
    suspend fun generateKnockoutBracket(
        cup: CupsEntity,
        season: Int,
        teamNames: List<String>,
        startDate: String,
        isTwoLegged: Boolean = false
    ): List<CupBracketsEntity> {
        val cupName = cup.name
        val cupId = cup.id
        try {
            cupBracketsDao?.deleteByCupAndSeason(cupName, season)

            val numTeams = teamNames.size
            if (numTeams < 2) return emptyList()

            val rounds = calculateRounds(numTeams)
            val currentTeams = teamNames.toMutableList()
            currentTeams.shuffle()

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            calendar.time = dateFormat.parse(startDate) ?: Date()

            val totalSlots = Math.pow(2.0, rounds.toDouble()).toInt()
            while (currentTeams.size < totalSlots) currentTeams.add("BYE")

            val brackets = mutableListOf<CupBracketsEntity>()
            var matchesInRound = totalSlots / 2
            var roundNum = 1
            var bracketPos = 1

            while (matchesInRound >= 1) {
                val roundName = getRoundName(roundNum, rounds)
                repeat(matchesInRound) {
                    val bracket = CupBracketsEntity(
                        cupId = cupId,
                        cupName = cupName,
                        season = season,
                        round = roundName,
                        roundNumber = roundNum,
                        bracketPosition = bracketPos++,
                        isTwoLegged = isTwoLegged && roundNum < rounds,
                        matchDate = dateFormat.format(calendar.time)
                    )
                    brackets.add(bracket)
                }
                calendar.add(Calendar.DAY_OF_YEAR, 14)
                matchesInRound /= 2
                roundNum++
            }

            val firstRoundMatchesCount = totalSlots / 2
            for (i in 0 until firstRoundMatchesCount) {
                val t1 = currentTeams[i * 2]
                val t2 = currentTeams[i * 2 + 1]
                
                val team1Id = if (t1 != "BYE") teamsRepository.getTeamByName(t1)?.id else null
                val team2Id = if (t2 != "BYE") teamsRepository.getTeamByName(t2)?.id else null
                
                brackets[i] = brackets[i].copy(
                    teamId = team1Id,
                    teamName = t1,
                    opponentId = team2Id,
                    opponentName = t2
                )
            }

            cupBracketsDao?.insertAll(brackets)
            linkBracketsInDb(cupName, season)

            // CRITICAL FIX: Generate fixtures for the first round immediately
            val firstRoundName = getRoundName(1, rounds)
            generateCupFixtures(cupName, season, firstRoundName)

            Log.d("AFM_CUP", "Generated $cupName bracket and fixtures for round $firstRoundName")
            return brackets

        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to generate bracket for $cupName: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Renamed from createFixturesForRound for consistency.
     * Ensures fixtures are created in the Fixtures table and linked to the bracket.
     */
    suspend fun generateCupFixtures(
        cupName: String,
        season: Int,
        round: String
    ): List<Int> {
        try {
            val brackets = cupBracketsDao?.getBracketsByRound(cupName, season, round)
                ?.firstOrNull() ?: return emptyList()
            val fixtureIds = mutableListOf<Int>()
            
            // Use consistent season string format "2025/26"
            val seasonString = formatSeasonString(season)

            for (bracket in brackets) {
                if (bracket.fixtureId != null) continue // Skip if already linked

                if (bracket.teamName != null && bracket.opponentName != null && bracket.opponentName != "BYE") {
                    val homeTeam = teamsRepository.getTeamByName(bracket.teamName!!)
                    val awayTeam = teamsRepository.getTeamByName(bracket.opponentName!!)

                    val fixture = fixturesRepository.createCupFixture(
                        homeTeamId = homeTeam?.id ?: 0,
                        homeTeamName = bracket.teamName!!,
                        awayTeamId = awayTeam?.id ?: 0,
                        awayTeamName = bracket.opponentName!!,
                        matchDate = bracket.matchDate ?: "",
                        season = seasonString,
                        cupName = cupName,
                        round = bracket.round ?: round
                    )
                    cupBracketsDao?.update(bracket.copy(fixtureId = fixture.id))
                    fixtureIds.add(fixture.id)
                } else if (bracket.opponentName == "BYE") {
                    // Walkover logic
                    val updated = bracket.copy(winner = bracket.teamName, result = "WALKOVER", isWalkover = true)
                    cupBracketsDao?.update(updated)
                    
                    // Push winner to next round
                    bracket.nextBracketId?.let { nextId ->
                        val next = cupBracketsDao?.getById(nextId)
                        if (next != null) {
                            val isFirst = bracket.bracketPosition % 2 != 0
                            
                            val winnerId = if (bracket.teamName != null) teamsRepository.getTeamByName(bracket.teamName!!)?.id else null
                            
                            cupBracketsDao?.update(
                                if (isFirst) next.copy(teamId = winnerId, teamName = bracket.teamName)
                                else next.copy(opponentId = winnerId, opponentName = bracket.teamName)
                            )
                        }
                    }
                }
            }
            return fixtureIds

        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to generate cup fixtures for $cupName ($round): ${e.message}")
            return emptyList()
        }
    }

    // Keep legacy method for compatibility if needed, but alias to new one
    suspend fun createFixturesForRound(cupName: String, season: Int, round: String) = 
        generateCupFixtures(cupName, season, round)

    private fun calculateRounds(numTeams: Int): Int {
        return when {
            numTeams <= 2 -> 1
            numTeams <= 4 -> 2
            numTeams <= 8 -> 3
            numTeams <= 16 -> 4
            numTeams <= 32 -> 5
            numTeams <= 64 -> 6
            else -> 7
        }
    }

    private fun getRoundName(roundNum: Int, totalRounds: Int): String {
        val teamsRemaining = Math.pow(2.0, (totalRounds - roundNum + 1).toDouble()).toInt()
        return when {
            roundNum == totalRounds -> "FINAL"
            roundNum == totalRounds - 1 -> "SEMI_FINAL"
            roundNum == totalRounds - 2 -> "QUARTER_FINAL"
            roundNum == totalRounds - 3 -> "ROUND_OF_16"
            roundNum == totalRounds - 4 -> "ROUND_OF_32"
            roundNum == totalRounds - 5 -> "ROUND_OF_64"
            else -> "ROUND_OF_$teamsRemaining"
        }
    }

    private suspend fun linkBracketsInDb(cupName: String, season: Int) {
        try {
            val allBrackets = cupBracketsDao?.getBracketsByCupAndSeason(cupName, season)
                ?.firstOrNull() ?: return
            val rounds = allBrackets.groupBy { it.roundNumber }

            for (r in 1 until rounds.size) {
                val currentRound = rounds[r] ?: continue
                val nextRound = rounds[r + 1] ?: continue

                for ((index, bracket) in currentRound.withIndex()) {
                    val nextBracket = nextRound[index / 2]
                    cupBracketsDao?.update(bracket.copy(nextBracketId = nextBracket.id))
                }
            }
        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to link brackets: ${e.message}")
        }
    }

    suspend fun generateGroupPlusKnockoutBracket(
        cup: CupsEntity,
        season: Int,
        groupCount: Int,
        startDate: String
    ): List<CupBracketsEntity> {
        val knockoutTeams = mutableListOf<String>()
        for (i in 0 until groupCount step 2) {
            if (i + 1 < groupCount) {
                knockoutTeams.add("Winner Group ${'A' + i}")
                knockoutTeams.add("Runner-up Group ${'A' + i + 1}")
                knockoutTeams.add("Winner Group ${'A' + i + 1}")
                knockoutTeams.add("Runner-up Group ${'A' + i}")
            } else {
                knockoutTeams.add("Winner Group ${'A' + i}")
                knockoutTeams.add("Runner-up Group ${'A' + i}")
            }
        }
        return generateKnockoutBracket(cup, season, knockoutTeams, startDate, true)
    }

    suspend fun updateBracketAfterMatch(
        fixtureId: Int,
        homeScore: Int,
        awayScore: Int,
        winner: String,
        loser: String,
        penaltyScore: String? = null,
        aggregateScore: String? = null
    ): CupBracketsEntity? {
        try {
            val bracket = cupBracketsDao?.getByFixtureId(fixtureId) ?: return null

            val resultStr = if (penaltyScore != null) "$homeScore-$awayScore ($penaltyScore)" else "$homeScore-$awayScore"

            val winnerId = teamsRepository.getTeamByName(winner)?.id
            val loserId = teamsRepository.getTeamByName(loser)?.id

            val updatedBracket = bracket.copy(
                result = resultStr,
                homeScore = homeScore,
                awayScore = awayScore,
                penaltyScore = penaltyScore,
                aggregateScore = aggregateScore,
                winner = winner,
                winnerId = winnerId,
                loser = loser,
                loserId = loserId
            )

            cupBracketsDao?.update(updatedBracket)

            bracket.nextBracketId?.let { nextId ->
                val nextBracket = cupBracketsDao?.getById(nextId) ?: return@let
                val isFirstSlot = bracket.bracketPosition % 2 != 0
                val linkedBracket = if (isFirstSlot) nextBracket.copy(teamId = winnerId, teamName = winner) else nextBracket.copy(opponentId = winnerId, opponentName = winner)
                cupBracketsDao?.update(linkedBracket)
                
                // If both participants in the next bracket are now known, generate its fixture
                if (linkedBracket.teamName != null && linkedBracket.opponentName != null && 
                    !linkedBracket.teamName!!.contains("Winner") && !linkedBracket.opponentName!!.contains("Winner") &&
                    linkedBracket.teamName != "TBD" && linkedBracket.opponentName != "TBD") {
                    generateCupFixtures(bracket.cupName ?: "", bracket.season, linkedBracket.round ?: "")
                }
            }

            return updatedBracket

        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to update bracket after match: ${e.message}")
            return null
        }
    }

    fun getCupStatistics(season: Int): Flow<List<CupStatistics>> = cupBracketsDao?.getCupStatistics(season) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    fun getTopCupPerformers(limit: Int): Flow<List<CupPerformerStats>> = cupBracketsDao?.getTopCupPerformers(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun getBracketWithDetails(bracketId: Int): BracketWithDetails? = cupBracketsDao?.getBracketWithDetails(bracketId)
    fun getFullBracketWithDetails(cupName: String, season: Int): Flow<List<BracketWithCupDetails>> = cupBracketsDao?.getFullBracketWithDetails(cupName, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())
}
