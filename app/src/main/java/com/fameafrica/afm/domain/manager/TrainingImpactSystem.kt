package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.model.core.Player
import com.fameafrica.afm.data.database.entities.TrainingSessionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingImpactSystem @Inject constructor() {

    /**
     * Calculates the impact of a single training session on a player.
     * Returns a pair of (Attribute Growth, Fatigue Increase).
     */
    fun calculateSessionImpact(
        player: com.fameafrica.afm.data.database.model.core.Player,
        sessionType: TrainingSessionType
    ): TrainingImpact {
        return when (sessionType) {
            TrainingSessionType.PHYSICAL_CONDITIONING -> {
                TrainingImpact(growth = 0.5, fatigue = 15, sharpness = 10)
            }
            TrainingSessionType.TACTICAL_SHAPE -> {
                TrainingImpact(growth = 0.2, fatigue = 5, sharpness = 5)
            }
            TrainingSessionType.RECOVERY -> {
                TrainingImpact(growth = 0.0, fatigue = -20, sharpness = -2)
            }
            TrainingSessionType.TECHNICAL_DRILLS -> {
                TrainingImpact(growth = 0.8, fatigue = 10, sharpness = 8)
            }
            else -> TrainingImpact(growth = 0.1, fatigue = 5, sharpness = 2)
        }
    }

    data class TrainingImpact(
        val growth: Double,
        val fatigue: Int,
        val sharpness: Int
    )
}
