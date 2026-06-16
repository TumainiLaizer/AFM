package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_filter_presets")
data class PlayerFilterPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "preset_name")
    val presetName: String,
    @ColumnInfo(name = "filter_json")
    val filterJson: String, // Store as JSON string for flexibility
    @ColumnInfo(name = "is_system_preset")
    val isSystemPreset: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
