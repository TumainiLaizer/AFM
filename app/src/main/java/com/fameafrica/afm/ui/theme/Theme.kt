package com.fameafrica.afm.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * AFM2026 Global Theme
 * Refactored to support Dynamic Light/Dark modes
 * with specific Africa Football Manager identity.
 */
@Composable
fun AFM2026Theme(
    themePreset: FootballThemePreset = FootballThemePreset.DEFAULT,
    clubTheme: ClubThemeConfig? = null,
    content: @Composable () -> Unit
) {
    val presetColors = when (themePreset) {
        FootballThemePreset.DEFAULT -> Triple(FameColors.PitchGreen, FameColors.ChampionsGold, FameColors.SharedNeutralBase)
        FootballThemePreset.CHAIRMAN_MODE -> Triple(FameColors.TrophyGold, FameColors.AmberBronze, FameColors.DeepNavyBlack)
        FootballThemePreset.MANAGER_MODE -> Triple(FameColors.GrowthGreen, FameColors.DeepBlue, FameColors.SharedNeutralBase)
        FootballThemePreset.NEWS_MODE -> Triple(FameColors.KenteRed, FameColors.BoldBlack, FameColors.SharedNeutralBase)
        else -> Triple(FameColors.PitchGreen, FameColors.ChampionsGold, FameColors.SharedNeutralBase)
    }

    val basePrimary = presetColors.first
    val baseSecondary = presetColors.second

    // Unified Dark Theme for AFRICAN FOOTBALL identity
    val primary = clubTheme?.primaryColor ?: basePrimary
    val secondary = clubTheme?.secondaryColor ?: baseSecondary

    val colors = darkColorScheme(
        primary = primary,
        onPrimary = FameColors.WarmIvory,
        secondary = secondary,
        onSecondary = FameColors.WarmIvory,
        tertiary = FameColors.AfroSunOrange,
        background = Color.Transparent, // Transparent to allow AfricanBackground patterns to show through
        onBackground = FameColors.WarmIvory,
        surface = FameColors.SurfaceDark,
        onSurface = FameColors.WarmIvory,
        surfaceVariant = FameColors.SurfaceMedium,
        onSurfaceVariant = FameColors.MutedParchment,
        error = FameColors.KenteRed,
        outline = Color.Gray
    )

    MaterialTheme(
        colorScheme = colors,
        typography = AFM2026Typography,
        shapes = FameShapes,
        content = {
            Background {
                content()
            }
        }
    )
}
