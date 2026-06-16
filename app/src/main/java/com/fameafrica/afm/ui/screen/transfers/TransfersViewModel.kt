package com.fameafrica.afm.ui.screen.transfers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.database.model.PlayerSummary
import com.fameafrica.afm.data.database.model.PlayerFilter
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.scouting.ScoutingIntelligenceService
import com.fameafrica.afm.domain.transfer.TransferMarketFilterEngine
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val transfersRepository: TransfersRepository,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val shortlistRepository: ShortlistRepository,
    private val newsRepository: NewsRepository,
    private val transferWindowsRepository: TransferWindowsRepository,
    private val scoutingMissionsRepository: ScoutingMissionsRepository,
    private val financesRepository: FinancesRepository,
    private val scoutAssignmentsRepository: ScoutAssignmentsRepository,
    private val presetRepository: PlayerFilterPresetRepository,
    private val gameManager: GameManager,
    private val currencyFormatter: CurrencyFormatter,
    private val filterEngine: TransferMarketFilterEngine,
    private val scoutingService: ScoutingIntelligenceService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransfersUiState())
    val uiState: StateFlow<TransfersUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(PlayerFilter())
    val filter: StateFlow<PlayerFilter> = _filter.asStateFlow()

    private val allPlayers = MutableStateFlow<List<PlayerSummary>>(emptyList())
    private val scoutReports = MutableStateFlow<Map<Int, ScoutAssignmentsEntity>>(emptyMap())
    private val teamsMap = MutableStateFlow<Map<String, TeamsEntity>>(emptyMap())
    private val leaguesMap = MutableStateFlow<Map<String, LeaguesEntity>>(emptyMap())

    init {
        observeGameManager()
        observeData()
        loadPresets()
    }

    private fun loadStaticData() {
        viewModelScope.launch {
            teamsRepository.getAllTeams().collect { teams ->
                teamsMap.value = teams.associateBy { it.name }
            }
        }
        viewModelScope.launch {
            leaguesRepository.getAllLeagues().collect { leagues ->
                leaguesMap.value = leagues.associateBy { it.name }
            }
        }
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    val currencyContext = currencyFormatter.getCurrentContext()
                    
                    _uiState.update { it.copy(
                        currencyContext = currencyContext,
                        isTransferWindowOpen = context.isTransferWindowOpen,
                        currentTeamName = context.teamName,
                        currentTeamId = context.teamId,
                        managerName = context.managerName,
                        managerPortrait = context.managerAvatar,
                        season = context.season,
                        competitionStatus = "CAF Champions League" // Mock for now
                    ) }
                    
                    loadStaticData()
                    refreshData(context.teamId)
                    analyzeSquadGaps(context.teamId)

                    // Fetch team details for confidence and form
                    val team = teamsRepository.getTeamById(context.teamId)
                    if (team != null) {
                        _uiState.update { it.copy(
                            boardConfidence = team.boardConfidence,
                            fanConfidence = team.fanSentiment,
                            reputation = team.reputation,
                            clubForm = team.formStreak.map { it.toString() }.takeLast(5),
                            teamLocation = "${team.country}, ${team.region}",
                            teamLogo = team.logoPath
                        ) }
                    }
                }
            }
        }

        viewModelScope.launch {
            combine(gameManager.currentFinances, gameManager.currentSquad) { finances, squad ->
                val currentWages = squad.sumOf { it.salary.toLong() }
                val upcomingRenewals = squad.filter { it.contractExpiry?.contains("2026") == true }
                
                _uiState.update { it.copy(
                    transferBudget = finances?.budget ?: 0,
                    wageBudget = finances?.wageBill ?: 0,
                    currentWageSpend = currentWages,
                    upcomingRenewalsCount = upcomingRenewals.size,
                    upcomingRenewalCost = upcomingRenewals.sumOf { it.salary.toLong() / 10 } // Estimated 10% increase
                ) }
            }.collect()
        }
    }

    private fun observeData() {
        combine(allPlayers, _filter, scoutReports, teamsMap, leaguesMap) { players, filter, reports, teams, leagues ->
            val filtered = filterEngine.filterPlayers(players, filter, reports)
            filtered.map { player ->
                val team = teams[player.teamName]
                val league = leagues[team?.league ?: ""]
                player.toUiModel(reports[player.id], team?.logoPath, league?.logo, league?.name)
            }
        }.onEach { filteredPlayers ->
            _uiState.update { it.copy(marketPlayers = filteredPlayers, isLoading = false) }
            getRecruitmentSuggestions() 
        }.launchIn(viewModelScope)
    }

    private fun loadPresets() {
        viewModelScope.launch {
            presetRepository.getAllPresets().collect { presets ->
                _uiState.update { it.copy(presets = presets) }
            }
        }
    }

    private fun refreshData(teamId: Int) {
        viewModelScope.launch {
            playersRepository.getAllPlayerSummaries().collect { players ->
                allPlayers.value = players
            }
        }

        viewModelScope.launch {
            scoutAssignmentsRepository.getAllAssignments().collect { assignments ->
                scoutReports.value = assignments.filter { it.isCompleted }.associateBy { it.playerId }
            }
        }

        viewModelScope.launch {
            scoutingMissionsRepository.getActiveMissions().collect { missions ->
                _uiState.update { it.copy(activeScoutInstructions = missions.map { it.toInstructionModel() }) }
            }
        }

        viewModelScope.launch {
            shortlistRepository.getShortlistedPlayers().collect { players ->
                _uiState.update { it.copy(shortlist = players.map { 
                    val team = teamsMap.value[it.teamName]
                    val league = leaguesMap.value[team?.league ?: ""]
                    it.toUiModel(scoutReports.value[it.id], team?.logoPath, league?.logo, league?.name) 
                }) }
            }
        }

        viewModelScope.launch {
            combine(
                transfersRepository.getIncomingTransfers(teamId),
                transfersRepository.getOutgoingTransfers(teamId)
            ) { incoming, outgoing ->
                _uiState.update { state ->
                    state.copy(
                        incomingOffers = incoming.filter { 
                            it.transferStatus != TransferStatus.COMPLETED.value && 
                            it.transferType != TransferType.LOAN.value 
                        }.map { it.toUiOfferModel() },
                        outgoingOffers = outgoing.map { it.toUiOfferModel() },
                        activeNegotiations = outgoing.filter { 
                            it.transferStatus == TransferStatus.NEGOTIATING.value || 
                            it.transferStatus == TransferStatus.PENDING.value ||
                            it.transferStatus == TransferStatus.ACCEPTED.value
                        }.map { it.toUiOfferModel() },
                        loanIncoming = incoming.filter { 
                            it.transferType == TransferType.LOAN.value && 
                            it.transferStatus != TransferStatus.COMPLETED.value 
                        }.map { it.toUiOfferModel() },
                        loanOutgoing = outgoing.filter { 
                            it.transferType == TransferType.LOAN.value && 
                            it.transferStatus != TransferStatus.COMPLETED.value 
                        }.map { it.toUiOfferModel() }
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            combine(
                newsRepository.getNewsByCategory("TRANSFER"),
                transfersRepository.getAllTransfers()
            ) { news, allTransfers ->
                val teamId = uiState.value.currentTeamId
                val myTransfers = allTransfers.filter { 
                    it.transferStatus == TransferStatus.COMPLETED.value && 
                    (it.currentTeamId == teamId || it.targetTeamId == teamId) 
                }
                
                val spent = myTransfers.filter { it.targetTeamId == teamId }.sumOf { it.transferFee }
                val received = myTransfers.filter { it.currentTeamId == teamId }.sumOf { it.transferFee }
                
                _uiState.update { it.copy(
                    transferNews = news,
                    completedDeals = myTransfers
                        .sortedByDescending { it.timestamp }
                        .map { it.toUiOfferModel() },
                    totalSpent = spent,
                    totalReceived = received,
                    netSpend = received - spent
                ) }
            }.collect()
        }
    }

    fun updateFilter(newFilter: PlayerFilter) {
        _filter.value = newFilter
        _uiState.update { it.copy(isLoading = true) }
    }

    fun resetFilter() {
        _filter.value = PlayerFilter()
    }

    fun saveCurrentFilter(name: String) {
        viewModelScope.launch {
            presetRepository.savePreset(name, _filter.value)
        }
    }

    fun loadPreset(preset: PlayerFilterPresetEntity) {
        val filter = presetRepository.parseFilter(preset.filterJson)
        _filter.value = filter
    }

    fun deletePreset(preset: PlayerFilterPresetEntity) {
        viewModelScope.launch {
            presetRepository.deletePreset(preset)
        }
    }

    private fun analyzeSquadGaps(teamId: Int) {
        viewModelScope.launch {
            playersRepository.getPlayersByTeamId(teamId).firstOrNull()?.let { squad ->
                val positions = listOf("GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "LW", "RW", "ST")
                val gaps = positions.filter { pos ->
                    squad.count { it.position == pos } < 2
                }
                
                val aiAdvice = generateRecruitmentAdvice(squad, gaps)
                
                _uiState.update { it.copy(squadGaps = gaps, recruitmentAdvice = aiAdvice) }
            }
        }
    }

    private fun generateRecruitmentAdvice(squad: List<PlayersEntity>, gaps: List<String>): List<String> {
        val advice = mutableListOf<String>()
        if (gaps.contains("GK")) advice.add("Urgent: You only have one reliable Goalkeeper. Look for a backup.")
        if (squad.count { it.age >= 32 } > 5) advice.add("Tactical: Your squad is aging. Consider signing players under 23 to ensure long-term stability.")
        if (squad.isEmpty()) return advice
        
        val avgRating = squad.map { it.rating }.average()
        if (avgRating < 70) advice.add("Performance: The average squad rating is low. You need high-impact 'Star Players' to compete.")
        
        return advice
    }

    fun getRecruitmentSuggestions() {
        viewModelScope.launch {
            val players = allPlayers.value
            val reports = scoutReports.value
            val currentTeam = uiState.value.currentTeamName
            
            val suggestions = players.asSequence()
                .filter { it.teamName != currentTeam }
                .map { player ->
                    val report = reports[player.id]
                    val score = scoutingService.calculateScoutScore(player, report, null, uiState.value.squadGaps)
                    player to score
                }
                .filter { it.second >= 60 } 
                .sortedByDescending { it.second }
                .take(10)
                .map { (player, score) ->
                    val team = teamsMap.value[player.teamName]
                    val league = leaguesMap.value[team?.league ?: ""]
                    RecruitmentSuggestion(
                        player = player.toUiModel(reports[player.id], team?.logoPath, league?.logo, league?.name),
                        scoutScore = score,
                        recommendation = scoutingService.getRecommendationLabel(score)
                    )
                }
                .toList()
            
            _uiState.update { it.copy(aiSuggestions = suggestions) }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun selectMarketView(view: MarketView) {
        _uiState.update { it.copy(selectedMarketView = view) }
        val currentFilter = _filter.value
        val newFilter = when (view) {
            MarketView.FOR_SALE -> currentFilter.copy(
                isTransferListed = true,
                isLoanListed = null,
                isFreeAgent = null,
                maxContractMonths = null
            )
            MarketView.EXPIRING -> currentFilter.copy(
                maxContractMonths = 6,
                isTransferListed = null,
                isLoanListed = null,
                isFreeAgent = null
            )
            MarketView.LOAN_LISTED -> currentFilter.copy(
                isLoanListed = true,
                isTransferListed = null,
                isFreeAgent = null,
                maxContractMonths = null
            )
            MarketView.FREE_AGENTS -> currentFilter.copy(
                isFreeAgent = true,
                isTransferListed = null,
                isLoanListed = null,
                maxContractMonths = null
            )
            MarketView.CAF_WINDOW -> currentFilter.copy(
                isTransferListed = null,
                isLoanListed = null,
                isFreeAgent = null,
                maxContractMonths = null
            )
            MarketView.ALL -> currentFilter.copy(
                isTransferListed = null,
                isLoanListed = null,
                isFreeAgent = null,
                maxContractMonths = null
            )
        }
        updateFilter(newFilter)
    }

    fun addToShortlist(playerId: Int) {
        viewModelScope.launch { shortlistRepository.addToShortlist(playerId) }
    }

    fun removeFromShortlist(playerId: Int) {
        viewModelScope.launch { shortlistRepository.removeFromShortlist(playerId) }
    }

    fun respondToOffer(offerId: Int, action: TransferAction) {
        viewModelScope.launch {
            when (action) {
                TransferAction.ACCEPT -> transfersRepository.acceptTransfer(offerId)
                TransferAction.REJECT -> transfersRepository.rejectTransfer(offerId)
                TransferAction.NEGOTIATE -> { }
            }
        }
    }

    fun adjustBudget(sliderPosition: Float) {
        val finances = gameManager.currentFinances.value ?: return
        
        // FM logic: Budget can be shifted between Transfer and Wage
        // Conversion: 1M Transfer ~= 20K Wage/wk (approx 50 weeks)
        val totalPool = finances.budget + (finances.wageBill * 50) 
        
        val newWageBill = (totalPool * sliderPosition / 50).toLong()
        val newBudget = (totalPool * (1 - sliderPosition)).toLong()
        
        _uiState.update { it.copy(
            transferBudget = newBudget,
            wageBudget = newWageBill
        ) }
    }

    private fun PlayersEntity.toUiModel(
        report: ScoutAssignmentsEntity? = null,
        clubLogo: String? = null,
        leagueLogo: String? = null,
        leagueName: String? = null
    ): TransferPlayerUiModel {
        val context = (gameManager.gameState.value as? GameManager.GameState.Active)?.context
        val currentYear = try {
            context?.season?.split("/")?.first()?.toInt() ?: 2025
        } catch (e: Exception) {
            2025
        }
        val expiryYear = contractExpiry?.split("-")?.firstOrNull()?.toIntOrNull() ?: (currentYear + 1)
        
        return TransferPlayerUiModel(
            id = id, name = name, age = age, position = position,
            nationality = nationality, club = teamName, rating = rating,
            potential = potential,
            value = marketValue.toLong(), wage = salary.toLong(),
            morale = morale, form = currentForm, injuryStatus = injuryStatus,
            scoutRating = report?.scoutRating,
            verdict = report?.verdict,
            isWonderkid = scoutingService.calculateWonderkidScore(this) >= 85,
            clubLogo = clubLogo,
            leagueLogo = leagueLogo,
            leagueName = leagueName,
            interestLevel = calculateInterestLevel(this, context),
            cafStatus = calculateCAFStatus(this),
            contractYears = (expiryYear - currentYear).coerceAtLeast(0),
            potentialRange = potentialGrade
        )
    }

    private fun PlayerSummary.toUiModel(
        report: ScoutAssignmentsEntity? = null,
        clubLogo: String? = null,
        leagueLogo: String? = null,
        leagueName: String? = null
    ): TransferPlayerUiModel {
        val context = (gameManager.gameState.value as? GameManager.GameState.Active)?.context
        val currentYear = try {
            context?.season?.split("/")?.first()?.toInt() ?: 2025
        } catch (e: Exception) {
            2025
        }
        val expiryYear = contractExpiry?.split("-")?.firstOrNull()?.toIntOrNull() ?: (currentYear + 1)

        return TransferPlayerUiModel(
            id = id, name = name, age = age, position = position,
            nationality = nationality, club = teamName, rating = rating,
            potential = potential,
            value = marketValue.toLong(), wage = salary.toLong(),
            morale = morale, form = currentForm, injuryStatus = injuryStatus,
            scoutRating = report?.scoutRating,
            verdict = report?.verdict,
            isWonderkid = scoutingService.calculateWonderkidScore(this) >= 85,
            clubLogo = clubLogo,
            leagueLogo = leagueLogo,
            leagueName = leagueName,
            interestLevel = calculateInterestLevel(this, context),
            cafStatus = calculateCAFStatus(this),
            contractYears = (expiryYear - currentYear).coerceAtLeast(0),
            potentialRange = "70-85" // Mock
        )
    }

    private fun calculateInterestLevel(player: PlayersEntity, context: GameManager.GameContext?): InterestLevel {
        if (context == null) return InterestLevel.UNSURE
        if (player.teamId == context.teamId) return InterestLevel.VERY_INTERESTED
        
        // Basic African context logic: Players usually want to join bigger clubs or stay in a good league
        val ratingDiff = context.teamId.let { /* Should fetch team rating */ 70 } - player.rating
        return when {
            player.freeAgent -> InterestLevel.VERY_INTERESTED
            player.isTransferListed -> InterestLevel.INTERESTED
            ratingDiff > 10 -> InterestLevel.VERY_INTERESTED
            ratingDiff < -10 -> InterestLevel.NOT_INTERESTED
            else -> InterestLevel.CAUTIOUS
        }
    }

    private fun calculateInterestLevel(player: PlayerSummary, context: GameManager.GameContext?): InterestLevel {
        if (context == null) return InterestLevel.UNSURE
        if (player.teamId == context.teamId) return InterestLevel.VERY_INTERESTED
        
        val ratingDiff = 70 - player.rating
        return when {
            player.freeAgent -> InterestLevel.VERY_INTERESTED
            player.isTransferListed -> InterestLevel.INTERESTED
            ratingDiff > 10 -> InterestLevel.VERY_INTERESTED
            ratingDiff < -10 -> InterestLevel.NOT_INTERESTED
            else -> InterestLevel.CAUTIOUS
        }
    }

    private fun calculateCAFStatus(player: PlayersEntity): CAFStatus {
        // African context: Champions League cup-tied is a big factor
        return when {
            player.retired -> CAFStatus.CONTRACT_EXPIRED
            player.isInjured -> CAFStatus.ELIGIBLE // Still eligible but injured
            player.rating > 80 && player.age < 23 -> CAFStatus.ITC_PENDING // High profile youth move often has ITC delay
            else -> CAFStatus.ELIGIBLE
        }
    }

    private fun calculateCAFStatus(player: PlayerSummary): CAFStatus {
        return when {
            player.retired -> CAFStatus.CONTRACT_EXPIRED
            player.isInjured -> CAFStatus.ELIGIBLE
            else -> CAFStatus.ELIGIBLE
        }
    }

    private fun TransfersEntity.toUiOfferModel() = TransferOfferUiModel(
        id = id, playerId = playerId, playerName = playerName,
        fromTeam = currentTeam, toTeam = targetTeam,
        fee = transferFee, status = transferStatus, type = transferType,
        timestamp = timestamp,
        fromTeamLogo = teamsMap.value[currentTeam]?.logoPath,
        toTeamLogo = teamsMap.value[targetTeam]?.logoPath,
        installments = installments,
        downPayment = if (installments > 0) transferFee / 2 else transferFee,
        contractYears = contractLength,
        weeklyWage = monthlyWage / 4,
        feeRelationToValue = when {
            transferFee == 0L -> FeeRelation.BARGAIN
            transferFee > 10_000_000 -> FeeRelation.PREMIUM
            else -> FeeRelation.MARKET_VALUE
        }
    )

    private fun ScoutingMissionsEntity.toInstructionModel() = ScoutInstruction(
        id = id.toString(),
        region = when {
            targetIdentifier.contains("West Africa", true) -> AfricanRegion.WEST_AFRICA
            targetIdentifier.contains("North Africa", true) -> AfricanRegion.NORTH_AFRICA
            targetIdentifier.contains("East Africa", true) -> AfricanRegion.EAST_AFRICA
            targetIdentifier.contains("Southern Africa", true) -> AfricanRegion.SOUTHERN_AFRICA
            targetIdentifier.contains("Central Africa", true) -> AfricanRegion.CENTRAL_AFRICA
            else -> null
        },
        specificCountries = if (missionType == MissionType.COUNTRY.value) listOf(targetIdentifier) else emptyList(),
        position = "Any",
        minAge = 16, maxAge = 35,
        quality = SquadQuality.FIRST_TEAM,
        durationWeeks = 4,
        costPerWeek = 2000,
        progress = try {
            val elapsed = System.currentTimeMillis() - startDate
            ((elapsed.toDouble() / (4 * 7 * 24 * 60 * 60 * 1000L)) * 100).toInt().coerceIn(0, 100)
        } catch (e: Exception) {
            0
        }
    )
}

data class TransfersUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val transferBudget: Long = 0,
    val wageBudget: Long = 0,
    val competitionStatus: String = "",
    val marketPlayers: List<TransferPlayerUiModel> = emptyList(),
    val shortlist: List<TransferPlayerUiModel> = emptyList(),
    val aiSuggestions: List<RecruitmentSuggestion> = emptyList(),
    val presets: List<PlayerFilterPresetEntity> = emptyList(),
    val squadGaps: List<String> = emptyList(),
    val recruitmentAdvice: List<String> = emptyList(),
    val activeNegotiations: List<TransferOfferUiModel> = emptyList(),
    val incomingOffers: List<TransferOfferUiModel> = emptyList(),
    val outgoingOffers: List<TransferOfferUiModel> = emptyList(),
    val loanIncoming: List<TransferOfferUiModel> = emptyList(),
    val loanOutgoing: List<TransferOfferUiModel> = emptyList(),
    val transferNews: List<NewsEntity> = emptyList(),
    val completedDeals: List<TransferOfferUiModel> = emptyList(),
    val activeScoutInstructions: List<ScoutInstruction> = emptyList(),
    val isLoading: Boolean = false,
    val isTransferWindowOpen: Boolean = false,
    val currentTeamName: String = "",
    val currentTeamId: Int = 0,
    val teamLocation: String = "",
    val teamLogo: String? = null,
    val managerName: String = "",
    val managerPortrait: String? = null,
    val reputation: Int = 50,
    val season: String = "",
    val boardConfidence: Int = 0,
    val fanConfidence: Int = 0,
    val clubForm: List<String> = emptyList(),
    val scoutingKnowledge: Int = 0,
    val registrationCount: Int = 0,
    val registrationLimit: Int = 0,
    val deadlineDay: String = "",
    val countdownTime: String = "",
    val currencyContext: CurrencyFormatter.CurrencyContext? = null,
    val selectedMarketView: MarketView = MarketView.ALL,
    val currentWageSpend: Long = 0,
    val upcomingRenewalCost: Long = 0,
    val upcomingRenewalsCount: Int = 0,
    val totalSpent: Long = 0,
    val totalReceived: Long = 0,
    val netSpend: Long = 0
)

data class TransferPlayerUiModel(
    val id: Int, val name: String, val age: Int, val position: String,
    val nationality: String, val club: String, val rating: Int, val potential: Int = 0,
    val value: Long, val wage: Long, val morale: Int, val form: Int,
    val injuryStatus: String, val height: Int = 180, val foot: String = "Right",
    val role: String = "Advanced Forward", val condition: Int = 100,
    val scoutRating: Int? = null,
    val verdict: String? = null,
    val isWonderkid: Boolean = false,
    val clubLogo: String? = null,
    val leagueLogo: String? = null,
    val leagueName: String? = null,
    val interestLevel: InterestLevel = InterestLevel.UNSURE,
    val cafStatus: CAFStatus = CAFStatus.ELIGIBLE,
    val contractYears: Int = 0,
    val potentialRange: String = ""
)

data class RecruitmentSuggestion(
    val player: TransferPlayerUiModel,
    val scoutScore: Int,
    val recommendation: String
)

data class TransferOfferUiModel(
    val id: Int, val playerId: Int, val playerName: String,
    val fromTeam: String, val toTeam: String, val fee: Long,
    val status: String, val type: String, val timestamp: Long,
    val fromTeamLogo: String? = null,
    val toTeamLogo: String? = null,
    val installments: Int = 0,
    val downPayment: Long = 0,
    val sponsorContribution: Long = 0,
    val thirdPartyInterest: Int = 0,
    val isDelayedPayment: Boolean = false,
    val playerNationality: String = "SEN",
    val playerPosition: String = "CM",
    val contractYears: Int = 0,
    val weeklyWage: Long = 0,
    val potentialRange: String = "",
    val feeRelationToValue: FeeRelation = FeeRelation.MARKET_VALUE
)

enum class FeeRelation {
    BARGAIN, MARKET_VALUE, PREMIUM, OVERPAID, BELOW_VALUE
}

data class ScoutInstruction(
    val id: String,
    val region: AfricanRegion?,
    val specificCountries: List<String>?,
    val position: String?,
    val minAge: Int,
    val maxAge: Int,
    val quality: SquadQuality,
    val durationWeeks: Int,
    val costPerWeek: Int,
    val progress: Int
)

enum class AfricanRegion {
    WEST_AFRICA, NORTH_AFRICA, EAST_AFRICA, SOUTHERN_AFRICA, CENTRAL_AFRICA
}

enum class InterestLevel {
    VERY_INTERESTED, INTERESTED, CAUTIOUS, NOT_INTERESTED, UNSURE
}

enum class CAFStatus {
    ITC_PENDING, ITC_COMPLETE, CHAMPIONS_LEAGUE_TIED, CONFEDERATION_TIED, ELIGIBLE, CONTRACT_EXPIRED
}

enum class SquadQuality { BACKUP, FIRST_TEAM, STAR_PLAYER }

enum class PlayingTimeRole {
    EMERGENCY_BACKUP, SQUAD_PLAYER, FIRST_TEAM, IMPORTANT_PLAYER, STAR_PLAYER
}

enum class TransferAction { ACCEPT, REJECT, NEGOTIATE }
