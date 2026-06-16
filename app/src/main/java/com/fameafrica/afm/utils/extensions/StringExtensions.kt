package com.fameafrica.afm.utils.extensions

import java.util.Locale

/**
 * Converts a raw string (e.g., "TRANSFER_FOCUSED") into Title Case ("Transfer Focused").
 * Replaces underscores with spaces.
 */
fun String.toTitleCase(): String {
    return this.lowercase(Locale.ROOT)
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
}

/**
 * Converts a raw string (e.g., "ATTACKING_FLUID") into Sentence case ("Attacking fluid").
 * Replaces underscores with spaces.
 */
fun String.toSentenceCase(): String {
    val clean = this.lowercase(Locale.ROOT).replace("_", " ")
    return clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}
