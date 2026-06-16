package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json
import kotlin.math.abs

@Entity(
    tableName = "fixtures_results",
    indices = [
        Index(value = ["fixture_id"], unique = true),
        Index(value = ["match_date"]),
        Index(value = ["home_team_id"]),
        Index(value = ["away_team_id"]),
        Index(value = ["referee_id"]),
        Index(value = ["league_name"]),
        Index(value = ["cup_name"]),
        Index(value = ["season"]),
        Index(value = ["match_type"]),
        Index(value = ["attendance"]),
        Index(value = ["is_upset"])
    ]
)
data class FixturesResultsEntity(
    @PrimaryKey(autoGenerate = false)  // Use same ID as fixture
    @param:Json(name = "fixture_id")
    @field:ColumnInfo(name = "fixture_id")
    val fixtureId: Int,

    @param:Json(name = "match_date")
    @field:ColumnInfo(name = "match_date")
    val matchDate: String,

    @param:Json(name = "home_team_id")
    @field:ColumnInfo(name = "home_team_id")
    val homeTeamId: Int,

    @param:Json(name = "home_team")
    @field:ColumnInfo(name = "home_team")
    val homeTeam: String,

    @param:Json(name = "away_team_id")
    @field:ColumnInfo(name = "away_team_id")
    val awayTeamId: Int,

    @param:Json(name = "away_team")
    @field:ColumnInfo(name = "away_team")
    val awayTeam: String,

    @param:Json(name = "home_score")
    @field:ColumnInfo(name = "home_score", defaultValue = "0")
    val homeScore: Int = 0,

    @param:Json(name = "away_score")
    @field:ColumnInfo(name = "away_score", defaultValue = "0")
    val awayScore: Int = 0,

    @param:Json(name = "home_halftime_score")
    @field:ColumnInfo(name = "home_halftime_score", defaultValue = "0")
    val homeHalftimeScore: Int = 0,

    @param:Json(name = "away_halftime_score")
    @field:ColumnInfo(name = "away_halftime_score", defaultValue = "0")
    val awayHalftimeScore: Int = 0,

    @param:Json(name = "home_penalty_score")
    @field:ColumnInfo(name = "home_penalty_score")
    val homePenaltyScore: Int? = null,

    @param:Json(name = "away_penalty_score")
    @field:ColumnInfo(name = "away_penalty_score")
    val awayPenaltyScore: Int? = null,

    @param:Json(name = "possession_home")
    @field:ColumnInfo(name = "possession_home", defaultValue = "50")
    val possessionHome: Int = 50,

    @param:Json(name = "possession_away")
    @field:ColumnInfo(name = "possession_away", defaultValue = "50")
    val possessionAway: Int = 50,

    @param:Json(name = "shots_home")
    @field:ColumnInfo(name = "shots_home", defaultValue = "0")
    val shotsHome: Int = 0,

    @param:Json(name = "shots_away")
    @field:ColumnInfo(name = "shots_away", defaultValue = "0")
    val shotsAway: Int = 0,

    @param:Json(name = "shots_on_target_home")
    @field:ColumnInfo(name = "shots_on_target_home", defaultValue = "0")
    val shotsOnTargetHome: Int = 0,

    @param:Json(name = "shots_on_target_away")
    @field:ColumnInfo(name = "shots_on_target_away", defaultValue = "0")
    val shotsOnTargetAway: Int = 0,

    @param:Json(name = "corners_home")
    @field:ColumnInfo(name = "corners_home", defaultValue = "0")
    val cornersHome: Int = 0,

    @param:Json(name = "corners_away")
    @field:ColumnInfo(name = "corners_away", defaultValue = "0")
    val cornersAway: Int = 0,

    @param:Json(name = "fouls_home")
    @field:ColumnInfo(name = "fouls_home", defaultValue = "0")
    val foulsHome: Int = 0,

    @param:Json(name = "fouls_away")
    @field:ColumnInfo(name = "fouls_away", defaultValue = "0")
    val foulsAway: Int = 0,

    @param:Json(name = "yellow_cards_home")
    @field:ColumnInfo(name = "yellow_cards_home", defaultValue = "0")
    val yellowCardsHome: Int = 0,

    @param:Json(name = "yellow_cards_away")
    @field:ColumnInfo(name = "yellow_cards_away", defaultValue = "0")
    val yellowCardsAway: Int = 0,

    @param:Json(name = "red_cards_home")
    @field:ColumnInfo(name = "red_cards_home", defaultValue = "0")
    val redCardsHome: Int = 0,

    @param:Json(name = "red_cards_away")
    @field:ColumnInfo(name = "red_cards_away", defaultValue = "0")
    val redCardsAway: Int = 0,

    @param:Json(name = "xg_home")
    @field:ColumnInfo(name = "xg_home", defaultValue = "0.0")
    val xgHome: Double = 0.0,

    @param:Json(name = "xg_away")
    @field:ColumnInfo(name = "xg_away", defaultValue = "0.0")
    val xgAway: Double = 0.0,

    @param:Json(name = "key_passes_home")
    @field:ColumnInfo(name = "key_passes_home", defaultValue = "0")
    val keyPassesHome: Int = 0,

    @param:Json(name = "key_passes_away")
    @field:ColumnInfo(name = "key_passes_away", defaultValue = "0")
    val keyPassesAway: Int = 0,

    @param:Json(name = "big_chances_missed_home")
    @field:ColumnInfo(name = "big_chances_missed_home", defaultValue = "0")
    val bigChancesMissedHome: Int = 0,

    @param:Json(name = "big_chances_missed_away")
    @field:ColumnInfo(name = "big_chances_missed_away", defaultValue = "0")
    val bigChancesMissedAway: Int = 0,

    @param:Json(name = "offsides_home")
    @field:ColumnInfo(name = "offsides_home", defaultValue = "0")
    val offsidesHome: Int = 0,

    @param:Json(name = "offsides_away")
    @field:ColumnInfo(name = "offsides_away", defaultValue = "0")
    val offsidesAway: Int = 0,

    @param:Json(name = "referee_id")
    @field:ColumnInfo(name = "referee_id")
    val refereeId: Int? = null,

    @field:ColumnInfo(name = "attendance", defaultValue = "0")
    val attendance: Int = 0,

    @param:Json(name = "weather_conditions")
    @field:ColumnInfo(name = "weather_conditions", defaultValue = "Clear")
    val weatherConditions: String = "Clear",

    @field:ColumnInfo(name = "stadium")
    val stadium: String,

    @param:Json(name = "match_type")
    @field:ColumnInfo(name = "match_type")
    val matchType: String,

    @param:Json(name = "league_name")
    @field:ColumnInfo(name = "league_name")
    val leagueName: String? = null,

    @param:Json(name = "cup_name")
    @field:ColumnInfo(name = "cup_name")
    val cupName: String? = null,

    @param:Json(name = "cup_round")
    @field:ColumnInfo(name = "cup_round")
    val cupRound: String? = null,

    @field:ColumnInfo(name = "season")
    val season: String,

    @param:Json(name = "match_status")
    @field:ColumnInfo(name = "match_status", defaultValue = "COMPLETED")
    val matchStatus: String = "COMPLETED",

    @param:Json(name = "man_of_match")
    @field:ColumnInfo(name = "man_of_match")
    val manOfMatch: String? = null,

    @param:Json(name = "man_of_match_id")
    @field:ColumnInfo(name = "man_of_match_id")
    val manOfMatchId: Int? = null,

    @param:Json(name = "man_of_match_team")
    @field:ColumnInfo(name = "man_of_match_team")
    val manOfMatchTeam: String? = null,

    @param:Json(name = "man_of_match_team_id")
    @field:ColumnInfo(name = "man_of_match_team_id")
    val manOfMatchTeamId: Int? = null,

    @param:Json(name = "man_of_match_rating")
    @field:ColumnInfo(name = "man_of_match_rating")
    val manOfMatchRating: Double? = null,

    @param:Json(name = "home_team_elo")
    @field:ColumnInfo(name = "home_team_elo", defaultValue = "1500")
    val homeTeamElo: Int = 1500,

    @param:Json(name = "away_team_elo")
    @field:ColumnInfo(name = "away_team_elo", defaultValue = "1500")
    val awayTeamElo: Int = 1500,

    @param:Json(name = "elo_change_home")
    @field:ColumnInfo(name = "elo_change_home", defaultValue = "0")
    val eloChangeHome: Int = 0,

    @param:Json(name = "elo_change_away")
    @field:ColumnInfo(name = "elo_change_away", defaultValue = "0")
    val eloChangeAway: Int = 0,

    @param:Json(name = "is_upset")
    @field:ColumnInfo(name = "is_upset", defaultValue = "0")
    val isUpset: Boolean = false,

    @param:Json(name = "upset_factor")
    @field:ColumnInfo(name = "upset_factor")
    val upsetFactor: Double? = null,

    @param:Json(name = "created_at")
    @field:ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String = "",

    @param:Json(name = "updated_at")
    @field:ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String = ""
) {
    val totalGoals: Int get() = homeScore + awayScore
    val totalShots: Int get() = shotsHome + shotsAway
    val totalShotsOnTarget: Int get() = shotsOnTargetHome + shotsOnTargetAway
    val shotAccuracyHome: Double get() = if (shotsHome > 0) (shotsOnTargetHome.toDouble() / shotsHome * 100) else 0.0
    val shotAccuracyAway: Double get() = if (shotsAway > 0) (shotsOnTargetAway.toDouble() / shotsAway * 100) else 0.0
    val totalYellowCards: Int get() = yellowCardsHome + yellowCardsAway
    val totalRedCards: Int get() = redCardsHome + redCardsAway
    val totalFouls: Int get() = foulsHome + foulsAway
    val winner: String? get() = when {
        homeScore > awayScore -> homeTeam
        awayScore > homeScore -> awayTeam
        homePenaltyScore != null && awayPenaltyScore != null -> if (homePenaltyScore > awayPenaltyScore) homeTeam else awayTeam
        else -> null
    }
    val isDraw: Boolean get() = homeScore == awayScore && homePenaltyScore == null
    val isPenaltyShootout: Boolean get() = homePenaltyScore != null && awayPenaltyScore != null
    val result: String get() = when {
        isPenaltyShootout -> {
            val res = if (homePenaltyScore!! > awayPenaltyScore!!) "$homeTeam wins on penalties" else "$awayTeam wins on penalties"
            "$homeScore-$awayScore ($res)"
        }
        homeScore > awayScore -> "$homeTeam Win"
        awayScore > homeScore -> "$awayTeam Win"
        else -> "Draw"
    }
    val scoreline: String get() = "$homeScore - $awayScore"
    val halftimeScoreline: String get() = "$homeHalftimeScore - $awayHalftimeScore"
    val penaltyScoreline: String? get() = if (isPenaltyShootout) "${homePenaltyScore ?: 0} - ${awayPenaltyScore ?: 0}" else null
    val homeTeamWin: Boolean get() = homeScore > awayScore || (isPenaltyShootout && homePenaltyScore!! > awayPenaltyScore!!)
    val awayTeamWin: Boolean get() = awayScore > homeScore || (isPenaltyShootout && awayPenaltyScore!! > homePenaltyScore!!)
    val homeTeamPoints: Int get() = when { homeTeamWin -> 3; isDraw -> 1; else -> 0 }
    val awayTeamPoints: Int get() = when { awayTeamWin -> 3; isDraw -> 1; else -> 0 }
    val goalDifference: Int get() = homeScore - awayScore
    val isHighScoring: Boolean get() = totalGoals >= 4
    val isCleanSheet: Pair<Boolean, Boolean> get() = Pair(homeScore == 0, awayScore == 0)
    val homeCleanSheet: Boolean get() = awayScore == 0
    val awayCleanSheet: Boolean get() = homeScore == 0
    val isComeback: Boolean get() = (homeHalftimeScore < awayHalftimeScore && homeScore > awayScore) || (awayHalftimeScore < homeHalftimeScore && awayScore > homeScore)
    val isThrashing: Boolean get() = abs(homeScore - awayScore) >= 3

    fun calculateUpsetFactor(): Double {
        if (homeTeamElo == awayTeamElo) return 1.0
        val expectedWinProbability = 1.0 / (1.0 + Math.pow(10.0, (awayTeamElo - homeTeamElo) / 400.0))
        return when {
            homeTeamWin -> (1.0 - expectedWinProbability) / expectedWinProbability
            awayTeamWin -> expectedWinProbability / (1.0 - expectedWinProbability)
            else -> 1.0
        }
    }

    fun calculateEloChanges(k: Int = 32): Pair<Int, Int> {
        val expectedHome = 1.0 / (1.0 + Math.pow(10.0, (awayTeamElo - homeTeamElo) / 400.0))
        val expectedAway = 1.0 / (1.0 + Math.pow(10.0, (homeTeamElo - awayTeamElo) / 400.0))
        val actualHome = when { homeTeamWin -> 1.0; isDraw -> 0.5; else -> 0.0 }
        val actualAway = when { awayTeamWin -> 1.0; isDraw -> 0.5; else -> 0.0 }
        val homeChange = (k * (actualHome - expectedHome)).toInt()
        val awayChange = (k * (actualAway - expectedAway)).toInt()
        return Pair(homeChange, awayChange)
    }

    fun withCalculatedElo(): FixturesResultsEntity {
        val (h, a) = calculateEloChanges()
        val u = calculateUpsetFactor()
        return this.copy(eloChangeHome = h, eloChangeAway = a, isUpset = u > 1.5, upsetFactor = u)
    }
}
