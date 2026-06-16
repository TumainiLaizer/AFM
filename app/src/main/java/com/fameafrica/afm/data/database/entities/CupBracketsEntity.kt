package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cup_brackets",
    foreignKeys = [
        ForeignKey(
            entity = CupsEntity::class,
            parentColumns = ["id"],
            childColumns = ["cupId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FixturesEntity::class,
            parentColumns = ["id"],
            childColumns = ["fixtureId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FixturesEntity::class,
            parentColumns = ["id"],
            childColumns = ["first_leg_fixtureId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FixturesEntity::class,
            parentColumns = ["id"],
            childColumns = ["second_leg_fixtureId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cupId"]),
        Index(value = ["season"]),
        Index(value = ["round"]),
        Index(value = ["teamId"]),
        Index(value = ["opponentId"]),
        Index(value = ["fixtureId"]),
        Index(value = ["cupId", "season", "round"]),
        Index(value = ["bracket_position"]),
        Index(value = ["parent_bracket_id"]),
        Index(value = ["is_walkover"]),
        Index(value = ["first_leg_fixtureId"]),
        Index(value = ["second_leg_fixtureId"])
    ]
)
data class CupBracketsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "cupId")
    val cupId: Int,

    @ColumnInfo(name = "cupName")
    val cupName: String? = null,

    @ColumnInfo(name = "season")
    val season: Int = 0,

    @ColumnInfo(name = "round")
    val round: String? = null,

    @ColumnInfo(name = "round_number")
    val roundNumber: Int = 0,

    @ColumnInfo(name = "bracket_position")
    val bracketPosition: Int = 0,

    @ColumnInfo(name = "teamId")
    val teamId: Int? = null,

    @ColumnInfo(name = "teamName")
    val teamName: String? = null,

    @ColumnInfo(name = "opponentId")
    val opponentId: Int? = null,

    @ColumnInfo(name = "opponentName")
    val opponentName: String? = null,

    @ColumnInfo(name = "result")
    val result: String? = null,

    @ColumnInfo(name = "home_score")
    val homeScore: Int? = null,

    @ColumnInfo(name = "away_score")
    val awayScore: Int? = null,

    @ColumnInfo(name = "penalty_score")
    val penaltyScore: String? = null,

    @ColumnInfo(name = "aggregate_score")
    val aggregateScore: String? = null,

    @ColumnInfo(name = "fixtureId")
    val fixtureId: Int? = null,

    @ColumnInfo(name = "first_leg_fixtureId")
    val firstLegFixtureId: Int? = null,

    @ColumnInfo(name = "second_leg_fixtureId")
    val secondLegFixtureId: Int? = null,

    @ColumnInfo(name = "is_two_legged")
    val isTwoLegged: Boolean = false,

    @ColumnInfo(name = "winnerId")
    val winnerId: Int? = null,

    @ColumnInfo(name = "winner")
    val winner: String? = null,

    @ColumnInfo(name = "loser_id")
    val loserId: Int? = null,

    @ColumnInfo(name = "loser")
    val loser: String? = null,

    @ColumnInfo(name = "next_bracket_id")
    val nextBracketId: Int? = null,

    @ColumnInfo(name = "parent_bracket_id")
    val parent_bracket_id: Int? = null,

    @ColumnInfo(name = "is_walkover")
    val isWalkover: Boolean = false,

    @ColumnInfo(name = "walkover_reason")
    val walkoverReason: String? = null,

    @ColumnInfo(name = "match_date")
    val matchDate: String? = null,

    @ColumnInfo(name = "stadium")
    val stadium: String? = null,

    @ColumnInfo(name = "attendance")
    val attendance: Int? = null,

    @ColumnInfo(name = "legacyTag")
    val legacyTag: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val isCompleted: Boolean
        get() = result != null && (result == "WIN" || result == "LOSS" || result?.contains("-") == true)

    val isScheduled: Boolean
        get() = !isCompleted && teamName != null && opponentName != null

    val isBye: Boolean
        get() = opponentName == null || opponentName == "BYE"

    val displayResult: String
        get() = when {
            isWalkover -> "WO"
            penaltyScore != null -> "$result (${penaltyScore} pens)"
            aggregateScore != null -> "$aggregateScore agg"
            else -> result ?: "TBD"
        }

    val roundDisplay: String
        get() = when (round) {
            "PRELIMINARY" -> "Preliminary Round"
            "FIRST" -> "First Round"
            "SECOND" -> "Second Round"
            "THIRD" -> "Third Round"
            "FOURTH" -> "Fourth Round"
            "GROUP" -> "Group Stage"
            "ROUND_64" -> "Round of 64"
            "ROUND_32" -> "Round of 32"
            "ROUND_16" -> "Round of 16"
            "QUARTER" -> "Quarter-Finals"
            "QUARTER_FINAL" -> "Quarter-Finals"
            "SEMI" -> "Semi-Finals"
            "SEMI_FINAL" -> "Semi-Finals"
            "FINAL" -> "Final"
            else -> round ?: "Unknown"
        }

    val bracketPath: String
        get() = "$cupName - $roundDisplay #$bracketPosition"

    val matchSummary: String
        get() = when {
            isBye -> "$teamName receives a bye"
            isCompleted && winner != null -> "$winner defeated $loser"
            isScheduled -> "$teamName vs $opponentName"
            else -> "Match TBD"
        }
}

// ============ ENUMS ============

enum class CupRound(val value: String, val order: Int) {
    PRELIMINARY("PRELIMINARY", 0),
    FIRST("FIRST", 1),
    SECOND("SECOND", 2),
    THIRD("THIRD", 3),
    FOURTH("FOURTH", 4),
    GROUP("GROUP", 5),
    ROUND_64("ROUND_64", 6),
    ROUND_32("ROUND_32", 7),
    ROUND_16("ROUND_16", 8),
    QUARTER_FINAL("QUARTER_FINAL", 9),
    SEMI_FINAL("SEMI_FINAL", 10),
    FINAL("FINAL", 11)
}

enum class MatchResult(val value: String) {
    WIN("WIN"),
    LOSS("LOSS"),
    DRAW("DRAW"),
    ADVANCE("ADVANCE"),
    ELIMINATED("ELIMINATED")
}
