package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rankings_cache")
data class RankingsCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "type")
    val type: String, // LEAGUE, CLUB, MANAGER

    @ColumnInfo(name = "json_data")
    val jsonData: String,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)
