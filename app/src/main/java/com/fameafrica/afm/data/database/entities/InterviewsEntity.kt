package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "interviews",
    foreignKeys = [
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"],
            childColumns = ["interviewee_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JournalistsEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalist_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["interviewee_id"]),
        Index(value = ["player_id"]),
        Index(value = ["journalist_id"]),
        Index(value = ["journalist_name"]),
        Index(value = ["status"]),
        Index(value = ["interview_type"]),
        Index(value = ["topic"]),
        Index(value = ["date_requested"])
    ]
)
data class InterviewsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "interviewee_id")
    val intervieweeId: Int? = null,  // Manager ID if interviewee is manager

    @ColumnInfo(name = "player_id")
    val playerId: Int? = null,       // Player ID if interviewee is player

    @ColumnInfo(name = "interviewee_name")
    val intervieweeName: String,

    @ColumnInfo(name = "interviewee_type")
    val intervieweeType: String,     // MANAGER, PLAYER

    @ColumnInfo(name = "journalist_id")
    val journalistId: Int,

    @ColumnInfo(name = "journalist_name")
    val journalistName: String,

    @ColumnInfo(name = "journalist_personality")
    val journalistPersonality: String,

    @ColumnInfo(name = "date_requested")
    val dateRequested: String,

    @ColumnInfo(name = "interview_date")
    val interviewDate: String? = null,

    @ColumnInfo(name = "status", defaultValue = "Pending")
    val status: String = "Pending",  // Pending, Scheduled, Completed, Declined

    @ColumnInfo(name = "interview_type")
    val interviewType: String,      // POST_MATCH, TRANSFER_RUMOR, CONTRACT, FORM, LOAN, UNSETTLED, EXCLUSIVE

    @ColumnInfo(name = "topic")
    val topic: String,              // Brief description of interview topic

    @ColumnInfo(name = "question")
    val question: String,

    @ColumnInfo(name = "response")
    val response: String? = null,

    @ColumnInfo(name = "response_type")
    val responseType: String? = null,  // POSITIVE, NEUTRAL, NEGATIVE

    @ColumnInfo(name = "impact_on_morale")
    val impactOnMorale: Int = 0,

    @ColumnInfo(name = "reputation_change")
    val reputationChange: Int = 0,

    @ColumnInfo(name = "fan_popularity_change")
    val fanPopularityChange: Int = 0,

    @ColumnInfo(name = "is_published")
    val isPublished: Boolean = false,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val isPending: Boolean
        get() = status == "Pending"

    val isScheduled: Boolean
        get() = status == "Scheduled"

    val isCompleted: Boolean
        get() = status == "Completed"

    val isDeclined: Boolean
        get() = status == "Declined"

    val isManagerInterview: Boolean
        get() = intervieweeType == "MANAGER"

    val isPlayerInterview: Boolean
        get() = intervieweeType == "PLAYER"

    val impactColor: String
        get() = when {
            impactOnMorale > 0 -> "Positive"
            impactOnMorale < 0 -> "Negative"
            else -> "Neutral"
        }
}

// ============ ENUMS ============

enum class IntervieweeType(val value: String) {
    MANAGER("MANAGER"),
    PLAYER("PLAYER")
}

enum class InterviewStatus(val value: String) {
    PENDING("Pending"),
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    DECLINED("Declined")
}

enum class InterviewType(val value: String) {
    POST_MATCH("Post-Match"),
    TRANSFER_RUMOR("Transfer Rumor"),
    CONTRACT("Contract"),
    FORM("Form"),
    LOAN("Loan"),
    UNSETTLED("Unsettled"),
    EXCLUSIVE("Exclusive"),
    INJURY("Injury"),
    DERBY("Derby"),
    TROPHY("Trophy")
}
