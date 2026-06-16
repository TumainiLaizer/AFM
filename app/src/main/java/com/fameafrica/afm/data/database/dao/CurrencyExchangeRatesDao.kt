package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.squareup.moshi.Json
import com.fameafrica.afm.data.database.entities.CurrencyExchangeRatesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyExchangeRatesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM currency_exchange_rates ORDER BY target_currency")
    fun getAll(): Flow<List<CurrencyExchangeRatesEntity>>

    @Query("SELECT * FROM currency_exchange_rates WHERE id = :id")
    suspend fun getById(id: Int): CurrencyExchangeRatesEntity?

    @Query("SELECT * FROM currency_exchange_rates WHERE base_currency = :base AND target_currency = :target")
    fun getRate(base: String, target: String): Flow<CurrencyExchangeRatesEntity?>

    @Query("SELECT * FROM currency_exchange_rates WHERE base_currency = :base AND target_currency = :target")
    suspend fun getRateSync(base: String, target: String): CurrencyExchangeRatesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rate: CurrencyExchangeRatesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<CurrencyExchangeRatesEntity>)

    @Update
    suspend fun update(rate: CurrencyExchangeRatesEntity)

    @Delete
    suspend fun delete(rate: CurrencyExchangeRatesEntity)

    @Query("DELETE FROM currency_exchange_rates")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM currency_exchange_rates")
    suspend fun getCount(): Int

    // ============ ACTIVE RATES QUERIES ============

    @Query("SELECT * FROM currency_exchange_rates WHERE is_active = 1 ORDER BY target_currency")
    fun getActiveRates(): Flow<List<CurrencyExchangeRatesEntity>>

    @Query("SELECT * FROM currency_exchange_rates WHERE target_currency = :target AND is_active = 1")
    fun getRatesForCurrency(target: String): Flow<List<CurrencyExchangeRatesEntity>>

    @Query("SELECT * FROM currency_exchange_rates WHERE base_currency = :base AND is_active = 1")
    fun getRatesFromBase(base: String): Flow<List<CurrencyExchangeRatesEntity>>

    // ============ CONVERSION QUERIES ============

    @Query("SELECT exchange_rate FROM currency_exchange_rates WHERE base_currency = :base AND target_currency = :target AND is_active = 1")
    suspend fun getExchangeRate(base: String, target: String): Double?

    @Query("SELECT inverse_rate FROM currency_exchange_rates WHERE base_currency = :base AND target_currency = :target AND is_active = 1")
    suspend fun getInverseRate(base: String, target: String): Double?

    @Query("""
        SELECT target_currency, exchange_rate 
        FROM currency_exchange_rates 
        WHERE base_currency = :base AND is_active = 1
    """)
    suspend fun getAllRatesForBase(base: String): List<CurrencyRate>

    // ============ BULK CONVERSION ============

    @Query("""
        SELECT * FROM currency_exchange_rates 
        WHERE base_currency = 'EUR' AND is_active = 1
    """)
    fun getAllEuroRates(): Flow<List<CurrencyExchangeRatesEntity>>

    // ============ UPDATE MANAGEMENT ============

    @Query("SELECT MAX(last_updated) FROM currency_exchange_rates WHERE is_active = 1")
    suspend fun getLastUpdateTimestamp(): Long?

    @Query("UPDATE currency_exchange_rates SET is_active = 0 WHERE last_updated < :cutoffTimestamp")
    suspend fun deactivateOldRates(cutoffTimestamp: Long)

    @Query("UPDATE currency_exchange_rates SET is_active = 1 WHERE target_currency IN (:currencies)")
    suspend fun activateRatesForCurrencies(currencies: List<String>)

    // ============ STATISTICS ============

    @Query("SELECT COUNT(*) FROM currency_exchange_rates WHERE is_active = 1")
    suspend fun getActiveRatesCount(): Int

    @Query("SELECT AVG(exchange_rate) FROM currency_exchange_rates WHERE base_currency = :base AND is_active = 1")
    suspend fun getAverageRate(base: String): Double?
    @Query("SELECT * FROM currency_exchange_rates ORDER BY target_currency ASC")
    suspend fun getAllStatic(): List<CurrencyExchangeRatesEntity>
}

// ============ DATA CLASSES ============

data class CurrencyRate(
    @param:Json(name = "target_currency")
    @field:ColumnInfo(name = "target_currency") val targetCurrency: String,
    
    @param:Json(name = "exchange_rate")
    @field:ColumnInfo(name = "exchange_rate") val exchangeRate: Double
)

data class ConversionResult(
    val fromCurrency: String,
    val toCurrency: String,
    val originalAmount: Double,
    val convertedAmount: Double,
    val rate: Double,
    val timestamp: Long
)
