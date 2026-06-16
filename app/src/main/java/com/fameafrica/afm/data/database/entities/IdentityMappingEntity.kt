package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "identity_mappings",
    indices = [
        Index(value = ["source_name", "category"], unique = true),
        Index(value = ["target_id"])
    ]
)
data class IdentityMappingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "source_name")
    val sourceName: String,

    @ColumnInfo(name = "target_id")
    val targetId: Int,

    @ColumnInfo(name = "category")
    val category: String, // e.g., "TEAM_RIVAL", "NATIONAL_RIVAL", "PLAYER_AGENT"

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
