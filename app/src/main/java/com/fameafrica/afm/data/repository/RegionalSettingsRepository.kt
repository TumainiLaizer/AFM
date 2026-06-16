package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.CurrencyInfo
import com.fameafrica.afm.data.database.dao.DateTimeFormatInfo
import com.fameafrica.afm.data.database.dao.LanguageInfo
import com.fameafrica.afm.data.database.dao.NumberFormatInfo
import com.fameafrica.afm.data.database.dao.RegionalSettingsDao
import com.fameafrica.afm.data.database.entities.RegionalSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class RegionalSettingsRepository @Inject constructor(
    private val regionalSettingsDaoProvider: Provider<RegionalSettingsDao>
) {
    private val regionalSettingsDao get() = regionalSettingsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllSettings(): Flow<List<RegionalSettingsEntity>> = regionalSettingsDao.getAll()

    suspend fun getSettingById(id: Int): RegionalSettingsEntity? = regionalSettingsDao.getById(id)

    suspend fun getSettingByCountryCode(countryCode: String): RegionalSettingsEntity? =
        regionalSettingsDao.getByCountryCodeSync(countryCode)

    fun getSettingByCountryCodeFlow(countryCode: String): Flow<RegionalSettingsEntity?> =
        regionalSettingsDao.getByCountryCode(countryCode)

    suspend fun insertSetting(setting: RegionalSettingsEntity) = regionalSettingsDao.insert(setting)

    suspend fun insertAllSettings(settings: List<RegionalSettingsEntity>) = regionalSettingsDao.insertAll(settings)

    suspend fun updateSetting(setting: RegionalSettingsEntity) = regionalSettingsDao.update(setting)

    suspend fun deleteSetting(setting: RegionalSettingsEntity) = regionalSettingsDao.delete(setting)

    suspend fun deleteAllSettings() = regionalSettingsDao.deleteAll()

    suspend fun getSettingsCount(): Int = regionalSettingsDao.getCount()

    // ============ LANGUAGE-BASED ============

    fun getSettingsByLanguage(languageCode: String): Flow<List<RegionalSettingsEntity>> =
        regionalSettingsDao.getByLanguage(languageCode)

    suspend fun getSettingsByLanguageSync(languageCode: String): List<RegionalSettingsEntity> =
        regionalSettingsDao.getByLanguageSync(languageCode)

    fun getSupportedLanguages(): Flow<List<LanguageInfo>> = regionalSettingsDao.getSupportedLanguages()

    // ============ CURRENCY-BASED ============

    fun getSettingsByCurrency(currencyCode: String): Flow<List<RegionalSettingsEntity>> =
        regionalSettingsDao.getByCurrency(currencyCode)

    suspend fun getSettingsByCurrencySync(currencyCode: String): List<RegionalSettingsEntity> =
        regionalSettingsDao.getByCurrencySync(currencyCode)

    fun getSupportedCurrencies(): Flow<List<CurrencyInfo>> = regionalSettingsDao.getSupportedCurrencies()

    suspend fun getCurrencySymbol(currencyCode: String): String? =
        regionalSettingsDao.getCurrencySymbol(currencyCode)

    // ============ AFRICAN COUNTRIES ============

    fun getAfricanCountries(): Flow<List<RegionalSettingsEntity>> = regionalSettingsDao.getAfricanCountries()

    fun getAfricanCountriesByRegion(region: String): Flow<List<RegionalSettingsEntity>> =
        regionalSettingsDao.getAfricanCountriesByRegion(region)

    fun getAfricanRegions(): Flow<List<String>> = regionalSettingsDao.getAfricanRegions()

    suspend fun getAfricanCountriesCount(): Int =
        regionalSettingsDao.getAfricanCountries().firstOrNull()?.size ?: 0

    // ============ CONTINENT-BASED ============

    fun getSettingsByContinent(continent: String): Flow<List<RegionalSettingsEntity>> =
        regionalSettingsDao.getByContinent(continent)

    fun getContinents(): Flow<List<String>> = regionalSettingsDao.getContinents()

    // ============ TIMEZONE ============

    suspend fun getSettingsByTimezone(timezone: String): List<RegionalSettingsEntity> =
        regionalSettingsDao.getByTimezone(timezone)

    fun getAllTimezones(): Flow<List<String>> = regionalSettingsDao.getAllTimezones()

    // ============ SEARCH ============

    fun searchCountries(searchQuery: String): Flow<List<RegionalSettingsEntity>> =
        regionalSettingsDao.searchCountries(searchQuery)

    // ============ FORMATTING HELPERS ============

    suspend fun getNumberFormatting(countryCode: String): NumberFormatInfo? =
        regionalSettingsDao.getNumberFormatting(countryCode)

    suspend fun getDateTimeFormat(countryCode: String): DateTimeFormatInfo? =
        regionalSettingsDao.getDateTimeFormat(countryCode)

    // ============ UTILITY METHODS ============

    suspend fun isAfricanCountry(countryCode: String): Boolean {
        val setting = getSettingByCountryCode(countryCode)
        return setting?.isAfricanCountry ?: false
    }

    suspend fun getCountriesByCurrency(currencyCode: String): List<RegionalSettingsEntity> =
        getSettingsByCurrencySync(currencyCode)

    suspend fun getDefaultFormatting(): Pair<NumberFormatInfo?, DateTimeFormatInfo?> {
        // Default to Tanzania if no country specified
        val tzSettings = getSettingByCountryCode("TZ")
        return Pair(
            tzSettings?.let {
                NumberFormatInfo(
                    numberFormat = it.numberFormat,
                    decimalSeparator = it.decimalSeparator,
                    thousandsSeparator = it.thousandsSeparator
                )
            },
            tzSettings?.let {
                DateTimeFormatInfo(
                    dateFormat = it.dateFormat,
                    timeFormat = it.timeFormat
                )
            }
        )
    }

    // ============ DASHBOARD ============

    suspend fun getRegionalSettingsDashboard(): RegionalSettingsDashboard {
        val allSettings = regionalSettingsDao.getAll().firstOrNull() ?: emptyList()
        val africanCountries = allSettings.filter { it.isAfricanCountry }
        val europeanCountries = allSettings.filter { it.continent == "Europe" }
        val asianCountries = allSettings.filter { it.continent == "Asia" }
        val americasCountries = allSettings.filter { it.continent == "North America" || it.continent == "South America" }
        val oceaniaCountries = allSettings.filter { it.continent == "Oceania" }

        val currencies = allSettings.groupBy { it.currencyCode }.map { (code, list) ->
            CurrencySummary(
                currencyCode = code,
                currencyName = list.firstOrNull()?.currencyName ?: code,
                currencySymbol = list.firstOrNull()?.currencySymbol ?: code,
                countryCount = list.size
            )
        }.sortedByDescending { it.countryCount }

        val languages = allSettings.groupBy { it.languageCode }.map { (code, list) ->
            LanguageSummary(
                languageCode = code,
                languageName = list.firstOrNull()?.languageName ?: code,
                countryCount = list.size
            )
        }.sortedByDescending { it.countryCount }

        return RegionalSettingsDashboard(
            totalCountries = allSettings.size,
            africanCountries = africanCountries.size,
            europeanCountries = europeanCountries.size,
            asianCountries = asianCountries.size,
            americasCountries = americasCountries.size,
            oceaniaCountries = oceaniaCountries.size,
            currenciesSupported = currencies.size,
            languagesSupported = languages.size,
            topCurrencies = currencies.take(5),
            topLanguages = languages.take(5),
            recentCountries = allSettings.takeLast(10)
        )
    }
}

// ============ DATA CLASSES ============

data class RegionalSettingsDashboard(
    val totalCountries: Int,
    val africanCountries: Int,
    val europeanCountries: Int,
    val asianCountries: Int,
    val americasCountries: Int,
    val oceaniaCountries: Int,
    val currenciesSupported: Int,
    val languagesSupported: Int,
    val topCurrencies: List<CurrencySummary>,
    val topLanguages: List<LanguageSummary>,
    val recentCountries: List<RegionalSettingsEntity>
)

data class CurrencySummary(
    val currencyCode: String,
    val currencyName: String,
    val currencySymbol: String,
    val countryCount: Int
)

data class LanguageSummary(
    val languageCode: String,
    val languageName: String,
    val countryCount: Int
)