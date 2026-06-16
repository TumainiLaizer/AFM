package com.fameafrica.afm.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Predefined theme presets for the football game.
 */
enum class FootballThemePreset(val displayName: String) {
    DEFAULT("FAME Africa (Default)"),
    CLASSIC_PITCH("Classic Pitch"),
    GOLDEN_ERA("Golden Era"),
    STREET_SOCCER("Street Soccer"),
    MIDNIGHT_STADIUM("Midnight Stadium"),
    RETRO_MANIA("Retro Mania"),
    MANAGER_MODE("Manager Mode"),
    CHAIRMAN_MODE("Chairman Mode"),
    NEWS_MODE("News Mode")
}

object FootballThemeColors {

    // 1. Classic Pitch (Fresh greens and whites)
    val ClassicPitchPrimary = Color(0xFF2E7D32)
    val ClassicPitchSecondary = Color(0xFFFFFFFF)
    val ClassicPitchBackground = Color(0xFF1B5E20)

    // 2. Golden Era (Trophy gold and deep navy)
    val GoldenEraPrimary = Color(0xFFD4AF37)
    val GoldenEraSecondary = Color(0xFF001F3F)
    val GoldenEraBackground = Color(0xFF001226)

    // 3. Street Soccer (Concrete grays and neon accents)
    val StreetSoccerPrimary = Color(0xFFE0E0E0)
    val StreetSoccerSecondary = Color(0xFF00E5FF)
    val StreetSoccerBackground = Color(0xFF212121)

    // 4. Midnight Stadium (Deep blacks and floodlight blues)
    val MidnightStadiumPrimary = Color(0xFF1A237E)
    val MidnightStadiumSecondary = Color(0xFF00BCD4)
    val MidnightStadiumBackground = Color(0xFF0A0A0F)

    // 5. Retro Mania (Vintage oranges and browns)
    val RetroManiaPrimary = Color(0xFFE65100)
    val RetroManiaSecondary = Color(0xFF795548)
    val RetroManiaBackground = Color(0xFF3E2723)

    // 6. Manager Mode (CAF Premium Tactical Tier)
    val ManagerModePrimary = FameColors.DeepBlue
    val ManagerModeSecondary = FameColors.PitchGreen
    val ManagerModeBackground = FameColors.SharedNeutralBase

    // 7. News Mode (CAF Premium Narrative Tier)
    val NewsModePrimary = FameColors.KenteRed
    val NewsModeSecondary = FameColors.BoldBlack
    val NewsModeBackground = FameColors.SharedNeutralBase
}
