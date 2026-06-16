package com.fameafrica.afm.data.database.entities

import androidx.room.*
import com.squareup.moshi.Json

@Entity(
    tableName = "training_days",
    foreignKeys = [
        ForeignKey(
            entity = TrainingScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["schedule_id"]),
        Index(value = ["date"])
    ]
)
data class TrainingDayEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "schedule_id")
    @ColumnInfo(name = "schedule_id")
    val scheduleId: Int,

    @ColumnInfo(name = "date")
    val date: String, // YYYY-MM-DD

    @Json(name = "morning_session")
    @ColumnInfo(name = "morning_session")
    val morningSession: String, // Enum: TrainingSessionType

    @Json(name = "afternoon_session")
    @ColumnInfo(name = "afternoon_session")
    val afternoonSession: String,

    @Json(name = "evening_session")
    @ColumnInfo(name = "evening_session")
    val eveningSession: String,

    @ColumnInfo(name = "intensity_modifier")
    val intensityModifier: Float = 1.0f,

    @ColumnInfo(name = "recovery_level")
    val recoveryLevel: Int = 0 // Extra recovery focus for this day
)

enum class TrainingSessionType(val value: String) {
    REST("REST"),
    RECOVERY("RECOVERY"),
    TACTICAL_SHAPE("TACTICAL_SHAPE"),
    ATTACKING_MOVEMENT("ATTACKING_MOVEMENT"),
    DEFENSIVE_ORGANIZATION("DEFENSIVE_ORGANIZATION"),
    SET_PIECES("SET_PIECES"),
    PHYSICAL_CONDITIONING("PHYSICAL_CONDITIONING"),
    TECHNICAL_DRILLS("TECHNICAL_DRILLS"),
    MATCH_PREP("MATCH_PREP"),
    MATCH_DAY("MATCH_DAY"),
    TEAM_BONDING("TEAM_BONDING"),
    GYM_WORK("GYM_WORK")
}
