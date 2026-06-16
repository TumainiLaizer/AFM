package com.fameafrica.afm.utils.tactics

import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.TacticsEntity

data class TacticalInsight(
    val type: String, // POSITIVE, NEGATIVE, WARNING
    val message: String,
    val zone: String // DEFENSE, MIDFIELD, ATTACK, GENERAL
)

data class ProbabilisticBreakdown(
    val winProb: Double,
    val drawProb: Double,
    val lossProb: Double,
    val tacticalAdvantage: Double, // % boost from tactic matchup
    val teamStrengthAdvantage: Double, // % boost from ELO/Rating
    val familiarityImpact: Double, // % impact from familiarity
    val roleSynergyImpact: Double, // % impact from player role synergy (NEW)
    val moraleImpact: Double = 0.0
)

/**
 * Tactical Matchup Engine
 * Calculates match probabilities based on archetype matchups, familiarity, roles, and manager influence.
 */
object TacticalMatchupEngine {

    private val probabilityMatrix = mapOf(
        "POSSESSION" to mapOf(
            "POSSESSION" to Triple(33, 34, 33),
            "ATTACKING" to Triple(40, 25, 35),
            "BALANCED" to Triple(35, 35, 30),
            "COUNTER" to Triple(40, 25, 35),
            "DEFENSIVE" to Triple(45, 35, 20),
            "PRESSING" to Triple(35, 25, 40),
            "SPECIALIZED" to Triple(35, 30, 35)
        ),
        "ATTACKING" to mapOf(
            "POSSESSION" to Triple(35, 25, 40),
            "ATTACKING" to Triple(33, 34, 33),
            "BALANCED" to Triple(40, 30, 30),
            "COUNTER" to Triple(45, 25, 30),
            "DEFENSIVE" to Triple(50, 30, 20),
            "PRESSING" to Triple(45, 20, 35),
            "SPECIALIZED" to Triple(40, 25, 35)
        ),
        "BALANCED" to mapOf(
            "POSSESSION" to Triple(30, 35, 35),
            "ATTACKING" to Triple(30, 30, 40),
            "BALANCED" to Triple(33, 34, 33),
            "COUNTER" to Triple(35, 35, 30),
            "DEFENSIVE" to Triple(35, 40, 25),
            "PRESSING" to Triple(30, 25, 45),
            "SPECIALIZED" to Triple(35, 30, 35)
        ),
        "COUNTER" to mapOf(
            "POSSESSION" to Triple(35, 25, 40),
            "ATTACKING" to Triple(30, 25, 45),
            "BALANCED" to Triple(30, 35, 35),
            "COUNTER" to Triple(30, 40, 30),
            "DEFENSIVE" to Triple(30, 40, 30),
            "PRESSING" to Triple(30, 20, 50),
            "SPECIALIZED" to Triple(35, 30, 35)
        ),
        "DEFENSIVE" to mapOf(
            "POSSESSION" to Triple(20, 35, 45),
            "ATTACKING" to Triple(20, 30, 50),
            "BALANCED" to Triple(25, 40, 35),
            "COUNTER" to Triple(30, 40, 30),
            "DEFENSIVE" to Triple(25, 50, 25),
            "PRESSING" to Triple(20, 25, 55),
            "SPECIALIZED" to Triple(30, 35, 35)
        ),
        "PRESSING" to mapOf(
            "POSSESSION" to Triple(40, 25, 35),
            "ATTACKING" to Triple(35, 20, 45),
            "BALANCED" to Triple(45, 25, 30),
            "COUNTER" to Triple(50, 20, 30),
            "DEFENSIVE" to Triple(55, 25, 20),
            "PRESSING" to Triple(33, 34, 33),
            "SPECIALIZED" to Triple(45, 25, 30)
        ),
        "SPECIALIZED" to mapOf(
            "POSSESSION" to Triple(35, 30, 35),
            "ATTACKING" to Triple(35, 25, 40),
            "BALANCED" to Triple(35, 30, 35),
            "COUNTER" to Triple(35, 30, 35),
            "DEFENSIVE" to Triple(35, 35, 30),
            "PRESSING" to Triple(30, 25, 45),
            "SPECIALIZED" to Triple(33, 34, 33)
        )
    )

    fun calculateDetailedBreakdown(
        homeTactics: TacticsEntity,
        awayTactics: TacticsEntity,
        homeElo: Int,
        homeAvgAbility: Double,
        awayElo: Int,
        awayAvgAbility: Double,
        homePlayers: List<PlayersEntity> = emptyList() // Added for role synergy
    ): ProbabilisticBreakdown {
        val baseProbs = probabilityMatrix[homeTactics.tacticalArchetype]?.get(awayTactics.tacticalArchetype)
            ?: Triple(33, 34, 33)

        val tacticalAdv = (baseProbs.first - 33.0) / 100.0

        val eloWeight = 0.6
        val playerAbilityWeight = 0.4
        val homeStr = ((homeElo / 2000.0 * eloWeight) + (homeAvgAbility / 100.0 * playerAbilityWeight)) * 100.0
        val awayStr = ((awayElo / 2000.0 * eloWeight) + (awayAvgAbility / 100.0 * playerAbilityWeight)) * 100.0
        val strengthDiff = homeStr - awayStr
        val teamStrAdv = strengthDiff / 200.0

        val famImpact = (homeTactics.tacticalFamiliarity - 50.0) / 250.0 
        
        // --- ROLE SYNERGY IMPACT (NEW) ---
        val synergyScore = PlayerRoleManager.calculateRoleSynergy(homePlayers)
        val roleSynergyImpact = synergyScore / 100.0 // Max +/- 30% -> +/- 0.3

        var winProb = (baseProbs.first / 100.0) + teamStrAdv + famImpact + roleSynergyImpact
        var lossProb = (baseProbs.third / 100.0) - teamStrAdv - famImpact - roleSynergyImpact
        val drawProb = (baseProbs.second / 100.0)

        val total = winProb.coerceAtLeast(0.01) + drawProb.coerceAtLeast(0.01) + lossProb.coerceAtLeast(0.01)
        
        return ProbabilisticBreakdown(
            winProb = (winProb / total).coerceIn(0.01, 0.98),
            drawProb = (drawProb / total).coerceIn(0.01, 0.98),
            lossProb = (lossProb / total).coerceIn(0.01, 0.98),
            tacticalAdvantage = tacticalAdv,
            teamStrengthAdvantage = teamStrAdv,
            familiarityImpact = famImpact,
            roleSynergyImpact = roleSynergyImpact
        )
    }

    fun generateInsights(
        homeTactics: TacticsEntity,
        awayTactics: TacticsEntity,
        breakdown: ProbabilisticBreakdown,
        homePlayers: List<PlayersEntity> = emptyList()
    ): List<TacticalInsight> {
        val insights = mutableListOf<TacticalInsight>()

        // Archetype Matchup
        if (breakdown.tacticalAdvantage > 0.05) {
            insights.add(TacticalInsight("POSITIVE", "✅ Your ${homeTactics.tacticalArchetype} style is stretching their defense.", "GENERAL"))
        } else if (breakdown.tacticalAdvantage < -0.05) {
            insights.add(TacticalInsight("NEGATIVE", "⚠️ Opponent's ${awayTactics.tacticalArchetype} is exploiting your high line.", "DEFENSE"))
        }

        // Familiarity
        if (homeTactics.tacticalFamiliarity < 40) {
            insights.add(TacticalInsight("WARNING", "⚠️ Low tactical familiarity: Disorganized play likely.", "GENERAL"))
        } else if (homeTactics.tacticalFamiliarity > 80) {
            insights.add(TacticalInsight("POSITIVE", "✅ High familiarity: Team positioning is excellent.", "GENERAL"))
        }

        // Role Synergy (NEW)
        if (breakdown.roleSynergyImpact > 0.1) {
            insights.add(TacticalInsight("POSITIVE", "✅ Excellent role synergy: Players complement each other perfectly.", "GENERAL"))
        } else if (breakdown.roleSynergyImpact < -0.1) {
            insights.add(TacticalInsight("NEGATIVE", "⚠️ Poor role balance: Team lacks tactical cohesion.", "GENERAL"))
        }

        // Specific Role Logic
        val roles = homePlayers.map { PlayerRoleManager.mapArchetypeToRoles(it.archetype).first }
        if (!roles.any { it.name.contains("ANCHOR") || it.name.contains("DEFENSIVE") }) {
            insights.add(TacticalInsight("NEGATIVE", "⚠️ Lacking a defensive midfielder: Highly vulnerable to counters.", "MIDFIELD"))
        }
        if (roles.count { it.name.contains("PLAYMAKER") } >= 3) {
            insights.add(TacticalInsight("WARNING", "⚠️ Too many playmakers: Possession is slow, lacking penetration.", "MIDFIELD"))
        }

        // Specific Tactical Logic
        if (homeTactics.depth > 70 && awayTactics.playstyle == "COUNTER_ATTACK") {
            insights.add(TacticalInsight("NEGATIVE", "⚠️ Your high line is being exposed by fast strikers.", "DEFENSE"))
        }
        
        if (homeTactics.width > 70 && awayTactics.width < 40) {
            insights.add(TacticalInsight("POSITIVE", "✅ Your width is stretching their narrow defense.", "ATTACK"))
        }

        return insights
    }

    /**
     * Legacy method for simple archetype matchup.
     */
    fun calculateMatchupProbabilities(
        homeTactics: TacticsEntity,
        awayTactics: TacticsEntity
    ): Triple<Int, Int, Int> {
        val baseProbs = probabilityMatrix[homeTactics.tacticalArchetype]?.get(awayTactics.tacticalArchetype)
            ?: Triple(33, 34, 33)

        val homeInfluence = calculateManagerInfluence(homeTactics)
        val awayInfluence = calculateManagerInfluence(awayTactics)

        var win = baseProbs.first
        var loss = baseProbs.third

        win = (win + 5).coerceIn(0, 100)
        loss = (loss - 5).coerceIn(0, 100)

        win = (win + homeInfluence - awayInfluence).coerceIn(0, 100)
        loss = (loss - homeInfluence + awayInfluence).coerceIn(0, 100)

        val draw = (100 - win - loss).coerceIn(0, 100)
        return Triple(win, draw, loss)
    }

    private fun calculateManagerInfluence(tactics: TacticsEntity): Int {
        var influence = 0
        tactics.managerTacticalFlexibility?.let { influence += (it - 50) / 10 }
        tactics.managerPreferredStyle?.let { if (it == tactics.playstyle) influence += 5 }
        return (influence + (tactics.tacticalFamiliarity - 50) / 5).coerceIn(-15, 15)
    }

    fun calculateComprehensiveProbabilities(
        homeTactics: TacticsEntity,
        awayTactics: TacticsEntity,
        homeElo: Int,
        homeAvgAbility: Double,
        awayElo: Int,
        awayAvgAbility: Double,
        homePlayers: List<PlayersEntity> = emptyList()
    ): Triple<Double, Double, Double> {
        val breakdown = calculateDetailedBreakdown(homeTactics, awayTactics, homeElo, homeAvgAbility, awayElo, awayAvgAbility, homePlayers)
        return Triple(breakdown.winProb, breakdown.drawProb, breakdown.lossProb)
    }

    fun getArchetypeDescription(archetype: String): String {
        return when (archetype) {
            "POSSESSION" -> "Ball retention, tempo control, chance creation through patience"
            "ATTACKING" -> "High chance creation, overloads in attack, momentum swings"
            "BALANCED" -> "Stability, adaptability, fewer extremes"
            "COUNTER" -> "Exploits space, punishes possession-heavy sides"
            "DEFENSIVE" -> "Blocks space, frustrates opponents, high draw probability"
            "PRESSING" -> "Forces turnovers, creates chaos, high xG chances"
            else -> "Specialized tactics"
        }
    }

    fun mapStyleToArchetype(style: String?, formation: String?): String {
        val s = style?.lowercase() ?: ""
        val f = formation?.lowercase() ?: ""
        val combined = "$f $s"

        val mapping = listOf(
            "4-4-2 diamond attacking" to "SPECIALIZED",
            "4-4-2 wing play" to "SPECIALIZED",
            "3-4-2-1" to "SPECIALIZED",
            "4-2-2-2 box midfield" to "SPECIALIZED",
            "3-3-3-1" to "SPECIALIZED",
            "4-1-2-1-2 narrow" to "SPECIALIZED",
            "3-4-3 wing overload" to "SPECIALIZED",

            "4-1-2-3 vertical tiki-taka" to "POSSESSION",
            "4-2-3-1 control possession" to "POSSESSION",
            "3-2-4-1 build-up" to "POSSESSION",
            "4-2-3-1 possession" to "POSSESSION",
            "4-3-3 possession" to "POSSESSION",
            "4-1-4-1 possession" to "POSSESSION",
            "3-4-3 possession" to "POSSESSION",
            "4-2-2-2 possession" to "POSSESSION",

            "4-2-3-1 fluid attack" to "ATTACKING",
            "3-4-3 overlapping wings" to "ATTACKING",
            "4-1-3-2 attack" to "ATTACKING",
            "4-2-3-1 attacking" to "ATTACKING",
            "4-3-3 attacking" to "ATTACKING",
            "4-2-4" to "ATTACKING",
            "2-3-5" to "ATTACKING",
            "3-4-1-2 attack" to "ATTACKING",

            "4-4-2 direct play" to "COUNTER",
            "4-4-2 counter" to "COUNTER",
            "4-4-1-1 counter" to "COUNTER",
            "4-3-3 counter" to "COUNTER",
            "3-5-2 counter" to "COUNTER",
            "5-3-2 counter" to "COUNTER",
            "4-2-2-2 direct" to "COUNTER",
            "3-4-3 counter" to "COUNTER",

            "4-4-2 compact defense" to "DEFENSIVE",
            "4-1-4-1 low block" to "DEFENSIVE",
            "3-6-1 ultra defensive" to "DEFENSIVE",
            "5-3-2 defensive" to "DEFENSIVE",
            "5-4-1 defensive" to "DEFENSIVE",
            "4-4-2 defensive" to "DEFENSIVE",
            "5-2-3 defensive" to "DEFENSIVE",

            "3-5-2 gegenpressing" to "PRESSING",
            "4-3-3 high pressing" to "PRESSING",
            "4-3-3 fast build-up" to "PRESSING",
            "4-2-3-1 high press" to "PRESSING",
            "3-4-3 pressing" to "PRESSING",
            "4-1-3-2 press" to "PRESSING",
            "4-4-2 pressing" to "PRESSING",

            "4-5-1 structured midfield" to "BALANCED",
            "4-2-3-1 balanced" to "BALANCED",
            "4-3-3 balanced" to "BALANCED",
            "4-4-2 balanced" to "BALANCED",
            "3-5-2 balanced" to "BALANCED",
            "4-1-4-1 balanced" to "BALANCED",
            "4-3-1-2 balanced" to "BALANCED",

            "vertical tiki-taka" to "POSSESSION",
            "control possession" to "POSSESSION",
            "possession" to "POSSESSION",
            "build-up" to "POSSESSION",
            "fluid attack" to "ATTACKING",
            "overlapping wings" to "ATTACKING",
            "attacking" to "ATTACKING",
            "direct play" to "COUNTER",
            "counter-attack" to "COUNTER",
            "direct" to "COUNTER",
            "compact defense" to "DEFENSIVE",
            "low block" to "DEFENSIVE",
            "ultra defensive" to "DEFENSIVE",
            "defensive" to "DEFENSIVE",
            "compact" to "DEFENSIVE",
            "parking the bus" to "DEFENSIVE",
            "gegenpressing" to "PRESSING",
            "high pressing" to "PRESSING",
            "fast build-up" to "PRESSING",
            "high press" to "PRESSING",
            "pressing" to "PRESSING",
            "high tempo" to "PRESSING",
            "structured midfield" to "BALANCED",
            "balanced" to "BALANCED",
            "structured" to "BALANCED",
            "specialized" to "SPECIALIZED"
        )

        for ((keyword, archetype) in mapping) {
            if (combined.contains(keyword) || s.contains(keyword) || f.contains(keyword)) return archetype
        }

        return "BALANCED"
    }
}
