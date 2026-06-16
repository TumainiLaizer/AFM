package com.fameafrica.afm.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FAME Africa™ Official Color System
 * Heritage + Power + Stadium Atmosphere + Continental Unity
 * Modern African football authority, not clichés
 */
object FameColors {

    // ============ BRAND COLORS - NEW DESIGN SYSTEM ============

    /** Deep Navy Charcoal - Updated to greyish background */
    val DeepNavyBlack = Color(0xFF3C434B)

    /** Header Gradient Start - Dark Grey */
    val HeaderDark = Color(0xFF2C2E33)

    /** Gold Accent - More vibrant for the new UI */
    val TrophyGold = Color(0xFFE5B134)

    /** Green Success - For "Satisfied", "Manager Mode" */
    val GrowthGreen = Color(0xFF4CAF50)

    /** Red Warning - For "Unhappy", "Manager Tension" */
    val AlertRed = Color(0xFFE53935)

    /** Blue Info - For "Transfer Hub", "Job Offers" */
    val TransferBlue = Color(0xFF1976D2)

    /** Pitch Authority Green - Headers, active tabs, league tables */
    val PitchGreen = Color(0xFF1B5E20) // Warmer forest green

    /** Champions Gold - Trophies, reputation stars, board approval */
    val ChampionsGold = Color(0xFFD4A017)

    /** Midnight Stadium Grey - Updated to stay in the new palette */
    val StadiumBlack = Color(0xFF1C1E21)


    // ============ CAF PREMIUM PALETTE GRID ADDITIONS ============

    /** Shared Neutral Base - Updated to greyish background */
    val SharedNeutralBase = Color(0xFF44474E)

    /** Amber Bronze - Chairman tier secondary accent */
    val AmberBronze = Color(0xFFCD7F32)

    /** Deep Blue - Manager tier primary accent */
    val DeepBlue = Color(0xFF003366)

    /** Bold Black - News tier secondary accent */
    val BoldBlack = Color(0xFF000000)


    // ============ SECONDARY CULTURAL PALETTE ============

    /** Afro-Sun Orange - Goals, breaking news, transfer deadlines */
    val AfroSunOrange = Color(0xFFFF7A00)

    /** Kente Passion Red - Cards, board tension, rivalry matches */
    val KenteRed = Color(0xFF9E1B1B)

    /** Baobab Brown - History, heritage, trophy cabinet, youth academy */
    val BaobabBrown = Color(0xFF6B4F2A)


    // ============ TEXT COLORS (Never pure white) ============

    /** Warm Ivory - Primary text */
    val WarmIvory = Color(0xFFF2EAD3)

    /** Muted Parchment - Secondary text */
    val MutedParchment = Color(0xFFC8BFAE)

    /** Disabled Text - 40% opacity of Muted Parchment */
    val DisabledText = Color(0xFF8E8578)


    // ============ MATCH DAY PITCH ============

    /** Match Pitch Green - Brighter than UI green for pitch view */
    val MatchPitch = Color(0xFF2E7D32)


    // ============ REPUTATION PROGRESSION ============

    val LocalBronze = Color(0xFFB87333)
    val NationalSilver = Color(0xFFC0C0C0)
    val ContinentalGold = ChampionsGold
    val AfricanLegendEmerald = Color(0xFF00A86B)


    // ============ SEMANTIC COLORS ============

    val Success = Color(0xFF388E3C)
    val Warning = AfroSunOrange
    val Error = KenteRed
    val Info = Color(0xFF0288D1)


    // ============ BACKGROUND VARIANTS ============

    val SurfaceDark = Color(0xFF37393E)
    val SurfaceMedium = Color(0xFF44474E)
    val SurfaceLight = Color(0xFF51545C)

    /** Light theme background (optional) */
    val LightSand = Color(0xFFF4E9D8)
    val LightCard = Color(0xFFFFFFFF)
    val LightBorder = BaobabBrown.copy(alpha = 0.2f)
}
