package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.utils.GameDateManager
import com.fameafrica.afm.utils.tactics.TacticalMatchupEngine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Singleton
class LeagueSimulator @Inject constructor(
    private val fixturesRepository: FixturesRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val matchSimulationEngine: MatchSimulationEngine,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val gameDateManager: GameDateManager,
    private val leaguesRepository: LeaguesRepository,
    private val tacticsRepository: TacticsRepository,
    private val managersRepository: ManagersRepository,
) {

    data class WeeklyLeagueReport(
        val results: List<FixturesResultsEntity>,
        val biggestWin: FixturesResultsEntity?,
        val shockResult: FixturesResultsEntity?,
        val playerOfTheWeekId: Int?,
        val teamOfTheWeekIds: List<Int>,
        val standingsChanges: Map<Int, Int>, // teamId to position change
    )

    enum class MatchImportance {
        NORMAL, DERBY
    }

    enum class SimulationTier {
        FULL, QUICK, WEIGHTED
    }

    suspend fun simulateAllLeagues(week: Int, userTeamId: Int, skipUserMatch: Boolean = false): WeeklyLeagueReport {
        return withContext(Dispatchers.Default) {
            val dbDate = gameDateManager.formatGameDateForDb(week)

            val allLeagues = leaguesRepository.getAllLeaguesSync()
            val leagueTierMap = allLeagues.associateBy({ it.id }) { it.simulationTier }

            val toSim: List<FixturesEntity> = fixturesRepository.getFixturesToSimulateSync(dbDate)
                .filter { it.matchType != "International" }

            if (toSim.isEmpty()) return@withContext generateWeeklyReport(emptyList())

            val simulatedResults = mutableListOf<FixturesResultsEntity>()

            // Batch tracking
            val teamsToUpdate = mutableMapOf<Int, TeamsEntity>()
            val standingsToUpdate = mutableMapOf<Triple<Int, String, Int>, LeagueStandingsEntity>()
            val fixturesToUpdate = mutableListOf<FixturesEntity>()
            val resultsToInsert = mutableListOf<FixturesResultsEntity>()

            // Prefetch Caches
            val involvedTeamIds = toSim.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.toSet()
            val teamsCache = teamsRepository.getAllTeamsSync().filter { it.id in involvedTeamIds }.associateBy { it.id }.toMutableMap()

            val playersCache = playersRepository.getPlayersByTeamIdsSync(involvedTeamIds.toList()).associateBy { it.id }.toMutableMap()

            val tacticsCache = tacticsRepository.getTacticsByTeamsSync(involvedTeamIds.toList()).associateBy { it.teamId }.toMutableMap()
            val managersCache = managersRepository.getManagersByTeamIdsSync(involvedTeamIds.toList()).associateBy { it.teamId }

            val firstFixture = toSim.first()
            val seasonYear = try { firstFixture.season.split("/").first().toInt() } catch (_: Exception) { 2026 }

            val involvedLeagueNames = toSim.mapNotNull { it.league }.toSet()
            val standingsCache = if (involvedLeagueNames.isNotEmpty()) {
                leagueStandingsRepository.getStandingsByLeagues(involvedLeagueNames.toList(), seasonYear)
                    .associateBy { Triple(it.teamId, it.leagueName, it.seasonYear) }
                    .toMutableMap()
            } else mutableMapOf()

            for (fixture in toSim) {
                val importance = determineMatchImportance(fixture)
                val isUserMatch = ((fixture.homeTeamId == userTeamId) || (fixture.awayTeamId == userTeamId))

                if (isUserMatch && skipUserMatch) continue

                val tier = when {
                    isUserMatch || importance != MatchImportance.NORMAL -> SimulationTier.FULL
                    leagueTierMap[fixture.leagueId] == 0 -> SimulationTier.QUICK
                    else -> SimulationTier.WEIGHTED
                }

                val result = when (tier) {
                    SimulationTier.FULL -> matchSimulationEngine.simulateInstantResult(fixture, userTeamId)
                    SimulationTier.QUICK -> {
                        val homeTeam = teamsCache[fixture.homeTeamId] ?: teamsRepository.getTeamById(fixture.homeTeamId)!!
                        val awayTeam = teamsCache[fixture.awayTeamId] ?: teamsRepository.getTeamById(fixture.awayTeamId)!!

                        val homeManager = managersCache[homeTeam.id]
                        val awayManager = managersCache[awayTeam.id]

                        val homeTactics = tacticsCache[homeTeam.id] ?: TacticsEntity(
                            teamId = homeTeam.id,
                            teamName = homeTeam.name,
                            formation = homeTeam.formation ?: "4-4-2",
                            tacticalArchetype = TacticalMatchupEngine.mapStyleToArchetype(homeManager?.style, homeTeam.formation),
                            playstyle = homeManager?.style ?: "Balanced"
                        )
                        val awayTactics = tacticsCache[awayTeam.id] ?: TacticsEntity(
                            teamId = awayTeam.id,
                            teamName = awayTeam.name,
                            formation = awayTeam.formation ?: "4-4-2",
                            tacticalArchetype = TacticalMatchupEngine.mapStyleToArchetype(awayManager?.style, awayTeam.formation),
                            playstyle = awayManager?.style ?: "Balanced"
                        )

                        val homePlayers = playersCache.values.filter { it.teamId == homeTeam.id }
                        val awayPlayers = playersCache.values.filter { it.teamId == awayTeam.id }

                        simulateQuickMatch(fixture, homeTeam, awayTeam, homeTactics, awayTactics, homePlayers, awayPlayers)
                    }
                    SimulationTier.WEIGHTED -> {
                        val homeTeam = teamsCache[fixture.homeTeamId] ?: teamsRepository.getTeamById(fixture.homeTeamId)!!
                        val awayTeam = teamsCache[fixture.awayTeamId] ?: teamsRepository.getTeamById(fixture.awayTeamId)!!

                        val homeManager = managersCache[homeTeam.id]
                        val awayManager = managersCache[awayTeam.id]

                        val homeTactics = tacticsCache[homeTeam.id] ?: TacticsEntity(
                            teamId = homeTeam.id,
                            teamName = homeTeam.name,
                            formation = homeTeam.formation ?: "4-4-2",
                            tacticalArchetype = TacticalMatchupEngine.mapStyleToArchetype(homeManager?.style, homeTeam.formation),
                            playstyle = homeManager?.style ?: "Balanced"
                        )
                        val awayTactics = tacticsCache[awayTeam.id] ?: TacticsEntity(
                            teamId = awayTeam.id,
                            teamName = awayTeam.name,
                            formation = awayTeam.formation ?: "4-4-2",
                            tacticalArchetype = TacticalMatchupEngine.mapStyleToArchetype(awayManager?.style, awayTeam.formation),
                            playstyle = awayManager?.style ?: "Balanced"
                        )

                        simulateTier3Match(fixture, homeTeam, awayTeam, homeTactics, awayTactics)
                    }
                }

                if (tier == SimulationTier.FULL) {
                    processTier0PostMatch()
                }

                // Determine win/loss/draw flags
                val homeTeamWin = result.homeScore > result.awayScore
                val awayTeamWin = result.awayScore > result.homeScore
                val isDraw = result.homeScore == result.awayScore

                // Create result entity
                val resEntity = FixturesResultsEntity(
                    fixtureId = result.fixture.id,
                    matchDate = result.fixture.matchDate,
                    homeTeam = result.fixture.homeTeam,
                    awayTeam = result.fixture.awayTeam,
                    homeScore = result.homeScore,
                    awayScore = result.awayScore,
                    possessionHome = result.homeStats.possession,
                    possessionAway = result.awayStats.possession,
                    shotsHome = result.homeStats.shots,
                    shotsAway = result.awayStats.shots,
                    shotsOnTargetHome = result.homeStats.shotsOnTarget,
                    shotsOnTargetAway = result.awayStats.shotsOnTarget,
                    cornersHome = result.homeStats.corners,
                    cornersAway = result.awayStats.corners,
                    foulsHome = result.homeStats.fouls,
                    foulsAway = result.awayStats.fouls,
                    yellowCardsHome = result.homeStats.yellowCards,
                    yellowCardsAway = result.awayStats.yellowCards,
                    redCardsHome = result.homeStats.redCards,
                    redCardsAway = result.awayStats.redCards,
                    xgHome = result.homeStats.xG,
                    xgAway = result.awayStats.xG,
                    keyPassesHome = result.homeStats.keyPasses,
                    keyPassesAway = result.awayStats.keyPasses,
                    bigChancesMissedHome = result.homeStats.bigChancesMissed,
                    bigChancesMissedAway = result.awayStats.bigChancesMissed,
                    matchType = result.fixture.matchType,
                    season = result.fixture.season,
                    leagueName = result.fixture.league,
                    cupName = result.fixture.cupName,
                    stadium = result.fixture.stadium,
                    homeTeamId = result.fixture.homeTeamId,
                    awayTeamId = result.fixture.awayTeamId,
                    manOfMatchId = result.manOfTheMatchId
                ).withCalculatedElo()

                resultsToInsert.add(resEntity)
                fixturesToUpdate.add(fixture.copy(homeScore = result.homeScore, awayScore = result.awayScore, matchStatus = "COMPLETED"))

                // Update Teams Cache
                teamsCache[fixture.homeTeamId]?.let { t ->
                    val updated = t.updateAfterMatch(resEntity)
                    teamsCache[fixture.homeTeamId] = updated
                    teamsToUpdate[fixture.homeTeamId] = updated
                }
                teamsCache[fixture.awayTeamId]?.let { t ->
                    val updated = t.updateAfterMatch(resEntity)
                    teamsCache[fixture.awayTeamId] = updated
                    teamsToUpdate[fixture.awayTeamId] = updated
                }

                // Update Standings Cache
                if (fixture.league != null) {
                    val leagueName = fixture.league
                    val homeKey = Triple(fixture.homeTeamId, leagueName, seasonYear)
                    standingsCache[homeKey]?.let { s ->
                        val updated = s.updateFromMatchResult(result.homeScore, result.awayScore, homeTeamWin, isDraw, awayTeamWin)
                        standingsCache[homeKey] = updated
                        standingsToUpdate[homeKey] = updated
                    }
                    val awayKey = Triple(fixture.awayTeamId, leagueName, seasonYear)
                    standingsCache[awayKey]?.let { s ->
                        val updated = s.updateFromMatchResult(result.awayScore, result.homeScore, awayTeamWin, isDraw, homeTeamWin)
                        standingsCache[awayKey] = updated
                        standingsToUpdate[awayKey] = updated
                    }
                }

                // Process non-batched side effects
                processNonBatchedEffects()

                simulatedResults.add(resEntity)
            }

            // BATCH FLUSH
            if (resultsToInsert.isNotEmpty()) fixturesResultsRepository.insertResultsBatch(resultsToInsert)
            if (fixturesToUpdate.isNotEmpty()) fixturesRepository.updateFixturesBatch(fixturesToUpdate)
            if (teamsToUpdate.isNotEmpty()) teamsRepository.updateTeamsBatch(teamsToUpdate.values.toList())

            if (standingsToUpdate.isNotEmpty()) {
                val leagueGroups = standingsToUpdate.values.groupBy { it.leagueName }
                val finalStandings = mutableListOf<LeagueStandingsEntity>()

                leagueGroups.forEach { (leagueName, _) ->
                    val leagueStandings = standingsCache.values.filter { it.leagueName == leagueName && it.seasonYear == seasonYear }
                    val sorted = leagueStandings.sortedWith(
                        compareByDescending<LeagueStandingsEntity> { it.points }
                            .thenByDescending { it.goalDifference }
                            .thenByDescending { it.goalsScored }
                    )
                    finalStandings.addAll(sorted.mapIndexed { index, s -> s.copy(position = index + 1) })
                }
                leagueStandingsRepository.updateStandingsBatch(finalStandings)
            }

            generateWeeklyReport(simulatedResults)
        }
    }

    suspend fun simulateFullSeason(userTeamId: Int) {
        // High-speed bulk simulation for the entire season
        val allFixturesList = fixturesRepository.getAllFixtures().first().filter { it.matchStatus != "COMPLETED" }
        if (allFixturesList.isEmpty()) return

        val involvedTeamIds = allFixturesList.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.toSet().toList()
        val teams = teamsRepository.getTeamsByIdsSync(involvedTeamIds).associateBy { it.id }.toMutableMap()
        val tactics = tacticsRepository.getTacticsByTeamsSync(involvedTeamIds).associateBy { it.teamId }

        val resultsToInsert = mutableListOf<FixturesResultsEntity>()
        val fixturesToUpdate = mutableListOf<FixturesEntity>()

        val firstFix = allFixturesList.first()
        val seasonYear = try { firstFix.season.split("/").first().toInt() } catch (_: Exception) { 2026 }
        val leagueNames = allFixturesList.mapNotNull { it.league }.toSet().toList()

        val standings = leagueStandingsRepository.getStandingsByLeagues(leagueNames, seasonYear)
            .associateBy { Triple(it.teamId, it.leagueName, it.seasonYear) }
            .toMutableMap()

        for (fixture in allFixturesList) {
            val homeTeam = teams[fixture.homeTeamId] ?: continue
            val awayTeam = teams[fixture.awayTeamId] ?: continue
            val homeTac = tactics[fixture.homeTeamId] ?: TacticsEntity(teamId = fixture.homeTeamId, teamName = fixture.homeTeam, formation = "4-4-2", tacticalArchetype = "BALANCED", playstyle = "Balanced")
            val awayTac = tactics[fixture.awayTeamId] ?: TacticsEntity(teamId = fixture.awayTeamId, teamName = fixture.awayTeam, formation = "4-4-2", tacticalArchetype = "BALANCED", playstyle = "Balanced")

            val probs = TacticalMatchupEngine.calculateDetailedBreakdown(
                homeTac, awayTac, homeTeam.eloRating, 70.0, awayTeam.eloRating, 70.0, emptyList()
            )

            val roll = Random.nextDouble()
            val (hScore, aScore) = when {
                roll < probs.winProb -> Pair(Random.nextInt(1, 4), Random.nextInt(0, 2))
                roll < probs.winProb + probs.drawProb -> { val s = Random.nextInt(0, 3); Pair(s, s) }
                else -> Pair(Random.nextInt(0, 2), Random.nextInt(1, 4))
            }

            val homeTeamWin = hScore > aScore
            val awayTeamWin = aScore > hScore
            val isDraw = hScore == aScore

            val res = FixturesResultsEntity(
                fixtureId = fixture.id, matchDate = fixture.matchDate,
                homeTeamId = fixture.homeTeamId, homeTeam = fixture.homeTeam,
                awayTeamId = fixture.awayTeamId, awayTeam = fixture.awayTeam,
                homeScore = hScore, awayScore = aScore,
                season = fixture.season, leagueName = fixture.league,
                stadium = fixture.stadium, matchType = fixture.matchType,
                possessionHome = 50, possessionAway = 50,
                shotsHome = 10, shotsAway = 10, shotsOnTargetHome = 5, shotsOnTargetAway = 5,
                cornersHome = 4, cornersAway = 4, foulsHome = 10, foulsAway = 10,
                yellowCardsHome = 1, yellowCardsAway = 1, redCardsHome = 0, redCardsAway = 0,
                xgHome = 1.5, xgAway = 1.2, keyPassesHome = 8, keyPassesAway = 8,
                bigChancesMissedHome = 1, bigChancesMissedAway = 1,
                weatherConditions = fixture.weatherConditions
            ).withCalculatedElo()

            resultsToInsert.add(res)
            fixturesToUpdate.add(fixture.copy(homeScore = hScore, awayScore = aScore, matchStatus = "COMPLETED"))

            fixture.league?.let { lName ->
                val hKey = Triple(fixture.homeTeamId, lName, seasonYear)
                standings[hKey]?.let { s ->
                    standings[hKey] = s.updateFromMatchResult(hScore, aScore, homeTeamWin, isDraw, awayTeamWin)
                }
                val aKey = Triple(fixture.awayTeamId, lName, seasonYear)
                standings[aKey]?.let { s ->
                    standings[aKey] = s.updateFromMatchResult(aScore, hScore, awayTeamWin, isDraw, homeTeamWin)
                }
            }
        }

        fixturesResultsRepository.insertResultsBatch(resultsToInsert)
        fixturesRepository.updateFixturesBatch(fixturesToUpdate)

        // Recalculate positions
        val sortedStandings = standings.values.groupBy { it.leagueName }.flatMap { (_, leagueStandings) ->
            leagueStandings.sortedWith(compareByDescending<LeagueStandingsEntity> { it.points }.thenByDescending { it.goalDifference }).mapIndexed { i, s -> s.copy(position = i + 1) }
        }
        leagueStandingsRepository.updateStandingsBatch(sortedStandings)
    }

    private fun processNonBatchedEffects() {
        // Implementation for additional match effects (morale, news, etc)
    }

    private suspend fun simulateQuickMatch(
        fixture: FixturesEntity,
        homeTeam: TeamsEntity,
        awayTeam: TeamsEntity,
        homeTactics: TacticsEntity,
        awayTactics: TacticsEntity,
        homePlayers: List<PlayersEntity>,
        awayPlayers: List<PlayersEntity>
    ): MatchSimulationEngine.MatchResult {
        // Python Logic Integration
        val probs = TacticalMatchupEngine.calculateDetailedBreakdown(
            homeTactics, awayTactics,
            homeTeam.eloRating, calculateTeamOverallAbility(homePlayers),
            awayTeam.eloRating, calculateTeamOverallAbility(awayPlayers),
            homePlayers
        )

        val roll = Random.nextDouble()
        val result = when {
            roll < probs.winProb -> "HOME_WIN"
            roll < probs.winProb + probs.drawProb -> "DRAW"
            else -> "AWAY_WIN"
        }

        // Score Simulation (Python Logic)
        val baseHomeScore = Random.nextInt(0, 3)
        val baseAwayScore = Random.nextInt(0, 3)
        val homeStrength = (homeTeam.eloRating / 20.0) + (homeTeam.overallRating / 2.0)
        val awayStrength = (awayTeam.eloRating / 20.0) + (awayTeam.overallRating / 2.0)
        val strengthDiff = homeStrength - awayStrength
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

        return MatchSimulationEngine.MatchResult(
            fixture = fixture.copy(homeScore = homeScore, awayScore = awayScore, matchStatus = "COMPLETED"),
            homeScore = homeScore,
            awayScore = awayScore,
            result = result,
            events = emptyList(),
            homeTeamObj = homeTeam,
            awayTeamObj = awayTeam,
            playerStats = emptyMap(),
            manOfTheMatchId = null,
            homeStats = MatchSimulationEngine.TeamMatchStats(possession = 50, shots = 10, shotsOnTarget = 5, corners = 4, fouls = 10, yellowCards = 1, redCards = 0, offsides = 1, saves = 3, keyPasses = 2, bigChancesMissed = 3, xG = 1.5),
            awayStats = MatchSimulationEngine.TeamMatchStats(possession = 50, shots = 10, shotsOnTarget = 5, corners = 4, fouls = 10, yellowCards = 1, redCards = 0, offsides = 1, saves = 3, keyPasses = 2, bigChancesMissed = 3, xG = 1.5)
        )
    }

    private fun simulateTier3Match(
        fixture: FixturesEntity,
        homeTeam: TeamsEntity,
        awayTeam: TeamsEntity,
        homeTactics: TacticsEntity,
        awayTactics: TacticsEntity
    ): MatchSimulationEngine.MatchResult {
        val probs = TacticalMatchupEngine.calculateDetailedBreakdown(
            homeTactics, awayTactics,
            homeTeam.eloRating, 65.0,
            awayTeam.eloRating, 65.0,
            emptyList()
        )

        val roll = Random.nextDouble()
        val (homeScore, awayScore) = when {
            roll < (probs.winProb) -> Pair(Random.nextInt(1, 4), Random.nextInt(0, 2))
            roll < (probs.winProb + probs.drawProb) -> {
                val s = Random.nextInt(0, 3)
                Pair(s, s)
            }
            else -> Pair(Random.nextInt(0, 2), Random.nextInt(1, 4))
        }

        return MatchSimulationEngine.MatchResult(
            fixture = fixture.copy(homeScore = homeScore, awayScore = awayScore, matchStatus = "COMPLETED"),
            homeScore = homeScore,
            awayScore = awayScore,
            result = if (homeScore > awayScore) "HOME_WIN" else if (awayScore > homeScore) "AWAY_WIN" else "DRAW",
            events = emptyList(),
            homeTeamObj = homeTeam,
            awayTeamObj = awayTeam,
            playerStats = emptyMap(),
            manOfTheMatchId = null,
            homeStats = MatchSimulationEngine.TeamMatchStats(possession = 50, shots = 0, shotsOnTarget = 0, corners = 0, fouls = 0, yellowCards = 0, redCards = 0, offsides = 0, saves = 0, keyPasses = 0, bigChancesMissed = 0, xG = 0.0),
            awayStats = MatchSimulationEngine.TeamMatchStats(possession = 50, shots = 0, shotsOnTarget = 0, corners = 0, fouls = 0, yellowCards = 0, redCards = 0, offsides = 0, saves = 0, keyPasses = 0, bigChancesMissed = 0, xG = 0.0)
        )
    }

    private fun processTier0PostMatch() {
        // Update player stats and ratings
    }

    private fun determineMatchImportance(fixture: FixturesEntity): MatchImportance {
        return MatchImportance.NORMAL
    }

    private fun generateWeeklyReport(results: List<FixturesResultsEntity>): WeeklyLeagueReport {
        return WeeklyLeagueReport(results, null, null, null, emptyList(), emptyMap())
    }

    private fun calculatePlayerOverallAbility(p: PlayersEntity): Double {
        val attacking = listOf(p.finishing, p.dribbling, p.skill, p.longShots, p.pace, p.acceleration).map { it.toDouble() }.average()
        val defensive = listOf(p.defending, p.aggression, p.positioning, p.strength, p.anticipation).map { it.toDouble() }.average()
        val midfield = listOf(p.passing, p.vision, p.creativity, p.dribbling, p.teamwork).map { it.toDouble() }.average()

        return listOf(attacking, defensive, midfield, p.finishing.toDouble(), p.passing.toDouble(), p.stamina.toDouble()).average()
    }

    private fun calculateTeamOverallAbility(players: List<PlayersEntity>): Double {
        if (players.isEmpty()) return 65.0
        return players.map { calculatePlayerOverallAbility(it) }.average()
    }

}