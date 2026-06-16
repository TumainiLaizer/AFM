package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.repository.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FederationPoliticsEngine @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val newsRepository: NewsRepository,
    private val financesRepository: FinancesRepository
) {

    data class FederationStatus(
        val teamId: Int,
        val federationFavor: Int, // 0-100
        val regulatoryPressure: Int, // 0-100
        val isUnderInvestigation: Boolean
    )

    /**
     * Processes weekly federation politics updates.
     */
    suspend fun processWeeklyUpdates(teamId: Int, season: String) {
        val team = teamsRepository.getTeamById(teamId) ?: return
        val finances = financesRepository.getTeamFinances(teamId, season)
        
        // FAKE DETAIL: Financial mismanagement triggers federation pressure
        if (finances != null && finances.bankBalance < -2000000) {
            increaseRegulatoryPressure(teamId, 15)
            if (Random.nextInt(100) < 30) {
                newsRepository.createNewsArticle(
                    headline = "FEDERATION WARNING: ${team.name} under financial scrutiny",
                    content = "The national football federation has issued a formal warning to ${team.name} following reports of growing debt. Sanctions could follow if the situation is not addressed.",
                    category = "FEDERATION",
                    relatedTeam = team.name,
                    isTopNews = true
                )
            }
        }
    }

    private fun increaseRegulatoryPressure(teamId: Int, amount: Int) {
        // Implementation to update pressure in DB would go here
    }

    /**
     * Chance for a "Special Grant" from the federation for successful clubs.
     */
    suspend fun checkGrantOpportunity(teamId: Int, reputation: Int): Long {
        if (reputation > 75 && Random.nextInt(100) < 5) {
            val grantAmount = 500000L
            return grantAmount
        }
        return 0L
    }
}
