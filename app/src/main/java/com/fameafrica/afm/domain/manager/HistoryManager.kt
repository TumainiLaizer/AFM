package com.fameafrica.afm.domain.manager

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryManager @Inject constructor() {

    /**
     * Records a season's outcome for a club.
     */
    fun recordSeasonOutcome(
        teamId: Int,
        season: String,
        position: Int,
        points: Int,
        trophies: List<String>
    ) {
        // Implementation would involve saving to SeasonHistoryEntity and TrophiesEntity
    }

    /**
     * Fetches all records for a specific club.
     */
    fun getClubHistory(teamId: Int): List<ClubRecord> {
        // Mocking for now
        return listOf(
            ClubRecord("2024/25", "Tanzania Premier League", 1, "Champions"),
            ClubRecord("2023/24", "Tanzania Premier League", 2, "Runners-up")
        )
    }

    data class ClubRecord(
        val season: String,
        val competition: String,
        val position: Int,
        val achievement: String
    )
}
