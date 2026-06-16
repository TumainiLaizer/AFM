package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "fan_expectations",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_id"], unique = true),
        Index(value = ["confidence_level"]),
        Index(value = ["board_trust"])
    ]
)
data class FanExpectationsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @Json(name = "team_name")
    @ColumnInfo(name = "team_name")
    val teamName: String,

    @Json(name = "confidence_level")
    @ColumnInfo(name = "confidence_level", defaultValue = "50")
    val confidenceLevel: Int = 50,

    @Json(name = "recent_performance")
    @ColumnInfo(name = "recent_performance")
    val recentPerformance: String? = null,

    @Json(name = "board_trust")
    @ColumnInfo(name = "board_trust", defaultValue = "50")
    val boardTrust: Int = 50
) {

    // ============ COMPUTED PROPERTIES ============

    val confidenceLevelString: String
        get() = when {
            confidenceLevel >= 90 -> "Euphoric"
            confidenceLevel >= 75 -> "Very Confident"
            confidenceLevel >= 60 -> "Confident"
            confidenceLevel >= 45 -> "Cautious"
            confidenceLevel >= 30 -> "Disappointed"
            confidenceLevel >= 15 -> "Angry"
            else -> "Furious"
        }

    val trustLevelString: String
        get() = when {
            boardTrust >= 80 -> "Complete Trust"
            boardTrust >= 60 -> "Trusting"
            boardTrust >= 40 -> "Neutral"
            boardTrust >= 20 -> "Skeptical"
            else -> "Hostile"
        }

    val isPositive: Boolean
        get() = confidenceLevel >= 60 && boardTrust >= 60

    val isNegative: Boolean
        get() = confidenceLevel <= 40 || boardTrust <= 40

    val isCritical: Boolean
        get() = confidenceLevel <= 25 && boardTrust <= 25

    val overallMood: String
        get() = when {
            isCritical -> "Critical"
            isNegative -> "Negative"
            isPositive -> "Positive"
            else -> "Neutral"
        }
}
