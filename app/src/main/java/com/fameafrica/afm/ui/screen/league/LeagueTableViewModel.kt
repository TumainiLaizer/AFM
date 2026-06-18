package com.fameafrica.afm.ui.screen.league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.screen.match.FixtureUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LeagueTableViewModel @Inject constructor(
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val playersRepository: PlayersRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val fixturesRepository: FixturesRepository,
    private val teamsRepository: TeamsRepository,
    private val matchEventsRepository: MatchEventsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueTableUiState(isLoading = true))
    val uiState: StateFlow<LeagueTableUiState> = _uiState.asStateFlow()

    data class PlayerWeeklyPerformance(
        val playerId: Int,
        val playerName: String,
        val teamName: String,
        val position: String,
        var rating: Double = 0.0,
        var goals: Int = 0,
        var assists: Int = 0,
        var motm: Boolean = false,
        var cleanSheet: Boolean = false,
        var matchesPlayed: Int = 0
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active && _uiState.value.leagueName.isNotEmpty()) {
                    refreshLeagueData(_uiState.value.leagueName, state.context)
                }
            }
        }
    }

    fun loadLeagueData(leagueName: String) {
        val currentState = gameManager.gameState.value
        if (currentState is GameManager.GameState.Active) {
            viewModelScope.launch(Dispatchers.IO) {
                val allLeagues = leaguesRepository.getAllLeagues().firstOrNull() ?: emptyList()
                val currentLeague = allLeagues.find { it.name.equals(leagueName, ignoreCase = true) }
                
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(
                        leagueName = currentLeague?.name ?: leagueName,
                        leagueLogo = currentLeague?.logo,
                        availableLeagues = allLeagues
                    ) }
                }
                refreshLeagueData(currentLeague?.name ?: leagueName, currentState.context)
            }
        }
    }

    private suspend fun refreshLeagueData(leagueName: String, context: GameManager.GameContext) = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            _uiState.update { it.copy(isLoading = true) }
        }
        try {
            val season = context.season
            val seasonYear = try { season.split("/").first().toInt() } catch (e: Exception) { 2025 }

            val standings = leagueStandingsRepository.getStandings(leagueName, seasonYear).firstOrNull() ?: emptyList()
            val standingModels = standings.map { standing ->
                val form = fixturesResultsRepository.getTeamForm(standing.teamId).formString
                val team = teamsRepository.getTeamById(standing.teamId)
                LeagueStandingUiModel(
                    id = standing.id, 
                    position = standing.position, 
                    teamName = standing.teamName,
                    played = standing.matchesPlayed, 
                    wins = standing.wins, 
                    draws = standing.draws,
                    losses = standing.losses, 
                    gf = standing.goalsScored, 
                    ga = standing.goalsConceded,
                    goalDifference = standing.goalDifference, 
                    points = standing.points,
                    form = form.padEnd(5, '-').take(5),
                    logoPath = team?.logoPath
                )
            }

            val repoStats = fixturesRepository.getLeagueStatistics(leagueName, context.season)
            val leagueStats = LeagueStatsUiModel(
                totalMatches = repoStats.totalMatches,
                totalGoals = repoStats.totalGoals,
                avgGoals = repoStats.averageGoalsPerGame,
                homeWinPct = repoStats.homeWinPercentage,
                awayWinPct = repoStats.awayWinPercentage,
                drawPct = repoStats.drawPercentage
            )

            val fixtures = fixturesRepository.getLeagueFixtures(leagueName, season).firstOrNull() ?: emptyList()
            val fixtureModels = fixtures.map { f ->
                FixtureUiModel(
                    id = f.id,
                    homeTeam = f.homeTeam,
                    awayTeam = f.awayTeam,
                    homeScore = f.homeScore ?: 0,
                    awayScore = f.awayScore ?: 0,
                    status = if (f.isCompleted) "FT" else "SCHEDULED",
                    round = f.position ?: 1
                )
            }
            
            val currentRound = fixturesRepository.getCurrentGameWeek(leagueName, season)
            val maxRounds = fixtures.maxOfOrNull { it.position ?: 0 } ?: 30
            val totwRound = if (currentRound > 1) currentRound - 1 else 1

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(
                    isLoading = false,
                    season = season,
                    standings = standingModels,
                    leagueStats = leagueStats,
                    fixtures = fixtureModels,
                    maxRounds = maxRounds,
                    teamOfTheWeekRound = totwRound,
                    userTeamId = context.teamId
                ) }
            }

            loadPlayerStats(leagueName)
            loadTOTW(leagueName, totwRound, season)

        } catch (e: Exception) {
             withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}") }
            }
        }
    }

    private suspend fun loadPlayerStats(leagueName: String) = withContext(Dispatchers.IO) {
        val leaguePlayers = playersRepository.getPlayersByLeague(leagueName).firstOrNull() ?: emptyList()

        val topScorers = leaguePlayers
            .filter { it.goals > 0 }
            .sortedByDescending { it.goals }
            .take(10)
            .mapIndexed { i, p ->
                TopScorerUiModel(i + 1, p.id, p.name, p.teamName, p.goals, p.matches, p.age)
            }

        val topAssisters = leaguePlayers
            .filter { it.assists > 0 }
            .sortedByDescending { it.assists }
            .take(10)
            .mapIndexed { i, p ->
                TopAssisterUiModel(i + 1, p.id, p.name, p.teamName, p.assists, p.matches)
            }

        withContext(Dispatchers.Main) {
            _uiState.update { it.copy(topScorers = topScorers, topAssisters = topAssisters) }
        }
    }

    suspend fun loadTOTW(leagueName: String, round: Int, season: String) = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            _uiState.update { it.copy(teamOfTheWeekRound = round) }
        }

        val fixtures = fixturesRepository.getLeagueFixtures(leagueName, season).firstOrNull() ?: emptyList()
        val roundFixtures = fixtures.filter { it.position == round && it.isCompleted }

        if (roundFixtures.isEmpty()) {
            withContext(Dispatchers.Main) { _uiState.update { it.copy(teamOfTheWeek = emptyList()) } }
            return@withContext
        }

        val performances = mutableMapOf<Int, PlayerWeeklyPerformance>()

        for (fixture in roundFixtures) {
            val events = matchEventsRepository.getEventsByMatch(fixture.id).firstOrNull() ?: continue
            val results = fixturesResultsRepository.getResultByFixtureId(fixture.id)

            events.forEach { event ->
                val playerId = event.playerId
                if (playerId == 0) return@forEach

                val player = playersRepository.getPlayerById(playerId) ?: return@forEach
                val performance = performances.getOrPut(playerId) {
                    PlayerWeeklyPerformance(
                        playerId = playerId,
                        playerName = player.name,
                        teamName = event.teamName,
                        position = player.position
                    )
                }

                performance.matchesPlayed++

                when (event.eventType) {
                    "GOAL", "PENALTY_SCORED" -> {
                        performance.goals++
                        performance.rating += 1.2
                    }
                    "OWN_GOAL" -> performance.rating -= 0.5
                    "YELLOW_CARD" -> performance.rating -= 0.3
                    "RED_CARD" -> performance.rating -= 1.0
                    "ASSIST" -> {
                        performance.assists++
                        performance.rating += 0.8
                    }
                }
            }

            results?.let { res ->
                if (res.homeScore == 0) {
                    performances.values.find { it.teamName == res.homeTeam && it.position == "GK" }?.cleanSheet = true
                }
                if (res.awayScore == 0) {
                    performances.values.find { it.teamName == res.awayTeam && it.position == "GK" }?.cleanSheet = true
                }

                performances.values
                    .filter { it.teamName == res.homeTeam || it.teamName == res.awayTeam }
                    .maxByOrNull { it.rating }
                    ?.motm = true
            }
        }

        if (performances.isEmpty()) {
            withContext(Dispatchers.Main) { _uiState.update { it.copy(teamOfTheWeek = emptyList()) } }
            return@withContext
        }

        performances.values.forEach { perf ->
            perf.rating = perf.rating.coerceIn(0.0, 10.0)
        }

        val goalkeepers = performances.values.filter { it.position == "GK" }.sortedByDescending { it.rating }
        val defenders = performances.values.filter { it.positionCategory == "DEFENDER" }.sortedByDescending { it.rating }
        val midfielders = performances.values.filter { it.positionCategory == "MIDFIELDER" }.sortedByDescending { it.rating }
        val forwards = performances.values.filter { it.positionCategory == "FORWARD" }.sortedByDescending { it.rating }

        val bestFormation = determineBestFormation(defenders, midfielders, forwards)
        val selectedPlayers = selectPlayersForFormation(bestFormation, goalkeepers, defenders, midfielders, forwards)

        val totwModels = selectedPlayers.map { player ->
            TOTWPlayerUiModel(
                playerId = player.playerId,
                playerName = player.playerName,
                teamName = player.teamName,
                position = player.position,
                matchRating = player.rating,
                nationality = null,
                shirtNumber = 0,
                goals = player.goals,
                assists = player.assists,
                cleanSheet = player.cleanSheet,
                motm = player.motm
            )
        }

        withContext(Dispatchers.Main) {
            _uiState.update { it.copy(teamOfTheWeek = totwModels, totwFormation = bestFormation) }
        }
    }

    private fun determineBestFormation(
        defenders: List<PlayerWeeklyPerformance>,
        midfielders: List<PlayerWeeklyPerformance>,
        forwards: List<PlayerWeeklyPerformance>
    ): String {
        val d = defenders.size
        val m = midfielders.size
        val f = forwards.size

        return when {
            m >= 5 && f >= 2 -> "3-5-2"
            m >= 5 && f >= 1 -> "4-5-1"
            f >= 3 && m >= 3 -> "4-3-3"
            d >= 5 && m >= 3 && f >= 2 -> "5-3-2"
            else -> "4-4-2"
        }
    }

    private fun selectPlayersForFormation(
        formation: String,
        goalkeepers: List<PlayerWeeklyPerformance>,
        defenders: List<PlayerWeeklyPerformance>,
        midfielders: List<PlayerWeeklyPerformance>,
        forwards: List<PlayerWeeklyPerformance>
    ): List<PlayerWeeklyPerformance> {
        val selected = mutableListOf<PlayerWeeklyPerformance>()
        val usedPlayers = mutableSetOf<Int>()

        val requirements = when (formation) {
            "4-3-3" -> mapOf("GK" to 1, "DEF" to 4, "MID" to 3, "FWD" to 3)
            "4-4-2" -> mapOf("GK" to 1, "DEF" to 4, "MID" to 4, "FWD" to 2)
            "4-5-1" -> mapOf("GK" to 1, "DEF" to 4, "MID" to 5, "FWD" to 1)
            "5-3-2" -> mapOf("GK" to 1, "DEF" to 5, "MID" to 3, "FWD" to 2)
            "3-5-2" -> mapOf("GK" to 1, "DEF" to 3, "MID" to 5, "FWD" to 2)
            else -> mapOf("GK" to 1, "DEF" to 4, "MID" to 4, "FWD" to 2)
        }

        goalkeepers.take(1).forEach { gk ->
            selected.add(gk)
            usedPlayers.add(gk.playerId)
        }

        defenders
            .filter { it.playerId !in usedPlayers }
            .take(requirements["DEF"] ?: 4)
            .forEach { def ->
                selected.add(def)
                usedPlayers.add(def.playerId)
            }

        val neededDef = (requirements["DEF"] ?: 4) - selected.count { it.positionCategory == "DEFENDER" }
        if (neededDef > 0) {
            midfielders
                .filter { it.position == "CDM" && it.playerId !in usedPlayers }
                .take(neededDef)
                .forEach { dm ->
                    selected.add(dm)
                    usedPlayers.add(dm.playerId)
                }
        }

        midfielders
            .filter { it.playerId !in usedPlayers }
            .take(requirements["MID"] ?: 4)
            .forEach { mid ->
                selected.add(mid)
                usedPlayers.add(mid.playerId)
            }

        forwards
            .filter { it.playerId !in usedPlayers }
            .take(requirements["FWD"] ?: 2)
            .forEach { fwd ->
                selected.add(fwd)
                usedPlayers.add(fwd.playerId)
            }

        return selected
    }

    private val PlayerWeeklyPerformance.positionCategory: String
        get() = when (position) {
            "GK" -> "GOALKEEPER"
            "CB", "LB", "RB", "LWB", "RWB" -> "DEFENDER"
            "CDM", "CM", "CAM", "LM", "RM" -> "MIDFIELDER"
            "LW", "RW", "ST", "CF" -> "FORWARD"
            else -> "MIDFIELDER"
        }
}
