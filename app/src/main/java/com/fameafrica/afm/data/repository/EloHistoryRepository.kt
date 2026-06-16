package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.EloHistoryDao
import com.fameafrica.afm.data.database.dao.EloRanking
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.entities.EloHistoryEntity
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import com.fameafrica.afm.domain.EloCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class EloHistoryRepository @Inject constructor(
    private val eloHistoryDaoProvider: Provider<EloHistoryDao>,
    private val teamsDaoProvider: Provider<TeamsDao>
) {

    private val eloHistoryDao: EloHistoryDao?
        get() = try {
            eloHistoryDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    private val teamsDao: TeamsDao?
        get() = try {
            teamsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllEloHistory(): Flow<List<EloHistoryEntity>> = eloHistoryDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getEloByTeamName(teamName: String): EloHistoryEntity? =
        eloHistoryDao?.getByTeamName(teamName)

    suspend fun getCurrentElo(teamName: String): Int {
        // First try to get from elo_history
        val history = eloHistoryDao?.getByTeamName(teamName)
        if (history != null) return history.currentElo

        // Fallback to teams table
        return teamsDao?.getByName(teamName)?.eloRating ?: 1500
    }

    suspend fun insertEloHistory(eloHistory: EloHistoryEntity) {
        eloHistoryDao?.insert(eloHistory)
    }

    suspend fun updateEloHistory(eloHistory: EloHistoryEntity) {
        eloHistoryDao?.update(eloHistory)
    }

    suspend fun deleteEloHistory(eloHistory: EloHistoryEntity) {
        eloHistoryDao?.delete(eloHistory)
    }

    // ============ INITIALIZATION ============

    /**
     * Initialize Elo history for all teams using their prepopulated elo_rating from teams table
     * This ensures the base ratings come from your prepopulated data
     */
    suspend fun initializeAllElo() {
        val teams = teamsDao?.getAll()?.firstOrNull() ?: return

        for (team in teams) {
            // Check if already exists in elo_history
            val existing = eloHistoryDao?.getByTeamName(team.name)
            if (existing != null) {
                // Ensure elo_history matches teams table (in case of desync)
                if (existing.currentElo != team.eloRating) {
                    val updatedHistory = existing.copy(
                        currentElo = team.eloRating,
                        previousElo = team.eloRating,
                        highestElo = maxOf(existing.highestElo ?: team.eloRating, team.eloRating),
                        lowestElo = minOf(existing.lowestElo ?: team.eloRating, team.eloRating),
                        lastUpdated = getCurrentDateTime()
                    )
                    eloHistoryDao?.update(updatedHistory)
                }
                continue
            }

            // Create new history entry using team's prepopulated elo_rating
            val initialElo = team.eloRating

            val eloHistory = EloHistoryEntity(
                teamId = team.id,
                teamName = team.name,
                currentElo = initialElo,
                previousElo = initialElo,
                highestElo = initialElo,
                lowestElo = initialElo,
                matchesPlayedElo = 0,
                eloChangeTotal = 0,
                lastUpdated = getCurrentDateTime()
            )

            eloHistoryDao?.insert(eloHistory)

            // Note: We DON'T update teams table here because it's already prepopulated
            // We just create the history record to track changes
        }
    }

    /**
     * Force sync elo_history with teams table
     * Use this if you suspect desynchronization
     */
    suspend fun syncWithTeamsTable() {
        val teams = teamsDao?.getAll()?.firstOrNull() ?: return
        val histories = eloHistoryDao?.getAll()?.firstOrNull() ?: emptyList()

        for (team in teams) {
            val history = histories.find { it.teamName == team.name }
            if (history == null) {
                // Missing history - create it
                val eloHistory = EloHistoryEntity(
                    teamId = team.id,
                    teamName = team.name,
                    currentElo = team.eloRating,
                    previousElo = team.eloRating,
                    highestElo = team.eloRating,
                    lowestElo = team.eloRating,
                    matchesPlayedElo = 0,
                    eloChangeTotal = 0,
                    lastUpdated = getCurrentDateTime()
                )
                eloHistoryDao?.insert(eloHistory)
            } else if (history.currentElo != team.eloRating) {
                // Desynchronized - update history to match team
                val updatedHistory = history.copy(
                    currentElo = team.eloRating,
                    previousElo = team.eloRating,
                    highestElo = maxOf(history.highestElo ?: team.eloRating, team.eloRating),
                    lowestElo = minOf(history.lowestElo ?: team.eloRating, team.eloRating),
                    lastUpdated = getCurrentDateTime()
                )
                eloHistoryDao?.update(updatedHistory)
            }
        }
    }

    // ============ MATCH PROCESSING ============

    /**
     * Process Elo changes after a match
     * Called automatically by the match engine after each matchday
     */
    suspend fun processMatchResult(result: FixturesResultsEntity) {
        // Get current Elo ratings from teams table (source of truth)
        val homeTeam = teamsDao?.getByName(result.homeTeam)
        val awayTeam = teamsDao?.getByName(result.awayTeam)

        if (homeTeam == null || awayTeam == null) return

        val homeElo = homeTeam.eloRating
        val awayElo = awayTeam.eloRating

        // Also get history records for tracking changes
        val homeEloHistory = eloHistoryDao?.getByTeamName(result.homeTeam)
        val awayEloHistory = eloHistoryDao?.getByTeamName(result.awayTeam)

        // Determine match type for K-factor
        val matchType = when {
            result.matchType == "Derby" -> "DERBY"
            result.matchType == "Cup" && result.cupRound == "Final" -> "FINAL"
            result.matchType == "Cup" -> "CUP"
            else -> "LEAGUE"
        }

        // Calculate new Elo ratings
        val (newHomeElo, newAwayElo) = EloCalculator.calculateNewRatings(
            homeTeam = homeElo,
            awayTeam = awayElo,
            homeScore = result.homeScore,
            awayScore = result.awayScore,
            matchType = matchType
        )

        val now = getCurrentDateTime()
        val homeResult = when {
            result.homeTeamWin -> "WIN"
            result.awayTeamWin -> "LOSS"
            else -> "DRAW"
        }
        val awayResult = when {
            result.awayTeamWin -> "WIN"
            result.homeTeamWin -> "LOSS"
            else -> "DRAW"
        }

        // 1. Update teams table (source of truth for current Elo)
        val updatedHomeTeam = homeTeam.copy(eloRating = newHomeElo)
        val updatedAwayTeam = awayTeam.copy(eloRating = newAwayElo)
        teamsDao?.update(updatedHomeTeam)
        teamsDao?.update(updatedAwayTeam)

        // 2. Update or create elo_history records
        updateOrCreateEloHistory(
            teamId = homeTeam.id,
            teamName = result.homeTeam,
            oldElo = homeElo,
            newElo = newHomeElo,
            lastUpdated = now,
            lastResult = homeResult,
            opponent = result.awayTeam,
            opponentElo = awayElo,
            existingHistory = homeEloHistory
        )

        updateOrCreateEloHistory(
            teamId = awayTeam.id,
            teamName = result.awayTeam,
            oldElo = awayElo,
            newElo = newAwayElo,
            lastUpdated = now,
            lastResult = awayResult,
            opponent = result.homeTeam,
            opponentElo = homeElo,
            existingHistory = awayEloHistory
        )
    }

    private suspend fun updateOrCreateEloHistory(
        teamId: Int,
        teamName: String,
        oldElo: Int,
        newElo: Int,
        lastUpdated: String,
        lastResult: String,
        opponent: String,
        opponentElo: Int,
        existingHistory: EloHistoryEntity?
    ) {
        if (existingHistory == null) {
            // Create new history entry
            val newHistory = EloHistoryEntity(
                teamId = teamId,
                teamName = teamName,
                currentElo = newElo,
                previousElo = oldElo,
                highestElo = newElo,
                lowestElo = newElo,
                matchesPlayedElo = 1,
                eloChangeTotal = newElo - oldElo,
                lastUpdated = lastUpdated,
                lastMatchResult = lastResult,
                lastOpponent = opponent,
                lastOpponentElo = opponentElo
            )
            eloHistoryDao?.insert(newHistory)
        } else {
            // Update existing history
            val newMatchesPlayed = existingHistory.matchesPlayedElo + 1
            val newEloChangeTotal = existingHistory.eloChangeTotal + (newElo - oldElo)
            val newHighestElo = maxOf(existingHistory.highestElo ?: newElo, newElo)
            val newLowestElo = minOf(existingHistory.lowestElo ?: newElo, newElo)

            val updatedHistory = existingHistory.copy(
                currentElo = newElo,
                previousElo = oldElo,
                highestElo = newHighestElo,
                lowestElo = newLowestElo,
                matchesPlayedElo = newMatchesPlayed,
                eloChangeTotal = newEloChangeTotal,
                lastUpdated = lastUpdated,
                lastMatchResult = lastResult,
                lastOpponent = opponent,
                lastOpponentElo = opponentElo
            )
            eloHistoryDao?.update(updatedHistory)
        }
    }

    /**
     * Batch process multiple match results (for matchday processing)
     */
    suspend fun processMatchdayResults(results: List<FixturesResultsEntity>) {
        for (result in results) {
            processMatchResult(result)
        }
    }

    // ============ ELO RANKINGS ============

    fun getTopEloTeams(limit: Int = 10): Flow<List<EloHistoryEntity>> =
        eloHistoryDao?.getTopEloTeams(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getBottomEloTeams(limit: Int = 10): Flow<List<EloHistoryEntity>> =
        eloHistoryDao?.getBottomEloTeams(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getEloRankings(): Flow<List<EloRanking>> = eloHistoryDao?.getEloRankings() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getAverageElo(): Double? = eloHistoryDao?.getAverageElo()

    suspend fun getHighestElo(): Int? = eloHistoryDao?.getHighestElo()

    suspend fun getLowestElo(): Int? = eloHistoryDao?.getLowestElo()

    fun getBiggestEloGainers(limit: Int = 5): Flow<List<EloHistoryEntity>> =
        eloHistoryDao?.getBiggestEloGainers(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getBiggestEloLosers(limit: Int = 5): Flow<List<EloHistoryEntity>> =
        eloHistoryDao?.getBiggestEloLosers(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ TEAM PERFORMANCE ============

    suspend fun getTeamEloRank(teamName: String): Int? {
        val rankings = eloHistoryDao?.getEloRankings()?.firstOrNull() ?: return null
        return rankings.indexOfFirst { it.teamName == teamName }.takeIf { it >= 0 }?.plus(1)
    }

    suspend fun getTeamsAboveElo(threshold: Int): Int =
        eloHistoryDao?.getTeamsAboveElo(threshold) ?: 0

    suspend fun getTeamsBelowElo(threshold: Int): Int =
        eloHistoryDao?.getTeamsBelowElo(threshold) ?: 0

    // ============ ELO PREDICTIONS ============

    /**
     * Predict match outcome based on Elo ratings from teams table
     */
    suspend fun predictMatchOutcome(homeTeam: String, awayTeam: String): MatchPrediction? {
        val home = teamsDao?.getByName(homeTeam)
        val away = teamsDao?.getByName(awayTeam)

        if (home == null || away == null) return null

        val homeElo = home.eloRating
        val awayElo = away.eloRating

        val homeWinProb = EloCalculator.calculateWinProbability(homeElo, awayElo, true)
        val awayWinProb = EloCalculator.calculateWinProbability(awayElo, homeElo, false)
        val drawProb = 1.0 - (homeWinProb + awayWinProb)

        val expectedHomeScore = homeWinProb * 3  // Rough estimate
        val expectedAwayScore = awayWinProb * 3

        return MatchPrediction(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeWinProbability = homeWinProb,
            awayWinProbability = awayWinProb,
            drawProbability = drawProb,
            expectedHomeScore = expectedHomeScore,
            expectedAwayScore = expectedAwayScore,
            predictedWinner = when {
                homeWinProb > awayWinProb + 0.1 -> homeTeam
                awayWinProb > homeWinProb + 0.1 -> awayTeam
                else -> "Draw"
            }
        )
    }

    // ============ UTILITY ============

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // ============ DASHBOARD ============

    suspend fun getEloDashboard(): EloDashboard {
        val allElo = eloHistoryDao?.getAll()?.firstOrNull() ?: emptyList()
        val top10 = eloHistoryDao?.getTopEloTeams(10)?.firstOrNull() ?: emptyList()
        val gainers = eloHistoryDao?.getBiggestEloGainers(5)?.firstOrNull() ?: emptyList()
        val losers = eloHistoryDao?.getBiggestEloLosers(5)?.firstOrNull() ?: emptyList()

        val averageElo = eloHistoryDao?.getAverageElo() ?: 1500.0
        val highestElo = eloHistoryDao?.getHighestElo() ?: 1500
        val lowestElo = eloHistoryDao?.getLowestElo() ?: 1500

        val teamsAbove1700 = eloHistoryDao?.getTeamsAboveElo(1700) ?: 0
        val teamsAbove1600 = eloHistoryDao?.getTeamsAboveElo(1600) ?: 0
        val teamsAbove1500 = eloHistoryDao?.getTeamsAboveElo(1500) ?: 0

        return EloDashboard(
            totalTeams = allElo.size,
            top10Teams = top10,
            biggestGainers = gainers,
            biggestLosers = losers,
            averageElo = averageElo,
            highestElo = highestElo,
            lowestElo = lowestElo,
            teamsAbove1700 = teamsAbove1700,
            teamsAbove1600 = teamsAbove1600,
            teamsAbove1500 = teamsAbove1500,
            strongestTeam = top10.firstOrNull()?.teamName,
            strongestTeamElo = top10.firstOrNull()?.currentElo
        )
    }

    suspend fun getTeamEloDashboard(teamName: String): TeamEloDashboard? {
        val elo = eloHistoryDao?.getByTeamName(teamName) ?: return null
        val rank = getTeamEloRank(teamName)

        return TeamEloDashboard(
            teamName = elo.teamName,
            currentElo = elo.currentElo,
            previousElo = elo.previousElo,
            eloChange = elo.eloChange,
            eloTrend = elo.eloTrend,
            eloCategory = elo.eloCategory,
            highestElo = elo.highestElo,
            lowestElo = elo.lowestElo,
            matchesPlayedElo = elo.matchesPlayedElo,
            eloChangeTotal = elo.eloChangeTotal,
            rank = rank,
            lastMatchResult = elo.lastMatchResult,
            lastOpponent = elo.lastOpponent,
            lastOpponentElo = elo.lastOpponentElo,
            lastUpdated = elo.lastUpdated
        )
    }
}

// ============ DATA CLASSES ============

data class MatchPrediction(
    val homeTeam: String,
    val awayTeam: String,
    val homeWinProbability: Double,
    val awayWinProbability: Double,
    val drawProbability: Double,
    val expectedHomeScore: Double,
    val expectedAwayScore: Double,
    val predictedWinner: String
)

data class EloDashboard(
    val totalTeams: Int,
    val top10Teams: List<EloHistoryEntity>,
    val biggestGainers: List<EloHistoryEntity>,
    val biggestLosers: List<EloHistoryEntity>,
    val averageElo: Double,
    val highestElo: Int,
    val lowestElo: Int,
    val teamsAbove1700: Int,
    val teamsAbove1600: Int,
    val teamsAbove1500: Int,
    val strongestTeam: String?,
    val strongestTeamElo: Int?
)

data class TeamEloDashboard(
    val teamName: String,
    val currentElo: Int,
    val previousElo: Int,
    val eloChange: Int,
    val eloTrend: String,
    val eloCategory: String,
    val highestElo: Int?,
    val lowestElo: Int?,
    val matchesPlayedElo: Int,
    val eloChangeTotal: Int,
    val rank: Int?,
    val lastMatchResult: String?,
    val lastOpponent: String?,
    val lastOpponentElo: Int?,
    val lastUpdated: String?
)
