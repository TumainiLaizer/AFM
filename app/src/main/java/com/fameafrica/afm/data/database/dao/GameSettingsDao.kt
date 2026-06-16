package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.GameSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSettingsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM game_settings LIMIT 1")
    fun getSettings(): Flow<GameSettingsEntity?>

    @Query("SELECT * FROM game_settings WHERE id = :id")
    suspend fun getById(id: Int): GameSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: GameSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<GameSettingsEntity>)

    @Update
    suspend fun update(settings: GameSettingsEntity)

    @Delete
    suspend fun delete(settings: GameSettingsEntity)

    @Query("DELETE FROM game_settings")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM game_settings")
    suspend fun getCount(): Int

    @Query("SELECT * FROM game_settings")
    suspend fun getAllStatic(): List<GameSettingsEntity>

    // ============ SPECIFIC SETTINGS GETTERS ============

    @Query("SELECT language FROM game_settings LIMIT 1")
    suspend fun getLanguage(): String?

    @Query("SELECT currency FROM game_settings LIMIT 1")
    suspend fun getCurrency(): String?

    @Query("SELECT country_code FROM game_settings LIMIT 1")
    suspend fun getCountryCode(): String?

    @Query("SELECT difficulty FROM game_settings LIMIT 1")
    suspend fun getDifficulty(): String?

    @Query("SELECT auto_detect_region FROM game_settings LIMIT 1")
    suspend fun isAutoDetectRegion(): Boolean?

    // ============ SPECIFIC SETTINGS UPDATERS ============

    @Query("UPDATE game_settings SET language = :language, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateLanguage(id: Int, language: String)

    @Query("UPDATE game_settings SET currency = :currency, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateCurrency(id: Int, currency: String)

    @Query("UPDATE game_settings SET country_code = :countryCode, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateCountryCode(id: Int, countryCode: String)

    @Query("UPDATE game_settings SET difficulty = :difficulty, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateDifficulty(id: Int, difficulty: String)

    @Query("UPDATE game_settings SET match_speed = :matchSpeed, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateMatchSpeed(id: Int, matchSpeed: Int)

    @Query("UPDATE game_settings SET auto_detect_region = :autoDetect, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateAutoDetectRegion(id: Int, autoDetect: Boolean)

    @Query("UPDATE game_settings SET last_exchange_rate_update = :timestamp, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateExchangeRateTimestamp(id: Int, timestamp: Long)

    // ============ BULK UPDATE WITH HISTORY ============

    @Transaction
    suspend fun updateWithHistory(
        settingsId: Int,
        updates: Map<String, Any>,
        changedAt: Long,
        settingsHistoryDao: SettingsHistoryDao
    ) {
        val current = getById(settingsId) ?: return

        updates.forEach { (field, newValue) ->
            val oldValue = when (field) {
                "language" -> current.language
                "currency" -> current.currency
                "countryCode" -> current.countryCode
                "difficulty" -> current.difficulty
                "matchSpeed" -> current.matchSpeed.toString()
                "autoDetectRegion" -> current.autoDetectRegion.toString()
                else -> null
            }

            if (oldValue != newValue.toString()) {
                settingsHistoryDao.insert(
                    com.fameafrica.afm.data.database.entities.SettingsHistoryEntity(
                        settingsId = settingsId,
                        changedField = field,
                        oldValue = oldValue,
                        newValue = newValue.toString(),
                        changedAt = changedAt
                    )
                )
            }
        }

        // Apply updates based on field types
        updates.forEach { (field, value) ->
            when (field) {
                "language" -> updateLanguage(settingsId, value as String)
                "currency" -> updateCurrency(settingsId, value as String)
                "countryCode" -> updateCountryCode(settingsId, value as String)
                "difficulty" -> updateDifficulty(settingsId, value as String)
                "matchSpeed" -> updateMatchSpeed(settingsId, value as Int)
                "autoDetectRegion" -> updateAutoDetectRegion(settingsId, value as Boolean)
            }
        }
    }
}