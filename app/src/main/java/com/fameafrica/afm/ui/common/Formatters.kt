package com.fameafrica.afm.ui.common

import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import java.text.NumberFormat
import java.util.Currency

fun formatCurrency(
    amount: Long,
    currencyCode: String = "EUR",
    context: CurrencyFormatter.CurrencyContext? = null
): String {
    if (context != null) {
        val convertedAmount = amount * context.rate
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(context.code)
        format.maximumFractionDigits = 0
        return format.format(convertedAmount)
    }
    
    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance(currencyCode)
    format.maximumFractionDigits = 0
    return format.format(amount)
}

fun formatCompactCurrency(
    amount: Long,
    currencyCode: String = "EUR",
    context: CurrencyFormatter.CurrencyContext? = null
): String {
    val finalCode = context?.code ?: currencyCode
    val finalRate = context?.rate ?: 1.0
    val convertedAmount = amount * finalRate

    val symbol = try {
        Currency.getInstance(finalCode).symbol
    } catch (_: Exception) {
        "€"
    }
    
    val absAmount = Math.abs(convertedAmount)
    val sign = if (convertedAmount < 0) "-" else ""
    
    val formatted = when {
        absAmount >= 1_000_000_000 -> String.format(java.util.Locale.US, "%.1fB", absAmount / 1_000_000_000.0)
        absAmount >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", absAmount / 1_000_000.0)
        absAmount >= 1_000 -> String.format(java.util.Locale.US, "%.0fK", absAmount / 1_000.0)
        else -> absAmount.toLong().toString()
    }
    
    return "$sign$symbol$formatted"
}

/**
 * Converts a raw string (potentially with underscores and uppercase) into a human-readable format.
 * Example: "TRANSFER_FOCUSED" -> "Transfer Focused"
 */
fun String.toHumanReadable(): String {
    return this.split("_")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}
