package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ChangeFrequency
import com.fameafrica.afm.data.database.dao.SettingsHistoryDao
import com.fameafrica.afm.data.database.entities.SettingsHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SettingsHistoryRepository @Inject constructor(
    private val settingsHistoryDaoProvider: Provider<SettingsHistoryDao>
) {

    private val settingsHistoryDao: SettingsHistoryDao?
        get() = try {
            settingsHistoryDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllHistory(): Flow<List<SettingsHistoryEntity>> = settingsHistoryDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getHistoryById(id: Int): SettingsHistoryEntity? = settingsHistoryDao?.getById(id)

    suspend fun insertHistory(history: SettingsHistoryEntity) {
        settingsHistoryDao?.insert(history)
    }

    suspend fun insertAllHistory(histories: List<SettingsHistoryEntity>) {
        settingsHistoryDao?.insertAll(histories)
    }

    suspend fun updateHistory(history: SettingsHistoryEntity) {
        settingsHistoryDao?.update(history)
    }

    suspend fun deleteHistory(history: SettingsHistoryEntity) {
        settingsHistoryDao?.delete(history)
    }

    suspend fun deleteAllHistory() {
        settingsHistoryDao?.deleteAll()
    }

    // ============ SETTINGS-BASED ============

    fun getHistoryForSettings(settingsId: Int): Flow<List<SettingsHistoryEntity>> =
        settingsHistoryDao?.getHistoryForSettings(settingsId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getHistoryForField(settingsId: Int, field: String): Flow<List<SettingsHistoryEntity>> =
        settingsHistoryDao?.getHistoryForField(settingsId, field) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getLastChangeForField(settingsId: Int, field: String): SettingsHistoryEntity? {
        return settingsHistoryDao?.getHistoryForField(settingsId, field)
            ?.firstOrNull()
            ?.firstOrNull()
    }

    // ============ TIME-BASED ============

    fun getHistorySince(startTime: Long): Flow<List<SettingsHistoryEntity>> =
        settingsHistoryDao?.getHistorySince(startTime) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getHistoryBetween(startTime: Long, endTime: Long): Flow<List<SettingsHistoryEntity>> =
        settingsHistoryDao?.getHistoryBetween(startTime, endTime) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getTodayHistory(): List<SettingsHistoryEntity> {
        val startOfDay = getStartOfDayTimestamp()
        return settingsHistoryDao?.getHistorySince(startOfDay)?.firstOrNull() ?: emptyList()
    }

    suspend fun getThisWeekHistory(): List<SettingsHistoryEntity> {
        val startOfWeek = getStartOfWeekTimestamp()
        return settingsHistoryDao?.getHistorySince(startOfWeek)?.firstOrNull() ?: emptyList()
    }

    suspend fun getThisMonthHistory(): List<SettingsHistoryEntity> {
        val startOfMonth = getStartOfMonthTimestamp()
        return settingsHistoryDao?.getHistorySince(startOfMonth)?.firstOrNull() ?: emptyList()
    }

    // ============ USER-BASED ============

    fun getHistoryByUser(changedBy: String): Flow<List<SettingsHistoryEntity>> =
        settingsHistoryDao?.getHistoryByUser(changedBy) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ STATISTICS ============

    suspend fun getChangeCount(settingsId: Int): Int = settingsHistoryDao?.getChangeCount(settingsId) ?: 0

    fun getChangeFrequency(settingsId: Int): Flow<List<ChangeFrequency>> =
        settingsHistoryDao?.getChangeFrequency(settingsId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getLastChangeTime(settingsId: Int): Long? = settingsHistoryDao?.getLastChangeTime(settingsId)

    suspend fun getMostChangedField(settingsId: Int): String? {
        val frequencies = settingsHistoryDao?.getChangeFrequency(settingsId)?.firstOrNull() ?: return null
        return frequencies.firstOrNull()?.changedField
    }

    suspend fun getAverageChangesPerDay(settingsId: Int): Double {
        val totalChanges = getChangeCount(settingsId)
        val firstChange = settingsHistoryDao?.getHistoryForSettings(settingsId)
            ?.firstOrNull()
            ?.minByOrNull { it.changedAt }

        val lastChange = settingsHistoryDao?.getHistoryForSettings(settingsId)
            ?.firstOrNull()
            ?.maxByOrNull { it.changedAt }

        if (firstChange == null || lastChange == null) return 0.0

        val daysDiff = (lastChange.changedAt - firstChange.changedAt) / (1000.0 * 60 * 60 * 24)
        return if (daysDiff > 0) totalChanges / daysDiff else totalChanges.toDouble()
    }

    // ============ CLEANUP ============

    suspend fun deleteOlderThan(days: Int): Int {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        settingsHistoryDao?.deleteOlderThan(cutoffTime)

        // Return count of remaining records
        return settingsHistoryDao?.getAll()?.firstOrNull()?.size ?: 0
    }

    // ============ EXPORT ============

    suspend fun exportHistoryAsCsv(settingsId: Int): String {
        val history = settingsHistoryDao?.getHistoryForSettings(settingsId)?.firstOrNull() ?: return ""

        val header = "Timestamp,Field,Old Value,New Value,Changed By\n"
        val rows = history.joinToString("\n") { entry ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(entry.changedAt))
            "${date},${entry.changedField},${entry.oldValue ?: ""},${entry.newValue ?: ""},${entry.changedBy}"
        }

        return header + rows
    }

    // ============ UTILITY ============

    private fun getStartOfDayTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeekTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfMonthTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // ============ DASHBOARD ============

    suspend fun getHistoryDashboard(settingsId: Int): HistoryDashboard {
        val allHistory = settingsHistoryDao?.getHistoryForSettings(settingsId)?.firstOrNull() ?: emptyList()
        val frequencies = settingsHistoryDao?.getChangeFrequency(settingsId)?.firstOrNull() ?: emptyList()

        val todayCount = getTodayHistory().size
        val weekCount = getThisWeekHistory().size
        val monthCount = getThisMonthHistory().size

        val lastChange = allHistory.maxByOrNull { it.changedAt }
        val firstChange = allHistory.minByOrNull { it.changedAt }

        val changesByField = frequencies.associate { it.changedField to it.changeCount }

        val mostActiveDay = allHistory
            .groupBy {
                java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(it.changedAt))
            }
            .maxByOrNull { it.value.size }

        return HistoryDashboard(
            totalChanges = allHistory.size,
            todayChanges = todayCount,
            thisWeekChanges = weekCount,
            thisMonthChanges = monthCount,
            lastChange = lastChange,
            firstChange = firstChange,
            changesByField = changesByField,
            mostActiveDay = mostActiveDay?.key,
            mostActiveDayCount = mostActiveDay?.value?.size ?: 0,
            recentChanges = allHistory.take(20)
        )
    }
}

// ============ DATA CLASSES ============

data class HistoryDashboard(
    val totalChanges: Int,
    val todayChanges: Int,
    val thisWeekChanges: Int,
    val thisMonthChanges: Int,
    val lastChange: SettingsHistoryEntity?,
    val firstChange: SettingsHistoryEntity?,
    val changesByField: Map<String, Int>,
    val mostActiveDay: String?,
    val mostActiveDayCount: Int,
    val recentChanges: List<SettingsHistoryEntity>
)