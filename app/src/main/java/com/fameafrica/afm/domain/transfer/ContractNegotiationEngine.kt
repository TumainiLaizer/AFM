package com.fameafrica.afm.domain.transfer

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.LeaguesRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

@Singleton
class ContractNegotiationEngine @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val leaguesRepository: LeaguesRepository
) {

    data class ContractExpectations(
        val wageRange: LongRange,
        val preferredLength: Int,
        val minimumRole: SquadRole,
        val importanceOfBonuses: Double, // 0.0 to 1.0
        val agentFeeExpectation: Long
    )

    data class NegotiatedContract(
        val wage: Long,
        val length: Int,
        val role: SquadRole,
        val signingBonus: Long,
        val goalBonus: Long,
        val cleanSheetBonus: Long,
        val releaseClause: Long?,
        val sellOnFee: Int,
        val agentFee: Long
    )

    /**
     * Calculates realistic wage expectations for a player.
     */
    suspend fun calculateExpectations(
        player: PlayersEntity,
        teamId: Int,
        targetRole: SquadRole = SquadRole.FIRST_TEAM
    ): ContractExpectations {
        val team = teamsRepository.getTeamById(teamId)
        val league = team?.league?.let { leaguesRepository.getLeagueByName(it) }
        
        // African context: Regional multipliers affect base wage levels
        val regionalMultiplier = when (player.region) {
            "NORTH_AFRICA" -> 1.4 // Higher wages (Egypt, Morocco, Tunisia)
            "SOUTHERN_AFRICA" -> 1.3 // Strong economies (South Africa, Namibia)
            "WEST_AFRICA" -> 0.9 // Talent export focus, lower local wages
            "EAST_AFRICA" -> 0.8 // Emerging market
            "CENTRAL_AFRICA" -> 0.75
            else -> 1.0
        }

        // League multiplier based on prize money and tier
        val leagueMultiplier = if (league != null) {
            (league.prizeMoney / 1_000_000.0).coerceIn(0.5, 3.0) * (2.0 / league.level)
        } else 1.0
        
        val teamMultiplier = (team?.reputation ?: 50) / 50.0
        
        // Base wage calculation (Exponential growth with rating)
        // Rating 60 -> ~200k, 80 -> ~1.2M, 90 -> ~4M
        val ratingFactor = (player.rating / 10.0).pow(3.0)
        val baseWage = (ratingFactor * 1000 * leagueMultiplier * teamMultiplier * regionalMultiplier).toLong()
        
        // Foreigner premium (Players from other regions often demand more)
        val foreignPremium = if (player.region != team?.region) 1.2 else 1.0
        
        // Role adjustment
        val roleMultiplier = when(targetRole) {
            SquadRole.STAR_PLAYER -> 1.5
            SquadRole.FIRST_TEAM -> 1.1
            SquadRole.ROTATION -> 0.8
            SquadRole.BACKUP -> 0.6
            SquadRole.PROSPECT -> 0.4
        }
        
        val adjustedWage = (baseWage * roleMultiplier * foreignPremium).toLong()
        val rangeMin = (adjustedWage * 0.85).toLong()
        val rangeMax = (adjustedWage * 1.25).toLong()
        
        // Length preference
        val preferredLength = when {
            player.age < 21 -> Random.nextInt(4, 6)
            player.age > 33 -> Random.nextInt(1, 3)
            else -> Random.nextInt(3, 5)
        }

        return ContractExpectations(
            wageRange = LongRange(rangeMin, rangeMax),
            preferredLength = preferredLength,
            minimumRole = determineMinimumRole(player, teamId),
            importanceOfBonuses = if (player.personalityType == "AMBITIOUS") 0.8 else 0.4,
            agentFeeExpectation = (adjustedWage * preferredLength * 0.1).toLong()
        )
    }

    private fun determineMinimumRole(player: PlayersEntity, teamId: Int): SquadRole {
        // Logic to determine if a player would even consider a backup role
        return if (player.rating > 80) SquadRole.STAR_PLAYER else SquadRole.ROTATION
    }

    /**
     * Evaluates a contract offer.
     * Returns a score from 0 to 100.
     */
    fun evaluateOffer(
        offer: NegotiatedContract,
        expectations: ContractExpectations,
        player: PlayersEntity,
        team: TeamsEntity? = null,
        agentPersonality: String = "BALANCED"
    ): Int {
        var score = 0.0
        
        // 1. Wage (40%)
        val wageScore = when {
            offer.wage >= expectations.wageRange.endInclusive -> 100.0
            offer.wage <= expectations.wageRange.start -> 0.0
            else -> (offer.wage - expectations.wageRange.start).toDouble() / (expectations.wageRange.endInclusive - expectations.wageRange.start) * 100
        }
        score += wageScore * 0.4
        
        // 2. Role (20%)
        val roleScore = if (offer.role.ordinal <= expectations.minimumRole.ordinal) 100.0 else 0.0
        score += roleScore * 0.2
        
        // 3. Length (10%)
        val lengthDiff = Math.abs(offer.length - expectations.preferredLength)
        val lengthScore = max(0.0, 100.0 - (lengthDiff * 25.0))
        score += lengthScore * 0.1
        
        // 4. Bonuses & Sign-on (20%)
        val bonusValue = offer.signingBonus + (offer.goalBonus * 10) + (offer.cleanSheetBonus * 10)
        val bonusScore = (bonusValue.toDouble() / (offer.wage * 0.5)).coerceIn(0.0, 1.0) * 100
        score += bonusScore * 0.2 * expectations.importanceOfBonuses
        
        // 5. Agent Fee (10%)
        val agentScore = (offer.agentFee.toDouble() / expectations.agentFeeExpectation).coerceIn(0.0, 1.5) * 100
        score += agentScore * 0.1

        // African Context Modifiers
        // Regional/Family Factor: Players prefer clubs in their home region or country
        if (player.region == team?.region) score += 5
        if (player.nationality == team?.country) score += 10
        
        // Move to Europe Ambition: Young elite players might accept lower wages for a shorter contract with a release clause
        if (player.age < 23 && player.potential > 80 && offer.length <= 3 && offer.releaseClause != null) {
            score += 15
        }

        // Personality modifiers
        if (agentPersonality == "GREEDY" && agentScore < 80) score -= 15
        
        return score.toInt().coerceIn(0, 100)
    }
}
