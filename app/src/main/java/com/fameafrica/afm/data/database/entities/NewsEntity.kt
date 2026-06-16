package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "news",
    foreignKeys = [
        ForeignKey(
            entity = JournalistsEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalist_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category"]),
        Index(value = ["journalist_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["is_top_news"]),
        Index(value = ["related_team_id"]),
        Index(value = ["related_player_id"]),
        Index(value = ["related_manager_id"])
    ]
)
data class NewsEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @field:ColumnInfo(name = "headline")
    val headline: String,

    @field:ColumnInfo(name = "content")
    val content: String,

    @field:ColumnInfo(name = "category")
    val category: String,  // MATCH, TRANSFER, INJURY, INTERVIEW, PRESS_CONFERENCE, BOARD, FANS, RUMOR, AWARD

    @param:Json(name = "journalist_id")
    @field:ColumnInfo(name = "journalist_id")
    val journalistId: Int? = null,

    @param:Json(name = "journalist_name")
    @field:ColumnInfo(name = "journalist_name")
    val journalistName: String?,

    @param:Json(name = "journalist_logo")
    @field:ColumnInfo(name = "journalist_logo")
    val journalistLogo: String?,

    @field:ColumnInfo(name = "timestamp")
    val timestamp: String,

    @param:Json(name = "is_top_news")
    @field:ColumnInfo(name = "is_top_news", defaultValue = "1")
    val isTopNews: Int = 1,

    @param:Json(name = "related_team_id")
    @field:ColumnInfo(name = "related_team_id")
    val relatedTeamId: Int? = null,

    @param:Json(name = "related_team")
    @field:ColumnInfo(name = "related_team")
    val relatedTeam: String? = null,

    @param:Json(name = "related_player_id")
    @field:ColumnInfo(name = "related_player_id")
    val relatedPlayerId: Int? = null,

    @param:Json(name = "related_player")
    @field:ColumnInfo(name = "related_player")
    val relatedPlayer: String? = null,

    @param:Json(name = "related_manager_id")
    @field:ColumnInfo(name = "related_manager_id")
    val relatedManagerId: Int? = null,

    @param:Json(name = "related_manager")
    @field:ColumnInfo(name = "related_manager")
    val relatedManager: String? = null,

    @param:Json(name = "image_url")
    @field:ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @field:ColumnInfo(name = "views")
    val views: Int = 0,

    @field:ColumnInfo(name = "likes")
    val likes: Int = 0,

    @field:ColumnInfo(name = "comments")
    val comments: Int = 0
) {

    // ============ COMPUTED PROPERTIES ============

    val formattedTimestamp: String
        get() {
            return try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(timestamp) ?: return timestamp
                val now = Date()
                val diff = now.time - date.time

                when {
                    diff < 60000 -> "Just now"
                    diff < 3600000 -> "${diff / 60000} minutes ago"
                    diff < 86400000 -> "${diff / 3600000} hours ago"
                    else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                timestamp
            }
        }

    val categoryColor: String
        get() = when (category) {
            "TRANSFER" -> "Blue"
            "MATCH" -> "Green"
            "INJURY" -> "Red"
            "INTERVIEW" -> "Purple"
            "PRESS_CONFERENCE" -> "Orange"
            "BOARD" -> "Brown"
            "FANS" -> "Yellow"
            "RUMOR" -> "Pink"
            "AWARD" -> "Gold"
            else -> "Gray"
        }
}

// ============ ENUMS ============

enum class NewsCategory(val value: String) {
    MATCH("MATCH"),
    TRANSFER("TRANSFER"),
    INJURY("INJURY"),
    INTERVIEW("INTERVIEW"),
    PRESS_CONFERENCE("PRESS_CONFERENCE"),
    BOARD("BOARD"),
    FANS("FANS"),
    RUMOR("RUMOR"),
    AWARD("AWARD"),
    ANNOUNCEMENT("ANNOUNCEMENT")
}
