package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.database.model.GlobalClubRanking
import com.fameafrica.afm.data.database.model.GlobalLeagueRanking
import com.fameafrica.afm.data.database.model.GlobalManagerRanking
import com.fameafrica.afm.utils.GameDateManager
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.utils.LeagueRankings
import com.fameafrica.afm.utils.calculators.LogisticsCalculator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class WorldSimulationEngine @Inject constructor(
    private val leagueSimulator: LeagueSimulator,
    private val gameDateManager: GameDateManager,
    private val transfersRepository: TransfersRepository,
    private val playersRepository: PlayersRepository,
    private val playerTrainingRepository: PlayerTrainingRepository,
    private val playerLoansRepository: PlayerLoansRepository,
    private val playerContractsRepository: PlayerContractsRepository,
    private val playerReactionsRepository: PlayerReactionsRepository,
    private val teamsRepository: TeamsRepository,
    private val nationalTeamsRepository: NationalTeamsRepository,
    private val nationalitiesRepository: NationalitiesRepository,
    private val financesRepository: FinancesRepository,
    private val infrastructureUpgradesRepository: InfrastructureUpgradesRepository,
    private val staffRepository: StaffRepository,
    private val sponsorsRepository: SponsorsRepository,
    private val storyEventsRepository: StoryEventsRepository,
    private val scoutAssignmentsRepository: ScoutAssignmentsRepository,
    private val scoutingMissionsRepository: ScoutingMissionsRepository,
    private val fanReactionsRepository: FanReactionsRepository,
    private val boardEvaluationRepository: BoardEvaluationRepository,
    private val managerOffersRepository: ManagerOffersRepository,
    private val managersRepository: ManagersRepository,
    private val jobMarketManager: JobMarketManager,
    private val retiredPlayerOffersRepository: ManagerOffersForRetiredPlayersRepository,
    private val transferWindowsRepository: TransferWindowsRepository,
    private val clubLegendsRepository: ClubLegendsRepository,
    private val seasonAwardsRepository: SeasonAwardsRepository,
    private val newsRepository: NewsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val seasonCalendarRepository: SeasonCalendarRepository,
    private val cupBracketsRepository: CupBracketsRepository,
    private val cupsRepository: CupsRepository,
    private val cupGroupStandingsRepository: CupGroupStandingsRepository,
    private val fixturesRepository: FixturesRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val matchFixingCasesRepository: MatchFixingCasesRepository,
    private val interviewsRepository: InterviewsRepository,
    private val clubVisionRepository: ClubVisionRepository,
    private val worldStateRepository: WorldStateRepository,
    private val leagueContextRepository: LeagueContextRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val trophiesRepository: TrophiesRepository,
    private val rankingsRepository: RankingsRepository,
    private val storyEngine: StoryEngine,
    private val clubDNARepository: ClubDNARepository,
    private val financialEvolutionSystem: FinancialEvolutionSystem,
    private val chairmanSystem: ChairmanSystem,
    private val journalistsRepository: JournalistsRepository,
    private val staffIntelligenceService: com.fameafrica.afm.domain.staff.StaffIntelligenceService,
    private val gameStatesRepository: GameStatesRepository,
    private val trainingSchedulerEngine: TrainingSchedulerEngine
) {

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    enum class SimulationPriority {
        PLAYABLE,   // Full simulation
        VIEWED,     // Advanced simplified
        BACKGROUND, // Quick result only
        DORMANT     // Skip or very basic
    }

    suspend fun advanceToNextPlayableEvent(teamId: Int, managerId: Int, startDay: Int, season: String): Pair<Int, List<SimulationEvent>> {
        var currentDay = startDay
        val accumulatedEvents = mutableListOf<SimulationEvent>()
        
        while (true) {
            val dbDate = gameDateManager.formatGameDateForDb(currentDay)
            
            // 1. Check for match before simulating the day (User must play match)
            val userFixture = fixturesRepository.getFixtureForTeamOnDate(teamId, dbDate)
            if (userFixture != null && userFixture.matchStatus != "COMPLETED") {
                accumulatedEvents.add(SimulationEvent.Matchday(
                    fixtureId = userFixture.id,
                    opponentName = if (userFixture.homeTeamId == teamId) userFixture.awayTeam else userFixture.homeTeam
                ))
                return currentDay to accumulatedEvents
            }
            
            // 2. Process Daily Systems for currentDay
            advanceOneDay(teamId, managerId, currentDay, season, accumulatedEvents)
            
            // 3. Increment Day
            currentDay++
            
            // 4. STOP if an important event occurred during simulation of the day just passed
            // This ensures we stop at the START of the next day.
            if (accumulatedEvents.any { it.shouldStop || it.importance >= 10 }) {
                return currentDay to accumulatedEvents
            }

            // Safety break to prevent infinite loops (max 30 days of background simulation)
            if (currentDay > startDay + 30) {
                return currentDay to accumulatedEvents
            }
        }
    }

    suspend fun advanceOneDay(teamId: Int, managerId: Int, day: Int, season: String, events: MutableList<SimulationEvent>) {
        val dbDate = gameDateManager.formatGameDateForDb(day)

        withContext(Dispatchers.Default) {
            // 1. Daily Player Systems
            processPlayerDailySystems(teamId, managerId, events)
            val upgradeEvents = infrastructureUpgradesRepository.processUpgrades()
            events.addAll(upgradeEvents)

            // 2. Match Simulation & Training
            trainingSchedulerEngine.simulateTrainingDay(dbDate)

            val fixturesReport = leagueSimulator.simulateAllLeagues(day, teamId, skipUserMatch = true)
            fixturesReport.results.forEach { result ->
                events.add(SimulationEvent.MatchPlayed(result))
            }
            processBackgroundSimulations(dbDate, season, events)

            // 3. Weekly Logic (Distributed)
            if (day % 7 == 0) {
                processWeeklySystems(teamId, managerId, day, season, events)
            }

            // 4. Monthly Logic
            if (day % 28 == 0) { // 4 weeks
                processMonthlySystems(teamId, managerId, day, season, events)
            }

            // 5. Narrative & News
            processMediaNoise(teamId, events)
            
            // Sponsorship Offers (Random chance)
            if (Random.nextInt(50) < 1) {
                events.add(SimulationEvent.SponsorshipOffer(
                    sponsorName = "Local Business",
                    value = (Random.nextInt(5, 20) * 1000).toLong()
                ))
            }
            
            // Market & AI Decisions (Run every 3 days to balance performance)
            if (day % 3 == 0) {
                val transferEvents = transfersRepository.processAITransfers(season, day, teamId)
                events.addAll(transferEvents)
                
                val responseEvents = transfersRepository.processAIResponses(day, teamId)
                events.addAll(responseEvents)

                // NEW: African Job Market System (Phase 7)
                val jobMarketEvents = jobMarketManager.processDailyJobMarket(day / 7, managerId)
                events.addAll(jobMarketEvents)

                transferWindowsRepository.updateWindowStatuses()
            }
        }
    }

    suspend fun advanceOneDay(teamId: Int, managerId: Int, day: Int, season: String): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        advanceOneDay(teamId, managerId, day, season, events)
        return events
    }

    private suspend fun processWeeklySystems(teamId: Int, managerId: Int, day: Int, season: String, events: MutableList<SimulationEvent>) {
        processCalendarMilestones(day, season, events)
        processPlayerSystems(teamId, managerId, events)
        processClubSystems(teamId, season)
        
        // Tiered Club Processing
        val allTeams = teamsRepository.getAllTeamsSync()
        val userTeam = allTeams.find { it.id == teamId }
        val relevantLeagues = setOfNotNull(userTeam?.league, "CAF Champions League", "CAF Confederation Cup")
        
        allTeams.shuffled().take(30).forEach { aiTeam ->
            if (aiTeam.id != teamId) {
                val priority = when {
                    relevantLeagues.contains(aiTeam.league) -> SimulationPriority.VIEWED
                    aiTeam.reputation > 80 -> SimulationPriority.VIEWED
                    else -> SimulationPriority.BACKGROUND
                }
                if (priority != SimulationPriority.BACKGROUND || Random.nextInt(4) == 0) {
                    processClubSystems(aiTeam.id, season)
                }
            }
        }

        chairmanSystem.processWeeklyChairmenChanges()
        staffRepository.processStaffPoaching()
        processWorldEvents(teamId, season, events)
        processManagerSystems(managerId, teamId, day, events)
        storyEngine.processWeeklyStories(day, season, teamId)
    }

    private suspend fun processMonthlySystems(teamId: Int, managerId: Int, day: Int, season: String, events: MutableList<SimulationEvent>) {
        val dateModel = gameDateManager.getGameDateModel(day)
        val month = dateModel.month
        val year = dateModel.year

        // Propose next month's training
        val scheduleId = trainingSchedulerEngine.proposeMonthlySchedule(teamId, month, year)
        
        val staffList = staffRepository.getStaffByTeamSync(teamId)
        val assistant = staffList.find { it.role == "ASSISTANT_MANAGER" }?.name ?: "Assistant Manager"
        
        val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthName = monthNames.getOrElse(month - 1) { "Unknown" }

        events.add(SimulationEvent.TrainingPlanProposed(scheduleId.toInt(), monthName, assistant))

        seasonAwardsRepository.generateMonthlyAwards(season, month)
        processMonthlyFinances()
        val boardEvents = boardEvaluationRepository.performMonthlyReview(managerId, teamId, season)
        events.addAll(boardEvents)
        
        if (boardEvents.isEmpty()) {
            events.add(SimulationEvent.BoardMeeting("Monthly Review", "Board has completed the monthly review of your performance."))
        }

        calculateGlobalRankings()
        updateWorldState(day, season)
        updateLeagueContexts(day)
    }

    /**
     * Processes background simulations for a given date.
     */
    private suspend fun processBackgroundSimulations(dbDate: String, season: String, events: MutableList<SimulationEvent>) {
        val fixtures = fixturesRepository.getFixturesToSimulate(dbDate).firstOrNull() ?: return
        val month = try { dbDate.substring(5, 7).toInt() } catch (_: Exception) { 1 }

        fixtures.forEach { fixture ->
            when (fixture.matchType) {
                "International" -> simulateInternationalMatch(fixture, season, month, events)
                else -> simulateFastMatch(fixture, season, month, events)
            }
        }
    }

    private suspend fun simulateFastMatch(fixture: FixturesEntity, season: String, month: Int, events: MutableList<SimulationEvent>) {
        val homeTeam = teamsRepository.getTeamById(fixture.homeTeamId) ?: return
        val awayTeam = teamsRepository.getTeamById(fixture.awayTeamId) ?: return
        
        val isInternational = homeTeam.country != awayTeam.country
        val travelDistance = if (homeTeam.latitude != null && homeTeam.longitude != null && awayTeam.latitude != null && awayTeam.longitude != null) {
            LogisticsCalculator.calculateDistance(homeTeam.latitude, homeTeam.longitude, awayTeam.latitude, awayTeam.longitude)
        } else {
            500.0
        }

        val travelImpact = LogisticsCalculator.calculateTravelImpact(
            distanceKm = travelDistance,
            isCharterFlight = homeTeam.reputation > 80,
            isInternational = isInternational,
            homeRegion = homeTeam.region,
            awayRegion = awayTeam.region,
            month = month
        )

        val homeAdvantage = 5.0 + (homeTeam.crowdSupport / 20.0)
        val awayFatiguePenalty = travelImpact.staminaDrop / 2.0
        
        val homeRating = homeTeam.overallRating + homeAdvantage
        val awayRating = awayTeam.overallRating - awayFatiguePenalty
        
        val ratingDiff = homeRating - awayRating
        val winProbability = 0.45 + (ratingDiff / 100.0).coerceIn(-0.4, 0.4)
        
        val random = Random.nextDouble()
        val (homeScore, awayScore) = when {
            random < winProbability -> Random.nextInt(1, 4) to Random.nextInt(0, 2)
            random < winProbability + 0.20 -> Random.nextInt(0, 2) to Random.nextInt(0, 2)
            else -> Random.nextInt(0, 2) to Random.nextInt(1, 4)
        }

        val result = createResultEntity(fixture, homeScore, awayScore, season)
        persistMatchResult(fixture, result, homeTeam, awayTeam, travelImpact)
        events.add(SimulationEvent.MatchPlayed(result))
    }

    private suspend fun simulateInternationalMatch(fixture: FixturesEntity, season: String, month: Int, events: MutableList<SimulationEvent>) {
        val homeNT = nationalTeamsRepository.getTeamById(fixture.homeTeamId) ?: return
        val awayNT = nationalTeamsRepository.getTeamById(fixture.awayTeamId) ?: return
        
        val homeCountry = nationalitiesRepository.getNationalityById(homeNT.nationalityId) ?: return
        val awayCountry = nationalitiesRepository.getNationalityById(awayNT.nationalityId) ?: return
        
        val travelDistance = if (homeCountry.latitude != null && homeCountry.longitude != null && awayCountry.latitude != null && awayCountry.longitude != null) {
            LogisticsCalculator.calculateDistance(homeCountry.latitude, homeCountry.longitude, awayCountry.latitude, awayCountry.longitude)
        } else {
            1000.0
        }

        val travelImpact = LogisticsCalculator.calculateTravelImpact(
            distanceKm = travelDistance,
            isCharterFlight = true,
            isInternational = true,
            homeRegion = homeCountry.region ?: "Unknown",
            awayRegion = awayCountry.region ?: "Unknown",
            month = month
        )

        val homeStrength = (homeNT.eloRating / 20.0) + 5.0
        val awayStrength = (awayNT.eloRating / 20.0) - (travelImpact.staminaDrop / 2.0)
        
        val winProb = 0.45 + ((homeStrength - awayStrength) / 100.0).coerceIn(-0.4, 0.4)
        
        val random = Random.nextDouble()
        val (homeScore, awayScore) = when {
            random < winProb -> Random.nextInt(1, 3) to Random.nextInt(0, 2)
            random < winProb + 0.25 -> Random.nextInt(0, 2) to Random.nextInt(0, 2)
            else -> Random.nextInt(0, 2) to Random.nextInt(1, 3)
        }

        val result = createResultEntity(fixture, homeScore, awayScore, season)
        fixturesResultsRepository.insertResult(result)
        fixturesRepository.completeFixture(fixture.id, homeScore, awayScore)
        
        nationalTeamsRepository.updateTeamForm(homeNT.id, if (homeScore > awayScore) "W" else if (homeScore == awayScore) "D" else "L")
        nationalTeamsRepository.updateTeamForm(awayNT.id, if (awayScore > homeScore) "W" else if (homeScore == awayScore) "D" else "L")
        events.add(SimulationEvent.MatchPlayed(result))
    }

    private fun createResultEntity(fixture: FixturesEntity, h: Int, a: Int, s: String): FixturesResultsEntity {
        return FixturesResultsEntity(
            fixtureId = fixture.id,
            homeTeamId = fixture.homeTeamId,
            awayTeamId = fixture.awayTeamId,
            homeTeam = fixture.homeTeam,
            awayTeam = fixture.awayTeam,
            homeScore = h,
            awayScore = a,
            season = s,
            matchDate = fixture.matchDate,
            leagueName = fixture.league,
            cupName = fixture.cupName,
            matchType = fixture.matchType,
            stadium = fixture.stadium
        ).withCalculatedElo()
    }

    private suspend fun persistMatchResult(
        fixture: FixturesEntity,
        result: FixturesResultsEntity,
        homeTeam: TeamsEntity,
        awayTeam: TeamsEntity,
        travelImpact: LogisticsCalculator.TravelImpact
    ) {
        fixturesResultsRepository.insertResult(result)
        fixturesRepository.completeFixture(fixture.id, result.homeScore, result.awayScore)
        
        teamsRepository.updateTeam(homeTeam.updateAfterMatch(result))
        teamsRepository.updateTeam(awayTeam.updateAfterMatch(result))

        if (fixture.league != null) {
            leagueStandingsRepository.updateStandingsAfterMatch(result)
        }
        
        if (fixture.cupName != null) {
            val winner = if (result.homeScore > result.awayScore) fixture.homeTeam else fixture.awayTeam
            val loser = if (result.homeScore > result.awayScore) fixture.awayTeam else fixture.homeTeam
            cupBracketsRepository.updateBracketAfterMatch(fixture.id, result.homeScore, result.awayScore, winner, loser)
            
            if (fixture.round.contains("Group")) {
                cupGroupStandingsRepository.updateGroupStandingsAfterMatch(result)
            }
        }

        playersRepository.getPlayersByTeamId(awayTeam.id).firstOrNull()?.forEach { player ->
             val updated = player.copy(stamina = (player.stamina - travelImpact.staminaDrop).coerceAtLeast(10))
             playersRepository.updatePlayer(updated)
        }
    }

    suspend fun simulateDay(teamId: Int, managerId: Int) {
        advanceOneDay(teamId, managerId, 1, "2024/25") // Mock parameters for compatibility
    }

    suspend fun simulateWeek(week: Int, season: String, teamId: Int, managerId: Int) {
        // Run advanceOneDay 7 times or just use the new advanceOneDay logic
        repeat(7) { i ->
            advanceOneDay(teamId, managerId, week * 7 + i, season)
        }
    }

    /**
     * Triggers a recalculation of seasonal rankings (e.g. Leagues)
     */
    suspend fun refreshSeasonalRankings() {
        calculateGlobalRankings(forceLeagueRefresh = true)
    }

    suspend fun simulateMonth(month: Int, season: String, teamId: Int, managerId: Int) {
        // Monthly: Awards, Finances, Board Evaluation
        seasonAwardsRepository.generateMonthlyAwards(season, month)
        processMonthlyFinances()
        boardEvaluationRepository.performMonthlyReview(managerId, teamId, season)
    }

    suspend fun updateWorldState(week: Int, season: String, force: Boolean = false) {
        val currentState = worldStateRepository.getWorldState().firstOrNull()
        
        // Phase 10 Optimization: Only update world state if forced or once per game week
        if (!force && currentState != null && currentState.lastUpdatedWeek == week && currentState.season == season) {
            return
        }

        withContext(Dispatchers.Default) {
            // 🔥 Ensure global rankings exist (Fix for initial empty rankings)
            calculateGlobalRankings()

            // Update continental rankings, dominantClubs, risingClubs, fallenGiants based on recent form/standings
            val allTeams = teamsRepository.getAllTeamsSync()
            if (allTeams.isEmpty()) return@withContext
            val topReputationTeams = allTeams.sortedByDescending { it.reputation }.take(10).map { it.id }
            
            val rising = mutableListOf<Int>()
            val fallen = mutableListOf<Int>()
            val leagueReps = mutableMapOf<String, Double>()
            
            val allStandings = leagueStandingsRepository.getAllStandingsSync()
            val allLeagues = allStandings.groupBy { it.leagueName }
            
            val teamsMap = allTeams.associateBy { it.id }

            allLeagues.forEach { (league, standings) ->
                val sorted = standings.sortedByDescending { it.points }
                
                // Calculate average league strength for reputation
                val avgRep = sorted.mapNotNull { s -> teamsMap[s.teamId]?.reputation }.average()
                leagueReps[league] = if (avgRep.isNaN()) 0.0 else avgRep

                sorted.take(3).forEach { s ->
                    val team = teamsMap[s.teamId]
                    if (team != null && team.reputation < 70) rising.add(team.id)
                }
                
                if (sorted.size > 10) {
                    sorted.takeLast(5).forEach { s ->
                        val team = teamsMap[s.teamId]
                        if (team != null && team.reputation > 80) fallen.add(team.id)
                    }
                }
            }

            // Calculate CAF Country Coefficients (Simplified: average reputation of top 4 teams per country)
            val countryCoefficients = allTeams.groupBy { it.country }
                .mapValues { (_, teams) -> 
                    teams.sortedByDescending { it.reputation }.take(4).map { it.reputation }.average()
                }
                .toList()
                .sortedByDescending { it.second }
                .take(54)
                .toMap()

            // Calculate Continental Club Rankings (Elo + Trophy weighted)
            val clubTrophyRankings = trophiesRepository.getClubTrophyRankings().firstOrNull() ?: emptyList()
            val continentalRankings = allTeams.map { team ->
                val trophyStats = clubTrophyRankings.find { it.clubName == team.name }
                val trophyScore = (trophyStats?.totalTrophies ?: 0) * 10.0 + 
                               (trophyStats?.continentalTitles ?: 0) * 25.0
                
                ClubRanking(
                    teamId = team.id,
                    teamName = team.name,
                    points = team.eloRating.toDouble() + trophyScore,
                    logoPath = team.logoPath
                )
            }.sortedByDescending { it.points }.take(100)

            val updatedState = (currentState ?: WorldStateEntity(
                continentalRankings = "{}",
                leagueReputation = "{}",
                dominantClubs = "[]",
                risingClubs = "[]",
                fallenGiants = "[]",
                clubRankings = "[]",
                lastUpdatedWeek = week,
                season = season
            )).copy(
                continentalRankings = Json.encodeToString(countryCoefficients),
                leagueReputation = Json.encodeToString(leagueReps),
                dominantClubs = Json.encodeToString(topReputationTeams),
                risingClubs = Json.encodeToString(rising.take(5)),
                fallenGiants = Json.encodeToString(fallen.take(5)),
                clubRankings = Json.encodeToString(continentalRankings),
                lastUpdatedWeek = week,
                season = season
            )

            worldStateRepository.updateWorldState(updatedState)
        }
    }

    @Serializable
    data class ClubRanking(
        val teamId: Int,
        val teamName: String,
        val points: Double,
        val logoPath: String?
    )

    suspend fun updateLeagueContexts(week: Int) {
        val allStandings = leagueStandingsRepository.getAllStandings().firstOrNull()?.groupBy { it.leagueName } ?: return
        
        allStandings.forEach { (league, standings) ->
            // Skip if no matches played in this league yet
            val totalPlayed = standings.sumOf { it.matchesPlayed }
            if (totalPlayed < standings.size * 3) return@forEach // At least 3 games per team avg before contexts

            val sorted = standings.sortedByDescending { it.points }
            if (sorted.isEmpty()) return@forEach

            val titleRace = sorted.take(3).map { it.teamId }
            val relegationBattle = if (sorted.size > 4) sorted.takeLast(4).map { it.teamId } else emptyList()
            
            val context = LeagueContextEntity(
                leagueName = league,
                titleRaceTeams = Json.encodeToString(titleRace),
                relegationBattleTeams = Json.encodeToString(relegationBattle),
                top4RaceTeams = Json.encodeToString(sorted.take(6).map { it.teamId }),
                surpriseTeamId = sorted.firstOrNull { s -> 
                    val team = teamsRepository.getTeamById(s.teamId)
                    (team?.reputation ?: 100) < 60 && s.points > 10 
                }?.teamId,
                underperformingTeamId = sorted.firstOrNull { s -> 
                    val team = teamsRepository.getTeamById(s.teamId)
                    (team?.reputation ?: 0) > 85 && standings.indexOf(s) > standings.size / 2
                }?.teamId,
                lastUpdatedWeek = week
            )
            leagueContextRepository.updateLeagueContext(context)
        }
    }

    private suspend fun processPlayerDailySystems(userTeamId: Int, managerId: Int, events: MutableList<SimulationEvent>) {
        // Bulk Recovery boosted by medical staff
        val recoveryMultiplier = staffIntelligenceService.getMedicalRecoveryMultiplier(userTeamId)
        playersRepository.recoverAllStamina((5 * recoveryMultiplier).toInt())
        
        playersRepository.advanceInjuryRecoveryDaily()
        playersRepository.clearRecoveredInjuries()

        // Daily Staff interactions
        val staffEvents = staffIntelligenceService.processDailyStaffInteractions(userTeamId, managerId)
        events.addAll(staffEvents)

        // Daily Random Injuries - Only for a small sample of active players to save time
        val samplePlayers = playersRepository.getTopRatedPlayers(200).firstOrNull() ?: emptyList()
        for (player in samplePlayers) {
            if (player.isAvailable && Random.nextInt(5000) < 1) { // Even rarer in sample
                val duration = Random.nextInt(1, 5)
                val updated = player.setInjury("Minor Knock", duration)
                playersRepository.updatePlayer(updated)
                
                val isUserPlayer = player.teamId == userTeamId
                events.add(SimulationEvent.Injury(player.name, duration, isUserPlayer))

                if (isUserPlayer) {
                    notificationsRepository.insertNotification(
                        NotificationsEntity.createInjuryNotification(player.id, player.name, "Minor Knock", duration, managerId)
                    )
                }
            }
        }
    }

    private suspend fun processMediaNoise(userTeamId: Int, events: MutableList<SimulationEvent>) {
        if (Random.nextInt(100) < 15) { // 15% chance of daily gossip
            val journalist = journalistsRepository.getRandomJournalist() ?: return
            
            // Phase 8: Optimized News Generation (Tiered)
            val userTeam = teamsRepository.getTeamById(userTeamId)
            val team = if (Random.nextBoolean() && userTeam != null) {
                userTeam
            } else {
                teamsRepository.getTopTeamsByElo(20).firstOrNull()?.randomOrNull() ?: return
            }
            
            val headlines = mutableListOf(
                "Report: ${team.name} monitoring youth prospects across the region",
                "Media: ${team.name} commercial value on the rise",
                "Insight: ${team.name} focusing on tactical discipline this week"
            )
            
            val standings = leagueStandingsRepository.getTeamStanding(team.id, team.league, 2025)
            if ((standings?.matchesPlayed ?: 0) > 3) {
                headlines.add("Social Media Buzz: ${team.name} fans demanding big changes!")
                headlines.add("Rumor: Tension brewing in ${team.name} boardroom")
            }
            
            val article = newsRepository.createNewsArticle(
                headline = headlines.random(),
                content = "${journalist.name} reports that things are moving fast behind the scenes at ${team.name}.",
                category = "RUMOR",
                journalistName = journalist.name,
                relatedTeamId = team.id,
                relatedTeam = team.name
            )
            events.add(SimulationEvent.NewsHeadline(article))
        }
    }

    private fun processMonthlyFinances() {
        // Monthly sponsorship payouts, bulk maintenance, etc.
    }

    private suspend fun processCalendarMilestones(week: Int, season: String, events: MutableList<SimulationEvent>) {
        val year = season.split("/").first().toInt()
        val milestones = seasonCalendarRepository.getMilestonesForWeek(week)
        val allTeams = teamsRepository.getAllTeamsSync()
        val userSave = gameStatesRepository.getAllSaveGames().firstOrNull()?.firstOrNull()
        val userTeam = allTeams.find { it.id == userSave?.teamId }
        val userTeamName = userTeam?.name ?: "Unknown"

        milestones.forEach { milestone ->
            when (milestone) {
                SeasonCalendarRepository.SeasonEvent.PRESEASON_START -> {
                    events.add(SimulationEvent.PreseasonStart(
                        teamName = userTeamName,
                        season = season,
                        boardExpectations = "Finish in Top 4 & Reach Cup Quarter-Finals",
                        budget = 2500000L
                    ))
                }
                SeasonCalendarRepository.SeasonEvent.TRANSFER_WINDOW_OPEN -> {
                    events.add(SimulationEvent.TransferWindowOpen(
                        budget = 2500000L,
                        wageBudget = 45000L,
                        deadlineDay = "August 31st"
                    ))
                }
                SeasonCalendarRepository.SeasonEvent.COMMUNITY_SHIELD -> {
                    events.add(SimulationEvent.CommunityShield(
                        team1 = userTeamName,
                        team2 = "Simba SC",
                        logo1 = null,
                        logo2 = null
                    ))
                }
                SeasonCalendarRepository.SeasonEvent.LEAGUE_START -> {
                    events.add(SimulationEvent.LeagueKickoff(
                        leagueName = userTeam?.league ?: "NBC Premier League",
                        predictedFinish = 3,
                        boardTarget = "Top 4",
                        openingOpponent = "Azam FC"
                    ))
                }
                SeasonCalendarRepository.SeasonEvent.CUP_ROUND_64 -> {
                    createCupRounds(year, "ROUND_OF_64")
                    events.add(SimulationEvent.CupMilestone("CRDB Federation Cup", "Round of 64", "Coastal Union", 5000))
                }
                SeasonCalendarRepository.SeasonEvent.CUP_ROUND_32 -> {
                    createCupRounds(year, "ROUND_OF_32")
                    events.add(SimulationEvent.CupMilestone("CRDB Federation Cup", "Round of 32", "Prisons FC", 10000))
                }
                SeasonCalendarRepository.SeasonEvent.CUP_ROUND_16 -> {
                    createCupRounds(year, "ROUND_OF_16")
                    events.add(SimulationEvent.CupMilestone("CRDB Federation Cup", "Round of 16", "Singida Big Stars", 20000))
                }
                SeasonCalendarRepository.SeasonEvent.CUP_QUARTER_FINAL -> {
                    createCupRounds(year, "QUARTER_FINAL")
                    events.add(SimulationEvent.CupMilestone("CRDB Federation Cup", "Quarter-Final", "Yanga SC", 50000))
                }
                SeasonCalendarRepository.SeasonEvent.CUP_SEMI_FINAL -> {
                    createCupRounds(year, "SEMI_FINAL")
                    events.add(SimulationEvent.CupMilestone("CRDB Federation Cup", "Semi-Final", "Simba SC", 100000))
                }
                SeasonCalendarRepository.SeasonEvent.CUP_FINAL -> {
                    createCupRounds(year, "FINAL")
                    events.add(SimulationEvent.CupMilestone("CRDB Federation Cup", "FINAL", "Azam FC", 250000))
                }
                SeasonCalendarRepository.SeasonEvent.CAF_GROUP_STAGE_START -> {
                    events.add(SimulationEvent.CAFGroupDraw(
                        groupName = "Group B",
                        opponents = listOf("Al Ahly", "ASEC Mimosas", "Wydad AC"),
                        travelDistance = 12500
                    ))
                }
                SeasonCalendarRepository.SeasonEvent.CAF_KNOCKOUT_START -> {
                    cupBracketsRepository.createFixturesForRound("CAF Champions League", year, "QUARTER_FINAL")
                    cupBracketsRepository.createFixturesForRound("CAF Confederation Cup", year, "QUARTER_FINAL")
                    events.add(SimulationEvent.CAFKnockoutDraw("CAF Champions League", "Quarter-Final", "Mamelodi Sundowns"))
                }
                SeasonCalendarRepository.SeasonEvent.CAF_FINAL -> {
                    events.add(SimulationEvent.CAFFinal("Al Ahly", "Cairo International Stadium", true))
                }
                SeasonCalendarRepository.SeasonEvent.TRANSFER_WINDOW_CLOSE -> {
                    events.add(SimulationEvent.DeadlineDay(6,4))
                }
                SeasonCalendarRepository.SeasonEvent.SEASON_END -> {
                    seasonAwardsRepository.generateEndOfSeasonAwards(season)
                    retiredPlayerOffersRepository.generateOffersForAllRetiredPlayers()
                    transferWindowsRepository.initializeSeasonWindows(season)
                    processEndOfSeasonLegends()
                    
                    events.add(SimulationEvent.AwardsGala(
                        season = season,
                        position = 2,
                        trophiesWon = listOf("Community Shield"),
                        topScorer = "Clement Mzize (18 Goals)",
                        revenue = 15000000,
                        fanApproval = 88
                    ))
                }
                else -> Unit
            }
        }
    }

    private suspend fun processEndOfSeasonLegends() {
        // Optimize: Only check players who are actually candidates for legends (older or high caps/reputation)
        val candidates = playersRepository.getTopRatedPlayers(500).firstOrNull() ?: emptyList()
        for (player in candidates) {
            if (player.retired || player.experience >= 150) {
                // Check if this player qualifies as a legend for their current team
                player.teamId?.let { tid ->
                    clubLegendsRepository.checkAndAddLegend(
                        playerId = player.id,
                        playerName = player.name,
                        teamId = tid,
                        clubName = player.teamName,
                        yearsPlayed = player.experience / 30, // Simplified conversion
                        totalTitles = player.trophies
                    )
                }
            }
        }
    }

    private suspend fun createCupRounds(year: Int, round: String) {
        // Only get active cups to save time
        cupsRepository.getAllCups().firstOrNull()?.filter { it.isDomesticCup }?.forEach { cup ->
            try { 
                // Only create if not already exists for this round/year to avoid duplicate effort
                cupBracketsRepository.createFixturesForRound(cup.name, year, round) 
            } catch (_: Exception) {}
        }
    }

    private suspend fun processPlayerSystems(userTeamId: Int, managerId: Int, events: MutableList<SimulationEvent>) {
        val userTeam = teamsRepository.getTeamById(userTeamId)
        val userTeamName = userTeam?.name ?: "Unknown"

        // 1. Bulk Updates (Fast)
        val recoveryMultiplier = staffIntelligenceService.getMedicalRecoveryMultiplier(userTeamId)
        playersRepository.recoverAllStamina((35 * recoveryMultiplier).toInt())

        playersRepository.advanceInjuryRecovery()
        playersRepository.clearRecoveredInjuries()

        // 2. Focused Training (User Team only, AI training is lightweight)
        playersRepository.getPlayersByTeamId(userTeamId).firstOrNull()?.forEach { player ->
            playerTrainingRepository.processPlayerWeeklyTraining(player.id)
        }
        
        // 3. Random Events - Use a much smaller sample for AI players to save time
        val playerUpdates = mutableListOf<PlayersEntity>()
        val aiPlayersSample = playersRepository.getTopRatedPlayers(200).firstOrNull() ?: emptyList()
        
        for (player in aiPlayersSample) {
            var updatedPlayer = player
            var changed = false

            // Random Injury Generation
            val injuryChance = if ((player.teamId ?: 0) == userTeamId) 8 else 2
            if (player.isAvailable && Random.nextInt(1000) < injuryChance) {
                val injuryTypes = listOf("Hamstring Strain", "Ankle Sprain", "Knee Knock", "Muscle Tear", "Groin Strain")
                val duration = Random.nextInt(3, 21)
                val type = injuryTypes.random()
                updatedPlayer = updatedPlayer.setInjury(type, duration)
                changed = true
                
                val isUserPlayer = player.teamId == userTeamId
                events.add(SimulationEvent.Injury(player.name, duration, isUserPlayer))

                if (isUserPlayer) {
                    notificationsRepository.insertNotification(
                        NotificationsEntity.createInjuryNotification(player.id, player.name, type, duration, managerId)
                    )
                    val article = newsRepository.createNewsArticle(
                        "Injury Blow for $userTeamName",
                        "${player.name} has been ruled out for approximately $duration days due to $type.",
                        "IN_PLAY",
                        relatedTeam = userTeamName,
                        relatedPlayer = player.name
                    )
                    events.add(SimulationEvent.NewsHeadline(article))
                }
            }
            
            // Player Reactions
            if (Random.nextInt(100) < 5) {
                if (player.morale < 40) playerReactionsRepository.addAngryReaction(player.id, player.name, "POOR_FORM")
                else if (player.morale > 80) playerReactionsRepository.addHappyReaction(player.id, player.name, "WIN")
            }

            if (changed) {
                playerUpdates.add(updatedPlayer)
            }
        }
        
        if (playerUpdates.isNotEmpty()) {
            playersRepository.updatePlayersBatch(playerUpdates)
        }

        // Contract Maintenance
        playerContractsRepository.updateContractStatuses()
        
        // Loan Returns
        playerLoansRepository.processLoanExpirations()
    }

    private suspend fun processClubSystems(teamId: Int, season: String) {
        val team = teamsRepository.getTeamById(teamId) ?: return
        val finances = financesRepository.getTeamFinances(teamId, season)
        val clubDNA = clubDNARepository.getClubDNA(teamId)

        // 1. Financial Evolution logic (Weekly update)
        // 1. FINANCIAL CONSEQUENCE SYSTEM (Evolution)
        if (finances != null && clubDNA != null) {
            val resultsTrend = calculateResultsTrend(team.formStreak)

            // DNA Evolution: Check if the club should mutate its spending behavior
            financialEvolutionSystem.evolveFinancialBehavior(
                teamId = teamId,
                currentBalance = finances.bankBalance,
                seasonProfit = finances.profitLoss,
                resultsTrend = resultsTrend
            )

            // 2. PRESSURE MULTIPLIERS (High Reputation = High Volatility)
            // val reputationFactor = (team.reputation / 100f).coerceIn(0.5f, 2.0f)

            // 3. PERSISTENT WORLD MEMORY (Board & Fan Sentiment)
            val performanceScore = calculatePerformanceScore(teamId, team.league, season)

            // Board Confidence decays faster if financial losses continue
            val financialPressure = if (finances.profitLoss < -1_000_000) 10 else 0
            val newBoardConfidence = (team.boardConfidence + (performanceScore - 50) - financialPressure)
                .coerceIn(0, 100)

            // Fan Sentiment is influenced by results + performanceTrend
            val fanOutrage = if (resultsTrend < 0.2f) -15 else 0
            val newFanSentiment = (team.fanSentiment + (performanceScore - 50) + fanOutrage)
                .coerceIn(0, 100)

            // Update DNA with persistent values
            teamsRepository.updateTeam(team.copy(
                boardConfidence = newBoardConfidence,
                fanSentiment = newFanSentiment,
                managerSecurity = calculateManagerSecurity(newBoardConfidence, newFanSentiment)
            ))

            // 4. STORY ENGINE EVOLUTION (Emotion-Driven Narratives)
            generateContextualNews(team, newBoardConfidence, newFanSentiment, resultsTrend)
        }

        // 2. Financial Maintenance (Data-driven DNA & Tier system)
        val baseCost = 10000L
        val (multiplier, volatility, stabilityBonus) = when (clubDNA?.financialBehavior) {
            FinancialBehavior.FRUGAL -> Triple(0.6, 0.1, 3000L)
            FinancialBehavior.LOW_REVENUE_SURVIVAL -> Triple(0.4, 0.25, 1000L)
            FinancialBehavior.SPONSOR_DEPENDENT -> Triple(1.0, 0.3, 5000L)
            FinancialBehavior.PLAYER_SALES_DEPENDENT -> Triple(1.1, 0.5, 2000L)
            FinancialBehavior.GOVERNMENT_BACKED -> Triple(1.6, 0.15, 12000L)
            FinancialBehavior.CORPORATE_STRUCTURED -> Triple(1.8, 0.1, 15000L)
            FinancialBehavior.TOURNAMENT_DRIVEN -> Triple(1.3, 0.6, 8000L)
            FinancialBehavior.SPENDER -> Triple(1.5, 0.4, 10000L)
            FinancialBehavior.RISKY -> Triple(1.4, 0.8, 5000L)
            FinancialBehavior.UNSTABLE -> Triple(0.9, 0.9, 0L)
            FinancialBehavior.COMMUNITY_FUNDED -> Triple(0.7, 0.2, 2000L)
            else -> Triple(1.0, 0.3, 3000L)
        }

        val sizeMultiplier = when (team.tier.uppercase()) {
            "ELITE" -> 2.0
            "CHAMPIONSHIP" -> 1.5
            "PROFESSIONAL" -> 1.0
            "SEMI-PROFESSIONAL" -> 0.7
            "AMATEUR" -> 0.5
            else -> 1.0
        }

        val randomRoll = Random.nextDouble()
        val maintenanceCost = if (randomRoll < volatility) {
            (baseCost * multiplier * 1.5 * sizeMultiplier).toLong()
        } else {
            ((baseCost * multiplier * sizeMultiplier) + stabilityBonus).toLong()
        }
        financesRepository.addInfrastructureCost(teamId, season, maintenanceCost)

        // 3. Club Vision & Philosophy Update
        val seasonYear = season.split("/").firstOrNull()?.toIntOrNull() ?: 2024
        val standing = leagueStandingsRepository.getTeamStanding(teamId, team.league, seasonYear)
        
        val manager = managersRepository.getManagerByTeamId(teamId)
        val vision = clubVisionRepository.getVisionForTeam(teamId).firstOrNull()

        val performanceScore = standing?.let { 
            // Normalize performance based on expected vs actual
            val diff = (standing.position - 5).coerceIn(-10, 10)
            50 - (diff * 5)
        } ?: 50

        clubVisionRepository.processWeeklyVisionUpdate(
            teamId = teamId,
            performanceScore = performanceScore,
            leaguePosition = standing?.position ?: 10,
            expectedPosition = 5,
            youthUsage = manager?.youthDevelopmentFocus ?: 0,
            managerStyle = manager?.style ?: "BALANCED"
        )

        // DNA Evolution
        if (clubDNA != null) {
            val financialHealthScore = when (finances?.financialHealth) {
                "Excellent" -> 90
                "Good" -> 75
                "Stable" -> 60
                "Fair" -> 45
                "Concerning" -> 25
                else -> 50
            }
            
            clubDNARepository.processWeeklyDNAEvolution(
                teamId = teamId,
                performanceScore = performanceScore,
                financialHealth = financialHealthScore,
                alignmentScore = vision?.visionAlignment ?: 50
            )
        }

        // 4. Financials: Wages
        financesRepository.getTeamFinances(teamId, season)?.let { finances ->
            financesRepository.addWages(teamId, season, finances.wageBill / 52.0)
        }
        
        // 5. Staff Growth (Only for the club being processed)
        staffRepository.getStaffByTeam(teamId).firstOrNull()?.forEach { staff ->
            staffRepository.processWeeklyStaffDevelopment(staff.id)
        }
        
        // 6. Commercial
        sponsorsRepository.getTeamSponsors(teamId).firstOrNull()?.forEach { sponsor ->
            financesRepository.addRevenue(teamId, season, sponsor.sponsorshipValue / 52.0, "Sponsorship")
        }

        // 7. Chairman's Business Skill Commercial Boost
        financesRepository.processChairmanCommercialBoost(teamId, season)
    }

    private fun calculateResultsTrend(formStreak: String): Float {
        if (formStreak.isEmpty()) return 0.5f
        val recent = formStreak.takeLast(5)
        val points = recent.sumOf { 
            when (it) {
                'W' -> 3
                'D' -> 1
                else -> 0
            }
        }
        return points / (recent.length * 3.0f)
    }

    private suspend fun processWorldEvents(teamId: Int, season: String, events: MutableList<SimulationEvent>) {
        val team = teamsRepository.getTeamById(teamId)
        val teamName = team?.name ?: "Unknown"

        // Random Story Events
        storyEventsRepository.generateRandomEvent(teamId, teamName)
        
        // Scouting Progress
        scoutingMissionsRepository.processWeeklyMissions()

        scoutAssignmentsRepository.getAllAssignments().firstOrNull()?.forEach { assignment ->
            if (assignment.reportStatus == "IN_PROGRESS" && Random.nextInt(100) < 40) {
                scoutAssignmentsRepository.autoGenerateScoutReport(assignment.id)
            }
        }
        
        // Match Fixing (Level 4 & 5 specific)
        if (Random.nextInt(1000) < 5) { // Rare event
             val t = teamsRepository.getTopTeamsByElo(100).firstOrNull()?.randomOrNull() 
             t?.let { team ->
                 matchFixingCasesRepository.createMatchFixingCase(
                     teamInvolved = team.name,
                     teamInvolvedId = team.id,
                     managerId = null,
                     managerName = "Unknown",
                     leagueName = team.league,
                     leagueLevel = 4, 
                     season = season,
                     allegationDetails = "Suspect betting patterns observed in recent matches."
                 )
                 val article = newsRepository.createNewsArticle(
                     "SCANDAL: Match Fixing Allegations in ${team.league}",
                     "Authorities are investigating ${team.name} over alleged irregularities in their recent league fixtures.",
                     "WORLD",
                     relatedTeam = team.name,
                     isTopNews = true
                 )
                 events.add(SimulationEvent.NewsHeadline(article))
             }
        }

        // Global Fan Sentiment
        fanReactionsRepository.generateWeeklyFanReactions(teamId, teamName, season)
        fanReactionsRepository.deleteOldReactions(30)
    }

    private suspend fun processManagerSystems(managerId: Int, teamId: Int, week: Int, events: MutableList<SimulationEvent>) {
        val team = teamsRepository.getTeamById(teamId)
        val teamName = team?.name ?: "Unknown"

        // Board logic
        val boardEvents = boardEvaluationRepository.evaluateBoardStatus(managerId)
        events.addAll(boardEvents)
        
        // Random sponsorship offer
        if (Random.nextInt(50) < 1) {
            val sponsorName = listOf("Safir", "Zenith", "EcoBank", "Orange", "MTN").random()
            val value = (Random.nextInt(100, 1000) * 1000L)
            events.add(SimulationEvent.SponsorshipOffer(sponsorName, value))
        }

        // Random job offer if eligible
        if (Random.nextInt(100) < 5) {
            val offer = managerOffersRepository.generateRandomOfferIfEligible(managerId)
            offer?.let {
                events.add(SimulationEvent.JobOffer(it.offeringTeam, it.offerType))
            }
        }
        
        // Media Opportunities
        if (Random.nextInt(100) < 20) {
            interviewsRepository.generateInterview(
                managerId = managerId,
                teamName = teamName,
                context = listOf("PLAYER_FORM", "TITLE_RACE", "CLUB_OUTLOOK").random(),
                currentWeek = week
            )
        }
    }

    private fun calculateManagerSecurity(boardConfidence: Int, fanSentiment: Int): Int {
        // Manager security is a weighted average of board and fan support
        return ((boardConfidence * 0.7) + (fanSentiment * 0.3)).toInt().coerceIn(0, 100)
    }

    private suspend fun calculatePerformanceScore(teamId: Int, league: String, season: String): Int {
        val year = season.split("/").firstOrNull()?.toIntOrNull() ?: 2026
        val standing = leagueStandingsRepository.getTeamStanding(teamId, league, year)

        return if (standing != null) {
            // High score for low position (1st place = 100 points)
            // Adjust '20' based on the number of teams in your league
            val base = 100 - ((standing.position - 1) * 5)
            base.coerceIn(0, 100)
        } else {
            50 // Neutral score if no games played
        }
    }

    private suspend fun generateContextualNews(
        team: TeamsEntity,
        boardConfidence: Int,
        fanSentiment: Int,
        resultsTrend: Float
    ) {
        val teamName = team.name

        when {
            boardConfidence < 20 -> {
                newsRepository.createNewsArticle(
                    "Board Ultimatum for $teamName",
                    "The board is reportedly losing patience. Rumors suggest the manager is one game away from the sack.",
                    "URGENT",
                    relatedTeam = teamName
                )
            }
            fanSentiment < 30 && resultsTrend < 0.3f -> {
                newsRepository.createNewsArticle(
                    "Protests at $teamName",
                    "Fans have gathered outside the stadium expressing their outrage at the recent run of poor results.",
                    "CRISIS",
                    relatedTeam = teamName
                )
            }
            fanSentiment > 85 && resultsTrend > 0.8f -> {
                newsRepository.createNewsArticle(
                    "Euphoria at $teamName",
                    "The supporters are dreaming big as $teamName continues their dominant streak in the league.",
                    "TRENDING",
                    relatedTeam = teamName
                )
            }
        }
    }

    private suspend fun calculateGlobalRankings(forceLeagueRefresh: Boolean = false) {
        // 1. League Rankings - Use LeagueRankings utils for initial data and seasonal updates
        val leagueCache = rankingsRepository.getRankingsByType("LEAGUE").firstOrNull()
        if (leagueCache == null || forceLeagueRefresh) {
            val allLeagues = leaguesRepository.getAllLeagues().firstOrNull() ?: emptyList()
            val leagueRankings = allLeagues.map { league ->
                val country = league.country ?: "Unknown"
                // Use the centralized rankings and reputation from utils
                val adjustedRank = com.fameafrica.afm.utils.LeagueRankings.getLeagueRank(country, league.level)
                val finalRating = com.fameafrica.afm.utils.LeagueRankings.getLeagueReputation(country, league.level).toDouble() / 20.0

                GlobalLeagueRanking(
                    rank = adjustedRank,
                    leagueName = league.name,
                    country = country,
                    averageRating = finalRating,
                    totalMarketValue = league.prizeMoney.toLong() * 10,
                    region = LeagueRankings.getCountryRegion(country),
                    logoPath = league.logo
                )
            }.sortedBy { it.rank }
            
            // Re-assign 1..N ranks based on sorted order for UI table consistency
            val finalLeagueRankings = leagueRankings.mapIndexed { index, ranking -> 
                ranking.copy(rank = index + 1) 
            }
            rankingsRepository.saveRankings("LEAGUE", json.encodeToString(finalLeagueRankings))
        }

        // 2. Club Rankings (Monthly)
        if (rankingsRepository.shouldUpdateRankings("CLUB", 30)) {
            val topTeams = teamsRepository.getTopTeamsByElo(100).firstOrNull() ?: emptyList()
            val clubRankings = topTeams.mapIndexed { index, team ->
                GlobalClubRanking(
                    rank = index + 1,
                    clubName = team.name,
                    league = team.league,
                    reputation = team.reputation,
                    totalMarketValue = team.revenue.toLong() * 5,
                    lastTrophy = null,
                    logoPath = team.logoPath
                )
            }.take(10)
            rankingsRepository.saveRankings("CLUB", json.encodeToString(clubRankings))
        }

        // 3. Manager Rankings (Monthly)
        if (rankingsRepository.shouldUpdateRankings("MANAGER", 30)) {
            val allManagers = managersRepository.getAllManagers().firstOrNull() ?: emptyList()
            val managerRankings = allManagers.filter { it.matchesManaged >= 50 }
                .map { manager ->
                    val team = teamsRepository.getTeamById(manager.teamId ?: -1)
                    GlobalManagerRanking(
                        rank = 0,
                        managerName = manager.name,
                        currentClub = team?.name,
                        trophiesWon = manager.trophiesWon,
                        winPercentage = manager.winPercentage,
                        reputation = manager.reputation
                    )
                }
                .sortedWith(compareByDescending<GlobalManagerRanking> { it.trophiesWon }.thenByDescending { it.winPercentage })
                .take(10)
                .mapIndexed { index, ranking -> ranking.copy(rank = index + 1) }

            rankingsRepository.saveRankings("MANAGER", json.encodeToString(managerRankings))
        }
    }
}
