package com.fameafrica.afm.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.theme.Inter

/**
 * Professional Management UI Typography
 * High density, high readability
 */
object AFMTextStyles {

    /** Extra small text for labels, stats, etc. (11.sp Inter) */
    val textXS = TextStyle(
        fontFamily = Inter,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )

    /** Extra-extra small text for dense UI (9.sp Inter) */
    val textXXS = TextStyle(
        fontFamily = Inter,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium
    )

    /** Small text for tables, lists, main readable data (12.sp Inter) */
    val textSM = TextStyle(
        fontFamily = Inter,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    )

    /** Medium text for secondary headers (14.sp Inter) */
    val textMD = TextStyle(
        fontFamily = Inter,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )

    /** Large text for headings (16.sp Inter) */
    val textLG = TextStyle(
        fontFamily = Inter,
        fontSize = 16.sp,
        fontWeight = FontWeight.Black
    )

    /** Main numerical data and important stats (12.sp Inter) */
    val statValue = textSM.copy(fontWeight = FontWeight.Bold)

    /** Small labels and descriptors (10.sp Inter) */
    val statLabel = textXS

    /** Headers for tables and sections (12.sp Inter) */
    val tableHeader = textSM.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)

    /** Standard table content (12.sp Inter) */
    val tableCell = textSM

    /** Large rating display for players/items */
    val playerRating = textLG.copy(fontSize = 18.sp, fontFeatureSettings = "tnum")

    val sectionHeader = textMD.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)

    /** Small ticker text for scrolling or compact UI (10.sp Inter) */
    val tickerText = textXS.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)

    /** Medium dense text (11.sp Inter) */
    val denseText = textXS.copy(fontSize = 11.sp)
}
