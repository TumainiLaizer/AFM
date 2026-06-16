package com.fameafrica.afm.utils.calculators

import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Elo Rating Calculation Engine
 *
 * General Trend: Elo should reward dominance and penalize repeated failure,
 * but not by too much to maintain realistic simulation dynamics.
 *
 * Key Principles:
 * - K-factor: 32 for normal matches, 40 for cup matches, 48 for derbies/finals
 * - Expected score based on rating difference
 * - Actual score: 1 for win, 0.5 for draw, 0 for loss
 * - Home advantage: +50-100 Elo points adjustment
 */
object EloCalculator {

    // Base K-factors (determines how much Elo changes per match)
    private const val K_FACTOR_LEAGUE = 32
    private const val K_FACTOR_CUP = 40
    private const val K_FACTOR_DERBY = 48
    private const val K_FACTOR_FINAL = 48

    // Home advantage adjustment (Elo points added to home team's effective rating)
    private const val HOME_ADVANTAGE = 70

    // Minimum and maximum Elo ratings
    private const val MIN_ELO = 1000
    private const val MAX_ELO = 2200

    // Thresholds for dominance bonuses
    private const val DOMINANCE_THRESHOLD = 200  // 200+ point difference = dominance
    private const val DOMINANCE_BONUS_MULTIPLIER = 1.2

    /**
     * Calculate expected score for team A against team B
     * Formula: 1 / (1 + 10^((ratingB - ratingA) / 400))
     */
    fun calculateExpectedScore(ratingA: Int, ratingB: Int): Double {
        val exponent = (ratingB - ratingA).toDouble() / 400.0
        return 1.0 / (1.0 + 10.0.pow(exponent))
    }

    /**
     * Calculate new Elo ratings after a match
     *
     * @param homeTeam Current Elo of home team
     * @param awayTeam Current Elo of away team
     * @param homeScore Goals scored by home team
     * @param awayScore Goals scored by away team
     * @param matchType "LEAGUE", "CUP", "DERBY", "FINAL"
     * @param isNeutralVenue Whether match is at neutral venue
     * @return Pair of (newHomeElo, newAwayElo)
     */
    fun calculateNewRatings(
        homeTeam: Int,
        awayTeam: Int,
        homeScore: Int,
        awayScore: Int,
        matchType: String = "LEAGUE",
        isNeutralVenue: Boolean = false
    ): Pair<Int, Int> {

        // Apply home advantage adjustment
        val effectiveHome = if (!isNeutralVenue) homeTeam + HOME_ADVANTAGE else homeTeam
        val effectiveAway = awayTeam

        // Calculate expected scores
        val expectedHome = calculateExpectedScore(effectiveHome, effectiveAway)
        val expectedAway = 1.0 - expectedHome

        // Determine actual scores
        val (actualHome, actualAway) = when {
            homeScore > awayScore -> 1.0 to 0.0  // Home win
            awayScore > homeScore -> 0.0 to 1.0  // Away win
            else -> 0.5 to 0.5  // Draw
        }

        // Select K-factor based on match importance
        val kFactor = when (matchType.uppercase()) {
            "DERBY", "FINAL" -> K_FACTOR_DERBY
            "CUP" -> K_FACTOR_CUP
            else -> K_FACTOR_LEAGUE
        }

        // Calculate rating changes
        var homeChange = kFactor * (actualHome - expectedHome)
        var awayChange = kFactor * (actualAway - expectedAway)

        // Apply dominance bonus for blowout wins
        val goalDifference = (homeScore - awayScore).absoluteValue
        if (goalDifference >= 3) {
            val dominanceFactor = 1.0 + (goalDifference * 0.05)
            if (homeScore > awayScore) {
                homeChange *= dominanceFactor
                awayChange *= 0.8  // Penalize losing team more for blowout
            } else {
                awayChange *= dominanceFactor
                homeChange *= 0.8
            }
        }

        // Apply Elo difference bonus for upsets
        val eloDifference = (homeTeam - awayTeam).absoluteValue
        if (eloDifference > DOMINANCE_THRESHOLD) {
            val upsetFactor = DOMINANCE_BONUS_MULTIPLIER
            // If lower-rated team wins, give them extra boost
            if (homeScore > awayScore && homeTeam < awayTeam) {
                homeChange *= upsetFactor
            } else if (awayScore > homeScore && awayTeam < homeTeam) {
                awayChange *= upsetFactor
            }
        }

        // Calculate new ratings and clamp to limits
        val newHome = (homeTeam + homeChange).toInt().coerceIn(MIN_ELO, MAX_ELO)
        val newAway = (awayTeam + awayChange).toInt().coerceIn(MIN_ELO, MAX_ELO)

        return Pair(newHome, newAway)
    }

    /**
     * Calculate new Elo for a single team (for tournament progression)
     */
    fun calculateSingleTeamNewRating(
        teamElo: Int,
        opponentElo: Int,
        result: String,  // "WIN", "LOSS", "DRAW"
        matchType: String = "LEAGUE",
        isNeutralVenue: Boolean = false
    ): Int {

        val effectiveTeam = teamElo
        val effectiveOpponent = opponentElo

        val expected = calculateExpectedScore(effectiveTeam, effectiveOpponent)

        val actual = when (result.uppercase()) {
            "WIN" -> 1.0
            "LOSS" -> 0.0
            else -> 0.5
        }

        val kFactor = when (matchType.uppercase()) {
            "DERBY", "FINAL" -> K_FACTOR_DERBY
            "CUP" -> K_FACTOR_CUP
            else -> K_FACTOR_LEAGUE
        }

        val change = kFactor * (actual - expected)

        return (teamElo + change).toInt().coerceIn(MIN_ELO, MAX_ELO)
    }

    /**
     * Calculate expected result for match prediction
     */
    fun calculateWinProbability(teamA: Int, teamB: Int, homeAdvantage: Boolean = true): Double {
        val effectiveA = if (homeAdvantage) teamA + HOME_ADVANTAGE else teamA
        return calculateExpectedScore(effectiveA, teamB)
    }

    /**
     * Determine if match is an upset based on Elo ratings
     */
    fun isUpset(winnerElo: Int, loserElo: Int, winnerIsHome: Boolean): Boolean {
        val effectiveWinner = if (winnerIsHome) winnerElo + HOME_ADVANTAGE else winnerElo
        return effectiveWinner < loserElo
    }

    /**
     * Calculate upset factor (how surprising the result is)
     */
    fun calculateUpsetFactor(winnerElo: Int, loserElo: Int, winnerIsHome: Boolean): Double {
        val effectiveWinner = if (winnerIsHome) winnerElo + HOME_ADVANTAGE else winnerElo
        val expectedWinner = calculateExpectedScore(effectiveWinner, loserElo)

        // Upset factor is inverse of expected probability (1.0 = even match, >1.0 = upset)
        return if (expectedWinner > 0) 1.0 / expectedWinner else 2.0
    }
}