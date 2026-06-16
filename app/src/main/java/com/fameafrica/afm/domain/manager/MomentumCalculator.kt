package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.TeamsEntity

object MomentumCalculator {
    fun calculateMatchMomentum(team: TeamsEntity, isDerby: Boolean): Float {
        var momentum = 1.0f
        
        // Morale Influence
        momentum += (team.moraleMomentum - 0.5f) * 0.2f
        
        // Tactical Stability Buff (Knowledge of system)
        if (team.tacticalStability > 10) momentum += 0.05f
        
        // Pressure Multiplier (High Rep Clubs)
        val pressureFactor = if (team.reputation > 80) 1.5f else 1.0f
        
        if (team.performanceTrend.takeLast(3).all { it == 0 }) {
            // Exponential pressure on big clubs during losing streaks
            momentum -= (0.1f * pressureFactor)
        }
        
        // Fan Pressure in Derbies
        if (isDerby && team.fanSentiment < 40) {
            momentum -= 0.15f // Players "choke" under toxic atmosphere
        }

        return momentum.coerceIn(0.7f, 1.3f)
    }
}
