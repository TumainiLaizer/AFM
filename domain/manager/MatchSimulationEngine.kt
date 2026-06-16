package com.fameafrica.afm2026.domain.manager

import com.fameafrica.afm2026.data.database.entities.*
import com.fameafrica.afm2026.data.repository.*
import com.fameafrica.afm2026.utils.calculators.EloCalculator
import com.fameafrica.afm2026.utils.commentary.AfricanFootballCommentaryGenerator
import com.fameafrica.afm2026.utils.constants.AfricanFootballDataHelper
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Advanced Match Simulation Engine for AFM2026.
 * Implements CAF-compliant substitution rules and realistic attribute-based simulation.
 * Calibrated against real-world African football data (footystats.org) via AfricanFootballDataHelper.
 * Now improvised with Rivalry Intensity, Form-based Man of the Match, and deeper repository integration.
 */
@Singleton
class MatchSimulationEngine @Inject constructor(
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val tacticsRepository: TacticsRepository,
    private val managersRepository: ManagersRepository,
    private val fixturesRepository: FixturesRepository
) {

    data class SimTeam(
        val team: TeamsEntity,
        var tactics: TacticsEntity,
        val manager: ManagersEntity?,
        val currentXi: MutableList<PlayersEntity>,
        val substitutes: MutableList<PlayersEntity>,
        var score: Int = 0,
        var possession: Double = 0.0,
        var shots: Int = 0,
        var shotsOnTarget: Int = 0,
        var passesAttempted: Int = 0,
        var passesCompleted: Int = 0,
        var corners: Int = 0,
        var fouls: Int = 0,
        var yellowCards: Int = 0,
        var redCards: Int = 0,
        var substitutionsUsed: Int = 0,
        var subWindowsUsed: Int = 0,
        val events: MutableList<MatchEventsEntity> = mutableListOf(),
        var redCardedPlayerIds: MutableSet<Int> = mutableSetOf(),
        var matchRatingMap: MutableMap<Int, Double> = mutableMapOf(),
        var isRivalryMatch: Boolean = false,
        var playerStatsMap: MutableMap<Int, PlayerMatchStats> = mutableMapOf()
    )

    data class PlayerMatchStats(
        var goals: Int = 0,
        var assists: Int = 0,
        var passes: Int = 0,
        var dribbles: Int = 0,
        var tackles: Int = 0,
        var shotsOnTarget: Int = 0
    )

    suspend fun simulateMatch(fixture: FixturesEntity): GameManager.MatchResult {
        val homeTeamObj = teamsRepository.getTeamByName(fixture.homeTeam)!!
        val awayTeamObj = teamsRepository.getTeamByName(fixture.awayTeam)!!

        val homeTactics = tacticsRepository.getTacticsByTeam(fixture.homeTeam) ?: createFallbackTactics(fixture.homeTeam)
        val awayTactics = tacticsRepository.getTacticsByTeam(fixture.awayTeam) ?: createFallbackTactics(fixture.awayTeam)

        val homeManager = homeTeamObj.managerId?.let { managersRepository.getManagerById(it) }
        val awayManager = awayTeamObj.managerId?.let { managersRepository.getManagerById(it) }

        val homePlayers = playersRepository.getPlayersByTeamName(fixture.homeTeam).firstOrNull() ?: emptyList()
        val awayPlayers = playersRepository.getPlayersByTeamName(fixture.awayTeam).firstOrNull() ?: emptyList()

        val isRivalry = homeTeamObj.rivalTeam == awayTeamObj.name || awayTeamObj.rivalTeam == homeTeamObj.name

        val homeSim = SimTeam(
            homeTeamObj, homeTactics, homeManager,
            homePlayers.filter { it.isStartingXi }.toMutableList(),
            homePlayers.filter { !it.isStartingXi }.sortedByDescending { it.overallRating }.take(7).toMutableList(),
            isRivalryMatch = isRivalry
        )
        val awaySim = SimTeam(
            awayTeamObj, awayTactics, awayManager,
            awayPlayers.filter { it.isStartingXi }.toMutableList(),
            awayPlayers.filter { !it.isStartingXi }.sortedByDescending { it.overallRating }.take(7).toMutableList(),
            isRivalryMatch = isRivalry
        )

        // Initialize match ratings with Form (0-10) and "Rivalry Pulse" boost
        (homeSim.currentXi + awaySim.currentXi).forEach { 
            // Scale current_form (0-10) to a match rating contribution
            val formContribution = (it.currentForm / 10.0) * 2.0 // Max 2.0 boost from form
            val baseRating = 5.0 + formContribution + (it.morale / 100.0)
            val rivalryBoost = if (isRivalry) 0.5 else 0.0
            
            homeSim.matchRatingMap[it.id] = (baseRating + rivalryBoost).coerceIn(5.0, 8.5)
            homeSim.playerStatsMap[it.id] = PlayerMatchStats()
            awaySim.playerStatsMap[it.id] = PlayerMatchStats()
        }

        val competitionName = fixture.league ?: fixture.cupName
        val simParams = AfricanFootballDataHelper.getSimulationParams(competitionName)

        val h2h = fixturesRepository.getHeadToHead(fixture.homeTeam, fixture.awayTeam).firstOrNull() ?: emptyList()
        val hWins = h2h.count { it.winner == fixture.homeTeam }
        val aWins = h2h.count { it.winner == fixture.awayTeam }
        val h2hBonus = (hWins - aWins) * 0.02

        val winProb = EloCalculator.calculateWinProbability(homeTeamObj.eloRating, awayTeamObj.eloRating, false)
        val homeMoraleBonus = (homeTeamObj.morale - 50) / 500.0
        val awayMoraleBonus = (awayTeamObj.morale - 50) / 500.0
        val adjustedHomeProb = (winProb * simParams.homeAdvantage).coerceIn(0.1, 0.95)
        val finalHomeWinProb = (adjustedHomeProb + h2hBonus + homeMoraleBonus - awayMoraleBonus).coerceIn(0.05, 0.95)

        // Midfield control influenced by team fan loyalty and rivalry intensity
        val fanLoyaltyBonus = homeTeamObj.fanLoyalty / 1000.0
        val rivalryIntensityBonus = if (isRivalry) 0.15 else 0.0
        val hMidPowerRaw = if (homeSim.currentXi.isNotEmpty()) homeSim.currentXi.filter { it.positionCategory == "MIDFIELDER" }.map { it.passing.toDouble() }.average().ifNaN(60.0) else 60.0
        val aMidPowerRaw = if (awaySim.currentXi.isNotEmpty()) awaySim.currentXi.filter { it.positionCategory == "MIDFIELDER" }.map { it.passing.toDouble() }.average().ifNaN(60.0) else 60.0
        
        val hMidPower = (hMidPowerRaw * 0.6 + (homeTeamObj.avgPlaymakingAbility ?: hMidPowerRaw) * 0.4) + fanLoyaltyBonus + rivalryIntensityBonus
        val aMidPower = (aMidPowerRaw * 0.6 + (awayTeamObj.avgPlaymakingAbility ?: aMidPowerRaw) * 0.4) + rivalryIntensityBonus
        
        val crowdEffect = (homeTeamObj.crowdSupport + homeTeamObj.fanLoyalty) / 1000.0
        var hPossBase = 50.0 + (hMidPower - aMidPower) * 0.4 + crowdEffect * 10.0
        if (homeTactics.tacticalArchetype == "POSSESSION") hPossBase += 5.0
        if (awayTactics.tacticalArchetype == "POSSESSION") hPossBase -= 5.0
        homeSim.possession = hPossBase.coerceIn(35.0, 65.0)
        awaySim.possession = 100.0 - homeSim.possession

        val stoppageTime = Random.nextInt(2, 7)
        for (minute in 1..(90 + stoppageTime)) {
            val period = if (minute <= 45) "FIRST_HALF" else "SECOND_HALF"
            val dynamicWinProb = (finalHomeWinProb + (awaySim.redCards - homeSim.redCards) * 0.15).coerceIn(0.01, 0.99)
            simulateMinute(minute, homeSim, awaySim, fixture, dynamicWinProb, period, simParams)
        }

        val hPassQuality = (hMidPower / 100.0).coerceIn(0.68, 0.85)
        val aPassQuality = (aMidPower / 100.0).coerceIn(0.68, 0.85)
        
        homeSim.passesAttempted = (homeSim.possession * 9).toInt()
        awaySim.passesAttempted = (awaySim.possession * 9).toInt()
        homeSim.passesCompleted = (homeSim.passesAttempted * (hPassQuality - Random.nextDouble(0.05))).toInt()
        awaySim.passesCompleted = (awaySim.passesAttempted * (aPassQuality - Random.nextDouble(0.05))).toInt()

        // Distribute passes among players
        distributeGeneralStats(homeSim)
        distributeGeneralStats(awaySim)

        val motm = calculateManOfTheMatch(homeSim, awaySim)
        finalizeMatchUpdates(homeSim, awaySim, motm?.id)

        return GameManager.MatchResult(
            fixture = fixture,
            homeScore = homeSim.score,
            awayScore = awaySim.score,
            result = if (homeSim.score > awaySim.score) "HOME_WIN" else if (awaySim.score > homeSim.score) "AWAY_WIN" else "DRAW",
            events = homeSim.events + awaySim.events,
            homeTeamObj = homeTeamObj,
            awayTeamObj = awayTeamObj,
            homeManager = homeManager,
            awayManager = awayManager,
            referee = null,
            manOfTheMatch = motm
        )
    }

    private fun simulateMinute(
        minute: Int, 
        home: SimTeam, 
        away: SimTeam, 
        fixture: FixturesEntity, 
        homeWinProb: Double, 
        period: String,
        params: AfricanFootballDataHelper.SimulationParams
    ) {
        val rivalryMultiplier = if (home.isRivalryMatch) 1.25 else 1.0
        val attackProb = 0.10 * params.goalFrequency * rivalryMultiplier
        
        if (Random.nextDouble() < attackProb) {
            val isHomeAttack = Random.nextDouble() < homeWinProb
            val attacker = if (isHomeAttack) home else away
            val defender = if (isHomeAttack) away else home

            if (attacker.currentXi.isEmpty()) return

            val attPlayerPower = attacker.currentXi.map { it.finishing.toDouble() }.average().ifNaN(55.0) * 0.6 + 
                                 attacker.currentXi.map { it.pace.toDouble() }.average().ifNaN(60.0) * 0.4
            val attPower = (attPlayerPower * 0.7 + (attacker.team.avgAttackingAbility ?: attPlayerPower) * 0.3)
            
            val defPlayerPower = if (defender.currentXi.isNotEmpty()) {
                defender.currentXi.map { it.defending.toDouble() }.average().ifNaN(55.0) * 0.7 + 
                defender.currentXi.map { it.strength.toDouble() }.average().ifNaN(60.0) * 0.3
            } else 30.0
            val defPower = (defPlayerPower * 0.7 + (defender.team.avgDefenceAbility ?: defPlayerPower) * 0.3)

            var attackEfficiency = attPower / (attPower + defPower)
            if (params.drawTendency > 1.0) attackEfficiency /= (1.0 + (params.drawTendency - 1.0) * 0.5)
            attacker.manager?.let { attackEfficiency *= (1.0 + (it.reputation - 50) / 500.0) }

            if (Random.nextDouble() < attackEfficiency) {
                attacker.shots++
                if (Random.nextDouble() < 0.42) {
                    attacker.shotsOnTarget++
                    val activeAttackers = attacker.currentXi.filter { it.positionCategory != "GK" }
                    val currentScorer = activeAttackers.randomOrNull()
                    
                    if (Random.nextDouble() < 0.13 * params.goalFrequency) {
                        attacker.score++
                        currentScorer?.let { scorer ->
                            attacker.playerStatsMap[scorer.id]?.goals = (attacker.playerStatsMap[scorer.id]?.goals ?: 0) + 1
                            attacker.matchRatingMap[scorer.id] = (attacker.matchRatingMap[scorer.id] ?: 6.0) + 1.2
                            attacker.events.add(createGoalEvent(minute, scorer, attacker.team.name, defender.team.name, home.score, away.score, fixture.id, period))
                        }
                    } else {
                        currentScorer?.let { s -> attacker.playerStatsMap[s.id]?.shotsOnTarget = (attacker.playerStatsMap[s.id]?.shotsOnTarget ?: 0) + 1 }
                        val keeper = defender.currentXi.firstOrNull { it.position == "GK" }
                        keeper?.let { defender.matchRatingMap[it.id] = (defender.matchRatingMap[it.id] ?: 6.0) + 0.2 }
                    }
                }
            } else {
                val foulChance = 0.18 * params.foulFrequency * rivalryMultiplier
                if (Random.nextDouble() < foulChance) {
                    defender.fouls++
                    val player = defender.currentXi.randomOrNull()
                    if (player != null && Random.nextDouble() < (0.22 * rivalryMultiplier)) {
                        val isRed = Random.nextDouble() < (0.05 * rivalryMultiplier) || (defender.events.count { it.playerId == player.id && it.eventType == "YELLOW_CARD" } >= 1)
                        if (isRed) {
                            defender.redCards++
                            defender.redCardedPlayerIds.add(player.id)
                            defender.currentXi.remove(player)
                            defender.matchRatingMap[player.id] = (defender.matchRatingMap[player.id] ?: 6.0) - 2.0
                            defender.events.add(createCardEvent(minute, player, defender.team.name, attacker.team.name, "RED_CARD", fixture.id, period))
                        } else {
                            defender.yellowCards++
                            defender.matchRatingMap[player.id] = (defender.matchRatingMap[player.id] ?: 6.0) - 0.5
                            defender.events.add(createCardEvent(minute, player, defender.team.name, attacker.team.name, "YELLOW_CARD", fixture.id, period))
                        }
                    }
                }
            }
        }

        // African Substitution Logic
        if (Random.nextDouble() < (if (minute < 45) 0.005 else if (minute < 70) 0.03 else 0.08)) {
            trySubstitution(minute, home, fixture.id, period, isAuto = false)
            tryOpponentSubstitution(minute, away, fixture.id, period)
        }
    }

    private fun distributeGeneralStats(team: SimTeam) {
        team.currentXi.forEach { player ->
            val stats = team.playerStatsMap[player.id] ?: return@forEach
            // Distribute passes based on position and possession
            val basePasses = (team.passesCompleted / 11).coerceAtLeast(10)
            stats.passes = basePasses + when(player.positionCategory) {
                "MIDFIELDER" -> Random.nextInt(15, 25)
                "DEFENDER" -> Random.nextInt(5, 15)
                else -> Random.nextInt(0, 10)
            }
            stats.dribbles = if (player.positionCategory == "FORWARD" || player.positionCategory == "MIDFIELDER") Random.nextInt(1, 6) else 0
            stats.tackles = if (player.positionCategory == "DEFENDER" || player.positionCategory == "MIDFIELDER") Random.nextInt(2, 8) else 1
            
            // Adjust match rating based on contribution
            val contributionBonus = (stats.passes / 40.0) + (stats.dribbles * 0.2) + (stats.tackles * 0.15)
            team.matchRatingMap[player.id] = (team.matchRatingMap[player.id] ?: 6.0) + contributionBonus
        }
    }

    private fun calculateManOfTheMatch(home: SimTeam, away: SimTeam): PlayersEntity? {
        val allPlayers = (home.currentXi + away.currentXi)
        return allPlayers.maxByOrNull { player ->
            val hStats = home.playerStatsMap[player.id]
            val aStats = away.playerStatsMap[player.id]
            val s = hStats ?: aStats ?: PlayerMatchStats()
            
            // Heuristic for MOTM: Goals > Assists > Contributions (Rating)
            (s.goals * 3.0) + (s.assists * 1.5) + (home.matchRatingMap[player.id] ?: away.matchRatingMap[player.id] ?: 6.0)
        }
    }

    private suspend fun finalizeMatchUpdates(home: SimTeam, away: SimTeam, motmId: Int?) {
        val result = if (home.score > away.score) "HOME_WIN" else if (away.score > home.score) "AWAY_WIN" else "DRAW"
        val rivalryMultiplier = if (home.isRivalryMatch) 2.5 else 1.0
        
        teamsRepository.updateTeamMorale(home.team.id, ((if (result == "HOME_WIN") 5 else if (result == "DRAW") 0 else -5) * rivalryMultiplier).toInt())
        teamsRepository.updateTeamMorale(away.team.id, ((if (result == "AWAY_WIN") 7 else if (result == "DRAW") 2 else -3) * rivalryMultiplier).toInt())

        (home.currentXi + home.substitutes + away.currentXi + away.substitutes).forEach { player ->
            val rating = (home.matchRatingMap[player.id] ?: away.matchRatingMap[player.id] ?: 6.0).coerceIn(4.0, 10.0)
            val hStats = home.playerStatsMap[player.id]
            val aStats = away.playerStatsMap[player.id]
            val s = hStats ?: aStats ?: PlayerMatchStats()
            val isMotm = player.id == motmId
            
            playersRepository.updatePlayerAfterMatch(player.id, s.goals, s.assists, isMotm)
            
            // Refine Current Form (0-10) based on match performance
            // A rating of 7.0 keeps form stable. Above 7.0 increases, below 7.0 decreases.
            val formChange = (rating - 7.0).coerceIn(-1.5, 1.5)
            val newForm = (player.currentForm + formChange).coerceIn(1.0, 10.0).toInt()
            
            // Update player form in DB
            playersRepository.updatePlayerAttributes(player.id, mapOf("current_form" to newForm), 0) // currentWeek 0 for now
            
            if (rating > 7.5) playersRepository.updatePlayerMorale(player.id, (3 * rivalryMultiplier).toInt())
            else if (rating < 5.5) playersRepository.updatePlayerMorale(player.id, (-3 * rivalryMultiplier).toInt())
        }
    }

    private fun tryOpponentSubstitution(minute: Int, team: SimTeam, matchId: Int, period: String) {
        if (team.substitutionsUsed >= 5 || team.subWindowsUsed >= 3) return
        if (minute < 55 && team.score >= 0) return
        val playerOff = team.currentXi.minByOrNull { team.matchRatingMap[it.id] ?: 6.0 }
        val playerOn = team.substitutes.maxByOrNull { it.overallRating }
        if (playerOff != null && playerOn != null) performSubstitution(minute, team, playerOff, playerOn, matchId, period)
    }

    private fun trySubstitution(minute: Int, team: SimTeam, matchId: Int, period: String, isAuto: Boolean) {
        if (isAuto && team.substitutionsUsed < 5) {
             val tiredPlayer = team.currentXi.minByOrNull { it.stamina }
             if (tiredPlayer != null && tiredPlayer.stamina < 30) {
                 val sub = team.substitutes.maxByOrNull { it.overallRating }
                 if (sub != null) performSubstitution(minute, team, tiredPlayer, sub, matchId, period)
             }
        }
    }

    private fun performSubstitution(minute: Int, team: SimTeam, off: PlayersEntity, on: PlayersEntity, matchId: Int, period: String) {
        team.substitutionsUsed++
        team.subWindowsUsed++
        team.currentXi.remove(off)
        team.currentXi.add(on)
        team.substitutes.remove(on)
        team.matchRatingMap[on.id] = 6.0
        team.events.add(createSubEvent(minute, off, on, team.team.name, matchId, period))
    }

    private fun createGoalEvent(minute: Int, player: PlayersEntity, team: String, opponent: String, h: Int, a: Int, matchId: Int, period: String) = MatchEventsEntity(
        matchId = matchId, teamName = team, opponentTeam = opponent, playerId = player.id, playerName = player.name,
        eventType = "GOAL", minute = minute, homeScore = h, awayScore = a, period = period
    )

    private fun createCardEvent(minute: Int, player: PlayersEntity, team: String, opponent: String, type: String, matchId: Int, period: String) = MatchEventsEntity(
        matchId = matchId, teamName = team, opponentTeam = opponent, playerId = player.id, playerName = player.name,
        eventType = type, minute = minute, period = period
    )

    private fun createSubEvent(minute: Int, off: PlayersEntity, on: PlayersEntity, team: String, matchId: Int, period: String) = MatchEventsEntity(
        matchId = matchId, teamName = team, playerId = off.id, playerName = off.name,
        eventType = "SUBSTITUTION", minute = minute, substitutionInPlayer = on.name, substitutionInPlayerId = on.id,
        substitutionOutPlayer = off.name, substitutionOutPlayerId = off.id, period = period
    )

    private fun createFallbackTactics(teamName: String) = TacticsEntity(
        teamName = teamName, formation = "4-4-2", tacticalArchetype = "BALANCED", playstyle = "Balanced"
    )

    private fun Double.ifNaN(fallback: Double): Double = if (this.isNaN()) fallback else this
}
