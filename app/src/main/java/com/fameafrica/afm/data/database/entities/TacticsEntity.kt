package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(
    tableName = "tactics",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"],
            childColumns = ["manager_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_id"], unique = true),
        Index(value = ["manager_id"]),
        Index(value = ["tactical_archetype"]),
        Index(value = ["formation"]),
        Index(value = ["playstyle"])
    ]
)
data class TacticsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @Json(name = "team_name")
    @ColumnInfo(name = "team_name")
    val teamName: String,

    @Json(name = "manager_id")
    @ColumnInfo(name = "manager_id")
    val managerId: Int? = null,

    // ============ CORE TACTICS ============
    @ColumnInfo(name = "formation")
    val formation: String,  // 4-4-2, 4-3-3, 4-2-3-1, 3-5-2, etc.

    @Json(name = "tactical_archetype")
    @ColumnInfo(name = "tactical_archetype")
    val tacticalArchetype: String,  // POSSESSION, ATTACKING, BALANCED, COUNTER, DEFENSIVE, PRESSING, SPECIALIZED

    @ColumnInfo(name = "playstyle")
    val playstyle: String,  // TIKI_TAKA, GEGENPRESSING, WING_PLAY, DIRECT, etc.

    @Json(name = "defensive_threshold")
    @ColumnInfo(name = "defensive_threshold")
    val defensiveThreshold: Int = 50,  // 0-100

    @Json(name = "attacking_threshold")
    @ColumnInfo(name = "attacking_threshold")
    val attackingThreshold: Int = 50,

    @ColumnInfo(name = "tempo")
    val tempo: Int = 50,

    @ColumnInfo(name = "width")
    val width: Int = 50,

    @ColumnInfo(name = "depth")
    val depth: Int = 50,

    @Json(name = "press_intensity")
    @ColumnInfo(name = "press_intensity")
    val pressIntensity: Int = 50,

    @Json(name = "passing_directness")
    @ColumnInfo(name = "passing_directness")
    val passingDirectness: Int = 50,

    @ColumnInfo(name = "creativity")
    val creativity: Int = 50,

    // ============ TACTICAL IDENTITY (NEW) ============
    @Json(name = "tactical_familiarity")
    @ColumnInfo(name = "tactical_familiarity")
    val tacticalFamiliarity: Int = 50, // 0-100: Fixes "randomness" by rewarding consistency

    // ============ SET PIECE TAKERS & ROLES ============
    @Json(name = "captain_id")
    @ColumnInfo(name = "captain_id")
    val captainId: Int? = null,

    @Json(name = "vice_captain_id")
    @ColumnInfo(name = "vice_captain_id")
    val viceCaptainId: Int? = null,

    @Json(name = "penalty_taker_id")
    @ColumnInfo(name = "penalty_taker_id")
    val penaltyTakerId: Int? = null,

    @Json(name = "free_kick_taker_id")
    @ColumnInfo(name = "free_kick_taker_id")
    val freeKickTakerId: Int? = null,

    @Json(name = "corner_taker_id")
    @ColumnInfo(name = "corner_taker_id")
    val cornerTakerId: Int? = null,

    // ============ MATCHUP PROBABILITIES ============
    @Json(name = "win_probability_vs_possession")
    @ColumnInfo(name = "win_probability_vs_possession")
    val winProbabilityVsPossession: Int = 33,
    @Json(name = "draw_probability_vs_possession")
    @ColumnInfo(name = "draw_probability_vs_possession")
    val drawProbabilityVsPossession: Int = 33,
    @Json(name = "loss_probability_vs_possession")
    @ColumnInfo(name = "loss_probability_vs_possession")
    val lossProbabilityVsPossession: Int = 34,

    @Json(name = "win_probability_vs_attacking")
    @ColumnInfo(name = "win_probability_vs_attacking")
    val winProbabilityVsAttacking: Int = 33,
    @Json(name = "draw_probability_vs_attacking")
    @ColumnInfo(name = "draw_probability_vs_attacking")
    val drawProbabilityVsAttacking: Int = 33,
    @Json(name = "loss_probability_vs_attacking")
    @ColumnInfo(name = "loss_probability_vs_attacking")
    val lossProbabilityVsAttacking: Int = 34,

    @Json(name = "win_probability_vs_balanced")
    @ColumnInfo(name = "win_probability_vs_balanced")
    val winProbabilityVsBalanced: Int = 33,
    @Json(name = "draw_probability_vs_balanced")
    @ColumnInfo(name = "draw_probability_vs_balanced")
    val drawProbabilityVsBalanced: Int = 34,
    @Json(name = "loss_probability_vs_balanced")
    @ColumnInfo(name = "loss_probability_vs_balanced")
    val lossProbabilityVsBalanced: Int = 33,

    @Json(name = "win_probability_vs_counter")
    @ColumnInfo(name = "win_probability_vs_counter")
    val winProbabilityVsCounter: Int = 33,
    @Json(name = "draw_probability_vs_counter")
    @ColumnInfo(name = "draw_probability_vs_counter")
    val drawProbabilityVsCounter: Int = 34,
    @Json(name = "loss_probability_vs_counter")
    @ColumnInfo(name = "loss_probability_vs_counter")
    val lossProbabilityVsCounter: Int = 33,

    @Json(name = "win_probability_vs_defensive")
    @ColumnInfo(name = "win_probability_vs_defensive")
    val winProbabilityVsDefensive: Int = 33,
    @Json(name = "draw_probability_vs_defensive")
    @ColumnInfo(name = "draw_probability_vs_defensive")
    val drawProbabilityVsDefensive: Int = 34,
    @Json(name = "loss_probability_vs_defensive")
    @ColumnInfo(name = "loss_probability_vs_defensive")
    val lossProbabilityVsDefensive: Int = 33,

    @Json(name = "win_probability_vs_pressing")
    @ColumnInfo(name = "win_probability_vs_pressing")
    val winProbabilityVsPressing: Int = 33,
    @Json(name = "draw_probability_vs_pressing")
    @ColumnInfo(name = "draw_probability_vs_pressing")
    val drawProbabilityVsPressing: Int = 33,
    @Json(name = "loss_probability_vs_pressing")
    @ColumnInfo(name = "loss_probability_vs_pressing")
    val lossProbabilityVsPressing: Int = 34,

    // ============ MANAGER INFLUENCE ============
    @Json(name = "manager_tactical_flexibility")
    @ColumnInfo(name = "manager_tactical_flexibility")
    val managerTacticalFlexibility: Int? = null,

    @Json(name = "manager_preferred_style")
    @ColumnInfo(name = "manager_preferred_style")
    val managerPreferredStyle: String? = null,

    @Json(name = "is_default")
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    @Json(name = "autoselect_lineup")
    @ColumnInfo(name = "autoselect_lineup")
    val autoselectLineup: Boolean = false,

    @Json(name = "substitution_strategy")
    @ColumnInfo(name = "substitution_strategy")
    val substitutionStrategy: String = "MANUAL",

    @Json(name = "starting_xi_ids")
    @ColumnInfo(name = "starting_xi_ids")
    val startingXiIds: List<Int>? = null,

    @Json(name = "substitute_ids")
    @ColumnInfo(name = "substitute_ids")
    val substituteIds: List<Int>? = null,

    @Json(name = "last_updated")
    @ColumnInfo(name = "last_updated")
    val lastUpdated: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {
    val isPossessionBased: Boolean get() = tacticalArchetype == "POSSESSION"
    val isAttacking: Boolean get() = tacticalArchetype == "ATTACKING"
    val isBalanced: Boolean get() = tacticalArchetype == "BALANCED"
    val isCounterAttacking: Boolean get() = tacticalArchetype == "COUNTER"
    val isDefensive: Boolean get() = tacticalArchetype == "DEFENSIVE"
    val isPressing: Boolean get() = tacticalArchetype == "PRESSING"
    val isSpecialized: Boolean get() = tacticalArchetype == "SPECIALIZED"

    val summary: String get() = "$tacticalArchetype - $formation ($playstyle)"

    fun getWinProbabilityVs(opponentArchetype: String): Int {
        return when (opponentArchetype) {
            "POSSESSION" -> winProbabilityVsPossession
            "ATTACKING" -> winProbabilityVsAttacking
            "BALANCED" -> winProbabilityVsBalanced
            "COUNTER" -> winProbabilityVsCounter
            "DEFENSIVE" -> winProbabilityVsDefensive
            "PRESSING" -> winProbabilityVsPressing
            else -> 33
        }
    }

    fun getDrawProbabilityVs(opponentArchetype: String): Int {
        return when (opponentArchetype) {
            "POSSESSION" -> drawProbabilityVsPossession
            "ATTACKING" -> drawProbabilityVsAttacking
            "BALANCED" -> drawProbabilityVsBalanced
            "COUNTER" -> drawProbabilityVsCounter
            "DEFENSIVE" -> drawProbabilityVsDefensive
            "PRESSING" -> drawProbabilityVsPressing
            else -> 33
        }
    }

    fun getLossProbabilityVs(opponentArchetype: String): Int {
        return when (opponentArchetype) {
            "POSSESSION" -> lossProbabilityVsPossession
            "ATTACKING" -> lossProbabilityVsAttacking
            "BALANCED" -> lossProbabilityVsBalanced
            "COUNTER" -> lossProbabilityVsCounter
            "DEFENSIVE" -> lossProbabilityVsDefensive
            "PRESSING" -> lossProbabilityVsPressing
            else -> 34
        }
    }

    fun getMatchupProbabilities(opponentArchetype: String): Triple<Int, Int, Int> {
        return Triple(
            getWinProbabilityVs(opponentArchetype),
            getDrawProbabilityVs(opponentArchetype),
            getLossProbabilityVs(opponentArchetype)
        )
    }
}
