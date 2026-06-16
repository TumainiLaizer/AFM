package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cup_group_standings",
    foreignKeys = [
        ForeignKey(
            entity = CupsEntity::class,
            parentColumns = ["id"],
            childColumns = ["cup_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cup_id", "group_name", "season_year", "position"], unique = true),
        Index(value = ["team_id", "cup_id", "season_year"], unique = true),
        Index(value = ["cup_id", "group_name", "season_year"]),
        Index(value = ["team_id"]),
        Index(value = ["points"], orders = [Index.Order.DESC]),
        Index(value = ["goal_difference"], orders = [Index.Order.DESC])
    ]
)
data class CupGroupStandingsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "cup_id")
    val cupId: Int,

    @ColumnInfo(name = "cup_name")
    val cupName: String,

    @ColumnInfo(name = "group_name", defaultValue = "")
    val groupName: String = "",

    @ColumnInfo(name = "season_year")
    val seasonYear: Int,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "matches_played", defaultValue = "0")
    val matchesPlayed: Int = 0,

    @ColumnInfo(name = "wins", defaultValue = "0")
    val wins: Int = 0,

    @ColumnInfo(name = "draws", defaultValue = "0")
    val draws: Int = 0,

    @ColumnInfo(name = "losses", defaultValue = "0")
    val losses: Int = 0,

    @ColumnInfo(name = "goals_scored", defaultValue = "0")
    val goalsScored: Int = 0,

    @ColumnInfo(name = "goals_conceded", defaultValue = "0")
    val goalsConceded: Int = 0,

    @ColumnInfo(name = "goal_difference", defaultValue = "0")
    val goalDifference: Int = 0,

    @ColumnInfo(name = "points", defaultValue = "0")
    val points: Int = 0,

    @ColumnInfo(name = "form")
    val form: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val winPercentage: Double
        get() = if (matchesPlayed > 0) (wins.toDouble() / matchesPlayed * 100) else 0.0

    val pointsPerGame: Double
        get() = if (matchesPlayed > 0) points.toDouble() / matchesPlayed else 0.0

    val averageGoalsScored: Double
        get() = if (matchesPlayed > 0) goalsScored.toDouble() / matchesPlayed else 0.0

    val averageGoalsConceded: Double
        get() = if (matchesPlayed > 0) goalsConceded.toDouble() / matchesPlayed else 0.0

    val formArray: List<String>
        get() = form?.split("")?.filter { it.isNotBlank() } ?: emptyList()

    val recentForm: String
        get() = form ?: "-----"

    val isQualificationZone: Boolean
        get() = position <= 2  // Top 2 qualify for knockout stage

    val isGroupWinner: Boolean
        get() = position == 1

    val isRunnerUp: Boolean
        get() = position == 2

    val isEliminated: Boolean
        get() = position > 2

    // ============ BUSINESS METHODS ============

    fun updateFromMatchResult(
        goalsFor: Int,
        goalsAgainst: Int,
        isWin: Boolean,
        isDraw: Boolean,
        isLoss: Boolean
    ): CupGroupStandingsEntity {
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

    fun updatePosition(newPosition: Int): CupGroupStandingsEntity {
        return this.copy(position = newPosition)
    }

    fun resetForNewSeason(): CupGroupStandingsEntity {
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
}
