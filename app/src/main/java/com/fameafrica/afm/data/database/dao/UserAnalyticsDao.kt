package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.UserAnalyticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAnalyticsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM user_analytics ORDER BY created_at DESC")
    fun getAll(): Flow<List<UserAnalyticsEntity>>

    @Query("SELECT * FROM user_analytics WHERE id = :id")
    suspend fun getById(id: Int): UserAnalyticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analytics: UserAnalyticsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(analytics: List<UserAnalyticsEntity>)

    @Update
    suspend fun update(analytics: UserAnalyticsEntity)

    @Delete
    suspend fun delete(analytics: UserAnalyticsEntity)

    @Query("DELETE FROM user_analytics WHERE created_at < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)

    @Query("DELETE FROM user_analytics")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM user_analytics")
    suspend fun getCount(): Int

    @Query("SELECT * FROM user_analytics")
    suspend fun getAllStatic(): List<UserAnalyticsEntity>

    // ============ EVENT-BASED QUERIES ============

    @Query("SELECT * FROM user_analytics WHERE event_type = :eventType ORDER BY created_at DESC")
    fun getByEventType(eventType: String): Flow<List<UserAnalyticsEntity>>

    @Query("SELECT COUNT(*) FROM user_analytics WHERE event_type = :eventType")
    suspend fun getEventCount(eventType: String): Int

    @Query("SELECT event_type, COUNT(*) as event_count FROM user_analytics GROUP BY event_type ORDER BY event_count DESC")
    fun getEventTypeDistribution(): Flow<List<EventTypeDistribution>>

    // ============ USER-BASED QUERIES ============

    @Query("SELECT * FROM user_analytics WHERE user_id = :userId ORDER BY created_at DESC")
    fun getByUser(userId: String): Flow<List<UserAnalyticsEntity>>

    @Query("SELECT * FROM user_analytics WHERE session_id = :sessionId ORDER BY created_at ASC")
    fun getBySession(sessionId: String): Flow<List<UserAnalyticsEntity>>

    @Query("SELECT COUNT(DISTINCT user_id) FROM user_analytics")
    suspend fun getUniqueUserCount(): Int

    @Query("SELECT COUNT(DISTINCT session_id) FROM user_analytics WHERE created_at >= :since")
    suspend fun getUniqueSessionCount(since: Long): Int

    // ============ TIME-BASED QUERIES ============

    @Query("SELECT * FROM user_analytics WHERE created_at >= :startTime ORDER BY created_at DESC")
    fun getSince(startTime: Long): Flow<List<UserAnalyticsEntity>>

    @Query("SELECT * FROM user_analytics WHERE created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    fun getBetween(startTime: Long, endTime: Long): Flow<List<UserAnalyticsEntity>>

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', datetime(created_at/1000, 'unixepoch')) as date,
            COUNT(*) as event_count
        FROM user_analytics 
        WHERE created_at >= :since
        GROUP BY date
        ORDER BY date DESC
    """)
    fun getDailyEventCounts(since: Long): Flow<List<DailyEventCount>>

    // ============ GEOGRAPHY QUERIES ============

    @Query("SELECT country_code, COUNT(*) as user_count FROM user_analytics WHERE country_code IS NOT NULL GROUP BY country_code ORDER BY user_count DESC")
    fun getUserDistributionByCountry(): Flow<List<CountryDistribution>>

    // ============ DEVICE QUERIES ============

    @Query("SELECT device_info, COUNT(*) as device_count FROM user_analytics WHERE device_info IS NOT NULL GROUP BY device_info ORDER BY device_count DESC")
    fun getDeviceDistribution(): Flow<List<DeviceDistribution>>

    @Query("SELECT app_version, COUNT(*) as version_count FROM user_analytics WHERE app_version IS NOT NULL GROUP BY app_version ORDER BY version_count DESC")
    fun getVersionDistribution(): Flow<List<VersionDistribution>>
}

// ============ DATA CLASSES ============

data class EventTypeDistribution(
    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "event_count")
    val eventCount: Int
)

data class DailyEventCount(
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "event_count")
    val eventCount: Int
)

data class CountryDistribution(
    @ColumnInfo(name = "country_code")
    val countryCode: String,

    @ColumnInfo(name = "user_count")
    val userCount: Int
)

data class DeviceDistribution(
    @ColumnInfo(name = "device_info")
    val deviceInfo: String,

    @ColumnInfo(name = "device_count")
    val deviceCount: Int
)

data class VersionDistribution(
    @ColumnInfo(name = "app_version")
    val appVersion: String,

    @ColumnInfo(name = "version_count")
    val versionCount: Int
)