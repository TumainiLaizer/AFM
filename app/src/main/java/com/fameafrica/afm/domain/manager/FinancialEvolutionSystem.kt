package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.data.repository.NewsRepository
import com.fameafrica.afm.data.database.entities.FinancialBehavior
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialEvolutionSystem @Inject constructor(
    private val repository: TeamsRepository,
    private val newsRepository: NewsRepository
) {
    suspend fun evolveFinancialBehavior(teamId: Int, currentBalance: Long, seasonProfit: Long, resultsTrend: Float) {
        val team = repository.getTeamSync(teamId) ?: return
        var newBehavior = team.financialBehavior

        when (team.financialBehavior) {
            FinancialBehavior.SPENDER -> {
                if (currentBalance < 0 || (seasonProfit < -1_000_000 && resultsTrend < 0.4)) {
                    newBehavior = FinancialBehavior.FRUGAL // Forced austerity
                    triggerStory(teamId, team.name, "FINANCIAL_CRISIS", "Board enforces spending freeze after heavy losses.")
                }
            }
            FinancialBehavior.FRUGAL -> {
                if (currentBalance > 5_000_000 && resultsTrend > 0.7) {
                    newBehavior = FinancialBehavior.SPENDER // Ambition growth
                    triggerStory(teamId, team.name, "WAR_CHEST_OPENED", "Success brings investment: ${team.name} ready to spend.")
                }
            }
            FinancialBehavior.RISKY -> {
                if (currentBalance < -2_000_000) {
                    newBehavior = FinancialBehavior.UNSTABLE
                }
            }
            FinancialBehavior.PLAYER_SALES_DEPENDENT -> {
                // Assuming lastSaleDate logic exists or can be simplified
                if (seasonProfit < -500_000) {
                    newBehavior = FinancialBehavior.EXPORT_CRISIS
                }
            }
            else -> {}
        }
        
        if (newBehavior != team.financialBehavior) {
            repository.updateFinancialBehavior(teamId, newBehavior)
        }
    }

    private suspend fun triggerStory(teamId: Int, teamName: String, title: String, content: String) {
        newsRepository.createNewsArticle(
            headline = title,
            content = content,
            category = "FINANCE",
            relatedTeamId = teamId,
            relatedTeam = teamName
        )
    }
}
