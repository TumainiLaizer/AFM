package com.fameafrica.afm.domain.model

import com.fameafrica.afm.data.database.entities.*

/**
 * Unified Career State Architecture for AFM2026.
 * Inspired by FCM26's lightweight but deep simulation.
 * This object is the single source of truth for the UI and simulation observers.
 */
data class CareerState(
    val world: WorldState,
    val club: ClubState,
    val manager: ManagerState,
    val competition: CompetitionState,
    val finance: FinanceState,
    val transfer: TransferState,
    val timeline: TimelineState
) {
    companion object {
        /**
         * Creates a loading/empty state for initial UI binding.
         */
        fun empty() = CareerState(
            world = WorldState(),
            club = ClubState(teamId = -1, teamName = "Loading..."),
            manager = ManagerState(id = -1, name = "Loading...", reputation = 0, reputationLevel = "Local", jobSecurity = 100, managerXp = 0, managerLevel = 1),
            competition = CompetitionState(leagueName = null, domesticCupName = null),
            finance = FinanceState(balance = 0, wageBudget = 0, transferBudget = 0),
            transfer = TransferState(isWindowOpen = false),
            timeline = TimelineState(currentWeek = 1, currentSeason = "2024/25", gameDateDisplay = "August 2024", isPreseason = true)
        )
    }
}

data class WorldState(
    val continentalRankings: Map<String, Int> = emptyMap(),
    val leagueReputations: Map<String, Double> = emptyMap(),
    val topClubs: List<Int> = emptyList(),
    val newsHeadlines: List<NewsEntity> = emptyList(),
    val dailyEvents: List<SimulationEvent> = emptyList()
)

data class ClubState(
    val teamId: Int,
    val teamName: String,
    val squad: List<PlayersEntity> = emptyList(),
    val nextMatch: FixturesEntity? = null,
    val recentResults: List<FixturesResultsEntity> = emptyList(),
    val facilitiesLevel: Int = 1,
    val boardConfidence: Int = 100,
    val fanSentiment: Int = 100
)

data class ManagerState(
    val id: Int,
    val name: String,
    val nationality: String = "Tanzania",
    val reputation: Int,
    val reputationLevel: String,
    val jobSecurity: Int,
    val managerXp: Int = 0,
    val managerLevel: Int = 1,
    val contractsManaged: Int = 1,
    val performanceRating: Int = 0,
    val pressureLevel: Int = 0 // 0-100, high pressure affects performance and mental health events
)

data class CompetitionState(
    val leagueName: String?,
    val leagueStandings: List<LeagueStandingsEntity> = emptyList(),
    val domesticCupName: String?,
    val internationalCompetition: String? = null
)

data class FinanceState(
    val balance: Long,
    val coins: Long = 0,
    val premiumCurrency: Long = 0,
    val wageBudget: Long,
    val transferBudget: Long,
    val monthlyProfit: Long = 0,
    val seasonRevenue: Long = 0
)

data class TransferState(
    val isWindowOpen: Boolean,
    val activeBids: List<TransfersEntity> = emptyList(),
    val scoutedPlayers: List<Int> = emptyList(),
    val transferRumors: List<NewsEntity> = emptyList()
)

data class TimelineState(
    val currentWeek: Int,
    val currentSeason: String,
    val gameDateDisplay: String,
    val isPreseason: Boolean
)
