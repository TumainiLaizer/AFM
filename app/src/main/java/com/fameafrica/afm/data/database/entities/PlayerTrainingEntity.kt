package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "player_training",
    foreignKeys = [
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StaffEntity::class,
            parentColumns = ["id"],
            childColumns = ["coach_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["player_name"]),
        Index(value = ["coach_id"]),
        Index(value = ["drill_type"]),
        Index(value = ["focus_area"]),
        Index(value = ["start_date"]),
        Index(value = ["status"]),
        Index(value = ["progress"]),
        Index(value = ["injury_risk"])
    ]
)
data class PlayerTrainingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @Json(name = "player_name")
    @ColumnInfo(name = "player_name")
    val playerName: String,

    @Json(name = "coach_id")
    @ColumnInfo(name = "coach_id")
    val coachId: Int? = null,

    @Json(name = "coach_name")
    @ColumnInfo(name = "coach_name")
    val coachName: String? = null,

    @Json(name = "drill_type")
    @ColumnInfo(name = "drill_type")
    val drillType: String,  // TECHNICAL, TACTICAL, PHYSICAL, MENTAL, GOALKEEPING

    @Json(name = "focus_area")
    @ColumnInfo(name = "focus_area")
    val focusArea: String,  // FINISHING, PASSING, DEFENDING, SPEED, etc.

    @Json(name = "specific_attribute")
    @ColumnInfo(name = "specific_attribute")
    val specificAttribute: String? = null,  // The specific attribute being trained

    @Json(name = "start_date")
    @ColumnInfo(name = "start_date")
    val startDate: String,

    @Json(name = "end_date")
    @ColumnInfo(name = "end_date")
    val endDate: String? = null,

    @Json(name = "duration_days")
    @ColumnInfo(name = "duration_days")
    val durationDays: Int = 7,

    @Json(name = "sessions_completed")
    @ColumnInfo(name = "sessions_completed")
    val sessionsCompleted: Int = 0,

    @Json(name = "total_sessions")
    @ColumnInfo(name = "total_sessions")
    val totalSessions: Int = 5,

    @ColumnInfo(name = "progress", defaultValue = "0")
    val progress: Int = 0,  // 0-100

    @Json(name = "injury_risk")
    @ColumnInfo(name = "injury_risk", defaultValue = "5")
    val injuryRisk: Int = 5,  // 0-100

    @Json(name = "fatigue_level")
    @ColumnInfo(name = "fatigue_level")
    val fatigueLevel: Int = 0,  // 0-100

    @ColumnInfo(name = "status")
    val status: String = "ACTIVE",  // ACTIVE, COMPLETED, PAUSED, CANCELLED

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @Json(name = "result_rating")
    @ColumnInfo(name = "result_rating")
    val resultRating: Int? = null,  // 0-100 effectiveness

    @Json(name = "attribute_before")
    @ColumnInfo(name = "attribute_before")
    val attributeBefore: Int? = null,

    @Json(name = "attribute_after")
    @ColumnInfo(name = "attribute_after")
    val attributeAfter: Int? = null,

    @Json(name = "improvement_amount")
    @ColumnInfo(name = "improvement_amount")
    val improvementAmount: Int? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val progressPercentage: Int
        get() = progress

    val isActive: Boolean
        get() = status == "ACTIVE"

    val isCompleted: Boolean
        get() = status == "COMPLETED"

    val sessionsProgress: String
        get() = "$sessionsCompleted/$totalSessions"

    val riskLevel: String
        get() = when {
            injuryRisk >= 80 -> "VERY HIGH"
            injuryRisk >= 60 -> "HIGH"
            injuryRisk >= 40 -> "MEDIUM"
            injuryRisk >= 20 -> "LOW"
            else -> "VERY LOW"
        }

    val fatigueLevelDescription: String
        get() = when {
            fatigueLevel >= 80 -> "EXHAUSTED"
            fatigueLevel >= 60 -> "VERY TIRED"
            fatigueLevel >= 40 -> "TIRED"
            fatigueLevel >= 20 -> "NORMAL"
            else -> "FRESH"
        }
}

// ============ ENUMS ============

enum class DrillType(val value: String) {
    TECHNICAL("TECHNICAL"),
    TACTICAL("TACTICAL"),
    PHYSICAL("PHYSICAL"),
    MENTAL("MENTAL"),
    GOALKEEPING("GOALKEEPING"),
    RECOVERY("RECOVERY")
}

enum class FocusArea(val value: String) {
    // Technical
    FINISHING("FINISHING"),
    PASSING("PASSING"),
    DRIBBLING("DRIBBLING"),
    CROSSING("CROSSING"),
    HEADING("HEADING"),
    LONG_SHOTS("LONG_SHOTS"),

    // Defensive
    TACKLING("TACKLING"),
    MARKING("MARKING"),
    POSITIONING("POSITIONING"),

    // Physical
    SPEED("SPEED"),
    STAMINA("STAMINA"),
    STRENGTH("STRENGTH"),
    AGILITY("AGILITY"),

    // Mental
    COMPOSURE("COMPOSURE"),
    DECISIONS("DECISIONS"),
    ANTICIPATION("ANTICIPATION"),
    LEADERSHIP("LEADERSHIP"),

    // Goalkeeping
    REFLEXES("REFLEXES"),
    HANDLING("HANDLING"),
    AERIAL("AERIAL"),
    KICKING("KICKING")
}

enum class TrainingStatus(val value: String) {
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED"),
    PAUSED("PAUSED"),
    CANCELLED("CANCELLED")
}
