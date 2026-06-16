package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "knockout_matches",
    foreignKeys = [
        ForeignKey(
            entity = CupsEntity::class,
            parentColumns = ["id"],
            childColumns = ["cup_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CupBracketsEntity::class,
            parentColumns = ["id"],
            childColumns = ["bracket_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FixturesEntity::class,
            parentColumns = ["id"],
            childColumns = ["fixture_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RefereesEntity::class,
            parentColumns = ["referee_id"],
            childColumns = ["referee_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cup_id"]),
        Index(value = ["season"]),
        Index(value = ["round"]),
        Index(value = ["round_number"]),
        Index(value = ["home_team_id"]),
        Index(value = ["away_team_id"]),
        Index(value = ["match_date"]),
        Index(value = ["bracket_id"]),
        Index(value = ["fixture_id"]),
        Index(value = ["referee_id"]),
        Index(value = ["is_two_legged"]),
        Index(value = ["leg"]),
        Index(value = ["cup_id", "season", "round_number"]),
        Index(value = ["cup_id", "season", "match_date"])
    ]
)
data class KnockoutMatchesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "cup_id")
    val cupId: Int,

    @ColumnInfo(name = "cup_name")
    val cupName: String,

    @ColumnInfo(name = "season")
    val season: String,

    @ColumnInfo(name = "round")
    val round: String,  // "First Round", "Second Round", "Quarter-final", "Semi-final", "Final"

    @ColumnInfo(name = "round_number")
    val roundNumber: Int,  // 1, 2, 3, 4, etc.

    @ColumnInfo(name = "match_number")
    val matchNumber: Int,  // Match number within the round (1, 2, 3, etc.)

    @ColumnInfo(name = "home_team_id")
    val homeTeamId: Int,

    @ColumnInfo(name = "home_team")
    val homeTeam: String,

    @ColumnInfo(name = "away_team_id")
    val awayTeamId: Int,

    @ColumnInfo(name = "away_team")
    val awayTeam: String,

    @ColumnInfo(name = "home_score", defaultValue = "0")
    val homeScore: Int = 0,

    @ColumnInfo(name = "away_score", defaultValue = "0")
    val awayScore: Int = 0,

    @ColumnInfo(name = "home_penalty_score")
    val homePenaltyScore: Int? = null,

    @ColumnInfo(name = "away_penalty_score")
    val awayPenaltyScore: Int? = null,

    @ColumnInfo(name = "aggregate_home_score")
    val aggregateHomeScore: Int? = null,  // For two-legged ties

    @ColumnInfo(name = "aggregate_away_score")
    val aggregateAwayScore: Int? = null,  // For two-legged ties

    @ColumnInfo(name = "is_two_legged")
    val isTwoLegged: Boolean = false,

    @ColumnInfo(name = "leg")
    val leg: String? = null,  // "FIRST", "SECOND", or null for single leg

    @ColumnInfo(name = "first_leg_id")
    val firstLegId: Int? = null,  // ID of first leg match (for two-legged ties)

    @ColumnInfo(name = "second_leg_id")
    val secondLegId: Int? = null,  // ID of second leg match (for two-legged ties)

    @ColumnInfo(name = "match_date")
    val matchDate: String,

    @ColumnInfo(name = "match_result")
    val matchResult: String,  // "HOME_WIN", "AWAY_WIN", "DRAW", "HOME_WIN_PENS", "AWAY_WIN_PENS", "AGGREGATE_HOME", "AGGREGATE_AWAY"

    @ColumnInfo(name = "winner_id")
    val winnerId: Int? = null,

    @ColumnInfo(name = "winner")
    val winner: String? = null,

    @ColumnInfo(name = "loser_id")
    val loserId: Int? = null,

    @ColumnInfo(name = "loser")
    val loser: String? = null,

    @ColumnInfo(name = "next_match_id")
    val nextMatchId: Int? = null,  // ID of match in next round

    @ColumnInfo(name = "bracket_id")
    val bracketId: Int? = null,  // Reference to cup_brackets

    @ColumnInfo(name = "fixture_id")
    val fixtureId: Int? = null,  // Reference to fixtures table

    @ColumnInfo(name = "stadium")
    val stadium: String?,

    @ColumnInfo(name = "referee_id")
    val refereeId: Int? = null,

    @ColumnInfo(name = "referee_name")
    val refereeName: String? = null,

    @ColumnInfo(name = "attendance", defaultValue = "5000")
    val attendance: Int = 5000,

    @ColumnInfo(name = "weather_conditions", defaultValue = "Clear")
    val weatherConditions: String = "Clear",

    @ColumnInfo(name = "tv_channel")
    val tvChannel: String? = null,

    @ColumnInfo(name = "is_played")
    val isPlayed: Boolean = false,

    @ColumnInfo(name = "highlights_url")
    val highlightsUrl: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val scoreline: String
        get() = "$homeScore - $awayScore"

    val penaltyScoreline: String?
        get() = if (homePenaltyScore != null && awayPenaltyScore != null) {
            "${homePenaltyScore} - ${awayPenaltyScore}"
        } else null

    val aggregateScoreline: String?
        get() = if (aggregateHomeScore != null && aggregateAwayScore != null) {
            "$aggregateHomeScore - $aggregateAwayScore"
        } else null

    val fullResult: String
        get() = buildString {
            append(scoreline)
            if (penaltyScoreline != null) {
                append(" (${penaltyScoreline} pens)")
            }
            if (aggregateScoreline != null && isTwoLegged && leg == "SECOND") {
                append(" (agg. ${aggregateScoreline})")
            }
        }

    val isHomeWin: Boolean
        get() = matchResult == "HOME_WIN" || matchResult == "HOME_WIN_PENS" || matchResult == "AGGREGATE_HOME"

    val isAwayWin: Boolean
        get() = matchResult == "AWAY_WIN" || matchResult == "AWAY_WIN_PENS" || matchResult == "AGGREGATE_AWAY"

    val isDraw: Boolean
        get() = matchResult == "DRAW"

    val isPenaltyShootout: Boolean
        get() = matchResult == "HOME_WIN_PENS" || matchResult == "AWAY_WIN_PENS"

    val matchDisplay: String
        get() = "$homeTeam vs $awayTeam - $round"

    val winnerDisplay: String
        get() = winner ?: "TBD"

    val matchSummary: String
        get() = if (isPlayed) {
            "$homeTeam $homeScore - $awayScore $awayTeam"
        } else {
            "$homeTeam vs $awayTeam on $matchDate"
        }
}

// ============ ENUMS ============

enum class KnockoutRound(val value: String, val order: Int) {
    PRELIMINARY("Preliminary Round", 0),
    FIRST("First Round", 1),
    SECOND("Second Round", 2),
    THIRD("Third Round", 3),
    FOURTH("Fourth Round", 4),
    ROUND_64("Round of 64", 5),
    ROUND_32("Round of 32", 6),
    ROUND_16("Round of 16", 7),
    QUARTER_FINAL("Quarter-final", 8),
    SEMI_FINAL("Semi-final", 9),
    FINAL("Final", 10)
}

enum class MatchResultType(val value: String) {
    HOME_WIN("HOME_WIN"),
    AWAY_WIN("AWAY_WIN"),
    DRAW("DRAW"),
    HOME_WIN_PENS("HOME_WIN_PENS"),
    AWAY_WIN_PENS("AWAY_WIN_PENS"),
    AGGREGATE_HOME("AGGREGATE_HOME"),
    AGGREGATE_AWAY("AGGREGATE_AWAY")
}

enum class MatchLeg(val value: String) {
    FIRST("FIRST"),
    SECOND("SECOND")
}
