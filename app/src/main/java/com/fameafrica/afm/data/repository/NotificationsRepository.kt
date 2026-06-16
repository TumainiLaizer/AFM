package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.DailyNotificationCount
import com.fameafrica.afm.data.database.dao.NotificationTypeStats
import com.fameafrica.afm.data.database.dao.NotificationsDao
import com.fameafrica.afm.data.database.dao.PriorityStats
import com.fameafrica.afm.data.database.entities.NotificationsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    private val notificationsDaoProvider: Provider<NotificationsDao>
) {
    private val notificationsDao get() = notificationsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllNotifications(): Flow<List<NotificationsEntity>> = notificationsDao.getAll()

    suspend fun getNotificationById(id: Int): NotificationsEntity? = notificationsDao.getById(id)

    suspend fun insertNotification(notification: NotificationsEntity) = notificationsDao.insert(notification)

    suspend fun insertAllNotifications(notifications: List<NotificationsEntity>) = notificationsDao.insertAll(notifications)

    suspend fun updateNotification(notification: NotificationsEntity) = notificationsDao.update(notification)

    suspend fun deleteNotification(notification: NotificationsEntity) = notificationsDao.delete(notification)

    suspend fun deleteNotificationById(id: Int) = notificationsDao.deleteById(id)

    suspend fun deleteAllNotifications() = notificationsDao.deleteAll()

    suspend fun getNotificationsCount(): Int = notificationsDao.getCount()

    // ============ UNREAD/READ MANAGEMENT ============

    fun getUnreadNotifications(): Flow<List<NotificationsEntity>> = notificationsDao.getUnreadNotifications()

    fun getUnreadCountFlow(): Flow<Int> = notificationsDao.getUnreadNotifications().map { it.size }

    fun getReadNotifications(): Flow<List<NotificationsEntity>> = notificationsDao.getReadNotifications()

    fun getArchivedNotifications(): Flow<List<NotificationsEntity>> = notificationsDao.getArchivedNotifications()

    suspend fun getUnreadCount(): Int = notificationsDao.getUnreadCount()

    suspend fun markAsRead(id: Int) = notificationsDao.markAsRead(id)

    suspend fun markAllAsRead() = notificationsDao.markAllAsRead()

    suspend fun archiveNotification(id: Int) = notificationsDao.archiveNotification(id)

    suspend fun archiveAll() = notificationsDao.archiveAll()

    // ============ TYPE-BASED ============

    fun getNotificationsByType(type: String): Flow<List<NotificationsEntity>> =
        notificationsDao.getByType(type)

    fun getUnreadByType(type: String): Flow<List<NotificationsEntity>> =
        notificationsDao.getUnreadByType(type)

    suspend fun getUnreadCountByType(type: String): Int =
        notificationsDao.getUnreadCountByType(type)

    // ============ PRIORITY-BASED ============

    fun getCriticalNotifications(): Flow<List<NotificationsEntity>> =
        notificationsDao.getCriticalNotifications()

    fun getNotificationsByMinPriority(minPriority: Int): Flow<List<NotificationsEntity>> =
        notificationsDao.getByMinPriority(minPriority)

    // ============ TIME-BASED ============

    fun getTodayNotifications(): Flow<List<NotificationsEntity>> =
        notificationsDao.getTodayNotifications()

    fun getYesterdayNotifications(): Flow<List<NotificationsEntity>> =
        notificationsDao.getYesterdayNotifications()

    fun getNotificationsSince(since: Long): Flow<List<NotificationsEntity>> =
        notificationsDao.getSince(since)

    fun getNotificationsBetween(start: Long, end: Long): Flow<List<NotificationsEntity>> =
        notificationsDao.getBetween(start, end)

    // ============ CLEANUP ============

    /**
     * Clean up old notifications (keep last 30 days)
     */
    suspend fun cleanupOldNotifications(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        val oldNotifications = notificationsDao.getOlderThan(cutoffTime)

        // Archive instead of delete
        oldNotifications.forEach { notification ->
            if (!notification.isArchived) {
                notificationsDao.archiveNotification(notification.id)
            }
        }
    }

    /**
     * Delete expired notifications
     */
    suspend fun deleteExpiredNotifications() {
        notificationsDao.deleteExpired(System.currentTimeMillis())
    }

    // ============ STATISTICS ============

    fun getNotificationTypeDistribution(): Flow<List<NotificationTypeStats>> =
        notificationsDao.getNotificationTypeDistribution()

    fun getPriorityDistribution(): Flow<List<PriorityStats>> =
        notificationsDao.getPriorityDistribution()

    fun getDailyNotificationCount(since: Long): Flow<List<DailyNotificationCount>> =
        notificationsDao.getDailyCount(since)

    // ============ INBOX DASHBOARD ============

    suspend fun getInboxDashboard(): InboxDashboard {
        val unread = notificationsDao.getUnreadNotifications().firstOrNull() ?: emptyList()
        val read = notificationsDao.getReadNotifications().firstOrNull() ?: emptyList()
        val archived = notificationsDao.getArchivedNotifications().firstOrNull() ?: emptyList()

        val critical = unread.filter { it.priority == 5 }
        val high = unread.filter { it.priority == 4 }
        val medium = unread.filter { it.priority == 3 }
        val low = unread.filter { it.priority <= 2 }

        val typeStats = notificationsDao.getNotificationTypeDistribution().firstOrNull() ?: emptyList()
        val priorityStats = notificationsDao.getPriorityDistribution().firstOrNull() ?: emptyList()

        return InboxDashboard(
            totalUnread = unread.size,
            totalRead = read.size,
            totalArchived = archived.size,
            criticalCount = critical.size,
            highCount = high.size,
            mediumCount = medium.size,
            lowCount = low.size,
            unreadNotifications = unread,
            criticalNotifications = critical,
            typeDistribution = typeStats,
            priorityDistribution = priorityStats
        )
    }

    suspend fun getUserNotificationSummary(userId: Int): UserNotificationSummary {
        val allUserNotifications = notificationsDao.getByUser(userId).firstOrNull() ?: emptyList()

        val unread = allUserNotifications.count { !it.isRead && !it.isArchived }
        val read = allUserNotifications.count { it.isRead && !it.isArchived }
        val archived = allUserNotifications.count { it.isArchived }

        val lastWeek = allUserNotifications.filter {
            it.timestamp > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        }

        return UserNotificationSummary(
            userId = userId,
            totalNotifications = allUserNotifications.size,
            unreadCount = unread,
            readCount = read,
            archivedCount = archived,
            lastWeekCount = lastWeek.size,
            recentNotifications = allUserNotifications.take(10)
        )
    }
}

// ============ DATA CLASSES ============

data class InboxDashboard(
    val totalUnread: Int,
    val totalRead: Int,
    val totalArchived: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val unreadNotifications: List<NotificationsEntity>,
    val criticalNotifications: List<NotificationsEntity>,
    val typeDistribution: List<NotificationTypeStats>,
    val priorityDistribution: List<PriorityStats>
)

data class UserNotificationSummary(
    val userId: Int,
    val totalNotifications: Int,
    val unreadCount: Int,
    val readCount: Int,
    val archivedCount: Int,
    val lastWeekCount: Int,
    val recentNotifications: List<NotificationsEntity>
)
