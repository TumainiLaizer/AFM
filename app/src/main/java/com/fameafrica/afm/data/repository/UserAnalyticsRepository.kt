package com.fameafrica.afm.data.repository

import android.os.Build
import com.fameafrica.afm.data.database.dao.CountryDistribution
import com.fameafrica.afm.data.database.dao.DailyEventCount
import com.fameafrica.afm.data.database.dao.DeviceDistribution
import com.fameafrica.afm.data.database.dao.EventTypeDistribution
import com.fameafrica.afm.data.database.dao.UserAnalyticsDao
import com.fameafrica.afm.data.database.dao.VersionDistribution
import com.fameafrica.afm.data.database.entities.UserAnalyticsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util. *
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class UserAnalyticsRepository @Inject constructor(
    private val userAnalyticsDaoProvider: Provider<UserAnalyticsDao>
) {
    private val userAnalyticsDao get() = userAnalyticsDaoProvider.get()

    // ============ BASIC CRUD =============

    fun getAllAnalytics(): Flow<List<UserAnalyticsEntity>> = userAnalyticsDao.getAll()

    suspend fun getAnalyticsById(id: Int): UserAnalyticsEntity? = userAnalyticsDao.getById(id)

    suspend fun insertAnalytics(analytics: UserAnalyticsEntity) = userAnalyticsDao.insert(analytics)

    suspend fun insertAllAnalytics(analytics: List<UserAnalyticsEntity>) = userAnalyticsDao.insertAll(analytics)

    suspend fun updateAnalytics(analytics: UserAnalyticsEntity) = userAnalyticsDao.update(analytics)

    suspend fun deleteAnalytics(analytics: UserAnalyticsEntity) = userAnalyticsDao.delete(analytics)

    suspend fun deleteOlderThan(days: Int) {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        userAnalyticsDao.deleteOlderThan(cutoffTime)
    }

    suspend fun deleteAllAnalytics() = userAnalyticsDao.deleteAll()

    suspend fun getAnalyticsCount(): Int = userAnalyticsDao.getCount()

    // ============ EVENT TRACKING =============

    suspend fun trackEvent(
        eventType: String,
        eventData: String? = null,
        userId: String? = null,
        sessionId: String? = null,
        countryCode: String? = null
    ) {
        val analytics = UserAnalyticsEntity(
            eventType = eventType,
            eventData = eventData,
            userId = userId,
            sessionId = sessionId,
            createdAt = System.currentTimeMillis(),
            deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}",
            appVersion = getAppVersion(),
            countryCode = countryCode
        )
        userAnalyticsDao.insert(analytics)
    }

    suspend fun trackGameStart(userId: String?, sessionId: String?, countryCode: String?) {
        trackEvent("GAME_START", null, userId, sessionId, countryCode)
    }

    suspend fun trackMatchPlayed(
        userId: String?,
        sessionId: String?,
        matchType: String,
        result: String
    ) {
        val eventData = mapOf(
            "matchType" to matchType,
            "result" to result
        ).toString()
        trackEvent("MATCH_PLAYED", eventData, userId, sessionId)
    }

    suspend fun trackTransferCompleted(
        userId: String?,
        sessionId: String?,
        playerName: String,
        transferFee: Double,
        fromTeam: String,
        toTeam: String
    ) {
        val eventData = mapOf(
            "playerName" to playerName,
            "transferFee" to transferFee,
            "fromTeam" to fromTeam,
            "toTeam" to toTeam
        ).toString()
        trackEvent("TRANSFER_COMPLETED", eventData, userId, sessionId)
    }

    suspend fun trackSettingsChange(
        userId: String?,
        sessionId: String?,
        settingName: String,
        oldValue: String?,
        newValue: String?
    ) {
        val eventData = mapOf(
            "settingName" to settingName,
            "oldValue" to (oldValue ?: "null"),
            "newValue" to (newValue ?: "null")
        ).toString()
        trackEvent("SETTINGS_CHANGE", eventData, userId, sessionId)
    }

    suspend fun trackAchievementUnlocked(
        userId: String?,
        sessionId: String?,
        achievementName: String
    ) {
        trackEvent("ACHIEVEMENT_UNLOCKED", achievementName, userId, sessionId)
    }

    // ============ EVENT-BASED QUERIES =============

    fun getEventsByType(eventType: String): Flow<List<UserAnalyticsEntity>> =
        userAnalyticsDao.getByEventType(eventType)

    suspend fun getEventCount(eventType: String): Int = userAnalyticsDao.getEventCount(eventType)

    fun getEventTypeDistribution(): Flow<List<EventTypeDistribution>> =
        userAnalyticsDao.getEventTypeDistribution()

    // ============ USER-BASED QUERIES =============

    fun getEventsByUser(userId: String): Flow<List<UserAnalyticsEntity>> =
        userAnalyticsDao.getByUser(userId)

    fun getEventsBySession(sessionId: String): Flow<List<UserAnalyticsEntity>> =
        userAnalyticsDao.getBySession(sessionId)

    suspend fun getUniqueUserCount(): Int = userAnalyticsDao.getUniqueUserCount()

    suspend fun getUniqueSessionCount(since: Long): Int = userAnalyticsDao.getUniqueSessionCount(since)

    // ============ TIME-BASED QUERIES =============

    fun getEventsSince(startTime: Long): Flow<List<UserAnalyticsEntity>> =
        userAnalyticsDao.getSince(startTime)

    fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<UserAnalyticsEntity>> =
        userAnalyticsDao.getBetween(startTime, endTime)

    fun getDailyEventCounts(since: Long): Flow<List<DailyEventCount>> =
        userAnalyticsDao.getDailyEventCounts(since)

    suspend fun getTodayEventCount(): Int {
        val startOfDay = getStartOfDayTimestamp()
        return userAnalyticsDao.getSince(startOfDay).firstOrNull()?.size ?: 0
    }

    suspend fun getThisWeekEventCount(): Int {
        val startOfWeek = getStartOfWeekTimestamp()
        return userAnalyticsDao.getSince(startOfWeek).firstOrNull()?.size ?: 0
    }

    suspend fun getThisMonthEventCount(): Int {
        val startOfMonth = getStartOfMonthTimestamp()
        return userAnalyticsDao.getSince(startOfMonth).firstOrNull()?.size ?: 0
    }

    // ============ GEOGRAPHY QUERIES =============

    fun getUserDistributionByCountry(): Flow<List<CountryDistribution>> =
        userAnalyticsDao.getUserDistributionByCountry()

    // ============ DEVICE QUERIES =============

    fun getDeviceDistribution(): Flow<List<DeviceDistribution>> =
        userAnalyticsDao.getDeviceDistribution()

    fun getVersionDistribution(): Flow<List<VersionDistribution>> =
        userAnalyticsDao.getVersionDistribution()

    // ============ STATISTICS ============

    suspend fun getMostCommonEventType(): String? {
        val distribution = userAnalyticsDao.getEventTypeDistribution().firstOrNull()
        return distribution?.maxByOrNull { it.eventCount }?.eventType
    }

    suspend fun getMostActiveUser(): String? {
        val users = userAnalyticsDao.getAll().firstOrNull()
            ?.groupBy { it.userId }
            ?.mapValues { it.value.size }
            ?.maxByOrNull { it.value }
        return users?.key
    }

    suspend fun getAverageEventsPerUser(): Double {
        val totalEvents = userAnalyticsDao.getCount()
        val uniqueUsers = userAnalyticsDao.getUniqueUserCount()
        return if (uniqueUsers > 0) totalEvents.toDouble() / uniqueUsers else 0.0
    }

    // ============ UTILITY =============

    private fun getAppVersion(): String {
        return "1.0.0" // This should come from BuildConfig
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeekTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfMonthTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // ============ DASHBOARD ============

    suspend fun getAnalyticsDashboard(): AnalyticsDashboard {
        val allEvents = userAnalyticsDao.getAll().firstOrNull() ?: emptyList()
        val eventDistribution = userAnalyticsDao.getEventTypeDistribution().firstOrNull() ?: emptyList()
        val countryDistribution = userAnalyticsDao.getUserDistributionByCountry().firstOrNull() ?: emptyList()
        val deviceDistribution = userAnalyticsDao.getDeviceDistribution().firstOrNull() ?: emptyList()
        val versionDistribution = userAnalyticsDao.getVersionDistribution().firstOrNull() ?: emptyList()

        val totalUsers = userAnalyticsDao.getUniqueUserCount()
        val totalSessions = userAnalyticsDao.getUniqueSessionCount(0)

        val todayCount = getTodayEventCount()
        val weekCount = getThisWeekEventCount()
        val monthCount = getThisMonthEventCount()

        val mostActiveHour = allEvents
            .groupBy {
                Calendar.getInstance().apply { timeInMillis = it.createdAt }.get(Calendar.HOUR_OF_DAY)
            }
            .maxByOrNull { it.value.size }

        return AnalyticsDashboard(
            totalEvents = allEvents.size,
            totalUsers = totalUsers,
            totalSessions = totalSessions,
            todayEvents = todayCount,
            thisWeekEvents = weekCount,
            thisMonthEvents = monthCount,
            averageEventsPerUser = getAverageEventsPerUser(),
            mostCommonEventType = getMostCommonEventType(),
            mostActiveHour = mostActiveHour?.key,
            mostActiveHourCount = mostActiveHour?.value?.size ?: 0,
            eventTypeDistribution = eventDistribution,
            countryDistribution = countryDistribution,
            deviceDistribution = deviceDistribution,
            versionDistribution = versionDistribution
        )
    }
}

// ============ DATA CLASSES ============

data class AnalyticsDashboard(
    val totalEvents: Int,
    val totalUsers: Int,
    val totalSessions: Int,
    val todayEvents: Int,
    val thisWeekEvents: Int,
    val thisMonthEvents: Int,
    val averageEventsPerUser: Double,
    val mostCommonEventType: String?,
    val mostActiveHour: Int?,
    val mostActiveHourCount: Int,
    val eventTypeDistribution: List<EventTypeDistribution>,
    val countryDistribution: List<CountryDistribution>,
    val deviceDistribution: List<DeviceDistribution>,
    val versionDistribution: List<VersionDistribution>
)
