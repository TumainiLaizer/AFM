package com.fameafrica.afm.utils.formatters

import com.fameafrica.afm.data.repository.CurrencyExchangeRatesRepository
import com.fameafrica.afm.data.repository.GameSettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyFormatter @Inject constructor(
    private val settingsRepository: GameSettingsRepository,
    private val exchangeRatesRepository: CurrencyExchangeRatesRepository
) {

    private val billionFormat = DecimalFormat("#,##0.00'B'")
    private val millionFormat = DecimalFormat("#,##0.1'M'")
    private val thousandFormat = DecimalFormat("#,##0'K'")
    private val exactFormat = DecimalFormat("#,##0")

    /**
     * Data class to hold currency context for bulk formatting.
     */
    data class CurrencyContext(
        val code: String,
        val symbol: String,
        val rate: Double
    )

    /**
     * Fetches the current currency context from settings and exchange rates.
     */
    suspend fun getCurrentContext(): CurrencyContext {
        val settings = settingsRepository.getSettings().firstOrNull()
        val code = settings?.currency ?: "EUR"
        val rate = if (code == "EUR") 1.0 
                   else exchangeRatesRepository.getEuroRateForCurrency(code) ?: 1.0
        return CurrencyContext(code, getSymbolForCurrency(code), rate)
    }

    /**
     * Formats a value from Euro to the user's preferred currency.
     * Note: Uses runBlocking for simple non-suspend access in legacy code or simple UI.
     * Prefer using the version that takes a context for performance in lists.
     */
    fun formatEuroAmount(euroAmount: Double): String {
        val context = runBlocking { getCurrentContext() }
        return format(euroAmount, context)
    }

    /**
     * Fast formatting using a pre-fetched context.
     */
    fun format(euroAmount: Double, context: CurrencyContext): String {
        val convertedAmount = euroAmount * context.rate
        return "${context.symbol}${formatValue(convertedAmount.toLong())}"
    }

    /**
     * Formats long values into football manager style strings (B, M, K).
     */
    fun formatValue(value: Long): String {
        val absValue = Math.abs(value)
        val sign = if (value < 0) "-" else ""
        val formatted = when {
            absValue >= 1_000_000_000 -> {
                val billions = absValue.toDouble() / 1_000_000_000.0
                billionFormat.format(billions)
            }
            absValue >= 1_000_000 -> {
                val millions = absValue.toDouble() / 1_000_000.0
                millionFormat.format(millions)
            }
            absValue >= 1_000 -> {
                val thousands = absValue.toDouble() / 1_000.0
                thousandFormat.format(thousands)
            }
            else -> exactFormat.format(absValue)
        }
        return sign + formatted
    }

    /**
     * Specific formatter for weekly wages.
     */
    fun formatWage(weeklyWageInEuro: Double, context: CurrencyContext): String {
        return "${format(weeklyWageInEuro, context)} p/w"
    }

    fun getSymbolForCurrency(currency: String): String {
        return when (currency) {
            "EUR" -> "€"
            "USD" -> "$"
            "GBP" -> "£"
            "TZS" -> "TSh "
            "KES" -> "KSh "
            "NGN" -> "₦"
            "ZAR" -> "R"
            "GHS" -> "GH₵"
            "EGP" -> "E£"
            "MAD" -> "DH "
            else -> "$currency "
        }
    }
}
