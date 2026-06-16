package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.InfrastructureUpgradesDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.dao.FinancesDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class InfrastructureUpgradesRepository @Inject constructor(
    private val upgradesDaoProvider: Provider<InfrastructureUpgradesDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val financesDaoProvider: Provider<FinancesDao>,
    private val financesRepository: FinancesRepository,
    private val newsRepository: NewsRepository
) {

    private val upgradesDao: InfrastructureUpgradesDao?
        get() = try {
            upgradesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    private val teamsDao: TeamsDao?
        get() = try {
            teamsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    private val financesDao: FinancesDao?
        get() = try {
            financesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllUpgrades(): Flow<List<InfrastructureUpgradesEntity>> = upgradesDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getUpgradeById(id: Int): InfrastructureUpgradesEntity? = upgradesDao?.getById(id)

    fun getTeamUpgrades(teamId: Int): Flow<List<InfrastructureUpgradesEntity>> =
        upgradesDao?.getUpgradesByTeam(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertUpgrade(upgrade: InfrastructureUpgradesEntity) {
        upgradesDao?.insert(upgrade)
    }

    suspend fun updateUpgrade(upgrade: InfrastructureUpgradesEntity) {
        upgradesDao?.update(upgrade)
    }

    suspend fun deleteUpgrade(upgrade: InfrastructureUpgradesEntity) {
        upgradesDao?.delete(upgrade)
    }

    // ============ UPGRADE INITIATION ============

    /**
     * Initiate a new infrastructure upgrade
     * Checks if team can afford it and if prerequisites are met
     */
    suspend fun initiateUpgrade(
        teamName: String,
        teamId: Int,
        upgradeType: String,
        targetLevel: Int,
        currentLevel: Int = targetLevel - 1
    ): InfrastructureUpgradesEntity? {

        val team = teamsDao?.getById(teamId) ?: return null
        val season = getCurrentSeason()
        val finances = financesDao?.getByTeamAndSeason(teamId, season) ?: return null

        // Calculate cost based on upgrade type and level
        val cost = calculateUpgradeCost(upgradeType, targetLevel, team)

        // Check if team can afford it
        if (finances.bankBalance < cost) {
            return null // Cannot afford
        }

        // Calculate completion date (3-12 months based on upgrade)
        val completionDate = calculateCompletionDate(upgradeType, targetLevel)
        val startDate = getCurrentDate()

        val upgrade = InfrastructureUpgradesEntity(
            teamName = teamName,
            teamId = teamId,
            upgradeType = upgradeType,
            upgradeLevel = currentLevel,
            targetLevel = targetLevel,
            cost = cost,
            status = UpgradeStatus.PENDING.value,
            startDate = startDate,
            completionDate = completionDate,
            benefitDescription = getBenefitDescription(upgradeType, targetLevel),
            capacityIncrease = getCapacityIncrease(upgradeType, targetLevel),
            trainingEfficiencyIncrease = getTrainingEfficiencyIncrease(upgradeType, targetLevel),
            youthTalentIncrease = getYouthTalentIncrease(upgradeType, targetLevel),
            injuryRecoveryBoost = getInjuryRecoveryBoost(upgradeType, targetLevel),
            fanCapacityIncrease = getFanCapacityIncrease(upgradeType, targetLevel)
        )

        // Reserve the funds using DAO directly
        financesDao?.addInfrastructureCost(teamId, season, cost)

        upgradesDao?.insert(upgrade)
        return upgrade
    }

    private fun calculateUpgradeCost(upgradeType: String, targetLevel: Int, team: TeamsEntity): Long {
        val baseCost = when (upgradeType) {
            UpgradeType.STADIUM.value -> 5_000_000L  // $5M base
            UpgradeType.TRAINING_FACILITY.value -> 3_000_000L
            UpgradeType.YOUTH_ACADEMY.value -> 2_000_000L
            UpgradeType.MEDICAL_CENTER.value -> 1_500_000L
            UpgradeType.FAN_ZONE.value -> 1_000_000L
            else -> 1_000_000L
        }

        val levelMultiplier = when (targetLevel) {
            2 -> 1.5
            3 -> 2.0
            4 -> 3.0
            5 -> 5.0
            else -> 1.0
        }

        val reputationMultiplier = 1.0 + (team.reputation / 100.0)

        return (baseCost * levelMultiplier * reputationMultiplier).toLong()
    }

    private fun calculateCompletionDate(upgradeType: String, targetLevel: Int): String {
        val calendar = Calendar.getInstance()

        val monthsToAdd = when (upgradeType) {
            UpgradeType.STADIUM.value -> targetLevel * 4  // 4-20 months
            UpgradeType.TRAINING_FACILITY.value -> targetLevel * 3
            UpgradeType.YOUTH_ACADEMY.value -> targetLevel * 3
            UpgradeType.MEDICAL_CENTER.value -> targetLevel * 2
            UpgradeType.FAN_ZONE.value -> targetLevel * 2
            else -> targetLevel * 2
        }

        calendar.add(Calendar.MONTH, monthsToAdd)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getBenefitDescription(upgradeType: String, targetLevel: Int): String {
        return when (upgradeType) {
            UpgradeType.STADIUM.value -> "Increases stadium capacity by ${getCapacityIncrease(upgradeType, targetLevel)} seats"
            UpgradeType.TRAINING_FACILITY.value -> "Improves player development by ${getTrainingEfficiencyIncrease(upgradeType, targetLevel)}%"
            UpgradeType.YOUTH_ACADEMY.value -> "Increases youth talent generation by ${getYouthTalentIncrease(upgradeType, targetLevel)}%"
            UpgradeType.MEDICAL_CENTER.value -> "Speeds up injury recovery by ${getInjuryRecoveryBoost(upgradeType, targetLevel)}%"
            UpgradeType.FAN_ZONE.value -> "Increases matchday atmosphere by ${getFanCapacityIncrease(upgradeType, targetLevel)}%"
            else -> "Infrastructure upgrade"
        }
    }

    private fun getCapacityIncrease(upgradeType: String, targetLevel: Int): Int? {
        if (upgradeType != UpgradeType.STADIUM.value) return null
        return when (targetLevel) {
            2 -> 5000
            3 -> 15000
            4 -> 30000
            5 -> 50000
            else -> 2000
        }
    }

    private fun getTrainingEfficiencyIncrease(upgradeType: String, targetLevel: Int): Int? {
        if (upgradeType != UpgradeType.TRAINING_FACILITY.value) return null
        return targetLevel * 5  // 5%, 10%, 15%, 20%, 25%
    }

    private fun getYouthTalentIncrease(upgradeType: String, targetLevel: Int): Int? {
        if (upgradeType != UpgradeType.YOUTH_ACADEMY.value) return null
        return targetLevel * 3  // 3%, 6%, 9%, 12%, 15%
    }

    private fun getInjuryRecoveryBoost(upgradeType: String, targetLevel: Int): Int? {
        if (upgradeType != UpgradeType.MEDICAL_CENTER.value) return null
        return targetLevel * 4  // 4%, 8%, 12%, 16%, 20%
    }

    private fun getFanCapacityIncrease(upgradeType: String, targetLevel: Int): Int? {
        if (upgradeType != UpgradeType.FAN_ZONE.value) return null
        return targetLevel * 2  // 2%, 4%, 6%, 8%, 10%
    }

    // ============ UPGRADE PROCESSING ============

    /**
     * Process all in-progress upgrades (called daily/weekly)
     * Completes upgrades that have reached completion date
     */
    suspend fun processUpgrades(): List<com.fameafrica.afm.domain.model.SimulationEvent> {
        val inProgress = upgradesDao?.getInProgressUpgrades()?.firstOrNull() ?: return emptyList()
        val today = getCurrentDate()
        val events = mutableListOf<com.fameafrica.afm.domain.model.SimulationEvent>()

        for (upgrade in inProgress) {
            if (upgrade.completionDate <= today) {
                completeUpgrade(upgrade.id)?.let {
                    events.add(com.fameafrica.afm.domain.model.SimulationEvent.FacilityCompletion(
                        facilityName = it.upgradeType,
                        level = it.targetLevel
                    ))
                }
            }
        }
        return events
    }

    /**
     * Start a pending upgrade (move from Pending to In Progress)
     */
    suspend fun startUpgrade(upgradeId: Int): InfrastructureUpgradesEntity? {
        val upgrade = upgradesDao?.getById(upgradeId) ?: return null
        if (upgrade.status != UpgradeStatus.PENDING.value) return null

        val updated = upgrade.copy(status = UpgradeStatus.IN_PROGRESS.value)
        upgradesDao?.update(updated)
        return updated
    }

    /**
     * Complete an upgrade and apply its benefits
     */
    suspend fun completeUpgrade(upgradeId: Int): InfrastructureUpgradesEntity? {
        val upgrade = upgradesDao?.getById(upgradeId) ?: return null

        val today = getCurrentDate()
        val updated = upgrade.copy(
            status = UpgradeStatus.COMPLETED.value,
            actualCompletionDate = today
        )

        upgradesDao?.update(updated)

        // Apply upgrade benefits to team
        applyUpgradeBenefits(updated)

        return updated
    }

    /**
     * Cancel an upgrade (refund partial amount)
     */
    suspend fun cancelUpgrade(upgradeId: Int): InfrastructureUpgradesEntity? {
        val upgrade = upgradesDao?.getById(upgradeId) ?: return null
        if (upgrade.status != UpgradeStatus.PENDING.value) return null

        // Refund 50% of cost - use DAO directly
        val refundAmount = (upgrade.cost * 0.5).toLong()
        val season = getCurrentSeason()

        // Use financesDao directly to add to bank balance
        financesDao?.addToBankBalance(upgrade.teamId, season, refundAmount)

        val updated = upgrade.copy(status = UpgradeStatus.CANCELLED.value)
        upgradesDao?.update(updated)

        return updated
    }

    private suspend fun applyUpgradeBenefits(upgrade: InfrastructureUpgradesEntity) {
        val team = teamsDao?.getById(upgrade.teamId) ?: return

        when (upgrade.upgradeType) {
            UpgradeType.STADIUM.value -> {
                upgrade.capacityIncrease?.let {
                    val updatedTeam = team.copy(stadiumCapacity = team.stadiumCapacity + it)
                    teamsDao?.update(updatedTeam)
                }
            }
            UpgradeType.TRAINING_FACILITY.value -> {
                // Benefits are applied globally in PlayerTrainingRepository via Level checking
                newsRepository.createInfrastructureCompletionNews(team.name, team.id, "Training", upgrade.targetLevel)
            }
            UpgradeType.YOUTH_ACADEMY.value -> {
                // Benefits are applied globally in YouthIntakeRepository via Level checking
                newsRepository.createInfrastructureCompletionNews(team.name, team.id, "Youth Academy", upgrade.targetLevel)
            }
            UpgradeType.MEDICAL_CENTER.value -> {
                // Benefits are applied globally in PlayersRepository/WorldSimulationEngine via Level checking
                newsRepository.createInfrastructureCompletionNews(team.name, team.id, "Medical", upgrade.targetLevel)
            }
            UpgradeType.FAN_ZONE.value -> {
                upgrade.fanCapacityIncrease?.let {
                    val updatedTeam = team.copy(crowdSupport = (team.crowdSupport + it).coerceAtMost(100))
                    teamsDao?.update(updatedTeam)
                }
            }
        }
    }

    // ============ TEAM CURRENT LEVELS ============

    /**
     * Get current upgrade level for a specific type
     */
    suspend fun getCurrentUpgradeLevel(teamId: Int, upgradeType: String): Int {
        val latest = upgradesDao?.getLatestUpgradeByType(teamId, upgradeType)
        return if (latest?.status == UpgradeStatus.COMPLETED.value) {
            latest.targetLevel
        } else {
            1 // Default level 1
        }
    }

    /**
     * Check if team can upgrade to next level
     */
    suspend fun canUpgradeTo(teamId: Int, upgradeType: String, targetLevel: Int): Boolean {
        val currentLevel = getCurrentUpgradeLevel(teamId, upgradeType)
        if (targetLevel <= currentLevel) return false

        val season = getCurrentSeason()
        val finances = financesDao?.getByTeamAndSeason(teamId, season) ?: return false
        val team = teamsDao?.getById(teamId) ?: return false

        val cost = calculateUpgradeCost(upgradeType, targetLevel, team)

        return finances.bankBalance >= cost
    }

    // ============ UTILITY ============

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentSeason(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        return if (month >= Calendar.AUGUST) {
            "$year/${year + 1}"
        } else {
            "${year - 1}/$year"
        }
    }

    // ============ QUERIES ============

    fun getTeamActiveUpgrades(teamId: Int): Flow<List<InfrastructureUpgradesEntity>> =
        upgradesDao?.getActiveUpgradesByTeam(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTeamCompletedUpgrades(teamId: Int): Flow<List<InfrastructureUpgradesEntity>> =
        upgradesDao?.getCompletedUpgradesByTeam(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getInProgressUpgrades(): Flow<List<InfrastructureUpgradesEntity>> =
        upgradesDao?.getInProgressUpgrades() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ DASHBOARD ============

    suspend fun getTeamInfrastructureDashboard(teamId: Int, teamName: String): InfrastructureDashboard {
        val allUpgrades = upgradesDao?.getUpgradesByTeam(teamId)?.firstOrNull() ?: emptyList()
        val completed = allUpgrades.filter { it.status == UpgradeStatus.COMPLETED.value }
        val inProgress = allUpgrades.filter { it.status == UpgradeStatus.IN_PROGRESS.value }
        val pending = allUpgrades.filter { it.status == UpgradeStatus.PENDING.value }

        val totalSpent = completed.sumOf { it.cost }

        val stadiumLevel = getCurrentUpgradeLevel(teamId, UpgradeType.STADIUM.value)
        val trainingLevel = getCurrentUpgradeLevel(teamId, UpgradeType.TRAINING_FACILITY.value)
        val youthLevel = getCurrentUpgradeLevel(teamId, UpgradeType.YOUTH_ACADEMY.value)
        val medicalLevel = getCurrentUpgradeLevel(teamId, UpgradeType.MEDICAL_CENTER.value)
        val fanZoneLevel = getCurrentUpgradeLevel(teamId, UpgradeType.FAN_ZONE.value)

        return InfrastructureDashboard(
            teamName = teamName,
            totalUpgrades = allUpgrades.size,
            completedUpgrades = completed.size,
            inProgressUpgrades = inProgress.size,
            pendingUpgrades = pending.size,
            totalSpent = totalSpent,
            stadiumLevel = stadiumLevel,
            trainingFacilityLevel = trainingLevel,
            youthAcademyLevel = youthLevel,
            medicalCenterLevel = medicalLevel,
            fanZoneLevel = fanZoneLevel,
            recentUpgrades = completed.sortedByDescending { it.completionDate }.take(5),
            activeUpgrades = (inProgress + pending).sortedBy { it.startDate }
        )
    }
}

// ============ DATA CLASSES ============

data class InfrastructureDashboard(
    val teamName: String,
    val totalUpgrades: Int,
    val completedUpgrades: Int,
    val inProgressUpgrades: Int,
    val pendingUpgrades: Int,
    val totalSpent: Long,
    val stadiumLevel: Int,
    val trainingFacilityLevel: Int,
    val youthAcademyLevel: Int,
    val medicalCenterLevel: Int,
    val fanZoneLevel: Int,
    val recentUpgrades: List<InfrastructureUpgradesEntity>,
    val activeUpgrades: List<InfrastructureUpgradesEntity>
)
