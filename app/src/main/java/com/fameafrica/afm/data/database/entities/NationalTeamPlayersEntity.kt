package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "national_team_players",
    primaryKeys = ["national_team_id", "player_id"],
    foreignKeys = [
        ForeignKey(
            entity = NationalTeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["national_team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["national_team_id"]),
        Index(value = ["player_id"]),
        Index(value = ["role"]),
        Index(value = ["national_team_id", "role"])
    ]
)
data class NationalTeamPlayersEntity(
    @Json(name = "national_team_id")
    @ColumnInfo(name = "national_team_id")
    val nationalTeamId: Int,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "role")
    val role: String?  // STARTER, RESERVE, CAPTAIN
) {
    // ============ COMPUTED PROPERTIES ============

    val isCaptain: Boolean
        get() = role == "CAPTAIN"

    val isStarter: Boolean
        get() = role == "STARTER"

    val isReserve: Boolean
        get() = role == "RESERVE"
}

// ============ ENUMS ============

enum class NationalTeamRole(val value: String) {
    STARTER("STARTER"),
    RESERVE("RESERVE"),
    CAPTAIN("CAPTAIN")
}