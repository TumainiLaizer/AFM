package com.fameafrica.afm.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class ClubThemeConfig(
    val clubId: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val isDarkTheme: Boolean = true
) {
    /**
     * Returns a color that contrasts well with the provided [backgroundColor].
     * Prefers [primaryColor] if it has good contrast, otherwise falls back to standard theme colors.
     */
    fun getContentColor(backgroundColor: Color): Color {
        val isBackgroundLight = isColorLight(backgroundColor)
        val isPrimaryLight = isColorLight(primaryColor)

        return if (isBackgroundLight) {
            // If background is light (e.g. White), and primary is dark (e.g. Red), use primary
            if (!isPrimaryLight) {
                primaryColor
            } else {
                FameColors.StadiumBlack
            }
        } else {
            // If background is dark, and primary is light, use primary
            if (isPrimaryLight) {
                primaryColor
            } else {
                FameColors.WarmIvory
            }
        }
    }

    companion object {
        fun isColorLight(color: Color): Boolean {
            val argb = color.toArgb()
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
            return luminance > 0.5
        }
    }
}
