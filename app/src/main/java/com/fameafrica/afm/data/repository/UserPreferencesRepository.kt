package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.UserPreferencesDao
import com.fameafrica.afm.data.database.entities.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDaoProvider: Provider<UserPreferencesDao>
) {

    private val userPreferencesDao: UserPreferencesDao?
        get() = try {
            userPreferencesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllPreferences(): Flow<List<UserPreferencesEntity>> = userPreferencesDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getPreferenceById(id: Int): UserPreferencesEntity? = userPreferencesDao?.getById(id)

    suspend fun getPreferenceByKey(key: String): UserPreferencesEntity? = userPreferencesDao?.getByKey(key)

    suspend fun insertPreference(preference: UserPreferencesEntity) {
        userPreferencesDao?.insert(preference)
    }

    suspend fun insertAllPreferences(preferences: List<UserPreferencesEntity>) {
        userPreferencesDao?.insertAll(preferences)
    }

    suspend fun updatePreference(preference: UserPreferencesEntity) {
        userPreferencesDao?.update(preference)
    }

    suspend fun deletePreference(preference: UserPreferencesEntity) {
        userPreferencesDao?.delete(preference)
    }

    suspend fun deletePreferenceByKey(key: String) {
        userPreferencesDao?.deleteByKey(key)
    }

    suspend fun deleteAllPreferences() {
        userPreferencesDao?.deleteAll()
    }

    suspend fun getPreferencesCount(): Int = userPreferencesDao?.getCount() ?: 0

    // ============ TYPED GETTERS ============

    suspend fun getString(key: String, defaultValue: String = ""): String {
        return userPreferencesDao?.getString(key) ?: defaultValue
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return userPreferencesDao?.getBoolean(key) ?: defaultValue
    }

    suspend fun getInt(key: String, defaultValue: Int = 0): Int {
        return userPreferencesDao?.getInt(key) ?: defaultValue
    }

    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return userPreferencesDao?.getFloat(key) ?: defaultValue
    }

    suspend fun getLong(key: String, defaultValue: Long = 0L): Long {
        return userPreferencesDao?.getLong(key) ?: defaultValue
    }

    // ============ TYPED SETTERS ============

    suspend fun setString(key: String, value: String) {
        userPreferencesDao?.setString(key, value)
    }

    suspend fun setBoolean(key: String, value: Boolean) {
        userPreferencesDao?.setBoolean(key, value)
    }

    suspend fun setInt(key: String, value: Int) {
        userPreferencesDao?.setInt(key, value)
    }

    suspend fun setFloat(key: String, value: Float) {
        userPreferencesDao?.setFloat(key, value)
    }

    suspend fun setLong(key: String, value: Long) {
        userPreferencesDao?.setLong(key, value)
    }

    // ============ BULK OPERATIONS ============

    suspend fun setPreferences(preferences: Map<String, Any>) {
        userPreferencesDao?.setPreferences(preferences)
    }

    suspend fun getAllPreferencesAsMap(): Map<String, String> = userPreferencesDao?.getAllAsMap() ?: emptyMap()

    // ============ TYPE-BASED QUERIES ============

    fun getPreferencesByType(type: String): Flow<List<UserPreferencesEntity>> =
        userPreferencesDao?.getByType(type) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getKeysByType(type: String): List<String> = userPreferencesDao?.getKeysByType(type) ?: emptyList()

    // ============ SPECIFIC PREFERENCE HELPERS ============

    suspend fun getTheme(): String = getString("theme", "light")
    suspend fun setTheme(theme: String) = setString("theme", theme)

    suspend fun areNotificationsEnabled(): Boolean = getBoolean("notifications_enabled", true)
    suspend fun setNotificationsEnabled(enabled: Boolean) = setBoolean("notifications_enabled", enabled)

    suspend fun areMatchHighlightsEnabled(): Boolean = getBoolean("match_highlights", true)
    suspend fun setMatchHighlightsEnabled(enabled: Boolean) = setBoolean("match_highlights", enabled)

    suspend fun isAutoSaveEnabled(): Boolean = getBoolean("auto_save", true)
    suspend fun setAutoSaveEnabled(enabled: Boolean) = setBoolean("auto_save", enabled)

    suspend fun getSoundVolume(): Int = getInt("sound_volume", 70)
    suspend fun setSoundVolume(volume: Int) = setInt("sound_volume", volume.coerceIn(0, 100))

    suspend fun getMusicVolume(): Int = getInt("music_volume", 50)
    suspend fun setMusicVolume(volume: Int) = setInt("music_volume", volume.coerceIn(0, 100))

    suspend fun shouldShowPlayerRatings(): Boolean = getBoolean("show_player_ratings", true)
    suspend fun setShowPlayerRatings(show: Boolean) = setBoolean("show_player_ratings", show)

    suspend fun shouldShowTacticalAdvice(): Boolean = getBoolean("show_tactical_advice", true)
    suspend fun setShowTacticalAdvice(show: Boolean) = setBoolean("show_tactical_advice", show)

    suspend fun shouldConfirmImportantActions(): Boolean = getBoolean("confirm_important_actions", true)
    suspend fun setConfirmImportantActions(confirm: Boolean) = setBoolean("confirm_important_actions", confirm)

    suspend fun shouldAutoContinueAfterMatch(): Boolean = getBoolean("auto_continue_after_match", false)
    suspend fun setAutoContinueAfterMatch(autoContinue: Boolean) = setBoolean("auto_continue_after_match", autoContinue)

    suspend fun getPreferredFormation(): String = getString("preferred_formation", "4-4-2")
    suspend fun setPreferredFormation(formation: String) = setString("preferred_formation", formation)

    suspend fun isAutoSubstitutionsEnabled(): Boolean = getBoolean("auto_substitutions", false)
    suspend fun setAutoSubstitutionsEnabled(enabled: Boolean) = setBoolean("auto_substitutions", enabled)

    suspend fun areInjuryWarningsEnabled(): Boolean = getBoolean("injury_warnings", true)
    suspend fun setInjuryWarningsEnabled(enabled: Boolean) = setBoolean("injury_warnings", enabled)

    suspend fun areTransferNotificationsEnabled(): Boolean = getBoolean("transfer_notifications", true)
    suspend fun setTransferNotificationsEnabled(enabled: Boolean) = setBoolean("transfer_notifications", enabled)

    suspend fun areScoutReportsEnabled(): Boolean = getBoolean("scout_reports", true)
    suspend fun setScoutReportsEnabled(enabled: Boolean) = setBoolean("scout_reports", enabled)

    // ============ RESET ============

    suspend fun resetToDefaults() {
        deleteAllPreferences()

        // Set default preferences
        setPreferences(
            mapOf(
                "theme" to "light",
                "notifications_enabled" to true,
                "match_highlights" to true,
                "auto_save" to true,
                "sound_volume" to 30,
                "music_volume" to 50,
                "show_player_ratings" to true,
                "show_tactical_advice" to true,
                "confirm_important_actions" to true,
                "auto_continue_after_match" to false,
                "preferred_formation" to "4-4-2",
                "auto_substitutions" to false,
                "injury_warnings" to true,
                "transfer_notifications" to true,
                "scout_reports" to true
            )
        )
    }

    // ============ DASHBOARD ============

    suspend fun getPreferencesDashboard(): PreferencesDashboard {
        val allPreferences = userPreferencesDao?.getAll()?.firstOrNull() ?: emptyList()

        val stringPrefs = allPreferences.filter { it.preferenceType == "string" }
        val booleanPrefs = allPreferences.filter { it.preferenceType == "boolean" }
        val integerPrefs = allPreferences.filter { it.preferenceType == "integer" }
        val floatPrefs = allPreferences.filter { it.preferenceType == "float" }

        val recentPrefs = allPreferences.sortedByDescending { it.updatedAt }.take(10)

        return PreferencesDashboard(
            totalPreferences = allPreferences.size,
            stringPreferences = stringPrefs.size,
            booleanPreferences = booleanPrefs.size,
            integerPreferences = integerPrefs.size,
            floatPreferences = floatPrefs.size,
            recentPreferences = recentPrefs,
            allPreferences = allPreferences
        )
    }
}

// ============ DATA CLASSES ============

data class PreferencesDashboard(
    val totalPreferences: Int,
    val stringPreferences: Int,
    val booleanPreferences: Int,
    val integerPreferences: Int,
    val floatPreferences: Int,
    val recentPreferences: List<UserPreferencesEntity>,
    val allPreferences: List<UserPreferencesEntity>
)