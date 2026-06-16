package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "agent_clients",
    foreignKeys = [
        ForeignKey(
            entity = PlayerAgentsEntity::class,
            parentColumns = ["id"],
            childColumns = ["agent_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["agent_id"]),
        Index(value = ["player_id"], unique = true)
    ]
)
data class AgentClientsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @param:Json(name = "agent_id")
    @field:ColumnInfo(name = "agent_id")
    val agentId: Int,
    @param:Json(name = "player_id")
    @field:ColumnInfo(name = "player_id")
    val playerId: Int,
    @param:Json(name = "contract_start_date")
    @field:ColumnInfo(name = "contract_start_date")
    val contractStartDate: String,
    @param:Json(name = "contract_end_date")
    @field:ColumnInfo(name = "contract_end_date")
    val contractEndDate: String
)
