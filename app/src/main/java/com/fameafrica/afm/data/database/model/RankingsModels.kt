package com.fameafrica.afm.data.database.model

import kotlinx.serialization.Serializable

@Serializable
data class GlobalLeagueRanking(
    val rank: Int,
    val leagueName: String,
    val country: String,
    val averageRating: Double,
    val totalMarketValue: Long,
    val region: String = "Others",
    val logoPath: String? = null
)

@Serializable
data class GlobalClubRanking(
    val rank: Int,
    val clubName: String,
    val league: String,
    val reputation: Int,
    val totalMarketValue: Long,
    val lastTrophy: String?,
    val logoPath: String? = null
)

@Serializable
data class GlobalManagerRanking(
    val rank: Int,
    val managerName: String,
    val currentClub: String?,
    val trophiesWon: Int,
    val winPercentage: Double,
    val reputation: Int
)
