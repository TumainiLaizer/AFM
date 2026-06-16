package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "player_reactions",
    foreignKeys = [
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["reaction_type"]),
        Index(value = ["player_id", "reaction_type"])
    ]
)
data class PlayerReactionsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @Json(name = "player_name")
    @ColumnInfo(name = "player_name")
    val playerName: String,

    @Json(name = "reaction_type")
    @ColumnInfo(name = "reaction_type", defaultValue = "Neutral")
    val reactionType: String = "Neutral",

    @Json(name = "reaction_text")
    @ColumnInfo(name = "reaction_text")
    val reactionText: String
) {

    // ============ COMPUTED PROPERTIES ============

    val isPositive: Boolean
        get() = reactionType == "Happy" || reactionType == "Excited" || reactionType == "Proud"

    val isNegative: Boolean
        get() = reactionType == "Angry" || reactionType == "Frustrated" || reactionType == "Disappointed" || reactionType == "Sad"

    val isNeutral: Boolean
        get() = reactionType == "Neutral"

    val reactionEmoji: String
        get() = when (reactionType) {
            "Happy" -> "😊"
            "Excited" -> "🎉"
            "Proud" -> "🏆"
            "Angry" -> "😠"
            "Frustrated" -> "😤"
            "Disappointed" -> "😞"
            "Sad" -> "😢"
            "Neutral" -> "😐"
            "Thoughtful" -> "🤔"
            else -> "💬"
        }
}

// ============ ENUMS ============

enum class PlayerReactionType(val value: String) {
    HAPPY("Happy"),
    EXCITED("Excited"),
    PROUD("Proud"),
    ANGRY("Angry"),
    FRUSTRATED("Frustrated"),
    DISAPPOINTED("Disappointed"),
    SAD("Sad"),
    NEUTRAL("Neutral"),
    THOUGHTFUL("Thoughtful");

    companion object {
        fun fromString(value: String): PlayerReactionType? {
            return values().find { it.value == value }
        }
    }
}
