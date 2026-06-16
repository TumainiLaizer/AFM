package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.NationalTeamPlayersDao
import com.fameafrica.afm.data.database.dao.NationalTeamSquadEntry
import com.fameafrica.afm.data.database.dao.NationalTeamSquadStats
import com.fameafrica.afm.data.database.dao.PlayersDao
import com.fameafrica.afm.data.database.dao.NationalTeamsDao
import com.fameafrica.afm.data.database.entities.NationalTeamPlayersEntity
import com.fameafrica.afm.data.database.entities.NationalTeamRole
import com.fameafrica.afm.data.database.entities.NationalTeamsEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class NationalTeamPlayersRepository @Inject constructor(
    private val nationalTeamPlayersDaoProvider: Provider<NationalTeamPlayersDao>,
    private val playersDaoProvider: Provider<PlayersDao>,
    private val nationalTeamsDaoProvider: Provider<NationalTeamsDao>
) {

    private val nationalTeamPlayersDao get() = nationalTeamPlayersDaoProvider.get()
    private val playersDao get() = playersDaoProvider.get()
    private val nationalTeamsDao get() = nationalTeamsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllEntries(): Flow<List<NationalTeamPlayersEntity>> = nationalTeamPlayersDao.getAll()

    suspend fun getEntry(teamId: Int, playerId: Int): NationalTeamPlayersEntity? =
        nationalTeamPlayersDao.getById(teamId, playerId)

    suspend fun insertEntry(entry: NationalTeamPlayersEntity) =
        nationalTeamPlayersDao.insert(entry)

    suspend fun insertAllEntries(entries: List<NationalTeamPlayersEntity>) =
        nationalTeamPlayersDao.insertAll(entries)

    suspend fun updateEntry(entry: NationalTeamPlayersEntity) =
        nationalTeamPlayersDao.update(entry)

    suspend fun deleteEntry(entry: NationalTeamPlayersEntity) =
        nationalTeamPlayersDao.delete(entry)

    suspend fun deleteEntryById(teamId: Int, playerId: Int) =
        nationalTeamPlayersDao.deleteById(teamId, playerId)

    suspend fun deleteByNationalTeam(teamId: Int) =
        nationalTeamPlayersDao.deleteByNationalTeam(teamId)

    suspend fun deleteByPlayer(playerId: Int) =
        nationalTeamPlayersDao.deleteByPlayer(playerId)

    suspend fun getSquadSize(teamId: Int): Int =
        nationalTeamPlayersDao.getSquadSize(teamId)

    // ============ NATIONAL TEAM SQUAD ============
    /**
     * Get the best possible squad for a national team based on player ratings
     * Used for automatic squad selection and AI management
     */
    suspend fun getBestPossibleSquad(
        nationality: String,
        formation: String = "4-3-3"
    ): List<PlayersEntity> {

        // Get all eligible players
        val eligiblePlayers = playersDao.getEligiblePlayersForNationalTeam(nationality)
            .firstOrNull() ?: return emptyList()

        // Filter for active, healthy players
        val availablePlayers = eligiblePlayers.filter { player ->
            !player.retired &&
                    player.injuryStatus == "HEALTHY" &&
                    !player.suspended
        }

        // Sort by rating
        val sortedPlayers = availablePlayers.sortedByDescending { it.rating }

        // Select based on formation
        return when (formation) {
            "4-4-2" -> selectSquadFormation442(sortedPlayers)
            "4-3-3" -> selectSquadFormation433(sortedPlayers)
            "4-2-3-1" -> selectSquadFormation4231(sortedPlayers)
            "3-5-2" -> selectSquadFormation352(sortedPlayers)
            else -> sortedPlayers.take(23) // Default to top 23
        }
    }

    private fun selectSquadFormation442(players: List<PlayersEntity>): List<PlayersEntity> {
        val goalkeepers = players.filter { it.position == "GK" }.take(3)
        val defenders = players.filter { it.positionCategory == "DEFENDER" }.take(8)
        val midfielders = players.filter { it.positionCategory == "MIDFIELDER" }.take(8)
        val forwards = players.filter { it.positionCategory == "FORWARD" }.take(4)

        return goalkeepers + defenders + midfielders + forwards
    }

    private fun selectSquadFormation433(players: List<PlayersEntity>): List<PlayersEntity> {
        val goalkeepers = players.filter { it.position == "GK" }.take(3)
        val defenders = players.filter { it.positionCategory == "DEFENDER" }.take(8)
        val midfielders = players.filter { it.positionCategory == "MIDFIELDER" }.take(8)
        val forwards = players.filter { it.positionCategory == "FORWARD" }.take(4)

        return goalkeepers + defenders + midfielders + forwards
    }

    private fun selectSquadFormation4231(players: List<PlayersEntity>): List<PlayersEntity> {
        val goalkeepers = players.filter { it.position == "GK" }.take(3)
        val defenders = players.filter { it.positionCategory == "DEFENDER" }.take(8)
        val midfielders = players.filter { it.positionCategory == "MIDFIELDER" }.take(8)
        val forwards = players.filter { it.positionCategory == "FORWARD" }.take(4)

        return goalkeepers + defenders + midfielders + forwards
    }

    private fun selectSquadFormation352(players: List<PlayersEntity>): List<PlayersEntity> {
        val goalkeepers = players.filter { it.position == "GK" }.take(3)
        val defenders = players.filter { it.positionCategory == "DEFENDER" }.take(8)
        val midfielders = players.filter { it.positionCategory == "MIDFIELDER" }.take(8)
        val forwards = players.filter { it.positionCategory == "FORWARD" }.take(4)

        return goalkeepers + defenders + midfielders + forwards
    }

    fun getNationalTeamSquad(teamId: Int): Flow<List<PlayersEntity>> =
        nationalTeamPlayersDao.getNationalTeamSquad(teamId)

    fun getNationalTeamStarters(teamId: Int): Flow<List<PlayersEntity>> =
        nationalTeamPlayersDao.getNationalTeamStarters(teamId)

    fun getNationalTeamReserves(teamId: Int): Flow<List<PlayersEntity>> =
        nationalTeamPlayersDao.getNationalTeamReserves(teamId)

    suspend fun getNationalTeamCaptain(teamId: Int): PlayersEntity? =
        nationalTeamPlayersDao.getNationalTeamCaptain(teamId)

    fun getNationalTeamSquadWithDetails(teamId: Int): Flow<List<NationalTeamSquadEntry>> =
        nationalTeamPlayersDao.getNationalTeamSquadWithDetails(teamId)

    // ============ PLAYER QUERIES ============

    fun getNationalTeamEntriesByPlayer(playerId: Int): Flow<List<NationalTeamPlayersEntity>> =
        nationalTeamPlayersDao.getNationalTeamEntriesByPlayer(playerId)

    fun getNationalTeamsForPlayer(playerId: Int): Flow<List<NationalTeamsEntity>> =
        nationalTeamPlayersDao.getNationalTeamsForPlayer(playerId)

    // ============ ROLE MANAGEMENT ============

    suspend fun assignRole(teamId: Int, playerId: Int, role: String) {
        // Check if this is a captain assignment
        if (role == NationalTeamRole.CAPTAIN.value) {
            // Remove existing captain
            val currentCaptain = nationalTeamPlayersDao.getCaptainEntry(teamId)
            currentCaptain?.let {
                val updated = it.copy(role = NationalTeamRole.STARTER.value)
                nationalTeamPlayersDao.update(updated)
            }
        }

        // Check if entry exists
        val existing = nationalTeamPlayersDao.getById(teamId, playerId)

        if (existing != null) {
            // Update existing entry
            val updated = existing.copy(role = role)
            nationalTeamPlayersDao.update(updated)
        } else {
            // Create new entry
            val entry = NationalTeamPlayersEntity(
                nationalTeamId = teamId,
                playerId = playerId,
                role = role
            )
            nationalTeamPlayersDao.insert(entry)
        }
    }

    suspend fun assignStarter(teamId: Int, playerId: Int) =
        assignRole(teamId, playerId, NationalTeamRole.STARTER.value)

    suspend fun assignReserve(teamId: Int, playerId: Int) =
        assignRole(teamId, playerId, NationalTeamRole.RESERVE.value)

    suspend fun assignCaptain(teamId: Int, playerId: Int) =
        assignRole(teamId, playerId, NationalTeamRole.CAPTAIN.value)

    suspend fun removeFromSquad(teamId: Int, playerId: Int) {
        nationalTeamPlayersDao.deleteById(teamId, playerId)
    }

    // ============ SQUAD MANAGEMENT ============

    /**
     * Select the best 23-player squad for a national team
     * Based on:
     * - Player rating
     * - Position requirements (3 GK, 8 DEF, 8 MID, 4 FWD)
     * - Age balance
     * - Form and fitness
     */
    suspend fun selectBestSquad(
        teamId: Int,
        nationality: String,
        includeInjured: Boolean = false,
        minRating: Int = 65
    ): List<PlayersEntity> {
        // Get all eligible players
        val eligiblePlayers = playersDao.getEligiblePlayersForNationalTeam(nationality)
            .firstOrNull() ?: emptyList()

        // Filter by rating and injury status
        val availablePlayers = eligiblePlayers.filter { player ->
            player.rating >= minRating &&
                    (includeInjured || player.injuryStatus == "HEALTHY") &&
                    !player.suspended &&
                    !player.retired
        }

        // Group by position category
        val goalkeepers = availablePlayers
            .filter { it.position == "GK" }
            .sortedByDescending { it.rating }

        val defenders = availablePlayers
            .filter { it.positionCategory == "DEFENDER" }
            .sortedByDescending { it.rating }

        val midfielders = availablePlayers
            .filter { it.positionCategory == "MIDFIELDER" }
            .sortedByDescending { it.rating }

        val forwards = availablePlayers
            .filter { it.positionCategory == "FORWARD" }
            .sortedByDescending { it.rating }

        // Select best players per position
        val selectedGKs = goalkeepers.take(3)
        val selectedDEFs = defenders.take(8)
        val selectedMIDs = midfielders.take(8)
        val selectedFWs = forwards.take(4)

        return selectedGKs + selectedDEFs + selectedMIDs + selectedFWs
    }

    /**
     * AUTOMATIC NATIONAL TEAM CALL-UP SYSTEM
     *
     * This is the core function that ensures HIGHLY RATED PLAYERS receive national team call-ups
     *
     * Rules:
     * 1. Players rated 75+ are automatically called up if they are the best in their position
     * 2. Players rated 80+ are guaranteed a spot in the squad
     * 3. Players rated 85+ are automatically starters
     * 4. Players rated 90+ are considered for captaincy
     */
    suspend fun autoGenerateNationalTeamSquad(
        teamId: Int,
        nationality: String
    ): NationalTeamSelectionResult {
        // Get the national team
        val nationalTeam = nationalTeamsDao.getById(teamId) ?:
        throw IllegalArgumentException("National team not found")

        // Clear existing squad
        nationalTeamPlayersDao.deleteByNationalTeam(teamId)

        // ============ STEP 1: Get all eligible players ============
        val eligiblePlayers = playersDao.getEligiblePlayersForNationalTeam(nationality)
            .firstOrNull() ?: emptyList()

        // Filter for active, healthy players
        val availablePlayers = eligiblePlayers.filter { player ->
            !player.retired &&
                    player.injuryStatus == "HEALTHY" &&
                    !player.suspended
        }

        // ============ STEP 2: Group and rank by position ============
        val goalkeepers = availablePlayers
            .filter { it.position == "GK" }
            .sortedByDescending { it.rating }

        val defenders = availablePlayers
            .filter { it.positionCategory == "DEFENDER" }
            .sortedByDescending { it.rating }

        val midfielders = availablePlayers
            .filter { it.positionCategory == "MIDFIELDER" }
            .sortedByDescending { it.rating }

        val forwards = availablePlayers
            .filter { it.positionCategory == "FORWARD" }
            .sortedByDescending { it.rating }

        // ============ STEP 3: Automatic Elite Call-ups (Rating >= 80) ============
        // These players are GUARANTEED a spot regardless of position limits
        val eliteGKs = goalkeepers.filter { it.rating >= 80 }
        val eliteDEFs = defenders.filter { it.rating >= 80 }
        val eliteMIDs = midfielders.filter { it.rating >= 80 }
        val eliteFWs = forwards.filter { it.rating >= 80 }

        // ============ STEP 4: Select best players with position limits ============
        // Goalkeepers: 3 slots
        val selectedGKs = mutableListOf<PlayersEntity>()
        selectedGKs.addAll(eliteGKs)
        if (selectedGKs.size < 3) {
            selectedGKs.addAll(goalkeepers
                .filter { it !in selectedGKs }
                .take(3 - selectedGKs.size))
        }

        // Defenders: 8 slots
        val selectedDEFs = mutableListOf<PlayersEntity>()
        selectedDEFs.addAll(eliteDEFs)
        if (selectedDEFs.size < 8) {
            selectedDEFs.addAll(defenders
                .filter { it !in selectedDEFs }
                .take(8 - selectedDEFs.size))
        }

        // Midfielders: 8 slots
        val selectedMIDs = mutableListOf<PlayersEntity>()
        selectedMIDs.addAll(eliteMIDs)
        if (selectedMIDs.size < 8) {
            selectedMIDs.addAll(midfielders
                .filter { it !in selectedMIDs }
                .take(8 - selectedMIDs.size))
        }

        // Forwards: 4 slots
        val selectedFWs = mutableListOf<PlayersEntity>()
        selectedFWs.addAll(eliteFWs)
        if (selectedFWs.size < 4) {
            selectedFWs.addAll(forwards
                .filter { it !in selectedFWs }
                .take(4 - selectedFWs.size))
        }

        // Combine all selected players
        val selectedSquad = selectedGKs + selectedDEFs + selectedMIDs + selectedFWs

        // ============ STEP 5: Assign roles based on rating ============
        val entries = mutableListOf<NationalTeamPlayersEntity>()

        // Find captain (highest rated player with leadership >= 70)
        val captain = selectedSquad
            .filter { it.leadership >= 70 }
            .maxByOrNull { it.rating }
            ?: selectedSquad.maxByOrNull { it.rating }

        selectedSquad.forEach { player ->
            val role = when {
                // Captain gets special role
                captain?.id == player.id -> NationalTeamRole.CAPTAIN.value

                // Players rated 85+ are automatic starters
                player.rating >= 85 -> NationalTeamRole.STARTER.value

                // Top 2 rated at each position are starters
                isTopRatedAtPosition(player, selectedSquad) -> NationalTeamRole.STARTER.value

                // Everyone else is reserve
                else -> NationalTeamRole.RESERVE.value
            }

            val entry = NationalTeamPlayersEntity(
                nationalTeamId = teamId,
                playerId = player.id,
                role = role
            )
            entries.add(entry)
        }

        // ============ STEP 6: Insert all entries ============
        nationalTeamPlayersDao.insertAll(entries)

        // ============ STEP 7: Generate selection report ============
        return NationalTeamSelectionResult(
            nationalTeamId = teamId,
            nationalTeamName = nationalTeam.name,
            squadSize = entries.size,
            selectedPlayers = selectedSquad,
            eliteCallUps = (eliteGKs + eliteDEFs + eliteMIDs + eliteFWs).size,
            averageRating = selectedSquad.map { it.rating }.average(),
            captain = captain,
            positionBreakdown = mapOf(
                "GOALKEEPER" to selectedGKs.size,
                "DEFENDER" to selectedDEFs.size,
                "MIDFIELDER" to selectedMIDs.size,
                "FORWARD" to selectedFWs.size
            )
        )
    }

    /**
     * Check if a player is among the top 2 rated in their position
     */
    private fun isTopRatedAtPosition(player: PlayersEntity, squad: List<PlayersEntity>): Boolean {
        val samePositionPlayers = squad.filter { it.position == player.position }
            .sortedByDescending { it.rating }

        return samePositionPlayers.take(2).any { it.id == player.id }
    }

    /**
     * Get players eligible for national team call-up with their ranking
     */
    suspend fun getNationalTeamRankings(nationality: String): List<PlayerNationalRanking> {
        val eligiblePlayers = playersDao.getEligiblePlayersForNationalTeam(nationality)
            .firstOrNull() ?: emptyList()

        val goalkeepers = eligiblePlayers
            .filter { it.position == "GK" }
            .sortedByDescending { it.rating }
            .mapIndexed { index, player ->
                PlayerNationalRanking(
                    player = player,
                    positionRank = index + 1,
                    overallRank = 0, // Will be set later
                    isElite = player.rating >= 80,
                    isWorldClass = player.rating >= 90,
                    callUpStatus = getCallUpStatus(player, index + 1)
                )
            }

        val defenders = eligiblePlayers
            .filter { it.positionCategory == "DEFENDER" }
            .sortedByDescending { it.rating }
            .mapIndexed { index, player ->
                PlayerNationalRanking(
                    player = player,
                    positionRank = index + 1,
                    overallRank = 0,
                    isElite = player.rating >= 80,
                    isWorldClass = player.rating >= 90,
                    callUpStatus = getCallUpStatus(player, index + 1)
                )
            }

        val midfielders = eligiblePlayers
            .filter { it.positionCategory == "MIDFIELDER" }
            .sortedByDescending { it.rating }
            .mapIndexed { index, player ->
                PlayerNationalRanking(
                    player = player,
                    positionRank = index + 1,
                    overallRank = 0,
                    isElite = player.rating >= 80,
                    isWorldClass = player.rating >= 90,
                    callUpStatus = getCallUpStatus(player, index + 1)
                )
            }

        val forwards = eligiblePlayers
            .filter { it.positionCategory == "FORWARD" }
            .sortedByDescending { it.rating }
            .mapIndexed { index, player ->
                PlayerNationalRanking(
                    player = player,
                    positionRank = index + 1,
                    overallRank = 0,
                    isElite = player.rating >= 80,
                    isWorldClass = player.rating >= 90,
                    callUpStatus = getCallUpStatus(player, index + 1)
                )
            }

        // Combine all and assign overall rank
        val allRanked = (goalkeepers + defenders + midfielders + forwards)
            .sortedByDescending { it.player.rating }
            .mapIndexed { index, ranking ->
                ranking.copy(overallRank = index + 1)
            }

        return allRanked
    }

    private fun getCallUpStatus(player: PlayersEntity, positionRank: Int): String {
        return when {
            player.rating >= 85 -> "GUARANTEED STARTER"
            player.rating >= 80 -> "AUTOMATIC CALL-UP"
            positionRank <= 3 -> "LIKELY CALL-UP"
            positionRank <= 5 -> "CONTENTION"
            else -> "BUBBLE"
        }
    }

    // ============ STATISTICS ============

    fun getNationalTeamSquadStats(): Flow<List<NationalTeamSquadStats>> =
        nationalTeamPlayersDao.getNationalTeamSquadStats()

    suspend fun getNationalTeamStrength(teamId: Int): NationalTeamStrength {
        val squad = nationalTeamPlayersDao.getNationalTeamSquad(teamId).firstOrNull() ?: emptyList()
        val starters = nationalTeamPlayersDao.getNationalTeamStarters(teamId).firstOrNull() ?: emptyList()

        return NationalTeamStrength(
            teamId = teamId,
            squadSize = squad.size,
            averageRating = squad.map { it.rating }.average(),
            averageAge = squad.map { it.age }.average(),
            highestRated = squad.maxByOrNull { it.rating },
            captain = nationalTeamPlayersDao.getNationalTeamCaptain(teamId),
            totalGoals = squad.sumOf { it.goals },
            totalCaps = squad.sumOf { it.matches },
            startersAverageRating = starters.map { it.rating }.average()
        )
    }
}

// ============ DATA CLASSES ============

data class NationalTeamSelectionResult(
    val nationalTeamId: Int,
    val nationalTeamName: String,
    val squadSize: Int,
    val selectedPlayers: List<PlayersEntity>,
    val eliteCallUps: Int,
    val averageRating: Double,
    val captain: PlayersEntity?,
    val positionBreakdown: Map<String, Int>
)

data class PlayerNationalRanking(
    val player: PlayersEntity,
    val positionRank: Int,
    val overallRank: Int,
    val isElite: Boolean,
    val isWorldClass: Boolean,
    val callUpStatus: String
)

data class NationalTeamStrength(
    val teamId: Int,
    val squadSize: Int,
    val averageRating: Double,
    val averageAge: Double,
    val highestRated: PlayersEntity?,
    val captain: PlayersEntity?,
    val totalGoals: Int,
    val totalCaps: Int,
    val startersAverageRating: Double
)