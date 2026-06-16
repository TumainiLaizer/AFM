package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "community_shield",
    foreignKeys = [
        ForeignKey(
            entity = LeaguesEntity::class,
            parentColumns = ["id"],
            childColumns = ["league_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["league_winner_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["league_runner_up_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["league_third_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["league_fourth_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["league_id"]),
        Index(value = ["season"]),
        Index(value = ["league_winner_id"]),
        Index(value = ["league_runner_up_id"]),
        Index(value = ["league_third_id"]),
        Index(value = ["league_fourth_id"]),
        Index(value = ["match_date"]),
        Index(value = ["is_played"]),
        Index(value = ["league_id", "season"], unique = true)
    ]
)
data class CommunityShieldEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "league_id")
    @ColumnInfo(name = "league_id")
    val leagueId: Int,

    @Json(name = "league_name")
    @ColumnInfo(name = "league_name")
    val leagueName: String,

    @ColumnInfo(name = "season")
    val season: String,  // e.g., "2025/26"

    @Json(name = "match_date")
    @ColumnInfo(name = "match_date")
    val matchDate: String,

    @Json(name = "league_winner_id")
    @ColumnInfo(name = "league_winner_id")
    val leagueWinnerId: Int?,

    @Json(name = "league_winner")
    @ColumnInfo(name = "league_winner")
    val leagueWinner: String?,

    @Json(name = "league_runner_up_id")
    @ColumnInfo(name = "league_runner_up_id")
    val leagueRunnerUpId: Int?,

    @Json(name = "league_runner_up")
    @ColumnInfo(name = "league_runner_up")
    val leagueRunnerUp: String?,

    @Json(name = "league_third_id")
    @ColumnInfo(name = "league_third_id")
    val leagueThirdId: Int?,

    @Json(name = "league_third")
    @ColumnInfo(name = "league_third")
    val leagueThird: String?,

    @Json(name = "league_fourth_id")
    @ColumnInfo(name = "league_fourth_id")
    val leagueFourthId: Int?,

    @Json(name = "league_fourth")
    @ColumnInfo(name = "league_fourth")
    val leagueFourth: String?,

    @Json(name = "participants_format")
    @ColumnInfo(name = "participants_format")
    val participantsFormat: String,  // "CHAMPION_VS_RUNNER_UP", "TOP_FOUR", "CHAMPION_VS_CUP_WINNER"

    @Json(name = "fixture_id")
    @ColumnInfo(name = "fixture_id")
    val fixtureId: Int? = null,

    @Json(name = "home_team")
    @ColumnInfo(name = "home_team")
    val homeTeam: String? = null,

    @Json(name = "away_team")
    @ColumnInfo(name = "away_team")
    val awayTeam: String? = null,

    @Json(name = "home_score")
    @ColumnInfo(name = "home_score")
    val homeScore: Int? = null,

    @Json(name = "away_score")
    @ColumnInfo(name = "away_score")
    val awayScore: Int? = null,

    @ColumnInfo(name = "winner")
    val winner: String? = null,

    @ColumnInfo(name = "result")
    val result: String? = null,

    @Json(name = "is_played")
    @ColumnInfo(name = "is_played")
    val isPlayed: Boolean = false,

    @Json(name = "prize_money")
    @ColumnInfo(name = "prize_money", defaultValue = "10000")
    val prizeMoney: Int = 10000,

    @ColumnInfo(name = "stadium")
    val stadium: String? = null,

    @ColumnInfo(name = "attendance")
    val attendance: Int? = null,

    @ColumnInfo(name = "logo")
    val logo: String? = null,

    @Json(name = "tv_channel")
    @ColumnInfo(name = "tv_channel")
    val tvChannel: String? = "Azam Sports TV",

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val displayName: String
        get() = when (participantsFormat) {
            "CHAMPION_VS_RUNNER_UP" -> "${leagueName} Community Shield"
            "TOP_FOUR" -> "${leagueName} Super Cup"
            "CHAMPION_VS_CUP_WINNER" -> "${leagueName} Charity Shield"
            else -> "${leagueName} Shield"
        }

    val matchDisplay: String
        get() = if (isPlayed && winner != null) {
            "$winner won the ${displayName}"
        } else if (homeTeam != null && awayTeam != null) {
            "$homeTeam vs $awayTeam"
        } else {
            "TBD"
        }

    val scoreline: String
        get() = if (homeScore != null && awayScore != null) {
            "$homeScore - $awayScore"
        } else {
            "Not Played"
        }

    val isChampionVsRunnerUp: Boolean
        get() = participantsFormat == "CHAMPION_VS_RUNNER_UP"

    val isTopFour: Boolean
        get() = participantsFormat == "TOP_FOUR"

    val isChampionVsCupWinner: Boolean
        get() = participantsFormat == "CHAMPION_VS_CUP_WINNER"
}

// ============ ENUMS ============

enum class ShieldFormat(val value: String) {
    CHAMPION_VS_RUNNER_UP("CHAMPION_VS_RUNNER_UP"),
    TOP_FOUR("TOP_FOUR"),
    CHAMPION_VS_CUP_WINNER("CHAMPION_VS_CUP_WINNER")
}

enum class ShieldStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}
