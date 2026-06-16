package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "training_schedules",
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["month", "year"])
    ]
)
data class TrainingScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "month")
    val month: Int,

    @ColumnInfo(name = "year")
    val year: Int,

    @Json(name = "is_approved")
    @ColumnInfo(name = "is_approved", defaultValue = "0")
    val isApproved: Boolean = false,

    @Json(name = "global_intensity")
    @ColumnInfo(name = "global_intensity", defaultValue = "NORMAL")
    val globalIntensity: String = "NORMAL", // LOW, NORMAL, HIGH, EXTREME

    @Json(name = "primary_focus")
    @ColumnInfo(name = "primary_focus", defaultValue = "BALANCED")
    val primaryFocus: String = "BALANCED", // TACTICAL, TECHNICAL, PHYSICAL, RECOVERY, MATCH_PREP

    @Json(name = "generated_by")
    @ColumnInfo(name = "generated_by")
    val generatedBy: String? = null // Staff name
)
