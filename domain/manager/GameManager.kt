package com.fameafrica.afm2026.domain.manager

import android.util.Log
import com.fameafrica.afm2026.data.database.entities.*
import com.fameafrica.afm2026.data.repository.*
import com.fameafrica.afm2026.utils.GameDateManager
import com.fameafrica.afm2026.utils.notifications.NotificationFactory
import com.fameafrica.afm2026.domain.model.enums.BoardStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Singleton
class GameManager @Inject constructor(
    private val gameStateRepository: GameStatesRepository,
    private val fixturesRepository: FixturesRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val managersRepository: ManagersRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val financesRepository: FinancesRepository,
    private val newsRepository: NewsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val boardEvaluationRepository: BoardEvaluationRepository,
    private val eloHistoryRepository: EloHistoryRepository,
    private val playerTrainingRepository: PlayerTrainingRepository,
    private val preseasonScheduleRepository: PreseasonScheduleRepository,
    private val transferWindowsRepository: TransferWindowsRepository,
    private val transfersRepository: TransfersRepository,
    private val communityShieldRepository: CommunityShieldRepository,
    private val infrastructureUpgradesRepository: InfrastructureUpgradesRepository,
    private val userAnalyticsRepository: UserAnalyticsRepository,
    private val scoutAssignmentsRepository: ScoutAssignmentsRepository,
    private val storyEventsRepository: StoryEventsRepository,
    private val gameDateManager: GameDateManager,
    private val matchSimulationEngine: MatchSimulationEngine,
    private val cupGroupStandingsRepository: CupGroupStandingsRepository,
    private val nationalTeamPlayersRepository: NationalTeamPlayersRepository,
    private val objectivesRepository: ObjectivesRepository,
    private val tacticsRepository: TacticsRepository
) {

    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val gameState: StateFlow<GameState> = _gameState

    // Caching for continuity
    private val _currentSquad = MutableStateFlow<List<PlayersEntity>>(emptyList())
    val currentSquad: StateFlow<List<PlayersEntity>> = _currentSquad

    private val _currentFinances = MutableStateFlow<FinancesEntity?>(null)
    val currentFinances: StateFlow<FinancesEntity?> = _currentFinances

    private val gameScope = CoroutineScope(Dispatchers.IO)

    data class GameContext(
        val managerId: Int,
        val teamId: Int,
        val teamName: String,
        val season: String,
        val week: Int,
        val gameDateDisplay: String,
        val isPreseason: Boolean,
        val isTransferWindowOpen: Boolean = false
    )

    sealed class GameState {
        object Loading : GameState()
        data class Active(val context: GameContext) : GameState()
        data class Processing(val message: String) : GameState()
        object NoSave : GameState()
    }

    fun initializeGame(gameStateId: Int) {
        gameScope.launch {
            val gameState = if (gameStateId != -1) {
                gameStateRepository.getGameStateById(gameStateId)
            } else {
                gameStateRepository.getValidSaveGames().firstOrNull()?.maxByOrNull { it.lastPlayed ?: "" }
            }

            if (gameState == null) {
                _gameState.value = GameState.NoSave
                return@launch
            }

            val season = gameDateManager.getSeasonString(gameState.week)
            
            // Ensure finances exist for current season
            financesRepository.initializeSeasonFinances(season)
            
            // Preload critical data
            preloadData(gameState.teamId, season)

            transferWindowsRepository.initializeSeasonWindows(season)
            val isWindowOpen = transferWindowsRepository.isTransferWindowOpen()

            val context = GameContext(
                managerId = gameState.managerId,
                teamId = gameState.teamId,
                teamName = gameState.teamName,
                season = season,
                week = gameState.week,
                gameDateDisplay = gameDateManager.formatGameDate(gameState.week),
                isPreseason = gameState.week % 52 <= 5,
                isTransferWindowOpen = isWindowOpen
            )
            
            _gameState.value = GameState.Active(context)
        }
    }

    private suspend fun preloadData(teamId: Int, season: String) {
        _currentSquad.value = playersRepository.getPlayersByTeamId(teamId).firstOrNull() ?: emptyList()
        _currentFinances.value = financesRepository.getTeamFinances(teamId, season)
    }

    fun processNextTurn() {
        gameScope.launch {
            val currentState = _gameState.value
            if (currentState !is GameState.Active) return@launch

            val context = currentState.context
            _gameState.value = GameState.Processing("Simulating world events...")

            // 1. MATCH SIMULATION
            simulateWeeklyMatches(context)

            // 2. PLAYER SYSTEMS
            processPlayerSystems(context)

            // 3. CLUB SYSTEMS
            processClubSystems(context)

            // 4. WORLD SYSTEMS
            processWorldSystems(context)

            // 5. MANAGER SYSTEMS
            processManagerSystems(context)

            // 6. ADVANCE WEEK
            val newWeek = context.week + 1
            advanceGameState(context, newWeek)
            
            // Refresh preloaded data for new week
            preloadData(context.teamId, gameDateManager.getSeasonString(newWeek))
        }
    }

    private suspend fun simulateWeeklyMatches(context: GameContext) {
        val currentWeekDateDb = gameDateManager.formatGameDateForDb(context.week)
        val allFixtures = fixturesRepository.getFixturesByDate(currentWeekDateDb).firstOrNull() ?: emptyList()
        
        allFixtures.filter { !it.isCompleted }.forEach { fixture ->
            val matchResult = matchSimulationEngine.simulateMatch(fixture)
            processMatchResult(matchResult, context)
        }
    }

    private suspend fun processPlayerSystems(context: GameContext) {
        playersRepository.getAllPlayers().firstOrNull()?.forEach { player ->
            var updatedPlayer = player

            // Fatigue Recovery (Realistic)
            val recoveredStamina = (player.stamina + 30).coerceAtMost(100)
            updatedPlayer = updatedPlayer.copy(stamina = recoveredStamina)

            // Natural Injury (Random)
            if (updatedPlayer.isAvailable && Random.nextInt(1000) < 5) {
                val recoveryDays = Random.nextInt(3, 14)
                updatedPlayer = updatedPlayer.setInjury("Training Strain", recoveryDays)
                if (player.teamId == context.teamId) {
                    notificationsRepository.insertNotification(NotificationFactory.createInjuryNotification(updatedPlayer, "Minor", recoveryDays))
                }
            }

            if (updatedPlayer != player) {
                playersRepository.updatePlayer(updatedPlayer)
            }
        }
    }

    private suspend fun processClubSystems(context: GameContext) {
        val userFinances = financesRepository.getTeamFinances(context.teamId, context.season)
        userFinances?.let {
            val weekly_wage_bill = it.wageBill / 52
            financesRepository.addWages(context.teamId, context.season, weekly_wage_bill)
        }
        infrastructureUpgradesRepository.processUpgrades()
    }

    private suspend fun processWorldSystems(context: GameContext) {
        transferWindowsRepository.updateWindowStatuses()
        storyEventsRepository.generateRandomEvent(context.teamId, context.teamName)
    }

    private suspend fun processManagerSystems(context: GameContext) {
        val gameState = gameStateRepository.getGameStateByManagerId(context.managerId) ?: return
        boardEvaluationRepository.evaluateBoardStatus(gameState.managerName)
    }

    private suspend fun advanceGameState(context: GameContext, newWeek: Int) {
        val currentSeason = context.season
        val nextSeason = gameDateManager.getSeasonString(newWeek)

        if (currentSeason != nextSeason) {
            // Initialize finances for all teams for the new season
            financesRepository.initializeSeasonFinances(nextSeason)
            // Process individual team rollover logic
            processEndOfSeason(currentSeason, nextSeason)
            // Initialize other season systems
            initializeNewSeason(nextSeason, currentSeason, context)
        }

        gameStateRepository.saveGame(
            gameStateId = gameStateRepository.getGameStateByManagerId(context.managerId)?.id ?: 0,
            week = newWeek
        )

        val isWindowOpen = transferWindowsRepository.isTransferWindowOpen()
        val newContext = context.copy(
            week = newWeek,
            season = nextSeason,
            gameDateDisplay = gameDateManager.formatGameDate(newWeek),
            isPreseason = newWeek % 52 <= 5,
            isTransferWindowOpen = isWindowOpen
        )
        _gameState.value = GameState.Active(newContext)
    }

    private suspend fun initializeNewSeason(newSeason: String, oldSeason: String, context: GameContext) {
        // 1. Generate Preseason Tour for User
        preseasonScheduleRepository.generateUserPreseasonTour(context.teamName, newSeason)
        
        // 2. Generate Community Shields
        communityShieldRepository.generateAllSeasonOpeningShields(newSeason, oldSeason)
        
        // 3. Generate Season Objectives for User
        val manager = managersRepository.getManagerById(context.managerId)
        val team = teamsRepository.getTeamById(context.teamId)
        if (manager != null && team != null) {
            objectivesRepository.generateSeasonObjectives(
                teamName = context.teamName,
                season = newSeason,
                leagueLevel = 1, // Defaulting to top level, can be refined
                clubReputation = team.reputation ?: 50
            )
        }
        
        // 4. Update National Team Squads
        nationalTeamPlayersRepository.getAllEntries().firstOrNull()?.map { it.nationalTeamId }?.distinct()?.forEach { teamId ->
            val nTeam = nationalTeamPlayersRepository.getNationalTeamStrength(teamId)
            // Logic to refresh squad if needed
        }

        // 5. Ensure all teams have tactics
        teamsRepository.getAllTeams().firstOrNull()?.forEach { t ->
            if (tacticsRepository.getTacticsByTeam(t.name) == null) {
                tacticsRepository.createDefaultTactics(t.name, t.managerId)
            }
        }
    }

    private suspend fun processEndOfSeason(oldSeason: String, newSeason: String) {
        financesRepository.processEndOfSeason(oldSeason, newSeason)
    }

    private suspend fun processMatchResult(matchResult: MatchResult, context: GameContext) {
        val fixture = matchResult.fixture
        fixturesRepository.completeFixture(fixture.id, matchResult.homeScore, matchResult.awayScore)
        
        val resultEntity = FixturesResultsEntity(
            fixtureId = fixture.id, matchDate = fixture.matchDate,
            homeTeam = fixture.homeTeam, awayTeam = fixture.awayTeam,
            homeScore = matchResult.homeScore, awayScore = matchResult.awayScore,
            matchType = fixture.matchType, season = fixture.season,
            leagueName = fixture.league, cupName = fixture.cupName, stadium = fixture.stadium
        )
        fixturesResultsRepository.insertResult(resultEntity)
        
        if (fixture.league != null) {
            leagueStandingsRepository.updateStandingsAfterMatch(resultEntity)
        }
        
        eloHistoryRepository.processMatchResult(resultEntity)

        // Sync team morale
        val hTeam = matchResult.homeTeamObj
        val aTeam = matchResult.awayTeamObj
        if (hTeam != null && aTeam != null) {
            val hMorale = if (matchResult.homeScore > matchResult.awayScore) 5 else if (matchResult.homeScore < matchResult.awayScore) -5 else 0
            teamsRepository.updateTeamMorale(hTeam.id, hMorale)
            teamsRepository.updateTeamMorale(aTeam.id, -hMorale)
        }
    }

    data class MatchResult(
        val fixture: FixturesEntity,
        val homeScore: Int,
        val awayScore: Int,
        val result: String,
        val events: List<MatchEventsEntity>,
        val homeTeamObj: TeamsEntity?,
        val awayTeamObj: TeamsEntity?,
        val homeManager: ManagersEntity?,
        val awayManager: ManagersEntity?,
        val referee: RefereesEntity?
    )
}
