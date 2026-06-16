package com.fameafrica.afm.ui.screen.tactics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.Formation
import com.fameafrica.afm.data.database.entities.NationalitiesEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.Playstyle
import com.fameafrica.afm.data.database.entities.TacticsEntity
import com.fameafrica.afm.data.repository.FixturesRepository
import com.fameafrica.afm.data.repository.NationalitiesRepository
import com.fameafrica.afm.data.repository.PlayersRepository
import com.fameafrica.afm.data.repository.TacticalSuggestion
import com.fameafrica.afm.data.repository.TacticsRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.screen.match.PitchPattern
import com.fameafrica.afm.utils.tactics.PlayerRoleManager
import com.fameafrica.afm.utils.tactics.ProbabilisticBreakdown
import com.fameafrica.afm.utils.tactics.TacticalInsight
import com.fameafrica.afm.utils.tactics.TacticalMatchupEngine
import com.fameafrica.afm.utils.tactics.LineupUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LineupStrategy { MANUAL, AUTOMATIC }
enum class SubstitutionStrategy {
    MANUAL,
    AUTOMATIC,
    AFTER_50,
    AFTER_60,
    AFTER_75;

    override fun toString(): String {
        return when(this) {
            MANUAL -> "MANUAL"
            AUTOMATIC -> "AUTOMATIC"
            AFTER_50 -> "AFTER 50 MINS"
            AFTER_60 -> "AFTER 60 MINS"
            AFTER_75 -> "AFTER 75 MINS"
        }
    }
}

enum class TacticsVisualizationMode { NONE, HEATMAP, PASSING_LINES, PRESS_ZONES }
enum class TacticsDisplayMode { RATING, FORM, NATIONALITY }

data class TacticalFeedback(
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val formationSpecific: List<String> = emptyList()
)

data class TacticSlot(
    val slotNumber: Int,
    val name: String = "Slot $slotNumber",
    val formation: String = "",
    val style: String = "",
    val isFavorite: Boolean = false,
    val isEmpty: Boolean = true
)

enum class PlayerFilterType(val label: String) {
    ALL("All"),
    GK("GK"),
    DEF("Def"),
    MID("Mid"),
    ATT("Att"),
    PROFICIENCY("Proficiency"),
    SHARPNESS("Sharpness"),
    FORM("Form"),
    FATIGUE("Fatigue")
}

enum class PlayerSortType(val label: String) {
    RATING("Rating"),
    PROFICIENCY("Proficiency"),
    SHARPNESS("Sharpness"),
    POSITION("Position"),
    NAME("Name"),
    AVG_RATING("Avg Rating")
}

enum class TacticsMentality(val label: String) {
    ATTACK("ATTACK"),
    BALANCE("BALANCE"),
    DEFEND("DEFEND"),
    PRESS("PRESS")
}

data class TeamRoles(
    val captainId: Int? = null,
    val viceCaptainId: Int? = null,
    val penaltyTakerId: Int? = null,
    val freeKickTakerId: Int? = null,
    val cornerTakerId: Int? = null
)

data class TacticsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val teamId: Int = 0,
    val teamName: String = "",
    val formations: List<String> = Formation.entries.map { it.value },
    val selectedFormation: String = Formation.FORMATION_442.value,
    val selectedStyle: String = Playstyle.STRUCTURED.value,
    val tacticalArchetype: String = "BALANCED",
    val defensiveThreshold: Int = 50,
    val attackingThreshold: Int = 50,
    val tempo: Int = 50,
    val width: Int = 50,
    val depth: Int = 50,
    val pressIntensity: Int = 50,
    val passingDirectness: Int = 50,
    val creativity: Int = 50,
    val tacticalFamiliarity: Int = 50,

    val teamPlayers: List<PlayersEntity> = emptyList(),
    val startingXiIds: List<Int> = emptyList(),
    val substituteIds: List<Int> = emptyList(),
    val roles: TeamRoles = TeamRoles(),
    val nationalities: Map<String, NationalitiesEntity> = emptyMap(),

    val lineupStrategy: LineupStrategy = LineupStrategy.AUTOMATIC,
    val substitutionStrategy: SubstitutionStrategy = SubstitutionStrategy.MANUAL,

    val filterType: String = "Form",
    val autoselectRoles: Boolean = true,
    val pitchPattern: PitchPattern = PitchPattern.VERTICAL_STRIPES,
    val visualizationMode: TacticsVisualizationMode = TacticsVisualizationMode.NONE,
    val displayMode: TacticsDisplayMode = TacticsDisplayMode.RATING,

    val feedback: TacticalFeedback = TacticalFeedback(),
    val savedTactics: List<TacticSlot> = List(4) { TacticSlot(it + 1) },
    val currentSlot: Int = 1,
    val mentality: TacticsMentality = TacticsMentality.BALANCE,
    val filter: PlayerFilterType = PlayerFilterType.ALL,
    val sortBy: PlayerSortType = PlayerSortType.RATING,

    val recommendations: List<TacticalSuggestion> = emptyList(),
    val opponentTeamId: Int? = null,
    val opponentTeam: String? = null,

    val teamStrength: Double = 0.0,
    val winProb: Double = 0.33,
    val drawProb: Double = 0.34,
    val lossProb: Double = 0.33,
    val insights: List<TacticalInsight> = emptyList(),
    val breakdown: ProbabilisticBreakdown? = null,
    val teamIdentity: String = "Balanced System"
)

@HiltViewModel
class TacticsViewModel @Inject constructor(
    private val tacticsRepository: TacticsRepository,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val nationalitiesRepository: NationalitiesRepository,
    private val fixturesRepository: FixturesRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TacticsUiState(isLoading = true))
    val uiState: StateFlow<TacticsUiState> = _uiState

    private var initialLoadDone = false

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            combine(
                gameManager.gameState,
                gameManager.currentSquad,
                nationalitiesRepository.getAllNationalities()
            ) { state, squad, nats ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    if (!initialLoadDone || context.teamId != _uiState.value.teamId) {
                        refreshTacticsState(context.teamId, context.teamName, context.currentDate, squad, nats.associateBy { it.nationality })
                        initialLoadDone = true
                    } else {
                        _uiState.update { it.copy(teamPlayers = squad) }
                    }
                }
            }.collect()
        }
    }

    private suspend fun refreshTacticsState(
        teamId: Int,
        teamName: String,
        currentDate: String,
        squad: List<PlayersEntity>,
        nats: Map<String, NationalitiesEntity>
    ) {
        val tactics = tacticsRepository.getTacticsByTeam(teamId)
        val team = teamsRepository.getTeamById(teamId)

        val nextFixture = team?.let { fixturesRepository.getUpcomingFixturesByTeam(it.id, currentDate).firstOrNull()?.firstOrNull() }
        val opponent = if (nextFixture?.homeTeamId == teamId) nextFixture.awayTeamId else nextFixture?.homeTeamId

        val suggestions = if (opponent != null) {
            tacticsRepository.getTacticalRecommendations(teamId, opponent)
        } else emptyList()

        val defaultFormation = team?.formation ?: Formation.FORMATION_442.value
        val currentFormation = tactics?.formation ?: defaultFormation

        // Logic: Strictly use saved IDs if they exist and are valid for the current squad.
        // This ensures the EXACT lineup order (positions) is persisted across sessions.
        val squadIds = squad.map { it.id }.toSet()
        var starters = tactics?.startingXiIds?.filter { squadIds.contains(it) } ?: emptyList()
        var subs = tactics?.substituteIds?.filter { squadIds.contains(it) } ?: emptyList()

        // Fallback: If no saved lineup or incomplete (due to transfers/sales), 
        // use LineupUtils to re-organize while respecting isStartingXi flags.
        if (starters.size < 11) {
            starters = LineupUtils.autoSelectLineup(squad, currentFormation, "Form")
        }

        if (subs.isEmpty()) {
            subs = squad.filter { !starters.contains(it.id) }
                .sortedByDescending { it.rating }
                .take(7)
                .map { it.id }
        }

        val databaseStarters = squad.filter { it.isStartingXi }

        _uiState.update { state ->
            if (tactics != null) {
                state.copy(
                    isLoading = false,
                    teamId = teamId,
                    teamName = teamName,
                    selectedFormation = tactics.formation,
                    selectedStyle = tactics.playstyle,
                    tacticalArchetype = tactics.tacticalArchetype,
                    defensiveThreshold = tactics.defensiveThreshold,
                    attackingThreshold = tactics.attackingThreshold,
                    tempo = tactics.tempo,
                    width = tactics.width,
                    depth = tactics.depth,
                    pressIntensity = tactics.pressIntensity,
                    passingDirectness = tactics.passingDirectness,
                    creativity = tactics.creativity,
                    tacticalFamiliarity = tactics.tacticalFamiliarity,
                    teamPlayers = squad,
                    startingXiIds = starters,
                    substituteIds = subs,
                    roles = TeamRoles(
                        captainId = tactics.captainId ?: databaseStarters.find { it.isCaptain }?.id ?: starters.firstOrNull(),
                        viceCaptainId = tactics.viceCaptainId ?: databaseStarters.find { it.isViceCaptain }?.id ?: starters.getOrNull(1),
                        penaltyTakerId = tactics.penaltyTakerId ?: squad.filter { starters.contains(it.id) }.maxByOrNull { it.finishing }?.id,
                        freeKickTakerId = tactics.freeKickTakerId ?: squad.filter { starters.contains(it.id) }.maxByOrNull { it.skill }?.id,
                        cornerTakerId = tactics.cornerTakerId ?: squad.filter { starters.contains(it.id) }.maxByOrNull { it.crossing }?.id
                    ),
                    nationalities = nats,
                    recommendations = suggestions,
                    opponentTeamId = opponent,
                    lineupStrategy = if (tactics.autoselectLineup) LineupStrategy.AUTOMATIC else LineupStrategy.MANUAL,
                    substitutionStrategy = SubstitutionStrategy.entries.find { s -> s.name == tactics.substitutionStrategy } ?: SubstitutionStrategy.MANUAL
                )
            } else {
                state.copy(
                    isLoading = false,
                    teamId = teamId,
                    teamName = teamName,
                    selectedFormation = currentFormation,
                    teamPlayers = squad,
                    startingXiIds = starters,
                    substituteIds = subs,
                    nationalities = nats,
                    recommendations = suggestions,
                    opponentTeamId = opponent
                )
            }
        }

        recalculateEngineStats()
    }

    private suspend fun recalculateEngineStats() {
        val activeContext = (gameManager.gameState.value as? GameManager.GameState.Active)?.context ?: return
        val teamName = activeContext.teamName
        val team = teamsRepository.getTeamByName(teamName) ?: return

        val strength = ((team.eloRating / 2000.0 * 0.6) + (team.overallRating / 100.0 * 0.4)) * 100.0

        var winP = 0.33
        var drawP = 0.34
        var lossP = 0.33
        var insights = emptyList<TacticalInsight>()
        var breakdown: ProbabilisticBreakdown? = null
        var identity = "Balanced System"

        _uiState.value.opponentTeamId?.let { oppId ->
            val opponent = teamsRepository.getTeamById(oppId)
            val oppTactics = tacticsRepository.getTacticsByTeam(oppId) ?: TacticsEntity(
                teamId = oppId,
                teamName = opponent?.name ?: "Unknown",
                formation = "4-4-2",
                tacticalArchetype = "BALANCED",
                playstyle = "Balanced"
            )

            if (opponent != null) {
                val currentStarters = _uiState.value.teamPlayers.filter { _uiState.value.startingXiIds.contains(it.id) }
                identity = PlayerRoleManager.getTeamIdentity(currentStarters)

                val currentTactics = TacticsEntity(
                    teamId = team.id,
                    teamName = team.name,
                    formation = _uiState.value.selectedFormation,
                    tacticalArchetype = _uiState.value.tacticalArchetype,
                    playstyle = _uiState.value.selectedStyle,
                    defensiveThreshold = _uiState.value.defensiveThreshold,
                    attackingThreshold = _uiState.value.attackingThreshold,
                    tempo = _uiState.value.tempo,
                    width = _uiState.value.width,
                    depth = _uiState.value.depth,
                    pressIntensity = _uiState.value.pressIntensity,
                    passingDirectness = _uiState.value.passingDirectness,
                    creativity = _uiState.value.creativity,
                    tacticalFamiliarity = _uiState.value.tacticalFamiliarity
                )

                val modifiedTactics = PlayerRoleManager.applyLineupRoleEffects(currentStarters, currentTactics)
                val detailedBreakdown = TacticalMatchupEngine.calculateDetailedBreakdown(modifiedTactics, oppTactics, team.eloRating, team.overallRating, opponent.eloRating, opponent.overallRating, currentStarters)
                
                breakdown = detailedBreakdown
                winP = detailedBreakdown.winProb
                drawP = detailedBreakdown.drawProb
                lossP = detailedBreakdown.lossProb
                insights = TacticalMatchupEngine.generateInsights(modifiedTactics, oppTactics, detailedBreakdown, currentStarters)
            }
        }

        _uiState.update { it.copy(teamStrength = strength, winProb = winP, drawProb = drawP, lossProb = lossP, insights = insights, breakdown = breakdown, teamIdentity = identity) }
        analyzeSquadFeedback()
    }

    private fun analyzeSquadFeedback() {
        val players = _uiState.value.teamPlayers
        val formation = _uiState.value.selectedFormation
        
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        val formationSpecific = mutableListOf<String>()

        val cbCount = players.count { it.position == "CB" }
        if (cbCount >= 5) strengths.add("Strong CB depth ($cbCount center-backs available)")
        
        val tallCbs = players.count { it.position == "CB" && it.height >= 185 }
        if (tallCbs >= 3) strengths.add("Multiple aerial threats ($tallCbs tall CBs)")

        val creativeForwards = players.count { it.positionCategory == "FORWARD" && it.creativity >= 70 }
        if (creativeForwards >= 2) strengths.add("Creative CF options available")

        val peakSharpness = players.count { it.sharpness >= 85 }
        if (peakSharpness >= players.size * 0.7) strengths.add("Peak sharpness across most players")

        val cdmCount = players.count { it.position == "CDM" }
        if (cdmCount <= 1) weaknesses.add(if (cdmCount == 1) "Only 1 natural CDM" else "No natural CDM in squad")

        val camCount = players.count { it.position == "CAM" }
        if (camCount == 0) weaknesses.add("No natural CAM in squad")

        val gkCount = players.count { it.position == "GK" }
        if (gkCount <= 1) weaknesses.add("GK depth limited")

        // Formation Specific
        when (formation) {
            "4-3-3" -> {
                formationSpecific.add("Wide overload potential with wingers and overlapping full-backs")
                if (cdmCount == 1) formationSpecific.add("Single pivot vulnerable to counter-attacks")
                formationSpecific.add("Recommend: Set CDM to 'Defend' focus for coverage")
            }
            "3-5-2" -> {
                if (cbCount >= 5) formationSpecific.add("Strong - You have enough CBs to rotate through back 3")
                val naturalWbs = players.count { it.position == "LWB" || it.position == "RWB" }
                if (naturalWbs == 0) {
                    formationSpecific.add("Warning: No natural wing-backs in squad")
                    formationSpecific.add("Recommend: Train wide midfielders for WB role")
                }
            }
        }

        _uiState.update { it.copy(feedback = TacticalFeedback(strengths, weaknesses, formationSpecific)) }
    }

    fun setFilter(filter: PlayerFilterType) {
        _uiState.update { it.copy(filter = filter) }
        // Implement filtering logic if needed for squad panel
    }

    fun setSortBy(sortBy: PlayerSortType) {
        _uiState.update { it.copy(sortBy = sortBy) }
        // Implement sorting logic if needed for squad panel
    }

    fun setMentality(mentality: TacticsMentality) {
        _uiState.update { it.copy(mentality = mentality) }
        // Adjust tactical sliders based on mentality?
        when (mentality) {
            TacticsMentality.ATTACK -> updateSlider("depth", 80)
            TacticsMentality.DEFEND -> updateSlider("depth", 20)
            else -> {}
        }
    }

    fun loadSlot(slotNumber: Int) {
        val slot = _uiState.value.savedTactics.find { it.slotNumber == slotNumber }
        if (slot != null && !slot.isEmpty) {
            _uiState.update { it.copy(
                currentSlot = slotNumber,
                selectedFormation = slot.formation,
                selectedStyle = slot.style
            ) }
            // Apply other settings from slot if stored
            viewModelScope.launch { recalculateEngineStats() }
        }
    }

    fun saveToSlot(slotNumber: Int) {
        _uiState.update { state ->
            val updatedSlots = state.savedTactics.map {
                if (it.slotNumber == slotNumber) {
                    it.copy(
                        formation = state.selectedFormation,
                        style = state.selectedStyle,
                        isEmpty = false
                    )
                } else it
            }
            state.copy(savedTactics = updatedSlots, currentSlot = slotNumber)
        }
    }

    fun favoriteSlot(slotNumber: Int) {
        _uiState.update { state ->
            val updatedSlots = state.savedTactics.map {
                if (it.slotNumber == slotNumber) it.copy(isFavorite = !it.isFavorite)
                else it
            }
            state.copy(savedTactics = updatedSlots)
        }
    }

    fun renameSlot(slotNumber: Int, newName: String) {
        _uiState.update { state ->
            val updatedSlots = state.savedTactics.map {
                if (it.slotNumber == slotNumber) it.copy(name = newName)
                else it
            }
            state.copy(savedTactics = updatedSlots)
        }
    }

    fun deleteSlot(slotNumber: Int) {
        _uiState.update { state ->
            val updatedSlots = state.savedTactics.map {
                if (it.slotNumber == slotNumber) TacticSlot(slotNumber)
                else it
            }
            state.copy(savedTactics = updatedSlots)
        }
    }

    fun swapPlayers(fromId: Int, toId: Int) {
        _uiState.update { state ->
            val currentStarters = state.startingXiIds.toMutableList()
            val currentSubs = state.substituteIds.toMutableList()

            val fromInStarters = currentStarters.indexOf(fromId)
            val toInStarters = currentStarters.indexOf(toId)
            val fromInSubs = currentSubs.indexOf(fromId)
            val toInSubs = currentSubs.indexOf(toId)

            when {
                fromInStarters != -1 && toInStarters != -1 -> {
                    currentStarters[fromInStarters] = toId
                    currentStarters[toInStarters] = fromId
                }
                fromInSubs != -1 && toInSubs != -1 -> {
                    currentSubs[fromInSubs] = toId
                    currentSubs[toInSubs] = fromId
                }
                fromInStarters != -1 && toInSubs != -1 -> {
                    currentStarters[fromInStarters] = toId
                    currentSubs[toInSubs] = fromId
                }
                fromInSubs != -1 && toInStarters != -1 -> {
                    currentSubs[fromInSubs] = toId
                    currentStarters[toInStarters] = fromId
                }
                fromInStarters != -1 -> { currentStarters[fromInStarters] = toId }
                toInStarters != -1 -> { currentStarters[toInStarters] = fromId }
                fromInSubs != -1 -> { currentSubs[fromInSubs] = toId }
                toInSubs != -1 -> { currentSubs[toInSubs] = fromId }
            }

            state.copy(startingXiIds = currentStarters.distinct().take(11), substituteIds = currentSubs.distinct().take(7))
        }
        if (_uiState.value.autoselectRoles) {
            viewModelScope.launch { recalculateEngineStats() }
        }
    }

    fun selectFormation(formation: String) {
        _uiState.update { it.copy(selectedFormation = formation) }
        if (_uiState.value.lineupStrategy == LineupStrategy.AUTOMATIC) autoSelectLineup()
        viewModelScope.launch { recalculateEngineStats() }
    }

    fun setFilterType(type: String) {
        _uiState.update { it.copy(filterType = type) }
        if (_uiState.value.lineupStrategy == LineupStrategy.AUTOMATIC) autoSelectLineup()
    }

    fun setLineupStrategy(strategy: LineupStrategy) {
        _uiState.update { it.copy(lineupStrategy = strategy) }
        if (strategy == LineupStrategy.AUTOMATIC) autoSelectLineup()
    }

    fun setSubstitutionStrategy(strategy: SubstitutionStrategy) {
        _uiState.update { it.copy(substitutionStrategy = strategy) }
    }

    fun toggleAutoselectRoles(enabled: Boolean) {
        _uiState.update { it.copy(autoselectRoles = enabled) }
        if (enabled) autoSelectLineup()
    }

    fun setPitchPattern(pattern: PitchPattern) {
        _uiState.update { it.copy(pitchPattern = pattern) }
    }

    fun toggleDisplayMode() {
        _uiState.update { state ->
            val nextMode = when (state.displayMode) {
                TacticsDisplayMode.RATING -> TacticsDisplayMode.FORM
                TacticsDisplayMode.FORM -> TacticsDisplayMode.NATIONALITY
                TacticsDisplayMode.NATIONALITY -> TacticsDisplayMode.RATING
            }
            state.copy(displayMode = nextMode)
        }
    }

    fun autoSelectLineup() {
        val players = _uiState.value.teamPlayers
        if (players.isEmpty()) return

        val newStarters = LineupUtils.autoSelectLineup(players, _uiState.value.selectedFormation, _uiState.value.filterType).take(11)
        val availablePlayers = players.filter { !newStarters.contains(it.id) }

        _uiState.update { state ->
            state.copy(
                startingXiIds = newStarters,
                substituteIds = availablePlayers.sortedByDescending { it.overallRating }.take(7).map { it.id }
            )
        }
        if (_uiState.value.autoselectRoles) {
            viewModelScope.launch { recalculateEngineStats() }
        }
    }

    fun updateRole(roleType: String, playerId: Int) {
        _uiState.update { state ->
            val currentRoles = state.roles
            val newRoles = when (roleType) {
                "Captain" -> currentRoles.copy(captainId = playerId)
                "Vice Captain" -> currentRoles.copy(viceCaptainId = playerId)
                "Penalty Taker" -> currentRoles.copy(penaltyTakerId = playerId)
                "Free-kick Taker" -> currentRoles.copy(freeKickTakerId = playerId)
                "Corner Taker" -> currentRoles.copy(cornerTakerId = playerId)
                else -> currentRoles
            }
            state.copy(roles = newRoles)
        }
    }

    fun updateStyle(style: String) {
        _uiState.update { it.copy(selectedStyle = style) }
        viewModelScope.launch { recalculateEngineStats() }
    }

    fun updateSlider(slider: String, value: Int) {
        _uiState.update { state ->
            when (slider) {
                "tempo" -> state.copy(tempo = value)
                "width" -> state.copy(width = value)
                "depth" -> state.copy(depth = value)
                "press" -> state.copy(pressIntensity = value)
                "passing" -> state.copy(passingDirectness = value)
                "creativity" -> state.copy(creativity = value)
                else -> state
            }
        }
        viewModelScope.launch { recalculateEngineStats() }
    }

    fun saveTactics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                val teamName = state.context.teamName
                val teamId = state.context.teamId

                tacticsRepository.customizeTactics(
                    teamId = teamId,
                    teamName = teamName,
                    formation = _uiState.value.selectedFormation,
                    playstyle = _uiState.value.selectedStyle,
                    archetype = _uiState.value.tacticalArchetype,
                    defensiveThreshold = _uiState.value.defensiveThreshold,
                    attackingThreshold = _uiState.value.attackingThreshold,
                    tempo = _uiState.value.tempo,
                    width = _uiState.value.width,
                    depth = _uiState.value.depth,
                    pressIntensity = _uiState.value.pressIntensity,
                    passingDirectness = _uiState.value.passingDirectness,
                    creativity = _uiState.value.creativity,
                    autoselectLineup = _uiState.value.lineupStrategy == LineupStrategy.AUTOMATIC,
                    substitutionStrategy = _uiState.value.substitutionStrategy.name,
                    startingXiIds = _uiState.value.startingXiIds,
                    substituteIds = _uiState.value.substituteIds,
                    captainId = _uiState.value.roles.captainId,
                    viceCaptainId = _uiState.value.roles.viceCaptainId,
                    penaltyTakerId = _uiState.value.roles.penaltyTakerId,
                    freeKickTakerId = _uiState.value.roles.freeKickTakerId,
                    cornerTakerId = _uiState.value.roles.cornerTakerId
                )

                teamsRepository.getTeamById(teamId)?.let { team ->
                    teamsRepository.updateTeam(team.copy(formation = _uiState.value.selectedFormation))
                }

                playersRepository.setStartingXI(teamId, _uiState.value.startingXiIds)
                _uiState.value.roles.captainId?.let { id -> playersRepository.assignCaptain(teamId, id) }
                _uiState.value.roles.viceCaptainId?.let { id -> playersRepository.assignViceCaptain(teamId, id) }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun resetTactics() {
        _uiState.update { it.copy(
            selectedFormation = Formation.FORMATION_442.value,
            selectedStyle = Playstyle.STRUCTURED.value,
            tempo = 50, width = 50, depth = 50, pressIntensity = 50, passingDirectness = 50, creativity = 50
        ) }
        autoSelectLineup()
    }
}
