package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "match_commentary",
    foreignKeys = [
        ForeignKey(
            entity = FixturesEntity::class,
            parentColumns = ["id"],
            childColumns = ["match_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["assist_player_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"],
            childColumns = ["manager_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RefereesEntity::class,
            parentColumns = ["referee_id"],
            childColumns = ["referee_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MatchEventsEntity::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["match_id", "minute"]),
        Index(value = ["match_id"]),
        Index(value = ["minute"]),
        Index(value = ["event_id"]),
        Index(value = ["commentary_type"]),
        Index(value = ["importance"]),
        Index(value = ["player_id"]),
        Index(value = ["assist_player_id"]),
        Index(value = ["team_id"]),
        Index(value = ["period"]),
        Index(value = ["is_controversial"]),
        Index(value = ["crowd_noise_level"]),
        Index(value = ["manager_id"]),
        Index(value = ["referee_id"])
    ]
)
data class MatchCommentaryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "match_id")
    @ColumnInfo(name = "match_id")
    val matchId: Int,

    @Json(name = "event_id")
    @ColumnInfo(name = "event_id")
    val eventId: Int? = null,

    @ColumnInfo(name = "minute")
    val minute: Int,

    @Json(name = "stoppage_time")
    @ColumnInfo(name = "stoppage_time")
    val stoppageTime: Int? = null,

    @ColumnInfo(name = "period")
    val period: String = "REGULAR",

    @Json(name = "commentary_text")
    @ColumnInfo(name = "commentary_text")
    val commentaryText: String,

    @Json(name = "commentary_type")
    @ColumnInfo(name = "commentary_type")
    val commentaryType: String,

    @ColumnInfo(name = "importance")
    val importance: Int = 1,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int? = null,

    @Json(name = "player_name")
    @ColumnInfo(name = "player_name")
    val playerName: String? = null,

    @Json(name = "assist_player_id")
    @ColumnInfo(name = "assist_player_id")
    val assistPlayerId: Int? = null,

    @Json(name = "assist_player_name")
    @ColumnInfo(name = "assist_player_name")
    val assistPlayerName: String? = null,

    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int? = null,

    @Json(name = "team_name")
    @ColumnInfo(name = "team_name")
    val teamName: String? = null,

    @Json(name = "opponent_team")
    @ColumnInfo(name = "opponent_team")
    val opponentTeam: String? = null,

    @Json(name = "opponent_team_id")
    @ColumnInfo(name = "opponent_team_id")
    val opponentTeamId: Int? = null,

    @Json(name = "manager_id")
    @ColumnInfo(name = "manager_id")
    val managerId: Int? = null,

    @Json(name = "manager_name")
    @ColumnInfo(name = "manager_name")
    val managerName: String? = null,

    @Json(name = "referee_id")
    @ColumnInfo(name = "referee_id")
    val refereeId: Int? = null,

    @Json(name = "referee_name")
    @ColumnInfo(name = "referee_name")
    val refereeName: String? = null,

    @Json(name = "current_score")
    @ColumnInfo(name = "current_score")
    val currentScore: String? = null,

    @Json(name = "home_score")
    @ColumnInfo(name = "home_score")
    val homeScore: Int? = null,

    @Json(name = "away_score")
    @ColumnInfo(name = "away_score")
    val awayScore: Int? = null,

    @Json(name = "is_controversial")
    @ColumnInfo(name = "is_controversial")
    val isControversial: Boolean = false,

    @Json(name = "var_review")
    @ColumnInfo(name = "var_review")
    val varReview: Boolean = false,

    @Json(name = "var_overturned")
    @ColumnInfo(name = "var_overturned")
    val varOverturned: Boolean = false,

    @Json(name = "penalty_saved")
    @ColumnInfo(name = "penalty_saved")
    val penaltySaved: Boolean = false,

    @Json(name = "penalty_post")
    @ColumnInfo(name = "penalty_post")
    val penaltyPost: Boolean = false,

    @Json(name = "own_goal")
    @ColumnInfo(name = "own_goal")
    val ownGoal: Boolean = false,

    @Json(name = "shot_type")
    @ColumnInfo(name = "shot_type")
    val shotType: String? = null,

    @Json(name = "shot_distance")
    @ColumnInfo(name = "shot_distance")
    val shotDistance: Int? = null,

    @Json(name = "expected_goals")
    @ColumnInfo(name = "expected_goals")
    val expectedGoals: Double? = null,

    @Json(name = "substitution_in_player")
    @ColumnInfo(name = "substitution_in_player")
    val substitutionInPlayer: String? = null,

    @Json(name = "substitution_in_player_id")
    @ColumnInfo(name = "substitution_in_player_id")
    val substitutionInPlayerId: Int? = null,

    @Json(name = "substitution_out_player")
    @ColumnInfo(name = "substitution_out_player")
    val substitutionOutPlayer: String? = null,

    @Json(name = "substitution_out_player_id")
    @ColumnInfo(name = "substitution_out_player_id")
    val substitutionOutPlayerId: Int? = null,

    @Json(name = "injury_type")
    @ColumnInfo(name = "injury_type")
    val injuryType: String? = null,

    @Json(name = "injury_minutes")
    @ColumnInfo(name = "injury_minutes")
    val injuryMinutes: Int? = null,

    @Json(name = "fan_reaction")
    @ColumnInfo(name = "fan_reaction")
    val fanReaction: String? = null,

    @Json(name = "crowd_noise_level")
    @ColumnInfo(name = "crowd_noise_level")
    val crowdNoiseLevel: Int = 5,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "description")
    val description: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val displayMinute: String
        get() = if (stoppageTime != null && stoppageTime > 0) {
            "$minute+$stoppageTime"
        } else {
            minute.toString()
        }

    val formattedCommentary: String
        get() = "$displayMinute' [$period] - $commentaryText"

    val importanceLevel: String
        get() = when (importance) {
            5 -> "🔴🔴🔴 CRITICAL"
            4 -> "🔴🔴 HIGH"
            3 -> "🟡 MEDIUM"
            2 -> "⚪ LOW"
            else -> "⚪ MINOR"
        }

    val commentaryIcon: String
        get() = when (commentaryType) {
            "GOAL" -> "⚽"
            "PENALTY" -> "⚽ (P)"
            "FREEKICK" -> "⚽ (FK)"
            "OWN_GOAL" -> "⚽ (OG)"
            "CARD" -> if (commentaryText.contains("red", ignoreCase = true) || commentaryText.contains("RED", ignoreCase = true)) "🟥" else "🟨"
            "SUBSTITUTION" -> "🔄"
            "INJURY" -> "🩹"
            "VAR" -> "📺"
            "CONTROVERSY" -> "🔥"
            "FAN_REACTION" -> "📢"
            "CELEBRATION" -> "🎉"
            "DRAMA" -> "🎭"
            "SHOT" -> "⚡"
            "SAVE" -> "🧤"
            "CORNER" -> "⛳"
            "FOUL" -> "🚫"
            "OFFSIDE" -> "🚩"
            else -> "📝"
        }

    val crowdNoiseDescription: String
        get() = when (crowdNoiseLevel) {
            1 -> "Pin drop silence"
            2 -> "Quiet murmuring"
            3 -> "Normal crowd noise"
            4 -> "Lively atmosphere"
            5 -> "Vocal support"
            6 -> "Loud cheering"
            7 -> "Very loud"
            8 -> "Deafening roar"
            9 -> "Ear-splitting"
            10 -> "Volcanic eruption"
            else -> "Unknown"
        }
}

// ============ ENUMS ============

enum class CommentaryType(val value: String) {
    GOAL("GOAL"),
    PENALTY("PENALTY"),
    FREEKICK("FREEKICK"),
    OWN_GOAL("OWN_GOAL"),
    CARD("CARD"),
    SUBSTITUTION("SUBSTITUTION"),
    INJURY("INJURY"),
    VAR("VAR"),
    CONTROVERSY("CONTROVERSY"),
    FAN_REACTION("FAN_REACTION"),
    CELEBRATION("CELEBRATION"),
    DRAMA("DRAMA"),
    SHOT("SHOT"),
    SAVE("SAVE"),
    CORNER("CORNER"),
    FOUL("FOUL"),
    OFFSIDE("OFFSIDE"),
    TACTICAL("TACTICAL"),
    STATISTIC("STATISTIC"),
    MANAGER("MANAGER"),
    REFEREE("REFEREE")
}

enum class FanReaction(val value: String) {
    CHEERING("CHEERING"),
    WHISTLING("WHISTLING"),
    PROTEST("PROTEST"),
    CHANTING("CHANTING"),
    OLE("OLE"),
    WAVING("WAVING"),
    FLARES("FLARES"),
    SILENCE("SILENCE"),
    APPLAUSE("APPLAUSE"),
    BOOING("BOOING")
}
