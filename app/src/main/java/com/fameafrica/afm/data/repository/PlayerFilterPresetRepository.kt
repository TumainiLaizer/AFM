package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PlayerFilterPresetDao
import com.fameafrica.afm.data.database.entities.PlayerFilterPresetEntity
import com.fameafrica.afm.data.database.model.PlayerFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PlayerFilterPresetRepository @Inject constructor(
    private val presetDaoProvider: Provider<PlayerFilterPresetDao>
) {
    private val presetDao get() = presetDaoProvider.get()
    fun getAllPresets(): Flow<List<PlayerFilterPresetEntity>> = presetDao.getAllPresets()

    suspend fun savePreset(name: String, filter: PlayerFilter, isSystem: Boolean = false) {
        val json = Json.encodeToString(filter)
        presetDao.insertPreset(
            PlayerFilterPresetEntity(
                presetName = name,
                filterJson = json,
                isSystemPreset = isSystem
            )
        )
    }

    suspend fun deletePreset(preset: PlayerFilterPresetEntity) {
        presetDao.deletePreset(preset)
    }

    fun parseFilter(json: String): PlayerFilter {
        return try {
            Json.decodeFromString<PlayerFilter>(json)
        } catch (e: Exception) {
            PlayerFilter()
        }
    }
}
