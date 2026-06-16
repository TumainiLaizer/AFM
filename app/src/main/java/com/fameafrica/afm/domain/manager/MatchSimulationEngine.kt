package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.data.database.model.match.MatchIntensity
import com.fameafrica.afm.data.database.model.match.MatchUpdate
import com.fameafrica.afm.utils.constants.AfricanFootballDataHelper
import com.fameafrica.afm.utils.tactics.LineupUtils
import com.fameafrica.afm.utils.tactics.TacticalMatchupEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MatchSimulationEngine @Inject constructor(
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val tacticsRepository: TacticsRepository,
    private val managersRepository: ManagersRepository,
    private val clubDNARepository: ClubDNARepository,
) {

    data class SimTeam(
        val team: TeamsEntity,
        var tactics: TacticsEntity,
        val manager: ManagersEntity?,
        val currentXi: MutableList<PlayersEntity>,
        val substitutes: MutableList<PlayersEntity>,
        var score: Int = 0,
        var possession: Double = 50.0,
        var shots: Int = 0,
        var shotsOnTarget: Int = 0,
        var corners: Int = 0,
        var fouls: Int = 0,
        var yellowCards: Int = 0,
        var redCards: Int = 0,
        var substitutionsUsed: Int = 0,
        var currentMentality: String = "BALANCED", 
        val events: MutableList<MatchEventsEntity> = mutableListOf(),
        var matchRatingMap: MutableMap<Int, Double> = mutableMapOf(),
        var playerStatsMap: MutableMap<Int, PlayerMatchStats> = mutableMapOf(),
        var attackEfficiency: Double = 0.5,
        var passesCompleted: Int = 0,
        var passesAttempted: Int = 0,
        var strength: Double = 50.0,
        var offsides: Int = 0,
        var saves: Int = 0,
        var xG: Double = 0.0,
        var keyPasses: Int = 0,
        var bigChancesMissed: Int = 0,
        
        // Momentum and Stability
        var momentum: Double = 0.0, // Match-specific momentum (shifts during game)
        var preMatchForm: Double = 0.0, // Based on formStreak
        var tacticalFamiliarity: Double = 0.0, // Based on tacticalStability
        
        // Assigned Roles from Tactics
        var captainId: Int? = null,
        var viceCaptainId: Int? = null,
        var penaltyTakerId: Int? = null,
        var freeKickTakerId: Int? = null,
        var cornerTakerId: Int? = null,

        // DNA integration
        var dna: ClubDNAEntity? = null,
    )

    data class PlayerMatchStats(
        val id: Int,
        var goals: Int = 0,
        var assists: Int = 0,
        var shotsOnTarget: Int = 0,
        var rating: Double = 6.0,
        var passesAttempted: Int = 0,
        var passesCompleted: Int = 0,
        var tackles: Int = 0,
        var interceptions: Int = 0,
        var dribbles: Int = 0,
        var crosses: Int = 0,
        var blocks: Int = 0,
        var keyPasses: Int = 0
    )

    data class TeamMatchStats(
        val possession: Int,
        val shots: Int,
        val shotsOnTarget: Int,
        val corners: Int,
        val fouls: Int,
        val yellowCards: Int,
        val redCards: Int,
        val offsides: Int,
        val saves: Int,
        val xG: Double = 0.0,
        val keyPasses: Int = 0,
        val bigChancesMissed: Int = 0,
        val tackles: Int = 0,
        val interceptions: Int = 0
    )

    data class MatchResult(
        val fixture: FixturesEntity,
        val homeScore: Int,
        val awayScore: Int,
        val result: String,
        val events: List<MatchEventsEntity>,
        val homeTeamObj: TeamsEntity,
        val awayTeamObj: TeamsEntity,
        val playerStats: Map<Int, PlayerMatchStats>,
        val manOfTheMatchId: Int?,
        val homeManager: ManagersEntity? = null,
        val awayManager: ManagersEntity? = null,
        val homeStats: TeamMatchStats,
        val awayStats: TeamMatchStats
    )

    fun simulateMatch(
        fixture: FixturesEntity,
        userTeamId: Int = -1
    ): Flow<com.fameafrica.afm.data.database.model.match.MatchUpdate> = flow {
        val homeTeamObj = teamsRepository.getTeamById(fixture.homeTeamId)!!
        val awayTeamObj = teamsRepository.getTeamById(fixture.awayTeamId)!!

        val homeDNA = clubDNARepository.getClubDNA(homeTeamObj.id)
        val awayDNA = clubDNARepository.getClubDNA(awayTeamObj.id)

        val homeTactics = tacticsRepository.getTacticsByTeam(homeTeamObj.id) ?: TacticsEntity(
            teamId = homeTeamObj.id,
            teamName = homeTeamObj.name,
            formation = homeTeamObj.formation ?: "4-4-2",
            tacticalArchetype = homeDNA?.playStyle ?: "BALANCED",
            playstyle = homeDNA?.playStyle ?: "Balanced"
        )
        val awayTactics = tacticsRepository.getTacticsByTeam(awayTeamObj.id) ?: TacticsEntity(
            teamId = awayTeamObj.id,
            teamName = awayTeamObj.name,
            formation = awayTeamObj.formation ?: "4-4-2",
            tacticalArchetype = awayDNA?.playStyle ?: "BALANCED",
            playstyle = awayDNA?.playStyle ?: "Balanced"
        )

        val homePlayers = playersRepository.getPlayersByTeamIdSync(homeTeamObj.id)
        val awayPlayers = playersRepository.getPlayersByTeamIdSync(awayTeamObj.id)

        val homeManager = managersRepository.getManagerByTeamId(homeTeamObj.id)
        val awayManager = managersRepository.getManagerByTeamId(awayTeamObj.id)

        val homeSim = prepareSimTeam(homeTeamObj, homeTactics, homeManager, homePlayers, homeDNA, userTeamId)
        val awaySim = prepareSimTeam(awayTeamObj, awayTactics, awayManager, awayPlayers, awayDNA, userTeamId)

        val params = AfricanFootballDataHelper.getSimulationParams(fixture.league ?: fixture.cupName)
        val weather = calculateWeatherImpact(fixture.weatherConditions)

        setupMatchDynamics(homeSim, awaySim, params)

        // Momentum Integration
        val isDerby = fixture.matchType == "DERBY" 
        homeSim.momentum = MomentumCalculator.calculateMatchMomentum(homeTeamObj, isDerby).toDouble()
        awaySim.momentum = MomentumCalculator.calculateMatchMomentum(awayTeamObj, isDerby).toDouble()

        homeSim.preMatchForm = calculateFormBonus(homeTeamObj.formStreak)
        awaySim.preMatchForm = calculateFormBonus(awayTeamObj.formStreak)
        homeSim.tacticalFamiliarity = homeTeamObj.tacticalStability / 100.0
        awaySim.tacticalFamiliarity = awayTeamObj.tacticalStability / 100.0

        val totalMinutes = 90 + Random.nextInt(2, 6)
        
        for (min in 1..totalMinutes) {
            emit(_root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchUpdate.MinuteUpdate(min))
            
            processManagerAI(min, homeSim, awaySim, fixture.id)
            processManagerAI(min, awaySim, homeSim, fixture.id)
            
            // Check for substitution events to emit
            (homeSim.events + awaySim.events).filter { it.minute == min && (it.eventType == EventType.SUBSTITUTION.value) }.forEach {
                emit(_root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchUpdate.EventUpdate(it, intensity = _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.LOW))
            }

            val event = simulateMinuteAndReturn(min, homeSim, awaySim, fixture, params, weather)
            if (event != null) {
                updateInGameMomentum(homeSim, awaySim, event)
                emit(_root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchUpdate.EventUpdate(event, intensity = mapEventToIntensity(event)))
                if (event.eventType == EventType.GOAL.value || event.eventType == EventType.PENALTY_SCORED.value) {
                    emit(_root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchUpdate.ScoreUpdate(homeSim.score, awaySim.score))
                }
            }

            if (min == 45) {
                emit(_root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchUpdate.HalfTime)
            }
        }

        emit(_root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchUpdate.FullTime)
    }

    private fun simulateMinuteAndReturn(
        minute: Int, home: SimTeam, away: SimTeam, fixture: FixturesEntity,
        params: AfricanFootballDataHelper.SimulationParams, weather: WeatherImpact
    ): MatchEventsEntity? {
        var result: MatchEventsEntity? = null
        simulateMinuteWithCallback(minute, home, away, fixture, params, weather) {
            result = it
        }
        return result
    }

    private fun mapEventToIntensity(event: MatchEventsEntity): com.fameafrica.afm.data.database.model.match.MatchIntensity {
        return when (event.eventType) {
            EventType.GOAL.value,
            EventType.PENALTY_SCORED.value,
            EventType.OWN_GOAL.value ->
                _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.GOAL

            EventType.SHOT_ON_TARGET.value,
            EventType.PENALTY_MISSED.value,
            EventType.BIG_CHANCE_MISSED.value,
            EventType.SAVE.value -> _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.BIG_CHANCE

            EventType.RED_CARD.value,
            EventType.INJURY.value,
            EventType.VAR.value -> _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.DRAMA

            EventType.YELLOW_CARD.value -> _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.CARD

            EventType.SHOT.value,
            EventType.SHOT_OFF_TARGET.value,
            EventType.CORNER.value,
            EventType.OFFSIDE.value,
            EventType.KEY_PASS.value,
            EventType.DRIBBLE.value,
            EventType.CROSS.value -> _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.BUILD_UP

            EventType.SUBSTITUTION.value,
            EventType.FOUL.value,
            EventType.PASS.value,
            EventType.TACKLE.value,
            EventType.INTERCEPTION.value,
            EventType.BLOCK.value,
            EventType.ASSIST.value -> _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.LOW

            else -> _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.LOW
        }
    }

    suspend fun simulateInstantResult(fixture: FixturesEntity, userTeamId: Int = -1): MatchResult {
        val homeTeamObj = teamsRepository.getTeamById(fixture.homeTeamId)!!
        val awayTeamObj = teamsRepository.getTeamById(fixture.awayTeamId)!!
        val homeDNA = clubDNARepository.getClubDNA(homeTeamObj.id)
        val awayDNA = clubDNARepository.getClubDNA(awayTeamObj.id)

        val homeManager = managersRepository.getManagerByTeamId(homeTeamObj.id)
        val awayManager = managersRepository.getManagerByTeamId(awayTeamObj.id)

        val homePlayers = playersRepository.getPlayersByTeamIdSync(homeTeamObj.id)
        val awayPlayers = playersRepository.getPlayersByTeamIdSync(awayTeamObj.id)

        val homeTactics = tacticsRepository.getTacticsByTeam(homeTeamObj.id) ?: TacticsEntity(
            teamId = homeTeamObj.id,
            teamName = homeTeamObj.name,
            formation = homeTeamObj.formation ?: "4-4-2",
            tacticalArchetype = TacticalMatchupEngine.mapStyleToArchetype(homeManager?.style, homeTeamObj.formation),
            playstyle = homeDNA?.playStyle ?: "Balanced"
        )
        val awayTactics = tacticsRepository.getTacticsByTeam(awayTeamObj.id) ?: TacticsEntity(
            teamId = awayTeamObj.id,
            teamName = awayTeamObj.name,
            formation = awayTeamObj.formation ?: "4-4-2",
            tacticalArchetype = TacticalMatchupEngine.mapStyleToArchetype(awayManager?.style, awayTeamObj.formation),
            playstyle = awayDNA?.playStyle ?: "Balanced"
        )

        val homeSim = prepareSimTeam(homeTeamObj, homeTactics, homeManager, homePlayers, homeDNA, userTeamId)
        val awaySim = prepareSimTeam(awayTeamObj, awayTactics, awayManager, awayPlayers, awayDNA, userTeamId)

        // Leverage TacticalMatchupEngine for probabilities (Python Logic)
        val probs = TacticalMatchupEngine.calculateDetailedBreakdown(
            homeSim.tactics, awaySim.tactics,
            homeTeamObj.eloRating, calculateTeamOverallAbility(homeSim.currentXi),
            awayTeamObj.eloRating, calculateTeamOverallAbility(awaySim.currentXi),
            homeSim.currentXi
        )

        val rand = Random.nextDouble()
        val result = when {
            rand < probs.winProb -> "HOME_WIN"
            rand < probs.winProb + probs.drawProb -> "DRAW"
            else -> "AWAY_WIN"
        }

        // Score Simulation (Python Logic)
        val baseHomeScore = Random.nextInt(0, 3)
        val baseAwayScore = Random.nextInt(0, 3)
        val strengthDiff = homeSim.strength - awaySim.strength
        val scoreAdjustment = (strengthDiff / 15.0).toInt()

        var homeScore = baseHomeScore + scoreAdjustment.coerceAtLeast(0)
        var awayScore = baseAwayScore + (-scoreAdjustment).coerceAtLeast(0)

        when (result) {
            "HOME_WIN" -> {
                if (homeScore <= awayScore) homeScore = awayScore + 1
            }
            "AWAY_WIN" -> {
                if (awayScore <= homeScore) awayScore = homeScore + 1
            }
            "DRAW" -> {
                val drawScore = Random.nextInt(0, 3)
                homeScore = drawScore
                awayScore = drawScore
            }
        }

        homeSim.score = homeScore
        awaySim.score = awayScore

        // Rapidly generate some events for the result screen
        val totalMinutes = 90 + Random.nextInt(2, 6)
        val params = AfricanFootballDataHelper.getSimulationParams(fixture.league ?: fixture.cupName)
        val weather = calculateWeatherImpact(fixture.weatherConditions)

        // For instant result, we still want a few events to show
        for (m in 1..totalMinutes) {
            if (Random.nextDouble() < 0.1) { // 10% chance of a descriptive event per minute
                 simulateMinuteWithCallback(m, homeSim, awaySim, fixture, params, weather) { _ -> }
            }
        }

        updatePlayerRatings(homeSim, awaySim)
        val allStats = (homeSim.playerStatsMap + awaySim.playerStatsMap).mapValues { entry ->
            val rating = homeSim.matchRatingMap[entry.key] ?: awaySim.matchRatingMap[entry.key] ?: 6.0
            entry.value.copy(rating = rating)
        }
        val motmId = allStats.maxByOrNull { it.value.rating + (it.value.goals * 2.5) }?.key

        return MatchResult(
            fixture.copy(homeScore = homeScore, awayScore = awayScore, matchStatus = "COMPLETED"),
            homeScore, awayScore, result, homeSim.events + awaySim.events,
            homeTeamObj, awayTeamObj, allStats, motmId, homeManager, awayManager,
            TeamMatchStats(50, homeSim.shots, homeSim.shotsOnTarget, homeSim.corners, homeSim.fouls, homeSim.yellowCards, homeSim.redCards, homeSim.offsides, homeSim.saves),
            TeamMatchStats(50, awaySim.shots, awaySim.shotsOnTarget, awaySim.corners, awaySim.fouls, awaySim.yellowCards, awaySim.redCards, awaySim.offsides, awaySim.saves)
        )
    }

    private fun processManagerAI(min: Int, team: SimTeam, opponent: SimTeam, fixtureId: Int) {
        // Granular AI logic for mentality shifts
        val dna = team.tactics.tacticalArchetype
        val scoreDiff = team.score - opponent.score
        
        if (min > 15) {
            when {
                scoreDiff <= -2 -> team.currentMentality = "VERY_ATTACKING"
                scoreDiff == -1 && min > 60 -> team.currentMentality = "ATTACKING"
                scoreDiff >= 2 && min > 75 -> team.currentMentality = "VERY_DEFENSIVE"
                scoreDiff == 1 && min > 80 -> team.currentMentality = "DEFENSIVE"
                else -> {
                    // Revert to DNA-preferred mentality if not in extreme situation
                    if (min < 80) {
                        team.currentMentality = when (dna) {
                            "DEFENSIVE", "COUNTER" -> "DEFENSIVE"
                            "POSSESSION" -> "ATTACKING"
                            else -> "BALANCED"
                        }
                    }
                }
            }
        }

        if (min > 55 && team.substitutionsUsed < 5 && Random.nextDouble() < 0.05) {
            performSub(min, team, fixtureId)
        }
    }

    fun simulateMinuteWithCallback(
        minute: Int, home: SimTeam, away: SimTeam, fixture: FixturesEntity,
        params: AfricanFootballDataHelper.SimulationParams, weather: WeatherImpact,
        callback: (MatchEventsEntity?) -> Unit
    ) {
        simulatePassing(home, away, weather)
        val period = if (minute <= 45) MatchPeriod.FIRST_HALF.value else MatchPeriod.SECOND_HALF.value
        var eventOccurred: MatchEventsEntity? = null

        // Crowd & Pitch Impact
        val isDerby = fixture.matchType == "DERBY"
        val crowdPressure = if (isDerby) 1.5 else (home.team.reputation / 100.0 + 0.5)
        val pitchQuality = AfricanFootballDataHelper.getLeagueMetadata(fixture.league ?: "")?.level ?: 1
        val technicalErrorMod = (pitchQuality * 0.1) + weather.technicalErrorMod

        // DNA Weights
        val homeStyle = home.tactics.tacticalArchetype
        val awayStyle = away.tactics.tacticalArchetype

        // 1. Crowd/Pressure Event (1 in 200 chance)
        if (Random.nextDouble() < 0.005 * crowdPressure) {
            val awayPlayer = away.currentXi.randomOrNull()
            if (awayPlayer != null && Random.nextDouble() < technicalErrorMod - 1.0) {
                 eventOccurred = MatchEventsEntity(
                    matchId = fixture.id, minute = minute, eventType = EventType.FOUL.value,
                    playerName = awayPlayer.name, playerId = awayPlayer.id, teamId = away.team.id, teamName = away.team.name,
                    opponentTeamId = home.team.id, opponentTeam = home.team.name,
                    period = period,
                    description = "${awayPlayer.name} is rattled by the hostile crowd! He commits a clumsy foul.",
                    x = Random.nextFloat() * 40, y = Random.nextFloat() * 100
                )
                away.fouls++
                callback(eventOccurred); return
            }
        }

        // 2. Random Events (Injury, Offside)
        if (Random.nextDouble() < 0.005) {
            val team = if (Random.nextBoolean()) home else away
            val player = team.currentXi.randomOrNull()
            if (player != null) {
                eventOccurred = MatchEventsEntity(
                    matchId = fixture.id, minute = minute, eventType = EventType.INJURY.value,
                    playerName = player.name, playerId = player.id, teamId = team.team.id, teamName = team.team.name,
                    opponentTeamId = if (team == home) away.team.id else home.team.id,
                    opponentTeam = if (team == home) away.team.name else home.team.name,
                    injuryType = listOf(InjurySeverity.MINOR.value, InjurySeverity.MODERATE.value, InjurySeverity.SEVERE.value).random(),
                    description = CommentaryGenerator.getInjury(player.name),
                    injuryMinutes = Random.nextInt(5, 15), period = period,
                    x = Random.nextFloat() * 100, y = Random.nextFloat() * 100
                )
                team.events.add(eventOccurred)
            }
        } else if (Random.nextDouble() < 0.03) {
            val team = if (Random.nextBoolean()) home else away
            val player = team.currentXi.randomOrNull()
            if (player != null) {
                team.offsides++
                eventOccurred = MatchEventsEntity(
                    matchId = fixture.id, minute = minute, eventType = EventType.OFFSIDE.value,
                    playerName = player.name, playerId = player.id, teamId = team.team.id, teamName = team.team.name,
                    opponentTeamId = if (team == home) away.team.id else home.team.id,
                    opponentTeam = if (team == home) away.team.name else home.team.name,
                    period = period,
                    description = CommentaryGenerator.getOffside(player.name),
                    x = 80.0f + Random.nextFloat() * 20, y = Random.nextFloat() * 100
                )
                team.events.add(eventOccurred)
            }
        }

        if (eventOccurred != null) { callback(eventOccurred); return }

        // 2. Tactical/Defensive Actions (Tackles, Interceptions, Blocks)
        // DNA & REGION Impact
        val homeDNA = home.dna
        val awayDNA = away.dna
        
        val homeGegenPress = homeDNA?.playStyle == "GEGENPRESS" || homeDNA?.playStyleSecondary == "GEGENPRESS"
        val awayGegenPress = awayDNA?.playStyle == "GEGENPRESS" || awayDNA?.playStyleSecondary == "GEGENPRESS"
        
        val homePhysical = homeDNA?.playStyle == "DIRECT_PHYSICAL" || homeDNA?.playStyleSecondary == "DIRECT_PHYSICAL"
        val awayPhysical = awayDNA?.playStyle == "DIRECT_PHYSICAL" || awayDNA?.playStyleSecondary == "DIRECT_PHYSICAL"
        
        val defensiveActionProb = 0.25 * if (homeGegenPress || awayGegenPress) 1.3 else 1.0
        if (Random.nextDouble() < defensiveActionProb) {
            val defender = if (Random.nextDouble() < (home.possession / 100.0)) away else home
            val attacker = if (defender == home) away else home
            val player = defender.currentXi.randomOrNull()
            
            if (player != null) {
                val dna = defender.dna
                val style = dna?.playStyle ?: "BALANCED"
                val region = dna?.region
                val actionRoll = Random.nextDouble()
                
                // Region Modifier: East Africa more physical/tackles, West Africa more interceptions (anticipation)
                var tackleWeight = if (region == "EAST_AFRICA") 0.45 else 0.35
                if (defender == home && homePhysical) tackleWeight += 0.15
                if (defender == away && awayPhysical) tackleWeight += 0.15
                
                val eventType = when {
                    actionRoll < tackleWeight -> EventType.TACKLE.value
                    actionRoll < 0.7 -> EventType.INTERCEPTION.value
                    else -> EventType.BLOCK.value
                }
                
                when (eventType) {
                    EventType.TACKLE.value -> defender.playerStatsMap[player.id]?.tackles = (defender.playerStatsMap[player.id]?.tackles ?: 0) + 1
                    EventType.INTERCEPTION.value -> defender.playerStatsMap[player.id]?.interceptions = (defender.playerStatsMap[player.id]?.interceptions ?: 0) + 1
                    EventType.BLOCK.value -> defender.playerStatsMap[player.id]?.blocks = (defender.playerStatsMap[player.id]?.blocks ?: 0) + 1
                }

                // Increased callback frequency for significant defensive actions, especially for GEGENPRESS
                if (Random.nextDouble() < (if (style == "GEGENPRESS") 0.25 else 0.15)) {
                    val isDeep = Random.nextDouble() < 0.7
                    eventOccurred = MatchEventsEntity(
                        matchId = fixture.id, minute = minute, eventType = eventType,
                        playerName = player.name, playerId = player.id, teamId = defender.team.id, teamName = defender.team.name,
                        opponentTeamId = attacker.team.id, opponentTeam = attacker.team.name,
                        period = period,
                        description = when(eventType) {
                            EventType.TACKLE.value -> CommentaryGenerator.getTackle(player.name, style, region)
                            EventType.INTERCEPTION.value -> CommentaryGenerator.getInterception(player.name, region)
                            else -> CommentaryGenerator.getBlock(player.name)
                        },
                        x = if (isDeep) 5.0f + Random.nextFloat() * 25 else 30.0f + Random.nextFloat() * 40, 
                        y = 10.0f + Random.nextFloat() * 80
                    )
                }
            }
        }

        if (eventOccurred != null) { callback(eventOccurred); return }

        // 2b. Offensive Skills (Dribbles, Crosses)
        // DNA & REGION Impact
        val flairModifier = if (home.dna?.region == "WEST_AFRICA" || away.dna?.region == "WEST_AFRICA") 1.25 else 1.0
        if (Random.nextDouble() < 0.22 * flairModifier) {
            val attacker = if (Random.nextDouble() < (home.possession / 100.0)) home else away
            val defender = if (attacker == home) away else home
            val player = attacker.currentXi.randomOrNull()

            if (player != null) {
                val dna = attacker.dna
                val region = dna?.region
                val roll = Random.nextDouble()
                
                // DNA/Region logic
                val crossWeight = if (dna?.playStyle == "WING_PLAY" || region == "EAST_AFRICA") 0.7 else 0.45

                val eventType = if (roll < crossWeight) EventType.CROSS.value else EventType.DRIBBLE.value

                if (eventType == EventType.DRIBBLE.value) {
                    attacker.playerStatsMap[player.id]?.dribbles = (attacker.playerStatsMap[player.id]?.dribbles ?: 0) + 1
                } else {
                    attacker.playerStatsMap[player.id]?.crosses = (attacker.playerStatsMap[player.id]?.crosses ?: 0) + 1
                }

                if (Random.nextDouble() < 0.12) {
                    val xPos = 60.0f + Random.nextFloat() * 35
                    val yPos = if (eventType == EventType.CROSS.value) {
                         if (Random.nextBoolean()) 2.0f + Random.nextFloat() * 10 else 88.0f + Random.nextFloat() * 10
                    } else {
                        15.0f + Random.nextFloat() * 70
                    }

                    eventOccurred = MatchEventsEntity(
                        matchId = fixture.id, minute = minute, eventType = eventType,
                        playerName = player.name, playerId = player.id, teamId = attacker.team.id, teamName = attacker.team.name,
                        opponentTeamId = defender.team.id, opponentTeam = defender.team.name,
                        period = period,
                        description = if (eventType == EventType.DRIBBLE.value) {
                            CommentaryGenerator.getDribble(player.name, dna?.playStyle, region)
                        } else {
                            CommentaryGenerator.getCross(player.name, dna?.playStyle, region)
                        },
                        x = xPos, y = yPos
                    )
                }
            }
        }

        if (eventOccurred != null) { callback(eventOccurred); return }

        // 3. Attack Logic
        if (Random.nextDouble() < 0.18 * params.goalFrequency) {
            val attacker = if (Random.nextDouble() < (home.possession / 100.0)) home else away
            val defender = if (attacker == home) away else home

            // DEFENSIVE (Parking the Bus) Impact: Lower xG conceded
            val defenseModifier = if (defender.tactics.tacticalArchetype == "DEFENSIVE") 0.65 else 1.0

            // Check for Penalty or Free kick first
            val isSetPiece = Random.nextDouble() < 0.15
            if (isSetPiece) {
                val isPenalty = Random.nextDouble() < 0.3
                if (isPenalty) {
                    val taker = attacker.currentXi.find { it.id == attacker.penaltyTakerId } ?: attacker.currentXi.random()
                    attacker.xG += 0.76 * defenseModifier
                    val scored = Random.nextDouble() < 0.76 * defenseModifier
                    if (scored) attacker.score++ else attacker.bigChancesMissed++
                    eventOccurred = MatchEventsEntity(
                        matchId = fixture.id, minute = minute,
                        eventType = if (scored) EventType.PENALTY_SCORED.value else EventType.PENALTY_MISSED.value,
                        playerName = taker.name, playerId = taker.id, teamId = attacker.team.id, teamName = attacker.team.name,
                        opponentTeamId = defender.team.id, opponentTeam = defender.team.name, homeScore = home.score, awayScore = away.score,
                        period = period, penaltySaved = !scored && Random.nextBoolean(),
                        description = CommentaryGenerator.getPenalty(taker.name, scored),
                        expectedGoals = 0.76, x = 88.5f, y = 50.0f
                    )
                } else {
                    val taker = attacker.currentXi.find { it.id == attacker.freeKickTakerId } ?: attacker.currentXi.random()
                    val chanceXG = 0.08 * defenseModifier
                    attacker.xG += chanceXG
                    val scored = Random.nextDouble() < chanceXG
                    if (scored) attacker.score++
                    eventOccurred = MatchEventsEntity(
                        matchId = fixture.id, minute = minute,
                        eventType = if (scored) EventType.GOAL.value else EventType.SHOT_ON_TARGET.value,
                        playerName = taker.name, playerId = taker.id, teamId = attacker.team.id, teamName = attacker.team.name,
                        opponentTeamId = defender.team.id, opponentTeam = defender.team.name, homeScore = home.score, awayScore = away.score,
                        period = period, shotType = ShotType.FREE_KICK.value, shotDistance = Random.nextInt(20, 30),
                        description = CommentaryGenerator.getFreeKick(taker.name, scored),
                        expectedGoals = chanceXG, x = 75.0f + Random.nextFloat() * 10, y = 30.0f + Random.nextFloat() * 40
                    )
                }
                attacker.events.add(eventOccurred)
                callback(eventOccurred); return
            }

            // Normal Play
            attacker.shots++
            
            // DNA Outcome Weights: Direct Physical increases "Heading" success or big chance creation from crosses
            val attackerDNA = attacker.dna
            val isDirectPhysical = attackerDNA?.playStyle == "DIRECT_PHYSICAL" || attackerDNA?.playStyleSecondary == "DIRECT_PHYSICAL"
            
            var chanceXG = Random.nextDouble(0.02, 0.4) * defenseModifier
            
            // Boost xG for Direct Physical if it's a "Aerial" type situation (simulated)
            if (isDirectPhysical && Random.nextDouble() < 0.3) {
                chanceXG *= 1.2 // Physicality payoff in the box
            }

            attacker.xG += chanceXG
            
            if (Random.nextDouble() < 0.4) {
                attacker.shotsOnTarget++
                if (Random.nextDouble() < (chanceXG * 2.5).coerceAtMost(0.9)) {
                    attacker.score++
                    val scorer = selectScorer(attacker)
                    val assister = selectAssister(attacker, scorer.id)
                    if (assister != null) {
                    attacker.keyPasses++
                    attacker.playerStatsMap[assister.id]?.keyPasses = (attacker.playerStatsMap[assister.id]?.keyPasses ?: 0) + 1
                }
                    val shotType = ShotType.entries.filter { it != ShotType.FREE_KICK && it != ShotType.PENALTY }.random()

                    eventOccurred = MatchEventsEntity(
                        matchId = fixture.id, minute = minute, eventType = EventType.GOAL.value,
                        playerName = scorer.name, playerId = scorer.id, teamId = attacker.team.id, teamName = attacker.team.name,
                        opponentTeamId = defender.team.id, opponentTeam = defender.team.name, homeScore = home.score, awayScore = away.score,
                        assistPlayerName = assister?.name, assistPlayerId = assister?.id,
                        shotType = shotType.value, period = period,
                        description = CommentaryGenerator.getGoal(scorer.name, attacker.team.name, attacker.dna),
                        expectedGoals = chanceXG, x = 85.0f + Random.nextFloat() * 10, y = 25.0f + Random.nextFloat() * 50
                    )
                } else {
                    defender.saves++
                    if (chanceXG > 0.3) attacker.bigChancesMissed++
                    val keeper = defender.currentXi.find { it.position == "GK" } ?: defender.currentXi.random()
                    eventOccurred = MatchEventsEntity(
                        matchId = fixture.id, minute = minute, eventType = EventType.SAVE.value,
                        playerName = keeper.name, playerId = keeper.id, teamId = defender.team.id, teamName = defender.team.name,
                        opponentTeamId = attacker.team.id, opponentTeam = attacker.team.name,
                        period = period,
                        description = CommentaryGenerator.getSave(keeper.name),
                        x = 95.0f, y = 50.0f
                    )
                }
                attacker.events.add(eventOccurred)
            } else if (Random.nextDouble() < 0.2) {
                attacker.corners++
                val taker = attacker.currentXi.find { it.id == attacker.cornerTakerId } ?: attacker.currentXi.random()
                eventOccurred = MatchEventsEntity(
                    matchId = fixture.id, minute = minute, eventType = EventType.CORNER.value,
                    playerName = taker.name, playerId = taker.id, teamId = attacker.team.id, teamName = attacker.team.name,
                    opponentTeamId = defender.team.id, opponentTeam = defender.team.name,
                    period = period,
                    description = CommentaryGenerator.getCorner(taker.name),
                    x = 99.0f, y = if (Random.nextBoolean()) 0.0f else 100.0f
                )
                attacker.events.add(eventOccurred)
            } else {
                // Shot Off Target
                val player = selectScorer(attacker)
                eventOccurred = MatchEventsEntity(
                    matchId = fixture.id, minute = minute, eventType = EventType.SHOT_OFF_TARGET.value,
                    playerName = player.name, playerId = player.id, teamId = attacker.team.id, teamName = attacker.team.name,
                    opponentTeamId = defender.team.id, opponentTeam = defender.team.name,
                    period = period,
                    description = CommentaryGenerator.getShotOffTarget(player.name),
                    x = 90.0f + Random.nextFloat() * 10, y = 10.0f + Random.nextFloat() * 80
                )
                attacker.events.add(eventOccurred)
            }
        } else if (Random.nextDouble() < (if (homeStyle == "GEGENPRESS" || awayStyle == "GEGENPRESS") 0.12 else 0.08)) {
            // Foul - GEGENPRESS teams commit more fouls
            val fTeam = if (Random.nextBoolean()) home else away
            val fouler = fTeam.currentXi.randomOrNull()
            if (fouler != null) {
                fTeam.fouls++
                val cardProb = Random.nextDouble()
                val eventType = when {
                    cardProb < 0.02 -> EventType.RED_CARD.value
                    cardProb < 0.15 -> EventType.YELLOW_CARD.value
                    else -> EventType.FOUL.value
                }
                if (eventType == EventType.RED_CARD.value) fTeam.redCards++
                else if (eventType == EventType.YELLOW_CARD.value) fTeam.yellowCards++

                val oTeam = if (fTeam == home) away else home
                eventOccurred = MatchEventsEntity(
                    matchId = fixture.id, minute = minute, eventType = eventType,
                    playerName = fouler.name, playerId = fouler.id, teamId = fTeam.team.id, teamName = fTeam.team.name,
                    opponentTeamId = oTeam.team.id, opponentTeam = oTeam.team.name,
                    period = period,
                    description = when (eventType) {
                        EventType.YELLOW_CARD.value -> CommentaryGenerator.getCard(fouler.name, "YELLOW")
                        EventType.RED_CARD.value -> CommentaryGenerator.getCard(fouler.name, "RED")
                        else -> CommentaryGenerator.getFoul(fouler.name)
                    }
                )
                fTeam.events.add(eventOccurred)
            }
        }

        callback(eventOccurred)
    }

    private fun simulatePassing(h: SimTeam, a: SimTeam, w: WeatherImpact) {
        val basePasses = Random.nextInt(8, 15)
        
        fun record(t: SimTeam, count: Int) {
            val dna = t.tactics.tacticalArchetype
            val accuracyBase = when(dna) {
                "POSSESSION" -> 0.82
                "GEGENPRESS" -> 0.70
                "COUNTER" -> 0.65
                "DEFENSIVE" -> 0.60
                else -> 0.75
            } * w.passAccuracyMod

            repeat(count) {
                t.passesAttempted++
                val p = t.currentXi.randomOrNull() ?: return@repeat
                
                // Technical Error based on weather and potential pitch issues
                val errorChance = (1.0 - accuracyBase) * w.technicalErrorMod
                
                if (Random.nextDouble() > errorChance) {
                    t.passesCompleted++
                    t.playerStatsMap[p.id]?.passesCompleted = (t.playerStatsMap[p.id]?.passesCompleted ?: 0) + 1
                }
            }
        }
        
        val hCount = (basePasses * h.possession / 100).toInt()
        val aCount = (basePasses * a.possession / 100).toInt()
        
        record(h, hCount)
        record(a, aCount)
    }

    fun prepareSimTeam(
        t: TeamsEntity,
        tac: TacticsEntity,
        m: ManagersEntity?,
        p: List<PlayersEntity>,
        dna: ClubDNAEntity?,
        userTeamId: Int
    ): SimTeam {
        val xi = p.asSequence().filter { it.isStartingXi }.toMutableList()

        // CRITICAL FIX: Ensure AI teams always have 11 players based on their formation
        if (xi.size < 11 && t.id != userTeamId) {
            val autoXiIds = LineupUtils.autoSelectLineup(p, tac.formation)
            xi.clear()
            xi.addAll(p.filter { autoXiIds.contains(it.id) })
        }

        // Last resort fallback
        if (xi.size < 11) {
            xi.clear()
            xi.addAll(p.sortedByDescending { it.rating }.take(11))
        }

        val subs = p.filter { !xi.contains(it) }.toMutableList()

        // Tactical Archetype Mapping (Python Logic)
        val mappedArchetype = TacticalMatchupEngine.mapStyleToArchetype(m?.style, tac.formation)
        val finalTactics = if (tac.tacticalArchetype == "BALANCED" || tac.tacticalArchetype.isEmpty()) {
            tac.copy(tacticalArchetype = mappedArchetype)
        } else tac

        val sim = SimTeam(t, finalTactics, m, xi, subs, dna = dna)
        xi.forEach { 
            sim.matchRatingMap[it.id] = 6.0 
            sim.playerStatsMap[it.id] = PlayerMatchStats(it.id) 
        }
        
        // Explicitly map roles from TacticsEntity
        sim.captainId = tac.captainId ?: xi.find { it.isCaptain }?.id ?: xi.maxByOrNull { it.leadership }?.id
        sim.viceCaptainId = tac.viceCaptainId ?: xi.find { it.isViceCaptain }?.id ?: xi.sortedByDescending { it.leadership }.getOrNull(1)?.id
        sim.penaltyTakerId = tac.penaltyTakerId ?: xi.maxByOrNull { it.finishing }?.id
        sim.freeKickTakerId = tac.freeKickTakerId ?: xi.maxByOrNull { it.skill }?.id
        sim.cornerTakerId = tac.cornerTakerId ?: xi.maxByOrNull { it.crossing }?.id
        
        // Team Strength Calculation (Python Logic)
        val teamOverallAbility = calculateTeamOverallAbility(xi)
        val eloWeight = 0.6
        val playerAbilityWeight = 0.4
        sim.strength = ((t.eloRating / 2000.0 * eloWeight) + (teamOverallAbility / 100.0 * playerAbilityWeight)) * 100.0

        return sim
    }

    private fun calculatePlayerOverallAbility(p: PlayersEntity): Double {
        val attacking = listOf(p.finishing, p.dribbling, p.skill, p.longShots, p.pace, p.acceleration).map { it.toDouble() }.average()
        val defensive = listOf(p.defending, p.aggression, p.positioning, p.strength, p.anticipation).map { it.toDouble() }.average()
        val midfield = listOf(p.passing, p.vision, p.creativity, p.dribbling, p.teamwork).map { it.toDouble() }.average()

        return listOf(attacking, defensive, midfield, p.finishing.toDouble(), p.passing.toDouble(), p.stamina.toDouble()).average()
    }

    private fun calculateTeamOverallAbility(players: List<PlayersEntity>): Double {
        if (players.isEmpty()) return 50.0
        return players.map { calculatePlayerOverallAbility(it) }.average()
    }

    private fun selectScorer(t: SimTeam) = t.currentXi.filter { it.position != "GK" }.random()
    private fun selectAssister(t: SimTeam, sid: Int) = t.currentXi.filter { it.id != sid && it.position != "GK" }.randomOrNull()
    private fun getResultType(h: Int, a: Int) = when { h > a -> "HOME_WIN"; a > h -> "AWAY_WIN"; else -> "DRAW" }
    fun calculateWeatherImpact(condition: String?): WeatherImpact {
        return when (condition?.uppercase()?.replace(" ", "_")?.replace("(", "")?.replace(")", "")) {
            "SCORCHING_HEAT", "VERY_HOT" -> WeatherImpact(passAccuracyMod = 0.92, staminaMod = 0.70, physicalityMod = 0.90, technicalErrorMod = 1.1)
            "HEAVY_RAIN", "TROPICAL_RAIN", "RAINY", "THUNDERSTORM" -> WeatherImpact(passAccuracyMod = 0.85, staminaMod = 0.90, physicalityMod = 1.20, technicalErrorMod = 1.25)
            "DUSTY_HARMATTAN", "DUSTY" -> WeatherImpact(passAccuracyMod = 0.82, staminaMod = 0.85, physicalityMod = 1.0, technicalErrorMod = 1.15)
            "ALTITUDE" -> WeatherImpact(passAccuracyMod = 0.95, staminaMod = 0.65, physicalityMod = 0.85, technicalErrorMod = 1.05)
            "HUMID" -> WeatherImpact(passAccuracyMod = 0.94, staminaMod = 0.75, physicalityMod = 1.0, technicalErrorMod = 1.05)
            "CLEAR", "SUNNY" -> WeatherImpact(1.0, 1.0, 1.0, 1.0)
            "COOL", "CLOUDY" -> WeatherImpact(1.05, 1.05, 1.0, 0.95)
            "WINDY" -> WeatherImpact(0.88, 1.0, 1.0, 1.1)
            "FOGGY", "HAZY" -> WeatherImpact(0.82, 1.0, 1.0, 1.1)
            else -> WeatherImpact(1.0, 1.0, 1.0, 1.0)
        }
    }

    private fun updatePlayerRatings(h: SimTeam, a: SimTeam) {
        val teams = listOf(h, a)
        teams.forEach { team ->
            team.currentXi.forEach { player ->
                val stats = team.playerStatsMap[player.id] ?: return@forEach
                var rating = 6.0 // Base rating

                // Offensive Contributions
                rating += stats.goals * 1.5
                rating += stats.assists * 1.0
                rating += stats.shotsOnTarget * 0.2
                rating += stats.keyPasses * 0.3
                rating += stats.dribbles * 0.1

                // Defensive Contributions
                rating += stats.tackles * 0.25
                rating += stats.interceptions * 0.2
                rating += stats.blocks * 0.15
                
                // Passing Quality
                if (stats.passesAttempted > 0) {
                    val accuracy = stats.passesCompleted.toDouble() / stats.passesAttempted
                    rating += (accuracy - 0.7) * 2.0
                }

                // Negative Impacts
                if (team.score < (if (team == h) a.score else h.score) && player.position == "GK") {
                    rating -= 0.5 // Penalty for GK in losing team
                }
                
                // Cards
                // (Cards are not in PlayerMatchStats yet, need to add them or check events)
                val playerEvents = team.events.filter { it.playerId == player.id }
                playerEvents.forEach { e ->
                    when (e.eventType) {
                        EventType.YELLOW_CARD.value -> rating -= 0.5
                        EventType.RED_CARD.value -> rating -= 2.5
                        EventType.PENALTY_MISSED.value -> rating -= 1.0
                    }
                }

                stats.rating = rating.coerceIn(3.0, 10.0)
                team.matchRatingMap[player.id] = stats.rating
            }
        }
    }

    private fun calculateFormBonus(formStreak: String): Double {
        if (formStreak.isEmpty()) return 0.0
        val lastFive = formStreak.takeLast(5)
        var bonus = 0.0
        lastFive.forEach { char ->
            bonus += when (char) {
                'W' -> 0.02
                'D' -> 0.005
                'L' -> -0.015
                else -> 0.0
            }
        }
        return bonus
    }

    private fun updateInGameMomentum(h: SimTeam, a: SimTeam, event: MatchEventsEntity?) {
        // Natural decay of momentum over time
        h.momentum *= 0.98
        a.momentum *= 0.98

        event?.let {
            when (it.eventType) {
                EventType.GOAL.value -> {
                    if (it.teamId == h.team.id) { h.momentum += 15.0; a.momentum -= 10.0 }
                    else { a.momentum += 15.0; h.momentum -= 10.0 }
                }
                EventType.SHOT_ON_TARGET.value -> {
                    if (it.teamId == h.team.id) h.momentum += 3.0
                    else a.momentum += 3.0
                }
                EventType.YELLOW_CARD.value -> {
                    if (it.teamId == h.team.id) h.momentum -= 5.0
                    else a.momentum -= 5.0
                }
                EventType.RED_CARD.value -> {
                    if (it.teamId == h.team.id) h.momentum -= 20.0
                    else a.momentum -= 20.0
                }
            }
        }
        
        // Dynamic possession shift based on momentum
        val momentumDiff = (h.momentum - a.momentum) / 10.0
        h.possession = (h.possession + momentumDiff).coerceIn(20.0, 80.0)
        a.possession = 100.0 - h.possession
    }
    
    fun setupMatchDynamics(h: SimTeam, a: SimTeam, p: AfricanFootballDataHelper.SimulationParams) {
        val hDNA = h.dna
        val aDNA = a.dna
        
        var hBase = 50.0
        
        // Use Hybrid DNA (Primary + Secondary)
        fun getStylePossessionMod(dna: ClubDNAEntity?): Double {
            if (dna == null) return 0.0
            var mod = 0.0
            
            val styles = listOf(dna.playStyle, dna.playStyleSecondary)
            styles.forEachIndexed { index, s ->
                val weight = if (index == 0) dna.primaryWeight else dna.secondaryWeight
                mod += when (s) {
                    "POSSESSION" -> 10.0
                    "COUNTER" -> -7.0
                    "DEFENSIVE" -> -12.0
                    "GEGENPRESS" -> 2.0
                    "DIRECT_PHYSICAL" -> -5.0
                    "WING_PLAY" -> 3.0
                    else -> 0.0
                } * weight
            }
            return mod
        }

        hBase += getStylePossessionMod(hDNA)
        hBase -= getStylePossessionMod(aDNA)
        
        // Regional Variance
        if (hDNA?.region == "NORTH_AFRICA") hBase += 3.0 // Better structure
        if (aDNA?.region == "NORTH_AFRICA") hBase -= 3.0
        
        // Momentum Impact
        hBase += (h.momentum - a.momentum) * 10.0
        
        // Strength difference impact
        val diff = h.strength - a.strength
        hBase += (diff / 2.0)
        
        // Momentum and Form Impact
        hBase += (h.preMatchForm - a.preMatchForm) * 50.0
        hBase += (h.tacticalFamiliarity - a.tacticalFamiliarity) * 20.0

        // Tactical Matchup Impact (Python Logic Integration)
        val probs = TacticalMatchupEngine.calculateDetailedBreakdown(
            h.tactics, a.tactics,
            h.team.eloRating, calculateTeamOverallAbility(h.currentXi),
            a.team.eloRating, calculateTeamOverallAbility(a.currentXi),
            h.currentXi
        )
        hBase += (probs.winProb - probs.lossProb) * 25.0 // Shift possession based on tactical advantage

        // Home advantage
        hBase *= p.homeAdvantage
        
        h.possession = hBase.coerceIn(25.0, 75.0)
        a.possession = 100.0 - h.possession
    }

    private fun performSub(min: Int, team: SimTeam, matchId: Int) {
        if (team.substitutionsUsed < 5 && team.substitutes.isNotEmpty()) {
            val off = team.currentXi.minByOrNull { it.stamina.toDouble() } ?: return
            val bestSub = team.substitutes.maxByOrNull { it.rating } ?: return

            team.currentXi.remove(off)
            team.currentXi.add(bestSub)
            team.substitutes.remove(bestSub)
            team.substitutionsUsed++

            team.matchRatingMap[bestSub.id] = 6.2
            team.playerStatsMap[bestSub.id] = PlayerMatchStats(bestSub.id)
            
            val event = MatchEventsEntity(
                matchId = matchId, teamId = team.team.id, teamName = team.team.name, playerId = off.id, playerName = off.name,
                substitutionInPlayer = bestSub.name, substitutionInPlayerId = bestSub.id,
                substitutionOutPlayer = off.name, substitutionOutPlayerId = off.id,
                eventType = EventType.SUBSTITUTION.value, minute = min,
                period = if (min <= 45) MatchPeriod.FIRST_HALF.value else MatchPeriod.SECOND_HALF.value,
                description = CommentaryGenerator.getSubstitution(bestSub.name, off.name)
            )
            team.events.add(event)
        }
    }

    private object CommentaryGenerator {
        fun getOffside(player: String) = listOf(
            "$player is caught in an offside position!",
            "Flag up! $player timed that run poorly.",
            "Offside! $player was a fraction too early.",
            "The linesman spots $player beyond the last defender.",
            "A wasted opportunity as $player is flagged offside."
        ).random()

        fun getTackle(player: String, style: String?, region: String?) = when {
            style == "GEGENPRESS" -> listOf(
                "$player wins it back immediately with a fierce high-press tackle!",
                "Incredible intensity from $player, winning the ball back high up!",
                "$player hunts down the opponent and forces the turnover!"
            ).random()
            region == "EAST_AFRICA" -> listOf(
                "$player uses superior physicality to win the ball cleanly.",
                "Pure strength from $player to shrug the attacker off the ball.",
                "The East African wall! $player wins the physical duel."
            ).random()
            region == "NORTH_AFRICA" -> listOf(
                "$player maintains defensive shape and steps out to tackle.",
                "Tactical masterclass! $player reads the movement and intercepts with a tackle.",
                "$player waits for the right moment and pokes the ball away."
            ).random()
            else -> listOf(
                "$player with a perfectly timed challenge!",
                "Superb tackle by $player to win the ball.",
                "$player slides in and dispossesses the attacker.",
                "Clean challenge from $player."
            ).random()
        }

        fun getInterception(player: String, region: String?) = when (region) {
            "NORTH_AFRICA" -> listOf(
                "$player reads the play perfectly in this structured defense to intercept.",
                "Tactical intelligence on display as $player cuts off the passing lane.",
                "North African discipline shows as $player intercepts with ease."
            ).random()
            "WEST_AFRICA" -> listOf(
                "$player shows great anticipation to nip in and steal the ball.",
                "Lightning quick reactions from $player to intercept!",
                "$player reads the intent and ghosts in to take possession."
            ).random()
            else -> listOf(
                "$player intercepts the pass!",
                "Great reading of the game by $player to cut that out.",
                "$player steps in to break up the play.",
                "Interception! $player wins the ball for his team."
            ).random()
        }

        fun getBlock(player: String) = listOf(
            "$player blocks the goalbound effort!",
            "Crucial block by $player!",
            "$player puts his body on the line to stop the shot.",
            "Shot blocked! $player was well-positioned there."
        ).random()

        fun getDribble(player: String, style: String?, region: String?) = when {
            region == "WEST_AFRICA" -> listOf(
                "$player dazzles with typical West African flair, skipping past his marker!",
                "Incredible footwork by $player, leaving the defender in the dust!",
                "$player dances past the challenge with ease."
            ).random()
            style == "DIRECT_PHYSICAL" -> listOf(
                "$player powers past the defender with raw strength.",
                "Too strong! $player brushes aside the defender to break through.",
                "$player uses his frame to shield the ball and advance."
            ).random()
            else -> listOf(
                "$player skips past his marker!",
                "$player shows a clean pair of heels to the defender.",
                "Lovely turn by $player to beat his man."
            ).random()
        }

        fun getCross(player: String, style: String?, region: String?) = when {
            style == "WING_PLAY" -> listOf(
                "$player hits the byline and whips in a trademark dangerous cross.",
                "Pinpoint cross from $player from the wide area!",
                "$player's delivery into the box is excellent."
            ).random()
            region == "EAST_AFRICA" -> listOf(
                "$player lofts a physical cross into the crowded box.",
                "High ball in from $player, looking for the target man.",
                "$player sends a hanging cross towards the far post."
            ).random()
            else -> listOf(
                "$player whips in a dangerous cross.",
                "Good delivery by $player into the danger zone.",
                "$player lofts one into the area."
            ).random()
        }

        fun getGoal(scorer: String, team: String, dna: ClubDNAEntity?) = when {
            dna?.playStyle == "POSSESSION" -> listOf(
                "Goal for $team! A patient build-up finished by $scorer.",
                "Tiki-taka at its best! $team pass their way through and $scorer scores.",
                "Pure dominance! $team maintain possession before $scorer provides the finishing touch."
            ).random()
            dna?.playStyle == "COUNTER" -> listOf(
                "DEVASTATING COUNTER! $team break from deep and $scorer finishes it!",
                "Lightning fast transition! $team catch them on the break, scored by $scorer.",
                "A clinical counter-attacking move! $scorer ends the move with a goal."
            ).random()
            dna?.region == "NORTH_AFRICA" -> listOf(
                "Clinical finish! $team exploit a gap in the structure, $scorer scores.",
                "Tactical precision! $team's structured attack leads to a goal for $scorer.",
                "A disciplined move ends with $scorer firing home for $team."
            ).random()
            dna?.region == "WEST_AFRICA" -> listOf(
                "Individual brilliance! $scorer scores with typical West African flair!",
                "Unstoppable! $scorer shows incredible skill to find the net for $team.",
                "The stadium erupts! $scorer's flair results in a spectacular goal for $team."
            ).random()
            else -> listOf(
                "Goal for $team! Scored by $scorer.",
                "$scorer finds the net for $team!",
                "GOAL! $scorer makes no mistake with that finish.",
                "$team take advantage! $scorer is the hero with a fine goal."
            ).random()
        }

        fun getSave(keeper: String) = listOf(
            "Great save by $keeper to deny the effort!",
            "Incredible reflexes from $keeper!",
            "$keeper tips it over the bar!",
            "World-class save by $keeper to keep his team in it.",
            "Point-blank save! $keeper is having a game to remember.",
            "The keeper stands tall! $keeper parries it away.",
            "Finger-tip save from $keeper! How did he reach that?"
        ).random()

        fun getShotOffTarget(player: String) = listOf(
            "$player's effort goes wide of the post.",
            "$player fires it high and over the bar!",
            "A wild shot from $player that doesn't trouble the keeper.",
            "$player should have done better with that chance.",
            "The shot from $player lacks precision and goes out for a goal kick.",
            "It's a wayward strike from $player.",
            "$player leans back and sends it into the stands."
        ).random()

        fun getCorner(taker: String) = listOf(
            "$taker steps up to take the corner kick.",
            "Corner awarded. $taker will deliver it.",
            "$taker places the ball for the corner.",
            "$taker swings it in from the corner flag."
        ).random()

        fun getFoul(player: String) = listOf(
            "Foul by $player.",
            "$player is penalised for a late challenge.",
            "The referee blows for a foul by $player.",
            "Nervy challenge from $player, it's a foul."
        ).random()

        fun getCard(player: String, type: String) = when(type) {
            "YELLOW" -> listOf(
                "$player is shown a yellow card.",
                "The referee reaches for his pocket... it's a yellow for $player.",
                "Yellow card for $player after that challenge."
            ).random()
            "RED" -> listOf(
                "RED CARD! $player is sent off!",
                "$player is given his marching orders!",
                "Disaster for the team! $player sees red!"
            ).random()
            else -> "$player is penalized."
        }

        fun getSubstitution(playerIn: String, playerOut: String) = listOf(
            "CHANGE: $playerIn replaces $playerOut",
            "Substitution for the team: $playerIn comes on for $playerOut",
            "Tactical switch: $playerOut makes way for $playerIn",
            "Fresh legs: $playerIn enters the pitch, replacing $playerOut"
        ).random()

        fun getPenalty(player: String, scored: Boolean) = if (scored) {
            listOf(
                "Penalty converted by $player!",
                "$player keeps his cool and slots the penalty home.",
                "GOAL! $player strikes it well from the spot.",
                "No mistake from $player! Penalty scored."
            ).random()
        } else {
            listOf(
                "$player misses the penalty!",
                "Saved! The keeper guesses right and denies $player from the spot.",
                "$player blazes the penalty over the bar!",
                "Heartbreak as $player fails to convert the penalty."
            ).random()
        }

        fun getFreeKick(player: String, scored: Boolean) = if (scored) {
            listOf(
                "STUNNING FREE KICK! $player scores!",
                "Over the wall and in! Brilliant free kick by $player.",
                "$player finds the top corner from the set piece!",
                "Exquisite technique from $player to score from the free kick."
            ).random()
        } else {
            listOf(
                "$player tests the keeper from a set piece.",
                "$player's free kick hits the wall.",
                "Wide of the target! $player's free kick didn't trouble the keeper.",
                "The free kick from $player is easily gathered by the keeper."
            ).random()
        }

        fun getInjury(player: String) = listOf(
            "$player goes down clutching his leg.",
            "Injury concern here! $player is unable to continue.",
            "$player is receiving treatment on the sidelines.",
            "The physio is out for $player. It looks like he's picked up a knock."
        ).random()
    }

    data class WeatherImpact(
        val passAccuracyMod: Double, 
        val staminaMod: Double, 
        val physicalityMod: Double,
        val technicalErrorMod: Double = 1.0 // Modifies chance of bad touches/passes
    )


}
