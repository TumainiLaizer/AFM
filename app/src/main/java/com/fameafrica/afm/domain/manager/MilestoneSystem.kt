package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestoneSystem @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val teamsRepository: TeamsRepository
) {

    /**
     * Checks for immediate rewards/milestones after a week simulation or match.
     */
    suspend fun checkMilestones(teamId: Int, managerId: Int) {
        val team = teamsRepository.getTeamById(teamId) ?: return
        
        // 1. Point Milestones
        if (team.points >= 50) {
            triggerReward(managerId, "50 POINTS REACHED!", "You've hit the 50-point mark. The board is delighted with the team's consistency.")
        }
        
        // 2. Unbeaten Run (FAKE DETAIL for micro-reward)
        if (team.formStreak.contains("WWWWW")) {
            triggerReward(managerId, "INVINCIBLE FORM!", "5 consecutive wins! Your tactical setup is currently unstoppable.")
        }
        
        // 3. Financial Milestone
        if (team.revenue > 20000000) {
            triggerReward(managerId, "FINANCIAL GIANT!", "The club's revenue has crossed a major threshold. Your brand value is skyrocketing.")
        }
    }

    private suspend fun triggerReward(managerId: Int, title: String, content: String) {
        notificationsRepository.insertNotification(
            NotificationsEntity(
                userId = managerId,
                title = title,
                message = content,
                notificationType = NotificationType.MILESTONE.value,
                priority = 4,
                timestamp = System.currentTimeMillis(),
                id = 0,
                isRead = false,
                isArchived = false,
                icon = "🏆",
                imageUrl = null,
                actionUrl = null,
                actionText = null,
                relatedEntityType = null,
                relatedEntityId = null,
                relatedEntityName = null,
                expiryTime = null,
                dismissible = true,
                color = "#D4AF37",
                dataJson = null
            )
        )
    }
}
