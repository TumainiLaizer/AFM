package com.fameafrica.afm.ui.screen.cup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.CupBracketsEntity
import com.fameafrica.afm.data.database.entities.CupGroupStandingsEntity
import com.fameafrica.afm.data.database.entities.CupsEntity
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import com.fameafrica.afm.data.database.entities.KnockoutMatchesEntity
import com.fameafrica.afm.data.repository.CupBracketsRepository
import com.fameafrica.afm.data.repository.CupGroupStandingsRepository
import com.fameafrica.afm.data.repository.CupsRepository
import com.fameafrica.afm.data.repository.FixturesRepository
import com.fameafrica.afm.data.repository.FixturesResultsRepository
import com.fameafrica.afm.data.repository.KnockoutMatchesRepository
import com.fameafrica.afm.data.repository.MatchEventsRepository
import com.fameafrica.afm.data.repository.PlayersRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.data.repository.TrophiesRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.screen.league.TOTWPlayerUiModel
import com.fameafrica.afm.ui.screen.match.FixtureUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.SortedMap
import javax.inject.Inject

data class CupStatsUiModel(
    val totalGoals: Int = 0,
    val totalMatches: Int = 0,
    val averageGoals: Double = 0.0,
    val topScorer: String = "N/A",
    val topScorerGoals: Int = 0,
    val cleanSheetLeader: String = "N/A",
    val averageAttendance: Int = 0,
    val topScorers: List<CupPlayerStatUiModel> = emptyList(),
    val topAssisters: List<CupPlayerStatUiModel> = emptyList(),
    val topGoalsAssists: List<CupPlayerStatUiModel> = emptyList(),
    val topGKs: List<CupPlayerStatUiModel> = emptyList()
)

data class CupPlayerStatUiModel(
    val rank: Int,
    val playerName: String,
    val teamName: String,
    val value: Int,
    val label: String,
    val nationality: String? = null
)

data class CupHistoryUiModel(
    val season: String,
    val winner: String,
    val runnerUp: String,
    val winnerLogo: String? = null,
    val runnerUpLogo: String? = null
)

data class CupDrawUiState(
    val cup: CupsEntity? = null,
    val matches: List<KnockoutMatchesEntity> = emptyList(),
    val bracketsByRound: SortedMap<Int, List<CupBracketsEntity>>? = null,
    val groupStandings: SortedMap<String, List<CupGroupStandingsEntity>>? = null,
    val cupStats: CupStatsUiModel = CupStatsUiModel(),
    val fixtures: List<FixtureUiModel> = emptyList(),
    val teamOfTheWeek: List<TOTWPlayerUiModel> = emptyList(),
    val history: List<CupHistoryUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val season: String = "2025/26",
    val cupName: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class CupDrawViewModel @Inject constructor(
    private val cupsRepository: CupsRepository,
    private val knockoutMatchesRepository: KnockoutMatchesRepository,
    private val cupBracketsRepository: CupBracketsRepository,
    private val cupGroupStandingsRepository: CupGroupStandingsRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val fixturesRepository: FixturesRepository,
    private val trophiesRepository: TrophiesRepository,
    private val matchEventsRepository: MatchEventsRepository,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CupDrawUiState())
    val uiState: StateFlow<CupDrawUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                when (state) {
                    is GameManager.GameState.Active -> {
                        if (_uiState.value.cupName.isNotEmpty()) {
                            refreshCupData(_uiState.value.cupName, state.context)
                        }
                    }
                    is GameManager.GameState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is GameManager.GameState.NoSave -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "No active career found") }
                    }
                    is GameManager.GameState.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = state.message) }
                    }
                }
            }
        }
    }

    fun loadCupData(cupName: String) {
        val state = gameManager.gameState.value
        if (state is GameManager.GameState.Active) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                _uiState.update { it.copy(cupName = cupName, isLoading = true, errorMessage = null) }
                refreshCupData(cupName, state.context)
            }
        } else {
            _uiState.update { it.copy(cupName = cupName, isLoading = false, errorMessage = "Game not active") }
        }
    }

    fun refreshData() {
        val state = gameManager.gameState.value
        if (state is GameManager.GameState.Active && _uiState.value.cupName.isNotEmpty()) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                refreshCupData(_uiState.value.cupName, state.context)
            }
        }
    }

    private suspend fun refreshCupData(cupName: String, context: GameManager.GameContext) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, season = context.season) }

        try {
            val cup = cupsRepository.getCupByName(cupName)
            if (cup == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Cup not found: $cupName") }
                return@withContext
            }

            val seasonYear = context.season.split("/").first().toInt()

            // Load all data in parallel
            val matchesDeferred = async {
                knockoutMatchesRepository.getMatchesByCupAndSeason(cupName, context.season).firstOrNull() ?: emptyList()
            }
            val bracketsDeferred = async {
                cupBracketsRepository.getBracketsByCupAndSeason(cupName, seasonYear).firstOrNull() ?: emptyList()
            }
            val standingsDeferred = async {
                cupGroupStandingsRepository.getGroupStandings(
                    cupName,
                    seasonYear = seasonYear,
                    groupName = cupName.split(" - ").lastOrNull() ?: "Group A"
                ).firstOrNull() ?: emptyList()
            }
            val historyDeferred = async {
                trophiesRepository.getTrophiesByCompetition(cupName).firstOrNull() ?: emptyList()
            }
            val resultsDeferred = async {
                fixturesResultsRepository.getCupResults(cupName, context.season).firstOrNull() ?: emptyList()
            }
            val fixturesDeferred = async {
                fixturesRepository.getCupFixtures(cupName, context.season).firstOrNull() ?: emptyList()
            }

            val matches = matchesDeferred.await()
            val brackets = bracketsDeferred.await()
            val standings = standingsDeferred.await()
            val history = historyDeferred.await()
            val results = resultsDeferred.await()
            val fixtures = fixturesDeferred.await()

            // Calculate cup stats
            val cupStats = calculateCupStats(results, matches)

            // Generate TOTW for the cup (based on recent matches)
            val totw = generateCupTOTW(cupName, seasonYear, results)

            // Group standings by group name
            val groupStandings = if (standings.isNotEmpty()) {
                standings.groupBy {
                    it.cupName.split(" - ").lastOrNull() ?: "Group A"
                }.toSortedMap()
            } else null

            // Group brackets by round number
            val bracketsByRound = if (brackets.isNotEmpty()) {
                brackets.groupBy { it.roundNumber }.toSortedMap()
            } else null

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        cup = cup,
                        matches = matches,
                        bracketsByRound = bracketsByRound,
                        groupStandings = groupStandings,
                        cupStats = cupStats,
                        fixtures = fixtures.map { f ->
                            FixtureUiModel(
                                id = f.id,
                                homeTeam = f.homeTeam,
                                awayTeam = f.awayTeam,
                                homeScore = f.homeScore ?: 0,
                                awayScore = f.awayScore ?: 0,
                                status = if (f.isCompleted) "FT" else "SCHEDULED",
                                round = f.position ?: 1
                            )
                        },
                        teamOfTheWeek = totw,
                        history = history.map { t ->
                            CupHistoryUiModel(
                                t.season,
                                t.clubName,
                                t.opponent ?: "N/A",
                                t.iconPath,
                                null
                            )
                        },
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }

            Log.d("AFM_CUP", "Loaded cup data for $cupName: ${matches.size} matches, ${brackets.size} brackets, ${standings.size} standings")

        } catch (e: Exception) {
            Log.e("AFM_CUP", "Failed to load cup data: ${e.message}", e)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load cup data: ${e.message}") }
            }
        }
    }

    private suspend fun calculateCupStats(
        results: List<FixturesResultsEntity>,
        matches: List<KnockoutMatchesEntity>
    ): CupStatsUiModel {
        val totalGoals = results.sumOf { it.homeScore + it.awayScore }
        val totalMatches = results.size
        val averageGoals = if (totalMatches > 0) totalGoals.toDouble() / totalMatches else 0.0

        // Get top scorers from match events
        val allGoals = mutableListOf<Triple<String, String, Int>>() // playerName, teamName, goals

        results.forEach { result ->
            val events = matchEventsRepository.getEventsByMatch(result.fixtureId).firstOrNull() ?: emptyList()
            events.filter { it.eventType == "GOAL" || it.eventType == "PENALTY_SCORED" }
                .forEach { event ->
                    allGoals.add(Triple(event.playerName, event.teamName, 1))
                }
        }

        val topScorers = allGoals.groupBy { it.first to it.second }
            .map { (key, goals) -> Triple(key.first, key.second, goals.size) }
            .sortedByDescending { it.third }
            .take(10)
            .mapIndexed { index, (player, team, goals) ->
                CupPlayerStatUiModel(
                    rank = index + 1,
                    playerName = player,
                    teamName = team,
                    value = goals,
                    label = "GOALS",
                    nationality = null
                )
            }

        // Get clean sheets (GK with 0 goals conceded in match)
        val cleanSheets = mutableMapOf<String, Int>()
        results.forEach { result ->
            if (result.homeScore == 0) {
                val homeGK = playersRepository.getPlayersByTeamName(result.homeTeam).firstOrNull()
                    ?.find { it.position == "GK" }
                homeGK?.let { cleanSheets[it.name] = (cleanSheets[it.name] ?: 0) + 1 }
            }
            if (result.awayScore == 0) {
                val awayGK = playersRepository.getPlayersByTeamName(result.awayTeam).firstOrNull()
                    ?.find { it.position == "GK" }
                awayGK?.let { cleanSheets[it.name] = (cleanSheets[it.name] ?: 0) + 1 }
            }
        }

        val topGKs = cleanSheets.entries
            .sortedByDescending { it.value }
            .take(5)
            .mapIndexed { index, (player, sheets) ->
                CupPlayerStatUiModel(
                    rank = index + 1,
                    playerName = player,
                    teamName = "",
                    value = sheets,
                    label = "CLEAN SHEETS",
                    nationality = null
                )
            }

        // Calculate average attendance
        val averageAttendance = if (results.isNotEmpty()) {
            results.sumOf { it.attendance } / results.size
        } else 0

        return CupStatsUiModel(
            totalGoals = totalGoals,
            totalMatches = totalMatches,
            averageGoals = averageGoals,
            topScorer = topScorers.firstOrNull()?.playerName ?: "N/A",
            topScorerGoals = topScorers.firstOrNull()?.value ?: 0,
            cleanSheetLeader = topGKs.firstOrNull()?.playerName ?: "N/A",
            averageAttendance = averageAttendance,
            topScorers = topScorers,
            topGKs = topGKs,
            topAssisters = emptyList(),
            topGoalsAssists = emptyList()
        )
    }

    private suspend fun generateCupTOTW(
        cupName: String,
        seasonYear: Int,
        results: List<FixturesResultsEntity>
    ): List<TOTWPlayerUiModel> {
        // Get the most recent round of matches
        val latestResults = results.takeLast(8)
        val performances = mutableMapOf<Int, Triple<String, String, Double>>() // playerId -> (name, team, rating)

        latestResults.forEach { result ->
            val events = matchEventsRepository.getEventsByMatch(result.fixtureId).firstOrNull() ?: emptyList()
            val playerRatings = mutableMapOf<Int, Double>()

            events.forEach { event ->
                if (event.playerId > 0) {
                    val current = playerRatings[event.playerId] ?: 6.0
                    val increment = when (event.eventType) {
                        "GOAL", "PENALTY_SCORED" -> 1.2
                        "ASSIST" -> 0.8
                        "YELLOW_CARD" -> -0.3
                        "RED_CARD" -> -1.0
                        else -> 0.0
                    }
                    playerRatings[event.playerId] = (current + increment).coerceIn(1.0, 10.0)
                    performances[event.playerId] = Triple(event.playerName, event.teamName, playerRatings[event.playerId] ?: 6.0)
                }
            }
        }

        // Return top 11 performers
        return performances.entries
            .sortedByDescending { it.value.third }
            .take(11)
            .mapIndexed { index, (_, value) ->
                TOTWPlayerUiModel(
                    playerId = index,
                    playerName = value.first,
                    teamName = value.second,
                    position = "MID",
                    matchRating = value.third,
                    nationality = null,
                    shirtNumber = 0,
                    goals = 0,
                    assists = 0,
                    cleanSheet = false,
                    motm = index == 0
                )
            }
    }
}