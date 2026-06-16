package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.TransferWindowsDao
import com.fameafrica.afm.data.database.entities.TransferWindowsEntity
import com.fameafrica.afm.data.database.entities.TransferWindowType
import com.fameafrica.afm.data.database.entities.ForeignPlayerRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TransferWindowsRepository @Inject constructor(
    private val transferWindowsDaoProvider: Provider<TransferWindowsDao>,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val leaguesRepository: LeaguesRepository
) {
    private val transferWindowsDao get() = transferWindowsDaoProvider.get()

    // ============ BASIC CRUD ============S

    fun getAllWindows(): Flow<List<TransferWindowsEntity>> = transferWindowsDao.getAll()

    suspend fun getWindowById(id: Int): TransferWindowsEntity? = transferWindowsDao.getById(id)

    suspend fun insertWindow(window: TransferWindowsEntity) = transferWindowsDao.insert(window)

    suspend fun updateWindow(window: TransferWindowsEntity) = transferWindowsDao.update(window)

    suspend fun deleteWindow(window: TransferWindowsEntity) = transferWindowsDao.delete(window)

    // ============ WINDOW MANAGEMENT ============S

    /**
     * Initialize transfer windows for a new season
     */
    suspend fun initializeSeasonWindows(season: String) {
        // Delete existing windows for this season
        val existing = transferWindowsDao.getWindowsBySeason(season).firstOrNull()
        if (!existing.isNullOrEmpty()) return

        val year = season.split("/").first().toInt()

        // Summer window: June 1 - August 31 (Weeks 1 to 13 roughly)
        val summerStart = Calendar.getInstance().apply {
            set(year, Calendar.JUNE, 1)
        }
        val summerEnd = Calendar.getInstance().apply {
            set(year, Calendar.AUGUST, 31)
        }

        // Winter window: January 1 - January 31 (Around week 31 to 35)
        val winterStart = Calendar.getInstance().apply {
            set(year + 1, Calendar.JANUARY, 1)
        }
        val winterEnd = Calendar.getInstance().apply {
            set(year + 1, Calendar.JANUARY, 31)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val summerWindow = TransferWindowsEntity(
            season = season,
            windowType = TransferWindowType.SUMMER.value,
            startDate = dateFormat.format(summerStart.time),
            endDate = dateFormat.format(summerEnd.time),
            startWeek = 1,
            endWeek = 13,
            isOpen = isDateInRange(summerStart.time, summerEnd.time),
            registrationDeadline = dateFormat.format(summerEnd.time),
            maxForeignPlayers = 5,
            maxPlayersIn = 15,
            maxPlayersOut = 15
        )

        val winterWindow = TransferWindowsEntity(
            season = season,
            windowType = TransferWindowType.WINTER.value,
            startDate = dateFormat.format(winterStart.time),
            endDate = dateFormat.format(winterEnd.time),
            startWeek = 31,
            endWeek = 35,
            isOpen = isDateInRange(winterStart.time, winterEnd.time),
            registrationDeadline = dateFormat.format(winterEnd.time),
            maxForeignPlayers = 5,
            maxPlayersIn = 5,
            maxPlayersOut = 5
        )

        transferWindowsDao.insertAll(listOf(summerWindow, winterWindow))
    }

    private fun isDateInRange(start: Date, end: Date): Boolean {
        val now = Date()
        return now in start..end
    }

    /**
     * Update window status (open/close) based on current date
     */
    suspend fun updateWindowStatuses() {
        transferWindowsDao.autoCloseExpiredWindows()

        val windows = transferWindowsDao.getAll().firstOrNull() ?: return

        windows.forEach { window ->
            val shouldBeOpen = isDateInRange(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(window.startDate) ?: return@forEach,
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(window.endDate) ?: return@forEach
            )

            if (window.isOpen != shouldBeOpen) {
                if (shouldBeOpen) {
                    transferWindowsDao.openWindow(window.id)
                } else {
                    transferWindowsDao.closeWindow(window.id)
                }
            }
        }
    }

    /**
     * Get current active transfer window
     */
    suspend fun getCurrentWindow(): TransferWindowsEntity? {
        updateWindowStatuses()
        return transferWindowsDao.getCurrentActiveWindow()
    }

    /**
     * Check if transfer window is open
     */
    suspend fun isTransferWindowOpen(): Boolean {
        return getCurrentWindow() != null
    }

    /**
     * Get summer window for a season
     */
    suspend fun getSummerWindow(season: String): TransferWindowsEntity? =
        transferWindowsDao.getSummerWindow(season)

    /**
     * Get winter window for a season
     */
    suspend fun getWinterWindow(season: String): TransferWindowsEntity? =
        transferWindowsDao.getWinterWindow(season)

    // ============ COUNTRY-SPECIFIC FOREIGN PLAYER RULES ============S

    /**
     * Get maximum foreign players allowed for a team based on its league
     */
    suspend fun getMaxForeignPlayersForTeam(teamName: String): Int {
        val team = teamsRepository.getTeamByName(teamName) ?: return 5
        val league = leaguesRepository.getLeagueByName(team.league) ?: return 5

        return ForeignPlayerRules.getMaxForeignPlayersByCountry(
            countryId = league.countryId ?: 0,
            leagueName = league.name
        )
    }

    /**
     * Check if a player would be considered foreign in a team's league
     */
    suspend fun isPlayerForeignForTeam(playerNationality: String, teamName: String): Boolean {
        val team = teamsRepository.getTeamByName(teamName) ?: return true
        val league = leaguesRepository.getLeagueByName(team.league) ?: return true

        return ForeignPlayerRules.isPlayerForeign(
            playerNationality = playerNationality,
            leagueCountryId = league.countryId ?: 0
        )
    }

    /**
     * Get the current foreign player count for a team
     */
    suspend fun getCurrentForeignPlayerCount(teamName: String): Int {
        val team = teamsRepository.getTeamByName(teamName) ?: return 0
        val squad = playersRepository.getPlayersByTeamId(team.id).firstOrNull() ?: return 0
        var foreignCount = 0

        for (player in squad) {
            if (isPlayerForeignForTeam(player.nationality, teamName)) {
                foreignCount++
            }
        }

        return foreignCount
    }

    /**
     * Check if a team has room to sign another foreign player
     */
    suspend fun canSignForeignPlayer(teamName: String): Boolean {
        val maxForeign = getMaxForeignPlayersForTeam(teamName)
        val currentForeign = getCurrentForeignPlayerCount(teamName)

        return currentForeign < maxForeign
    }

    /**
     * Get foreign player limit status for a team
     */
    suspend fun getForeignPlayerStatus(teamName: String): ForeignPlayerStatus {
        val maxForeign = getMaxForeignPlayersForTeam(teamName)
        val currentForeign = getCurrentForeignPlayerCount(teamName)
        val remaining = maxForeign - currentForeign

        return ForeignPlayerStatus(
            teamName = teamName,
            maxForeignPlayers = maxForeign,
            currentForeignPlayers = currentForeign,
            remainingSlots = remaining,
            canSignMore = remaining > 0
        )
    }

    // ============ QUERIES ============S

    fun getActiveWindows(): Flow<List<TransferWindowsEntity>> =
        transferWindowsDao.getActiveWindows()

    fun getWindowsBySeason(season: String): Flow<List<TransferWindowsEntity>> =
        transferWindowsDao.getWindowsBySeason(season)

    fun getSeasons(): Flow<List<String>> = transferWindowsDao.getSeasons()

    // ============ DASHBOARD ============S

    suspend fun getTransferWindowDashboard(): TransferWindowDashboard {
        val allWindows = transferWindowsDao.getAll().firstOrNull() ?: emptyList()
        val activeWindows = allWindows.filter { it.isActive }
        val currentWindow = getCurrentWindow()

        val upcomingWindows = allWindows
            .filter { !it.isActive && it.startDate > getTodayString() }
            .sortedBy { it.startDate }

        return TransferWindowDashboard(
            totalWindows = allWindows.size,
            activeWindows = activeWindows.size,
            currentWindow = currentWindow,
            upcomingWindows = upcomingWindows,
            seasons = allWindows.map { it.season }.distinct().sortedDescending()
        )
    }

    private fun getTodayString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

// ============ DATA CLASSES ============S

data class TransferWindowDashboard(
    val totalWindows: Int,
    val activeWindows: Int,
    val currentWindow: TransferWindowsEntity?,
    val upcomingWindows: List<TransferWindowsEntity>,
    val seasons: List<String>
)

data class ForeignPlayerStatus(
    val teamName: String,
    val maxForeignPlayers: Int,
    val currentForeignPlayers: Int,
    val remainingSlots: Int,
    val canSignMore: Boolean
)
