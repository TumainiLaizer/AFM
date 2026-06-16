package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PlayerLoansDao
import com.fameafrica.afm.data.database.entities.PlayerLoansEntity
import com.fameafrica.afm.data.database.entities.LoanStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PlayerLoansRepository @Inject constructor(
    private val playerLoansDaoProvider: Provider<PlayerLoansDao>
) {
    private val playerLoansDao get() = playerLoansDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllLoans(): Flow<List<PlayerLoansEntity>> = playerLoansDao.getAll()

    suspend fun getLoanById(id: Int): PlayerLoansEntity? = playerLoansDao.getById(id)

    suspend fun insertLoan(loan: PlayerLoansEntity) = playerLoansDao.insert(loan)

    suspend fun updateLoan(loan: PlayerLoansEntity) = playerLoansDao.update(loan)

    suspend fun deleteLoan(loan: PlayerLoansEntity) = playerLoansDao.delete(loan)

    // ============ LOAN CREATION ============

    /**
     * Create a new loan agreement
     */
    suspend fun createLoan(
        playerName: String,
        playerId: Int,
        loaningTeam: String,
        loaningTeamId: Int,
        receivingTeam: String,
        receivingTeamId: Int,
        season: String,
        durationMonths: Int,
        loanFee: Int? = null,
        wageContribution: Int = 100,
        optionToBuy: Boolean = false,
        buyOptionFee: Int? = null,
        recallOption: Boolean = false
    ): PlayerLoansEntity {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.format(Date())

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, durationMonths)
        val endDate = dateFormat.format(calendar.time)

        val loan = PlayerLoansEntity(
            playerName = playerName,
            playerId = playerId,
            loaningTeam = loaningTeam,
            loaningTeamId = loaningTeamId,
            receivingTeam = receivingTeam,
            receivingTeamId = receivingTeamId,
            season = season,
            startDate = startDate,
            endDate = endDate,
            duration = durationMonths,
            loanFee = loanFee,
            wageContribution = wageContribution,
            optionToBuy = optionToBuy,
            buyOptionFee = buyOptionFee,
            recallOption = recallOption,
            status = LoanStatus.ACTIVE.value
        )

        playerLoansDao.insert(loan)
        return loan
    }

    /**
     * Complete a loan (normal end)
     */
    suspend fun completeLoan(loanId: Int): PlayerLoansEntity? {
        val loan = playerLoansDao.getById(loanId) ?: return null

        val updated = loan.copy(status = LoanStatus.COMPLETED.value)
        playerLoansDao.update(updated)

        return updated
    }

    /**
     * Trigger buy option on loan
     */
    suspend fun triggerBuyOption(loanId: Int): PlayerLoansEntity? {
        val loan = playerLoansDao.getById(loanId) ?: return null

        if (!loan.optionToBuy) return null

        val updated = loan.copy(
            status = LoanStatus.BUY_OPTION_TRIGGERED.value,
            notes = "Buy option triggered on ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}"
        )

        playerLoansDao.update(updated)
        return updated
    }

    /**
     * Early return from loan
     */
    suspend fun earlyReturn(loanId: Int, reason: String): PlayerLoansEntity? {
        val loan = playerLoansDao.getById(loanId) ?: return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val updated = loan.copy(
            status = LoanStatus.EARLY_RETURN.value,
            recallDate = dateFormat.format(Date()),
            notes = "Early return: $reason"
        )

        playerLoansDao.update(updated)
        return updated
    }

    /**
     * Update loan statistics (games played, goals, assists)
     */
    suspend fun updateLoanStats(
        loanId: Int,
        gamesPlayed: Int,
        goalsScored: Int,
        assistsMade: Int
    ): PlayerLoansEntity? {

        val loan = playerLoansDao.getById(loanId) ?: return null

        val updated = loan.copy(
            gamesPlayed = loan.gamesPlayed + gamesPlayed,
            goalsScored = loan.goalsScored + goalsScored,
            assistsMade = loan.assistsMade + assistsMade
        )

        playerLoansDao.update(updated)
        return updated
    }

    // ============ TEAM-BASED ============

    fun getPlayersOutOnLoan(teamName: String): Flow<List<PlayerLoansEntity>> =
        playerLoansDao.getPlayersOutOnLoan(teamName)

    fun getPlayersInOnLoan(teamName: String): Flow<List<PlayerLoansEntity>> =
        playerLoansDao.getPlayersInOnLoan(teamName)

    fun getAllTeamLoans(teamName: String): Flow<List<PlayerLoansEntity>> =
        playerLoansDao.getAllTeamLoans(teamName) // Wait, this should be playerLoansDao

    suspend fun getActiveLoanByPlayer(playerName: String): PlayerLoansEntity? =
        playerLoansDao.getActiveLoanByPlayer(playerName)

    suspend fun getActiveLoanByPlayerId(playerId: Int): PlayerLoansEntity? {
        // Need to add this to DAO if not there
        return playerLoansDao.getAll().firstOrNull()?.find { it.playerId == playerId && it.status == LoanStatus.ACTIVE.value }
    }

    // ============ LOAN MANAGEMENT ============

    /**
     * Process loan expirations
     */
    suspend fun processLoanExpirations(): List<PlayerLoansEntity> {
        val overdueLoans = playerLoansDao.getOverdueLoans().firstOrNull() ?: return emptyList()
        val completed = mutableListOf<PlayerLoansEntity>()

        for (loan in overdueLoans) {
            val updated = loan.copy(status = LoanStatus.COMPLETED.value)
            playerLoansDao.update(updated)
            completed.add(updated)
        }

        return completed
    }

    // ============ DASHBOARD ============

    suspend fun getTeamLoanDashboard(teamName: String): TeamLoanDashboard {
        val playersOut = playerLoansDao.getPlayersOutOnLoan(teamName).firstOrNull() ?: emptyList()
        val playersIn = playerLoansDao.getPlayersInOnLoan(teamName).firstOrNull() ?: emptyList()
        val allLoans = playerLoansDao.getAllTeamLoans(teamName).firstOrNull() ?: emptyList()

        return TeamLoanDashboard(
            teamName = teamName,
            totalLoans = allLoans.size,
            playersOutCount = playersOut.size,
            playersInCount = playersIn.size,
            activeOutLoans = playersOut.filter { it.status == LoanStatus.ACTIVE.value },
            activeInLoans = playersIn.filter { it.status == LoanStatus.ACTIVE.value },
            expiringSoon = allLoans.filter {
                it.status == LoanStatus.ACTIVE.value && it.monthsRemaining <= 1
            }
        )
    }
}

// ============ DATA CLASSES ============

data class TeamLoanDashboard(
    val teamName: String,
    val totalLoans: Int,
    val playersOutCount: Int,
    val playersInCount: Int,
    val activeOutLoans: List<PlayerLoansEntity>,
    val activeInLoans: List<PlayerLoansEntity>,
    val expiringSoon: List<PlayerLoansEntity>
)
