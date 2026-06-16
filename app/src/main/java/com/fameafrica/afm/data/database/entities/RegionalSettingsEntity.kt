package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "regional_settings",
    indices = [
        Index(value = ["country_code"], unique = true),
        Index(value = ["currency_code"]),
        Index(value = ["language_code"]),
        Index(value = ["is_african_country"])
    ]
)
data class RegionalSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "country_code")
    @ColumnInfo(name = "country_code")
    val countryCode: String,  // ISO 3166-1 alpha-2 (TZ, KE, NG, etc.)

    @Json(name = "country_name")
    @ColumnInfo(name = "country_name")
    val countryName: String,

    @Json(name = "language_code")
    @ColumnInfo(name = "language_code")
    val languageCode: String,  // ISO 639-1 (en, sw, fr, pt, etc.)

    @Json(name = "language_name")
    @ColumnInfo(name = "language_name")
    val languageName: String,

    @Json(name = "currency_code")
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,  // TZS, KES, NGN, ZAR, EUR, USD, etc.

    @Json(name = "currency_symbol")
    @ColumnInfo(name = "currency_symbol")
    val currencySymbol: String,

    @Json(name = "currency_name")
    @ColumnInfo(name = "currency_name")
    val currencyName: String,

    @Json(name = "number_format")
    @ColumnInfo(name = "number_format")
    val numberFormat: String,  // #,##0.00, # ###,## etc.

    @Json(name = "date_format")
    @ColumnInfo(name = "date_format")
    val dateFormat: String,  // dd/MM/yyyy, MM/dd/yyyy, yyyy-MM-dd

    @Json(name = "time_format")
    @ColumnInfo(name = "time_format")
    val timeFormat: String,  // HH:mm, hh:mm a

    @ColumnInfo(name = "timezone")
    val timezone: String,  // Africa/Dar_es_Salaam, Africa/Lagos, etc.

    @Json(name = "first_day_of_week")
    @ColumnInfo(name = "first_day_of_week")
    val firstDayOfWeek: Int = 1,  // 1 = Monday, 7 = Sunday

    @Json(name = "decimal_separator")
    @ColumnInfo(name = "decimal_separator")
    val decimalSeparator: String = ".",

    @Json(name = "thousands_separator")
    @ColumnInfo(name = "thousands_separator")
    val thousandsSeparator: String = ",",

    @Json(name = "is_african_country")
    @ColumnInfo(name = "is_african_country", defaultValue = "0")
    val isAfricanCountry: Boolean = false,

    @ColumnInfo(name = "continent")
    val continent: String,  // Africa, Europe, Asia, Americas, Oceania

    @ColumnInfo(name = "region")
    val region: String? = null,  // East Africa, West Africa, Southern Africa, etc.

    @Json(name = "flag_emoji")
    @ColumnInfo(name = "flag_emoji")
    val flagEmoji: String? = null,

    @Json(name = "calling_code")
    @ColumnInfo(name = "calling_code")
    val callingCode: String? = null,

    @Json(name = "created_at")
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val displayName: String
        get() = "$countryName ($currencyCode)"

    val isEastAfrican: Boolean
        get() = region == "East Africa"

    val isWestAfrican: Boolean
        get() = region == "West Africa"

    val isSouthernAfrican: Boolean
        get() = region == "Southern Africa"

    val isNorthAfrican: Boolean
        get() = region == "North Africa"

    val isCentralAfrican: Boolean
        get() = region == "Central Africa"
}

// ============ ENUMS ============

enum class Continent(val value: String) {
    AFRICA("Africa"),
    EUROPE("Europe"),
    ASIA("Asia"),
    NORTH_AMERICA("North America"),
    SOUTH_AMERICA("South America"),
    OCEANIA("Oceania")
}

enum class AfricanRegion(val value: String) {
    EAST_AFRICA("East Africa"),
    WEST_AFRICA("West Africa"),
    SOUTHERN_AFRICA("Southern Africa"),
    NORTH_AFRICA("North Africa"),
    CENTRAL_AFRICA("Central Africa")
}