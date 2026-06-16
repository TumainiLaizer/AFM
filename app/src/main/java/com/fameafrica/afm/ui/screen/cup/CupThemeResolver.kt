package com.fameafrica.afm.ui.screen.cup

import androidx.compose.ui.graphics.Color
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.FameColors

data class CupTheme(
    val primaryColor: Color,
    val accentColor: Color,
    val headerRes: Int
)

fun determineCupTheme(cupName: String, cupType: String?): CupTheme {
    return when {
        cupName.contains("Champions League", ignoreCase = true) -> 
            CupTheme(Color(0xFF006400), FameColors.TrophyGold, R.drawable.caf_champions_league)
        cupName.contains("Confederation Cup", ignoreCase = true) -> 
            CupTheme(Color(0xFFE67E22), Color.White, R.drawable.caf_confederation_cup)
        cupName.contains("Muungano", ignoreCase = true) -> 
            CupTheme(Color(0xFF2C3E50), FameColors.TrophyGold, R.drawable.muungano_cup)
        cupName.contains("FAME Africa", ignoreCase = true) -> 
            CupTheme(FameColors.StadiumBlack, FameColors.ChampionsGold, R.drawable.fame_africa_cup)
        cupType == "Domestic" ->
            CupTheme(Color(0xFF1B5E20), Color.White, R.drawable.crdb_federation_cup)
        else -> 
            CupTheme(FameColors.StadiumBlack, FameColors.ChampionsGold, R.drawable.season_cup_round)
    }
}
