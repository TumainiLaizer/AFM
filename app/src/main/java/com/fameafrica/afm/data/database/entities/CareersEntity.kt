package com.fameafrica.afm.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "careers",
    indices = [
        Index(value = ["manager_id"], name = "idx_careers_manager_id"),
        Index(value = ["team_id"], name = "idx_careers_team_id"),
        Index(value = ["is_active"], name = "idx_careers_is_active"),
        Index(value = ["created_at"], name = "idx_careers_created_at"),
        Index(value = ["last_updated"], name = "idx_careers_last_updated"),
        Index(value = ["league_name"], name = "idx_careers_league_name"),
        Index(value = ["league_level"], name = "idx_careers_league_level"),
        Index(value = ["difficulty"], name = "idx_careers_difficulty"),
        Index(value = ["season"], name = "idx_careers_season"),
        Index(value = ["manager_id", "is_active"], name = "idx_careers_manager_active"),
        Index(value = ["team_id", "is_active"], name = "idx_careers_team_active")
    ]
)
data class CareersEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "manager_id")
    @field:ColumnInfo(name = "manager_id")
    val managerId: Int,

    @param:Json(name = "manager_name")
    @field:ColumnInfo(name = "manager_name")
    val managerName: String,

    @param:Json(name = "team_id")
    @field:ColumnInfo(name = "team_id")
    val teamId: Int,

    @param:Json(name = "team_name")
    @field:ColumnInfo(name = "team_name")
    val teamName: String,

    @param:Json(name = "league_name")
    @field:ColumnInfo(name = "league_name")
    val leagueName: String,

    @param:Json(name = "league_level")
    @field:ColumnInfo(name = "league_level")
    val leagueLevel: Int,

    @field:ColumnInfo(name = "difficulty")
    val difficulty: String,

    @field:ColumnInfo(name = "season")
    val season: Int,

    @field:ColumnInfo(name = "mode", defaultValue = "MANAGER")
    val mode: String = "MANAGER",

    @param:Json(name = "start_date")
    @field:ColumnInfo(name = "start_date")
    val startDate: String,

    @param:Json(name = "is_active")
    @field:ColumnInfo(name = "is_active")
    val isActive: Int = 1,

    @field:ColumnInfo(name = "achievements")
    val achievements: String? = null,

    @param:Json(name = "seasons_completed")
    @field:ColumnInfo(name = "seasons_completed")
    val seasonsCompleted: Int = 0,

    @param:Json(name = "total_matches")
    @field:ColumnInfo(name = "total_matches")
    val totalMatches: Int = 0,

    @param:Json(name = "total_wins")
    @field:ColumnInfo(name = "total_wins")
    val totalWins: Int = 0,

    @param:Json(name = "total_draws")
    @field:ColumnInfo(name = "total_draws")
    val totalDraws: Int = 0,

    @param:Json(name = "total_losses")
    @field:ColumnInfo(name = "total_losses")
    val totalLosses: Int = 0,

    @param:Json(name = "total_goals_for")
    @field:ColumnInfo(name = "total_goals_for")
    val totalGoalsFor: Int = 0,

    @param:Json(name = "total_goals_against")
    @field:ColumnInfo(name = "total_goals_against")
    val totalGoalsAgainst: Int = 0,

    @param:Json(name = "total_trophies")
    @field:ColumnInfo(name = "total_trophies")
    val totalTrophies: Int = 0,

    @param:Json(name = "highest_league_position")
    @field:ColumnInfo(name = "highest_league_position")
    val highestLeaguePosition: Int? = null,

    @param:Json(name = "highest_league_level")
    @field:ColumnInfo(name = "highest_league_level")
    val highestLeagueLevel: Int? = null,

    @param:Json(name = "longest_winning_streak")
    @field:ColumnInfo(name = "longest_winning_streak")
    val longestWinningStreak: Int = 0,

    @param:Json(name = "longest_unbeaten_streak")
    @field:ColumnInfo(name = "longest_unbeaten_streak")
    val longestUnbeatenStreak: Int = 0,

    @param:Json(name = "total_transfer_budget_used")
    @field:ColumnInfo(name = "total_transfer_budget_used")
    val totalTransferBudgetUsed: Long = 0L,

    @param:Json(name = "total_wage_budget_used")
    @field:ColumnInfo(name = "total_wage_budget_used")
    val totalWageBudgetUsed: Long = 0L,

    @param:Json(name = "created_at")
    @field:ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @param:Json(name = "last_updated")
    @field:ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),

    @param:Json(name = "save_name")
    @field:ColumnInfo(name = "save_name")
    val saveName: String? = null,

    @field:ColumnInfo(name = "notes")
    val notes: String? = null
)
