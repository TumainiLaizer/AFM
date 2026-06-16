package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.dao.SponsorsDao
import com.fameafrica.afm.data.database.entities.SponsorsEntity
import com.fameafrica.afm.data.database.entities.SponsorType
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.data.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.random.Random

@Singleton
class SponsorshipNegotiationSystem @Inject constructor(
    private val sponsorsDao: SponsorsDao,
    private val teamsRepository: TeamsRepository,
    private val newsRepository: NewsRepository
) {
    enum class SponsorshipTier {
        MAIN_SHIRT, KIT_MANUFACTURER, STADIUM_NAMING, SLEEVE, REGIONAL_PARTNER
    }

    data class SponsorshipOffer(
        val id: String,
        val sponsorName: String,
        val sponsorId: Int,
        val annualValue: Long,
        val durationYears: Int,
        val winBonus: Long,
        val leagueTitleBonus: Long,
        val cupTitleBonus: Long,
        val tier: SponsorshipTier,
        val demands: List<SponsorDemand> = emptyList(),
        var negotiationStatus: NegotiationStatus = NegotiationStatus.INITIAL_OFFER,
        var currentCounter: Int = 0
    )

    data class SponsorDemand(
        val type: DemandType,
        val value: String,
        val description: String
    )

    enum class DemandType {
        MIN_REPUTATION, LOCAL_PLAYERS_ONLY, YOUTH_FOCUS, SIGN_STAR_PLAYER, NO_CONTROVERSY
    }

    enum class NegotiationStatus {
        INITIAL_OFFER, COUNTERED, ACCEPTED, REJECTED, WITHDRAWN
    }

    /**
     * Simulates sponsorship opportunities for a team based on their reputation, league, and success.
     */
    suspend fun generateOffers(teamId: Int, leagueReputation: Int): List<SponsorshipOffer> {
        val team = teamsRepository.getTeamById(teamId) ?: return emptyList()
        val allSponsors = sponsorsDao.getAllStatic()
        
        // Tiered availability
        val offers = mutableListOf<SponsorshipOffer>()
        
        // Filter sponsors that align with team reputation
        val potentialSponsors = allSponsors.filter { s ->
            s.teamId == null && (s.region == "Global" || s.region == team.region)
        }

        SponsorshipTier.entries.forEach { tier ->
            val count = when(tier) {
                SponsorshipTier.MAIN_SHIRT -> 1
                SponsorshipTier.KIT_MANUFACTURER -> 1
                SponsorshipTier.STADIUM_NAMING -> if (Random.nextInt(100) < 30) 1 else 0
                else -> Random.nextInt(1, 3)
            }

            repeat(count) {
                potentialSponsors.randomOrNull()?.let { sponsor ->
                    offers.add(createOffer(sponsor, team, leagueReputation, tier))
                }
            }
        }

        return offers.distinctBy { it.sponsorId }
    }

    private fun createOffer(
        sponsor: SponsorsEntity, 
        team: TeamsEntity, 
        leagueRep: Int, 
        tier: SponsorshipTier
    ): SponsorshipOffer {
        // Valuation logic based on Team Reputation + League Reputation
        val combinedRep = (team.reputation * 0.7 + leagueRep * 0.3)
        val baseValuation = (combinedRep.pow(1.5) * 50000).toLong()
        
        val multiplier = when(tier) {
            SponsorshipTier.MAIN_SHIRT -> 1.0
            SponsorshipTier.KIT_MANUFACTURER -> 0.8
            SponsorshipTier.STADIUM_NAMING -> 1.2
            SponsorshipTier.SLEEVE -> 0.3
            SponsorshipTier.REGIONAL_PARTNER -> 0.15
        }

        val annualValue = (baseValuation * multiplier * (0.9 + Random.nextDouble(0.2))).toLong()
        
        // Regional Boost Logic
        val regionalBoost = when {
            team.region == "East Africa" && sponsor.sponsorType == SponsorType.TELECOM_PARTNER.value -> 1.25
            team.region == "North Africa" && sponsor.sponsorType == SponsorType.REAL_ESTATE_PARTNER.value -> 1.30
            team.region == "West Africa" && sponsor.sponsorType == SponsorType.TOURISM_PARTNER.value -> 1.20 // Tourism/Agriculture proxy
            else -> 1.0
        }
        
        val finalAnnualValue = (annualValue * regionalBoost).toLong()

        val duration = when(tier) {
            SponsorshipTier.STADIUM_NAMING -> Random.nextInt(5, 11)
            else -> Random.nextInt(1, 4)
        }

        return SponsorshipOffer(
            id = java.util.UUID.randomUUID().toString(),
            sponsorName = sponsor.name,
            sponsorId = sponsor.id,
            annualValue = finalAnnualValue,
            durationYears = duration,
            winBonus = (finalAnnualValue * 0.05).toLong(),
            leagueTitleBonus = (finalAnnualValue * 0.2).toLong(),
            cupTitleBonus = (finalAnnualValue * 0.1).toLong(),
            tier = tier,
            demands = generateRandomDemands(sponsor, team)
        )
    }

    private fun generateRandomDemands(sponsor: SponsorsEntity, team: TeamsEntity): List<SponsorDemand> {
        val demands = mutableListOf<SponsorDemand>()
        if (Random.nextInt(100) < 30) {
            demands.add(SponsorDemand(DemandType.MIN_REPUTATION, "${team.reputation + 5}", "Reach a reputation of ${team.reputation + 5}"))
        }
        if (sponsor.preferredPlayerNationalities != null && Random.nextInt(100) < 40) {
            demands.add(SponsorDemand(DemandType.LOCAL_PLAYERS_ONLY, sponsor.preferredPlayerNationalities, "Sign at least 3 players from ${sponsor.preferredPlayerNationalities}"))
        }
        return demands
    }

    /**
     * Evaluates a counter-offer from the manager.
     */
    fun evaluateCounter(offer: SponsorshipOffer, counterValue: Long, counterYears: Int): NegotiationResult {
        offer.currentCounter++
        
        if (offer.currentCounter > 3) {
            offer.negotiationStatus = NegotiationStatus.WITHDRAWN
            return NegotiationResult.WITHDRAWN
        }

        val percentageIncrease = (counterValue - offer.annualValue).toDouble() / offer.annualValue
        val durationDiff = counterYears - offer.durationYears
        
        var baseProb = when {
            percentageIncrease <= 0.0 -> 0.95
            percentageIncrease <= 0.05 -> 0.70
            percentageIncrease <= 0.15 -> 0.40
            percentageIncrease <= 0.30 -> 0.10
            else -> 0.01
        }
        
        // Duration adjustment (Sponsors usually prefer shorter deals if paying more, or longer if paying less)
        if (durationDiff > 0) baseProb -= 0.1
        else if (durationDiff < 0) baseProb += 0.05

        return if (Random.nextDouble() < baseProb) {
            offer.negotiationStatus = NegotiationStatus.ACCEPTED
            NegotiationResult.ACCEPTED
        } else {
            if (Random.nextDouble() < 0.2) {
                offer.negotiationStatus = NegotiationStatus.WITHDRAWN
                NegotiationResult.WITHDRAWN
            } else {
                offer.negotiationStatus = NegotiationStatus.COUNTERED
                NegotiationResult.REJECTED_BUT_OPEN
            }
        }
    }

    enum class NegotiationResult {
        ACCEPTED, REJECTED_BUT_OPEN, WITHDRAWN
    }
}
