package com.fameafrica.afm.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object ColorUtils {
    /**
     * Determines if a color is "dark" based on its luminance.
     */
    fun isDark(color: Color): Boolean = color.luminance() < 0.5f

    /**
     * Returns either White or Black depending on which provides better contrast
     * against the given background color.
     */
    fun getContrastingColor(background: Color): Color {
        return if (isDark(background)) Color.White else Color.Black
    }

    /**
     * Adjusts a color to ensure it meets a minimum contrast ratio against a background.
     * If the contrast is too low, it returns a safe alternative (White or Black).
     */
    fun ensureContrast(color: Color, background: Color, threshold: Float = 0.3f): Color {
        val contrast = Math.abs(color.luminance() - background.luminance())
        return if (contrast < threshold) {
            getContrastingColor(background)
        } else {
            color
        }
    }
}
