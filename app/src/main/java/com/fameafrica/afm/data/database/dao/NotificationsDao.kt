package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.NotificationsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getById(id: Int): NotificationsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationsEntity>)

    @Update
    suspend fun update(notification: NotificationsEntity)

    @Delete
    suspend fun delete(notification: NotificationsEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getCount(): Int

    @Query("SELECT * FROM notifications")
    suspend fun getAllStatic(): List<NotificationsEntity>

    // ============ UNREAD/READ QUERIES ============

    @Query("SELECT * FROM notifications WHERE isRead = 0 AND is_archived = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE isRead = 1 AND is_archived = 0 ORDER BY timestamp DESC")
    fun getReadNotifications(): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE is_archived = 1 ORDER BY timestamp DESC")
    fun getArchivedNotifications(): Flow<List<NotificationsEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0 AND is_archived = 0")
    suspend fun getUnreadCount(): Int

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("UPDATE notifications SET is_archived = 1 WHERE id = :id")
    suspend fun archiveNotification(id: Int)

    @Query("UPDATE notifications SET is_archived = 1 WHERE is_archived = 0")
    suspend fun archiveAll()

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM notifications WHERE notification_type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE notification_type = :type AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadByType(type: String): Flow<List<NotificationsEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE notification_type = :type AND isRead = 0")
    suspend fun getUnreadCountByType(type: String): Int

    // ============ PRIORITY-BASED QUERIES ============

    @Query("SELECT * FROM notifications WHERE priority >= :minPriority ORDER BY timestamp DESC")
    fun getByMinPriority(minPriority: Int): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE priority = 5 ORDER BY timestamp DESC")
    fun getCriticalNotifications(): Flow<List<NotificationsEntity>>

    // ============ TIME-BASED QUERIES ============

    @Query("SELECT * FROM notifications WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getSince(since: Long): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getBetween(start: Long, end: Long): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE date(timestamp/1000, 'unixepoch') = date('now')")
    fun getTodayNotifications(): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE date(timestamp/1000, 'unixepoch') = date('now', '-1 day')")
    fun getYesterdayNotifications(): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp < :cutoffTime")
    suspend fun getOlderThan(cutoffTime: Long): List<NotificationsEntity>

    // ============ ENTITY-BASED QUERIES ============

    @Query("SELECT * FROM notifications WHERE related_entity_id = :entityId AND related_entity_type = :entityType ORDER BY timestamp DESC")
    fun getByRelatedEntity(entityId: Int, entityType: String): Flow<List<NotificationsEntity>>

    @Query("SELECT * FROM notifications WHERE related_entity_name LIKE '%' || :entityName || '%' ORDER BY timestamp DESC")
    fun getByRelatedEntityName(entityName: String): Flow<List<NotificationsEntity>>

    // ============ USER-BASED QUERIES ============

    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getByUser(userId: Int): Flow<List<NotificationsEntity>>

    // ============ EXPIRY QUERIES ============

    @Query("SELECT * FROM notifications WHERE expiry_time IS NOT NULL AND expiry_time < :currentTime")
    suspend fun getExpiredNotifications(currentTime: Long): List<NotificationsEntity>

    @Query("DELETE FROM notifications WHERE expiry_time IS NOT NULL AND expiry_time < :currentTime")
    suspend fun deleteExpired(currentTime: Long)

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            notification_type,
            COUNT(*) as count,
            SUM(CASE WHEN isRead = 0 THEN 1 ELSE 0 END) as unread_count
        FROM notifications 
        GROUP BY notification_type
        ORDER BY count DESC
    """)
    fun getNotificationTypeDistribution(): Flow<List<NotificationTypeStats>>

    @Query("""
        SELECT 
            priority,
            COUNT(*) as count
        FROM notifications 
        GROUP BY priority
        ORDER BY priority DESC
    """)
    fun getPriorityDistribution(): Flow<List<PriorityStats>>

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', timestamp/1000, 'unixepoch') as date,
            COUNT(*) as daily_count
        FROM notifications 
        WHERE timestamp >= :since
        GROUP BY date
        ORDER BY date DESC
    """)
    fun getDailyCount(since: Long): Flow<List<DailyNotificationCount>>
}

// ============ DATA CLASSES ============

data class NotificationTypeStats(
    @ColumnInfo(name = "notification_type")
    val notificationType: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int
)

data class PriorityStats(
    @ColumnInfo(name = "priority")
    val priority: Int,

    @ColumnInfo(name = "count")
    val count: Int
)

data class DailyNotificationCount(
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "daily_count")
    val dailyCount: Int
)