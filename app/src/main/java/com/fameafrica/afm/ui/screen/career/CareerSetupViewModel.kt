package com.fameafrica.afm.ui.screen.career

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.data.database.entities.PlayerAgentsEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.data.initializer.GameInitializer
import com.fameafrica.afm.data.repository.LeaguesRepository
import com.fameafrica.afm.data.repository.ManagersRepository
import com.fameafrica.afm.data.repository.PlayerAgentsRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.domain.manager.CareerManager
import com.fameafrica.afm.data.repository.ClubDNARepository
import com.fameafrica.afm.data.repository.ChairmanRepository
import com.fameafrica.afm.utils.NationalityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import javax.inject.Inject

sealed class CareerEvent {
    data class NavigateToDashboard(val careerId: Int) : CareerEvent()
    data class ShowError(val message: String) : CareerEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CareerSetupViewModel @Inject constructor(
    private val leaguesRepository: LeaguesRepository,
    private val teamsRepository: TeamsRepository,
    private val managersRepository: ManagersRepository,
    private val playerAgentsRepository: PlayerAgentsRepository,
    private val clubDNARepository: ClubDNARepository,
    private val chairmanRepository: ChairmanRepository,
    private val playerGenerator: com.fameafrica.afm.domain.manager.PlayerGenerator,
    private val staffGenerator: com.fameafrica.afm.domain.manager.StaffGenerator,
    private val careerManager: CareerManager,
    private val gameInitializer: GameInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(CareerUiState())
    val uiState: StateFlow<CareerUiState> = _uiState.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)

    private val _events = Channel<CareerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Cache for leagues to avoid repeated API calls
    private var cachedLeagues: List<LeaguesEntity>? = null
    private var countryObservationJob: Job? = null
    
    val leagueTeamCounts: Flow<Map<String, Int>> = refreshTrigger
        .flatMapLatest { leaguesRepository.getAllLeagues() }
        .flatMapLatest { leagues ->
            if (leagues.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val countFlows = leagues.map { league ->
                    teamsRepository.getTeamsByLeague(league.name.trim())
                        .map { teams -> league.name to teams.size }
                        .catch { emit(league.name to 0) }
                }
                combine(countFlows) { pairs -> pairs.toMap() }
            }
        }

    init {
        initializeAfricanFootballUniverse()
    }

    private fun initializeAfricanFootballUniverse() {
        viewModelScope.launch {
            updateInitialization(0.05f, "Waking up the African Football Universe...")

            try {
                // Switch to static database for browsing
                updateInitialization(0.1f, "Connecting to the African Football Network...")
                withContext(Dispatchers.IO) {
                    careerManager.switchToStaticUniverse()
                }
                
                // 🔥 Trigger a refresh of all flows now that the database has switched
                refreshTrigger.value += 1
                
                // Clear cache so it reloads from the new database context
                cachedLeagues = null

                // Ensure all teams have players before browsing
                updateInitialization(0.15f, "Populating African Talent Pools...")
                withContext(Dispatchers.IO) {
                    playerGenerator.generateMissingPlayers { progress ->
                        updateInitialization(0.15f + (progress * 0.2f), "Scouting Prospects: ${(progress * 100).toInt()}%")
                    }
                }

                // Ensure all teams have staff
                updateInitialization(0.35f, "Hiring Club Staff...")
                withContext(Dispatchers.IO) {
                    staffGenerator.generateDefaultStaffForAllClubs { progress ->
                        updateInitialization(0.35f + (progress * 0.2f), "Vetting Staff: ${(progress * 100).toInt()}%")
                    }
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "❌ Failed to initialize browsing database", e)
                _uiState.update { it.copy(errorMessage = "Universe initialization failed: ${e.message}") }
            }

            updateInitialization(0.6f, "Loading League Data...")
            loadAllLeagues()
            delay(200)

            updateInitialization(0.75f, "Loading Available Managers...")
            loadAvailableManagers()
            delay(200)

            updateInitialization(0.85f, "Meeting with Player Agents...")
            loadAgents()
            delay(200)

            updateInitialization(0.9f, "Checking Existing Careers...")
            checkFirstCareer()
            delay(200)

            updateInitialization(1.0f, "African Football Universe Ready.")
            delay(500)

            _uiState.update { it.copy(
                currentStep = CareerStep.MANAGER_SELECTION,
                isLoading = false
            ) }
        }
    }

    private fun updateInitialization(progress: Float, status: String) {
        _uiState.update { it.copy(
            initializationProgress = progress,
            initializationStatus = status
        ) }
    }

    private fun loadAllLeagues() {
        viewModelScope.launch {
            try {
                // Ensure we are observing the latest data after refresh
                val leagues = refreshTrigger.flatMapLatest { 
                    leaguesRepository.getAllLeagues() 
                }.firstOrNull() ?: emptyList()
                
                Log.d("CareerSetupVM", "Fetching leagues from database context: ${careerManager.getCurrentCareerId()}")
                
                if (leagues.isEmpty()) {
                    Log.w("CareerSetupVM", "⚠️ No leagues found in current database!")
                }

                cachedLeagues = leagues
                Log.d("CareerSetupVM", "✅ Loaded ${leagues.size} leagues")

                val leaguesByCountry: Map<String, List<LeaguesEntity>> = leagues.groupBy { it.country?.trim() ?: "Unknown" }

                Log.d("CareerSetupVM", "📋 Available countries: ${leaguesByCountry.keys.joinToString()}")

                _uiState.update { it.copy(
                    availableLeagues = leagues,
                    leaguesByCountry = leaguesByCountry,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "Failed to load leagues", e)
                _uiState.update { it.copy(
                    errorMessage = "Failed to load leagues: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    private fun checkFirstCareer() {
        viewModelScope.launch {
            try {
                val careers = careerManager.listCareers()
                _uiState.update { it.copy(isFirstCareer = careers.isEmpty()) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "Failed to check careers", e)
            }
        }
    }

    private fun loadAvailableManagers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingManagers = true) }
            try {
                val managers = refreshTrigger.flatMapLatest { 
                    managersRepository.getAvailableManagers() 
                }.firstOrNull() ?: emptyList()
                
                if (managers.isEmpty()) {
                    Log.w("CareerSetupVM", "⚠️ No available managers found in current database!")
                }

                Log.d("CareerSetupVM", "✅ Loaded ${managers.size} available managers")
                _uiState.update { it.copy(availableManagers = managers, isLoadingManagers = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "Failed to load managers", e)
                _uiState.update { it.copy(
                    errorMessage = "Failed to load managers: ${e.message}",
                    isLoadingManagers = false
                ) }
            }
        }
    }

    private fun loadAgents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAgents = true) }
            try {
                val entities = refreshTrigger.flatMapLatest { 
                    playerAgentsRepository.getAllAgents() 
                }.firstOrNull() ?: emptyList()

                val agents = entities.map { mapToAgent(it) }
                Log.d("CareerSetupVM", "✅ Loaded ${agents.size} agents")
                _uiState.update { it.copy(
                    availableAgents = agents,
                    filteredAgents = agents,
                    isLoadingAgents = false 
                ) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "Failed to load agents", e)
                _uiState.update { it.copy(isLoadingAgents = false) }
            }
        }
    }

    fun updateAgentFilters(query: String? = null, personality: String? = null) {
        _uiState.update { state ->
            val newQuery = query ?: state.agentSearchQuery
            val newPersonality = if (personality == "ALL") null else (personality ?: state.agentPersonalityFilter)
            
            val filtered = state.availableAgents.filter { agent ->
                val matchesQuery = agent.name.contains(newQuery, ignoreCase = true) || 
                                 agent.agency.contains(newQuery, ignoreCase = true)
                val matchesPersonality = newPersonality == null || agent.personality == newPersonality
                matchesQuery && matchesPersonality
            }
            
            state.copy(
                agentSearchQuery = newQuery,
                agentPersonalityFilter = newPersonality,
                filteredAgents = filtered
            )
        }
    }

    private fun mapToAgent(entity: PlayerAgentsEntity): Agent {
        val nationality = entity.nationality ?: "Tanzania"
        return Agent(
            id = entity.id,
            name = entity.agentName,
            agency = entity.agency ?: "Independent",
            reputation = entity.reputation,
            negotiationSkill = entity.negotiationPower,
            personality = mapAgentPersonality(entity.personality),
            commissionRate = entity.commissionRate / 100f,
            connections = entity.languagesList,
            nationality = nationality,
            nationalityFlag = NationalityUtils.getWavingFlagUrl(nationality)
        )
    }

    private fun mapAgentPersonality(personality: String): String {
        return when (personality) {
            "GREEDY" -> "Shark"
            "LOYAL" -> "Loyal"
            "AGGRESSIVE" -> "Hard Bargainer"
            "DEVELOPMENT_FOCUSED" -> "Networker"
            else -> "Networker"
        }
    }

    fun selectAgent(agent: Agent) {
        _uiState.update { it.copy(assignedAgent = agent) }
    }

    // ============ STEP ACTIONS ============

    fun setGameMode(mode: GameMode) {
        _uiState.update { it.copy(gameMode = mode) }
    }

    fun setManagerSelectionMode(mode: ManagerSelectionMode) {
        _uiState.update { it.copy(
            managerSelectionMode = mode,
            selectedExistingManager = if (mode == ManagerSelectionMode.CREATE_NEW) null else it.selectedExistingManager
        ) }
        
        // If selecting Existing, we might want to navigate immediately or via Next button
        // Based on user request "dedicated screen which it navigates to when existing is clicked"
        if (mode == ManagerSelectionMode.USE_EXISTING) {
            nextStep()
        }
    }

    fun selectExistingManager(manager: ManagersEntity) {
        _uiState.update { it.copy(
            selectedExistingManager = manager,
            managerName = manager.name,
            managerNationality = manager.nationality,
            managerAge = manager.age,
            managerStyle = getManagerialStyleFromString(manager.style),
            coachingLicense = getCoachingLicenseFromString(manager.coachingLicense),
            selectedAvatar = manager.faceImage ?: "coach_male_east",
            tacticalFlexibility = manager.tacticalFlexibility ?: 50,
            playerMotivation = manager.playerMotivation ?: 50,
            youthDevelopmentFocus = manager.youthDevelopmentFocus ?: 50,
            mediaHandling = manager.mediaHandling ?: 50,
            disciplineLevel = manager.disciplineLevel ?: 50,
            adaptability = manager.adaptability,
            managerReputationLevel = manager.reputationLevel
        ) }

        // Automatically select the existing manager's assigned agent
        manager.agentId?.let { agentId ->
            viewModelScope.launch {
                try {
                    playerAgentsRepository.getAgentById(agentId)?.let { entity ->
                        selectAgent(mapToAgent(entity))
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("CareerSetupVM", "Failed to load manager's agent", e)
                }
            }
        }
    }

    fun proceedFromManagerSelection() {
        if (_uiState.value.managerSelectionMode == ManagerSelectionMode.CREATE_NEW) {
            nextStep()
        } else if (_uiState.value.selectedExistingManager != null) {
            nextStep()
        }
    }

    // ============ CREATION ACTIONS ============

    fun updateManagerName(name: String) {
        _uiState.update { it.copy(
            managerName = name,
            managerNameError = if (name.isBlank()) "Name cannot be empty" else null
        ) }
    }

    fun updateManagerNationality(nationality: String) {
        _uiState.update { it.copy(managerNationality = nationality) }
        generateManagerAttributes()
    }

    fun updateBirthDate(year: Int, month: Int, day: Int) {
        val age = 2026 - year
        _uiState.update { it.copy(
            managerBirthYear = year,
            managerBirthMonth = month,
            managerBirthDay = day,
            managerAge = age
        ) }
        generateManagerAttributes()
    }

    fun updateManagerStyle(style: ManagerialStyle) {
        _uiState.update { it.copy(managerStyle = style) }
        generateManagerAttributes()
    }

    fun updateCoachingLicense(license: CoachingLicense) {
        _uiState.update { it.copy(coachingLicense = license) }
        generateManagerAttributes()
    }

    fun updateSpecialAbility(ability: String) {
        _uiState.update { it.copy(specialAbility = ability) }
        generateManagerAttributes()
    }

    fun selectAvatar(avatar: String) {
        _uiState.update { it.copy(selectedAvatar = avatar) }
    }

    fun updateAvatarRegion(region: String) {
        _uiState.update { it.copy(selectedAvatarRegion = region) }
    }

    private fun generateManagerAttributes() {
        val state = _uiState.value

        val country = PLAYABLE_COUNTRIES.find { it.displayName == state.managerNationality }
        val countryMod = when (country?.slug) {
            "tanzania", "kenya", "rwanda" -> CountryModifierValues(0.9f, 1.1f, 1.2f, 0.8f, 1.0f, 0.9f, 0.8f, "East African Focus: High Youth & Man-Management")
            "egypt", "tunisia", "algeria", "morocco" -> CountryModifierValues(1.2f, 0.9f, 0.8f, 1.1f, 1.1f, 1.1f, 0.9f, "North African Focus: Tactical Rigor & Discipline")
            "nigeria", "cameroon", "congo_drc" -> CountryModifierValues(1.0f, 1.2f, 1.1f, 0.9f, 1.2f, 0.8f, 1.1f, "West/Central Focus: High Motivation & Adaptability")
            "south_africa", "angola" -> CountryModifierValues(1.1f, 1.0f, 0.9f, 1.2f, 1.0f, 1.0f, 1.0f, "Southern Focus: Commercial & Tactical Balance")
            else -> CountryModifierValues(1f, 1f, 1f, 1f, 1f, 1f, 1f, "Continental Standard")
        }

        val licenseMod = state.coachingLicense.reputationBonus / 20f

        val personality = PersonalityProfile(
            temperament = (40..85).random(),
            ambition = (50..95).random(),
            loyalty = (30..80).random(),
            ego = (30..90).random(),
            professionalism = (50..95).random(),
            pressure = (40..90).random(),
            adaptability = (40..90).random()
        )

        fun calc(base: Int, mod: Float, lic: Float, special: Float = 1.0f): Int {
            return ((base * mod * (1.0f + lic)) * special).toInt().coerceIn(10, 99)
        }

        _uiState.update { it.copy(
            tacticalFlexibility = calc(55, countryMod.tacticalMod, licenseMod, if (state.specialAbility == "TACTICAL_GENIUS") 1.2f else 1.0f),
            playerMotivation = calc(60, countryMod.managementMod, licenseMod, if (state.specialAbility == "MOTIVATOR") 1.2f else 1.0f),
            youthDevelopmentFocus = calc(50, countryMod.youthMod, licenseMod, if (state.specialAbility == "YOUTH_DEVELOPER") 1.2f else 1.0f),
            mediaHandling = calc(55, countryMod.mediaMod, licenseMod),
            disciplineLevel = calc(50, countryMod.disciplineMod, licenseMod),
            adaptability = calc(50, countryMod.adaptabilityMod, licenseMod),
            countryModifier = countryMod,
            personalityProfile = personality
        ) }
    }

    // ============ SELECTION ACTIONS ============

    fun selectCountry(countryId: Int) {
        val country = PLAYABLE_COUNTRIES.find { it.id == countryId } ?: return

        _uiState.update { it.copy(
            selectedCountryId = countryId,
            selectedCountryName = country.displayName,
            isLoading = true
        ) }

        countryObservationJob?.cancel()
        countryObservationJob = viewModelScope.launch {
            try {
                val allLeagues = cachedLeagues ?: leaguesRepository.getAllLeagues().firstOrNull() ?: emptyList()

                val countryLeagues = allLeagues.filter { league ->
                    league.countryId == country.databaseId ||
                            league.country?.trim()?.equals(country.displayName.trim(), ignoreCase = true) == true
                }

                Log.d("CareerSetupVM", "📋 Found ${countryLeagues.size} leagues for ${country.displayName}")

                _uiState.update { currentState -> currentState.copy(
                    availableLeagues = countryLeagues,
                    leaguesByLevel = countryLeagues.groupBy { it.level },
                    isLoading = false
                ) }

                if (countryLeagues.isNotEmpty()) {
                    selectLeague(countryLeagues.first())
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "Failed to select country", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectLeague(league: LeaguesEntity) {
        Log.d("CareerSetupVM", "🏆 Selecting league: ${league.name}")

        _uiState.update { it.copy(
            selectedLeague = league,
            selectedClub = null,
            isLoading = true
        ) }

        viewModelScope.launch {
            try {
                val teams = teamsRepository.getTeamsByLeague(league.name.trim()).firstOrNull() ?: emptyList()
                Log.d("CareerSetupVM", "⚽ Found ${teams.size} teams for league ${league.name}")

                _uiState.update { it.copy(
                    availableClubs = teams,
                    filteredClubs = teams,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "Failed to load teams", e)
                _uiState.update { it.copy(
                    errorMessage = "Failed to load clubs: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun selectDivision(divisionId: Int?) {
        _uiState.update { it.copy(selectedDivisionId = divisionId) }

        val currentClubs = _uiState.value.availableClubs
        if (divisionId != null && currentClubs.isNotEmpty()) {
            val filtered = currentClubs.filter { it.id == divisionId }
            _uiState.update { it.copy(filteredClubs = filtered) }
        } else {
            _uiState.update { it.copy(filteredClubs = currentClubs) }
        }
    }

    fun selectClub(club: TeamsEntity) {
        Log.d("CareerSetupVM", "🏟️ Selecting club: ${club.name}")
        _uiState.update { it.copy(selectedClub = club) }
        
        // Load Club DNA and Ownership
        viewModelScope.launch {
            try {
                val dna = clubDNARepository.getClubDNA(club.id)
                val chairman = chairmanRepository.getChairmanByTeam(club.id)
                _uiState.update { it.copy(
                    selectedClubDNA = dna,
                    selectedClubChairman = chairman
                ) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CareerSetupVM", "Failed to load club details", e)
            }
        }
    }

    fun toggleDNADialog(show: Boolean) {
        _uiState.update { it.copy(showDNADialog = show) }
    }

    fun toggleOwnershipDialog(show: Boolean) {
        _uiState.update { it.copy(showOwnershipDialog = show) }
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun selectCareerVision(vision: CareerVision) {
        _uiState.update { it.copy(careerVision = vision) }
    }

    // ============ NAVIGATION & FINALIZATION ============

    fun startCareer() {
        if (_uiState.value.isLoading && _uiState.value.currentStep == CareerStep.PREPARING_GAME) return

        // Check for existing save before proceeding
        if (!_uiState.value.isFirstCareer && !_uiState.value.showOverwriteWarning) {
            _uiState.update { it.copy(showOverwriteWarning = true) }
            return
        }

        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedClub == null || state.selectedLeague == null) {
                Log.e("CareerSetupVM", "Cannot start career: Missing club or league")
                _uiState.update { it.copy(errorMessage = "Missing club or league selection") }
                return@launch
            }

            val finalCareerId = 1 // Force ID 1 for single-save release
            _uiState.update { it.copy(currentStep = CareerStep.PREPARING_GAME, isLoading = true, careerId = finalCareerId.toLong()) }

            try {
                // Use NonCancellable to ensure DB creation completes even if user navigates away
                withContext(Dispatchers.IO + NonCancellable) {

                    // STEP 0: Overwrite existing save if it exists
                    if (!state.isFirstCareer) {
                        updatePreparation(0.01f, "Overwriting existing career...")
                        careerManager.deleteCareer(finalCareerId)
                        delay(200)
                    }

                    // STEP 1: Create and ensure the career database exists
                    updatePreparation(0.05f, "Creating career database...")
                    careerManager.ensureCareerDatabase(finalCareerId)

                    // STEP 2: Initialize the database with static data
                    updatePreparation(0.1f, "Loading African Football Universe...")
                    gameInitializer.initializeNewCareer(
                        careerId = finalCareerId,
                        managerName = state.managerName,
                        selectedTeamId = state.selectedClub.id,
                        onProgress = { progress, message ->
                            updatePreparation(0.1f + (progress * 0.4f), "Universe Migration: $message")
                        }
                    )

                    updatePreparation(0.55f, "Negotiating with your agent...")

                    val agent = state.assignedAgent ?: assignStartingAgent()

                    updatePreparation(0.65f, "Agent contract signed...")
                    delay(300)

                    _uiState.update { it.copy(assignedAgent = agent) }

                    updatePreparation(0.75f, "Signing management contract...")
                    delay(300)

                    // STEP 3: Create the career with all the setup data
                    careerManager.createNewCareer(
                        careerId = finalCareerId,
                        managerName = state.managerName,
                        managerAge = state.managerAge,
                        nationality = state.managerNationality,
                        coachingLicense = state.coachingLicense.displayName,
                        managerStyle = state.managerStyle.displayName,
                        preferredFormation = state.favoriteTactics.formation,
                        youthDevelopment = state.youthDevelopmentFocus,
                        mediaHandling = state.mediaHandling,
                        tacticalFlexibility = state.tacticalFlexibility,
                        playerMotivation = state.playerMotivation,
                        disciplineLevel = state.disciplineLevel,
                        adaptability = state.adaptability,
                        teamId = state.selectedClub.id,
                        difficulty = state.selectedDifficulty?.displayName ?: "Normal",
                        selectedAvatar = state.selectedAvatar,
                        agentId = agent.id,
                        careerVision = state.careerVision?.name,
                        personalityProfile = state.personalityProfile?.let { Json.encodeToString(it) }
                    )

                    updatePreparation(1.0f, "Career Established!")
                    delay(500)

                    // Navigate using Channel
                    _events.send(CareerEvent.NavigateToDashboard(finalCareerId))

                    _uiState.update { it.copy(setupComplete = true, isLoading = false, canStartCareer = true) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("AFM_CAREER", "Failed to start career", e)
                if (viewModelScope.isActive) {
                    _uiState.update { it.copy(
                        errorMessage = "Failed to start career: ${e.message}\nPlease try again",
                        isLoading = false,
                        currentStep = CareerStep.CAREER_SUMMARY
                    ) }
                }
            }
        }
    }

    private fun assignStartingAgent(): Agent {
        val agentPool = listOf(
            Agent(1, "Saidi King", "East African Sports", 45, 60, "Loyal", 0.05f, listOf("Tanzania", "Kenya"), "Tanzania", NationalityUtils.getWavingFlagUrl("Tanzania")),
            Agent(2, "Moussa Diop", "Sahel Management", 70, 85, "Shark", 0.12f, listOf("Senegal", "Mali", "France"), "Senegal", NationalityUtils.getWavingFlagUrl("Senegal")),
            Agent(3, "John Phiri", "COSAFA Elite", 55, 70, "Networker", 0.08f, listOf("South Africa", "Zambia"), "South Africa", NationalityUtils.getWavingFlagUrl("South Africa")),
            Agent(4, "Ahmed Mansour", "Pharaoh Agency", 80, 90, "Hard Bargainer", 0.15f, listOf("Egypt", "UAE", "Saudi Arabia"), "Egypt", NationalityUtils.getWavingFlagUrl("Egypt"))
        )

        return agentPool.random()
    }

    private fun updatePreparation(progress: Float, status: String) {
        _uiState.update { it.copy(preparationProgress = progress, preparationStatus = status) }
    }

    fun nextStep() {
        val state = _uiState.value
        if (!state.canProceedToNextStep) return

        val nextStep = when (state.currentStep) {
            CareerStep.INITIALIZING -> CareerStep.MODE_SELECTION
            CareerStep.MODE_SELECTION -> CareerStep.MANAGER_SELECTION
            CareerStep.MANAGER_SELECTION -> {
                if (state.managerSelectionMode == ManagerSelectionMode.CREATE_NEW) CareerStep.MANAGER_CREATION
                else CareerStep.EXISTING_MANAGER_SELECTION
            }
            CareerStep.EXISTING_MANAGER_SELECTION -> CareerStep.AGENT_SELECTION
            CareerStep.MANAGER_CREATION -> CareerStep.AVATAR_CUSTOMIZATION
            CareerStep.AVATAR_CUSTOMIZATION -> CareerStep.AGENT_SELECTION
            CareerStep.AGENT_SELECTION -> CareerStep.CAREER_VISION
            CareerStep.CAREER_VISION -> CareerStep.COUNTRY_SELECTION
            CareerStep.COUNTRY_SELECTION -> CareerStep.LEAGUE_SELECTION
            CareerStep.LEAGUE_SELECTION -> CareerStep.CLUB_SELECTION
            CareerStep.CLUB_SELECTION -> CareerStep.DIFFICULTY_SELECTION
            CareerStep.DIFFICULTY_SELECTION -> CareerStep.CAREER_SUMMARY
            CareerStep.CAREER_SUMMARY -> CareerStep.PREPARING_GAME
            CareerStep.PREPARING_GAME -> CareerStep.PREPARING_GAME
        }

        _uiState.update { it.copy(currentStep = nextStep) }

        if (nextStep == CareerStep.MANAGER_CREATION && state.managerName.isNotBlank()) {
            generateManagerAttributes()
        }
    }

    fun previousStep() {
        val state = _uiState.value
        val prevStep = when (state.currentStep) {
            CareerStep.MODE_SELECTION -> CareerStep.INITIALIZING
            CareerStep.MANAGER_SELECTION -> CareerStep.MODE_SELECTION
            CareerStep.EXISTING_MANAGER_SELECTION -> CareerStep.MANAGER_SELECTION
            CareerStep.MANAGER_CREATION -> CareerStep.MANAGER_SELECTION
            CareerStep.AVATAR_CUSTOMIZATION -> CareerStep.MANAGER_CREATION
            CareerStep.AGENT_SELECTION -> {
                if (state.managerSelectionMode == ManagerSelectionMode.USE_EXISTING) {
                    CareerStep.EXISTING_MANAGER_SELECTION
                } else {
                    CareerStep.AVATAR_CUSTOMIZATION
                }
            }
            CareerStep.CAREER_VISION -> CareerStep.AGENT_SELECTION
            CareerStep.COUNTRY_SELECTION -> CareerStep.CAREER_VISION
            CareerStep.LEAGUE_SELECTION -> CareerStep.COUNTRY_SELECTION
            CareerStep.CLUB_SELECTION -> CareerStep.LEAGUE_SELECTION
            CareerStep.DIFFICULTY_SELECTION -> CareerStep.CLUB_SELECTION
            CareerStep.CAREER_SUMMARY -> CareerStep.DIFFICULTY_SELECTION
            else -> state.currentStep
        }
        _uiState.update { it.copy(currentStep = prevStep) }
    }

    fun cancelOverwrite() {
        _uiState.update { it.copy(showOverwriteWarning = false) }
    }

    // ============ HELPERS ============

    private fun getManagerialStyleFromString(style: String?): ManagerialStyle {
        return ManagerialStyle.entries.find { it.displayName.equals(style, ignoreCase = true) } ?: ManagerialStyle.BALANCED
    }

    private fun getCoachingLicenseFromString(license: String?): CoachingLicense {
        return CoachingLicense.entries.find { it.displayName.equals(license, ignoreCase = true) } ?: CoachingLicense.CAF_C
    }
}