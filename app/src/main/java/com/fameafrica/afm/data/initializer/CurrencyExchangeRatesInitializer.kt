package com.fameafrica.afm.data.initializer

import com.fameafrica.afm.data.database.entities.CurrencyExchangeRatesEntity

object CurrencyExchangeRatesInitializer {

    /**
     * Returns a list of CurrencyExchangeRatesEntity with USD as base currency.
     * Rates are realistic as of 2026 (mid‑market).
     */
    fun getAllRateEntities(): List<CurrencyExchangeRatesEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // ========== AFRICAN CURRENCIES ==========
            createRate("USD", "TZS", 2500.0, now),
            createRate("USD", "KES", 130.0, now),
            createRate("USD", "UGX", 3800.0, now),
            createRate("USD", "RWF", 1300.0, now),
            createRate("USD", "BIF", 2800.0, now),
            createRate("USD", "CDF", 2800.0, now),
            createRate("USD", "XAF", 600.0, now),   // Central African CFA
            createRate("USD", "XOF", 600.0, now),   // West African CFA
            createRate("USD", "ZMW", 25.0, now),
            createRate("USD", "ZWL", 30000.0, now),
            createRate("USD", "BWP", 13.5, now),
            createRate("USD", "NAD", 19.0, now),    // pegged to ZAR
            createRate("USD", "LSL", 19.0, now),    // pegged to ZAR
            createRate("USD", "SZL", 19.0, now),    // pegged to ZAR
            createRate("USD", "ZAR", 19.0, now),
            createRate("USD", "MZN", 64.0, now),
            createRate("USD", "AOA", 850.0, now),
            createRate("USD", "MWK", 1700.0, now),
            createRate("USD", "GHS", 12.0, now),
            createRate("USD", "NGN", 1500.0, now),
            createRate("USD", "GNF", 8500.0, now),
            createRate("USD", "GMD", 70.0, now),
            createRate("USD", "SLE", 22.0, now),
            createRate("USD", "LRD", 190.0, now),
            createRate("USD", "MRU", 38.0, now),
            createRate("USD", "MUR", 46.0, now),
            createRate("USD", "SCR", 14.0, now),
            createRate("USD", "KMF", 450.0, now),
            createRate("USD", "MAD", 10.0, now),
            createRate("USD", "DZD", 135.0, now),
            createRate("USD", "TND", 3.1, now),
            createRate("USD", "LYD", 4.8, now),
            createRate("USD", "EGP", 48.0, now),
            createRate("USD", "SDG", 600.0, now),
            createRate("USD", "SSP", 1300.0, now),
            createRate("USD", "ETB", 57.0, now),
            createRate("USD", "SOS", 570.0, now),
            createRate("USD", "DJF", 178.0, now),   // fixed to USD
            createRate("USD", "ERN", 15.0, now),
            createRate("USD", "CVE", 105.0, now),
            createRate("USD", "STN", 23.0, now),

            // ========== MAJOR WORLD CURRENCIES ==========
            createRate("USD", "EUR", 0.92, now),
            createRate("USD", "GBP", 0.79, now),
            createRate("USD", "JPY", 150.0, now),
            createRate("USD", "CNY", 7.25, now),
            createRate("USD", "CHF", 0.88, now),
            createRate("USD", "CAD", 1.38, now),
            createRate("USD", "AUD", 1.53, now),
            createRate("USD", "INR", 83.5, now),
            createRate("USD", "BRL", 5.15, now),
            createRate("USD", "RUB", 92.0, now),
            createRate("USD", "TRY", 32.5, now),
            createRate("USD", "AED", 3.67, now),
            createRate("USD", "SAR", 3.75, now)
        )
    }

    private fun createRate(base: String, target: String, rate: Double, timestamp: Long): CurrencyExchangeRatesEntity {
        return CurrencyExchangeRatesEntity(
            baseCurrency = base,
            targetCurrency = target,
            exchangeRate = rate,
            inverseRate = 1.0 / rate,
            lastUpdated = timestamp,
            source = "local",
            isActive = true
        )
    }
}