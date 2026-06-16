package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(
    tableName = "shortlist",
    foreignKeys = [
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["player_id"], unique = true)]
)
data class ShortlistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int,
    @Json(name = "added_date")
    @ColumnInfo(name = "added_date")
    val addedDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "priority")
    val priority: Int = 1, // 1: Low, 2: Medium, 3: High
    @ColumnInfo(name = "notes")
    val notes: String? = null
)
