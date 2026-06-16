package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "rivalries")
data class RivalryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Json(name = "team_a_id") @ColumnInfo(name = "team_a_id") val teamAId: Int,
    @Json(name = "team_b_id") @ColumnInfo(name = "team_b_id") val teamBId: Int,
    @Json(name = "rivalry_name") @ColumnInfo(name = "rivalry_name") val rivalryName: String,
    @ColumnInfo(name = "intensity") val intensity: Double, // 0.0 to 1.0
    @Json(name = "last_results") @ColumnInfo(name = "last_results") val lastResults: String // JSON List of scores
)
