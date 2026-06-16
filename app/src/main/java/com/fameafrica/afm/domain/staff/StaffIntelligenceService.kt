package com.fameafrica.afm.domain.staff

import com.fameafrica.afm.data.database.entities.StaffEntity
import com.fameafrica.afm.data.database.entities.StaffRole
import com.fameafrica.afm.data.repository.StaffRepository
import com.fameafrica.afm.domain.model.SimulationEvent
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Service that provides gameplay impact and intelligence based on the club's staff hierarchy.
 * Handles suggestions, recovery boosts, and tactical advice.
 */
@Singleton
class StaffIntelligenceService @Inject constructor(
    private val staffRepository: StaffRepository
) {

    /**
     * Calculates a recovery multiplier based on the quality of medical staff.
     * Returns a multiplier between 1.0 (base) and 1.5 (elite).
     */
    suspend fun getMedicalRecoveryMultiplier(teamId: Int): Double {
        val medicalStaff = staffRepository.getMedicalStaffByTeam(teamId).firstOrNull() ?: return 1.0
        if (medicalStaff.isEmpty()) return 0.8 // Penalty for no medical staff
        
        val avgImpact = medicalStaff.map { it.impactRating }.average()
        return 1.0 + (avgImpact / 200.0) // 1.0 to 1.5
    }

    /**
     * Generates a tactical suggestion from the Assistant Manager.
     */
    suspend fun getAssistantManagerAdvice(teamId: Int): String? {
        val assistant = staffRepository.getAssistantManager(teamId) ?: return null
        
        val advices = listOf(
            "Boss, our midfield looks a bit thin. Maybe we should try a 3-man midfield in the next game?",
            "The lads are looking sharp in training. I think we can push higher in our next match.",
            "Some of the younger players are ready for a run in the first team, what do you think?",
            "Our set-piece defending has been sloppy lately. We've scheduled extra drills.",
            "The morale in the camp is fantastic. This is the perfect time to demand more from them."
        )
        
        // Quality of advice depends on impact rating
        if (Random.nextInt(100) < assistant.impactRating) {
            return advices.random()
        }
        
        return "Everything is looking standard for now, Boss."
    }

    /**
     * Generates periodic staff-related events (e.g., coaching breakthroughs, medical warnings).
     */
    suspend fun processDailyStaffInteractions(teamId: Int, managerId: Int): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        val allStaff = staffRepository.getStaffByTeam(teamId).firstOrNull() ?: return emptyList()

        for (staff in allStaff) {
            val roll = Random.nextInt(1000)
            
            // 1. Coaching Breakthrough (1 in 500 chance)
            if (staff.staffType == "COACHING" && roll < 2) {
                events.add(SimulationEvent.Story(
                    title = "Training Insight",
                    message = "${staff.name} (${staff.roleDisplay}) has discovered a new tactical variation that could benefit our squad."
                ))
            }

            // 2. Medical Warning (1 in 300 chance)
            if (staff.staffType == "MEDICAL" && roll < 3) {
                events.add(SimulationEvent.Story(
                    title = "Physio Report",
                    message = "${staff.name} is concerned about the fatigue levels of our key players. Consider rotating in the next match."
                ))
            }
        }
        
        return events
    }
}
