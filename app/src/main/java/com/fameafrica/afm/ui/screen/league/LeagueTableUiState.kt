package com.fameafrica.afm.ui.screen.league

import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.ui.screen.match.FixtureUiModel

data class LeagueTableUiState(
    val isLoading: Boolean = false,
    val leagueName: String = "",
    val leagueLogo: String? = null,
    val season: String = "",
    val standings: List<LeagueStandingUiModel> = emptyList(),
    val leagueStats: LeagueStatsUiModel? = null,
    val fixtures: List<FixtureUiModel> = emptyList(),
    val topScorers: List<TopScorerUiModel> = emptyList(),
    val topAssisters: List<TopAssisterUiModel> = emptyList(),
    val teamOfTheWeek: List<TOTWPlayerUiModel> = emptyList(),
    val totwFormation: String = "4-4-2",
    val teamOfTheWeekRound: Int = 1,
    val maxRounds: Int = 30,
    val userTeamId: Int = 0,
    val availableLeagues: List<LeaguesEntity> = emptyList(),
    val errorMessage: String? = null
)

data class LeagueStandingUiModel(
    val id: Int,
    val position: Int,
    val teamName: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val gf: Int,
    val ga: Int,
    val goalDifference: Int,
    val points: Int,
    val form: String,
    val logoPath: String?
)

data class LeagueStatsUiModel(
    val totalMatches: Int,
    val totalGoals: Int,
    val avgGoals: Double,
    val homeWinPct: Double,
    val awayWinPct: Double,
    val drawPct: Double
)

data class TopScorerUiModel(
    val position: Int,
    val playerId: Int,
    val playerName: String,
    val teamName: String,
    val goals: Int,
    val matches: Int,
    val age: Int
)

data class TopAssisterUiModel(
    val position: Int,
    val playerId: Int,
    val playerName: String,
    val teamName: String,
    val assists: Int,
    val matches: Int
)

data class TOTWPlayerUiModel(
    val playerId: Int,
    val playerName: String,
    val teamName: String,
    val position: String,
    val matchRating: Double,
    val nationality: String?,
    val shirtNumber: Int,
    val goals: Int,
    val assists: Int,
    val cleanSheet: Boolean,
    val motm: Boolean
)
