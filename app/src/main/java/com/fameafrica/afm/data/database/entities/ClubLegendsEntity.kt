package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "club_legends",
    foreignKeys = [
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["player_id"]),
        Index(value = ["player_name"]), // Not unique
        Index(value = ["club_name"]),
        Index(value = ["major_titles_won"], orders = [Index.Order.DESC]),
        Index(value = ["status"])
    ]
)
data class ClubLegendsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "club_name")
    val clubName: String,

    @ColumnInfo(name = "years_played")
    val yearsPlayed: Int,

    @ColumnInfo(name = "major_titles_won", defaultValue = "0")
    val majorTitlesWon: Int = 0,

    @ColumnInfo(name = "status", defaultValue = "Retired")
    val status: String = "Retired"
) {

    // ============ COMPUTED PROPERTIES ============

    val isActive: Boolean
        get() = status == "Active"

    val isRetired: Boolean
        get() = status == "Retired"

    val isHonored: Boolean
        get() = status == "Honored" || status == "Legend"

    val titlesPerYear: Double
        get() = if (yearsPlayed > 0) majorTitlesWon.toDouble() / yearsPlayed else 0.0

    val legendStatus: String
        get() = when {
            majorTitlesWon >= 10 -> "Iconic Legend"
            majorTitlesWon >= 7 -> "Club Legend"
            majorTitlesWon >= 4 -> "Cult Hero"
            majorTitlesWon >= 1 -> "Honored Member"
            else -> "Loyal Servant"
        }

    val yearsActive: String
        get() = "$yearsPlayed year${if (yearsPlayed > 1) "s" else ""}"
}

// ============ ENUMS ============

enum class LegendStatus(val value: String) {
    ACTIVE("Active"),
    RETIRED("Retired"),
    HONORED("Honored"),
    LEGEND("Legend")
}
