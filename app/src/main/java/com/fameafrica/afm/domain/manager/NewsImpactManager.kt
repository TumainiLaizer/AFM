package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.data.repository.PlayersRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsImpactManager @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository
) {
    /**
     * Applies the consequences of a news article to the game world.
     * news: The news article that was just created.
     */
    suspend fun applyNewsImpact(news: NewsEntity) {
        when (news.category) {
            "MATCH" -> handleMatchNewsImpact(news)
            "TRANSFER" -> handleTransferNewsImpact(news)
            "INJURY" -> handleInjuryNewsImpact(news)
            "MANAGER" -> handleManagerNewsImpact(news)
            "RUMOR" -> handleRumorNewsImpact(news)
            "FANS" -> handleFanNewsImpact(news)
        }
    }

    private suspend fun handleMatchNewsImpact(news: NewsEntity) {
        val teamId = news.relatedTeamId ?: return
        val content = news.content.lowercase()
        val isTopNews = news.isTopNews == 1

        // Hostile or Sensationalist news about a loss hits harder
        if (content.contains("disaster") || content.contains("ruins") || content.contains("shameful") || content.contains("collapse")) {
            val impact = if (isTopNews) -8 else -4
            teamsRepository.updateTeamMorale(teamId, impact)
            playersRepository.getPlayersByTeamId(teamId).firstOrNull()?.forEach {
                playersRepository.updatePlayerMorale(it.id, impact / 2)
            }
        } else if (content.contains("historic") || content.contains("stun") || content.contains("classic") || content.contains("thriller") || content.contains("jubilation")) {
            val impact = if (isTopNews) +8 else +4
            teamsRepository.updateTeamMorale(teamId, impact)
            playersRepository.getPlayersByTeamId(teamId).firstOrNull()?.forEach {
                playersRepository.updatePlayerMorale(it.id, impact / 2)
            }
        }
    }

    private suspend fun handleTransferNewsImpact(news: NewsEntity) {
        val teamId = news.relatedTeamId ?: return
        if (news.headline.contains("BLOCKBUSTER") || news.headline.contains("CONFIRMED")) {
            // New signing boosts morale and fan sentiment
            teamsRepository.updateTeamMorale(teamId, 5)
            // Fan sentiment update should be added to TeamsRepository
        }
    }

    private suspend fun handleInjuryNewsImpact(news: NewsEntity) {
        val teamId = news.relatedTeamId ?: return
        if (news.headline.contains("BLOW")) {
            teamsRepository.updateTeamMorale(teamId, -3)
        }
    }

    private suspend fun handleManagerNewsImpact(news: NewsEntity) {
        val teamId = news.relatedTeamId ?: return
        if (news.headline.contains("NEW ERA")) {
            teamsRepository.updateTeamMorale(teamId, 10) // New manager bounce
        } else if (news.headline.contains("SACKED")) {
            teamsRepository.updateTeamMorale(teamId, -15) // Uncertainty
        }
    }

    private suspend fun handleRumorNewsImpact(news: NewsEntity) {
        val playerId = news.relatedPlayerId ?: return
        // Rumors can affect player morale (distraction)
        playersRepository.updatePlayerMorale(playerId, -2)
    }

    private suspend fun handleFanNewsImpact(news: NewsEntity) {
        val teamId = news.relatedTeamId ?: return
        if (news.headline.contains("Protests")) {
            teamsRepository.updateTeamMorale(teamId, -10)
        }
    }
}
