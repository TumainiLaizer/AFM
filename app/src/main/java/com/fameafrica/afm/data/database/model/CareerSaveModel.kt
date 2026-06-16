package com.fameafrica.afm.data.database.model

/**
 * UI model representing a saved career game.
 */
data class CareerSaveModel(
    val careerId: Int,
    val managerId: Int,
    val managerName: String,
    val managerAvatar: String?,
    val teamId: Int,
    val teamName: String,
    val season: String,
    val week: Int,
    val gameDate: String,
    val difficulty: String,
    val lastPlayed: String,
    val saveName: String,
    val gameVersion: String
)