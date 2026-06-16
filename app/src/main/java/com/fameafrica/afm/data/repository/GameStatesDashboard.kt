package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.entities.GameStatesEntity

data class SeasonSaves(
    val season: String,
    val saveCount: Int,
    val latestSave: GameStatesEntity?
)

data class TeamSaves(
    val teamName: String,
    val saveCount: Int,
    val latestSave: GameStatesEntity?
)

data class GameStatesDashboard(
    val totalSaves: Int,
    val validSaves: Int,
    val invalidSaves: Int,
    val latestSave: GameStatesEntity?,
    val savesBySeason: List<SeasonSaves>,
    val topTeamsBySaves: List<TeamSaves>,
    val allSaves: List<GameStatesEntity>
)

data class SaveGameSummary(
    val totalSaves: Int,
    val validSaves: Int,
    val latestSave: GameStatesEntity?,
    val oldestSave: GameStatesEntity?,
    val averageWeek: Double
)
