package com.fameafrica.afm.data.repository

import android.util.Log
import com.fameafrica.afm.data.database.dao.CupGroupStandingsDao
import com.fameafrica.afm.data.database.dao.CupGroupStatistics
import com.fameafrica.afm.data.database.dao.FullGroupStandingEntry
import com.fameafrica.afm.data.database.dao.GroupWinsStats
import com.fameafrica.afm.data.database.entities.CupGroupStandingsEntity
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CupGroupStandingsRepository @Inject constructor(
    private val cupGroupStandingsDaoProvider: Provider<CupGroupStandingsDao>,
    private val teamsRepository: TeamsRepository
) {

    private val cupGroupStandingsDao: CupGroupStandingsDao?
        get() = try {
            cupGroupStandingsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllStandings(): Flow<List<CupGroupStandingsEntity>> = cupGroupStandingsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getStandingById(id: Int): CupGroupStandingsEntity? = cupGroupStandingsDao?.getById(id)

    suspend fun getTeamStanding(teamId: Int, cupName: String, seasonYear: Int): CupGroupStandingsEntity? =
        cupGroupStandingsDao?.getTeamStanding(teamId, cupName, seasonYear)

    suspend fun insertStanding(standing: CupGroupStandingsEntity) {
        cupGroupStandingsDao?.insert(standing)
    }

    suspend fun insertAllStandings(standings: List<CupGroupStandingsEntity>) {
        cupGroupStandingsDao?.insertAll(standings)
    }

    suspend fun updateStanding(standing: CupGroupStandingsEntity) {
        cupGroupStandingsDao?.update(standing)
    }

    suspend fun deleteStanding(standing: CupGroupStandingsEntity) {
        cupGroupStandingsDao?.delete(standing)
    }

    suspend fun deleteByCupAndSeason(cupName: String, seasonYear: Int) {
        cupGroupStandingsDao?.deleteByCupAndSeason(cupName, seasonYear)
    }

    // ============ GROUP STANDINGS QUERIES ============

    fun getAllGroupStandings(cupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>> =
        cupGroupStandingsDao?.getAllGroupStandings(cupName, seasonYear) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getGroupStandings(cupName: String, groupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>> =
        cupGroupStandingsDao?.getGroupStandings(cupName, groupName, seasonYear) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getStandingsByPosition(cupName: String, groupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>> =
        cupGroupStandingsDao?.getStandingsByPosition(cupName, groupName, seasonYear) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getQualifiedTeams(cupName: String, groupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>> =
        cupGroupStandingsDao?.getQualifiedTeams(cupName, groupName, seasonYear) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getGroupWinner(cupName: String, groupName: String, seasonYear: Int): CupGroupStandingsEntity? =
        cupGroupStandingsDao?.getGroupWinner(cupName, groupName, seasonYear)

    suspend fun getTeamPosition(cupName: String, seasonYear: Int, teamId: Int): CupGroupStandingsEntity? =
        cupGroupStandingsDao?.getTeamPosition(cupName, seasonYear, teamId)

    // ============ TEAM HISTORY ============

    fun getTeamCupHistory(teamId: Int): Flow<List<CupGroupStandingsEntity>> =
        cupGroupStandingsDao?.getTeamCupHistory(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTeamGroupWins(teamId: Int): Flow<List<CupGroupStandingsEntity>> =
        cupGroupStandingsDao?.getTeamGroupWins(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ STATISTICS ============

    suspend fun getCupGroupStatistics(cupName: String, groupName: String, seasonYear: Int): CupGroupStatistics? =
        cupGroupStandingsDao?.getCupGroupStatistics(cupName, groupName, seasonYear)

    fun getMostGroupWins(cupName: String): Flow<List<GroupWinsStats>> =
        cupGroupStandingsDao?.getMostGroupWins(cupName) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ JOIN QUERIES ============

    fun getFullGroupStandings(cupName: String, groupName: String, seasonYear: Int): Flow<List<FullGroupStandingEntry>> =
        cupGroupStandingsDao?.getFullGroupStandings(cupName, groupName, seasonYear) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ STANDINGS MANAGEMENT ============

    /**
     * Initialize cup group standings for a new season with safe insertion
     */
    suspend fun initializeGroup(
        cupId: Int,
        cupName: String,
        groupName: String,
        seasonYear: Int,
        teamNames: List<String>
    ): List<CupGroupStandingsEntity> {
        // Delete existing entries first to avoid foreign key conflicts
        try {
            cupGroupStandingsDao?.deleteByGroup(cupName, groupName, seasonYear)
        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to delete existing group standings: ${e.message}")
        }

        val standings = teamNames.mapIndexed { index, teamName ->
            val teamId = teamsRepository.getTeamByName(teamName)?.id ?: 0
            CupGroupStandingsEntity(
                cupId = cupId,
                cupName = cupName,
                groupName = groupName,
                seasonYear = seasonYear,
                position = index + 1,
                teamId = teamId,
                teamName = teamName,
                matchesPlayed = 0,
                wins = 0,
                draws = 0,
                losses = 0,
                goalsScored = 0,
                goalsConceded = 0,
                goalDifference = 0,
                points = 0,
                form = null
            )
        }

        return try {
            cupGroupStandingsDao?.insertAll(standings)
            Log.d("AFM_CUP", "Successfully initialized group $groupName for $cupName with ${standings.size} teams")
            standings
        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to initialize group $groupName for $cupName: ${e.message}")
            // Try inserting one by one
            standings.forEach { standing ->
                try {
                    cupGroupStandingsDao?.insert(standing)
                } catch (inner: Exception) {
                    Log.e("AFM_CUP", "Failed to insert team ${standing.teamName}: ${inner.message}")
                }
            }
            standings
        }
    }

    /**
     * Update group standings after a match result
     */
    suspend fun updateGroupStandingsAfterMatch(result: FixturesResultsEntity) {
        // Only process cup group stage matches
        if (result.cupName == null || result.cupRound?.contains("Group") != true) return

        val seasonYear = result.season.split("/").first().toInt()
        val groupName = result.cupRound.substringAfter("Group ").trim()

        try {
            // Update home team
            val homeStanding = cupGroupStandingsDao?.getTeamStanding(
                teamId = result.homeTeamId,
                cupName = result.cupName,
                seasonYear = seasonYear
            )

            homeStanding?.let { standing ->
                val isWin = result.homeTeamWin
                val isDraw = result.isDraw
                val isLoss = result.awayTeamWin

                val updatedStanding = standing.updateFromMatchResult(
                    goalsFor = result.homeScore,
                    goalsAgainst = result.awayScore,
                    isWin = isWin,
                    isDraw = isDraw,
                    isLoss = isLoss
                )
                cupGroupStandingsDao?.update(updatedStanding)
            }

            // Update away team
            val awayStanding = cupGroupStandingsDao?.getTeamStanding(
                teamId = result.awayTeamId,
                cupName = result.cupName,
                seasonYear = seasonYear
            )

            awayStanding?.let { standing ->
                val isWin = result.awayTeamWin
                val isDraw = result.isDraw
                val isLoss = result.homeTeamWin

                val updatedStanding = standing.updateFromMatchResult(
                    goalsFor = result.awayScore,
                    goalsAgainst = result.homeScore,
                    isWin = isWin,
                    isDraw = isDraw,
                    isLoss = isLoss
                )
                cupGroupStandingsDao?.update(updatedStanding)
            }

            // Recalculate positions
            recalculateGroupPositions(result.cupName, groupName, seasonYear)

        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to update group standings: ${e.message}")
        }
    }

    /**
     * Recalculate all positions in the group based on points, GD, goals scored
     */
    suspend fun recalculateGroupPositions(cupName: String, groupName: String, seasonYear: Int) {
        try {
            val standings = cupGroupStandingsDao?.getGroupStandings(cupName, groupName, seasonYear)
                ?.firstOrNull() ?: return

            // Sort by points, then goal difference, then goals scored
            val sortedStandings = standings.sortedWith(
                compareByDescending<CupGroupStandingsEntity> { it.points }
                    .thenByDescending { it.goalDifference }
                    .thenByDescending { it.goalsScored }
            )

            // Update positions
            sortedStandings.forEachIndexed { index, standing ->
                if (standing.position != index + 1) {
                    val updatedStanding = standing.updatePosition(index + 1)
                    cupGroupStandingsDao?.update(updatedStanding)
                }
            }
        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to recalculate group positions: ${e.message}")
        }
    }

    /**
     * Process qualification at end of group stage
     */
    suspend fun processGroupStageEnd(cupName: String, groupName: String, seasonYear: Int): GroupStageResult {
        val standings = cupGroupStandingsDao?.getStandingsByPosition(cupName, groupName, seasonYear)
            ?.firstOrNull() ?: return GroupStageResult(emptyList(), emptyList())

        val qualified = standings.filter { it.position <= 2 }
        val eliminated = standings.filter { it.position > 2 }

        return GroupStageResult(qualified, eliminated)
    }
}

// ============ DATA CLASSES ============

data class GroupStageResult(
    val qualifiedTeams: List<CupGroupStandingsEntity>,
    val eliminatedTeams: List<CupGroupStandingsEntity>
)
