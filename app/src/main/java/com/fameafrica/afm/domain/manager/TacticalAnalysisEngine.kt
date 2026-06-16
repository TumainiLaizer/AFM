package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.model.core.Player
import com.fameafrica.afm.data.database.entities.TacticsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TacticalAnalysisEngine @Inject constructor() {

    /**
     * Analyzes how well a squad fits a specific tactical setup.
     * Returns a score from 0-100.
     */
    fun calculateSquadTacticalFit(players: List<com.fameafrica.afm.data.database.model.core.Player>, tactics: TacticsEntity): Int {
        if (players.isEmpty()) return 0
        
        var totalFit = 0
        players.forEach { player ->
            totalFit += calculatePlayerRoleFit(player, tactics)
        }
        
        return (totalFit / players.size).coerceIn(0, 100)
    }

    private fun calculatePlayerRoleFit(player: com.fameafrica.afm.data.database.model.core.Player, tactics: TacticsEntity): Int {
        // Simplified logic: Check if player's position matches formation requirements
        // and if their attributes align with playstyle (e.g., Pace for Counter-Attack)
        val positionFit = if (tactics.formation.contains(player.position)) 80 else 40
        
        val styleBonus = when (tactics.playstyle.uppercase()) {
            "COUNTER_ATTACK" -> if (player.rating > 70) 10 else 0
            "POSSESSION" -> if (player.rating > 75) 15 else 5
            else -> 5
        }
        
        return (positionFit + styleBonus).coerceIn(0, 100)
    }

    /**
     * Generates a tactical summary for the manager.
     */
    fun getTacticalFeedback(fitScore: Int): String {
        return when {
            fitScore >= 80 -> "The squad is perfectly suited for this system. Expect high cohesion."
            fitScore >= 60 -> "The system is solid, but some players may struggle with their roles."
            else -> "Warning: Several players are out of position or ill-suited for this style."
        }
    }
}
