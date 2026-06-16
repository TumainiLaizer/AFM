package com.fameafrica.afm.domain.manager

import androidx.room.withTransaction
import android.util.Log
import com.fameafrica.afm.data.database.CareerDatabaseProvider
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameManager @Inject constructor(
    private val databaseProvider: CareerDatabaseProvider,
    private val gameStateRepository: GameStatesRepository,
    private val fixturesRepository: FixturesRepository,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val financesRepository: FinancesRepository,
    private val newsRepository: NewsRepository,
    private val seasonCalendarRepository: SeasonCalendarRepository,
    private val transferWindowsRepository: TransferWindowsRepository,
    private val cupsRepository: CupsRepository,
    private val cupBracketsRepository: CupBracketsRepository,
    private val cupGroupStandingsRepository: CupGroupStandingsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val communityShieldRepository: CommunityShieldRepository,
    private val objectivesRepository: ObjectivesRepository,
    private val gameSettingsRepository: GameSettingsRepository,
    private val seasonHistoryRepository: SeasonHistoryRepository,
    private val seasonAwardsRepository: SeasonAwardsRepository,
    private val transfersRepository: TransfersRepository,
    private val gameDateManager: GameDateManager,
    private val worldSimulationEngine: WorldSimulationEngine,
    private val managersRepository: ManagersRepository,
    private val playerGenerator: PlayerGenerator,
    private val newsImpactManager: NewsImpactManager,
    private val trainingSchedulerEngine: TrainingSchedulerEngine,
    private val economyManager: EconomyManager,
    private val leagueSimulator: LeagueSimulator,
    private val seasonPreviewNewsGenerator: SeasonPreviewNewsGenerator,
    private val inboxActionEngine: com.fameafrica.afm.domain.manager.inbox.InboxActionEngine
) {

    enum class CareerMode {
        MANAGER, CHAIRMAN
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val gameState: StateFlow<GameState> = _gameState

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _processingStatus = MutableStateFlow("")
    val processingStatus: StateFlow<String> = _processingStatus

    private val _currentSquad = MutableStateFlow<List<PlayersEntity>>(emptyList())
    val currentSquad: StateFlow<List<PlayersEntity>> = _currentSquad

    private val _currentTeam = MutableStateFlow<TeamsEntity?>(null)
    val currentTeam: StateFlow<TeamsEntity?> = _currentTeam

    private val _currentManager = MutableStateFlow<ManagersEntity?>(null)
    val currentManager: StateFlow<ManagersEntity?> = _currentManager

    private val _currentFinances = MutableStateFlow<FinancesEntity?>(null)
    val currentFinances: StateFlow<FinancesEntity?> = _currentFinances

    private val _dashboardStandings = MutableStateFlow<List<LeagueStandingsEntity>>(emptyList())
    val dashboardStandings: StateFlow<List<LeagueStandingsEntity>> = _dashboardStandings

    private val _nextMatch = MutableStateFlow<FixturesEntity?>(null)
    val nextMatch: StateFlow<FixturesEntity?> = _nextMatch

    private val _recentResults = MutableStateFlow<List<FixturesResultsEntity>>(emptyList())
    val recentResults: StateFlow<List<FixturesResultsEntity>> = _recentResults

    private val _latestNews = MutableStateFlow<List<NewsEntity>>(emptyList())
    val latestNews: StateFlow<List<NewsEntity>> = _latestNews

    private val _activeTransfers = MutableStateFlow<List<TransfersEntity>>(emptyList())
    val activeTransfers: StateFlow<List<TransfersEntity>> = _activeTransfers

    // --- NEW STABILITY COMPONENTS ---
    sealed class InitializationState {
        object Idle : InitializationState()
        data class Loading(val phase: LoadingPhase, val progress: Float) : InitializationState()
        data class Ready(val careerId: Int) : InitializationState()
        data class Failed(val error: String, val recoverable: Boolean) : InitializationState()
    }

    enum class LoadingPhase {
        CONNECTING, OPTIMIZING, LOADING_WORLD, GENERATING_PLAYERS, GENERATING_FIXTURES, SYNCING_DATA, READY
    }

    sealed class GameEvent {
        object WeekAdvanced : GameEvent()
        object DaySimulated : GameEvent()
        data class MatchFinished(val matchId: Int) : GameEvent()
        data class NewsPublished(val newsId: Int) : GameEvent()
    }

    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.Idle)
    val initializationState: StateFlow<InitializationState> = _initializationState.asStateFlow()

    private val _gameEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 32, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val gameEvents: SharedFlow<GameEvent> = _gameEvents.asSharedFlow()

    private val _dailyEvents = MutableStateFlow<List<SimulationEvent>>(emptyList())
    val dailyEvents: StateFlow<List<SimulationEvent>> = _dailyEvents.asStateFlow()

    @Volatile private var stopSimulation = false
    @Volatile private var isInitialized = false

    private val gameScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * UNIFIED CAREER STATE (Phase 7 Architecture)
     * Single source of truth for the entire UI.
     */
    val careerState: StateFlow<com.fameafrica.afm.domain.model.CareerState> = combine(
        listOf(_currentSquad, _currentTeam, _currentManager, _currentFinances, _dashboardStandings, _nextMatch, _recentResults, _latestNews, _gameState, _dailyEvents, _activeTransfers)
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val squad = array[0] as List<PlayersEntity>
        val team = array[1] as TeamsEntity?
        val manager = array[2] as ManagersEntity?
        val finances = array[3] as FinancesEntity?
        @Suppress("UNCHECKED_CAST")
        val standings = array[4] as List<LeagueStandingsEntity>
        val nextMatch = array[5] as FixturesEntity?
        @Suppress("UNCHECKED_CAST")
        val results = array[6] as List<FixturesResultsEntity>
        @Suppress("UNCHECKED_CAST")
        val news = array[7] as List<NewsEntity>
        val gameState = array[8] as GameState
        @Suppress("UNCHECKED_CAST")
        val dailyEvents = array[9] as List<SimulationEvent>
        @Suppress("UNCHECKED_CAST")
        val transfers = array[10] as List<TransfersEntity>

        if (gameState is GameState.Active) {
            val context = gameState.context
            com.fameafrica.afm.domain.model.CareerState(
                world = com.fameafrica.afm.domain.model.WorldState(
                    newsHeadlines = news,
                    dailyEvents = dailyEvents
                ),
                club = com.fameafrica.afm.domain.model.ClubState(
                    teamId = context.teamId,
                    teamName = context.teamName,
                    squad = squad,
                    nextMatch = nextMatch,
                    recentResults = results,
                    boardConfidence = team?.boardConfidence ?: 100,
                    fanSentiment = team?.fanSentiment ?: 100
                ),
                manager = com.fameafrica.afm.domain.model.ManagerState(
                    id = context.managerId,
                    name = context.managerName,
                    nationality = manager?.nationality ?: "Tanzania",
                    reputation = manager?.reputation ?: 0,
                    reputationLevel = manager?.reputationLevel ?: "Local",
                    jobSecurity = manager?.jobSecurity ?: 100,
                    managerXp = manager?.managerXp ?: 0,
                    managerLevel = manager?.managerLevel ?: 1,
                    performanceRating = manager?.performanceRating ?: 0,
                    pressureLevel = manager?.pressure ?: 0
                ),
                competition = com.fameafrica.afm.domain.model.CompetitionState(
                    leagueName = context.leagueName,
                    leagueStandings = standings,
                    domesticCupName = context.domesticCupName
                ),
                finance = com.fameafrica.afm.domain.model.FinanceState(
                    balance = finances?.bankBalance ?: 0,
                    coins = finances?.coins ?: 0,
                    premiumCurrency = finances?.premiumCash ?: 0,
                    wageBudget = finances?.budget ?: 0,
                    transferBudget = finances?.transferBudget ?: 0,
                    monthlyProfit = finances?.profitLoss ?: 0,
                    seasonRevenue = finances?.revenue ?: 0
                ),
                transfer = com.fameafrica.afm.domain.model.TransferState(
                    isWindowOpen = context.isTransferWindowOpen,
                    activeBids = transfers
                ),
                timeline = com.fameafrica.afm.domain.model.TimelineState(
                    currentWeek = context.week,
                    currentSeason = context.season,
                    gameDateDisplay = context.gameDateDisplay,
                    isPreseason = context.isPreseason
                )
            )
        } else {
            com.fameafrica.afm.domain.model.CareerState.empty()
        }
    }.stateIn(gameScope, SharingStarted.Eagerly, com.fameafrica.afm.domain.model.CareerState.empty())

    private var observationJob: Job? = null
    private var initializationJob: Job? = null
    private val initMutex = Mutex()
    private val turnProcessingMutex = Mutex()
    @Volatile private var activeCareerId: Int = -1

    data class GameContext(
        val careerId: Int,
        val managerId: Int,
        val managerName: String,
        val managerAvatar: String?,
        val teamId: Int,
        val teamName: String,
        val season: String,
        val week: Int,
        val gameDateDisplay: String,
        val currentDate: String,
        val isPreseason: Boolean,
        val isTransferWindowOpen: Boolean,
        val leagueName: String?,
        val domesticCupName: String?,
        val saveName: String,
        val difficulty: String,
        val careerMode: CareerMode = CareerMode.MANAGER
    )

    sealed class GameState {
        object Loading : GameState()
        data class Active(val context: GameContext) : GameState()
        object NoSave : GameState()
        data class Error(val message: String) : GameState()
    }

    /**
     * 🔥 AAA-Stable Lifecycle-Safe initialization.
     * Uses job deduplication and state machine to ensure correct loading.
     */
    suspend fun ensureInitialized(careerId: Int) {
        initializationJob?.join() // Wait OUTSIDE mutex to avoid deadlock

        initMutex.withLock {
            val currentState = _initializationState.value
            if (currentState is InitializationState.Ready && currentState.careerId == careerId && activeCareerId == careerId) {
                if (databaseProvider.getCurrentCareerId() == careerId) {
                    Log.d("AFM_CORE", "GameManager already ready for career $careerId")
                    return
                }
            }

            // Cancel any stale initialization
            initializationJob?.cancel()
            
            initializationJob = gameScope.launch {
                try {
                    withTimeout(60000) {
                        initializeGameInternal(careerId)
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e("AFM_CORE", "Initialization TIMEOUT for career $careerId")
                    _initializationState.value = InitializationState.Failed("Initialization timed out. Please retry.", true)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("AFM_CORE", "Initialization FATAL ERROR for career $careerId", e)
                    _initializationState.value = InitializationState.Failed(e.message ?: "Unknown error", true)
                }
            }
        }
        initializationJob?.join()
    }

    suspend fun initializeGame(careerId: Int) {
        ensureInitialized(careerId)
    }

    private suspend fun initializeGameInternal(careerId: Int) {
        val startTime = System.currentTimeMillis()
        Log.d("AFM_CORE", "🚀 Starting initialization for career $careerId")
        
        _initializationState.value = InitializationState.Loading(LoadingPhase.CONNECTING, 0.1f)
        _isProcessing.value = true
        _processingStatus.value = "Connecting to Universe..."

        // Reset state flows to prevent data bleed
        resetStateFlows()

        try {
            // Verify DB context
            val dbStartTime = System.currentTimeMillis()
            if (databaseProvider.getCurrentCareerId() != careerId && careerId != -1) {
                databaseProvider.switchToCareerDb(careerId)
            }
            Log.d("AFM_CORE", "DB switch took ${System.currentTimeMillis() - dbStartTime}ms")

            _initializationState.value = InitializationState.Loading(LoadingPhase.OPTIMIZING, 0.3f)
            _processingStatus.value = "Optimizing World..."

            val state = if (careerId != -1) {
                // In a specific career DB, we just want the latest save record (there's usually only one)
                // We use getAllSaveGames instead of getValidSaveGames because setup might not be 100% complete yet
                gameStateRepository.getAllSaveGames().firstOrNull()?.firstOrNull()
            } else {
                gameStateRepository.getValidSaveGames().firstOrNull()?.maxByOrNull { it.lastPlayed ?: "" }
            }

            if (state == null) {
                Log.e("AFM_CORE", "Initialization failed: No valid save found for career $careerId")
                _initializationState.value = InitializationState.Failed("No save found.", false)
                _gameState.value = GameState.NoSave
                return
            }

            withContext(Dispatchers.IO) {
                // Health Check: Verify database integrity
                val healthStartTime = System.currentTimeMillis()
                val teamCount = teamsRepository.getTotalTeamCount()
                if (teamCount == 0) {
                    Log.e("AFM_CORE", "Database health check FAILED: 0 teams found!")
                    // Attempt restore
                    if (databaseProvider.restoreFromBackup(careerId)) {
                        initializeGameInternal(careerId) // Recursive retry after restore
                        return@withContext
                    }
                    throw IllegalStateException("Career database is empty or corrupted (0 teams).")
                }
                Log.d("AFM_CORE", "Health check took ${System.currentTimeMillis() - healthStartTime}ms (Found $teamCount teams)")

                _initializationState.value = InitializationState.Loading(LoadingPhase.LOADING_WORLD, 0.6f)
                
                val season = gameDateManager.getSeasonString(state.week)
                val team = teamsRepository.getTeamById(state.teamId)
                val manager = managersRepository.getManagerById(state.managerId)
                val league = team?.league?.let { leaguesRepository.getLeagueByName(it) }
                val domesticCup = league?.countryId?.let { cid -> cupsRepository.getDomesticCupsByCountry(cid).firstOrNull()?.firstOrNull()?.name }

                val context = GameContext(
                    careerId = state.id, managerId = state.managerId, teamId = state.teamId, teamName = state.teamName,
                    managerName = state.managerName, managerAvatar = manager?.faceImage, season = season, week = state.week,
                    gameDateDisplay = gameDateManager.formatGameDate(state.week), currentDate = gameDateManager.formatGameDateForDb(state.week),
                    isPreseason = seasonCalendarRepository.getSeasonPhase(state.week) == SeasonCalendarRepository.SeasonPhase.PRESEASON,
                    isTransferWindowOpen = seasonCalendarRepository.isTransferWindowOpen(state.week),
                    leagueName = team?.league, domesticCupName = domesticCup, saveName = state.name,
                    difficulty = gameSettingsRepository.getDifficulty(),
                    careerMode = state.careerMode
                )

                startDataObservation(state.teamId, season, state.week)
                
                // Phase 10 Optimization: Move player generation to a background task that doesn't block the init flow
                gameScope.launch(Dispatchers.Default) {
                    playerGenerator.generateMissingPlayers()
                }

                ensureSeasonInitialized(context) { progress, message ->
                    _initializationState.value = InitializationState.Loading(LoadingPhase.GENERATING_FIXTURES, 0.9f + (progress * 0.09f))
                    _processingStatus.value = message
                }

                // Force world state update to populate initial rankings (fix for empty World screen)
                worldSimulationEngine.updateWorldState(context.week, context.season)
                
                activeCareerId = careerId
                isInitialized = true
                _gameState.value = GameState.Active(context)
                _initializationState.value = InitializationState.Ready(careerId)
            }
            
            Log.d("AFM_CORE", "✅ GameManager READY in ${System.currentTimeMillis() - startTime}ms")

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("AFM_CORE", "Failed to initialize game internals", e)
            _initializationState.value = InitializationState.Failed(e.message ?: "Sync error", true)
            throw e
        } finally {
            _isProcessing.value = false
            _processingStatus.value = ""
        }
    }

    private fun startDataObservation(teamId: Int, season: String, week: Int) {
        observationJob?.cancel()
        observationJob = gameScope.launch {
            val dbDate = gameDateManager.formatGameDateForDb(week)
            launch { 
                playersRepository.getPlayersByTeamId(teamId)
                    .distinctUntilChanged()
                    .collect { _currentSquad.value = it } 
            }
            launch {
                while(isActive) {
                    try {
                        val team = teamsRepository.getTeamById(teamId)
                        if (team != _currentTeam.value) _currentTeam.value = team
                        
                        val manager = managersRepository.getManagerByTeamId(teamId)
                        if (manager != _currentManager.value) _currentManager.value = manager
                        
                        yield()
                        delay(5000) // Poll for static/slow changes
                    } catch (e: Exception) {
                        Log.e("AFM_CORE", "Error during observation polling", e)
                    }
                }
            }
            launch {
                teamsRepository.getTeamById(teamId)?.let { team ->
                    _nextMatch.value = fixturesRepository.getNextMatchForTeam(team.id, dbDate)
                    team.league?.let { lName ->
                        val seasonYear = try { season.split("/").first().toInt() } catch(e: Exception) { 2025 }
                        leagueStandingsRepository.getStandingsByPosition(lName, seasonYear)
                            .distinctUntilChanged()
                            .collect { _dashboardStandings.value = it }
                    }
                }
            }
            launch { 
                financesRepository.getTeamFinancesFlow(teamId, season)
                    .distinctUntilChanged()
                    .collect { _currentFinances.value = it } 
            }
            launch { 
                newsRepository.getTopNews(15)
                    .distinctUntilChanged()
                    .collect { _latestNews.value = it } 
            }
            launch { 
                transfersRepository.getAllTransfersByTeam(teamId)
                    .distinctUntilChanged()
                    .collect { _activeTransfers.value = it } 
            }
            launch {
                newsRepository.newsEvents.collect { news ->
                    newsImpactManager.applyNewsImpact(news)
                }
            }
        }
    }

    fun processNextTurn() {
        if (_isProcessing.value) return
        gameScope.launch {
            turnProcessingMutex.withLock {
                _isProcessing.value = true
                stopSimulation = false
                
                try {
                    val currentState = _gameState.value
                    if (currentState is GameState.Active) {
                        val context = currentState.context
                        
                        _processingStatus.value = "Advancing to Next Event..."
                        
                        val result = worldSimulationEngine.advanceToNextPlayableEvent(
                            context.teamId,
                            context.managerId,
                            context.week,
                            context.season
                        )
                        
                        val newDay = result.first
                        val events = result.second
                        
                        _dailyEvents.value = events
                        
                        // Emit DaySimulated for each day advanced if needed, or just once for the turn
                        _gameEvents.emit(GameEvent.DaySimulated)

                        // Update time to the new day
                        advanceTime(context, newDay)
                        
                        // Check for mandatory events and pause if necessary
                        if (events.any { it.shouldStop }) {
                            events.filter { it.shouldStop }.forEach { event ->
                                inboxActionEngine.processEvent(event)
                            }
                            stopSimulation = true
                        }
                        
                        saveGame(context.careerId)
                        databaseProvider.backupDatabase(context.careerId)
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("AFM_CORE", "SIMULATION ERROR: ${e.message}")
                } finally {
                    _isProcessing.value = false
                    _processingStatus.value = ""
                }
            }
        }
    }

    fun stopSimulation() {
        stopSimulation = true
    }

    fun simulateDay() {
        gameScope.launch {
            val currentState = _gameState.value
            if (currentState !is GameState.Active) return@launch
            val context = currentState.context
            
            worldSimulationEngine.simulateDay(context.teamId, context.managerId)
            trainingSchedulerEngine.simulateTrainingDay(context.currentDate)
            _gameEvents.emit(GameEvent.DaySimulated)
        }
    }

    private fun checkMemoryPressure() {
        val runtime = Runtime.getRuntime()
        val free = runtime.freeMemory()
        val total = runtime.totalMemory()
        val max = runtime.maxMemory()
        val used = total - free
        
        // Log memory status for monitoring
        Log.d("AFM_CORE", "Memory: Used=${used/1024/1024}MB, Max=${max/1024/1024}MB, Free=${free/1024/1024}MB")

        // Aggressive memory management for 1GB RAM devices
        if (free < 100 * 1024 * 1024) { // 100MB threshold for lower-end devices
            Log.w("AFM_CORE", "Memory pressure high (${used/1024/1024}MB), triggering aggressive GC")
            System.gc()
            System.runFinalization()
        }
    }

    private suspend fun advanceTime(context: GameContext, nextWeek: Int) {
        val newSeason = gameDateManager.getSeasonString(nextWeek)
        if (context.season != newSeason) processEndOfSeason(context.season, newSeason, context)

        val currentDateModel = gameDateManager.getGameDateModel(context.week)
        val nextDateModel = gameDateManager.getGameDateModel(nextWeek)

        if (currentDateModel.month != nextDateModel.month) {
            _processingStatus.value = "Finalizing Monthly Accounts..."
            val allTeams = teamsRepository.getAllTeamsSync()
            allTeams.forEach { team ->
                trainingSchedulerEngine.generateMonthlySchedule(team.id, nextDateModel.month, nextDateModel.year)
                // New: Monthly economy processing
                economyManager.processEndOfMonth(team.id, context.season)
            }
        }

        gameStateRepository.saveGame(context.careerId, nextWeek)
        
        val team = teamsRepository.getTeamById(context.teamId)
        val newContext = context.copy(
            week = nextWeek, season = newSeason, gameDateDisplay = gameDateManager.formatGameDate(nextWeek),
            currentDate = gameDateManager.formatGameDateForDb(nextWeek),
            isPreseason = seasonCalendarRepository.getSeasonPhase(nextWeek) == SeasonCalendarRepository.SeasonPhase.PRESEASON,
            isTransferWindowOpen = seasonCalendarRepository.isTransferWindowOpen(nextWeek), leagueName = team?.league
        )
        _gameState.value = GameState.Active(newContext)
        startDataObservation(context.teamId, newSeason, nextWeek)
    }

    private suspend fun ensureSeasonInitialized(context: GameContext, onProgress: (Float, String) -> Unit = { _, _ -> }) {
        val count = fixturesRepository.getFixturesBySeason(context.season).firstOrNull()?.size ?: 0
        if (count == 0) initializeNewSeason(context.season, "2024/25", context, onProgress)
    }

    private suspend fun initializeNewSeason(
        newSeason: String, 
        oldSeason: String, 
        context: GameContext,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ) {
        val year = newSeason.split("/").first().toInt()
        
        databaseProvider.getActiveDatabase().withTransaction {
            financesRepository.initializeSeasonFinances(newSeason)
            transferWindowsRepository.initializeSeasonWindows(newSeason)

            val leagues = leaguesRepository.getAllLeagues().firstOrNull() ?: emptyList()
            val totalLeagues = leagues.size
            
            leagues.chunked(5).forEach { chunk ->
                chunk.forEach { league ->
                    val teams = teamsRepository.getTeamsByLeague(league.name).firstOrNull() ?: emptyList()
                    if (teams.isNotEmpty()) {
                        fixturesRepository.generateBalancedLeagueFixtures(league.name, newSeason, teams, "${year}-08-15 15:00")
                        leagueStandingsRepository.initializeLeagueStandings(league.name, year, teams)
                    }
                    yield() // Prevent blocking
                }
            }
            
            communityShieldRepository.generateAllSeasonOpeningShields(newSeason, oldSeason)
            newsRepository.createNewsArticle("AFM26: Season $newSeason Kick-off!", "The journey begins anew.", "WORLD")
            
            // Phase 7: Season Preview Analysis (Python Research Logic)
            seasonPreviewNewsGenerator.generateSeasonPreviews(context.week)
        }
    }

    suspend fun simulateFullSeason() {
        val userTeamId = currentTeam.value?.id ?: -1
        _isProcessing.value = true
        _processingStatus.value = "Simulating Full Season..."
        
        try {
            leagueSimulator.simulateFullSeason(userTeamId)
            _gameEvents.emit(GameEvent.WeekAdvanced)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isProcessing.value = false
        }
    }

    private suspend fun processEndOfSeason(old: String, new: String, context: GameContext) {
        seasonAwardsRepository.generateEndOfSeasonAwards(old)
        financesRepository.processEndOfSeason(old, new)
        worldSimulationEngine.refreshSeasonalRankings()
        initializeNewSeason(new, old, context)
    }

    suspend fun saveGame(careerId: Int) {
        val currentState = _gameState.value
        if (currentState !is GameState.Active) return
        val context = currentState.context
        gameStateRepository.saveGame(careerId, context.week)
        Log.d("AFM_GAME", "Game state saved for career $careerId at week ${context.week}")
    }

    /**
     * Resets all internal StateFlows to their default values.
     * Prevents data bleed when switching between career databases.
     */
    private fun resetStateFlows() {
        _currentSquad.value = emptyList()
        _currentFinances.value = null
        _dashboardStandings.value = emptyList()
        _nextMatch.value = null
        _recentResults.value = emptyList()
        _latestNews.value = emptyList()
    }
}
