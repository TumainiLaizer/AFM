package com.fameafrica.afm.data.database.entities

import androidx.room.*
import com.squareup.moshi.Json

@Entity(
    tableName = "league_standings",
    indices = [
        Index(value = ["league_name", "season_year", "position"], unique = true),
        Index(value = ["team_id", "league_name", "season_year"], unique = true),
        Index(value = ["league_name", "season_year"]),
        Index(value = ["team_id"]),
        Index(value = ["points"], orders = [Index.Order.DESC]),
        Index(value = ["goal_difference"], orders = [Index.Order.DESC])
    ]
)
data class LeagueStandingsEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "league_name")
    @field:ColumnInfo(name = "league_name")
    val leagueName: String,

    @param:Json(name = "season_year")
    @field:ColumnInfo(name = "season_year")
    val seasonYear: Int,

    @field:ColumnInfo(name = "position")
    val position: Int,

    @param:Json(name = "team_id")
    @field:ColumnInfo(name = "team_id")
    val teamId: Int,

    @param:Json(name = "team_name")
    @field:ColumnInfo(name = "team_name")
    val teamName: String,

    @param:Json(name = "matches_played")
    @field:ColumnInfo(name = "matches_played", defaultValue = "0")
    val matchesPlayed: Int = 0,

    @field:ColumnInfo(name = "wins", defaultValue = "0")
    val wins: Int = 0,

    @field:ColumnInfo(name = "draws", defaultValue = "0")
    val draws: Int = 0,

    @field:ColumnInfo(name = "losses", defaultValue = "0")
    val losses: Int = 0,

    @param:Json(name = "goals_scored")
    @field:ColumnInfo(name = "goals_scored", defaultValue = "0")
    val goalsScored: Int = 0,

    @param:Json(name = "goals_conceded")
    @field:ColumnInfo(name = "goals_conceded", defaultValue = "0")
    val goalsConceded: Int = 0,

    @param:Json(name = "goal_difference")
    @field:ColumnInfo(name = "goal_difference", defaultValue = "0")
    val goalDifference: Int = 0,

    @field:ColumnInfo(name = "points", defaultValue = "0")
    val points: Int = 0,

    @field:ColumnInfo(name = "form")
    val form: String? = null  // e.g., "WWDLW"
) {

    // ============ COMPUTED PROPERTIES ============

    @get:Ignore
    val winPercentage: Double
        get() = if (matchesPlayed > 0) (wins.toDouble() / matchesPlayed * 100) else 0.0

    @get:Ignore
    val pointsPerGame: Double
        get() = if (matchesPlayed > 0) points.toDouble() / matchesPlayed else 0.0

    @get:Ignore
    val averageGoalsScored: Double
        get() = if (matchesPlayed > 0) goalsScored.toDouble() / matchesPlayed else 0.0

    @get:Ignore
    val averageGoalsConceded: Double
        get() = if (matchesPlayed > 0) goalsConceded.toDouble() / matchesPlayed else 0.0

    @get:Ignore
    val formArray: List<String>
        get() = form?.split("")?.filter { it.isNotBlank() } ?: emptyList()

    @get:Ignore
    val recentForm: String
        get() = form ?: "-----"

    @get:Ignore
    val isPromotionZone: Boolean
        get() = position <= 2  // Top 2 get promoted

    @get:Ignore
    val isPlayoffZone: Boolean
        get() = position in 3..4  // 3rd and 4th enter playoffs

    @get:Ignore
    val isRelegationZone: Boolean
        get() = position >= 14  // Bottom 3 get relegated (assuming 16 teams)

    @get:Ignore
    val isChampion: Boolean
        get() = position == 1

    @get:Ignore
    val championsLeagueQualification: Boolean
        get() = position == 1 || position == 2


    @get:Ignore
    val confederationCupQualification: Boolean
        get() = position == 3

    // ============ BUSINESS METHODS ============

    fun updateFromMatchResult(
        goalsFor: Int,
        goalsAgainst: Int,
        isWin: Boolean,
        isDraw: Boolean,
        isLoss: Boolean
    ): LeagueStandingsEntity {
        val newMatchesPlayed = matchesPlayed + 1
        val newWins = wins + (if (isWin) 1 else 0)
        val newDraws = draws + (if (isDraw) 1 else 0)
        val newLosses = losses + (if (isLoss) 1 else 0)
        val newGoalsScored = goalsScored + goalsFor
        val newGoalsConceded = goalsConceded + goalsAgainst
        val newGoalDifference = newGoalsScored - newGoalsConceded
        val newPoints = points + when {
            isWin -> 3
            isDraw -> 1
            else -> 0
        }

        // Update form string (append result, keep last 5)
        val newForm = (form ?: "") + when {
            isWin -> "W"
            isDraw -> "D"
            else -> "L"
        }
        val trimmedForm = newForm.takeLast(5)

        return this.copy(
            matchesPlayed = newMatchesPlayed,
            wins = newWins,
            draws = newDraws,
            losses = newLosses,
            goalsScored = newGoalsScored,
            goalsConceded = newGoalsConceded,
            goalDifference = newGoalDifference,
            points = newPoints,
            form = trimmedForm
        )
    }

    fun resetForNewSeason(): LeagueStandingsEntity {
        return this.copy(
            matchesPlayed = 0,
            wins = 0,
            draws = 0,
            losses = 0,
            goalsScored = 0,
            goalsConceded = 0,
            goalDifference = 0,
            points = 0,
            form = null
        )
    }

    fun updatePosition(newPosition: Int): LeagueStandingsEntity {
        return this.copy(position = newPosition)
    }

    fun getQualificationStatus(): QualificationStatus {
        return when (position) {
            1 -> QualificationStatus.CHAMPIONS_LEAGUE
            2 -> QualificationStatus.CHAMPIONS_LEAGUE
            3 -> QualificationStatus.CONFEDERATION_LEAGUE
            in 14..16 -> QualificationStatus.RELEGATION
            else -> QualificationStatus.NONE
        }
    }
}

enum class QualificationStatus {
    CHAMPIONS_LEAGUE,
    CONFEDERATION_LEAGUE,
    CONFERENCE_LEAGUE,
    RELEGATION,
    NONE,
    PROMOTION,
    PLAYOFFS
}
