package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "player_form")
data class PlayerFormEntity(
    @PrimaryKey val playerId: Int,
    @Json(name = "last_5_ratings") @ColumnInfo(name = "last_5_ratings") val last5Ratings: String, // JSON List<Double>
    @Json(name = "goals_last_5") @ColumnInfo(name = "goals_last_5") val goalsLast5: Int,
    @Json(name = "assists_last_5") @ColumnInfo(name = "assists_last_5") val assistsLast5: Int,
    @Json(name = "cleansheets_last_5") @ColumnInfo(name = "cleansheets_last_5") val cleansheetsLast5: Int,
    @Json(name = "form_status") @ColumnInfo(name = "form_status") val formStatus: String // HOT, GOOD, AVERAGE, POOR
)
