package com.fameafrica.afm.domain.model.core

/**
 * Core Domain Model for a Match Fixture.
 */
data class Match(
    val id: Int,
    val homeTeamId: Int,
    val homeTeamName: String,
    val awayTeamId: Int,
    val awayTeamName: String,
    val matchDate: String,
    val competition: String,
    val venue: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val status: String // SCHEDULED, LIVE, COMPLETED
) {
    val isCompleted: Boolean get() = status == "COMPLETED"
}
