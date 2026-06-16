package com.fameafrica.afm.utils.extensions

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a long value as currency (e.g., 1,000,000 -> "1.0M").
 * Uses a simplified format for mobile information density.
 */
fun Long.formatCurrency(): String {
    return when {
        this >= 1_000_000_000 -> String.format("%.1fB", this / 1_000_000_000.0)
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.1fK", this / 1_000.0)
        else -> this.toString()
    }
}

/**
 * Formats a double value as currency.
 */
fun Double.formatCurrency(): String {
    return this.toLong().formatCurrency()
}
