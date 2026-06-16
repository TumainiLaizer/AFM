package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "elo_history",
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
        Index(value = ["current_elo"]),
        Index(value = ["last_updated"])
    ]
)
data class EloHistoryEntity(
    @PrimaryKey
    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @Json(name = "team_name")
    @ColumnInfo(name = "team_name")
    val teamName: String,

    @Json(name = "current_elo")
    @ColumnInfo(name = "current_elo", defaultValue = "1500")
    val currentElo: Int = 1500,

    @Json(name = "previous_elo")
    @ColumnInfo(name = "previous_elo", defaultValue = "1500")
    val previousElo: Int = 1500,

    @Json(name = "highest_elo")
    @ColumnInfo(name = "highest_elo")
    val highestElo: Int? = null,

    @Json(name = "lowest_elo")
    @ColumnInfo(name = "lowest_elo")
    val lowestElo: Int? = null,

    @Json(name = "matches_played_elo")
    @ColumnInfo(name = "matches_played_elo", defaultValue = "0")
    val matchesPlayedElo: Int = 0,

    @Json(name = "elo_change_total")
    @ColumnInfo(name = "elo_change_total", defaultValue = "0")
    val eloChangeTotal: Int = 0,

    @Json(name = "last_updated")
    @ColumnInfo(name = "last_updated")
    val lastUpdated: String? = null,

    @Json(name = "last_match_result")
    @ColumnInfo(name = "last_match_result")
    val lastMatchResult: String? = null,  // WIN, LOSS, DRAW

    @Json(name = "last_opponent")
    @ColumnInfo(name = "last_opponent")
    val lastOpponent: String? = null,

    @Json(name = "last_opponent_elo")
    @ColumnInfo(name = "last_opponent_elo")
    val lastOpponentElo: Int? = null,

    @Json(name = "elo_history_json")
    @ColumnInfo(name = "elo_history_json")
    val eloHistoryJson: String? = null  // JSON array of last 10 Elo values for trending
) {

    // ============ COMPUTED PROPERTIES ============

    val eloChange: Int
        get() = currentElo - previousElo

    val eloChangePercentage: Double
        get() = if (previousElo > 0) (eloChange.toDouble() / previousElo * 100) else 0.0

    val eloTrend: String
        get() = when {
            eloChange > 50 -> "🚀 Rocketing"
            eloChange > 20 -> "📈 Strong Rise"
            eloChange > 5 -> "↗️ Rising"
            eloChange >= -5 -> "➡️ Stable"
            eloChange >= -20 -> "↘️ Falling"
            eloChange >= -50 -> "📉 Strong Drop"
            else -> "💥 Crashing"
        }

    val eloCategory: String
        get() = when {
            currentElo >= 1800 -> "World Class"
            currentElo >= 1700 -> "Continental Elite"
            currentElo >= 1600 -> "Championship Contender"
            currentElo >= 1500 -> "Professional"
            currentElo >= 1400 -> "Semi-Professional"
            else -> "Developing"
        }
}
