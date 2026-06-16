package com.fameafrica.afm.ui.theme

import androidx.compose.ui.unit.dp

object Dimensions {

    /* =========================
       SPACING SYSTEM (FM DENSE UI)
       ========================= */

    val micro = 2.dp   // ultra-tight spacing (lists, stats rows)
    val xs = 4.dp      // minimal separation
    val sm = 6.dp      // compact spacing (default FM spacing)
    val md = 10.dp     // light separation (reduced from 12)
    val lg = 14.dp     // section grouping (reduced)
    val xl = 18.dp     // major grouping (reduced from 24)
    val xxl = 24.dp    // max spacing (rare use only)

    /* =========================
       PANEL / LAYOUT DENSITY
       ========================= */

    val panelPadding = 8.dp        // FM panels are tight
    val cardPadding = 6.dp         // internal card spacing
    val listItemSpacing = 4.dp     // between rows in tables/lists
    val sectionGap = 12.dp         // between UI sections

    /* =========================
       ICONS (COMPACT FM STYLE)
       ========================= */

    val iconXs = 14.dp
    val iconSm = 16.dp
    val iconMd = 18.dp
    val iconLg = 22.dp
    val iconXl = 26.dp

    /* =========================
       NAVIGATION (MINIMAL FOOTPRINT)
       ========================= */

    val bottomNavHeight = 56.dp    // reduced for more screen space
    val bottomNavIconSize = 22.dp

    /* =========================
       PLAYER CARDS (DENSE LIST VIEW)
       ========================= */

    val playerCardHeight = 72.dp   // FM-style compact row cards
    val playerAvatar = 44.dp       // smaller avatars for dense squads
    val playerStatBarHeight = 4.dp // thin stat indicators

    /* =========================
       MATCH UI (COMPACT OVERLAY STYLE)
       ========================= */

    val teamBadge = 40.dp
    val matchScore = 34.dp
    val matchMiniPanelHeight = 48.dp

    /* =========================
       TABLE / DATA GRID UI
       ========================= */

    val tableRowHeight = 28.dp
    val tableCellPadding = 6.dp
    val columnGap = 8.dp

    /* =========================
       TACTICS / FIELD UI
       ========================= */

    val pitchPadding = 6.dp
    val playerNodeSize = 18.dp  // tactical dots on pitch
}