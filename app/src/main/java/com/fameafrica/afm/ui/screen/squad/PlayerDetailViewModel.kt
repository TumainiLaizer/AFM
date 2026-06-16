package com.fameafrica.afm.ui.screen.squad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.screen.transfers.TransferOfferDetails
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import com.fameafrica.afm.utils.NationalityUtils
import com.fameafrica.afm.utils.tactics.PlayerRoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SquadStatus {
    STAR_PLAYER, FIRST_TEAM, ROTATION, BACKUP, SURPLUS
}

enum class FormTrend {
    RISING, STABLE, DECLINING, VOLATILE
}

enum class TrendDirection {
    IMPROVING, DECLINING, STABLE
}

enum class TrainingIntensity(val value: String) {
    NORMAL("NORMAL")
}

enum class Sentiment {
    POSITIVE, NEUTRAL, NEGATIVE
}

@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    private val playersRepository: PlayersRepository,
    private val matchEventsRepository: MatchEventsRepository,
    private val playerContractsRepository: PlayerContractsRepository,
    private val agentClientsRepository: AgentClientsRepository,
    private val seasonAwardsRepository: SeasonAwardsRepository,
    private val interviewsRepository: InterviewsRepository,
    private val gameManager: GameManager,
    private val currencyFormatter: CurrencyFormatter,
    private val playerReactionsRepository: PlayerReactionsRepository,
    private val playerLoansRepository: PlayerLoansRepository,
    private val transfersRepository: TransfersRepository,
    private val transferWindowsRepository: TransferWindowsRepository,
    private val teamsRepository: TeamsRepository,
    private val newsRepository: NewsRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val shortlistRepository: ShortlistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerDetailUiState(isLoading = true))
    val uiState: StateFlow<PlayerDetailUiState> = _uiState.asStateFlow()

    private var currentPlayerId: Int = -1

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    _uiState.update { it.copy(
                        currentGameDate = state.context.gameDateDisplay,
                        currentTeamName = state.context.teamName
                    ) }
                    val id = currentPlayerId
                    if (id != -1) {
                        refreshPlayer(id)
                    }
                }
            }
        }
    }

    fun loadPlayer(playerId: Int) {
        if (currentPlayerId == playerId) return
        currentPlayerId = playerId
        val state = gameManager.gameState.value
        if (state is GameManager.GameState.Active) {
            refreshPlayer(playerId)
        }
    }

    fun formatCurrency(amount: Double): String {
        return currencyFormatter.formatEuroAmount(amount)
    }

    // ============ PLAYER MANAGEMENT FUNCTIONS ============

    fun setSquadStatus(playerId: Int, status: SquadStatus) {
        viewModelScope.launch {
            val player = playersRepository.getPlayerById(playerId) ?: return@launch
            val updatedPlayer = when (status) {
                SquadStatus.STAR_PLAYER -> player.copy(isStartingXi = true, morale = (player.morale + 5).coerceAtMost(100))
                SquadStatus.FIRST_TEAM -> player.copy(isStartingXi = true)
                SquadStatus.ROTATION -> player.copy(isStartingXi = false)
                SquadStatus.BACKUP -> player.copy(isStartingXi = false)
                SquadStatus.SURPLUS -> {
                    transfersRepository.createTransfer(
                        playerId = playerId,
                        targetTeamId = 0,
                        targetTeamName = "",
                        transferType = TransferType.BUY.value,
                        transferFee = (player.marketValue * 0.7).toLong(),
                        monthlyWage = player.salary.toLong(),
                        currentWeek = getCurrentWeek(),
                        contractLength = 1,
                        currentTeamId = player.teamId ?: 0
                    )
                    player.copy(isStartingXi = false)
                }
            }
            playersRepository.updatePlayer(updatedPlayer)
            refreshPlayer(playerId)

            playerReactionsRepository.addPlayerReaction(
                playerId = player.id,
                playerName = player.name,
                reactionType = when (status) {
                    SquadStatus.STAR_PLAYER -> PlayerReactionType.EXCITED.value
                    SquadStatus.SURPLUS -> PlayerReactionType.FRUSTRATED.value
                    else -> PlayerReactionType.THOUGHTFUL.value
                },
                reactionText = when (status) {
                    SquadStatus.STAR_PLAYER -> "I'm honored to be considered a star player for this team!"
                    SquadStatus.SURPLUS -> "I understand the decision. I'll look for new opportunities."
                    else -> "I'll work hard to earn my place in the team."
                }
            )
        }
    }

    fun addToTransferList(playerId: Int, askingPrice: Long) {
        viewModelScope.launch {
            val player = playersRepository.getPlayerById(playerId) ?: return@launch
            transfersRepository.createTransfer(
                playerId = playerId,
                targetTeamId = 0,
                targetTeamName = "",
                transferType = TransferType.BUY.value,
                transferFee = askingPrice,
                monthlyWage = (player.salary / 12).toLong(),
                currentWeek = getCurrentWeek(),
                contractLength = 1,
                currentTeamId = player.teamId ?: 0
            )

            val updatedPlayer = player.copy(transferListStatus = "AVAILABLE")
            playersRepository.updatePlayer(updatedPlayer)
            refreshPlayer(playerId)

            newsRepository.createTransferRumor(
                playerName = player.name,
                playerId = player.id,
                fromTeam = player.teamName,
                fromTeamId = player.teamId ?: 0,
                toTeam = "TBD",
                fee = askingPrice.toInt(),
                playerRating = player.rating
            )
        }
    }

    fun removeFromTransferList(playerId: Int) {
        viewModelScope.launch {
            val player = playersRepository.getPlayerById(playerId) ?: return@launch
            val updatedPlayer = player.copy(transferListStatus = "NOT_LISTED")
            playersRepository.updatePlayer(updatedPlayer)
            refreshPlayer(playerId)
        }
    }

    fun submitTransferOffer(offer: TransferOfferDetails) {
        viewModelScope.launch {
            val state = (gameManager.gameState.value as? GameManager.GameState.Active) ?: return@launch
            val player = playersRepository.getPlayerById(currentPlayerId) ?: return@launch
            transfersRepository.createTransfer(
                playerId = currentPlayerId,
                targetTeamId = state.context.teamId,
                targetTeamName = state.context.teamName,
                transferType = offer.type.value,
                transferFee = offer.fee,
                monthlyWage = offer.wage / 4,
                currentWeek = state.context.week,
                contractLength = offer.years,
                installments = offer.installments,
                sellOnPercentage = offer.sellOn,
                goalBonusFee = offer.goalBonus,
                signingBonus = offer.signingBonus,
                squadRole = offer.role.value,
                currentTeamId = player.teamId ?: 0
            )
            refreshPlayer(currentPlayerId)
        }
    }

    fun toggleShortlist() {
        viewModelScope.launch {
            val isShortlisted = uiState.value.isShortlisted
            if (isShortlisted) {
                shortlistRepository.removeFromShortlist(currentPlayerId)
            } else {
                shortlistRepository.addToShortlist(currentPlayerId)
            }
            _uiState.update { it.copy(isShortlisted = !isShortlisted) }
        }
    }

    fun offerNewContract(
        playerId: Int,
        wage: Double,
        years: Int,
        signingBonus: Int,
        releaseClause: Int? = null
    ) {
        viewModelScope.launch {
            val player = playersRepository.getPlayerById(playerId) ?: return@launch
            val currentContract = playerContractsRepository.getContractByPlayerId(playerId)

            if (currentContract != null) {
                playerContractsRepository.renewContract(
                    contractId = currentContract.id,
                    newSalary = wage,
                    newContractLength = years,
                    newReleaseClause = releaseClause
                )
            } else {
                playerContractsRepository.createContract(
                    playerName = player.name,
                    playerId = playerId,
                    teamName = player.teamName,
                    teamId = player.teamId ?: 0,
                    salary = wage.toInt(),
                    contractLength = years,
                    releaseClause = releaseClause ?: 500000000,
                    signingBonus = signingBonus
                )
            }

            val updatedPlayer = player.copy(salary = wage)
            playersRepository.updatePlayer(updatedPlayer)
            refreshPlayer(playerId)

            playerReactionsRepository.addHappyReaction(
                playerId = player.id,
                playerName = player.name,
                context = "CONTRACT"
            )
        }
    }

    private fun refreshPlayer(playerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val player = playersRepository.getPlayerById(playerId)
            if (player == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val currencyContext = currencyFormatter.getCurrentContext()
            val isUserPlayer = player.teamName == uiState.value.currentTeamName
            
            // Only load overview data initially
            val playerStatus = loadPlayerStatus(player)
            val attributes = PlayerAttributesUiModel.fromPlayersEntity(player)
            val attributeTrends = loadAttributeTrends(playerId)
            
            // Roles
            val roles = PlayerRoleManager.mapArchetypeToRoles(player.archetype)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isUserPlayer = isUserPlayer,
                    player = PlayerDetailUiModel(
                        id = player.id,
                        name = player.name,
                        age = player.age,
                        height = player.height,
                        position = player.position,
                        nationality = player.nationality,
                        nationalityFlag = NationalityUtils.getWavingFlagUrl(player.nationality),
                        shirtNumber = player.shirtNumber,
                        preferredFoot = player.preferredFoot,
                        overallRating = player.rating,
                        potential = player.potential,
                        form = player.currentForm,
                        morale = player.morale,
                        appearances = player.matches,
                        goals = player.goals,
                        assists = player.assists,
                        cleanSheets = player.cleanSheets,
                        isCaptain = player.isCaptain,
                        isViceCaptain = player.isViceCaptain,
                        marketValue = player.marketValue,
                        formattedValue = currencyFormatter.format(player.marketValue.toDouble(), currencyContext),
                        wage = player.salary,
                        formattedWage = currencyFormatter.formatWage(player.salary, currencyContext),
                        contractExpiry = player.contractExpiry,
                        injuryStatus = player.injuryStatus,
                        personality = player.personalityType,
                        archetype = player.archetype,
                        primaryRole = roles.first.displayName,
                        secondaryRole = roles.second.displayName,
                        experience = player.experience,
                        positionCategory = player.positionCategory,
                    ),
                    attributes = attributes,
                    playerStatus = playerStatus,
                    attributeTrends = attributeTrends
                )
            }
            
            // Load remaining data asynchronously without blocking UI update
            loadAdditionalTabData(playerId, currencyContext)
        }
    }

    private suspend fun loadAdditionalTabData(playerId: Int, currencyContext: CurrencyFormatter.CurrencyContext) {
        val player = playersRepository.getPlayerById(playerId) ?: return
        
        val contract = playerContractsRepository.getContractByPlayerId(playerId)
        val agent = agentClientsRepository.getAgentByPlayerId(playerId)
        val awards = seasonAwardsRepository.getPlayerAwards(playerId).firstOrNull() ?: emptyList()
        val interviews = interviewsRepository.getPlayerInterviews(playerId).firstOrNull() ?: emptyList()
        val reactions = playerReactionsRepository.getPlayerReactionsDashboard(playerId, player.name)
        val activeLoan = playerLoansRepository.getActiveLoanByPlayerId(playerId)
        val team = player.teamId?.let { teamsRepository.getTeamById(it) }
        val contractDashboard = playerContractsRepository.getTeamContractDashboard(player.teamName)
        val isShortlisted = shortlistRepository.isShortlisted(playerId).firstOrNull() ?: false

        val goals = matchEventsRepository.getPlayerGoalCount(playerId)
        val assists = matchEventsRepository.getPlayerAssistCount(playerId)
        val formHistory = generateFormHistory(playerId)
        val recentMatches = loadRecentMatches(playerId)
        val transferInterest = loadTransferInterest(playerId, currencyContext)
        val playerEvents = matchEventsRepository.getEventsByPlayer(playerId).firstOrNull() ?: emptyList()
        val shots = playerEvents.count { it.eventType in listOf("SHOT", "SHOT_ON_TARGET", "GOAL") }

        _uiState.update {
            it.copy(
                isShortlisted = isShortlisted,
                seasonStats = SeasonStatsUiModel(
                    matches = player.matches, goals = goals, assists = assists,
                    manOfMatch = player.manOfMatch, yellowCards = player.yellowCards,
                    redCards = player.redCards, passAccuracy = 85, tackles = 0,
                    shots = shots, shotsOnTarget = 0, fouls = 0, offsides = 0,
                    minutesPlayed = player.matches * 90, expectedGoals = 0.0,
                    expectedAssists = 0.0, goalConversionRate = if (shots > 0) (goals * 100 / shots) else 0,
                    cleanSheets = player.cleanSheets
                ),
                contract = contract?.let { c ->
                    ContractUiModel(
                        salary = c.salary.toLong(),
                        formattedSalary = currencyFormatter.formatWage(c.salary.toDouble(), currencyContext),
                        expiry = c.contractEndDate,
                        isExpiring = c.contractStatus == "EXPIRING",
                        releaseClause = c.releaseClause,
                        formattedReleaseClause = currencyFormatter.format(c.releaseClause.toDouble(), currencyContext),
                        signingBonus = c.signingBonus ?: 0,
                        formattedSigningBonus = currencyFormatter.format(c.signingBonus?.toDouble() ?: 0.0, currencyContext),
                        isNegotiable = c.isNegotiable
                    )
                },
                agent = agent?.let { a ->
                    AgentUiModel(
                        name = a.agentName,
                        agency = a.agency ?: "",
                        negotiationPower = a.negotiationPower,
                        commissionRate = a.commissionRate.toDouble(),
                        reputation = a.reputation,
                        yearsExperience = a.yearsExperience,
                        successfulDeals = a.successfulDeals,
                        totalDealValue = a.totalDealValue
                    )
                },
                careerAwards = awards.map { a ->
                    AwardUiModel(
                        awardType = a.awardType,
                        season = a.season,
                        category = a.awardCategory,
                        description = a.description ?: "",
                        prizeMoney = a.prizeMoney ?: 0,
                        formattedPrizeMoney = currencyFormatter.format(a.prizeMoney?.toDouble() ?: 0.0, currencyContext)
                    )
                },
                recentInterviews = interviews.take(5).map { i ->
                    InterviewUiModel(
                        journalistName = i.journalistName,
                        date = i.dateRequested,
                        topic = i.topic,
                        responseType = i.responseType,
                        impactOnMorale = i.impactOnMorale
                    )
                },
                injuryHistory = if (player.isInjured) listOf(
                    InjuryUiModel(
                        type = player.injuryStatus,
                        severity = "MODERATE",
                        date = player.updatedAt,
                        days = player.recoveryTime,
                        injuryStatus = player.injuryStatus,
                        recoveryTime = player.recoveryTime
                    )
                ) else emptyList(),
                formHistory = formHistory,
                recentMatches = recentMatches,
                transferInterest = transferInterest,
                playerReactions = reactions,
                activeLoan = activeLoan?.let { loan ->
                    LoanUiModel(
                        receivingTeam = loan.receivingTeam,
                        duration = loan.duration,
                        optionToBuy = loan.optionToBuy,
                        formattedBuyOptionFee = currencyFormatter.format(loan.buyOptionFee?.toDouble() ?: 0.0, currencyContext)
                    )
                },
                teamInfo = team,
                contractDashboard = contractDashboard
            )
        }
    }

    private suspend fun loadPlayerStatus(player: PlayersEntity): PlayerStatusUiModel {
        val activeLoan = playerLoansRepository.getActiveLoanByPlayerId(player.id)
        val lastMatchDate = getLastMatchDate(player.id)
        val consecutiveStartsCount = getConsecutiveStarts(player.id)

        return PlayerStatusUiModel(
            condition = calculateCondition(player),
            fatigue = calculateFatigue(player),
            sharpness = calculateSharpness(player),
            happiness = determineHappiness(player),
            squadStatus = determineSquadStatus(player),
            isOnTransferList = player.isTransferListed,
            isLoanedOut = activeLoan != null,
            parentClub = activeLoan?.loaningTeam,
            daysSinceLastMatch = lastMatchDate?.let { daysSinceFix(it) } ?: 0,
            consecutiveStarts = consecutiveStartsCount,
            formTrend = calculateFormTrend(getFormHistoryFromMatches(player.id))
        )
    }

    private suspend fun loadRecentMatches(playerId: Int): List<MatchPerformanceUiModel> {
        val player = playersRepository.getPlayerById(playerId) ?: return emptyList()
        val teamResults = player.teamId?.let { fixturesResultsRepository.getRecentResultsByTeam(it, 5).firstOrNull() } ?: return emptyList()

        val performances = mutableListOf<MatchPerformanceUiModel>()

        for (result in teamResults) {
            val playerEvents = matchEventsRepository.getEventsByPlayer(playerId).firstOrNull() ?: continue
            val matchEvents = playerEvents.filter { it.matchId == result.fixtureId }

            val goalsCount = matchEvents.count { it.eventType == "GOAL" }
            val assistsCount = matchEvents.count { it.eventType == "ASSIST" }
            val shotsCount = matchEvents.count { it.eventType == "SHOT" }
            val shotsOnTargetCount = matchEvents.count { it.eventType == "SHOT_ON_TARGET" }
            val tacklesCount = matchEvents.count { it.eventType == "TACKLE" }
            val interceptionsCount = matchEvents.count { it.eventType == "INTERCEPTION" }
            val dribblesCount = matchEvents.count { it.eventType == "DRIBBLE" }

            val passAccuracyVal = if (matchEvents.any { it.eventType == "PASS" }) 85 else 0

            val isHomeMatch = result.homeTeam == player.teamName
            val opponentName = if (isHomeMatch) result.awayTeam else result.homeTeam

            performances.add(
                MatchPerformanceUiModel(
                    matchId = result.fixtureId,
                    opponent = opponentName,
                    date = result.matchDate,
                    rating = calculateMatchRating(goalsCount, assistsCount, shotsOnTargetCount, tacklesCount, result),
                    goals = goalsCount,
                    assists = assistsCount,
                    keyPasses = matchEvents.count { it.eventType == "KEY_PASS" },
                    tackles = tacklesCount,
                    interceptions = interceptionsCount,
                    dribblesCompleted = dribblesCount,
                    passAccuracy = passAccuracyVal,
                    shotAccuracy = if (shotsCount > 0) (shotsOnTargetCount * 100 / shotsCount) else 0,
                    motm = result.manOfMatch == player.name
                )
            )
        }

        return performances
    }

    private suspend fun loadAttributeTrends(playerId: Int): Map<String, AttributeTrend> {
        val player = playersRepository.getPlayerById(playerId) ?: return emptyMap()
        val currentAttributes = PlayerAttributesUiModel.fromPlayersEntity(player)

        return mapOf(
            "finishing" to createTrend(currentAttributes.finishing - 2, currentAttributes.finishing),
            "pace" to createTrend(currentAttributes.pace - 1, currentAttributes.pace),
            "stamina" to createTrend(currentAttributes.stamina - 3, currentAttributes.stamina),
            "defending" to createTrend(currentAttributes.defending - 1, currentAttributes.defending),
            "passing" to createTrend(currentAttributes.passing - 1, currentAttributes.passing)
        )
    }

    private suspend fun loadTransferInterest(playerId: Int, currencyContext: CurrencyFormatter.CurrencyContext): List<TransferInterestUiModel> {
        val player = playersRepository.getPlayerById(playerId) ?: return emptyList()
        val incoming = player.teamId?.let { transfersRepository.getIncomingTransfers(it).firstOrNull() } ?: return emptyList()

        return incoming.filter { it.transferStatus == TransferStatus.PENDING.value }
            .map { transfer ->
                TransferInterestUiModel(
                    clubName = transfer.currentTeam,
                    clubReputation = 70, 
                    offeredAmount = transfer.transferFee,
                    formattedAmount = currencyFormatter.format(transfer.transferFee.toDouble(), currencyContext),
                    status = when (transfer.transferStatus) {
                        TransferStatus.PENDING.value -> InterestStatus.INQUIRY
                        TransferStatus.NEGOTIATING.value -> InterestStatus.NEGOTIATING
                        TransferStatus.ACCEPTED.value -> InterestStatus.FIRM_OFFER
                        else -> InterestStatus.INQUIRY
                    },
                    expiryDate = getTransferWindowEndDate()
                )
            }
    }

    private suspend fun generateFormHistory(playerId: Int): List<FormHistoryEntry> {
        val player = playersRepository.getPlayerById(playerId) ?: return emptyList()
        val teamResults = player.teamId?.let { fixturesResultsRepository.getRecentResultsByTeam(it, 10).firstOrNull() } ?: return emptyList()

        return teamResults.mapIndexed { index, result ->
            val playerEvents = matchEventsRepository.getEventsByPlayer(playerId).firstOrNull() ?: emptyList()
            val matchEvents = playerEvents.filter { it.matchId == result.fixtureId }

            val goalsCount = matchEvents.count { it.eventType == "GOAL" }
            val assistsCount = matchEvents.count { it.eventType == "ASSIST" }
            val motmStatus = result.manOfMatch == player.name

            FormHistoryEntry(
                matchNumber = index + 1,
                rating = calculateMatchRating(goalsCount, assistsCount, 0, 0, result),
                opponent = if (result.homeTeam == player.teamName) result.awayTeam else result.homeTeam,
                date = result.matchDate,
                goals = goalsCount,
                assists = assistsCount,
                motm = motmStatus
            )
        }
    }

    private fun calculateMatchRating(
        goals: Int,
        assists: Int,
        shotsOnTarget: Int,
        tackles: Int,
        result: FixturesResultsEntity
    ): Int {
        var ratingVal = 6.0 
        ratingVal += goals * 1.5
        ratingVal += assists * 1.0
        ratingVal += shotsOnTarget * 0.3
        ratingVal += tackles * 0.2

        val teamScoredCount = result.homeScore
        val teamConcededCount = result.awayScore

        if (teamScoredCount > teamConcededCount) {
            ratingVal += 0.5 
        } else if (teamScoredCount < teamConcededCount) {
            ratingVal -= 0.3 
        }

        return ratingVal.coerceIn(1.0, 10.0).toInt()
    }

    private suspend fun getLastMatchDate(playerId: Int): String? {
        val player = playersRepository.getPlayerById(playerId) ?: return null
        val results = player.teamId?.let { fixturesResultsRepository.getRecentResultsByTeam(it, 1).firstOrNull() }
        return results?.firstOrNull()?.matchDate
    }

    private suspend fun getConsecutiveStarts(playerId: Int): Int {
        val player = playersRepository.getPlayerById(playerId) ?: return 0
        return if (player.isStartingXi) 5 else 0
    }

    private suspend fun getFormHistoryFromMatches(playerId: Int): List<Int> {
        val player = playersRepository.getPlayerById(playerId) ?: return emptyList()
        val results = player.teamId?.let { fixturesResultsRepository.getRecentResultsByTeam(it, 5).firstOrNull() } ?: return emptyList()

        return results.map { 7 } 
    }

    private fun calculateCondition(player: PlayersEntity): Int {
        val fatiguePenalty = (100 - player.stamina) / 2
        val injuryPenalty = if (player.isInjured) 50 else 0
        return (100 - fatiguePenalty - injuryPenalty).coerceIn(0, 100)
    }

    private fun calculateFatigue(player: PlayersEntity): Int {
        return (100 - player.stamina).coerceIn(0, 100)
    }

    private fun calculateSharpness(player: PlayersEntity): Int {
        val matchSharpness = (player.matches * 5).coerceAtMost(50)
        val trainingSharpness = (player.experience / 2).coerceAtMost(50)
        return (matchSharpness + trainingSharpness).coerceIn(0, 100)
    }

    private fun determineHappiness(player: PlayersEntity): HappinessLevel {
        return when {
            player.morale >= 80 -> HappinessLevel.VERY_HAPPY
            player.morale >= 60 -> HappinessLevel.HAPPY
            player.morale >= 40 -> HappinessLevel.CONTENT
            player.morale >= 20 -> HappinessLevel.UNHAPPY
            else -> HappinessLevel.FURIOUS
        }
    }

    private fun determineSquadStatus(player: PlayersEntity): SquadStatus {
        return when {
            player.isCaptain || player.isViceCaptain -> SquadStatus.STAR_PLAYER
            player.isStartingXi && player.matches >= 25 -> SquadStatus.FIRST_TEAM
            player.matches >= 15 -> SquadStatus.ROTATION
            player.matches >= 5 -> SquadStatus.BACKUP
            else -> SquadStatus.SURPLUS
        }
    }

    private fun calculateFormTrend(formHistory: List<Int>): FormTrend {
        if (formHistory.size < 5) return FormTrend.VOLATILE

        val recentAvg = formHistory.takeLast(3).average()
        val previousAvg = formHistory.takeLast(6).take(3).average()

        return when {
            recentAvg > previousAvg + 5 -> FormTrend.RISING
            recentAvg < previousAvg - 5 -> FormTrend.DECLINING
            else -> FormTrend.STABLE
        }
    }

    private fun createTrend(historicalValue: Int, currentValue: Int): AttributeTrend {
        val change = currentValue - historicalValue
        return AttributeTrend(
            attributeName = "",
            currentValue = currentValue,
            previousValue = historicalValue,
            change = change,
            trend = when {
                change > 0 -> TrendDirection.IMPROVING
                change < 0 -> TrendDirection.DECLINING
                else -> TrendDirection.STABLE
            }
        )
    }

    private fun calculateTrainingProgress(player: PlayersEntity): Int {
        return (player.experience % 100).coerceAtMost(100)
    }

    private fun daysSinceFix(date: String): Int {
        return date.length * 0 // Dummy use to avoid warning
    }

    private fun getCurrentWeek(): Int {
        val state = gameManager.gameState.value
        return (state as? GameManager.GameState.Active)?.context?.week ?: 1
    }

    private fun getTransferWindowEndDate(): String {
        return "2024-12-31"
    }
}

data class PlayerDetailUiState(
    val isLoading: Boolean = true,
    val player: PlayerDetailUiModel? = null,
    val attributes: PlayerAttributesUiModel? = null,
    val formHistory: List<FormHistoryEntry> = emptyList(),
    val seasonStats: SeasonStatsUiModel? = null,
    val contract: ContractUiModel? = null,
    val injuryHistory: List<InjuryUiModel> = emptyList(),
    val agent: AgentUiModel? = null,
    val careerAwards: List<AwardUiModel> = emptyList(),
    val recentInterviews: List<InterviewUiModel> = emptyList(),
    val currentGameDate: String = "",
    val currentTeamName: String = "",
    val isUserPlayer: Boolean = false,
    val isShortlisted: Boolean = false,

    // Enhanced features
    val playerStatus: PlayerStatusUiModel? = null,
    val attributeTrends: Map<String, AttributeTrend> = emptyMap(),
    val recentMatches: List<MatchPerformanceUiModel> = emptyList(),
    val comparisonPlayer: PlayerDetailUiModel? = null,
    val transferInterest: List<TransferInterestUiModel> = emptyList(),
    val injuryRisk: InjuryRiskUiModel? = null,
    val trainingProgress: TrainingProgressUiModel? = null,
    val relationships: List<PlayerRelationshipUiModel> = emptyList(),
    val mediaBuzz: List<MediaBuzzUiModel> = emptyList(),
    val playerReactions: PlayerReactionsDashboard? = null,
    val activeLoan: LoanUiModel? = null,
    val teamInfo: TeamsEntity? = null,
    val contractDashboard: TeamContractDashboard? = null,
    val transferWindowOpen: Boolean = false,
    val teamForm: TeamForm? = null,
    val showGrowthAnimation: Boolean = false,
    val trainingOutcome: TrainingOutcomeUiModel? = null
)

data class TrainingOutcomeUiModel(
    val moraleEffect: Int,
    val fatigueImpact: Int,
    val injuryProbability: Int,
    val focusArea: String,
    val intensity: TrainingIntensity
)

data class PlayerDetailUiModel(
    val id: Int,
    val name: String,
    val age: Int,
    val height: Int,
    val position: String,
    val nationality: String,
    val nationalityFlag: String?,
    val shirtNumber: Int,
    val preferredFoot: String,
    val overallRating: Int,
    val potential: Int,
    val form: Int,
    val morale: Int,
    val appearances: Int,
    val goals: Int,
    val assists: Int,
    val cleanSheets: Int,
    val isCaptain: Boolean,
    val isViceCaptain: Boolean,
    val marketValue: Int,
    val formattedValue: String,
    val wage: Double,
    val formattedWage: String,
    val contractExpiry: String?,
    val injuryStatus: String,
    val personality: String,
    val archetype: String?,
    val primaryRole: String?,
    val secondaryRole: String?,
    val experience: Int,
    val positionCategory: String
) {
    val displayName: String
        get() {
            val parts = name.trim().split("\\s+".toRegex())
            if (parts.size <= 1) return name
            val initials = parts.dropLast(1).joinToString("") { it.take(1).uppercase() + "." }
            return "$initials${parts.last()}"
        }
}

data class PlayerAttributesUiModel(
    val finishing: Int,
    val passing: Int,
    val dribbling: Int,
    val crossing: Int,
    val heading: Int,
    val longShots: Int,
    val defending: Int,
    val skill: Int,
    val pace: Int,
    val stamina: Int,
    val strength: Int,
    val acceleration: Int,
    val agility: Int,
    val composure: Int,
    val decisions: Int,
    val leadership: Int,
    val vision: Int,
    val workRate: String,
    val positioning: Int,
    val anticipation: Int,
    val creativity: Int,
    val teamwork: Int,
    val aggression: Int,
    val kicking: Int,
    val handling: Int,
    val reflexes: Int,
    val goalkeeping: Int,
    val aerialAbility: Int,
    val commandOfArea: Int
) {
    companion object {
        fun fromPlayersEntity(player: PlayersEntity): PlayerAttributesUiModel {
            return PlayerAttributesUiModel(
                finishing = player.finishing,
                passing = player.passing,
                dribbling = player.dribbling,
                crossing = player.crossing,
                heading = player.heading,
                longShots = player.longShots,
                defending = player.defending,
                skill = player.skill,
                pace = player.pace,
                stamina = player.stamina,
                strength = player.strength,
                acceleration = player.acceleration,
                agility = player.agility,
                composure = player.composure,
                decisions = player.decisions,
                leadership = player.leadership,
                vision = player.vision,
                workRate = player.workRate,
                positioning = player.positioning,
                anticipation = player.anticipation,
                creativity = player.creativity,
                teamwork = player.teamwork,
                aggression = player.aggression,
                kicking = player.kicking,
                handling = player.handling,
                reflexes = player.reflexes,
                aerialAbility = player.aerialAbility,
                goalkeeping = player.goalkeeping,
                commandOfArea = player.commandOfArea
            )
        }
    }
}

data class SeasonStatsUiModel(
    val matches: Int,
    val goals: Int,
    val assists: Int,
    val manOfMatch: Int,
    val yellowCards: Int,
    val redCards: Int,
    val passAccuracy: Int,
    val tackles: Int,
    val shots: Int,
    val shotsOnTarget: Int,
    val fouls: Int,
    val offsides: Int,
    val minutesPlayed: Int,
    val expectedGoals: Double,
    val expectedAssists: Double,
    val goalConversionRate: Int,
    val cleanSheets: Int
)

data class ContractUiModel(
    val salary: Long,
    val formattedSalary: String,
    val expiry: String,
    val isExpiring: Boolean,
    val releaseClause: Int,
    val formattedReleaseClause: String,
    val signingBonus: Int,
    val formattedSigningBonus: String,
    val isNegotiable: Boolean
)

data class AgentUiModel(
    val name: String,
    val agency: String,
    val negotiationPower: Int,
    val commissionRate: Double,
    val reputation: Int,
    val yearsExperience: Int,
    val successfulDeals: Int,
    val totalDealValue: Long
)

data class AwardUiModel(
    val awardType: String,
    val season: String,
    val category: String,
    val description: String,
    val prizeMoney: Int,
    val formattedPrizeMoney: String
)

data class InterviewUiModel(
    val journalistName: String,
    val date: String,
    val topic: String,
    val responseType: String?,
    val impactOnMorale: Int
)

data class InjuryUiModel(
    val type: String,
    val severity: String,
    val date: String,
    val days: Int,
    val injuryStatus: String,
    val recoveryTime: Int
)

data class FormHistoryEntry(
    val matchNumber: Int,
    val rating: Int,
    val opponent: String,
    val date: String,
    val goals: Int,
    val assists: Int,
    val motm: Boolean
)

data class PlayerStatusUiModel(
    val condition: Int,
    val fatigue: Int,
    val sharpness: Int,
    val happiness: HappinessLevel,
    val squadStatus: SquadStatus,
    val isOnTransferList: Boolean,
    val isLoanedOut: Boolean,
    val parentClub: String?,
    val daysSinceLastMatch: Int,
    val consecutiveStarts: Int,
    val formTrend: FormTrend
)

enum class HappinessLevel {
    VERY_HAPPY, HAPPY, CONTENT, UNHAPPY, FURIOUS
}

data class AttributeTrend(
    val attributeName: String,
    val currentValue: Int,
    val previousValue: Int,
    val change: Int,
    val trend: TrendDirection
)

data class MatchPerformanceUiModel(
    val matchId: Int,
    val opponent: String,
    val date: String,
    val rating: Int,
    val goals: Int,
    val assists: Int,
    val keyPasses: Int,
    val tackles: Int,
    val interceptions: Int,
    val dribblesCompleted: Int,
    val passAccuracy: Int,
    val shotAccuracy: Int,
    val motm: Boolean
)

data class TransferInterestUiModel(
    val clubName: String,
    val clubReputation: Int,
    val offeredAmount: Long,
    val formattedAmount: String,
    val status: InterestStatus,
    val expiryDate: String
)

data class InjuryRiskUiModel(
    val riskLevel: RiskLevel,
    val fatigueFactor: Int,
    val previousInjuries: Int,
    val recommendedRestDays: Int,
    val vulnerableAreas: List<String>
)

enum class RiskLevel {
    LOW, HIGH, CRITICAL
}

data class TrainingProgressUiModel(
    val overallProgress: Int,
    val recentImprovements: List<TrainingImprovement>,
    val focusArea: String?,
    val intensity: TrainingIntensity,
    val satisfaction: Int
)

data class TrainingImprovement(
    val attribute: String,
    val improvement: Int,
    val date: String
)

data class PlayerRelationshipUiModel(
    val playerId: Int,
    val playerName: String,
    val relationshipType: RelationshipType,
    val strength: Int
)

data class MediaBuzzUiModel(
    val headline: String,
    val source: String,
    val sentiment: Sentiment,
    val impactOnMorale: Int,
    val date: String
)

data class LoanUiModel(
    val receivingTeam: String,
    val duration: Int,
    val optionToBuy: Boolean,
    val formattedBuyOptionFee: String
)
