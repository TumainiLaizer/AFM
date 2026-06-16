package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayerFilterPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerFilterPresetDao {
    @Query("SELECT * FROM player_filter_presets ORDER BY created_at DESC")
    fun getAllPresets(): Flow<List<PlayerFilterPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PlayerFilterPresetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<PlayerFilterPresetEntity>)

    @Query("SELECT * FROM player_filter_presets")
    suspend fun getAllStatic(): List<PlayerFilterPresetEntity>

    @Delete
    suspend fun deletePreset(preset: PlayerFilterPresetEntity)

    @Query("DELETE FROM player_filter_presets WHERE preset_name = :name")
    suspend fun deleteByName(name: String)
}
