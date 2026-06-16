package com.fameafrica.afm.data.database.entities

import androidx.room.*

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["notification_type"]),
        Index(value = ["priority"]),
        Index(value = ["isRead"]),
        Index(value = ["timestamp"]),
        Index(value = ["user_id"]),
        Index(value = ["related_entity_type", "related_entity_id"])
    ]
)
data class NotificationsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String?,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "isRead")
    val isRead: Boolean = false,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "notification_type")
    val notificationType: String,  // e.g. MATCH_RESULT, INJURY_UPDATE, TRANSFER_OFFER

    @ColumnInfo(name = "priority")
    val priority: Int = 1,  // 1-5, 1 = highest priority

    @ColumnInfo(name = "icon")
    val icon: String?,

    @ColumnInfo(name = "image_url")
    val imageUrl: String?,

    @ColumnInfo(name = "action_url")
    val actionUrl: String?,

    @ColumnInfo(name = "action_text")
    val actionText: String?,

    @ColumnInfo(name = "related_entity_type")
    val relatedEntityType: String?,  // e.g. "PLAYER", "MATCH", "TRANSFER", "TROPHY"

    @ColumnInfo(name = "related_entity_id")
    val relatedEntityId: Int?,

    @ColumnInfo(name = "related_entity_name")
    val relatedEntityName: String?,

    @ColumnInfo(name = "user_id")
    val userId: Int?,

    @ColumnInfo(name = "expiry_time")
    val expiryTime: Long?,

    @ColumnInfo(name = "dismissible")
    val dismissible: Boolean = true,

    @ColumnInfo(name = "color")
    val color: String?,  // Optional accent color (hex code)

    @ColumnInfo(name = "data_json")
    val dataJson: String?  // Additional JSON data for complex notifications
) {

    // ============ COMPUTED PROPERTIES ============

    @get:Ignore
    val isExpired: Boolean
        get() = expiryTime?.let { System.currentTimeMillis() > it } ?: false

    @get:Ignore
    val priorityLevel: NotificationPriority
        get() = when (priority) {
            1 -> NotificationPriority.CRITICAL
            2 -> NotificationPriority.HIGH
            3 -> NotificationPriority.MEDIUM
            4 -> NotificationPriority.LOW
            5 -> NotificationPriority.INFO
            else -> NotificationPriority.MEDIUM
        }

    @get:Ignore
    val timeAgo: String
        get() {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
                else -> "${diff / (7 * 24 * 60 * 60 * 1000)} weeks ago"
            }
        }

    @get:Ignore
    val formattedTime: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }

    @get:Ignore
    val notificationIcon: String
        get() = icon ?: "🔔"

    @get:Ignore
    val displayColor: Int
        get() = color?.removePrefix("#")?.toIntOrNull(16) ?: 0

    // ============ BUSINESS METHODS ============

    fun markAsRead(): NotificationsEntity {
        return this.copy(isRead = true)
    }

    fun markAsUnread(): NotificationsEntity {
        return this.copy(isRead = false)
    }

    fun archive(): NotificationsEntity {
        return this.copy(isArchived = true)
    }

    fun unarchive(): NotificationsEntity {
        return this.copy(isArchived = false)
    }

    fun dismiss(): NotificationsEntity? {
        return if (dismissible) {
            this.copy(isArchived = true)
        } else null
    }

    fun updateData(jsonData: String): NotificationsEntity {
        return this.copy(dataJson = jsonData)
    }

    companion object {
        fun createMatchResultNotification(
            matchId: Int,
            homeTeam: String,
            awayTeam: String,
            homeScore: Int,
            awayScore: Int,
            result: String,
            userId: Int? = null
        ): NotificationsEntity {
            val title = if (homeScore > awayScore) "Victory!" else if (homeScore < awayScore) "Defeat" else "Draw"
            val message = "$homeTeam $homeScore - $awayScore $awayTeam: $result"

            return NotificationsEntity(
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.MATCH_RESULT.value,
                priority = 2,
                icon = if (homeScore > awayScore) "🏆" else "⚽",
                relatedEntityType = "MATCH",
                relatedEntityId = matchId,
                relatedEntityName = "$homeTeam vs $awayTeam",
                userId = userId,
                dismissible = true,
                color = if (homeScore > awayScore) "#0F5A36" else "#9E1B1B",
                id = 0, // Let Room handle auto-generation
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                actionText = null,
                expiryTime = null,
                dataJson = null
            )
        }

        fun createTransferOfferNotification(
            transferId: Int,
            playerName: String,
            fromTeam: String,
            toTeam: String,
            fee: Int,
            userId: Int? = null
        ): NotificationsEntity {
            return NotificationsEntity(
                title = "Transfer Offer Received",
                message = "$playerName: $fromTeam → $toTeam (€${fee / 1_000_000}M)",
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.TRANSFER_OFFER.value,
                priority = 1,
                icon = "💰",
                actionText = "View Offer",
                relatedEntityType = "TRANSFER",
                relatedEntityId = transferId,
                relatedEntityName = playerName,
                userId = userId,
                dismissible = true,
                color = "#D4A017",
                id = 0,
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                expiryTime = null,
                dataJson = null
            )
        }

        fun createInjuryNotification(
            playerId: Int,
            playerName: String,
            injuryType: String,
            recoveryDays: Int,
            userId: Int? = null
        ): NotificationsEntity {
            return NotificationsEntity(
                title = "Injury Update",
                message = "$playerName out for $recoveryDays days with $injuryType",
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.INJURY_UPDATE.value,
                priority = 1,
                icon = "🏥",
                actionText = "View Player",
                relatedEntityType = "PLAYER",
                relatedEntityId = playerId,
                relatedEntityName = playerName,
                userId = userId,
                dismissible = true,
                color = "#FF7A00",
                id = 0,
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                expiryTime = null,
                dataJson = null
            )
        }

        fun createTrophyWonNotification(
            trophyId: Int,
            trophyName: String,
            teamName: String,
            season: String,
            userId: Int? = null
        ): NotificationsEntity {
            return NotificationsEntity(
                title = "🏆 Trophy Won!",
                message = "$teamName won the $trophyName in season $season",
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.TROPHY_WON.value,
                priority = 1,
                icon = "🏆",
                actionText = "View Trophy",
                relatedEntityType = "TROPHY",
                relatedEntityId = trophyId,
                relatedEntityName = trophyName,
                userId = userId,
                dismissible = true,
                color = "#D4A017",
                id = 0,
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                expiryTime = null,
                dataJson = null
            )
        }

        fun createContractExpiryNotification(
            playerId: Int,
            playerName: String,
            expiryDate: String,
            daysRemaining: Int,
            userId: Int? = null
        ): NotificationsEntity {
            return NotificationsEntity(
                title = "Contract Expiring Soon",
                message = "$playerName's contract expires in $daysRemaining days ($expiryDate)",
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.CONTRACT_EXPIRY.value,
                priority = 2,
                icon = "📄",
                actionText = "Renegotiate",
                relatedEntityType = "PLAYER",
                relatedEntityId = playerId,
                relatedEntityName = playerName,
                userId = userId,
                dismissible = true,
                color = "#6B4F2A",
                id = 0,
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                expiryTime = null,
                dataJson = null
            )
        }

        fun createBoardMessageNotification(
            title: String,
            message: String,
            priority: Int = 2,
            userId: Int? = null
        ): NotificationsEntity {
            return NotificationsEntity(
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.BOARD_MESSAGE.value,
                priority = priority,
                icon = "📋",
                actionText = "View",
                userId = userId,
                dismissible = true,
                color = "#6B4F2A",
                id = 0,
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                relatedEntityType = null,
                relatedEntityId = null,
                relatedEntityName = null,
                expiryTime = null,
                dataJson = null
            )
        }

        fun createYouthIntakeNotification(
            playerId: Int,
            playerName: String,
            potential: Int,
            userId: Int? = null
        ): NotificationsEntity {
            return NotificationsEntity(
                title = "🌟 Youth Intake Prospect",
                message = "$playerName (Potential: $potential) has joined the academy",
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.YOUTH_INTAKE.value,
                priority = 3,
                icon = "🌟",
                actionText = "View Player",
                relatedEntityType = "PLAYER",
                relatedEntityId = playerId,
                relatedEntityName = playerName,
                userId = userId,
                dismissible = true,
                color = "#00A86B",
                id = 0,
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                expiryTime = null,
                dataJson = null
            )
        }

        fun createSystemNotification(
            title: String,
            message: String,
            priority: Int = 3
        ): NotificationsEntity {
            return NotificationsEntity(
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                notificationType = NotificationType.SYSTEM.value,
                priority = priority,
                icon = "ℹ️",
                actionText = null,
                userId = null,
                dismissible = true,
                color = "#C0C0C0",
                id = 0,
                isRead = false,
                isArchived = false,
                imageUrl = null,
                actionUrl = null,
                relatedEntityType = null,
                relatedEntityId = null,
                relatedEntityName = null,
                expiryTime = null,
                dataJson = null
            )
        }
    }
}

// ============ ENUMS ============

enum class NotificationType(val value: String) {
    MATCH_RESULT("MATCH_RESULT"),
    MATCH("MATCH"),
    TRANSFER("TRANSFER"),
    TRANSFER_OFFER("TRANSFER_OFFER"),
    TRANSFER_COMPLETED("TRANSFER_COMPLETED"),
    LOAN_OFFER("LOAN_OFFER"),
    LOAN_COMPLETED("LOAN_COMPLETED"),
    INJURY("INJURY"),
    INJURY_UPDATE("INJURY_UPDATE"),
    INJURY_RECOVERY("INJURY_RECOVERY"),
    SUSPENSION("SUSPENSION"),
    CONTRACT("CONTRACT"),
    CONTRACT_OFFER("CONTRACT_OFFER"),
    CONTRACT_RENEWED("CONTRACT_RENEWED"),
    CONTRACT_EXPIRY("CONTRACT_EXPIRY"),
    SCOUT("SCOUT"),
    SCOUT_ASSIGNMENT("SCOUT_ASSIGNMENT"),
    TROPHY_WON("TROPHY_WON"),
    BOARD_MESSAGE("BOARD_MESSAGE"),
    BOARD_EVALUATION("BOARD_EVALUATION"),
    FAN_MESSAGE("FAN_MESSAGE"),
    MEDIA("MEDIA"),
    TROPHY("TROPHY"),
    OFFER("OFFER"),
    REMINDER("REMINDER"),
    NEWS("NEWS"),
    ARTICLE("ARTICLE"),
    MEDIA_INTERVIEW("MEDIA_INTERVIEW"),
    JOURNALIST_ARTICLE("JOURNALIST_ARTICLE"),
    TRAINING_COMPLETED("TRAINING_COMPLETED"),
    YOUTH_INTAKE("YOUTH_INTAKE"),
    YOUTH_GRADUATION("YOUTH_GRADUATION"),
    STAFF_CHANGE("STAFF_CHANGE"),
    FINANCIAL_UPDATE("FINANCIAL_UPDATE"),
    SPONSORSHIP_DEAL("SPONSORSHIP_DEAL"),
    SYSTEM("SYSTEM"),
    ACHIEVEMENT("ACHIEVEMENT"),
    MILESTONE("MILESTONE")
}

enum class RelatedEntityType(val value: String) {
    PLAYER("PLAYER"),
    MATCH("MATCH"),
    TRANSFER("TRANSFER"),
    LOAN("LOAN"),
    CONTRACT("CONTRACT"),
    TROPHY("TROPHY"),
    STAFF("STAFF"),
    SPONSOR("SPONSOR"),
    FINANCE("FINANCE"),
    TRAINING("TRAINING")
}
