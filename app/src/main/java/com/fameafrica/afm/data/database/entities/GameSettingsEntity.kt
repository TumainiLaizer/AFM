package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json
import com.fameafrica.afm.ui.theme.FootballThemePreset

@Entity(
    tableName = "game_settings",
    indices = [
        Index(value = ["country_code"]),
        Index(value = ["currency"]),
        Index(value = ["language"])
    ]
)
data class GameSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    // ============ LANGUAGE & REGION ============
    @ColumnInfo(name = "language", defaultValue = "en")
    val language: String = "en",

    @Json(name = "country_code")
    @ColumnInfo(name = "country_code", defaultValue = "TZ")
    val countryCode: String = "TZ",

    @Json(name = "auto_detect_region")
    @ColumnInfo(name = "auto_detect_region", defaultValue = "1")
    val autoDetectRegion: Boolean = true,

    // ============ CURRENCY ============
    @ColumnInfo(name = "currency", defaultValue = "USD")
    val currency: String = "USD",

    @Json(name = "exchange_rate_source")
    @ColumnInfo(name = "exchange_rate_source", defaultValue = "local")
    val exchangeRateSource: String = "local",

    @Json(name = "last_exchange_rate_update")
    @ColumnInfo(name = "last_exchange_rate_update", defaultValue = "0")
    val lastExchangeRateUpdate: Long = 0,

    // ============ GAMEPLAY ============
    @ColumnInfo(name = "difficulty", defaultValue = "Beginner")
    val difficulty: String = "Beginner",  // Beginner, Intermediate, Expert, Legend

    @Json(name = "match_speed")
    @ColumnInfo(name = "match_speed", defaultValue = "1")
    val matchSpeed: Int = 1,  // 0 = Slow, 1 = Normal, 2 = Fast, 3 = Very Fast

    @ColumnInfo(name = "autosave", defaultValue = "1")
    val autosave: Boolean = true,

    @Json(name = "auto_save_frequency")
    @ColumnInfo(name = "auto_save_frequency", defaultValue = "2")
    val autoSaveFrequency: Int = 2,  // 0 = Never, 1 = Monthly, 2 = Weekly, 3 = Daily

    // ============ AUDIO/VISUAL ============
    @ColumnInfo(name = "music", defaultValue = "1")
    val music: Boolean = true,

    @Json(name = "sound_enabled")
    @ColumnInfo(name = "sound_enabled", defaultValue = "1")
    val soundEnabled: Boolean = true,

    @Json(name = "animations_enabled")
    @ColumnInfo(name = "animations_enabled", defaultValue = "1")
    val animationsEnabled: Boolean = true,

    @Json(name = "theme_preset")
    @ColumnInfo(name = "theme_preset", defaultValue = "DEFAULT")
    val themePreset: FootballThemePreset = FootballThemePreset.DEFAULT,

    @Json(name = "theme_color")
    @ColumnInfo(name = "theme_color", defaultValue = "Theme.FAME2025.Splash")
    val themeColor: String = "Theme.FAME2025.Splash",

    @Json(name = "font_size")
    @ColumnInfo(name = "font_size", defaultValue = "1")
    val fontSize: Int = 1,  // 0 = Small, 1 = Medium, 2 = Large, 3 = Extra Large

    // ============ NOTIFICATIONS ============
    @ColumnInfo(name = "notifications", defaultValue = "1")
    val notifications: Boolean = true,

    // ============ TIMESTAMPS ============
    @Json(name = "created_at")
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val matchSpeedText: String
        get() = when (matchSpeed) {
            0 -> "Slow"
            1 -> "Normal"
            2 -> "Fast"
            3 -> "Very Fast"
            else -> "Normal"
        }

    val difficultyText: String
        get() = difficulty

    val fontSizeText: String
        get() = when (fontSize) {
            0 -> "Small"
            1 -> "Medium"
            2 -> "Large"
            3 -> "Extra Large"
            else -> "Medium"
        }
}

// ============ ENUMS ============

enum class DifficultyLevel(val value: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    EXPERT("Expert"),
    LEGEND("Legend")
}

enum class AutoSaveFrequency(val value: Int, val description: String) {
    NEVER(0, "Never"),
    MONTHLY(1, "Monthly"),
    WEEKLY(2, "Weekly"),
    DAILY(3, "Daily")
}
