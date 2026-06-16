package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.TransfersDao
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TransfersRepository @Inject constructor(
    private val transfersDaoProvider: Provider<TransfersDao>,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val newsRepository: NewsRepository,
    private val transferWindowsRepository: TransferWindowsRepository,
    private val gameDateManager: GameDateManager,
    private val financesRepository: FinancesRepository,
    private val clubVisionRepository: ClubVisionRepository,
    private val fanReactionsRepository: FanReactionsRepository,
    private val clubDNARepository: ClubDNARepository,
    private val managersRepository: ManagersRepository,
    private val pressConferencesRepository: PressConferencesRepository
) {

    private val transfersDao get() = transfersDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllTransfers(): Flow<List<TransfersEntity>> = transfersDao.getAll()

    suspend fun getTransferById(id: Int): TransfersEntity? = transfersDao.getById(id)

    suspend fun insertTransfer(transfer: TransfersEntity) = transfersDao.insert(transfer)

    suspend fun updateTransfer(transfer: TransfersEntity) = transfersDao.update(transfer)

    suspend fun deleteTransfer(transfer: TransfersEntity) = transfersDao.delete(transfer)

    // ============ TRANSFER CREATION ============

    suspend fun createTransfer(
        playerId: Int,
        targetTeamId: Int,
        targetTeamName: String,
        transferType: String,
        transferFee: Long,
        monthlyWage: Long,
        currentWeek: Int,
        currentTeamId: Int,
        contractLength: Int = 3,
        installments: Int = 0,
        sellOnPercentage: Int = 0,
        goalBonusFee: Long = 0,
        appearanceBonusFee: Long = 0,
        trophyBonusFee: Long = 0,
        signingBonus: Long = 0,
        squadRole: String = SquadRole.ROTATION.value,
        isLoanToBuy: Boolean = false,
        loanBuyFee: Long? = null,
        agentFee: Long? = null,
        isUserOffer: Boolean = true
    ): TransfersEntity? {

        val player = playersRepository.getPlayerById(playerId) ?: return null
        val currentTeamName = player.teamName

        val currentWindow = transferWindowsRepository.getCurrentWindow()
        if (currentWindow == null && transferType != TransferType.FREE.value) {
            return null 
        }

        val transfer = TransfersEntity(
            playerId = playerId,
            playerName = player.name,
            currentTeam = currentTeamName,
            currentTeamId = currentTeamId,
            targetTeam = targetTeamName,
            targetTeamId = targetTeamId,
            transferFee = transferFee,
            contractLength = contractLength,
            monthlyWage = monthlyWage,
            transferType = transferType,
            transferStatus = TransferStatus.PENDING.value,
            windowId = currentWindow?.id,
            installments = installments,
            sellOnPercentage = sellOnPercentage,
            goalBonusFee = goalBonusFee,
            appearanceBonusFee = appearanceBonusFee,
            trophyBonusFee = trophyBonusFee,
            signingBonus = signingBonus,
            squadRole = squadRole,
            isLoanToBuy = isLoanToBuy,
            loanBuyFee = loanBuyFee,
            agentFee = agentFee,
            isUserOffer = isUserOffer,
            lastActionDate = System.currentTimeMillis()
        )

        transfersDao.insert(transfer)

        if (!isUserOffer && targetTeamId == currentTeamId) return null // Safety

        if (transferFee >= 5_000_000 || player.rating >= 75) {
            generateTransferRumour(transfer)
        }

        return transfer
    }

    // ============ AI DECISION ENGINE ============

    suspend fun processAITransfers(season: String, currentWeek: Int, userTeamId: Int): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        val currentWindow = transferWindowsRepository.getCurrentWindow() ?: return emptyList<SimulationEvent>()
        
        // Optimize: Fetch subset of teams and available players once outside the loop
        val topTeamsPool = teamsRepository.getTopTeamsByElo(300).firstOrNull() ?: emptyList()
        val aiTeams = topTeamsPool.filter { it.id != userTeamId }.shuffled().take(20)
        val allAvailablePlayers = playersRepository.getTransferListed().firstOrNull() ?: emptyList()
        
        for (team in aiTeams) {
            val finances = financesRepository.getTeamFinances(team.id, season) ?: continue
            val dna = clubDNARepository.getClubDNA(team.id)
            val players = playersRepository.getPlayersByTeamId(team.id).firstOrNull() ?: emptyList()
            
            if (players.isEmpty()) continue

            // DNA Influenced Logic
            val budget = finances.budget
            val budgetUtilizationLimit = when (dna?.financialBehavior) {
                FinancialBehavior.SPENDER -> 0.95
                FinancialBehavior.FRUGAL -> 0.30
                FinancialBehavior.PLAYER_SALES_DEPENDENT -> 0.50
                FinancialBehavior.RISKY -> 1.20
                FinancialBehavior.LOW_REVENUE_SURVIVAL -> 0.15
                else -> 0.70
            }

            val maxTransferSpend = (budget * budgetUtilizationLimit).toLong()

            // Buying Chance modified by DNA
            val buyingChance = when (dna?.financialBehavior) {
                FinancialBehavior.SPENDER -> 65
                FinancialBehavior.RISKY -> 50
                FinancialBehavior.GOVERNMENT_BACKED -> 45
                FinancialBehavior.FRUGAL -> 15
                FinancialBehavior.LOW_REVENUE_SURVIVAL -> 5
                else -> 30
            }

            // Selling Behavior check
            val isSalesDependent = dna?.financialBehavior == FinancialBehavior.PLAYER_SALES_DEPENDENT
            if (Random.nextInt(100) < (if (dna?.transferPolicy == "EXPORT_FOCUSED" || isSalesDependent) 40 else 10)) {
                val bestPlayers = players.sortedByDescending { it.rating }.take(5)
                val seller = bestPlayers.randomOrNull()
                if (seller != null && seller.marketValue > 0) {
                     // AI scout finding this player
                     val potentialBuyers = topTeamsPool.filter { it.id != team.id && it.reputation > team.reputation }.shuffled().take(3)
                     for (buyerTeam in potentialBuyers) {
                         val buyerFinances = financesRepository.getTeamFinances(buyerTeam.id, season)
                         if (buyerFinances != null && buyerFinances.budget > seller.marketValue) {
                             val transfer = createTransfer(
                                playerId = seller.id,
                                targetTeamId = buyerTeam.id,
                                targetTeamName = buyerTeam.name,
                                currentTeamId = team.id,
                                transferType = TransferType.BUY.value,
                                transferFee = (seller.marketValue * Random.nextDouble(1.1, 1.4)).toLong(),
                                monthlyWage = (seller.salary * 1.2 / 12).toLong(),
                                currentWeek = currentWeek,
                                isUserOffer = false
                             )
                             
                             if (transfer != null && buyerTeam.id == userTeamId) {
                                 events.add(SimulationEvent.TransferOffer(
                                     playerName = seller.name,
                                     offeringTeam = team.name,
                                     fee = transfer.transferFee
                                 ))
                             }
                             break
                         }
                     }
                }
            }

            // Buying condition check
            val isSpender = dna?.financialBehavior == FinancialBehavior.SPENDER
            if ((players.size < 28 || isSpender) && Random.nextInt(100) < buyingChance) {
                var targetPlayers = allAvailablePlayers.filter { 
                    it.teamId != team.id && it.marketValue < maxTransferSpend
                }

                // Apply Transfer Policy Filters
                targetPlayers = when (dna?.transferPolicy) {
                    "YOUTH", "ACADEMY_TO_FIRST_TEAM" -> targetPlayers.filter { it.age < 23 }
                    "LOCAL_TALENT_ONLY" -> targetPlayers.filter { it.nationality == team.country }
                    "EXPORT_FOCUSED" -> targetPlayers.filter { it.age < 25 && it.potential > 75 }
                    "FREE_AGENT_FOCUSED" -> targetPlayers.filter { it.marketValue == 0 } 
                    "SHORT_TERM_FIXES" -> targetPlayers.filter { it.age > 28 && it.rating > 70 }
                    else -> targetPlayers
                }

                val targets = targetPlayers.shuffled().take(2)
                
                for (player in targets) {
                    val isLoanOffer = (Random.nextInt(100) < 30 || budget < player.marketValue) && 
                                     dna?.financialBehavior != FinancialBehavior.SPENDER &&
                                     dna?.transferPolicy != "AGGRESSIVE"
                    
                    if (isLoanOffer) {
                        val transfer = createTransfer(
                            playerId = player.id,
                            targetTeamId = team.id,
                            targetTeamName = team.name,
                            currentTeamId = player.teamId ?: 0,
                            transferType = TransferType.LOAN.value,
                            transferFee = 0,
                            monthlyWage = (player.salary * Random.nextDouble(0.4, 0.8) / 12).toLong(),
                            currentWeek = currentWeek,
                            squadRole = if (player.rating > 70) SquadRole.FIRST_TEAM.value else SquadRole.ROTATION.value,
                            isUserOffer = false
                        )
                        
                        if (transfer != null && player.teamId == userTeamId) {
                            events.add(SimulationEvent.TransferOffer(
                                playerName = player.name,
                                offeringTeam = team.name,
                                fee = 0
                            ))
                        }
                    } else {
                        // Regional logic: Clubs prefer players from their own region or country
                        val regionalBias = if (team.region == player.region) 0.9 else 1.2
                        val countryBias = if (team.country == player.nationality) 0.85 else 1.1
                        
                        val feeMultiplier = Random.nextDouble(0.9, 1.5) * regionalBias * countryBias
                        val fee = (player.marketValue * feeMultiplier).toLong()
                        
                        if (fee <= budget) {
                            val transfer = createTransfer(
                                playerId = player.id,
                                targetTeamId = team.id,
                                targetTeamName = team.name,
                                currentTeamId = player.teamId ?: 0,
                                transferType = TransferType.BUY.value,
                                transferFee = fee,
                                monthlyWage = (player.salary * Random.nextDouble(1.0, 1.3) / 12).toLong(),
                                currentWeek = currentWeek,
                                isUserOffer = false
                            )
                            
                            if (transfer != null && player.teamId == userTeamId) {
                                events.add(SimulationEvent.TransferOffer(
                                    playerName = player.name,
                                    offeringTeam = team.name,
                                    fee = fee
                                ))
                            }
                        }
                    }
                }
            }
        }
        return events
    }

    /**
     * Processes responses for all pending offers using advanced AI scoring.
     */
    suspend fun processAIResponses(currentWeek: Int, userTeamId: Int = -1): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        val pendingOffers = transfersDao.getPendingTransfers().firstOrNull() ?: return emptyList()
        
        for (offer in pendingOffers) {
            // Realistic timeline: 1-3 days delay for response
            val delay = if (offer.isUserOffer) 86400000 else 43200000 // Users wait longer
            if (System.currentTimeMillis() - offer.lastActionDate < delay) continue 

            val player = playersRepository.getPlayerById(offer.playerId) ?: continue
            
            val responseEvents = if (offer.transferType == TransferType.LOAN.value) {
                handleLoanDecision(offer, player, currentWeek, userTeamId)
            } else {
                handlePermanentTransferDecision(offer, player, currentWeek, userTeamId)
            }
            events.addAll(responseEvents)
        }
        return events
    }

    private suspend fun handleLoanDecision(offer: TransfersEntity, player: PlayersEntity, currentWeek: Int, userTeamId: Int): List<SimulationEvent> {
        val clubScore = calculateClubLoanDecisionScore(offer, player)
        val playerScore = calculatePlayerLoanScore(offer, player)
        
        val finalScore = (clubScore * 0.4 + playerScore * 0.6).toInt()
        
        val events = mutableListOf<SimulationEvent>()
        when {
            finalScore >= 75 -> {
                completeTransfer(offer.id, currentWeek, userTeamId)
                if (offer.isUserOffer) {
                    events.add(SimulationEvent.NewsHeadline(
                        news = newsRepository.createNewsArticle(
                            headline = "Transfer Complete: ${player.name}",
                            content = "${player.name} has joined ${offer.targetTeam} on loan.",
                            category = "TRANSFER",
                            relatedTeam = offer.targetTeam
                        ),
                        shouldStop = true
                    ))
                }
            }
            finalScore >= 55 -> negotiateTransfer(offer.id, (offer.transferFee * 1.05).toLong())
            else -> rejectTransfer(offer.id)
        }
        return events
    }

    private suspend fun handlePermanentTransferDecision(offer: TransfersEntity, player: PlayersEntity, currentWeek: Int, userTeamId: Int): List<SimulationEvent> {
        val clubScore = calculateClubDecisionScore(offer, player)
        
        val events = mutableListOf<SimulationEvent>()
        when {
            clubScore >= 80 -> {
                acceptTransfer(offer.id)
                if (offer.isUserOffer) {
                    events.add(SimulationEvent.NewsHeadline(
                        news = newsRepository.createNewsArticle(
                            headline = "Offer Accepted: ${player.name}",
                            content = "${offer.currentTeam} have accepted your offer for ${player.name}.",
                            category = "TRANSFER",
                            relatedTeam = offer.currentTeam
                        ),
                        shouldStop = true
                    ))
                }
            }
            clubScore >= 55 -> negotiateTransfer(offer.id, (offer.transferFee * 1.15).toLong())
            else -> rejectTransfer(offer.id)
        }
        return events
    }

    // --- Decision Helpers ---

    private fun calculatePlayerLoanScore(offer: TransfersEntity, player: PlayersEntity): Int {
        val input = getLoanDecisionInput(offer, player)
        val score =
            input.playingTime * 0.30 +
            input.clubReputation * 0.15 +
            input.leagueQuality * 0.10 +
            input.wageScore * 0.10 +
            input.development * 0.15 +
            input.morale * 0.10 +
            input.personality * 0.10

        return score.toInt().coerceIn(0, 100)
    }

    private suspend fun calculateClubLoanDecisionScore(offer: TransfersEntity, player: PlayersEntity): Int {
        var score = 40
        val teamPlayers = player.teamId?.let { playersRepository.getPlayersByTeamId(it).firstOrNull() } ?: emptyList()
        val positionPeers = teamPlayers.count { it.positionCategory == player.positionCategory }
        
        if (positionPeers > 3) score += 30 // Surplus players
        if (player.rating < 65) score += 10
        if (offer.monthlyWage > (player.salary / 12 * 0.4)) score += 15
        
        return score.coerceIn(0, 100)
    }

    private fun getLoanDecisionInput(offer: TransfersEntity, player: PlayersEntity): LoanInput {
        return LoanInput(
            playingTime = when(offer.squadRole) {
                SquadRole.STAR_PLAYER.value -> 95
                SquadRole.FIRST_TEAM.value -> 80
                else -> 50
            },
            clubReputation = 60,
            leagueQuality = 70,
            wageScore = 100,
            development = if (player.age < 22) 90 else 40,
            managerReputation = 60,
            teamForm = 60,
            distancePenalty = 0,
            morale = player.morale,
            personality = 60
        )
    }

    data class LoanInput(
        val playingTime: Int, val clubReputation: Int, val leagueQuality: Int,
        val wageScore: Int, val development: Int, val managerReputation: Int,
        val teamForm: Int, val distancePenalty: Int, val morale: Int, val personality: Int
    )

    private suspend fun calculateClubDecisionScore(offer: TransfersEntity, player: PlayersEntity): Int {
        val dna = player.teamId?.let { clubDNARepository.getClubDNA(it) }
        var score = 35
        
        // Club DNA influence: "SELL_HIGH" or "EXPORT_FOCUSED" clubs are more likely to accept good offers
        when (dna?.transferPolicy) {
            "SELL_HIGH", "EXPORT_FOCUSED", "SELLING" -> score += 20
            "LOCAL_TALENT_ONLY" -> if (offer.targetTeamId != player.teamId) score += 5 // Slightly more willing to sell if not staying in country? Or maybe less? 
            "ACADEMY_TO_FIRST_TEAM" -> if (player.age > 24) score += 15 // Sell older academy products to make space
            "LOW_BUDGET_OPPORTUNISTIC" -> score += 10
        }

        // Financial behavior impact
        when (dna?.financialBehavior) {
            FinancialBehavior.FRUGAL,
            FinancialBehavior.PLAYER_SALES_DEPENDENT,
            FinancialBehavior.UNSTABLE,
            FinancialBehavior.LOW_REVENUE_SURVIVAL,
            FinancialBehavior.AGGRESSIVE,
            FinancialBehavior.EXPORT_CRISIS -> score += 15

            FinancialBehavior.SPENDER,
            FinancialBehavior.GOVERNMENT_BACKED,
            FinancialBehavior.RISKY -> score -= 20

            FinancialBehavior.CORPORATE_STRUCTURED,
            FinancialBehavior.SPONSOR_DEPENDENT,
            FinancialBehavior.TOURNAMENT_DRIVEN,
            FinancialBehavior.COMMUNITY_FUNDED -> score += 0 // Neutral impact

            null -> score += 0 // Handle null case
        }

        val ratio = if (player.marketValue > 0) offer.transferFee.toDouble() / player.marketValue else 2.0
        score += ((ratio - 1.0) * 80).toInt()
        
        if (player.rating >= 80) score -= 25
        if (player.isTransferListed) score += 35
        
        if (offer.transferFee > 5_000_000) score += 15
        
        // Regional loyalty: North African clubs often hold onto talent more than West African ones
        if (dna?.region == "NORTH_AFRICA") score -= 10
        if (dna?.region == "WEST_AFRICA") score += 10 // Export focus
        
        return score.coerceIn(0, 100)
    }

    // ============ ACTIONS ============

    suspend fun completeTransfer(transferId: Int, currentWeek: Int, userTeamId: Int = -1): Boolean {
        val transfer = transfersDao.getById(transferId) ?: return false

        val today = gameDateManager.formatGameDateForDb(currentWeek)
        val updated = transfer.copy(
            transferStatus = TransferStatus.COMPLETED.value,
            completedDate = today
        )
        transfersDao.update(updated)

        val player = playersRepository.getPlayerById(transfer.playerId) ?: return false
        val targetTeam = teamsRepository.getTeamById(transfer.targetTeamId) ?: return false

        financesRepository.addTransferSpending(targetTeam.id, transfer.completedDate ?: today, transfer.transferFee)
        teamsRepository.getTeamById(transfer.currentTeamId)?.let { fromTeam ->
            financesRepository.addPlayerSale(fromTeam.id, transfer.completedDate ?: today, transfer.transferFee)
        }

        playersRepository.transferPlayer(
            playerId = transfer.playerId,
            newTeamId = targetTeam.id,
            newTeamName = transfer.targetTeam,
            newMarketValue = if (transfer.transferType == TransferType.BUY.value) transfer.transferFee.toInt() else player.marketValue
        )

        if (player.teamId == userTeamId) {
            evaluateFanBacklash(player, targetTeam)
        }

        // Trigger Transfer Press Conference
        if (transfer.targetTeamId == userTeamId || transfer.currentTeamId == userTeamId) {
            val involvesUser = transfer.targetTeamId == userTeamId || transfer.currentTeamId == userTeamId
            if (involvesUser) {
                val manager = managersRepository.getManagerByTeamId(userTeamId)
                manager?.let { mid ->
                    pressConferencesRepository.generatePressConference(
                        managerId = mid.id,
                        context = "NEW_SIGNING",
                        category = QuestionCategory.TRANSFER_RUMORS,
                        relatedPlayer = player.name,
                        relatedTeam = if (transfer.targetTeamId == userTeamId) transfer.currentTeam else transfer.targetTeam
                    )
                }
            }
        }

        generateTransferNews(updated, currentWeek)
        return true
    }

    private suspend fun evaluateFanBacklash(player: PlayersEntity, targetTeam: TeamsEntity) {
        val isRival = player.teamId?.let { teamsRepository.getRivals(it).firstOrNull() }?.any { it.id == targetTeam.id } ?: false
        
        if (player.reputation > 70 && isRival) {
            player.teamId?.let { tid ->
                fanReactionsRepository.addNegativeReaction(
                    teamId = tid,
                    teamName = player.teamName,
                    reaction = "FAN BACKLASH: Selling star player ${player.name} to rivals ${targetTeam.name} is seen as a betrayal!"
                )
                
                // Impact team morale/loyalty or manager security
                teamsRepository.updateTeamFanLoyalty(tid, -15)
            }
        }
    }

    suspend fun acceptTransfer(transferId: Int): Boolean {
        val transfer = transfersDao.getById(transferId) ?: return false
        
        // Evaluate if transfer fits Club Vision
        val player = playersRepository.getPlayerById(transfer.playerId)
        val vision = clubVisionRepository.getVisionForTeam(transfer.targetTeamId).firstOrNull()
        val targetTeam = teamsRepository.getTeamById(transfer.targetTeamId)

        if (player != null && vision != null && targetTeam != null) {
            val fitsPolicy = vision.fitsSigningPolicy(
                playerNationality = player.nationality,
                playerAge = player.age,
                playerReputation = player.reputation,
                teamCountry = targetTeam.country
            )
            
            // Adjust philosophy score based on fit
            val adjustment = if (fitsPolicy) 5 else -10
            val newScore = (vision.philosophyScore + adjustment).coerceIn(0, 100)
            clubVisionRepository.updateVision(vision.copy(philosophyScore = newScore))
        }

        transfersDao.update(transfer.copy(transferStatus = TransferStatus.ACCEPTED.value, lastActionDate = System.currentTimeMillis()))
        return true
    }

    suspend fun rejectTransfer(transferId: Int): Boolean {
        val transfer = transfersDao.getById(transferId) ?: return false
        transfersDao.update(transfer.copy(transferStatus = TransferStatus.REJECTED.value, lastActionDate = System.currentTimeMillis()))
        return true
    }

    suspend fun negotiateTransfer(transferId: Int, newFee: Long): Boolean {
        val transfer = transfersDao.getById(transferId) ?: return false
        transfersDao.update(transfer.copy(
            transferFee = newFee,
            transferStatus = TransferStatus.NEGOTIATING.value,
            lastActionDate = System.currentTimeMillis()
        ))
        return true
    }

    // ============ HUB & NEWS ============

    private suspend fun generateTransferRumour(transfer: TransfersEntity) {
        val player = playersRepository.getPlayerById(transfer.playerId) ?: return
        newsRepository.createTransferRumor(
            playerName = player.name, fromTeam = transfer.currentTeam,
            toTeam = transfer.targetTeam, fee = transfer.transferFee.toInt()
        )
    }

    private suspend fun generateTransferNews(transfer: TransfersEntity, currentWeek: Int) {
        val player = playersRepository.getPlayerById(transfer.playerId) ?: return
        
        // Late window check
        val isLate = currentWeek >= 12 && currentWeek <= 13
        
        val headline = if (isLate) {
            "DEADLINE DAY: ${player.name} joins ${transfer.targetTeam}!"
        } else {
            "CONFIRMED: ${player.name} to ${transfer.targetTeam}"
        }

        val content = "${player.name} has completed his ${if (transfer.transferType == TransferType.LOAN.value) "loan" else "permanent"} move from ${transfer.currentTeam} to ${transfer.targetTeam}."

        newsRepository.createNewsArticle(
            headline = headline,
            content = content,
            category = "TRANSFER",
            relatedPlayerId = player.id,
            relatedPlayer = player.name,
            relatedTeamId = transfer.targetTeamId,
            relatedTeam = transfer.targetTeam,
            isTopNews = transfer.transferFee >= 5_000_000 || isLate
        )
    }

    // --- FLOWS ---
    fun getIncomingTransfers(teamId: Int): Flow<List<TransfersEntity>> = transfersDao.getIncomingTransfers(teamId)
    fun getOutgoingTransfers(teamId: Int): Flow<List<TransfersEntity>> = transfersDao.getOutgoingTransfers(teamId)
    fun getAllTransfersByTeam(teamId: Int): Flow<List<TransfersEntity>> = transfersDao.getAllTransfersByTeam(teamId)
    fun getPendingIncomingTransfers(teamId: Int): Flow<List<TransfersEntity>> = transfersDao.getPendingIncomingTransfers(teamId)
}
