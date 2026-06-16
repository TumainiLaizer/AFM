package com.fameafrica.afm.data.repository

data class HeadToHeadRecord(
    val totalMatches: Int,
    val team1Wins: Int,
    val team2Wins: Int,
    val draws: Int,
    val team1Goals: Int,
    val team2Goals: Int,
    val team1RecentForm: String,
    val team2RecentForm: String
)
