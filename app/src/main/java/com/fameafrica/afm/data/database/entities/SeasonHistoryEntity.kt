package com.fameafrica.afm.data.database.entities

import androidx.room.*
import com.squareup.moshi.Json

@Entity(
    tableName = "season_history",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_id", "season"], unique = true),
        Index(value = ["season"]),
        Index(value = ["team_id"]),
        Index(value = ["team_name"]),
        Index(value = ["position"]),
        Index(value = ["trophies_won"])
    ]
)
data class SeasonHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "season")
    val season: String,

    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @Json(name = "team_name")
    @ColumnInfo(name = "team_name")
    val teamName: String,

    @Json(name = "league_name")
    @ColumnInfo(name = "league_name")
    val leagueName: String? = null,

    @ColumnInfo(name = "position")
    val position: Int? = null,

    @ColumnInfo(name = "points")
    val points: Int? = null,

    @ColumnInfo(name = "wins")
    val wins: Int? = null,

    @ColumnInfo(name = "draws")
    val draws: Int? = null,

    @ColumnInfo(name = "losses")
    val losses: Int? = null,

    @Json(name = "goals_for")
    @ColumnInfo(name = "goals_for")
    val goalsFor: Int? = null,

    @Json(name = "goals_against")
    @ColumnInfo(name = "goals_against")
    val goalsAgainst: Int? = null,

    @Json(name = "goal_difference")
    @ColumnInfo(name = "goal_difference")
    val goalDifference: Int? = null,

    @Json(name = "trophies_won")
    @ColumnInfo(name = "trophies_won")
    val trophiesWon: Int = 0,

    @Json(name = "league_titles")
    @ColumnInfo(name = "league_titles")
    val leagueTitles: Int = 0,

    @Json(name = "cup_titles")
    @ColumnInfo(name = "cup_titles")
    val cupTitles: Int = 0,

    @Json(name = "continental_titles")
    @ColumnInfo(name = "continental_titles")
    val continentalTitles: Int = 0,

    @ColumnInfo(name = "promoted")
    val promoted: Boolean = false,

    @ColumnInfo(name = "relegated")
    val relegated: Boolean = false,

    @Json(name = "qualified_for_continental")
    @ColumnInfo(name = "qualified_for_continental")
    val qualifiedForContinental: Boolean = false,

    @Json(name = "average_attendance")
    @ColumnInfo(name = "average_attendance")
    val averageAttendance: Int? = null,

    @Json(name = "top_scorer")
    @ColumnInfo(name = "top_scorer")
    val topScorer: String? = null,

    @Json(name = "top_scorer_goals")
    @ColumnInfo(name = "top_scorer_goals")
    val topScorerGoals: Int? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    @get:Ignore
    val winPercentage: Double
        get() = if ((wins?.plus(draws ?: 0)?.plus(losses ?: 0) ?: 0) > 0) {
            (wins?.toDouble() ?: 0.0) / (wins?.plus(draws ?: 0)?.plus(losses ?: 0) ?: 1) * 100
        } else 0.0

    @get:Ignore
    val isChampion: Boolean
        get() = position == 1

    @get:Ignore
    val isPromoted: Boolean
        get() = promoted

    @get:Ignore
    val isRelegated: Boolean
        get() = relegated

    @get:Ignore
    val seasonDisplay: String
        get() = "$season Season"

    @get:Ignore
    val summary: String
        get() = buildString {
            append("$teamName - $season: ")
            if (position != null) append("${position}th place")
            if (trophiesWon > 0) append(", $trophiesWon trophy(ies)")
        }
}

// ============ ENUMS ============

enum class SeasonOutcome(val value: String) {
    CHAMPION("Champion"),
    PROMOTED("Promoted"),
    RELEGATED("Relegated"),
    QUALIFIED_CONTINENTAL("Qualified for Continental"),
    MID_TABLE("Mid-table"),
    STRUGGLING("Struggling")
}
