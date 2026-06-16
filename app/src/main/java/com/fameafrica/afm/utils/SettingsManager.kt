package com.fameafrica.afm.utils

import android.content.Context
import android.os.Build
import com.fameafrica.afm.data.database.dao.*
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.firstOrNull
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    private val gameSettingsDao: GameSettingsDao,
    private val regionalSettingsDao: RegionalSettingsDao,
    private val currencyExchangeRatesDao: CurrencyExchangeRatesDao,
    private val settingsHistoryDao: SettingsHistoryDao,
    private val userAnalyticsDao: UserAnalyticsDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val context: Context
) {

    // ============ INITIALIZATION ============

    /**
     * Initialize settings with auto-detection based on device locale
     */
    suspend fun initializeSettings() {
        val currentSettings = gameSettingsDao.getSettings().firstOrNull()

        if (currentSettings == null) {
            // First time setup - create default settings with auto-detection
            val detectedRegion = autoDetectRegion()

            val defaultSettings = GameSettingsEntity(
                language = detectedRegion?.languageCode ?: "en",
                countryCode = detectedRegion?.countryCode ?: "TZ",
                autoDetectRegion = true,
                currency = detectedRegion?.currencyCode ?: "EUR",
                exchangeRateSource = "local",
                lastExchangeRateUpdate = System.currentTimeMillis(),
                difficulty = "Beginner",
                matchSpeed = 1,
                autosave = true,
                autoSaveFrequency = 2,
                music = false,
                soundEnabled = true,
                animationsEnabled = false,
                themeColor = "Theme.FAME2025.Splash",
                fontSize = 1,
                notifications = true
            )
            gameSettingsDao.insert(defaultSettings)
        } else if (currentSettings.autoDetectRegion) {
            // Auto-detect on every startup if enabled
            val detected = autoDetectRegion()
            detected?.let {
                if (currentSettings.countryCode != it.countryCode ||
                    currentSettings.currency != it.currencyCode ||
                    currentSettings.language != it.languageCode) {

                    updateSettings(
                        currency = it.currencyCode,
                        countryCode = it.countryCode,
                        language = it.languageCode
                    )
                }
            }
        }
    }

    /**
     * Auto-detect region based on device locale
     */
    suspend fun autoDetectRegion(): RegionalSettingsEntity? {
        val locale = context.resources.configuration.locales[0]
        val countryCode = locale.country.uppercase()

        // Try to find exact match by country code
        var region = regionalSettingsDao.getByCountryCodeSync(countryCode)

        // If not found, try to find by language
        if (region == null) {
            val language = locale.language
            val regionsByLanguage = regionalSettingsDao.getByLanguageSync(language)
            if (regionsByLanguage.isNotEmpty()) {
                // Prefer the first one, or could use more sophisticated logic
                region = regionsByLanguage.firstOrNull()
            }
        }

        // Default to Tanzania if nothing found
        if (region == null) {
            region = regionalSettingsDao.getByCountryCodeSync("TZ")
        }

        return region
    }

    // ============ SETTINGS MANAGEMENT ============

    /**
     * Get current settings
     */
    suspend fun getCurrentSettings(): GameSettingsEntity? {
        return gameSettingsDao.getSettings().firstOrNull()
    }

    /**
     * Update settings and track history
     */
    suspend fun updateSettings(
        currency: String? = null,
        countryCode: String? = null,
        language: String? = null,
        difficulty: String? = null,
        matchSpeed: Int? = null,
        autoDetectRegion: Boolean? = null
    ): Boolean {
        val current = gameSettingsDao.getSettings().firstOrNull() ?: return false

        val updates = mutableMapOf<String, Any>()
        currency?.let { updates["currency"] = it }
        countryCode?.let { updates["countryCode"] = it }
        language?.let { updates["language"] = it }
        difficulty?.let { updates["difficulty"] = it }
        matchSpeed?.let { updates["matchSpeed"] = it }
        autoDetectRegion?.let { updates["autoDetectRegion"] = it }

        if (updates.isNotEmpty()) {
            gameSettingsDao.updateWithHistory(
                settingsId = current.id,
                updates = updates,
                changedAt = System.currentTimeMillis(),
                settingsHistoryDao = settingsHistoryDao
            )
            return true
        }
        return false
    }

    /**
     * Reset to default settings
     */
    suspend fun resetToDefaults() {
        val current = gameSettingsDao.getSettings().firstOrNull() ?: return
        val detected = autoDetectRegion()

        val defaults = mapOf(
            "language" to (detected?.languageCode ?: "en"),
            "countryCode" to (detected?.countryCode ?: "TZ"),
            "currency" to (detected?.currencyCode ?: "EUR"),
            "difficulty" to "Beginner",
            "matchSpeed" to 1,
            "autoDetectRegion" to true
        )

        gameSettingsDao.updateWithHistory(
            settingsId = current.id,
            updates = defaults,
            changedAt = System.currentTimeMillis(),
            settingsHistoryDao = settingsHistoryDao
        )
    }

    // ============ CURRENCY CONVERSION ============

    /**
     * Convert amount from EUR to user's selected currency
     */
    suspend fun convertToUserCurrency(amountInEuro: Double): Double {
        val settings = gameSettingsDao.getSettings().firstOrNull() ?: return amountInEuro

        if (settings.currency == "EUR") {
            return amountInEuro
        }

        val rate = currencyExchangeRatesDao.getRateSync("EUR", settings.currency)
        return amountInEuro * (rate?.exchangeRate ?: 1.0)
    }

    /**
     * Convert amount from user's currency to EUR
     */
    suspend fun convertFromUserCurrency(amountInUserCurrency: Double): Double {
        val settings = gameSettingsDao.getSettings().firstOrNull() ?: return amountInUserCurrency

        if (settings.currency == "EUR") {
            return amountInUserCurrency
        }

        val rate = currencyExchangeRatesDao.getRateSync("EUR", settings.currency)
        return amountInUserCurrency / (rate?.exchangeRate ?: 1.0)
    }

    /**
     * Convert between any two currencies
     */
    suspend fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        if (fromCurrency == toCurrency) return amount

        // Convert to EUR first if needed
        val amountInEuro = if (fromCurrency == "EUR") {
            amount
        } else {
            val rateToEuro = currencyExchangeRatesDao.getRateSync("EUR", fromCurrency)
            amount / (rateToEuro?.exchangeRate ?: 1.0)
        }

        // Convert from EUR to target
        return if (toCurrency == "EUR") {
            amountInEuro
        } else {
            val rateFromEuro = currencyExchangeRatesDao.getRateSync("EUR", toCurrency)
            amountInEuro * (rateFromEuro?.exchangeRate ?: 1.0)
        }
    }

    // ============ FORMATTING ============

    /**
     * Format amount in user's currency with proper formatting
     */
    suspend fun formatAmount(amountInEuro: Double): String {
        val settings = gameSettingsDao.getSettings().firstOrNull() ?: return "€${String.format("%.2f", amountInEuro)}"
        val converted = convertToUserCurrency(amountInEuro)
        val region = regionalSettingsDao.getByCountryCodeSync(settings.countryCode)

        return formatAmountWithCurrency(converted, settings.currency, region)
    }

    /**
     * Format amount with specific currency and regional settings
     */
    fun formatAmountWithCurrency(
        amount: Double,
        currencyCode: String,
        region: RegionalSettingsEntity?
    ): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            decimalSeparator = region?.decimalSeparator?.first() ?: '.'
            groupingSeparator = region?.thousandsSeparator?.first() ?: ','
        }

        val pattern = if (currencyCode == "JPY") "#,##0" else "#,##0.00"

        val formatter = DecimalFormat(pattern, symbols)
        val formattedAmount = formatter.format(amount)

        val currencySymbol = region?.currencySymbol ?: getCurrencySymbol(currencyCode)

        return when (currencyCode) {
            "EUR", "USD", "CAD", "AUD", "NZD", "GBP", "JPY", "CNY" -> "$currencySymbol$formattedAmount"
            else -> "$formattedAmount $currencyCode"
        }
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "EUR" -> "€"
            "USD", "CAD", "AUD", "NZD" -> "$"
            "GBP" -> "£"
            "JPY", "CNY" -> "¥"
            "CHF" -> "Fr"
            "TZS" -> "TSh"
            "KES" -> "KSh"
            "UGX" -> "USh"
            "NGN" -> "₦"
            "ZAR" -> "R"
            "GHS" -> "GH₵"
            "EGP" -> "E£"
            "MAD" -> "DH"
            else -> currencyCode
        }
    }

    /**
     * Format date according to user's regional settings
     */
    suspend fun formatDate(timestamp: Long): String {
        val settings = gameSettingsDao.getSettings().firstOrNull() ?: return Date(timestamp).toString()
        val region = regionalSettingsDao.getByCountryCodeSync(settings.countryCode)
        val pattern = region?.dateFormat ?: "dd/MM/yyyy"

        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Format time according to user's regional settings
     */
    suspend fun formatTime(timestamp: Long): String {
        val settings = gameSettingsDao.getSettings().firstOrNull() ?: return Date(timestamp).toString()
        val region = regionalSettingsDao.getByCountryCodeSync(settings.countryCode)
        val pattern = region?.timeFormat ?: "HH:mm"

        val timeFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Format datetime according to user's regional settings
     */
    suspend fun formatDateTime(timestamp: Long): String {
        return "${formatDate(timestamp)} ${formatTime(timestamp)}"
    }

    // ============ USER PREFERENCES ============

    /**
     * Get a user preference with automatic type conversion
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> getPreference(key: String, defaultValue: T): T {
        val pref = userPreferencesDao.getByKey(key) ?: return defaultValue
        return when (defaultValue) {
            is String -> (pref.preferenceValue as? T) ?: defaultValue
            is Boolean -> (pref.preferenceValue.toBooleanStrictOrNull() as? T) ?: defaultValue
            is Int -> (pref.preferenceValue.toIntOrNull() as? T) ?: defaultValue
            is Float -> (pref.preferenceValue.toFloatOrNull() as? T) ?: defaultValue
            is Long -> (pref.preferenceValue.toLongOrNull() as? T) ?: defaultValue
            else -> defaultValue
        }
    }

    /**
     * Set a user preference with automatic type detection
     */
    suspend fun setPreference(key: String, value: Any) {
        when (value) {
            is String -> userPreferencesDao.setString(key, value)
            is Boolean -> userPreferencesDao.setBoolean(key, value)
            is Int -> userPreferencesDao.setInt(key, value)
            is Float -> userPreferencesDao.setFloat(key, value)
            is Long -> userPreferencesDao.setLong(key, value)
        }
    }

    // ============ SETTINGS HISTORY ============

    /**
     * Get settings change history
     */
    suspend fun getSettingsHistory(limit: Int = 50): List<SettingsHistoryEntity> {
        val settings = gameSettingsDao.getSettings().firstOrNull() ?: return emptyList()
        return settingsHistoryDao.getHistoryForSettings(settings.id).firstOrNull()?.take(limit) ?: emptyList()
    }

    /**
     * Get most recent changes
     */
    suspend fun getRecentChanges(days: Int = 7): List<SettingsHistoryEntity> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return settingsHistoryDao.getHistorySince(cutoff).firstOrNull() ?: emptyList()
    }

    // ============ ANALYTICS ============

    /**
     * Track user event for analytics
     */
    suspend fun trackEvent(
        eventType: String,
        eventData: String? = null,
        userId: String? = null,
        sessionId: String? = null
    ) {
        val analytics = UserAnalyticsEntity(
            eventType = eventType,
            eventData = eventData,
            userId = userId,
            sessionId = sessionId,
            createdAt = System.currentTimeMillis(),
            deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}",
            appVersion = getAppVersion(),
            countryCode = gameSettingsDao.getCountryCode()
        )
        userAnalyticsDao.insert(analytics)
    }

    private fun getAppVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    // ============ DASHBOARD ============

    /**
     * Get complete settings dashboard
     */
    suspend fun getSettingsDashboard(): SettingsDashboard {
        val settings = gameSettingsDao.getSettings().firstOrNull()
        val region = settings?.countryCode?.let { regionalSettingsDao.getByCountryCodeSync(it) }
        val currencyRate = settings?.currency?.let {
            currencyExchangeRatesDao.getRateSync("EUR", it)
        }
        val history = settings?.id?.let { settingsHistoryDao.getHistoryForSettings(it).firstOrNull() } ?: emptyList()
        val preferences = userPreferencesDao.getAll().firstOrNull() ?: emptyList()

        return SettingsDashboard(
            currentSettings = settings,
            detectedRegion = region,
            currentExchangeRate = currencyRate,
            recentChanges = history.take(10),
            userPreferences = preferences.associate { it.preferenceKey to it.preferenceValue },
            lastSyncTime = currencyRate?.lastUpdated
        )
    }
}

// ============ DATA CLASSES ============

data class SettingsDashboard(
    val currentSettings: GameSettingsEntity?,
    val detectedRegion: RegionalSettingsEntity?,
    val currentExchangeRate: CurrencyExchangeRatesEntity?,
    val recentChanges: List<SettingsHistoryEntity>,
    val userPreferences: Map<String, String>,
    val lastSyncTime: Long?
)