package com.fameafrica.afm.domain.scouting

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.domain.transfer.TransferNegotiationEngine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ScoutReportEngine @Inject constructor(
    private val transferNegotiationEngine: TransferNegotiationEngine
) {

    /**
     * Generates a professional scout report.
     */
    suspend fun generateDetailedReport(
        player: PlayersEntity,
        scout: StaffEntity?
    ): ProfessionalReport {
        val scoutAbility = scout?.impactRating ?: 60
        val accuracy = scoutAbility / 100.0
        
        // 1. Attribute Ranges (Fog of War)
        val rangeWidth = (1.0 - accuracy) * 15
        val ratingRange = IntRange(
            (player.rating - Random.nextDouble(0.0, rangeWidth)).toInt().coerceIn(1, 99),
            (player.rating + Random.nextDouble(0.0, rangeWidth)).toInt().coerceIn(1, 99)
        )
        
        // 2. Pros and Cons
        val pros = mutableListOf<String>()
        val cons = mutableListOf<String>()
        
        if (player.pace >= 80) pros.add("Extremely explosive in transitions")
        if (player.passing >= 80) pros.add("Capable of dictating match tempo")
        if (player.leadership >= 75) pros.add("Natural leadership qualities")
        if (player.finishing >= 80) pros.add("Clinical finisher in the box")
        if (player.potential - player.rating > 10) pros.add("Significant ceiling for growth")
        
        if (player.stamina < 50) cons.add("Conditioning is a major concern for 90 mins")
        if (player.decisions < 50) cons.add("Poor decision-making under pressure")
        if (player.injuryRisk > 60) cons.add("Worrying injury history")
        if (player.workRate == "LOW") cons.add("Questionable work ethic in defensive phases")

        // 3. Market Estimation
        val estimatedValue = transferNegotiationEngine.calculateMarketValue(player)
        
        return ProfessionalReport(
            ratingRange = ratingRange,
            pros = pros.shuffled().take(3),
            cons = cons.shuffled().take(2),
            estimatedValue = estimatedValue,
            tacticalRecommendation = generateTacticalRec(player),
            verdict = determineVerdict(player, scoutAbility)
        )
    }

    private fun generateTacticalRec(player: PlayersEntity): String {
        return when {
            player.position == "GK" -> "Reliable shot-stopper, fits a structured defense."
            player.passing >= 75 && player.vision >= 70 -> "Suited for a possession-based 'Tiki-Taka' style."
            player.pace >= 80 && player.dribbling >= 70 -> "Perfect for a high-intensity counter-attacking setup."
            player.defending >= 75 && player.strength >= 75 -> "Strong physical presence, ideal for a deep block."
            else -> "Versatile player, can adapt to multiple systems."
        }
    }

    private fun determineVerdict(player: PlayersEntity, scoutAbility: Int): ScoutVerdict {
        val score = (player.rating * 0.4 + player.potential * 0.6)
        return when {
            score >= 82 -> ScoutVerdict.RECOMMENDED
            score >= 70 -> ScoutVerdict.WATCH
            else -> ScoutVerdict.NOT_RECOMMENDED
        }
    }

    data class ProfessionalReport(
        val ratingRange: IntRange,
        val pros: List<String>,
        val cons: List<String>,
        val estimatedValue: Long,
        val tacticalRecommendation: String,
        val verdict: ScoutVerdict
    )
}
