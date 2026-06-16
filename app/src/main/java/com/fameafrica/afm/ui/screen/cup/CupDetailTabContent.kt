package com.fameafrica.afm.ui.screen.cup

import androidx.compose.runtime.Composable
import com.fameafrica.afm.ui.screen.league.TeamOfTheWeekPage

@Composable
fun CupDetailTabContent(tabTitle: String?, uiState: CupDrawUiState) {
    when (tabTitle) {
        "STANDINGS" -> CupGroupsView(uiState.groupStandings)
        "BRACKETS" -> KnockoutView(uiState)
        "FIXTURES" -> CupFixturesPage(uiState)
        "STATS" -> CupStatsPage(uiState.cupStats)
        "TOTW" -> TeamOfTheWeekPage(
            totwPlayers = uiState.teamOfTheWeek,
            formation = "4-3-3",
            currentRound = 1,
            maxRounds = 1,
            onRoundChange = {}
        )
        "HISTORY" -> CupHistoryPage(uiState.history)
    }
}
