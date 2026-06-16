package com.fameafrica.afm.domain.transfer

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

@Singleton
class TransferNegotiationEngine @Inject constructor(
    private val clubDNARepository: ClubDNARepository,
    private val playerContractsRepository: PlayerContractsRepository,
) {

    /**
     * Calculates a realistic market value for a player.
     */
    suspend fun calculateMarketValue(player: PlayersEntity): Long {
        val contract = playerContractsRepository.getContractByPlayerId(player.id)
        
        // Base value from rating and age
        val ratingFactor = (player.rating / 10.0).pow(3.5)
        var baseValue = (ratingFactor * 150000).toLong()
        
        // African context: Regional scouting focus and local hype
        val regionalMultiplier = when (player.region) {
            "WEST_AFRICA" -> 1.2 // High scouting visibility (Senegal, Nigeria, Ghana)
            "NORTH_AFRICA" -> 1.3 // High local market value (Egypt, Morocco)
            "SOUTHERN_AFRICA" -> 1.1 // Stable market
            else -> 0.9
        }
        baseValue = (baseValue * regionalMultiplier).toLong()

        // Potential premium
        if (player.age < 23) {
            val potentialPremium = (player.potential - player.rating).coerceAtLeast(0) * 0.05
            baseValue = (baseValue * (1.0 + potentialPremium)).toLong()
        }
        
        // Contract length impact
        val yearsRemaining = contract?.yearsRemaining ?: 1
        val contractMultiplier = when {
            yearsRemaining <= 0 -> 0.1 // Expired
            yearsRemaining == 1 -> 0.6 // One year left, value drops
            yearsRemaining == 2 -> 0.9
            else -> 1.1 // Long term security
        }
        
        // Form impact
        val formMultiplier = 0.8 + (player.currentForm / 10.0) * 0.4
        
        // Reputation impact
        val reputationMultiplier = 0.9 + (player.reputation / 100.0) * 0.5
        
        return (baseValue * contractMultiplier * formMultiplier * reputationMultiplier).toLong()
    }

    /**
     * Evaluates a transfer offer from another club (AI or User).
     */
    suspend fun evaluateOffer(
        offer: TransfersEntity,
        player: PlayersEntity
    ): OfferResponse {
        val currentValue = calculateMarketValue(player)
        val dna = clubDNARepository.getClubDNA(offer.currentTeamId)
        
        // 1. Total Package Value
        val totalPackage = offer.transferFee + (offer.goalBonusFee * 0.4).toLong() + (offer.appearanceBonusFee * 0.6).toLong()
        
        // 2. Relative Value Score (0.0 to 2.0)
        val valueRatio = totalPackage.toDouble() / max(1, currentValue)
        
        // 3. DNA & Financial Behavior modifiers
        var threshold = when (dna?.financialBehavior) {
            FinancialBehavior.FRUGAL -> 1.2
            FinancialBehavior.SPENDER -> 0.9
            FinancialBehavior.PLAYER_SALES_DEPENDENT -> 0.85
            else -> 1.0
        }
        
        // 4. Strategic Importance
        if (player.rating > 80) threshold *= 1.3 // Hard to replace star players
        if (player.isTransferListed) threshold *= 0.75 // Desperate to sell
        
        return when {
            valueRatio >= threshold * 1.3 -> OfferResponse.ACCEPT
            valueRatio >= threshold * 0.9 -> {
                // If it's close, maybe counter
                if (Random.nextInt(100) < 60) {
                    val counterFee = (totalPackage * 1.15).toLong()
                    OfferResponse.COUNTER(counterFee)
                } else {
                    OfferResponse.REJECT
                }
            }
            else -> OfferResponse.REJECT
        }
    }

    /**
     * African Context: Evaluates the chance of a successful transfer based on regional factors.
     */
    fun calculateRegionalTransferChance(player: PlayersEntity, targetTeam: TeamsEntity): Double {
        var baseChance = 0.5
        
        // Same region bonus
        if (player.region == targetTeam.region) baseChance += 0.2
        
        // Target team reputation vs player reputation
        val repDiff = targetTeam.reputation - player.reputation
        baseChance += (repDiff / 100.0)
        
        // Visa/ITC difficulty for cross-regional moves in Africa
        if (player.region != targetTeam.region) baseChance -= 0.1
        
        return baseChance.coerceIn(0.1, 1.0)
    }

    sealed class OfferResponse {
        object ACCEPT : OfferResponse()
        object REJECT : OfferResponse()
        data class COUNTER(val suggestedFee: Long) : OfferResponse()
    }
}
