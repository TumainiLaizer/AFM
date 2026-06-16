package com.fameafrica.afm.utils.tactics

import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.ui.screen.tactics.getPositionLabelsForFormation

object LineupUtils {
    fun autoSelectLineup(
        players: List<PlayersEntity>,
        formation: String,
        filterType: String = "Rating"
    ): List<Int> {
        if (players.isEmpty()) return emptyList()

        val requiredPositions = getPositionLabelsForFormation(formation).take(11)
        val availablePlayers = players.toMutableList()
        val newStarters = MutableList<Int?>(11) { null }

        // Logic: Prioritize players who are already marked as starters (isStartingXi),
        // but still filter by position fit and the selected strategy (Rating/Form/Value).

        // 1. Exact matches: isStartingXi -> sortingValue
        requiredPositions.forEachIndexed { index, pos ->
            val match = availablePlayers.filter { it.position == pos }
                .sortedWith(
                    compareByDescending<PlayersEntity> { it.isStartingXi }
                        .thenByDescending { getSortingValue(it, filterType) }
                )
                .firstOrNull()
            
            if (match != null) {
                newStarters[index] = match.id
                availablePlayers.remove(match)
            }
        }

        // 2. Compatible matches: isStartingXi -> sortingValue
        requiredPositions.forEachIndexed { index, pos ->
            if (newStarters[index] == null) {
                val compatible = availablePlayers.filter { isPositionEligibleForSlot(it, pos) }
                    .sortedWith(
                        compareByDescending<PlayersEntity> { it.isStartingXi }
                            .thenByDescending { getSortingValue(it, filterType) }
                    )
                    .firstOrNull()
                
                if (compatible != null) {
                    newStarters[index] = compatible.id
                    availablePlayers.remove(compatible)
                }
            }
        }

        // 3. Best remaining: isStartingXi -> sortingValue
        requiredPositions.forEachIndexed { index, _ ->
            if (newStarters[index] == null && availablePlayers.isNotEmpty()) {
                val best = availablePlayers.sortedWith(
                    compareByDescending<PlayersEntity> { it.isStartingXi }
                        .thenByDescending { getSortingValue(it, filterType) }
                )
                    .firstOrNull()
                
                if (best != null) {
                    newStarters[index] = best.id
                    availablePlayers.remove(best)
                }
            }
        }

        return newStarters.filterNotNull().take(11)
    }

    private fun getSortingValue(player: PlayersEntity, filterType: String): Double {
        return when (filterType) {
            "Form" -> player.currentForm.toDouble()
            "Value" -> player.marketValue.toDouble()
            else -> player.overallRating.toDouble()
        }
    }

    private fun isPositionEligibleForSlot(player: PlayersEntity, requiredPosition: String): Boolean {
        if (player.position == requiredPosition) return true
        return when (requiredPosition) {
            "GK" -> player.position == "GK"
            "CB" -> player.position in listOf("CB")
            "CDM" -> player.position in listOf("CDM", "CB", "CM")
            "LB" -> player.position in listOf("LB", "LWB", "LM")
            "RB" -> player.position in listOf("RB", "RWB", "RM")
            "LWB" -> player.position in listOf("LWB", "LB", "LM")
            "RWB" -> player.position in listOf("RWB", "RB", "RM")
            "CM" -> player.position in listOf("CM", "CDM", "CAM")
            "CAM" -> player.position in listOf("CAM", "CM", "CF")
            "LM" -> player.position in listOf("LM", "LW", "LWB")
            "RM" -> player.position in listOf("RM", "RW", "RWB")
            "LW" -> player.position in listOf("LW", "LM", "RW")
            "RW" -> player.position in listOf("RW", "RM", "LW")
            "ST" -> player.position in listOf("ST", "CF")
            "CF" -> player.position in listOf("CF", "ST")
            else -> player.position == requiredPosition
        }
    }
}
