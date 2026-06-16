package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.RegionalSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionalSettingsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM regional_settings ORDER BY country_name")
    fun getAll(): Flow<List<RegionalSettingsEntity>>

    @Query("SELECT * FROM regional_settings WHERE id = :id")
    suspend fun getById(id: Int): RegionalSettingsEntity?

    @Query("SELECT * FROM regional_settings WHERE country_code = :countryCode")
    fun getByCountryCode(countryCode: String): Flow<RegionalSettingsEntity?>

    @Query("SELECT * FROM regional_settings WHERE country_code = :countryCode")
    suspend fun getByCountryCodeSync(countryCode: String): RegionalSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: RegionalSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<RegionalSettingsEntity>)

    @Update
    suspend fun update(settings: RegionalSettingsEntity)

    @Delete
    suspend fun delete(settings: RegionalSettingsEntity)

    @Query("DELETE FROM regional_settings")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM regional_settings")
    suspend fun getCount(): Int

    // ============ LANGUAGE-BASED QUERIES ============

    @Query("SELECT * FROM regional_settings WHERE language_code = :languageCode ORDER BY country_name")
    fun getByLanguage(languageCode: String): Flow<List<RegionalSettingsEntity>>

    @Query("SELECT * FROM regional_settings WHERE language_code = :languageCode")
    suspend fun getByLanguageSync(languageCode: String): List<RegionalSettingsEntity>

    @Query("SELECT DISTINCT language_code, language_name FROM regional_settings ORDER BY language_name")
    fun getSupportedLanguages(): Flow<List<LanguageInfo>>

    // ============ CURRENCY-BASED QUERIES ============

    @Query("SELECT * FROM regional_settings WHERE currency_code = :currencyCode ORDER BY country_name")
    fun getByCurrency(currencyCode: String): Flow<List<RegionalSettingsEntity>>

    @Query("SELECT * FROM regional_settings WHERE currency_code = :currencyCode")
    suspend fun getByCurrencySync(currencyCode: String): List<RegionalSettingsEntity>

    @Query("SELECT DISTINCT currency_code, currency_name, currency_symbol FROM regional_settings ORDER BY currency_code")
    fun getSupportedCurrencies(): Flow<List<CurrencyInfo>>

    @Query("SELECT currency_symbol FROM regional_settings WHERE currency_code = :currencyCode LIMIT 1")
    suspend fun getCurrencySymbol(currencyCode: String): String?

    // ============ AFRICAN COUNTRY QUERIES ============

    @Query("SELECT * FROM regional_settings WHERE is_african_country = 1 ORDER BY country_name")
    fun getAfricanCountries(): Flow<List<RegionalSettingsEntity>>

    @Query("SELECT * FROM regional_settings WHERE is_african_country = 1 AND region = :region ORDER BY country_name")
    fun getAfricanCountriesByRegion(region: String): Flow<List<RegionalSettingsEntity>>

    @Query("SELECT DISTINCT region FROM regional_settings WHERE is_african_country = 1 AND region IS NOT NULL ORDER BY region")
    fun getAfricanRegions(): Flow<List<String>>

    // ============ CONTINENT-BASED QUERIES ============

    @Query("SELECT * FROM regional_settings WHERE continent = :continent ORDER BY country_name")
    fun getByContinent(continent: String): Flow<List<RegionalSettingsEntity>>

    @Query("SELECT DISTINCT continent FROM regional_settings ORDER BY continent")
    fun getContinents(): Flow<List<String>>

    // ============ TIMEZONE QUERIES ============

    @Query("SELECT * FROM regional_settings WHERE timezone = :timezone")
    suspend fun getByTimezone(timezone: String): List<RegionalSettingsEntity>

    @Query("SELECT DISTINCT timezone FROM regional_settings ORDER BY timezone")
    fun getAllTimezones(): Flow<List<String>>

    // ============ SEARCH QUERIES ============

    @Query("SELECT * FROM regional_settings WHERE country_name LIKE '%' || :searchQuery || '%' OR country_code LIKE '%' || :searchQuery || '%' ORDER BY country_name")
    fun searchCountries(searchQuery: String): Flow<List<RegionalSettingsEntity>>

    // ============ FORMATTING HELPERS ============

    @Query("SELECT number_format, decimal_separator, thousands_separator FROM regional_settings WHERE country_code = :countryCode")
    suspend fun getNumberFormatting(countryCode: String): NumberFormatInfo?

    @Query("SELECT date_format, time_format FROM regional_settings WHERE country_code = :countryCode")
    suspend fun getDateTimeFormat(countryCode: String): DateTimeFormatInfo?
    @Query("SELECT * FROM regional_settings ORDER BY country_name ASC")
    suspend fun getAllStatic(): List<RegionalSettingsEntity>
}

// ============ DATA CLASSES ============

data class LanguageInfo(
    @ColumnInfo(name = "language_code")
    val languageCode: String,

    @ColumnInfo(name = "language_name")
    val languageName: String
)

data class CurrencyInfo(
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "currency_name")
    val currencyName: String,

    @ColumnInfo(name = "currency_symbol")
    val currencySymbol: String
)

data class NumberFormatInfo(
    @ColumnInfo(name = "number_format")
    val numberFormat: String,

    @ColumnInfo(name = "decimal_separator")
    val decimalSeparator: String,

    @ColumnInfo(name = "thousands_separator")
    val thousandsSeparator: String
)

data class DateTimeFormatInfo(
    @ColumnInfo(name = "date_format")
    val dateFormat: String,

    @ColumnInfo(name = "time_format")
    val timeFormat: String
)