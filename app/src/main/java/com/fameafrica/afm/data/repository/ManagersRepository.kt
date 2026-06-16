package com.fameafrica.afm.data.repository

import android.util.Log
import com.fameafrica.afm.data.database.dao.ChairmanDao
import com.fameafrica.afm.data.database.dao.EmployedManagerWithTeam
import com.fameafrica.afm.data.database.dao.ManagerLevelDistribution
import com.fameafrica.afm.data.database.dao.ManagerStyleDistribution
import com.fameafrica.afm.data.database.dao.ManagerWithDetails
import com.fameafrica.afm.data.database.dao.ManagersDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ManagersRepository @Inject constructor(
    private val managersDaoProvider: Provider<ManagersDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val chairmanDao: Provider<ChairmanDao>,
    private val gameDateManager: GameDateManager
) {
    private val managersDao get() = managersDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    // ============ BASIC CRUD ============

    fun getAllManagers(): Flow<List<ManagersEntity>> = managersDao.getAll()

    suspend fun getManagerById(id: Int): ManagersEntity? = managersDao.getById(id)

    suspend fun getManagerByName(name: String): ManagersEntity? = managersDao.getByName(name)

    suspend fun getManagerByTeamId(teamId: Int): ManagersEntity? = managersDao.getByTeamId(teamId)

    suspend fun insertManager(manager: ManagersEntity) = managersDao.insert(manager)

    suspend fun insertAllManagers(managers: List<ManagersEntity>) = managersDao.insertAll(managers)

    suspend fun updateManager(manager: ManagersEntity) = managersDao.update(manager)

    suspend fun deleteManager(manager: ManagersEntity) = managersDao.delete(manager)

    suspend fun getManagersCount(): Int = managersDao.getCount()

    // ============ EMPLOYMENT STATUS ============

    fun getAvailableManagers(): Flow<List<ManagersEntity>> = managersDao.getAvailableManagers()

    fun getEmployedManagers(): Flow<List<ManagersEntity>> = managersDao.getEmployedManagers()

    suspend fun getManagerByTeam(teamId: Int): ManagersEntity? = managersDao.getManagerByTeam(teamId)

    suspend fun getManagersByTeamIdsSync(teamIds: List<Int>): List<ManagersEntity> = managersDao.getByTeamIds(teamIds)

    fun getAvailableManagersByReputation(minRep: Int, maxRep: Int): Flow<List<ManagersEntity>> =
        managersDao.getAvailableManagersByReputation(minRep, maxRep)



    // ============ AI LOGIC: HIRING ============

    /**
     * Finds teams without managers and hires appropriate candidates from the available pool.
     */
    suspend fun processAIHiring() {
        // Optimize: Only handle a few teams per week to spread the load and make world feel dynamic
        val teamsWithoutManagers = teamsDao.getTeamsWithoutManager().firstOrNull()?.shuffled()?.take(5) ?: return
        if (teamsWithoutManagers.isEmpty()) return
        
        val availableManagers = managersDao.getAvailableManagers().firstOrNull()?.toMutableList() ?: return
        if (availableManagers.isEmpty()) return

        for (team in teamsWithoutManagers) {
            // Find a manager whose reputation matches the team's
            val candidateIndex = availableManagers.indexOfFirst { 
                it.reputation in (team.reputation - 20)..(team.reputation + 30)
            }
            
            val candidate = if (candidateIndex != -1) {
                availableManagers.removeAt(candidateIndex)
            } else {
                if (availableManagers.isNotEmpty()) availableManagers.removeAt(0) else null
            }

            if (candidate != null) {
                Log.d("AFM_AI", "AI Hiring: ${team.name} has appointed ${candidate.name}")
                
                // Update manager
                val updatedManager = candidate.copy(
                    teamId = team.id,
                    contractEndDate = Calendar.getInstance().get(Calendar.YEAR) + Random.nextInt(1, 4),
                    salary = (team.revenue / 1000).toInt().coerceIn(2000, 50000)
                )
                managersDao.update(updatedManager)
                
                // Update team
                teamsDao.update(team.copy(managerId = candidate.id))
            }
        }
    }

    /**
     * Checks all managed teams and sacks managers with critically low board confidence.
     */
    suspend fun processAISackings(userTeamId: Int? = null) {
        val allTeams = teamsDao?.getAllStatic()?.filter { it.managerId != null } ?: return
        
        for (team in allTeams) {
            // Skip user's team - sacking is handled by board evaluation system
            if (team.id == userTeamId) continue

            // Board confidence critically low
            if (team.boardConfidence < 20 && Random.nextInt(100) < 35) {
                val manager = managersDao?.getById(team.managerId!!) ?: continue
                
                Log.d("AFM_AI", "AI Sack: ${team.name} has fired ${manager.name}")
                
                // Fire manager
                managersDao?.update(manager.copy(teamId = null, contractEndDate = null))
                
                // Update team
                teamsDao?.update(team.copy(managerId = null, boardConfidence = 50))
                
                // Note: News should be generated by a higher-level system (WorldSimulationEngine)
            }
        }
    }

    // ============ REPUTATION-BASED ============

    fun getManagersByReputationLevel(level: String): Flow<List<ManagersEntity>> =
        managersDao.getManagersByReputationLevel(level)

    fun getHighReputationManagers(minReputation: Int): Flow<List<ManagersEntity>> =
        managersDao.getHighReputationManagers(minReputation)

    fun getLowReputationManagers(maxReputation: Int): Flow<List<ManagersEntity>> =
        managersDao.getLowReputationManagers(maxReputation)

    fun getTopManagers(limit: Int): Flow<List<ManagersEntity>> =
        managersDao.getTopManagers(limit)

    // ============ NATIONALITY-BASED ============
    fun getManagersByNationality(nationality: String): Flow<List<ManagersEntity>> =
        managersDao.getManagersByNationality(nationality)

    fun getDistinctNationalities(): Flow<List<String>> = managersDao.getDistinctNationalities()

    // ============ AGE-BASED ============

    fun getYoungManagers(): Flow<List<ManagersEntity>> = managersDao.getYoungManagers()

    fun getPrimeAgeManagers(): Flow<List<ManagersEntity>> = managersDao.getPrimeAgeManagers()

    fun getVeteranManagers(): Flow<List<ManagersEntity>> = managersDao.getVeteranManagers()

    // ============ PERFORMANCE-BASED ============

    fun getManagersByWinPercentage(limit: Int): Flow<List<ManagersEntity>> =
        managersDao.getManagersByWinPercentage(limit)

    fun getAllManagersByWinPercentage(limit: Int): Flow<List<ManagersEntity>> =
        managersDao.getAllManagersByWinPercentage(limit)

    fun getManagersByWinPercentageWithMinMatches(minMatches: Int, limit: Int): Flow<List<ManagersEntity>> =
        managersDao.getManagersByWinPercentageWithMinMatches(minMatches, limit)

    /**
     * Get top active managers (employed, 50+ matches) by win percentage
     */
    fun getTopActiveManagersByWinPercentage(limit: Int): Flow<List<ManagersEntity>> =
        managersDao.getTopActiveManagersByWinPercentage(limit)

    fun getMostTrophyWinningManagers(limit: Int): Flow<List<ManagersEntity>> =
        managersDao.getMostTrophyWinningManagers(limit)

    fun getMostExperiencedManagers(limit: Int): Flow<List<ManagersEntity>> =
        managersDao.getMostExperiencedManagers(limit)

    fun getHighPerformingManagers(): Flow<List<ManagersEntity>> =
        managersDao.getHighPerformingManagers()

    // ============ WIN PERCENTAGE UTILITIES ============

    /**
     * Calculate win percentage for a single manager
     */
    fun calculateWinPercentage(manager: ManagersEntity): Double {
        return if (manager.matchesManaged > 0) {
            (manager.wins.toDouble() / manager.matchesManaged * 100)
        } else 0.0
    }

    /**
     * Get manager with win percentage as a flow
     */
    suspend fun getManagerWithWinPercentage(managerId: Int): Pair<ManagersEntity?, Double> {
        val manager = managersDao.getById(managerId)
        return Pair(manager, calculateWinPercentage(manager ?: return Pair(null, 0.0)))
    }

    // ============ LICENSE & ABILITY ============

    fun getManagersByLicense(licenses: List<String>): Flow<List<ManagersEntity>> =
        managersDao.getManagersByLicense(licenses)

    fun getManagersBySpecialAbility(ability: String): Flow<List<ManagersEntity>> =
        managersDao.getManagersBySpecialAbility(ability)

    fun getYouthDevelopmentSpecialists(): Flow<List<ManagersEntity>> =
        managersDao.getYouthDevelopmentSpecialists()

    fun getMediaFriendlyManagers(): Flow<List<ManagersEntity>> =
        managersDao.getMediaFriendlyManagers()

    fun getTacticallyFlexibleManagers(): Flow<List<ManagersEntity>> =
        managersDao.getTacticallyFlexibleManagers()

    fun getMotivationalManagers(): Flow<List<ManagersEntity>> =
        managersDao.getMotivationalManagers()

    fun getStrictManagers(): Flow<List<ManagersEntity>> =
        managersDao.getStrictManagers()

    fun getAdaptableManagers(): Flow<List<ManagersEntity>> =
        managersDao.getAdaptableManagers()

    // ============ FORMATION & STYLE ============

    fun getManagersByPreferredFormation(formation: String): Flow<List<ManagersEntity>> =
        managersDao.getManagersByPreferredFormation(formation)

    fun getManagersByStyle(style: String): Flow<List<ManagersEntity>> =
        managersDao.getManagersByStyle(style)

    // ============ SEARCH ============

    fun searchManagers(searchQuery: String): Flow<List<ManagersEntity>> =
        managersDao.searchManagers(searchQuery)

    fun advancedSearch(searchQuery: String): Flow<List<ManagersEntity>> =
        managersDao.advancedSearch(searchQuery)

    // ============ STATISTICS ============

    suspend fun getAverageReputation(): Double? = managersDao.getAverageReputation()

    suspend fun getAverageAge(): Double? = managersDao.getAverageAge()

    suspend fun getAverageMatchesManaged(): Double? = managersDao.getAverageMatchesManaged()

    suspend fun getAverageWinPercentage(): Double? = managersDao.getAverageWinPercentage()

    fun getManagerDistributionByLevel(): Flow<List<ManagerLevelDistribution>> =
        managersDao.getManagerDistributionByLevel()

    fun getManagerStyleDistribution(): Flow<List<ManagerStyleDistribution>> =
        managersDao.getManagerStyleDistribution()

    // ============ JOIN QUERIES ============

    suspend fun getManagerWithDetails(managerId: Int): ManagerWithDetails? =
        managersDao.getManagerWithDetails(managerId)

    fun getAllEmployedManagersWithTeams(): Flow<List<EmployedManagerWithTeam>> =
        managersDao.getAllEmployedManagersWithTeams()

    // ============ MANAGER MANAGEMENT ============

    suspend fun updateManagerAfterMatch(
        managerId: Int,
        won: Boolean,
        drew: Boolean,
        lost: Boolean
    ) {
        val manager = managersDao.getById(managerId) ?: return
        val updatedManager = manager.updateAfterMatch(won, drew, lost)
        managersDao.update(updatedManager)
    }

    suspend fun winTrophy(managerId: Int) {
        val manager = managersDao.getById(managerId) ?: return
        val updatedManager = manager.winTrophy()
        managersDao.update(updatedManager)
    }

    suspend fun signContract(managerId: Int, teamId: Int, salary: Int, contractYears: Int, currentWeek: Int = 1) {
        val manager = managersDao.getById(managerId) ?: return
        val calendar = Calendar.getInstance()
        calendar.time = gameDateManager.getGameDate(currentWeek)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val updatedManager = manager.signContract(teamId, salary, contractYears, currentYear)
        managersDao.update(updatedManager)
    }

    suspend fun leaveClub(managerId: Int) {
        val manager = managersDao.getById(managerId) ?: return
        val updatedManager = manager.leaveClub()
        managersDao.update(updatedManager)
    }

    suspend fun renewContract(managerId: Int, newSalary: Int, additionalYears: Int, currentWeek: Int = 1) {
        val manager = managersDao.getById(managerId) ?: return
        val calendar = Calendar.getInstance()
        calendar.time = gameDateManager.getGameDate(currentWeek)
        val currentYear = calendar.get(Calendar.YEAR)

        val updatedManager = manager.renewContract(newSalary, additionalYears, currentYear)
        managersDao.update(updatedManager)
    }

    suspend fun updateReputation(managerId: Int, newReputation: Int) {
        val manager = managersDao.getById(managerId) ?: return
        val updatedManager = manager.updateReputation(newReputation)
        managersDao.update(updatedManager)
    }

    suspend fun earnAward(managerId: Int, awardType: String) {
        val manager = managersDao.getById(managerId) ?: return
        val updatedManager = manager.earnAward(awardType)
        managersDao.update(updatedManager)
    }

    suspend fun earnPromotion(managerId: Int) {
        val manager = managersDao.getById(managerId) ?: return
        managersDao.update(manager.copy(reputation = (manager.reputation + 10).coerceAtMost(100)))
    }

    suspend fun upgradeLicense(managerId: Int, newLicense: String) {
        val manager = managersDao.getById(managerId) ?: return
        val updatedManager = manager.upgradeLicense(newLicense)
        managersDao.update(updatedManager)
    }

    // ============ MANAGER CREATION ============

    suspend fun createNewManager(
        name: String,
        nationality: String,
        age: Int,
        coachingLicense: String = "NATIONAL_C",
        specialAbility: String? = null
    ): ManagersEntity {
        val manager = ManagersEntity(
            name = name,
            nationality = nationality,
            age = age,
            teamId = null,
            coachingLicense = coachingLicense,
            specialAbility = specialAbility,
            reputation = 30,
            reputationLevel = "Local",
            preferredFormation = "4-4-2",
            style = "Balanced",
            matchesManaged = 0,
            wins = 0,
            draws = 0,
            losses = 0,
            trophiesWon = 0,
            performanceRating = 50,
            youthDevelopmentFocus = 50,
            mediaHandling = 50,
            tacticalFlexibility = 50,
            playerMotivation = 50,
            disciplineLevel = 50,
            adaptability = 50
        )

        managersDao.insert(manager)
        return manager
    }

    // ============ DASHBOARD ============

    suspend fun getManagerDashboard(managerId: Int): ManagerDashboard {
        val manager = managersDao.getById(managerId) ?: throw IllegalArgumentException("Manager not found")
        val managerWithDetails = managersDao.getManagerWithDetails(managerId)

        val winRate = calculateWinPercentage(manager)

        val trophyRate = if (manager.matchesManaged > 0) {
            (manager.trophiesWon.toDouble() / manager.matchesManaged * 100)
        } else 0.0

        return ManagerDashboard(
            manager = manager,
            managerWithDetails = managerWithDetails,
            winPercentage = winRate,
            trophyPercentage = trophyRate,
            matchesPerTrophy = if (manager.trophiesWon > 0)
                (manager.matchesManaged / manager.trophiesWon) else 0,
            overallRating = manager.overallRating,
            careerStage = manager.careerStage,
            isEmployed = manager.isEmployed,
            availableManagersCount = managersDao.getAvailableManagers().firstOrNull()?.size ?: 0
        )
    }
}

// ============ DATA CLASSES ============

data class ManagerDashboard(
    val manager: ManagersEntity,
    val managerWithDetails: ManagerWithDetails?,
    val winPercentage: Double,
    val trophyPercentage: Double,
    val matchesPerTrophy: Int,
    val overallRating: Int,
    val careerStage: String,
    val isEmployed: Boolean,
    val availableManagersCount: Int
)
