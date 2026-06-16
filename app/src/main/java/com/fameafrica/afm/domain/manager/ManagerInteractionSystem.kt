package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagerInteractionSystem @Inject constructor() {

    /**
     * Generates weekly narrative events/requests from the AI Manager.
     */
    fun generateWeeklyRequests(
        manager: ManagersEntity,
        team: TeamsEntity,
        recentForm: String // e.g. "WWWLD"
    ): List<ManagerRequest> {
        val requests = mutableListOf<ManagerRequest>()

        // 1. Budget Requests based on personality and spending habits
        if (manager.spendingHabits == "DEMANDING" && team.revenue < 50000000) {
            requests.add(
                ManagerRequest(
                    type = RequestType.INCREASE_WAGE_BUDGET,
                    message = "Mr. Chairman, the squad needs quality. I need more wage budget to attract stars.",
                    urgency = Urgency.HIGH
                )
            )
        }

        // 2. Facility Requests
        if (manager.youthDevelopmentFocus ?: 0 > 70 && team.stadiumCapacity < 10000) {
            requests.add(
                ManagerRequest(
                    type = RequestType.UPGRADE_ACADEMY,
                    message = "Our youth setup is outdated. We are losing African talents to rivals.",
                    urgency = Urgency.MEDIUM
                )
            )
        }

        // 3. Resignation Threats
        if (manager.jobSecurity < 30 && recentForm.count { it == 'L' } >= 3) {
            requests.add(
                ManagerRequest(
                    type = RequestType.THREATEN_RESIGN,
                    message = "I cannot work under these conditions. If results don't improve and support isn't shown, I'm out.",
                    urgency = Urgency.CRITICAL
                )
            )
        }

        return requests
    }

    data class ManagerRequest(
        val type: RequestType,
        val message: String,
        val urgency: Urgency
    )

    enum class RequestType {
        INCREASE_WAGE_BUDGET,
        UPGRADE_ACADEMY,
        UPGRADE_TRAINING,
        THREATEN_RESIGN,
        CONTRACT_RENEWAL
    }

    enum class Urgency {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
