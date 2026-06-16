package com.fameafrica.afm.domain

import kotlin.math.pow

object EloCalculator {

    private const val K_FACTOR_LEAGUE = 20
    private const val K_FACTOR_CUP = 30
    private const val K_FACTOR_FINAL = 50
    private const val K_FACTOR_DERBY = 40

    fun calculateNewRatings(
        homeTeam: Int,
        awayTeam: Int,
        homeScore: Int,
        awayScore: Int,
        matchType: String
    ): Pair<Int, Int> {
        val kFactor = when (matchType) {
            "LEAGUE" -> K_FACTOR_LEAGUE
            "CUP" -> K_FACTOR_CUP
            "FINAL" -> K_FACTOR_FINAL
            "DERBY" -> K_FACTOR_DERBY
            else -> K_FACTOR_LEAGUE
        }

        val homeExpected = calculateWinProbability(homeTeam, awayTeam, true)
        val awayExpected = 1.0 - homeExpected

        val homeActual = when {
            homeScore > awayScore -> 1.0
            homeScore < awayScore -> 0.0
            else -> 0.5
        }
        val awayActual = 1.0 - homeActual

        val homeNewElo = homeTeam + kFactor * (homeActual - homeExpected)
        val awayNewElo = awayTeam + kFactor * (awayActual - awayExpected)

        return Pair(homeNewElo.toInt(), awayNewElo.toInt())
    }

    fun calculateWinProbability(
        playerRating: Int,
        opponentRating: Int,
        isHome: Boolean
    ): Double {
        val homeAdvantage = if (isHome) 100 else 0
        val ratingDifference = opponentRating - (playerRating + homeAdvantage)
        return 1.0 / (1.0 + 10.0.pow(ratingDifference / 400.0))
    }
}
