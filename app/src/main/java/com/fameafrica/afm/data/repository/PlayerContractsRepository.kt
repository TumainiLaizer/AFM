package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PlayerContractsDao
import com.fameafrica.afm.data.database.dao.TeamWageStats
import com.fameafrica.afm.data.database.entities.PlayerContractsEntity
import com.fameafrica.afm.data.database.entities.ContractStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PlayerContractsRepository @Inject constructor(
    private val playerContractsDaoProvider: Provider<PlayerContractsDao>
) {
    private val playerContractsDao get() = playerContractsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllContracts(): Flow<List<PlayerContractsEntity>> = playerContractsDao.getAll()

    suspend fun getContractById(id: Int): PlayerContractsEntity? = playerContractsDao.getById(id)

    suspend fun getContractByPlayerName(playerName: String): PlayerContractsEntity? =
        playerContractsDao.getByPlayerName(playerName)

    suspend fun getContractByPlayerId(playerId: Int): PlayerContractsEntity? =
        playerContractsDao.getByPlayerId(playerId)

    suspend fun getContractsByPlayerIds(playerIds: List<Int>): List<PlayerContractsEntity> =
        playerContractsDao.getByPlayerIds(playerIds)

    suspend fun insertContract(contract: PlayerContractsEntity) = playerContractsDao.insert(contract)

    suspend fun updateContract(contract: PlayerContractsEntity) = playerContractsDao.update(contract)

    suspend fun deleteContract(contract: PlayerContractsEntity) = playerContractsDao.delete(contract)

    // ============ CONTRACT CREATION ============

    /**
     * Create a new player contract
     */
    suspend fun createContract(
        playerName: String,
        playerId: Int,
        teamName: String,
        teamId: Int,
        salary: Int,
        contractLength: Int,
        releaseClause: Int = 500000000,
        signingBonus: Int? = null,
        bonusGoals: Int = 150000,
        bonusAssists: Int = 100000,
        bonusTrophies: Int = 5000000,
        isNegotiable: Boolean = true
    ): PlayerContractsEntity {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.format(Date())

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, contractLength)
        val endDate = dateFormat.format(calendar.time)

        val contract = PlayerContractsEntity(
            playerName = playerName,
            playerId = playerId,
            teamName = teamName,
            teamId = teamId,
            salary = salary,
            signingBonus = signingBonus,
            contractLength = contractLength,
            contractStartDate = startDate,
            contractEndDate = endDate,
            releaseClause = releaseClause,
            isNegotiable = isNegotiable,
            bonusGoals = bonusGoals,
            bonusAssists = bonusAssists,
            bonusTrophies = bonusTrophies,
            contractStatus = ContractStatus.ACTIVE.value
        )

        playerContractsDao.insert(contract)
        return contract
    }

    /**
     * Renew an existing contract
     */
    suspend fun renewContract(
        contractId: Int,
        newSalary: Double,
        newContractLength: Int,
        newReleaseClause: Int? = null
    ): PlayerContractsEntity? {

        val contract = playerContractsDao.getById(contractId) ?: return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, newContractLength)
        val newEndDate = dateFormat.format(calendar.time)

        val updated = contract.copy(
            salary = newSalary.toInt(),
            contractLength = newContractLength,
            contractEndDate = newEndDate,
            releaseClause = newReleaseClause ?: contract.releaseClause,
            contractStatus = ContractStatus.ACTIVE.value,
            lastRenegotiationDate = dateFormat.format(Date())
        )

        playerContractsDao.update(updated)
        return updated
    }

    /**
     * Terminate a contract
     */
    suspend fun terminateContract(contractId: Int, reason: String): PlayerContractsEntity? {
        val contract = playerContractsDao.getById(contractId) ?: return null

        val updated = contract.copy(
            contractStatus = ContractStatus.TERMINATED.value,
            notes = "Terminated: $reason"
        )

        playerContractsDao.update(updated)
        return updated
    }

    /**
     * Update contract statuses based on dates
     */
    suspend fun updateContractStatuses() {
        playerContractsDao.updateExpiringStatus()
        playerContractsDao.updateExpiredStatus()
    }

    // ============ TEAM-BASED ============

    fun getContractsByTeam(teamName: String): Flow<List<PlayerContractsEntity>> =
        playerContractsDao.getContractsByTeam(teamName)

    suspend fun getTotalWageBill(teamName: String): Int? =
        playerContractsDao.getTotalWageBill(teamName)

    suspend fun getAverageWage(teamName: String): Double? =
        playerContractsDao.getAverageWage(teamName)

    // ============ CONTRACT STATUS ============

    fun getExpiringContracts(): Flow<List<PlayerContractsEntity>> =
        playerContractsDao.getExpiringContracts()

    fun getContractsExpiringSoon(): Flow<List<PlayerContractsEntity>> =
        playerContractsDao.getContractsExpiringSoon()

    fun getNegotiableContracts(): Flow<List<PlayerContractsEntity>> =
        playerContractsDao.getNegotiableContracts()

    // ============ STATISTICS ============

    fun getTeamWageStatistics(): Flow<List<TeamWageStats>> =
        playerContractsDao.getTeamWageStatistics()

    fun getTopEarners(limit: Int): Flow<List<PlayerContractsEntity>> =
        playerContractsDao.getTopEarners(limit)

    // ============ DASHBOARD ============

    suspend fun getTeamContractDashboard(teamName: String): TeamContractDashboard {
        val contracts = playerContractsDao.getContractsByTeam(teamName).firstOrNull() ?: emptyList()
        val active = contracts.filter { it.contractStatus == ContractStatus.ACTIVE.value }
        val expiring = contracts.filter { it.contractStatus == ContractStatus.EXPIRING.value }
        val expired = contracts.filter { it.contractStatus == ContractStatus.EXPIRED.value }

        val totalWageBill = active.sumOf { it.salary }
        val averageWage = if (active.isNotEmpty()) totalWageBill / active.size else 0

        val highestEarner = active.maxByOrNull { it.salary }
        val lowestEarner = active.minByOrNull { it.salary }

        return TeamContractDashboard(
            teamName = teamName,
            totalContracts = contracts.size,
            activeContracts = active.size,
            expiringContracts = expiring.size,
            expiredContracts = expired.size,
            totalWageBill = totalWageBill,
            averageWage = averageWage,
            highestEarner = highestEarner,
            lowestEarner = lowestEarner,
            expiringSoon = expiring
        )
    }
}

// ============ DATA CLASSES ============

data class TeamContractDashboard(
    val teamName: String,
    val totalContracts: Int,
    val activeContracts: Int,
    val expiringContracts: Int,
    val expiredContracts: Int,
    val totalWageBill: Int,
    val averageWage: Int,
    val highestEarner: PlayerContractsEntity?,
    val lowestEarner: PlayerContractsEntity?,
    val expiringSoon: List<PlayerContractsEntity>
)
