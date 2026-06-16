package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.data.repository.PlayersRepository
import com.fameafrica.afm.data.repository.TacticsRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.utils.LeagueRankings
import com.fameafrica.afm.utils.tactics.TacticalMatchupEngine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SeasonPredictorEngine @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val tacticsRepository: TacticsRepository,
    private val playersRepository: PlayersRepository
) {

    data class SeasonPrediction(
        val leagueName: String,
        val predictedStandings: List<TeamPrediction>,
        val championId: Int,
        val top4Ids: List<Int>,
        val cafCLQualifiedIds: List<Int>,
        val cafCCQualifiedIds: List<Int>,
        val relegationIds: List<Int>
    )

    data class TeamPrediction(
        val teamId: Int,
        val teamName: String,
        val predictedPoints: Int,
        val predictedPosition: Int,
        val confidence: Double,
        val keyPlayerName: String?,
        val strength: Double,
        val qualificationStatus: String? = null
    )

    suspend fun generateLeaguePrediction(league: LeaguesEntity): SeasonPrediction {
        val teams = teamsRepository.getTeamsByLeagueSync(league.name)
        val tactics = tacticsRepository.getTacticsByTeamsSync(teams.map { it.id }).associateBy { it.teamId }
        
        val predictions = teams.map { team ->
            val teamTactics = tactics[team.id]
            val players = playersRepository.getPlayersByTeamIdSync(team.id)
            val avgAbility = players.map { it.rating }.average().takeIf { !it.isNaN() } ?: 50.0
            
            val eloWeight = 0.6
            val abilityWeight = 0.4
            val strength = ((team.eloRating / 2000.0 * eloWeight) + (avgAbility / 100.0 * abilityWeight)) * 100.0
            
            TeamPrediction(
                teamId = team.id,
                teamName = team.name,
                predictedPoints = 0, // Calculated below
                predictedPosition = 0,
                confidence = 0.7 + (Random.nextDouble() * 0.2),
                keyPlayerName = players.maxByOrNull { it.rating }?.name,
                strength = strength
            )
        }

        // Simulate 50 season runs for statistical stability (Monte Carlo)
        val standingsMap = mutableMapOf<Int, Int>() // teamId to totalPoints
        val runs = 20 // Lower runs for performance if needed, 20 is enough for a prediction
        
        repeat(runs) {
            for (i in predictions.indices) {
                for (j in i + 1 until predictions.size) {
                    val home = predictions[i]
                    val away = predictions[j]
                    
                    // Run two matches (Home & Away)
                    standingsMap[home.teamId] = (standingsMap[home.teamId] ?: 0) + simulateMatchPoints(home.strength, away.strength, true)
                    standingsMap[away.teamId] = (standingsMap[away.teamId] ?: 0) + simulateMatchPoints(away.strength, home.strength, false)
                    
                    standingsMap[away.teamId] = (standingsMap[away.teamId] ?: 0) + simulateMatchPoints(away.strength, home.strength, true)
                    standingsMap[home.teamId] = (standingsMap[home.teamId] ?: 0) + simulateMatchPoints(home.strength, away.strength, false)
                }
            }
        }

        val sortedStandings = predictions.map { 
            it.copy(predictedPoints = (standingsMap[it.teamId] ?: 0) / runs) 
        }.sortedByDescending { it.predictedPoints }

        val rankedStandings = sortedStandings.mapIndexed { index, p -> 
            p.copy(predictedPosition = index + 1) 
        }

        if (rankedStandings.isEmpty()) {
            return SeasonPrediction(
                leagueName = league.name,
                predictedStandings = emptyList(),
                championId = -1,
                top4Ids = emptyList(),
                cafCLQualifiedIds = emptyList(),
                cafCCQualifiedIds = emptyList(),
                relegationIds = emptyList()
            )
        }

        val rank = LeagueRankings.getLeagueRank(league.country ?: "", league.level)
        val isTop5 = league.country in setOf("Egypt", "Morocco", "South Africa", "Algeria", "Tanzania")
        val isTop11 = rank <= 11

        val clIds = mutableListOf<Int>()
        val ccIds = mutableListOf<Int>()

        if (isTop11 && rankedStandings.isNotEmpty()) {
            clIds.add(rankedStandings[0].teamId) // 1st always CL
            if (rankedStandings.size >= 3) {
                ccIds.add(rankedStandings[2].teamId) // 3rd always CC
            }
            
            if (isTop5 && rankedStandings.size >= 2) {
                clIds.add(rankedStandings[1].teamId) // 2nd CL for Top 5
            }
            if (isTop5 && rankedStandings.size >= 4) {
                ccIds.add(rankedStandings[3].teamId) // 4th CC for Top 5
            }
        }

        val finalWithQual = rankedStandings.map { p ->
            val status = when {
                p.teamId == rankedStandings[0].teamId -> "Champions"
                clIds.contains(p.teamId) -> "CAF Champions League"
                ccIds.contains(p.teamId) -> "CAF Confederation Cup"
                else -> null
            }
            p.copy(qualificationStatus = status)
        }

        return SeasonPrediction(
            leagueName = league.name,
            predictedStandings = finalWithQual,
            championId = rankedStandings[0].teamId,
            top4Ids = rankedStandings.take(4).map { it.teamId },
            cafCLQualifiedIds = clIds,
            cafCCQualifiedIds = ccIds,
            relegationIds = rankedStandings.takeLast(league.relegationSpots.coerceAtMost(rankedStandings.size)).map { it.teamId }
        )
    }

    private fun simulateMatchPoints(homeStr: Double, awayStr: Double, isHome: Boolean): Int {
        val diff = if (isHome) (homeStr * 1.1) - awayStr else homeStr - (awayStr * 1.1)
        val rand = Random.nextDouble()
        
        return when {
            diff > 15 -> if (rand < 0.8) 3 else if (rand < 0.95) 1 else 0
            diff > 5 -> if (rand < 0.6) 3 else if (rand < 0.85) 1 else 0
            diff < -15 -> if (rand < 0.1) 3 else if (rand < 0.3) 1 else 0
            diff < -5 -> if (rand < 0.25) 3 else if (rand < 0.5) 1 else 0
            else -> if (rand < 0.35) 3 else if (rand < 0.7) 1 else 0
        }
    }
    
    suspend fun getMatchAnalysis(fixtureId: Int): MatchAnalysis {
        // Mocking for now, will implement actual logic
        return MatchAnalysis("Balanced", "Watch out for counter attacks")
    }

    data class MatchAnalysis(val winProbability: String, val tacticalAdvice: String)
}
