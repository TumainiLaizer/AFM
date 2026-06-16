package com.fameafrica.afm.ui.screen.league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============ UI Models ============

data class LeagueStandingUiModel(
    val id: Int,
    val position: Int,
    val teamName: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val gf: Int,
    val ga: Int,
    val goalDifference: Int,
    val points: Int,
    val form: String,
    val logoPath: String? = null
)

data class TopScorerUiModel(
    val position: Int,
    val playerId: Int,
    val playerName: String,
    val teamName: String,
    val goals: Int,
    val appearances: Int,
    val age: Int
)

data class TopAssisterUiModel(
    val position: Int,
    val playerId: Int,
    val playerName: String,
    val teamName: String,
    val assists: Int,
    val appearances: Int
)

data class TOTWPlayerUiModel(
    val playerId: Int,
    val playerName: String,
    val teamName: String,
    val position: String,
    val matchRating: Double,
    val nationality: String? = null,
    val shirtNumber: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val cleanSheet: Boolean = false,
    val motm: Boolean = false
)

data class FixtureUiModel(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val date: String,
    val round: Int,
    val isCompleted: Boolean
)

data class LeagueStatsUiModel(
    val totalMatches: Int = 0,
    val totalGoals: Int = 0,
    val avgGoals: Double = 0.0,
    val homeWinPct: Double = 0.0,
    val awayWinPct: Double = 0.0,
    val drawPct: Double = 0.0
)

data class LeagueTableUiState(
    val isLoading: Boolean = true,
    val season: String = "2025/26",
    val standings: List<LeagueStandingUiModel> = emptyList(),
    val topScorers: List<TopScorerUiModel> = emptyList(),
    val topAssisters: List<TopAssisterUiModel> = emptyList(),
    val teamOfTheWeek: List<TOTWPlayerUiModel> = emptyList(),
    val teamOfTheWeekRound: Int = 1,
    val maxRounds: Int = 30,
    val fixtures: List<FixtureUiModel> = emptyList(),
    val leagueStats: LeagueStatsUiModel = LeagueStatsUiModel(),
    val userTeamId: Int? = null,
    val leagueName: String = "",
    val leagueLogo: String? = null,
    val availableLeagues: List<LeaguesEntity> = emptyList(),
    val totwFormation: String = "4-3-3"
)

// ============ ViewModel ============

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
        // Auto‑refresh when game advances
        viewModelScope.launch {
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
            viewModelScope.launch {
                val allLeagues = leaguesRepository.getAllLeagues().firstOrNull() ?: emptyList()
                val currentLeague = allLeagues.find { it.name.equals(leagueName, ignoreCase = true) }
                
                _uiState.update { it.copy(
                    leagueName = currentLeague?.name ?: leagueName,
                    leagueLogo = currentLeague?.logo,
                    availableLeagues = allLeagues
                ) }
                refreshLeagueData(currentLeague?.name ?: leagueName, currentState.context)
            }
        }
    }

    private suspend fun refreshLeagueData(leagueName: String, context: GameManager.GameContext) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val season = context.season
            val seasonYear = try { season.split("/").first().toInt() } catch (e: Exception) { 2025 }

            // 1. Standings
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

            // 2. League Stats
            val repoStats = fixturesRepository.getLeagueStatistics(leagueName, context.season)
            val leagueStats = LeagueStatsUiModel(
                totalMatches = repoStats.totalMatches,
                totalGoals = repoStats.totalGoals,
                avgGoals = repoStats.averageGoalsPerGame,
                homeWinPct = repoStats.homeWinPercentage,
                awayWinPct = repoStats.awayWinPercentage,
                drawPct = repoStats.drawPercentage
            )

            // 3. Fixtures
            val fixtures = fixturesRepository.getLeagueFixtures(leagueName, season).firstOrNull() ?: emptyList()
            val fixtureModels = fixtures.map { f ->
                FixtureUiModel(f.id, f.homeTeam, f.awayTeam, f.homeScore, f.awayScore, f.matchDate, f.position, f.isCompleted)
            }

            val currentRound = fixturesRepository.getCurrentGameWeek(leagueName, season)
            val maxRounds = fixtures.maxOfOrNull { it.position } ?: 30
            val totwRound = if (currentRound > 1) currentRound - 1 else 1

            _uiState.update {
                it.copy(
                    isLoading = false,
                    season = season,
                    standings = standingModels,
                    leagueStats = leagueStats,
                    fixtures = fixtureModels,
                    maxRounds = maxRounds,
                    teamOfTheWeekRound = totwRound,
                    userTeamId = context.teamId
                )
            }

            loadPlayerStats(leagueName)
            loadTOTW(leagueName, totwRound, season)

        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadPlayerStats(leagueName: String) {
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

        _uiState.update { it.copy(topScorers = topScorers, topAssisters = topAssisters) }
    }

    /**
     * Advanced TOTW Generation using Match Events and Tactical Formation
     * Ensures position‑appropriate selections (GK can't be ST) and uses the
     * best possible formation based on available players.
     */
    suspend fun loadTOTW(leagueName: String, round: Int, season: String) {
        _uiState.update { it.copy(teamOfTheWeekRound = round) }

        // Get all matches from this round
        val fixtures = fixturesRepository.getLeagueFixtures(leagueName, season).firstOrNull() ?: emptyList()
        val roundFixtures = fixtures.filter { it.position == round && it.isCompleted }

        if (roundFixtures.isEmpty()) {
            _uiState.update { it.copy(teamOfTheWeek = emptyList()) }
            return
        }

        // Gather player performances from match events
        val performances = mutableMapOf<Int, PlayerWeeklyPerformance>()

        for (fixture in roundFixtures) {
            val events = matchEventsRepository.getEventsByMatch(fixture.id).firstOrNull() ?: continue
            val results = fixturesResultsRepository.getResultByFixtureId(fixture.id)

            // Process each event to build performance data
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

            // Check clean sheets
            results?.let { res ->
                if (res.homeScore == 0) {
                    performances.values.find { it.teamName == res.homeTeam && it.position == "GK" }?.cleanSheet = true
                }
                if (res.awayScore == 0) {
                    performances.values.find { it.teamName == res.awayTeam && it.position == "GK" }?.cleanSheet = true
                }

                // MOTM (highest rated in match)
                performances.values
                    .filter { it.teamName == res.homeTeam || it.teamName == res.awayTeam }
                    .maxByOrNull { it.rating }
                    ?.motm = true
            }
        }

        if (performances.isEmpty()) {
            _uiState.update { it.copy(teamOfTheWeek = emptyList()) }
            return
        }

        // Normalise ratings (0-10 scale)
        performances.values.forEach { perf ->
            perf.rating = perf.rating.coerceIn(0.0, 10.0)
        }

        // Group by position category
        val goalkeepers = performances.values.filter { it.position == "GK" }.sortedByDescending { it.rating }
        val defenders = performances.values.filter { it.positionCategory == "DEFENDER" }.sortedByDescending { it.rating }
        val midfielders = performances.values.filter { it.positionCategory == "MIDFIELDER" }.sortedByDescending { it.rating }
        val forwards = performances.values.filter { it.positionCategory == "FORWARD" }.sortedByDescending { it.rating }

        // Determine best formation based on available talent
        val bestFormation = determineBestFormation(defenders, midfielders, forwards)
        _uiState.update { it.copy(totwFormation = bestFormation) }

        // Select players based on formation requirements
        val selectedPlayers = selectPlayersForFormation(bestFormation, goalkeepers, defenders, midfielders, forwards)

        // Convert to UI model
        val totwModels = selectedPlayers.map { player ->
            TOTWPlayerUiModel(
                playerId = player.playerId,
                playerName = player.playerName,
                teamName = player.teamName,
                position = player.position,
                matchRating = player.rating,
                nationality = null, // Would need to fetch from player
                shirtNumber = 0,
                goals = player.goals,
                assists = player.assists,
                cleanSheet = player.cleanSheet,
                motm = player.motm
            )
        }

        _uiState.update { it.copy(teamOfTheWeek = totwModels) }
    }

    /**
     * Determine the best formation based on available player strengths
     */
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
            else -> "4-4-2" // Default
        }
    }

    /**
     * Select players for a specific formation, ensuring position correctness
     */
    private fun selectPlayersForFormation(
        formation: String,
        goalkeepers: List<PlayerWeeklyPerformance>,
        defenders: List<PlayerWeeklyPerformance>,
        midfielders: List<PlayerWeeklyPerformance>,
        forwards: List<PlayerWeeklyPerformance>
    ): List<PlayerWeeklyPerformance> {
        val selected = mutableListOf<PlayerWeeklyPerformance>()
        val usedPlayers = mutableSetOf<Int>()

        // Formation requirements (position counts)
        val requirements = when (formation) {
            "4-3-3" -> mapOf("GK" to 1, "DEF" to 4, "MID" to 3, "FWD" to 3)
            "4-4-2" -> mapOf("GK" to 1, "DEF" to 4, "MID" to 4, "FWD" to 2)
            "4-5-1" -> mapOf("GK" to 1, "DEF" to 4, "MID" to 5, "FWD" to 1)
            "5-3-2" -> mapOf("GK" to 1, "DEF" to 5, "MID" to 3, "FWD" to 2)
            "3-5-2" -> mapOf("GK" to 1, "DEF" to 3, "MID" to 5, "FWD" to 2)
            else -> mapOf("GK" to 1, "DEF" to 4, "MID" to 4, "FWD" to 2)
        }

        // Select GK (1)
        goalkeepers.take(1).forEach { gk ->
            selected.add(gk)
            usedPlayers.add(gk.playerId)
        }

        // Select DEF (based on requirements)
        defenders
            .filter { it.playerId !in usedPlayers }
            .take(requirements["DEF"] ?: 4)
            .forEach { def ->
                selected.add(def)
                usedPlayers.add(def.playerId)
            }

        // If we need more defenders, take from midfielders (defensive mids)
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

        // Select MID (based on requirements)
        midfielders
            .filter { it.playerId !in usedPlayers }
            .take(requirements["MID"] ?: 4)
            .forEach { mid ->
                selected.add(mid)
                usedPlayers.add(mid.playerId)
            }

        // Select FWD (based on requirements)
        forwards
            .filter { it.playerId !in usedPlayers }
            .take(requirements["FWD"] ?: 2)
            .forEach { fwd ->
                selected.add(fwd)
                usedPlayers.add(fwd.playerId)
            }

        return selected
    }

    // Extension property to get position category
    private val PlayerWeeklyPerformance.positionCategory: String
        get() = when (position) {
            "GK" -> "GOALKEEPER"
            "CB", "LB", "RB", "LWB", "RWB" -> "DEFENDER"
            "CDM", "CM", "CAM", "LM", "RM" -> "MIDFIELDER"
            "LW", "RW", "ST", "CF" -> "FORWARD"
            else -> "MIDFIELDER"
        }
}
