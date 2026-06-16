package com.fameafrica.afm.ui.screen.world

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.ui.screen.dashboard.NewsUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorldUiState(
    val isLoading: Boolean = true,
    val selectedContinent: String = "Africa",
    val latestNews: List<NewsUiModel> = emptyList(),
    val worldStats: WorldStatsUiModel = WorldStatsUiModel(),
    val featuredCompetitions: List<CompetitionUiModel> = emptyList(),
    val leaguesByRegion: Map<String, List<LeagueUiModel>> = emptyMap(),
    val majorCups: List<CupUiModel> = emptyList(),
    val nationalTeams: List<NationalTeamUiModel> = emptyList(),
    val continentalRankings: List<RankingUiModel> = emptyList(),
    val countryCoefficients: List<CountryCoefficientUiModel> = emptyList(),
    val clubRankings: List<ClubRankingUiModel> = emptyList(),
    val internationalFixtures: List<InternationalFixtureUiModel> = emptyList(),
    val transferNews: List<TransferNewsUiModel> = emptyList(),
    val globalLeagueRankings: List<com.fameafrica.afm.data.models.GlobalLeagueRanking> = emptyList(),
    val globalClubRankings: List<com.fameafrica.afm.data.models.GlobalClubRanking> = emptyList(),
    val globalManagerRankings: List<com.fameafrica.afm.data.models.GlobalManagerRanking> = emptyList(),
    val wonderkids: List<PlayerStatUiModel> = emptyList(),
    val dailySimulationEvents: List<SimulationEvent> = emptyList(),
    val nextMatch: MatchHubUiModel? = null,
    val rankingsLastUpdated: Long = 0L,
    val errorMessage: String? = null
)

data class WorldStatsUiModel(
    val topScorers: List<PlayerStatUiModel> = emptyList(),
    val topAssists: List<PlayerStatUiModel> = emptyList(),
    val cleanSheets: List<PlayerStatUiModel> = emptyList(),
    val hottestTeams: List<TeamStatUiModel> = emptyList()
)

data class PlayerStatUiModel(
    val playerId: Int,
    val name: String,
    val teamName: String,
    val value: Int,
    val photo: String? = null
)

data class TeamStatUiModel(
    val teamId: Int,
    val teamName: String,
    val statValue: String,
    val logoPath: String? = null
)

data class MatchHubUiModel(
    val matchId: Int,
    val competitionName: String,
    val matchDateDisplay: String,
    val matchTime: String,
    val isHome: Boolean,
    val opponentName: String,
    val opponentLogo: String? = null
)

data class CompetitionUiModel(
    val id: String,
    val name: String,
    val type: String,
    val confederation: String,
    val teams: Int,
    val logoUrl: Any?
)

data class LeagueUiModel(
    val id: String,
    val name: String,
    val country: String,
    val level: Int,
    val prizeMoney: Long,
    val logoUrl: Any?
)

data class CupUiModel(
    val id: String,
    val name: String,
    val type: String,
    val prizeMoney: Long,
    val teams: Int,
    val currentStage: String,
    val logoUrl: Any? = null
)

data class NationalTeamUiModel(
    val id: String,
    val name: String,
    val fifaRanking: Int,
    val confederation: String,
    val flagUrl: Any?,
    val eloRating: Int? = null
)

data class RankingUiModel(
    val country: String,
    val points: Int,
    val flagUrl: Any?,
    val change: Int = 0
)

data class ClubRankingUiModel(
    val teamId: Int,
    val teamName: String,
    val points: Double,
    val logoPath: String?,
    val rank: Int
)

data class CountryCoefficientUiModel(
    val country: String,
    val points: Double,
    val rank: Int
)

data class InternationalFixtureUiModel(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeFlag: Any?,
    val awayFlag: Any?,
    val date: String,
    val competition: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val isCompleted: Boolean = false
)

data class TransferNewsUiModel(
    val id: Int,
    val player: String,
    val fromTeam: String,
    val toTeam: String,
    val fee: Long,
    val date: String = ""
)

@HiltViewModel
class WorldViewModel @Inject constructor(
    private val leaguesRepository: LeaguesRepository,
    private val cupsRepository: CupsRepository,
    private val cupBracketsRepository: CupBracketsRepository,
    private val nationalTeamsRepository: NationalTeamsRepository,
    private val fixturesRepository: FixturesRepository,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val transfersRepository: TransfersRepository,
    private val newsRepository: NewsRepository,
    private val worldStateRepository: WorldStateRepository,
    private val rankingsRepository: com.fameafrica.afm.data.repository.RankingsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldUiState(isLoading = true))
    val uiState: StateFlow<WorldUiState> = _uiState.asStateFlow()

    init {
        observeWorldState()
        observeGameEvents()
    }

    private fun observeWorldState() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                when (state) {
                    is GameManager.GameState.Active -> {
                        loadWorldData(state.context)
                    }
                    is GameManager.GameState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is GameManager.GameState.NoSave -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No active career found"
                            )
                        }
                    }
                    is GameManager.GameState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = state.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeGameEvents() {
        viewModelScope.launch {
            gameManager.gameEvents.collect { event ->
                when (event) {
                    is GameManager.GameEvent.WeekAdvanced,
                    is GameManager.GameEvent.DaySimulated -> {
                        val state = gameManager.gameState.value
                        if (state is GameManager.GameState.Active) {
                            loadWorldData(state.context)
                        }
                    }
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            gameManager.dailyEvents.collect { events ->
                _uiState.update { it.copy(dailySimulationEvents = events) }
            }
        }
    }

    fun selectContinent(continent: String) {
        _uiState.update { it.copy(selectedContinent = continent) }
    }

    fun refreshData() {
        viewModelScope.launch {
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                loadWorldData(state.context)
            }
        }
    }

    private suspend fun loadWorldData(context: GameManager.GameContext) {
        try {
            // Parallel data loading
            val leaguesDeferred = viewModelScope.async { leaguesRepository.getAllLeagues().firstOrNull() ?: emptyList() }
            val cupsDeferred = viewModelScope.async { cupsRepository.getAllCups().firstOrNull() ?: emptyList() }
            val nationalsDeferred = viewModelScope.async { nationalTeamsRepository.getAllTeams().firstOrNull() ?: emptyList() }
            val transfersDeferred = viewModelScope.async { transfersRepository.getAllTransfers().firstOrNull() ?: emptyList() }
            val newsDeferred = viewModelScope.async { newsRepository.getTopNews(20).firstOrNull() ?: emptyList() }
            
            // Dynamic Career Context
            val nextMatchDeferred = viewModelScope.async {
                fixturesRepository.getNextMatchForTeam(context.teamId, context.currentDate)
            }

            // Stats Center
            val topScorersDeferred = viewModelScope.async { playersRepository.getTopRatedPlayers(10).firstOrNull() ?: emptyList() } 
            val hotTeamsDeferred = viewModelScope.async { teamsRepository.getTopTeamsByElo(10).firstOrNull() ?: emptyList() }
            val wonderkidsDeferred = viewModelScope.async { playersRepository.getTopYoungPlayers(10).firstOrNull() ?: emptyList() }

            val leagues = leaguesDeferred.await()
            val cups = cupsDeferred.await()
            val nationals = nationalsDeferred.await()
            val latestNewsEntities = newsDeferred.await()
            val nextFixtureEntity = nextMatchDeferred.await()
            val topPlayers = topScorersDeferred.await()
            val topTeams = hotTeamsDeferred.await()
            val wonderkids = wonderkidsDeferred.await()

            // Load Global Rankings
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val leagueRankingsJson = rankingsRepository.getRankingsByType("LEAGUE").firstOrNull()?.jsonData
            val clubRankingsJson = rankingsRepository.getRankingsByType("CLUB").firstOrNull()?.jsonData
            val managerRankingsJson = rankingsRepository.getRankingsByType("MANAGER").firstOrNull()?.jsonData

            val globalLeagues = leagueRankingsJson?.let { json.decodeFromString<List<com.fameafrica.afm.data.models.GlobalLeagueRanking>>(it) } ?: emptyList()
            val globalClubs = clubRankingsJson?.let { json.decodeFromString<List<com.fameafrica.afm.data.models.GlobalClubRanking>>(it) } ?: emptyList()
            val globalManagers = managerRankingsJson?.let { json.decodeFromString<List<com.fameafrica.afm.data.models.GlobalManagerRanking>>(it) } ?: emptyList()
            val lastUpdated = rankingsRepository.getRankingsByType("LEAGUE").firstOrNull()?.lastUpdated ?: 0L

            // Group leagues by African Football Regions
            val leagueModels = leagues.map { l ->
                LeagueUiModel(l.id.toString(), l.name, l.country ?: "Unknown", l.level, l.prizeMoney.toLong(), l.logo)
            }

            val leaguesByRegion = leagueModels.groupBy { league ->
                val country = league.country
                val nationalityItem = com.fameafrica.afm.utils.NationalityUtils.getNationalityItem(country)
                
                when (nationalityItem?.region) {
                    com.fameafrica.afm.utils.FootballRegion.EAST_AFRICA -> "East Africa"
                    com.fameafrica.afm.utils.FootballRegion.NORTH_AFRICA -> "North Africa"
                    com.fameafrica.afm.utils.FootballRegion.SOUTHERN_AFRICA -> "Southern Africa"
                    com.fameafrica.afm.utils.FootballRegion.WEST_AFRICA -> "West Africa"
                    com.fameafrica.afm.utils.FootballRegion.CENTRAL_AFRICA -> "Central Africa"
                    else -> "Others"
                }
            }
            // Map Real News
            val newsModels = latestNewsEntities.map { n ->
                NewsUiModel(n.id, n.headline, n.content.take(80) + "...", n.category)
            }

            // Map Stats
            val worldStats = WorldStatsUiModel(
                topScorers = topPlayers.take(5).map { PlayerStatUiModel(it.id, it.name, it.teamName ?: "Unknown", it.goals) },
                topAssists = topPlayers.take(5).map { PlayerStatUiModel(it.id, it.name, it.teamName ?: "Unknown", it.assists) },
                cleanSheets = topPlayers.filter { it.position == "GK" }.take(5).map { PlayerStatUiModel(it.id, it.name, it.teamName ?: "Unknown", it.cleanSheets) },
                hottestTeams = topTeams.take(5).map { TeamStatUiModel(it.id, it.name, "ELO: ${it.eloRating}", it.logoPath) }
            )

            val cupModels = cups.map { c ->
                CupUiModel(c.id.toString(), c.name, if (c.isDomesticCup) "Domestic" else "Continental", c.prizeMoney.toLong(), c.teamsInvolved, "In Progress", c.logo)
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    latestNews = newsModels,
                    worldStats = worldStats,
                    featuredCompetitions = cupModels.take(4).map { CompetitionUiModel(it.id, it.name, it.type, "CAF", it.teams, it.logoUrl) },
                    leaguesByRegion = leaguesByRegion,
                    majorCups = cupModels,
                    continentalRankings = nationals.sortedBy { it.fifaRanking ?: 999 }.take(10).mapIndexed { i, n -> RankingUiModel(n.name, 2000 - (n.fifaRanking ?: 999) * 2, null, if (i < 3) 2 else 0) },
                    globalLeagueRankings = globalLeagues,
                    globalClubRankings = globalClubs,
                    globalManagerRankings = globalManagers,
                    wonderkids = wonderkids.filter { it.potential >= 85 }.map { PlayerStatUiModel(it.id, it.name, it.teamName ?: "Unknown", it.potential) },
                    rankingsLastUpdated = lastUpdated,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Sync failed: ${e.message}") }
        }
    }

    private fun determineCurrentStage(brackets: List<com.fameafrica.afm.data.database.entities.CupBracketsEntity>): String {
        if (brackets.isEmpty()) return "Not Started"
        val finalMatch = brackets.find { it.round?.uppercase() == "FINAL" }
        if (finalMatch?.isCompleted == true) return "Winner: ${finalMatch.winner}"
        if (finalMatch?.isScheduled == true) return "Finals"
        val semis = brackets.filter { it.round?.uppercase()?.contains("SEMI") == true }
        if (semis.isNotEmpty()) return "Semi-Finals"
        return "In Progress"
    }

    companion object {
        private val africanCountries = setOf("Tanzania", "Egypt", "Morocco", "Algeria", "South Africa", "Nigeria", "Ghana", "Cameroon", "Ivory Coast", "Senegal")
        private val europeanCountries = setOf("England", "Spain", "Italy", "Germany", "France")
        private val asianCountries = setOf("Japan", "South Korea", "Saudi Arabia")
        private val americanCountries = setOf("USA", "Brazil", "Argentina", "Mexico")
    }
}
