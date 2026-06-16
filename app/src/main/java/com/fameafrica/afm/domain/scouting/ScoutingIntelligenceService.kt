package com.fameafrica.afm.domain.scouting

import com.fameafrica.afm.data.database.entities.ClubDNAEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.ScoutAssignmentsEntity
import com.fameafrica.afm.data.database.entities.StaffEntity
import com.fameafrica.afm.data.database.model.PlayerSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class ScoutingIntelligenceService @Inject constructor() {

    /**
     * Calculates the Scout Score (0-100) based on the FM-style recruitment formula.
     */
    fun calculateScoutScore(
        player: PlayersEntity,
        scoutReport: ScoutAssignmentsEntity?,
        scout: StaffEntity?,
        squadNeeds: List<String> = emptyList(),
        clubDNA: ClubDNAEntity? = null
    ): Int {
        val currentAbility = player.rating.toDouble()
        val potentialAbility = player.potential.toDouble()
        val formScore = player.currentForm * 10.0
        val consistency = 70.0
        val injuryPenalty = if (player.injuryRisk > 50) -10.0 else 0.0
        
        val ageFactor = when {
            player.age in 16..21 -> 95.0
            player.age in 22..26 -> 80.0
            player.age in 27..30 -> 60.0
            else -> 40.0
        }
        
        val normalizedValue = player.marketValue.toDouble() / 10_000_000.0
        val valueForMoney = (player.potential / max(1.0, normalizedValue)).coerceIn(0.0, 100.0)
        val isNeeded = if (squadNeeds.contains(player.position)) 90.0 else 50.0

        val dnaAlignment = if (clubDNA != null) {
            calculateDNAPlaystyleFit(player, clubDNA) * 100.0
        } else {
            70.0
        }
        
        val scoutKnowledge = scout?.impactRating?.toDouble() ?: 50.0

        val score = (currentAbility * 0.15) +
                (potentialAbility * 0.20) +
                (formScore * 0.10) +
                (consistency * 0.05) +
                (injuryPenalty) +
                (ageFactor * 0.10) +
                (valueForMoney * 0.10) +
                (isNeeded * 0.10) +
                (dnaAlignment * 0.10) +
                (scoutKnowledge * 0.10)

        return score.toInt().coerceIn(0, 100)
    }

    /**
     * Overload for PlayerSummary to save memory during mass calculations.
     */
    fun calculateScoutScore(
        player: PlayerSummary,
        scoutReport: ScoutAssignmentsEntity?,
        scout: StaffEntity?,
        squadNeeds: List<String> = emptyList(),
        clubDNA: ClubDNAEntity? = null
    ): Int {
        val currentAbility = player.rating.toDouble()
        val potentialAbility = player.potential.toDouble()
        val formScore = player.currentForm * 10.0
        val consistency = 70.0
        val injuryPenalty = if (player.injuryRisk > 50) -10.0 else 0.0
        
        val ageFactor = when {
            player.age in 16..21 -> 95.0
            player.age in 22..26 -> 80.0
            player.age in 27..30 -> 60.0
            else -> 40.0
        }
        
        val normalizedValue = player.marketValue.toDouble() / 10_000_000.0
        val valueForMoney = (player.potential / max(1.0, normalizedValue)).coerceIn(0.0, 100.0)
        val isNeeded = if (squadNeeds.contains(player.position)) 90.0 else 50.0

        val dnaAlignment = if (clubDNA != null) {
            calculateDNAPlaystyleFit(player, clubDNA) * 100.0
        } else {
            70.0
        }
        
        val scoutKnowledge = scout?.impactRating?.toDouble() ?: 50.0

        val score = (currentAbility * 0.15) +
                (potentialAbility * 0.20) +
                (formScore * 0.10) +
                (consistency * 0.05) +
                (injuryPenalty) +
                (ageFactor * 0.10) +
                (valueForMoney * 0.10) +
                (isNeeded * 0.10) +
                (dnaAlignment * 0.10) +
                (scoutKnowledge * 0.10)

        return score.toInt().coerceIn(0, 100)
    }

    fun calculateDNAPlaystyleFit(player: PlayersEntity, dna: ClubDNAEntity): Double {
        val playStyle = dna.playStyle.uppercase()
        val primaryFit = getDNAMultiplier(player, playStyle)
        val secondaryPlayStyle = dna.playStyleSecondary?.uppercase()
        val secondaryFit = if (secondaryPlayStyle != null) getDNAMultiplier(player, secondaryPlayStyle) else primaryFit
        return (primaryFit * dna.primaryWeight) + (secondaryFit * dna.secondaryWeight)
    }

    fun calculateDNAPlaystyleFit(player: PlayerSummary, dna: ClubDNAEntity): Double {
        val playStyle = dna.playStyle.uppercase()
        val primaryFit = getDNAMultiplier(player, playStyle)
        val secondaryPlayStyle = dna.playStyleSecondary?.uppercase()
        val secondaryFit = if (secondaryPlayStyle != null) getDNAMultiplier(player, secondaryPlayStyle) else primaryFit
        return (primaryFit * dna.primaryWeight) + (secondaryFit * dna.secondaryWeight)
    }

    private fun getDNAMultiplier(player: PlayersEntity, style: String): Double {
        return when (style) {
            "POSSESSION" -> (player.passing * 0.4 + player.vision * 0.3 + player.composure * 0.3) / 100.0
            "COUNTER" -> (player.pace * 0.4 + player.acceleration * 0.3 + player.stamina * 0.3) / 100.0
            "DEFENSIVE" -> (player.defending * 0.4 + player.positioning * 0.3 + player.strength * 0.3) / 100.0
            "GEGENPRESS" -> (player.stamina * 0.4 + player.aggression * 0.3 + player.pace * 0.3) / 100.0
            "DIRECT_PHYSICAL" -> (player.strength * 0.4 + player.heading * 0.3 + player.stamina * 0.3) / 100.0
            "FLAIR_EXPRESSIVE" -> (player.dribbling * 0.4 + player.creativity * 0.3 + player.skill * 0.3) / 100.0
            "WING_PLAY" -> (player.crossing * 0.4 + player.pace * 0.3 + player.acceleration * 0.3) / 100.0
            "TRANSITION_HEAVY" -> (player.anticipation * 0.3 + player.decisions * 0.3 + player.stamina * 0.4) / 100.0
            "TACTICAL_DISCIPLINE" -> (player.positioning * 0.4 + player.decisions * 0.3 + player.teamwork * 0.3) / 100.0
            "YOUTH_ENERGY" -> (player.stamina * 0.4 + player.pace * 0.3 + player.agility * 0.3) / 100.0
            else -> 0.7
        }
    }

    private fun getDNAMultiplier(player: PlayerSummary, style: String): Double {
        return when (style) {
            "POSSESSION" -> (player.passing * 0.4 + player.vision * 0.3 + player.composure * 0.3) / 100.0
            "COUNTER" -> (player.pace * 0.4 + player.acceleration * 0.3 + player.stamina * 0.3) / 100.0
            "DEFENSIVE" -> (player.defending * 0.4 + player.positioning * 0.3 + player.strength * 0.3) / 100.0
            "GEGENPRESS" -> (player.stamina * 0.4 + player.aggression * 0.3 + player.pace * 0.3) / 100.0
            "DIRECT_PHYSICAL" -> (player.strength * 0.4 + player.heading * 0.3 + player.stamina * 0.3) / 100.0
            "FLAIR_EXPRESSIVE" -> (player.dribbling * 0.4 + player.creativity * 0.3 + player.skill * 0.3) / 100.0
            "WING_PLAY" -> (player.crossing * 0.4 + player.pace * 0.3 + player.acceleration * 0.3) / 100.0
            "TRANSITION_HEAVY" -> (player.anticipation * 0.3 + player.decisions * 0.3 + player.stamina * 0.4) / 100.0
            "TACTICAL_DISCIPLINE" -> (player.positioning * 0.4 + player.decisions * 0.3 + player.teamwork * 0.3) / 100.0
            "YOUTH_ENERGY" -> (player.stamina * 0.4 + player.pace * 0.3 + player.agility * 0.3) / 100.0
            else -> 0.7
        }
    }

    fun calculateWonderkidScore(player: PlayersEntity): Int {
        if (player.age > 21) return 0
        val potentialScore = player.potential.toDouble()
        val ageScore = when {
            player.age <= 18 -> 100.0
            player.age <= 19 -> 90.0
            player.age <= 21 -> 75.0
            else -> 0.0
        }
        val growthRate = (player.potential - player.rating).toDouble() * 2.5
        val performanceScore = player.currentForm * 10.0
        val personalityScore = when (player.personalityType) {
            "PROFESSIONAL", "AMBITIOUS" -> 100.0
            "LAZY" -> 40.0
            else -> 70.0
        }
        val scoutConfidence = 80.0
        val score = (potentialScore * 0.35) + (ageScore * 0.25) + (growthRate * 0.15) + (performanceScore * 0.10) + (personalityScore * 0.10) + (scoutConfidence * 0.05)
        return score.toInt().coerceIn(0, 100)
    }

    fun calculateWonderkidScore(player: PlayerSummary): Int {
        if (player.age > 21) return 0
        val potentialScore = player.potential.toDouble()
        val ageScore = when {
            player.age <= 18 -> 100.0
            player.age <= 19 -> 90.0
            player.age <= 21 -> 75.0
            else -> 0.0
        }
        val growthRate = (player.potential - player.rating).toDouble() * 2.5
        val performanceScore = player.currentForm * 10.0
        val personalityScore = when (player.personalityType) {
            "PROFESSIONAL", "AMBITIOUS" -> 100.0
            "LAZY" -> 40.0
            else -> 70.0
        }
        val scoutConfidence = 80.0
        val score = (potentialScore * 0.35) + (ageScore * 0.25) + (growthRate * 0.15) + (performanceScore * 0.10) + (personalityScore * 0.10) + (scoutConfidence * 0.05)
        return score.toInt().coerceIn(0, 100)
    }

    fun getRecommendationLabel(score: Int): String {
        return when {
            score >= 85 -> "World Class Signing"
            score >= 70 -> "Strong Recommendation"
            score >= 55 -> "Decent Option"
            score >= 50 -> "Plausible Option"
            else -> "Not Recommended"
        }
    }

    fun getWonderkidClassification(score: Int): String {
        return when {
            score >= 85 -> "Elite Wonderkid"
            score >= 75 -> "High Potential Prospect"
            score >= 65 -> "Emerging Talent"
            else -> "Standard Prospect"
        }
    }
}
