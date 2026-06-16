package com.fameafrica.afm.utils.tactics

import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.TacticsEntity
import com.fameafrica.afm.data.database.model.enums.PlayerArchetype
import com.fameafrica.afm.data.database.model.enums.PlayerRole

object PlayerRoleManager {

    /**
     * Maps a Player's Archetype to their Primary and Secondary Roles.
     */
    fun mapArchetypeToRoles(archetype: String?): Pair<PlayerRole, PlayerRole> {
        val arch = PlayerArchetype.fromString(archetype ?: "") ?: return PlayerRole.GOALKEEPER to PlayerRole.GOALKEEPER
        
        return when (arch) {
            PlayerArchetype.DYNAMIC_FORWARD -> PlayerRole.INSIDE_FORWARD to PlayerRole.SHADOW_STRIKER
            PlayerArchetype.STRATEGIC_MIDFIELDER -> PlayerRole.DEEP_LYING_PLAYMAKER to PlayerRole.ADVANCED_PLAYMAKER
            PlayerArchetype.RESILIENT_DEFENDER -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.NO_NONSENSE_DEFENDER
            PlayerArchetype.INSPIRATIONAL_CAPTAIN -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.ANCHOR_MAN
            PlayerArchetype.COMPLETE_FORWARD -> PlayerRole.DEEP_LYING_FORWARD to PlayerRole.TARGET_MAN
            PlayerArchetype.BOX_TO_BOX -> PlayerRole.BOX_TO_BOX to PlayerRole.CARRILERO
            PlayerArchetype.SWEEPING_KEEPER -> PlayerRole.SWEEPER_KEEPER to PlayerRole.GOALKEEPER
            PlayerArchetype.TRADITIONAL_STRIKER -> PlayerRole.POACHER to PlayerRole.TARGET_MAN
            PlayerArchetype.WING_WIZARD -> PlayerRole.WINGER to PlayerRole.INSIDE_FORWARD
            PlayerArchetype.ANCHOR_MAN -> PlayerRole.ANCHOR_MAN to PlayerRole.DEFENSIVE_MIDFIELDER
            PlayerArchetype.REGISTA -> PlayerRole.DEEP_LYING_PLAYMAKER to PlayerRole.REGISTA
            PlayerArchetype.LIBERO -> PlayerRole.LIBERO to PlayerRole.BALL_PLAYING_DEFENDER
            PlayerArchetype.PLAYMAKER -> PlayerRole.ADVANCED_PLAYMAKER to PlayerRole.ENGANCHE
            PlayerArchetype.WONDERKID -> PlayerRole.ADVANCED_PLAYMAKER to PlayerRole.INSIDE_FORWARD
            PlayerArchetype.POACHER -> PlayerRole.POACHER to PlayerRole.ADVANCED_FORWARD
            PlayerArchetype.POWERFUL_DEFENDER -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.NO_NONSENSE_DEFENDER
            PlayerArchetype.TECHNICAL_FORWARD -> PlayerRole.FALSE_9 to PlayerRole.INSIDE_FORWARD
            PlayerArchetype.EXPERIENCED_WINGER -> PlayerRole.WINGER to PlayerRole.ADVANCED_PLAYMAKER
            PlayerArchetype.PACEY_STRIKER -> PlayerRole.PRESSING_FORWARD to PlayerRole.ADVANCED_FORWARD
            PlayerArchetype.TARGET_MAN -> PlayerRole.TARGET_MAN to PlayerRole.DEEP_LYING_FORWARD
            PlayerArchetype.EXPERIENCED_DEFENDER -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.LIBERO
            PlayerArchetype.TECHNICAL_MIDFIELDER -> PlayerRole.ADVANCED_PLAYMAKER to PlayerRole.DEEP_LYING_PLAYMAKER
            PlayerArchetype.DOMINANT_DEFENDER -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.BALL_PLAYING_DEFENDER
            PlayerArchetype.VETERAN -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.ANCHOR_MAN
            PlayerArchetype.ADVANCED_PLAYMAKER -> PlayerRole.ADVANCED_PLAYMAKER to PlayerRole.ENGANCHE
            PlayerArchetype.YOUNG_SPEEDSTER -> PlayerRole.WINGER to PlayerRole.ADVANCED_FORWARD
            PlayerArchetype.DEFENSIVE_ROCK -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.NO_NONSENSE_DEFENDER
            PlayerArchetype.CREATIVE_MIDFIELDER -> PlayerRole.ADVANCED_PLAYMAKER to PlayerRole.DEEP_LYING_PLAYMAKER
            PlayerArchetype.DEFENSIVE_LEADER -> PlayerRole.CENTRAL_DEFENDER to PlayerRole.LIBERO
            PlayerArchetype.DEFENSIVE_SHIELD -> PlayerRole.DEFENSIVE_MIDFIELDER to PlayerRole.ANCHOR_MAN
            PlayerArchetype.ACROBATIC_GOALKEEPER -> PlayerRole.GOALKEEPER to PlayerRole.SWEEPER_KEEPER
            PlayerArchetype.CREATIVE_VETERAN -> PlayerRole.DEEP_LYING_PLAYMAKER to PlayerRole.ADVANCED_PLAYMAKER
            PlayerArchetype.MIDFIELD_DYNAMO -> PlayerRole.BOX_TO_BOX to PlayerRole.BALL_WINNING_MIDFIELDER
            PlayerArchetype.TRADITIONAL_GOALKEEPER -> PlayerRole.GOALKEEPER to PlayerRole.GOALKEEPER
            PlayerArchetype.SPEEDSTER -> PlayerRole.WINGER to PlayerRole.ADVANCED_FORWARD
        }
    }

    /**
     * Applies tactical modifications based on the roles of the players in the lineup.
     */
    fun applyLineupRoleEffects(players: List<PlayersEntity>, baseTactics: TacticsEntity): TacticsEntity {
        var modifiedTactics = baseTactics
        players.forEach { player ->
            val role = mapArchetypeToRoles(player.archetype).first
            modifiedTactics = applyRoleEffects(role, modifiedTactics)
        }
        return modifiedTactics
    }

    private fun applyRoleEffects(role: PlayerRole, tactics: TacticsEntity): TacticsEntity {
        return when (role) {
            PlayerRole.POACHER -> tactics.copy(
                attackingThreshold = (tactics.attackingThreshold + 4).coerceAtMost(100),
                creativity = (tactics.creativity - 2).coerceAtLeast(0)
            )
            PlayerRole.ADVANCED_PLAYMAKER -> tactics.copy(
                creativity = (tactics.creativity + 5).coerceAtMost(100),
                tempo = (tactics.tempo - 2).coerceAtLeast(0),
                passingDirectness = (tactics.passingDirectness - 2).coerceAtLeast(0)
            )
            PlayerRole.ANCHOR_MAN -> tactics.copy(
                defensiveThreshold = (tactics.defensiveThreshold + 5).coerceAtMost(100),
                pressIntensity = (tactics.pressIntensity - 2).coerceAtLeast(0)
            )
            PlayerRole.WINGBACK -> tactics.copy(
                width = (tactics.width + 4).coerceAtMost(100),
                attackingThreshold = (tactics.attackingThreshold + 2).coerceAtMost(100)
            )
            PlayerRole.PRESSING_FORWARD -> tactics.copy(
                pressIntensity = (tactics.pressIntensity + 5).coerceAtMost(100),
                defensiveThreshold = (tactics.defensiveThreshold - 2).coerceAtLeast(0)
            )
            PlayerRole.FALSE_9 -> tactics.copy(
                creativity = (tactics.creativity + 3).coerceAtMost(100),
                attackingThreshold = (tactics.attackingThreshold - 2).coerceAtLeast(0),
                tempo = (tactics.tempo - 1).coerceAtLeast(0)
            )
            PlayerRole.INSIDE_FORWARD -> tactics.copy(
                attackingThreshold = (tactics.attackingThreshold + 3).coerceAtMost(100),
                width = (tactics.width - 2).coerceAtLeast(0)
            )
            PlayerRole.SHADOW_STRIKER -> tactics.copy(
                attackingThreshold = (tactics.attackingThreshold + 4).coerceAtMost(100),
                depth = (tactics.depth + 2).coerceAtMost(100)
            )
            PlayerRole.DEEP_LYING_PLAYMAKER -> tactics.copy(
                tempo = (tactics.tempo - 3).coerceAtLeast(0),
                creativity = (tactics.creativity + 3).coerceAtMost(100)
            )
            PlayerRole.CENTRAL_DEFENDER -> tactics.copy(
                defensiveThreshold = (tactics.defensiveThreshold + 2).coerceAtMost(100)
            )
            PlayerRole.NO_NONSENSE_DEFENDER -> tactics.copy(
                defensiveThreshold = (tactics.defensiveThreshold + 4).coerceAtMost(100),
                passingDirectness = (tactics.passingDirectness + 4).coerceAtMost(100)
            )
            PlayerRole.DEEP_LYING_FORWARD -> tactics.copy(
                creativity = (tactics.creativity + 2).coerceAtMost(100),
                attackingThreshold = (tactics.attackingThreshold + 1).coerceAtMost(100)
            )
            PlayerRole.TARGET_MAN -> tactics.copy(
                passingDirectness = (tactics.passingDirectness + 5).coerceAtMost(100),
                tempo = (tactics.tempo - 2).coerceAtLeast(0)
            )
            PlayerRole.BOX_TO_BOX -> tactics.copy(
                pressIntensity = (tactics.pressIntensity + 3).coerceAtMost(100),
                defensiveThreshold = (tactics.defensiveThreshold + 2).coerceAtMost(100),
                attackingThreshold = (tactics.attackingThreshold + 2).coerceAtMost(100)
            )
            PlayerRole.CARRILERO -> tactics.copy(
                width = (tactics.width + 2).coerceAtMost(100),
                defensiveThreshold = (tactics.defensiveThreshold + 2).coerceAtMost(100)
            )
            PlayerRole.SWEEPER_KEEPER -> tactics.copy(
                depth = (tactics.depth + 4).coerceAtMost(100),
                passingDirectness = (tactics.passingDirectness - 2).coerceAtLeast(0)
            )
            PlayerRole.WINGER -> tactics.copy(
                width = (tactics.width + 5).coerceAtMost(100),
                attackingThreshold = (tactics.attackingThreshold + 2).coerceAtMost(100)
            )
            PlayerRole.DEFENSIVE_MIDFIELDER -> tactics.copy(
                defensiveThreshold = (tactics.defensiveThreshold + 3).coerceAtMost(100),
                pressIntensity = (tactics.pressIntensity + 2).coerceAtMost(100)
            )
            PlayerRole.REGISTA -> tactics.copy(
                creativity = (tactics.creativity + 4).coerceAtMost(100),
                tempo = (tactics.tempo - 4).coerceAtLeast(0)
            )
            PlayerRole.LIBERO -> tactics.copy(
                depth = (tactics.depth - 2).coerceAtLeast(0),
                creativity = (tactics.creativity + 3).coerceAtMost(100)
            )
            PlayerRole.BALL_PLAYING_DEFENDER -> tactics.copy(
                passingDirectness = (tactics.passingDirectness - 3).coerceAtLeast(0),
                creativity = (tactics.creativity + 2).coerceAtMost(100)
            )
            PlayerRole.ENGANCHE -> tactics.copy(
                tempo = (tactics.tempo - 5).coerceAtLeast(0),
                creativity = (tactics.creativity + 6).coerceAtMost(100)
            )
            PlayerRole.ADVANCED_FORWARD -> tactics.copy(
                attackingThreshold = (tactics.attackingThreshold + 5).coerceAtMost(100),
                depth = (tactics.depth + 3).coerceAtMost(100)
            )
            PlayerRole.BALL_WINNING_MIDFIELDER -> tactics.copy(
                pressIntensity = (tactics.pressIntensity + 6).coerceAtMost(100),
                defensiveThreshold = (tactics.defensiveThreshold + 2).coerceAtMost(100)
            )
            PlayerRole.GOALKEEPER -> tactics
        }
    }

    /**
     * Calculates synergy score based on the combination of roles in the starting XI.
     */
    fun calculateRoleSynergy(players: List<PlayersEntity>): Int {
        if (players.isEmpty()) return 0
        var score = 0
        val roles = players.map { mapArchetypeToRoles(it.archetype).first }

        // ✅ ATTACKING SYNERGIES
        if (roles.contains(PlayerRole.TARGET_MAN) && (roles.contains(PlayerRole.WINGER) || roles.contains(PlayerRole.INSIDE_FORWARD))) score += 10
        if (roles.contains(PlayerRole.ADVANCED_PLAYMAKER) && (roles.contains(PlayerRole.POACHER) || roles.contains(PlayerRole.ADVANCED_FORWARD))) score += 8
        if (roles.contains(PlayerRole.FALSE_9) && roles.contains(PlayerRole.INSIDE_FORWARD)) score += 12
        if (roles.contains(PlayerRole.SHADOW_STRIKER) && roles.contains(PlayerRole.TARGET_MAN)) score += 10
        if (roles.contains(PlayerRole.WINGBACK) && roles.contains(PlayerRole.INSIDE_FORWARD)) score += 7 // Overlap potential

        // ✅ MIDFIELD SYNERGIES
        if (roles.contains(PlayerRole.REGISTA) && (roles.contains(PlayerRole.BOX_TO_BOX) || roles.contains(PlayerRole.BALL_WINNING_MIDFIELDER))) score += 12
        if (roles.contains(PlayerRole.ANCHOR_MAN) && roles.contains(PlayerRole.ADVANCED_PLAYMAKER)) score += 8
        if (roles.contains(PlayerRole.DEEP_LYING_PLAYMAKER) && roles.contains(PlayerRole.PRESSING_FORWARD)) score += 6

        // ✅ DEFENSIVE SYNERGIES
        if (roles.contains(PlayerRole.SWEEPER_KEEPER) && (roles.contains(PlayerRole.BALL_PLAYING_DEFENDER) || roles.contains(PlayerRole.LIBERO))) score += 10
        if (roles.count { it == PlayerRole.CENTRAL_DEFENDER || it == PlayerRole.BALL_PLAYING_DEFENDER } >= 2) score += 5

        // ❌ NEGATIVE SYNERGIES (Role Clashing)
        if (roles.count { it == PlayerRole.POACHER } >= 2) score -= 12
        if (roles.count { it == PlayerRole.TARGET_MAN } >= 2) score -= 10
        if (roles.count { it == PlayerRole.ADVANCED_PLAYMAKER || it == PlayerRole.DEEP_LYING_PLAYMAKER || it == PlayerRole.REGISTA || it == PlayerRole.ENGANCHE } >= 3) score -= 15
        if (roles.count { it == PlayerRole.NO_NONSENSE_DEFENDER } >= 3) score -= 10 // Too limited build-up
        
        // ❌ LACK OF BALANCE
        if (!roles.any { it == PlayerRole.ANCHOR_MAN || it == PlayerRole.DEFENSIVE_MIDFIELDER || it == PlayerRole.BALL_WINNING_MIDFIELDER || it == PlayerRole.BOX_TO_BOX }) score -= 20
        if (!roles.any { it == PlayerRole.POACHER || it == PlayerRole.ADVANCED_FORWARD || it == PlayerRole.TARGET_MAN || it == PlayerRole.SHADOW_STRIKER }) score -= 15

        return score.coerceIn(-40, 40)
    }

    /**
     * Returns an identity description based on dominant roles.
     */
    fun getTeamIdentity(players: List<PlayersEntity>): String {
        if (players.isEmpty()) return "Unknown"
        val roles = players.map { mapArchetypeToRoles(it.archetype).first }
        
        return when {
            roles.count { it == PlayerRole.WINGER || it == PlayerRole.INSIDE_FORWARD } >= 3 -> "Wide Attacking Team"
            roles.count { it == PlayerRole.DEEP_LYING_PLAYMAKER || it == PlayerRole.ADVANCED_PLAYMAKER || it == PlayerRole.REGISTA } >= 2 -> "Technical Possession Side"
            roles.count { it == PlayerRole.BALL_WINNING_MIDFIELDER || it == PlayerRole.PRESSING_FORWARD } >= 3 -> "High Intensity Pressing"
            roles.count { it == PlayerRole.NO_NONSENSE_DEFENDER || it == PlayerRole.ANCHOR_MAN } >= 4 -> "Low Block Defensive Unit"
            roles.contains(PlayerRole.TARGET_MAN) && roles.contains(PlayerRole.WINGER) -> "Direct Wing Play"
            else -> "Balanced System"
        }
    }
}
