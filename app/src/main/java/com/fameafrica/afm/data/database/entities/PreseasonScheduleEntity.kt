package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "preseason_schedule",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["opponent_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["opponent_id"]),
        Index(value = ["team_name"]),
        Index(value = ["opponent"]),
        Index(value = ["match_date"]),
        Index(value = ["status"]),
        Index(value = ["season"])
    ]
)
data class PreseasonScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "season")
    val season: String,

    @ColumnInfo(name = "match_date")
    val matchDate: String,

    @ColumnInfo(name = "opponent_id")
    val opponentId: Int,

    @ColumnInfo(name = "opponent")
    val opponent: String,

    @ColumnInfo(name = "location")
    val location: String? = null,  // Home, Away, Neutral

    @ColumnInfo(name = "stadium")
    val stadium: String? = null,

    @ColumnInfo(name = "status", defaultValue = "Scheduled")
    val status: String = "Scheduled",  // Scheduled, Completed, Cancelled

    @ColumnInfo(name = "home_score")
    val homeScore: Int? = null,

    @ColumnInfo(name = "opponent_score")
    val opponentScore: Int? = null,

    @ColumnInfo(name = "is_user_team")
    val isUserTeam: Boolean = false,  // Whether this is the user's team

    @ColumnInfo(name = "tour_location")
    val tourLocation: String? = null  // Tanzania, Kenya, Uganda, Ghana, Nigeria, South Africa
) {

    // ============ COMPUTED PROPERTIES ============

    val isCompleted: Boolean
        get() = status == "Completed"

    val isScheduled: Boolean
        get() = status == "Scheduled"

    val isCancelled: Boolean
        get() = status == "Cancelled"

    val result: String
        get() = if (isCompleted && homeScore != null && opponentScore != null) {
            "$homeScore - $opponentScore"
        } else "Not Played"

    val didWin: Boolean
        get() = isCompleted && homeScore != null && opponentScore != null && homeScore > opponentScore

    val didLose: Boolean
        get() = isCompleted && homeScore != null && opponentScore != null && homeScore < opponentScore

    val isDraw: Boolean
        get() = isCompleted && homeScore != null && opponentScore != null && homeScore == opponentScore
}

// ============ ENUMS ============

enum class PreseasonStatus(val value: String) {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class TourLocation(val value: String) {
    TANZANIA("Tanzania"),
    KENYA("Kenya"),
    UGANDA("Uganda"),
    RWANDA("Rwanda"),
    EGYPT("Egypt"),
    GHANA("Ghana"),
    NIGERIA("Nigeria"),
    SOUTH_AFRICA("South Africa"),
    ZAMBIA("Zambia"),
    ZIMBABWE("Zimbabwe")
}
