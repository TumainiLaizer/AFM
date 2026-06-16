package com.fameafrica.afm.data.database.entities

import androidx.room.*
import com.squareup.moshi.Json

@Entity(
    tableName = "fixtures",
    indices = [
        Index(value = ["match_date"]),
        Index(value = ["home_team_id", "away_team_id", "match_date"], unique = true),
        Index(value = ["referee_id"]),
        Index(value = ["league"]),
        Index(value = ["cup_name"]),
        Index(value = ["match_status"]),
        Index(value = ["season"]),
        Index(value = ["match_type"])
    ]
)
data class FixturesEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "match_date")
    @field:ColumnInfo(name = "match_date")
    val matchDate: String,  // Format: YYYY-MM-DD HH:MM

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

    @param:Json(name = "weather_conditions")
    @field:ColumnInfo(name = "weather_conditions", defaultValue = "Clear")
    val weatherConditions: String = "Clear",

    @field:ColumnInfo(name = "stadium", defaultValue = "FAME Africa Stadium")
    val stadium: String = "FAME Africa Stadium",

    @param:Json(name = "referee_id")
    @field:ColumnInfo(name = "referee_id")  // FK to referees table
    val refereeId: Int? = null,

    @param:Json(name = "tv_channel")
    @field:ColumnInfo(name = "tv_channel", defaultValue = "Azam Sports TV")
    val tvChannel: String = "Azam Sports TV",

    @param:Json(name = "match_type")
    @field:ColumnInfo(name = "match_type", defaultValue = "League")
    val matchType: String = "League",  // League, Cup, Friendly, Preseason Tour, Playoff, International

    @field:ColumnInfo(name = "postseason", defaultValue = "0")
    val postseason: Int = 0,  // 0 = regular season, 1 = playoffs, 2 = finals

    @param:Json(name = "rescheduled_date")
    @field:ColumnInfo(name = "rescheduled_date")
    val rescheduledDate: String? = null,

    @field:ColumnInfo(name = "season", defaultValue = "2025/26")
    val season: String = "2025/26",

    @param:Json(name = "league_id")
    @field:ColumnInfo(name = "league_id")
    val leagueId: Int? = null,

    @field:ColumnInfo(name = "league")  // Deprecated: use leagueId
    val league: String? = null,

    @param:Json(name = "cup_id")
    @field:ColumnInfo(name = "cup_id")
    val cupId: Int? = null,

    @param:Json(name = "cup_name")
    @field:ColumnInfo(name = "cup_name")  // Deprecated: use cupId
    val cupName: String? = null,

    @param:Json(name = "match_status")
    @field:ColumnInfo(name = "match_status")
    val matchStatus: String? = null,  // SCHEDULED, LIVE, COMPLETED, POSTPONED, CANCELLED

    @field:ColumnInfo(name = "position")
    val position: Int = 0,  // Game week / round number

    @field:ColumnInfo(name = "round", defaultValue = "Round 1")
    val round: String = "Round 1",

    @field:ColumnInfo(name = "timeZone", defaultValue = "Africa/Dar es Salaam")
    val timeZone: String = "Africa/Dar es Salaam",

    @field:ColumnInfo(name = "badgeTag", defaultValue = "None")
    val badgeTag: String = "None",

    @field:ColumnInfo(name = "importance", defaultValue = "REGULAR")
    val importance: String = "REGULAR",

    @field:ColumnInfo(name = "stage")
    val stage: String? = null,

    @param:Json(name = "is_tournament")
    @field:ColumnInfo(name = "is_tournament", defaultValue = "0")
    val isTournament: Boolean = false
) {

    // ============ COMPUTED PROPERTIES ============

    @get:Ignore
    val isCompleted: Boolean
        get() = matchStatus == "COMPLETED"

    @get:Ignore
    val isLive: Boolean
        get() = matchStatus == "LIVE"

    @get:Ignore
    val isScheduled: Boolean
        get() = matchStatus == "SCHEDULED"

    @get:Ignore
    val isPostponed: Boolean
        get() = matchStatus == "POSTPONED"

    @get:Ignore
    val isCancelled: Boolean
        get() = matchStatus == "CANCELLED"

    @get:Ignore
    val isLeagueMatch: Boolean
        get() = matchType == "League" && league != null

    @get:Ignore
    val isCupMatch: Boolean
        get() = matchType == "Cup" && cupName != null

    @get:Ignore
    val isInternational: Boolean
        get() = matchType == "International"

    @get:Ignore
    val isFriendly: Boolean
        get() = matchType == "Friendly"

    @get:Ignore
    val isPlayoff: Boolean
        get() = matchType == "Playoff" || postseason > 0

    @get:Ignore
    val isPreseason: Boolean
        get() = matchType == "Preseason Tour"

    @get:Ignore
    val winner: String?
        get() = when {
            !isCompleted -> null
            homeScore > awayScore -> homeTeam
            awayScore > homeScore -> awayTeam
            else -> "Draw"
        }

    @get:Ignore
    val loser: String?
        get() = when {
            !isCompleted -> null
            homeScore > awayScore -> awayTeam
            awayScore > homeScore -> homeTeam
            else -> null
        }

    @get:Ignore
    val result: String
        get() = when {
            !isCompleted -> "Not Played"
            homeScore > awayScore -> "$homeTeam Win"
            awayScore > homeScore -> "$awayTeam Win"
            else -> "Draw"
        }

    @get:Ignore
    val scoreline: String
        get() = "$homeTeam $homeScore - $awayScore $awayTeam"

    // ============ BUSINESS METHODS ============

    fun updateScore(home: Int, away: Int): FixturesEntity {
        return this.copy(
            homeScore = home,
            awayScore = away,
            matchStatus = "COMPLETED"
        )
    }

    fun postpone(newDate: String): FixturesEntity {
        return this.copy(
            matchStatus = "POSTPONED",
            rescheduledDate = newDate
        )
    }

    fun reschedule(newDate: String): FixturesEntity {
        return this.copy(
            matchDate = newDate,
            matchStatus = "SCHEDULED",
            rescheduledDate = null
        )
    }

    fun cancel(): FixturesEntity {
        return this.copy(
            matchStatus = "CANCELLED"
        )
    }

    fun start(): FixturesEntity {
        return this.copy(
            matchStatus = "LIVE"
        )
    }
}
