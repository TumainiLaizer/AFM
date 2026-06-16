package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ConfederationAverages
import com.fameafrica.afm.data.database.dao.ConfederationStats
import com.fameafrica.afm.data.database.dao.NationalTeamWithDetails
import com.fameafrica.afm.data.database.dao.NationalTeamWithFlags
import com.fameafrica.afm.data.database.dao.NationalTeamWithPlayerStats
import com.fameafrica.afm.data.database.dao.NationalTeamsDao
import com.fameafrica.afm.data.database.entities.NationalTeamsEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class NationalTeamsRepository @Inject constructor(
    private val nationalTeamsDaoProvider: Provider<NationalTeamsDao>,
    private val nationalitiesRepository: NationalitiesRepository,
    private val playersRepository: PlayersRepository,
    private val nationalTeamPlayersRepository: NationalTeamPlayersRepository
) {

    private val nationalTeamsDao get() = nationalTeamsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllTeams(): Flow<List<NationalTeamsEntity>> = nationalTeamsDao.getAll()

    suspend fun getTeamById(id: Int): NationalTeamsEntity? = nationalTeamsDao.getById(id)

    suspend fun getTeamByName(name: String): NationalTeamsEntity? = nationalTeamsDao.getByName(name)

    suspend fun getTeamByFifaCode(fifaCode: String): NationalTeamsEntity? = nationalTeamsDao.getByFifaCode(fifaCode)

    suspend fun insertTeam(team: NationalTeamsEntity) = nationalTeamsDao.insert(team)

    suspend fun insertAllTeams(teams: List<NationalTeamsEntity>) = nationalTeamsDao.insertAll(teams)

    suspend fun updateTeam(team: NationalTeamsEntity) = nationalTeamsDao.update(team)

    suspend fun deleteTeam(team: NationalTeamsEntity) = nationalTeamsDao.delete(team)

    suspend fun getTeamsCount(): Int = nationalTeamsDao.getCount()

    // ============ CONFEDERATION QUERIES ============

    fun getAfricanTeams(): Flow<List<NationalTeamsEntity>> = nationalTeamsDao.getAfricanTeams()

    fun getEuropeanTeams(): Flow<List<NationalTeamsEntity>> = nationalTeamsDao.getEuropeanTeams()

    fun getSouthAmericanTeams(): Flow<List<NationalTeamsEntity>> = nationalTeamsDao.getSouthAmericanTeams()

    fun getNorthAmericanTeams(): Flow<List<NationalTeamsEntity>> = nationalTeamsDao.getNorthAmericanTeams()

    fun getAsianTeams(): Flow<List<NationalTeamsEntity>> = nationalTeamsDao.getAsianTeams()

    fun getOceanianTeams(): Flow<List<NationalTeamsEntity>> = nationalTeamsDao.getOceanianTeams()

    fun getTeamsByConfederation(confederation: String): Flow<List<NationalTeamsEntity>> =
        nationalTeamsDao.getByConfederation(confederation)

    // ============ RANKING QUERIES ============

    fun getTeamsByEloRanking(): Flow<List<NationalTeamsEntity>> =
        nationalTeamsDao.getTeamsByEloRanking()

    fun getConfederationRanking(confederation: String): Flow<List<NationalTeamsEntity>> =
        nationalTeamsDao.getConfederationRanking(confederation)

    fun getTopTeams(limit: Int): Flow<List<NationalTeamsEntity>> =
        nationalTeamsDao.getTopTeams(limit)

    fun getTopAfricanTeams(limit: Int): Flow<List<NationalTeamsEntity>> =
        nationalTeamsDao.getTopConfederationTeams("CAF", limit)

    fun getTopEuropeanTeams(limit: Int): Flow<List<NationalTeamsEntity>> =
        nationalTeamsDao.getTopConfederationTeams("UEFA", limit)

    // ============ MANAGER MANAGEMENT ============

    suspend fun getTeamByManager(managerId: Int): NationalTeamsEntity? =
        nationalTeamsDao.getTeamByManager(managerId)

    fun getTeamsWithoutManager(): Flow<List<NationalTeamsEntity>> =
        nationalTeamsDao.getTeamsWithoutManager()

    suspend fun assignManager(teamId: Int, managerId: Int?) {
        nationalTeamsDao.assignManager(teamId, managerId)
    }

    // ============ TEAM STATISTICS UPDATE ============

    /**
     * Update national team statistics based on current squad
     */
    suspend fun refreshTeamStats(teamId: Int): NationalTeamsEntity? {
        val team = nationalTeamsDao.getById(teamId) ?: return null
        val squad = nationalTeamPlayersRepository.getNationalTeamSquad(teamId).firstOrNull() ?: emptyList()

        if (squad.isEmpty()) return team

        // Calculate average abilities
        val attackingPlayers = squad.filter { it.positionCategory == "FORWARD" }
        val defensivePlayers = squad.filter { it.positionCategory == "DEFENDER" }
        val midfieldPlayers = squad.filter { it.positionCategory == "MIDFIELDER" }

        val avgAttack = if (attackingPlayers.isNotEmpty())
            attackingPlayers.map { it.rating }.average() else team.avgAttackingAbility ?: 50.0

        val avgDefence = if (defensivePlayers.isNotEmpty())
            defensivePlayers.map { it.rating }.average() else team.avgDefenceAbility ?: 50.0

        val avgPlaymaking = if (midfieldPlayers.isNotEmpty())
            midfieldPlayers.map { it.rating }.average() else team.avgPlaymakingAbility ?: 50.0

        // Calculate average age
        val avgAge = if (squad.isNotEmpty()) squad.map { it.age }.average() else 0.0

        // Find captain, top scorer, most capped
        val captain = squad.firstOrNull { it.isCaptain }
        val topScorer = squad.maxByOrNull { it.goals }
        val mostCapped = squad.maxByOrNull { it.matches }

        val updatedTeam = team.copy(
            squadSize = squad.size,
            averageAge = avgAge,
            avgAttackingAbility = avgAttack,
            avgDefenceAbility = avgDefence,
            avgPlaymakingAbility = avgPlaymaking,
            captainId = captain?.id,
            topScorerId = topScorer?.id,
            mostCappedId = mostCapped?.id,
            lastUpdated = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
        )

        nationalTeamsDao.update(updatedTeam)
        return updatedTeam
    }

    /**
     * Update team ranking after matches
     */
    suspend fun updateEloRating(teamId: Int, newElo: Int) {
        val team = nationalTeamsDao.getById(teamId) ?: return
        val updatedTeam = team.copy(eloRating = newElo)
        nationalTeamsDao.update(updatedTeam)
    }

    /**
     * Update team form after match
     */
    suspend fun updateTeamForm(teamId: Int, result: String) {
        val team = nationalTeamsDao.getById(teamId) ?: return

        val currentForm = team.recentForm ?: ""
        val newForm = (currentForm + result).takeLast(5)

        val updatedTeam = team.copy(recentForm = newForm)
        nationalTeamsDao.update(updatedTeam)
    }

    // ============ JOIN QUERIES ============

    suspend fun getNationalTeamWithDetails(teamId: Int): NationalTeamWithDetails? =
        nationalTeamsDao.getNationalTeamWithDetails(teamId)

    fun getAllNationalTeamsWithDetails(): Flow<List<NationalTeamWithFlags>> =
        nationalTeamsDao.getAllNationalTeamsWithDetails()

    suspend fun getNationalTeamWithPlayerStats(teamId: Int): NationalTeamWithPlayerStats? =
        nationalTeamsDao.getNationalTeamWithPlayerStats(teamId)

    // ============ STATISTICS ============

    fun getConfederationStatistics(): Flow<List<ConfederationStats>> =
        nationalTeamsDao.getConfederationStatistics()

    suspend fun getConfederationAverages(confederation: String): ConfederationAverages? =
        nationalTeamsDao.getConfederationAverages(confederation)

    // ============ DASHBOARD ============

    suspend fun getNationalTeamDashboard(teamId: Int): NationalTeamDashboard {
        val team = nationalTeamsDao.getById(teamId) ?: throw IllegalArgumentException("Team not found")
        val teamWithDetails = nationalTeamsDao.getNationalTeamWithDetails(teamId)
        val squad = nationalTeamPlayersRepository.getNationalTeamSquad(teamId).firstOrNull() ?: emptyList()

        val starters = squad.filter { it.isCaptain || it.isStartingXi }.take(11)
        val averageRating = if (squad.isNotEmpty()) squad.map { it.rating }.average() else 0.0

        val cafRanking = if (team.confederation == "CAF") {
            nationalTeamsDao.getConfederationRanking("CAF").firstOrNull()
                ?.indexOfFirst { it.id == teamId }?.plus(1)
        } else null

        return NationalTeamDashboard(
            team = team,
            teamWithDetails = teamWithDetails,
            squadSize = squad.size,
            averageRating = averageRating,
            starters = starters,
            cafRanking = cafRanking,
            needsRefresh = team.lastUpdated == null ||
                    team.avgAttackingAbility == null ||
                    team.squadSize != squad.size
        )
    }
}

// ============ DATA CLASSES ============

data class NationalTeamDashboard(
    val team: NationalTeamsEntity,
    val teamWithDetails: NationalTeamWithDetails?,
    val squadSize: Int,
    val averageRating: Double,
    val starters: List<PlayersEntity>,
    val cafRanking: Int?,
    val needsRefresh: Boolean
)