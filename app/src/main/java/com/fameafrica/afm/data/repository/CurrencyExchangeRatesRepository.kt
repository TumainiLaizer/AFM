package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.CurrencyExchangeRatesDao
import com.fameafrica.afm.data.database.entities.CurrencyExchangeRatesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CurrencyExchangeRatesRepository @Inject constructor(
    private val currencyExchangeRatesDaoProvider: Provider<CurrencyExchangeRatesDao>
) {

    private val currencyExchangeRatesDao: CurrencyExchangeRatesDao?
        get() = try {
            currencyExchangeRatesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllRates(): Flow<List<CurrencyExchangeRatesEntity>> = currencyExchangeRatesDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getRateById(id: Int): CurrencyExchangeRatesEntity? = currencyExchangeRatesDao?.getById(id)

    fun getRate(base: String, target: String): Flow<CurrencyExchangeRatesEntity?> =
        currencyExchangeRatesDao?.getRate(base, target) ?: kotlinx.coroutines.flow.flowOf(null)

    suspend fun getRateSync(base: String, target: String): CurrencyExchangeRatesEntity? =
        currencyExchangeRatesDao?.getRateSync(base, target)

    suspend fun insertRate(rate: CurrencyExchangeRatesEntity) {
        currencyExchangeRatesDao?.insert(rate)
    }

    suspend fun insertAllRates(rates: List<CurrencyExchangeRatesEntity>) {
        currencyExchangeRatesDao?.insertAll(rates)
    }

    suspend fun updateRate(rate: CurrencyExchangeRatesEntity) {
        currencyExchangeRatesDao?.update(rate)
    }

    suspend fun deleteRate(rate: CurrencyExchangeRatesEntity) {
        currencyExchangeRatesDao?.delete(rate)
    }

    suspend fun deleteAllRates() {
        currencyExchangeRatesDao?.deleteAll()
    }

    suspend fun getRatesCount(): Int = currencyExchangeRatesDao?.getCount() ?: 0

    // ============ ACTIVE RATES ============

    fun getActiveRates(): Flow<List<CurrencyExchangeRatesEntity>> = currencyExchangeRatesDao?.getActiveRates() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getRatesForCurrency(target: String): Flow<List<CurrencyExchangeRatesEntity>> =
        currencyExchangeRatesDao?.getRatesForCurrency(target) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getRatesFromBase(base: String): Flow<List<CurrencyExchangeRatesEntity>> =
        currencyExchangeRatesDao?.getRatesFromBase(base) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ CONVERSION ============

    suspend fun getExchangeRate(base: String, target: String): Double? =
        currencyExchangeRatesDao?.getExchangeRate(base, target)

    suspend fun getInverseRate(base: String, target: String): Double? =
        currencyExchangeRatesDao?.getInverseRate(base, target)

    suspend fun getAllRatesForBase(base: String): Map<String, Double> =
        currencyExchangeRatesDao?.getAllRatesForBase(base)?.associate { it.targetCurrency to it.exchangeRate } ?: emptyMap()

    suspend fun convertAmount(amount: Double, from: String, to: String): Double? {
        if (from == to) return amount

        val rate = if (from == "EUR") {
            getExchangeRate("EUR", to)
        } else if (to == "EUR") {
            getInverseRate("EUR", from)
        } else {
            // Cross-currency conversion via EUR
            val rateFromEur = getExchangeRate("EUR", from)
            val rateToEur = getExchangeRate("EUR", to)
            if (rateFromEur != null && rateToEur != null) {
                return (amount / rateFromEur) * rateToEur
            }
            null
        }

        return rate?.let { amount * it }
    }

    // ============ EURO-SPECIFIC ============

    fun getAllEuroRates(): Flow<List<CurrencyExchangeRatesEntity>> = currencyExchangeRatesDao?.getAllEuroRates() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getEuroRateForCurrency(currency: String): Double? =
        getExchangeRate("EUR", currency)

    suspend fun convertFromEuro(amountInEuro: Double, toCurrency: String): Double? {
        val rate = getExchangeRate("EUR", toCurrency)
        return rate?.let { amountInEuro * it }
    }

    suspend fun convertToEuro(amount: Double, fromCurrency: String): Double? {
        val rate = getExchangeRate("EUR", fromCurrency)
        return rate?.let { amount / it }
    }

    // ============ UPDATE MANAGEMENT ============

    suspend fun getLastUpdateTimestamp(): Long? = currencyExchangeRatesDao?.getLastUpdateTimestamp()

    suspend fun deactivateOldRates(cutoffTimestamp: Long) {
        currencyExchangeRatesDao?.deactivateOldRates(cutoffTimestamp)
    }

    suspend fun activateRatesForCurrencies(currencies: List<String>) {
        currencyExchangeRatesDao?.activateRatesForCurrencies(currencies)
    }

    suspend fun refreshRates(newRates: List<CurrencyExchangeRatesEntity>) {
        // Deactivate all current rates first
        deactivateOldRates(System.currentTimeMillis())

        // Insert new rates
        insertAllRates(newRates)
    }

    // ============ STATISTICS ============

    suspend fun getActiveRatesCount(): Int = currencyExchangeRatesDao?.getActiveRatesCount() ?: 0

    suspend fun getAverageRate(base: String): Double? = currencyExchangeRatesDao?.getAverageRate(base)

    suspend fun getStrongestCurrency(): Pair<String, Double>? {
        val rates = getAllRatesForBase("EUR")
        return rates.maxByOrNull { it.value }?.let { it.key to it.value }
    }

    suspend fun getWeakestCurrency(): Pair<String, Double>? {
        val rates = getAllRatesForBase("EUR")
        return rates.minByOrNull { it.value }?.let { it.key to it.value }
    }

    // ============ DASHBOARD ============

    suspend fun getCurrencyDashboard(): CurrencyDashboard {
        val activeRates = currencyExchangeRatesDao?.getActiveRates()?.firstOrNull() ?: emptyList()
        val euroRates = activeRates.filter { it.baseCurrency == "EUR" }

        val top5Strongest = euroRates.sortedByDescending { it.exchangeRate }.take(5)
        val top5Weakest = euroRates.sortedBy { it.exchangeRate }.take(5)

        val averageRate = if (euroRates.isNotEmpty()) euroRates.map { it.exchangeRate }.average() else 0.0
        val totalValue = euroRates.sumOf { it.exchangeRate }

        val lastUpdate = euroRates.maxOfOrNull { it.lastUpdated }

        return CurrencyDashboard(
            totalActiveRates = activeRates.size,
            euroBasedRates = euroRates.size,
            averageExchangeRate = averageRate,
            totalExchangeValue = totalValue,
            strongestCurrencies = top5Strongest,
            weakestCurrencies = top5Weakest,
            lastUpdate = lastUpdate,
            currenciesByRegion = groupCurrenciesByRegion(euroRates)
        )
    }

    private fun groupCurrenciesByRegion(rates: List<CurrencyExchangeRatesEntity>): Map<String, List<CurrencyExchangeRatesEntity>> {
        val africanCurrencies = listOf("TZS", "KES", "UGX", "RWF", "BIF", "CDF", "XAF", "XOF", "ZMW", "ZWL", "BWP", "NAD", "LSL", "SZL", "ZAR", "MZN", "AOA", "MWK", "GHS", "NGN", "GNF", "GMD", "SLE", "LRD", "MRU", "MUR", "SCR", "KMF", "MAD", "DZD", "TND", "LYD", "EGP", "SDG", "SSP", "ETB", "SOS", "DJF", "ERN", "CVE", "STN")

        val europeanCurrencies = listOf("EUR", "GBP", "CHF", "SEK", "NOK", "DKK", "PLN", "CZK", "HUF", "RON", "BGN", "HRK", "RSD", "ISK")

        val asianCurrencies = listOf("JPY", "CNY", "INR", "KRW", "SGD", "MYR", "THB", "IDR", "PHP", "VND", "PKR", "BDT", "LKR", "MMK", "KHR", "LAK", "MNT")

        val americasCurrencies = listOf("USD", "CAD", "MXN", "BRL", "ARS", "CLP", "COP", "PEN", "UYU", "PYG", "BOB", "VES")

        val oceaniaCurrencies = listOf("AUD", "NZD", "FJD", "PGK", "SBD", "TOP", "WST", "VUV")

        return mapOf(
            "Africa" to rates.filter { it.targetCurrency in africanCurrencies },
            "Europe" to rates.filter { it.targetCurrency in europeanCurrencies },
            "Asia" to rates.filter { it.targetCurrency in asianCurrencies },
            "Americas" to rates.filter { it.targetCurrency in americasCurrencies },
            "Oceania" to rates.filter { it.targetCurrency in oceaniaCurrencies },
            "Other" to rates.filter { it.targetCurrency !in africanCurrencies + europeanCurrencies + asianCurrencies + americasCurrencies + oceaniaCurrencies }
        )
    }
}

// ============ DATA CLASSES ============

data class CurrencyDashboard(
    val totalActiveRates: Int,
    val euroBasedRates: Int,
    val averageExchangeRate: Double,
    val totalExchangeValue: Double,
    val strongestCurrencies: List<CurrencyExchangeRatesEntity>,
    val weakestCurrencies: List<CurrencyExchangeRatesEntity>,
    val lastUpdate: Long?,
    val currenciesByRegion: Map<String, List<CurrencyExchangeRatesEntity>>
)
