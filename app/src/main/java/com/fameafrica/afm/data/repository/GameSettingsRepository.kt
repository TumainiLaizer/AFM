package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.GameSettingsDao
import com.fameafrica.afm.data.database.dao.SettingsHistoryDao
import com.fameafrica.afm.data.database.entities.GameSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class GameSettingsRepository @Inject constructor(
    private val gameSettingsDaoProvider: Provider<GameSettingsDao>,
    private val settingsHistoryDaoProvider: Provider<SettingsHistoryDao>
) {
    private val gameSettingsDao get() = gameSettingsDaoProvider.get()
    private val settingsHistoryDao get() = settingsHistoryDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getSettings(): Flow<GameSettingsEntity?> = gameSettingsDao.getSettings()

    suspend fun getSettingsSync(): GameSettingsEntity? = gameSettingsDao.getSettings().firstOrNull()

    suspend fun getSettingsById(id: Int): GameSettingsEntity? = gameSettingsDao.getById(id)

    suspend fun insertSettings(settings: GameSettingsEntity) = gameSettingsDao.insert(settings)

    suspend fun updateSettings(settings: GameSettingsEntity) = gameSettingsDao.update(settings)

    suspend fun deleteSettings(settings: GameSettingsEntity) = gameSettingsDao.delete(settings)

    suspend fun deleteAllSettings() = gameSettingsDao.deleteAll()

    suspend fun getSettingsCount(): Int = gameSettingsDao.getCount()

    // ============ GETTERS ============

    suspend fun getLanguage(): String = gameSettingsDao.getLanguage() ?: "en"

    suspend fun getCurrency(): String = gameSettingsDao.getCurrency() ?: "EUR"

    suspend fun getCountryCode(): String = gameSettingsDao.getCountryCode() ?: "TZ"

    suspend fun getDifficulty(): String = gameSettingsDao.getDifficulty() ?: "Beginner"

    suspend fun isAutoDetectRegion(): Boolean = gameSettingsDao.isAutoDetectRegion() ?: true

    suspend fun getMatchSpeed(): Int {
        val settings = getSettingsSync()
        return settings?.matchSpeed ?: 1
    }

    suspend fun getThemeColor(): String {
        val settings = getSettingsSync()
        return settings?.themeColor ?: "Theme.FAME2025.Splash"
    }

    suspend fun getFontSize(): Int {
        val settings = getSettingsSync()
        return settings?.fontSize ?: 1
    }

    suspend fun isAutosaveEnabled(): Boolean {
        val settings = getSettingsSync()
        return settings?.autosave ?: true
    }

    suspend fun getAutosaveFrequency(): Int {
        val settings = getSettingsSync()
        return settings?.autoSaveFrequency ?: 2
    }

    suspend fun isMusicEnabled(): Boolean {
        val settings = getSettingsSync()
        return settings?.music ?: true
    }

    suspend fun isSoundEnabled(): Boolean {
        val settings = getSettingsSync()
        return settings?.soundEnabled ?: true
    }

    suspend fun isAnimationsEnabled(): Boolean {
        val settings = getSettingsSync()
        return settings?.animationsEnabled ?: true
    }

    suspend fun areNotificationsEnabled(): Boolean {
        val settings = getSettingsSync()
        return settings?.notifications ?: true
    }

    // ============ SETTERS ============

    suspend fun updateLanguage(language: String): Boolean {
        val settings = getSettingsSync() ?: return false
        gameSettingsDao.updateLanguage(settings.id, language)
        return true
    }

    suspend fun updateCurrency(currency: String): Boolean {
        val settings = getSettingsSync() ?: return false
        gameSettingsDao.updateCurrency(settings.id, currency)
        return true
    }

    suspend fun updateCountryCode(countryCode: String): Boolean {
        val settings = getSettingsSync() ?: return false
        gameSettingsDao.updateCountryCode(settings.id, countryCode)
        return true
    }

    suspend fun updateDifficulty(difficulty: String): Boolean {
        val settings = getSettingsSync() ?: return false
        gameSettingsDao.updateDifficulty(settings.id, difficulty)
        return true
    }

    suspend fun updateMatchSpeed(matchSpeed: Int): Boolean {
        val settings = getSettingsSync() ?: return false
        gameSettingsDao.updateMatchSpeed(settings.id, matchSpeed)
        return true
    }

    suspend fun updateAutoDetectRegion(autoDetect: Boolean): Boolean {
        val settings = getSettingsSync() ?: return false
        gameSettingsDao.updateAutoDetectRegion(settings.id, autoDetect)
        return true
    }

    suspend fun updateExchangeRateTimestamp(timestamp: Long): Boolean {
        val settings = getSettingsSync() ?: return false
        gameSettingsDao.updateExchangeRateTimestamp(settings.id, timestamp)
        return true
    }

    // ============ BULK UPDATE WITH HISTORY ============

    suspend fun updateSettingsWithHistory(
        updates: Map<String, Any>,
        changedBy: String = "user"
    ): Boolean {
        val settings = getSettingsSync() ?: return false

        gameSettingsDao.updateWithHistory(
            settingsId = settings.id,
            updates = updates,
            changedAt = System.currentTimeMillis(),
            settingsHistoryDao = settingsHistoryDao
        )

        return true
    }

    // ============ RESET ============

    suspend fun resetToDefaults(defaultSettings: GameSettingsEntity): Boolean {
        val current = getSettingsSync() ?: return false

        val updates = mapOf(
            "language" to defaultSettings.language,
            "currency" to defaultSettings.currency,
            "countryCode" to defaultSettings.countryCode,
            "difficulty" to defaultSettings.difficulty,
            "matchSpeed" to defaultSettings.matchSpeed,
            "autoDetectRegion" to defaultSettings.autoDetectRegion
        )

        return updateSettingsWithHistory(updates)
    }

    // ============ DASHBOARD ============

    suspend fun getSettingsDashboard(): GameSettingsDashboard {
        val settings = getSettingsSync()

        return GameSettingsDashboard(
            currentSettings = settings,
            language = settings?.language ?: "en",
            currency = settings?.currency ?: "EUR",
            countryCode = settings?.countryCode ?: "TZ",
            difficulty = settings?.difficulty ?: "Beginner",
            matchSpeed = settings?.matchSpeed ?: 1,
            autoDetectRegion = settings?.autoDetectRegion ?: true,
            autosave = settings?.autosave ?: true,
            autoSaveFrequency = settings?.autoSaveFrequency ?: 2,
            music = settings?.music ?: true,
            soundEnabled = settings?.soundEnabled ?: true,
            animationsEnabled = settings?.animationsEnabled ?: true,
            notifications = settings?.notifications ?: true,
            fontSize = settings?.fontSize ?: 1,
            lastExchangeRateUpdate = settings?.lastExchangeRateUpdate
        )
    }
}

// ============ DATA CLASSES ============

data class GameSettingsDashboard(
    val currentSettings: GameSettingsEntity?,
    val language: String,
    val currency: String,
    val countryCode: String,
    val difficulty: String,
    val matchSpeed: Int,
    val autoDetectRegion: Boolean,
    val autosave: Boolean,
    val autoSaveFrequency: Int,
    val music: Boolean,
    val soundEnabled: Boolean,
    val animationsEnabled: Boolean,
    val notifications: Boolean,
    val fontSize: Int,
    val lastExchangeRateUpdate: Long?
)