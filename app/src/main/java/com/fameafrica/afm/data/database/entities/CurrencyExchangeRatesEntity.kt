package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "currency_exchange_rates",
    indices = [
        Index(value = ["base_currency", "target_currency"], unique = true),
        Index(value = ["target_currency"]),
        Index(value = ["is_active"])
    ]
)
data class CurrencyExchangeRatesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "base_currency")
    @ColumnInfo(name = "base_currency", defaultValue = "USD")
    val baseCurrency: String = "USD",

    @Json(name = "target_currency")
    @ColumnInfo(name = "target_currency")
    val targetCurrency: String,

    @Json(name = "exchange_rate")
    @ColumnInfo(name = "exchange_rate")
    val exchangeRate: Double,  // 1 USD = X target currency

    @Json(name = "inverse_rate")
    @ColumnInfo(name = "inverse_rate")
    val inverseRate: Double,  // 1 target currency = X USD

    @Json(name = "last_updated")
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long,

    @ColumnInfo(name = "source", defaultValue = "local")
    val source: String = "local",  // local, ECB, custom

    @Json(name = "is_active")
    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @Json(name = "created_at")
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val formattedRate: String
        get() = "1 $baseCurrency = $exchangeRate $targetCurrency"

    val formattedInverse: String
        get() = "1 $targetCurrency = $inverseRate $baseCurrency"
}

// ============ ENUMS ============

enum class Currency(val code: String, val symbol: String, val currencyName: String) {
    // African Currencies
    TZS("TZS", "TSh", "Tanzanian Shilling"),
    KES("KES", "KSh", "Kenyan Shilling"),
    UGX("UGX", "USh", "Ugandan Shilling"),
    RWF("RWF", "FRw", "Rwandan Franc"),
    BIF("BIF", "FBu", "Burundian Franc"),
    CDF("CDF", "FC", "Congolese Franc"),
    XAF("XAF", "FCFA", "Central African CFA Franc"),
    XOF("XOF", "FCFA", "West African CFA Franc"),
    ZMW("ZMW", "ZK", "Zambian Kwacha"),
    ZWL("ZWL", "Z$", "Zimbabwean Dollar"),
    BWP("BWP", "P", "Botswana Pula"),
    NAD("NAD", "N$", "Namibian Dollar"),
    LSL("LSL", "L", "Lesotho Loti"),
    SZL("SZL", "E", "Eswatini Lilangeni"),
    ZAR("ZAR", "R", "South African Rand"),
    MZN("MZN", "MT", "Mozambican Metical"),
    AOA("AOA", "Kz", "Angolan Kwanza"),
    MWK("MWK", "MK", "Malawian Kwacha"),
    GHS("GHS", "GH₵", "Ghanaian Cedi"),
    NGN("NGN", "₦", "Nigerian Naira"),
    GNF("GNF", "FG", "Guinean Franc"),
    GMD("GMD", "D", "Gambian Dalasi"),
    SLE("SLE", "Le", "Sierra Leonean Leone"),
    LRD("LRD", "L$", "Liberian Dollar"),
    MRU("MRU", "UM", "Mauritanian Ouguiya"),
    MUR("MUR", "Rs", "Mauritian Rupee"),
    SCR("SCR", "SR", "Seychellois Rupee"),
    KMF("KMF", "CF", "Comorian Franc"),
    MAD("MAD", "DH", "Moroccan Dirham"),
    DZD("DZD", "DA", "Algerian Dinar"),
    TND("TND", "DT", "Tunisian Dinar"),
    LYD("LYD", "LD", "Libyan Dinar"),
    EGP("EGP", "E£", "Egyptian Pound"),
    SDG("SDG", "SDG", "Sudanese Pound"),
    SSP("SSP", "SSP", "South Sudanese Pound"),
    ETB("ETB", "Br", "Ethiopian Birr"),
    SOS("SOS", "Sh", "Somali Shilling"),
    DJF("DJF", "Fdj", "Djiboutian Franc"),
    ERN("ERN", "Nfk", "Eritrean Nakfa"),
    CVE("CVE", "$", "Cape Verdean Escudo"),
    STN("STN", "Db", "São Tomé and Príncipe Dobra"),

    // Major World Currencies
    EUR("EUR", "€", "Euro"),
    USD("USD", "$", "US Dollar"),
    GBP("GBP", "£", "British Pound"),
    JPY("JPY", "¥", "Japanese Yen"),
    CNY("CNY", "¥", "Chinese Yuan"),
    CHF("CHF", "Fr", "Swiss Franc"),
    CAD("CAD", "C$", "Canadian Dollar"),
    AUD("AUD", "A$", "Australian Dollar"),
    INR("INR", "₹", "Indian Rupee"),
    BRL("BRL", "R$", "Brazilian Real"),
    RUB("RUB", "₽", "Russian Ruble"),
    TRY("TRY", "₺", "Turkish Lira"),
    AED("AED", "د.إ", "UAE Dirham"),
    SAR("SAR", "﷼", "Saudi Riyal")
}