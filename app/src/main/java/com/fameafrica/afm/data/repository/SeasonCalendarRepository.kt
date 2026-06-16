package com.fameafrica.afm.data.repository

import com.fameafrica.afm.utils.GameDateManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonCalendarRepository @Inject constructor(
    private val gameDateManager: GameDateManager
) {
    enum class SeasonPhase {
        PRESEASON,      // June - July
        EARLY_SEASON,   // August - October
        MID_SEASON,     // November - February
        LATE_SEASON,    // March - May
        OFF_SEASON      // June
    }

    enum class SeasonEvent {
        TRANSFER_WINDOW_OPEN,
        PRESEASON_START,
        COMMUNITY_SHIELD,
        LEAGUE_START,
        CUP_ROUND_64,       // For cups with 64 teams
        CUP_ROUND_32,
        CUP_ROUND_16,
        CUP_QUARTER_FINAL,
        CUP_SEMI_FINAL,
        CUP_FINAL,
        CAF_GROUP_STAGE_START,
        CAF_GROUP_STAGE_END,
        CAF_KNOCKOUT_START,
        CAF_FINAL,
        TRANSFER_WINDOW_CLOSE,
        SEASON_END
    }

    data class Milestone(
        val week: Int,
        val event: SeasonEvent,
        val description: String
    )

    /**
     * Get season phase based on week number
     */
    fun getSeasonPhase(week: Int): SeasonPhase {
        val w = week % 52
        return when (w) {
            in 1..8 -> SeasonPhase.PRESEASON
            in 9..20 -> SeasonPhase.EARLY_SEASON
            in 21..35 -> SeasonPhase.MID_SEASON
            in 36..50 -> SeasonPhase.LATE_SEASON
            else -> SeasonPhase.OFF_SEASON
        }
    }

    /**
     * Get major events for a given week (based on African football calendar)
     * Special handling for cups with variable round counts (e.g., 64‑team cup)
     */
    fun getMilestonesForWeek(week: Int): List<SeasonEvent> {
        val w = week % 52
        return when (w) {
            1 -> listOf(SeasonEvent.TRANSFER_WINDOW_OPEN)
            5 -> listOf(SeasonEvent.PRESEASON_START)
            10 -> listOf(SeasonEvent.COMMUNITY_SHIELD)
            11 -> listOf(SeasonEvent.LEAGUE_START)

            // CRDB Federation Cup (64 teams) – round of 64 in week 15
            15 -> listOf(SeasonEvent.CUP_ROUND_64)

            // Round of 32 and CAF Group Stage start in week 18
            18 -> listOf(SeasonEvent.CUP_ROUND_32, SeasonEvent.CAF_GROUP_STAGE_START)

            // Round of 16 in week 21
            21 -> listOf(SeasonEvent.CUP_ROUND_16)

            // Quarterfinals in week 24
            24 -> listOf(SeasonEvent.CUP_QUARTER_FINAL)

            // Semifinals in week 27
            27 -> listOf(SeasonEvent.CUP_SEMI_FINAL)

            // Final in week 30
            30 -> listOf(SeasonEvent.CUP_FINAL)

            // CAF competitions
            32 -> listOf(SeasonEvent.CAF_GROUP_STAGE_END)
            40 -> listOf(SeasonEvent.CAF_KNOCKOUT_START, SeasonEvent.CUP_QUARTER_FINAL)
            44 -> listOf(SeasonEvent.CUP_SEMI_FINAL)
            48 -> listOf(SeasonEvent.CAF_FINAL)
            50 -> listOf(SeasonEvent.CUP_FINAL)

            // Transfer window & season end
            35 -> listOf(SeasonEvent.TRANSFER_WINDOW_CLOSE)
            0 -> listOf(SeasonEvent.SEASON_END)

            else -> emptyList()
        }
    }

    /**
     * Check if transfer window is open for a given week
     */
    fun isTransferWindowOpen(week: Int): Boolean {
        val w = week % 52
        // Summer: weeks 1–12 (June–August), Winter: weeks 31–35 (January)
        return w in 1..12 || w in 31..35
    }

    /**
     * Get the week number for a given event (useful for scheduling)
     */
    fun getWeekForEvent(event: SeasonEvent): Int? {
        return when (event) {
            SeasonEvent.TRANSFER_WINDOW_OPEN -> 1
            SeasonEvent.PRESEASON_START -> 5
            SeasonEvent.COMMUNITY_SHIELD -> 10
            SeasonEvent.LEAGUE_START -> 11
            SeasonEvent.CUP_ROUND_64 -> 15
            SeasonEvent.CUP_ROUND_32 -> 18
            SeasonEvent.CUP_ROUND_16 -> 21
            SeasonEvent.CUP_QUARTER_FINAL -> 24
            SeasonEvent.CUP_SEMI_FINAL -> 27
            SeasonEvent.CUP_FINAL -> 30
            SeasonEvent.CAF_GROUP_STAGE_START -> 18
            SeasonEvent.CAF_GROUP_STAGE_END -> 32
            SeasonEvent.CAF_KNOCKOUT_START -> 40
            SeasonEvent.CAF_FINAL -> 48
            SeasonEvent.TRANSFER_WINDOW_CLOSE -> 35
            SeasonEvent.SEASON_END -> 52
        }
    }
}
