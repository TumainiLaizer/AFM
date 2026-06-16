package com.fameafrica.afm.ui.screen.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.manager.MatchSimulationEngine
import com.fameafrica.afm.utils.commentary.AfricanFootballCommentaryGenerator
import com.fameafrica.afm.utils.constants.AfricanFootballDataHelper
import com.fameafrica.afm.data.database.model.match.MatchSpeed
import com.fameafrica.afm.data.database.model.match.MatchUpdate
import com.fameafrica.afm.domain.manager.DynamicPacingEngine
import com.fameafrica.afm.ui.screen.match.model.MatchVisualizerUiState
import com.fameafrica.afm.ui.screen.match.model.MomentumPoint
import com.fameafrica.afm.utils.tactics.TacticalMatchupEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

enum class MatchStatus {
    PRESS_CONFERENCE_INVITATION,
    PRE_MATCH_CONFERENCE,
    POST_MATCH_INTERVIEW,
    POST_MATCH_CONFERENCE,
    PRE_MATCH_ANALYSIS,
    LINEUP_ANNOUNCEMENT,
    TRANSITION_TO_LIVE,
    KICKOFF,
    FIRST_HALF,
    SECOND_HALF,
    HALFTIME,
    FULL_TIME,
    MATCH_SUMMARY
}

enum class MatchPeriod {
    REGULAR
}

data class MatchInfoUiModel(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamId: Int = -1,
    val awayTeamId: Int = -1,
    val homeLogo: String? = null,
    val awayLogo: String? = null,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val competition: String,
    val stadium: String,
    val kickoff: String,
    val homePosition: Int = 0,
    val awayPosition: Int = 0,
    val homeForm: String = "",
    val awayForm: String = "",
    val homeAvgGoals: Double = 0.0,
    val awayAvgGoals: Double = 0.0,
    val h2hHomeWins: Int = 0,
    val h2hAwayWins: Int = 0,
    val h2hDraws: Int = 0,
    val winProb: Float = 0.33f,
    val drawProb: Float = 0.33f,
    val lossProb: Float = 0.34f,
    val homeFormation: String = "4-4-2",
    val awayFormation: String = "4-4-2",
    val homeCoach: String = "",
    val awayCoach: String = "",
    val homeLineup: List<PlayerLineupUiModel> = emptyList(),
    val awayLineup: List<PlayerLineupUiModel> = emptyList(),
    val homeSubs: List<PlayerLineupUiModel> = emptyList(),
    val awaySubs: List<PlayerLineupUiModel> = emptyList(),
    val homeStrength: Double = 50.0,
    val awayStrength: Double = 50.0,
    val staffRecommendation: StaffRecommendation? = null,
    val matchType: String = "League",
    val weather: String = "Clear"
)

data class StaffRecommendation(
    val role: String,
    val staffName: String,
    val title: String,
    val description: String
)

data class MatchEventUiModel(
    val id: Int,
    val minute: String,
    val player: String,
    val team: String,
    val type: String,
    val icon: String,
    val detail: String,
    val colorCode: String = "INFO" 
)

data class CommentaryUiModel(
    val id: Int,
    val minute: String,
    val text: String,
    val type: String,
    val importance: Int = 1,
    val importanceLabel: String = "",
    val icon: String = "📋",
    val team: String = "",
    val colorCode: String = "INFO",
    val crowdNoise: Int = 5,
    val crowdNoiseLabel: String = "",
    val isControversial: Boolean = false
)

data class MatchStatsUiModel(
    var homePossession: Int = 50,
    var awayPossession: Int = 50,
    var homeShots: Int = 0,
    var awayShots: Int = 0,
    var homeShotsOnTarget: Int = 0,
    var awayShotsOnTarget: Int = 0,
    var homeCorners: Int = 0,
    var awayCorners: Int = 0,
    var homeFouls: Int = 0,
    var awayFouls: Int = 0,
    var homePassesAttempted: Int = 0,
    var awayPassesAttempted: Int = 0,
    var homePassesCompleted: Int = 0,
    var awayPassesCompleted: Int = 0,
    var homePassAccuracy: Int = 0,
    var awayPassAccuracy: Int = 0,
    var homeYellowCards: Int = 0,
    var awayYellowCards: Int = 0,
    var homeRedCards: Int = 0,
    var awayRedCards: Int = 0,
    var homeOffsides: Int = 0,
    var awayOffsides: Int = 0,
    var homeSaves: Int = 0,
    var awaySaves: Int = 0,
    var homeSubsUsed: Int = 0,
    var awaySubsUsed: Int = 0
)

data class PreMatchReportUiModel(
    val analystTitle: String,
    val analystAdvice: String,
    val scoutTitle: String,
    val scoutAdvice: String,
    val keyMatchup: String,
    val winProbability: Int,
    val drawProbability: Int,
    val lossProbability: Int
)

data class MatchUiState(
    val isLoading: Boolean = true,
    val matchStatus: MatchStatus = MatchStatus.PRESS_CONFERENCE_INVITATION,
    val matchInfo: MatchInfoUiModel? = null,
    val referee: RefereeUiModel? = null,
    val commentators: List<String> = emptyList(),
    val events: List<MatchEventUiModel> = emptyList(),
    val commentary: List<CommentaryUiModel> = emptyList(),
    val stats: MatchStatsUiModel = MatchStatsUiModel(),
    val homePlayers: List<PlayerLineupUiModel> = emptyList(),
    val awayPlayers: List<PlayerLineupUiModel> = emptyList(),
    val manOfTheMatch: PlayerLineupUiModel? = null,
    val possession: Float = 50f,
    val currentMinute: Int = 0,
    val matchSpeed: com.fameafrica.afm.data.database.model.match.MatchSpeed = _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.NORMAL,
    val isPaused: Boolean = false,
    val canSkip: Boolean = true,
    val otherFixtures: List<FixtureUiModel> = emptyList(),
    val leagueStandings: List<StandingUiModel> = emptyList(),
    val cupBrackets: List<CupBracketsEntity> = emptyList(),
    val currentPeriod: MatchPeriod = MatchPeriod.REGULAR,
    val currentPressConference: PressConferencesEntity? = null,
    val homeManager: ManagersEntity? = null,
    val awayManager: ManagersEntity? = null,
    val simulationParams: AfricanFootballDataHelper.SimulationParams = AfricanFootballDataHelper.SimulationParams(),
    var homeRatings: MutableMap<Int, Double> = mutableMapOf(),
    var awayRatings: MutableMap<Int, Double> = mutableMapOf(),
    val homeTactics: TacticsEntity? = null,
    val awayTactics: TacticsEntity? = null,
    val matchSummary: String = "",
    val userTeamId: Int = -1,
    val preMatchReport: PreMatchReportUiModel? = null,
    val visualEvents: List<MatchEventsEntity> = emptyList(),
    val visualizerState: MatchVisualizerUiState = MatchVisualizerUiState()
)

data class FixtureUiModel(val id: Int, val homeTeam: String, val awayTeam: String, val homeScore: Int, val awayScore: Int, val status: String, val minute: String = "")
data class StandingUiModel(val position: Int, val teamName: String, val played: Int, val won: Int = 0, val drawn: Int = 0, val lost: Int = 0, val points: Int, val goalDifference: Int)

data class PlayerLineupUiModel(
    val id: Int,
    val name: String,
    val position: String,
    val rating: Double,
    val photo: String? = null,
    val goals: Int = 0,
    val assists: Int = 0,
    val value: Long = 0,
    val shirtNumber: Int = 0,
    val matchRating: Double = 6.0
)

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val fixturesRepository: FixturesRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val matchEventsRepository: MatchEventsRepository,
    private val matchCommentaryRepository: MatchCommentaryRepository,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val managersRepository: ManagersRepository,
    private val staffRepository: StaffRepository,
    private val tacticsRepository: TacticsRepository,
    private val pressConferencesRepository: PressConferencesRepository,
    private val clubDNARepository: ClubDNARepository,
    private val matchSimulationEngine: MatchSimulationEngine,
    private val dynamicPacingEngine: DynamicPacingEngine,
    private val refereesRepository: RefereesRepository,
    private val staffIntelligenceService: com.fameafrica.afm.domain.staff.StaffIntelligenceService,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState(isLoading = true))
    val uiState: StateFlow<MatchUiState> = _uiState

    private var simulationJob: Job? = null
    private var simHome: MatchSimulationEngine.SimTeam? = null
    private var simAway: MatchSimulationEngine.SimTeam? = null
    private var currentFixture: FixturesEntity? = null

    fun loadMatch(matchId: Int) {
        if (_uiState.value.matchInfo?.id == matchId) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val fixture = fixturesRepository.getFixtureById(matchId) ?: return@launch
            currentFixture = fixture

            val competitionName = fixture.league ?: fixture.cupName
            val simParams = AfricanFootballDataHelper.getSimulationParams(competitionName)

            val homeTeam = teamsRepository.getTeamByName(fixture.homeTeam) ?: return@launch
            val awayTeam = teamsRepository.getTeamByName(fixture.awayTeam) ?: return@launch

            val homePlayers = playersRepository.getPlayersByTeamId(homeTeam.id).firstOrNull() ?: emptyList()
            val awayPlayers = playersRepository.getPlayersByTeamId(awayTeam.id).firstOrNull() ?: emptyList()

            val homeTactics = tacticsRepository.getTacticsByTeam(homeTeam.id) ?: createDefaultTactics(homeTeam.id, homeTeam.name, homeTeam.formation ?: "4-4-2", homePlayers)
            val awayTactics = tacticsRepository.getTacticsByTeam(awayTeam.id) ?: createDefaultTactics(awayTeam.id, awayTeam.name, awayTeam.formation ?: "4-4-2", awayPlayers)

            val homeManager = managersRepository.getManagerByTeam(homeTeam.id)
            val awayManager = managersRepository.getManagerByTeam(awayTeam.id)

            val homeDNA = clubDNARepository.getClubDNA(homeTeam.id)
            val awayDNA = clubDNARepository.getClubDNA(awayTeam.id)

            val userTeamId = (gameManager.gameState.value as? GameManager.GameState.Active)?.context?.teamId ?: -1

            simHome = matchSimulationEngine.prepareSimTeam(homeTeam, homeTactics, homeManager, homePlayers, homeDNA, userTeamId)
            simAway = matchSimulationEngine.prepareSimTeam(awayTeam, awayTactics, awayManager, awayPlayers, awayDNA, userTeamId)

            simHome?.let { h -> simAway?.let { a ->
                matchSimulationEngine.setupMatchDynamics(h, a, simParams)
            } }

            val probs = TacticalMatchupEngine.calculateComprehensiveProbabilities(
                homeTactics, awayTactics,
                homeTeam.eloRating, homeTeam.overallRating,
                awayTeam.eloRating, awayTeam.overallRating
            )

            val h2h = fixturesResultsRepository.getTeamRecordVsOpponent(fixture.homeTeamId, fixture.awayTeamId)

            val homeLineup = generateLineup(homePlayers.filter { it.isStartingXi })
            val awayLineup = generateLineup(awayPlayers.filter { it.isStartingXi })
            
            val homeSubs = selectSubstitutes(homePlayers.filter { !it.isStartingXi })
            val awaySubs = selectSubstitutes(awayPlayers.filter { !it.isStartingXi })

            val assistantAdvice = staffIntelligenceService.getAssistantManagerAdvice(homeTeam.id)
            val assistant = staffRepository.getAssistantManager(homeTeam.id)
            
            val staffRecommendation = assistantAdvice?.let {
                StaffRecommendation(
                    role = "Assistant Manager",
                    staffName = assistant?.name ?: "Samuel Eto'o",
                    title = "Pre-Match Briefing",
                    description = it
                )
            } ?: getStaffRecommendation()

            val refereeEntity = fixture.refereeId?.let { refereesRepository.getRefereeById(it) }
            val refereeUi = refereeEntity?.let {
                RefereeUiModel(it.refereeId, it.name, "Africa", it.strictness, it.bias, it.rating.toDouble())
            }

            AfricanFootballCommentaryGenerator.resetUsedCommentary()
            matchEventsRepository.deleteEventsByMatch(matchId)
            matchCommentaryRepository.deleteByMatch(matchId)

            val allCommentators = AfricanFootballCommentaryGenerator.getCommentatorsForCountry("Africa")
            val matchCommentators = if (allCommentators.size >= 2) allCommentators.shuffled().take(2) else allCommentators

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    matchStatus = MatchStatus.PRESS_CONFERENCE_INVITATION,
                    matchInfo = MatchInfoUiModel(
                        id = matchId, homeTeam = fixture.homeTeam, awayTeam = fixture.awayTeam,
                        homeTeamId = homeTeam.id, awayTeamId = awayTeam.id,
                        homeLogo = homeTeam.logoPath, awayLogo = awayTeam.logoPath,
                        competition = competitionName ?: "Friendly", stadium = fixture.stadium,
                        kickoff = fixture.matchDate.substringAfter(" "), homePosition = homeTeam.points,
                        awayPosition = awayTeam.points, homeForm = homeTeam.formDescription,
                        awayForm = awayTeam.formDescription, homeAvgGoals = homeTeam.avgAttackingAbility ?: 1.2,
                        awayAvgGoals = awayTeam.avgAttackingAbility ?: 1.0, h2hHomeWins = h2h.wins,
                        h2hAwayWins = h2h.losses, h2hDraws = h2h.draws, winProb = probs.first.toFloat(),
                        drawProb = probs.second.toFloat(), lossProb = probs.third.toFloat(),
                        homeFormation = homeTactics.formation, awayFormation = awayTactics.formation,
                        homeCoach = homeManager?.name ?: "Head Coach", awayCoach = awayManager?.name ?: "Opponent Coach",
                        homeLineup = homeLineup, awayLineup = awayLineup, homeSubs = homeSubs, awaySubs = awaySubs,
                        homeStrength = simHome?.strength ?: 50.0, awayStrength = simAway?.strength ?: 50.0,
                        staffRecommendation = staffRecommendation, matchType = fixture.matchType,
                        weather = fixture.weatherConditions ?: "Clear"
                    ),
                    preMatchReport = PreMatchReportUiModel(
                        analystTitle = "Tactical Forecast",
                        analystAdvice = "The opposition tends to use ${awayTactics.formation}. We should exploit the space on the wings.",
                        scoutTitle = "Chief Scout's Brief",
                        scoutAdvice = "${fixture.awayTeam} are strongest in the final third. Keep our defensive line deep.",
                        keyMatchup = "${homePlayers.maxByOrNull { it.rating }?.name} vs ${awayPlayers.maxByOrNull { it.rating }?.name}",
                        winProbability = (probs.first).toInt(),
                        drawProbability = (probs.second).toInt(),
                        lossProbability = (probs.third).toInt()
                    ),
                    referee = refereeUi,
                    commentators = matchCommentators,
                    homePlayers = homePlayers.map { it.toUiModel() },
                    awayPlayers = awayPlayers.map { it.toUiModel() },
                    homeManager = homeManager,
                    awayManager = awayManager,
                    simulationParams = simParams,
                    currentMinute = 0,
                    events = emptyList(),
                    commentary = emptyList(),
                    stats = MatchStatsUiModel(),
                    homeTactics = homeTactics,
                    awayTactics = awayTactics,
                    userTeamId = userTeamId,
                    visualEvents = emptyList()
                )
            }

            if (fixture.matchType == "League") loadLeagueStandings(fixture.league, fixture.season)
            
            loadOtherFixtures(fixture)
        }
    }

    private fun createDefaultTactics(teamId: Int, teamName: String, formation: String, players: List<PlayersEntity>): TacticsEntity {
        val starters = players.filter { it.isStartingXi }
        return TacticsEntity(
            teamId = teamId,
            teamName = teamName,
            formation = formation,
            tacticalArchetype = "BALANCED",
            playstyle = "Balanced",
            captainId = starters.maxByOrNull { it.leadership }?.id,
            penaltyTakerId = starters.maxByOrNull { it.finishing }?.id,
            freeKickTakerId = starters.maxByOrNull { it.skill }?.id,
            cornerTakerId = starters.maxByOrNull { it.crossing }?.id
        )
    }

    private fun loadLeagueStandings(league: String?, season: String?) {
        if (league == null) return
        viewModelScope.launch {
            val seasonYear = season?.split("/")?.first()?.toInt() ?: 2025
            leagueStandingsRepository.getStandingsByPosition(league, seasonYear)
                .collect { list ->
                    _uiState.update { state -> state.copy(leagueStandings = list.map { it.toUiModel() }) }
                }
        }
    }

    private fun loadOtherFixtures(current: FixturesEntity) {
        viewModelScope.launch {
            fixturesRepository.getFixturesByDate(current.matchDate)
                .collect { list ->
                    _uiState.update { state ->
                        state.copy(otherFixtures = list.asSequence().filter { it.id != current.id }.map { fixture ->
                            FixtureUiModel(fixture.id, fixture.homeTeam, fixture.awayTeam, fixture.homeScore, fixture.awayScore, fixture.matchStatus ?: "SCHEDULED")
                        }.toList())
                    }
                }
        }
    }

    fun proceedToNextState() {
        val nextStatus = when (_uiState.value.matchStatus) {
            MatchStatus.PRESS_CONFERENCE_INVITATION -> MatchStatus.PRE_MATCH_ANALYSIS
            MatchStatus.PRE_MATCH_ANALYSIS -> MatchStatus.LINEUP_ANNOUNCEMENT
            MatchStatus.LINEUP_ANNOUNCEMENT -> MatchStatus.TRANSITION_TO_LIVE
            MatchStatus.TRANSITION_TO_LIVE -> MatchStatus.KICKOFF
            MatchStatus.KICKOFF -> {
                startMatch()
                MatchStatus.FIRST_HALF
            }
            MatchStatus.FIRST_HALF -> MatchStatus.HALFTIME
            MatchStatus.HALFTIME -> {
                resumeMatch()
                MatchStatus.SECOND_HALF
            }
            MatchStatus.SECOND_HALF -> MatchStatus.FULL_TIME
            MatchStatus.FULL_TIME -> MatchStatus.MATCH_SUMMARY
            else -> _uiState.value.matchStatus
        }
        _uiState.update { it.copy(matchStatus = nextStatus) }
    }

    fun submitPressConferenceResponse(response: String) {
        viewModelScope.launch {
            val pc = _uiState.value.currentPressConference ?: return@launch
            pressConferencesRepository.submitResponse(pc.id, response)
            proceedToNextState()
        }
    }

    fun startMatch() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val fixture = currentFixture ?: return@launch
            val userTeamId = (gameManager.gameState.value as? GameManager.GameState.Active)?.context?.teamId ?: -1
            
            matchSimulationEngine.simulateMatch(fixture, userTeamId).collect { update ->
                waitIfPaused()
                
                when (update) {
                    is MatchUpdate.MinuteUpdate -> {
                        _uiState.update { state -> 
                            val newMomentum = calculateMomentum(state.stats)
                            state.copy(
                                currentMinute = update.minute,
                                visualizerState = state.visualizerState.copy(
                                    momentumPoints = state.visualizerState.momentumPoints + MomentumPoint(update.minute, newMomentum)
                                )
                            )
                        }
                    }
                    is MatchUpdate.EventUpdate -> {
                        processMatchEvent(update)
                        
                        val delayMs = dynamicPacingEngine.calculateDelay(
                            intensity = update.intensity,
                            speed = _uiState.value.matchSpeed,
                            minute = _uiState.value.currentMinute,
                            scoreDifference = abs((_uiState.value.matchInfo?.homeScore ?: 0) - (_uiState.value.matchInfo?.awayScore ?: 0)),
                            isDerby = fixture.matchType == "DERBY"
                        )
                        delay(delayMs)
                    }
                    is MatchUpdate.ScoreUpdate -> {
                        _uiState.update { state ->
                            state.copy(matchInfo = state.matchInfo?.copy(homeScore = update.homeScore, awayScore = update.awayScore))
                        }
                    }
                    is MatchUpdate.HalfTime -> {
                        _uiState.update { it.copy(matchStatus = MatchStatus.HALFTIME, isPaused = true) }
                    }
                    is MatchUpdate.FullTime -> {
                        val finalResult = matchSimulationEngine.simulateInstantResult(fixture)
                        finalizeMatch(finalResult)
                    }
                }
            }
        }
    }

    private fun calculateMomentum(stats: MatchStatsUiModel): Float {
        val homeWeight = (stats.homeShots * 0.5f) + (stats.homeShotsOnTarget * 1.5f) + (stats.homePossession * 0.05f)
        val awayWeight = (stats.awayShots * 0.5f) + (stats.awayShotsOnTarget * 1.5f) + (stats.awayPossession * 0.05f)
        val diff = homeWeight - awayWeight
        return (diff / 20f).coerceIn(-1f, 1f)
    }

    private suspend fun waitIfPaused() {
        while (_uiState.value.isPaused) {
            delay(100)
        }
    }

    private fun processMatchEvent(update: MatchUpdate.EventUpdate) {
        viewModelScope.launch {
            val event = update.event
            val savedEvent = matchEventsRepository.insertEvent(event)
            
            _uiState.update { state ->
                val updatedEvents = state.events + savedEvent.toUiModel()
                val updatedVisualEvents = state.visualEvents + savedEvent
                
                val updatedCommentary = state.commentary.toMutableList()
                matchCommentaryRepository.generateCommentaryForEvent(savedEvent)?.let {
                    updatedCommentary.add(it.toUiModel())
                }

                state.copy(
                    events = updatedEvents,
                    commentary = updatedCommentary,
                    visualEvents = updatedVisualEvents
                )
            }
        }
    }

    fun skipToResult() {
        simulationJob?.cancel()
        viewModelScope.launch {
            val fixture = currentFixture ?: return@launch
            val userTeamId = (gameManager.gameState.value as? GameManager.GameState.Active)?.context?.teamId ?: -1
            val result = matchSimulationEngine.simulateInstantResult(fixture, userTeamId)
            finalizeMatch(result)
        }
    }

    private fun finalizeMatch(result: MatchSimulationEngine.MatchResult) {
        viewModelScope.launch {
            val fixture = currentFixture ?: return@launch
            
            val updatedFixture = fixture.copy(
                homeScore = result.homeScore,
                awayScore = result.awayScore,
                matchStatus = "COMPLETED"
            )
            fixturesRepository.updateFixture(updatedFixture)
            
            val resultEntity = FixturesResultsEntity(
                fixtureId = fixture.id,
                homeTeamId = fixture.homeTeamId,
                homeTeam = fixture.homeTeam,
                awayTeamId = fixture.awayTeamId,
                awayTeam = fixture.awayTeam,
                homeScore = result.homeScore,
                awayScore = result.awayScore,
                matchDate = fixture.matchDate,
                matchType = fixture.matchType,
                season = fixture.season,
                attendance = Random.nextInt(5000, 50000),
                refereeId = fixture.refereeId,
                stadium = fixture.stadium
            )
            fixturesResultsRepository.insertResult(resultEntity)
            
            teamsRepository.updateTeamAfterMatch(resultEntity)
            
            result.playerStats.forEach { (id, stats) ->
                playersRepository.updatePlayerAfterMatch(
                    playerId = id,
                    goalsScored = stats.goals,
                    assistsMade = stats.assists,
                    isManOfMatch = id == result.manOfTheMatchId,
                    matchRating = stats.rating
                )
            }

            val motm = result.manOfTheMatchId?.let { playersRepository.getPlayerById(it) }

            _uiState.update { 
                it.copy(
                    matchStatus = MatchStatus.FULL_TIME,
                    manOfTheMatch = motm?.toUiModel(),
                    matchSummary = "Full Time: ${fixture.homeTeam} ${result.homeScore} - ${result.awayScore} ${fixture.awayTeam}"
                ) 
            }
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun resumeMatch() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun setMatchSpeed(speed: MatchSpeed) {
        _uiState.update { it.copy(matchSpeed = speed) }
    }

    private fun generateLineup(players: List<PlayersEntity>): List<PlayerLineupUiModel> {
        return players.map { it.toUiModel() }
    }

    private fun selectSubstitutes(players: List<PlayersEntity>): List<PlayerLineupUiModel> {
        return players.sortedByDescending { it.rating }.take(7).map { it.toUiModel() }
    }

    private fun getStaffRecommendation(): StaffRecommendation {
        return StaffRecommendation(
            role = "Assistant Manager",
            staffName = "Samuel Eto'o",
            title = "Tactical Insight",
            description = "They are playing a very high line. We should try to exploit the space behind their full-backs using our fast wingers."
        )
    }

    private fun MatchEventsEntity.toUiModel() = MatchEventUiModel(
        id = eventId,
        minute = "$minute'",
        player = playerName,
        team = teamName,
        type = eventType,
        icon = getEventIcon(eventType),
        detail = description ?: ""
    )

    private fun MatchCommentaryEntity.toUiModel() = CommentaryUiModel(
        id = id,
        minute = "$minute'",
        text = commentaryText,
        type = commentaryType,
        importance = importance,
        team = teamName ?: "",
        crowdNoise = crowdNoiseLevel,
        isControversial = isControversial
    )

    private fun LeagueStandingsEntity.toUiModel() = StandingUiModel(
        position = position,
        teamName = teamName,
        played = matchesPlayed,
        won = wins,
        drawn = draws,
        lost = losses,
        points = points,
        goalDifference = goalDifference
    )

    private fun PlayersEntity.toUiModel() = PlayerLineupUiModel(
        id = id,
        name = name,
        position = position,
        rating = rating.toDouble(),
        photo = null,
        goals = goals,
        assists = assists,
        value = marketValue.toLong(),
        shirtNumber = 0 // Needs proper mapping if available
    )

    private fun getEventIcon(type: String) = when(type) {
        "GOAL" -> "⚽"
        "YELLOW_CARD" -> "🟨"
        "RED_CARD" -> "🟥"
        "SUBSTITUTION" -> "🔄"
        else -> "🔔"
    }
}

data class RefereeUiModel(
    val id: Int,
    val name: String,
    val country: String,
    val strictness: Int,
    val bias: Int,
    val rating: Double
)
