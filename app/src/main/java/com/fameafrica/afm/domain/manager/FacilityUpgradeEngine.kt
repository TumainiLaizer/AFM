package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.InfrastructureUpgradesEntity
import com.fameafrica.afm.data.database.entities.UpgradeStatus
import com.fameafrica.afm.data.repository.InfrastructureUpgradesRepository
import com.fameafrica.afm.data.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FacilityUpgradeEngine @Inject constructor(
    private val infrastructureUpgradesRepository: InfrastructureUpgradesRepository,
    private val economyManager: EconomyManager,
    private val newsRepository: NewsRepository
) {

    suspend fun requestUpgrade(
        managerId: Int,
        teamId: Int,
        teamName: String,
        upgradeType: String
    ): InfrastructureUpgradesEntity? {
        val currentLevel = infrastructureUpgradesRepository.getCurrentUpgradeLevel(teamId, upgradeType)
        val targetLevel = currentLevel + 1
        
        // In a full implementation, this might go through BoardDecisionSystem
        // For now, we delegate to the repository which checks finances and initiates
        return infrastructureUpgradesRepository.initiateUpgrade(
            teamName = teamName,
            teamId = teamId,
            upgradeType = upgradeType,
            targetLevel = targetLevel,
            currentLevel = currentLevel
        )
    }

    suspend fun speedUpUpgrade(
        teamId: Int,
        season: String,
        upgradeId: Int,
        coinCost: Long
    ): Boolean {
        if (economyManager.spendCoins(teamId, season, coinCost)) {
            val upgrade = infrastructureUpgradesRepository.getUpgradeById(upgradeId) ?: return false
            if (upgrade.status == UpgradeStatus.IN_PROGRESS.value || upgrade.status == UpgradeStatus.PENDING.value) {
                infrastructureUpgradesRepository.completeUpgrade(upgradeId)
                
                // Optional: Notify user or create news
                newsRepository.createInfrastructureCompletionNews(
                    upgrade.teamName, upgrade.teamId, upgrade.upgradeType, upgrade.targetLevel
                )
                return true
            }
        }
        return false
    }

    suspend fun processDailyUpgrades() {
        infrastructureUpgradesRepository.processUpgrades()
    }
}
