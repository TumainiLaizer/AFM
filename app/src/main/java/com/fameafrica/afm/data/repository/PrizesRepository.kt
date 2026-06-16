package com.fameafrica.afm.data.repository

import androidx.room.ColumnInfo
import com.squareup.moshi.Json
import com.fameafrica.afm.data.database.dao.PrizesLeaguesDao
import com.fameafrica.afm.data.database.dao.PrizesCupDao
import com.fameafrica.afm.data.database.dao.LeaguesDao
import com.fameafrica.afm.data.database.dao.CupsDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class PrizesRepository @Inject constructor(
    private val prizesLeaguesDaoProvider: Provider<PrizesLeaguesDao>,
    private val prizesCupDaoProvider: Provider<PrizesCupDao>,
    private val leaguesDaoProvider: Provider<LeaguesDao>,
    private val cupsDaoProvider: Provider<CupsDao>,
    private val teamsDaoProvider: Provider<TeamsDao>
) {
    private val prizesLeaguesDao get() = prizesLeaguesDaoProvider.get()
    private val prizesCupDao get() = prizesCupDaoProvider.get()
    private val leaguesDao get() = leaguesDaoProvider.get()
    private val cupsDao get() = cupsDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()

    // ============ LEAGUE PRIZE CALCULATION ============
    // Paid at END OF SEASON based on final position

    /**
     * Calculate and insert prize money for all leagues based on:
     * - Champion prize money (from leagues table)
     * - Actual number of teams in the league
     * - Weighted distribution favoring top positions
     */
    suspend fun initializeLeaguePrizes() {
        val leagues = leaguesDao.getAll().firstOrNull() ?: return

        for (league in leagues) {
            // Get actual number of teams in this league
            val teamsInLeague = teamsDao.getTeamsByLeague(league.name).firstOrNull() ?: emptyList()
            val numTeams = teamsInLeague.size

            if (numTeams == 0) continue

            // Calculate prize distribution
            val prizes = calculateLeaguePrizeDistribution(
                championPrize = league.prizeMoney,
                numTeams = numTeams,
                leagueId = league.id
            )

            // Insert prizes
            prizesLeaguesDao.deleteByCompetition(league.id)
            prizesLeaguesDao.insertAll(prizes)
        }
    }

    /**
     * Calculate prize distribution for a league based on number of teams
     *
     * Distribution Formula:
     * - Champion: 100% of base (champion prize)
     * - Runner-up: 50% of champion
     * - 3rd Place: 33% of champion
     * - 4th Place: 25% of champion
     * - Top 25% of remaining teams: 15% of champion
     * - Middle 50% of teams: 10% of champion
     * - Bottom 25% of teams: 5% of champion
     */
    fun calculateLeaguePrizeDistribution(
        championPrize: Int,
        numTeams: Int,
        leagueId: Int
    ): List<PrizesLeaguesEntity> {
        val prizes = mutableListOf<PrizesLeaguesEntity>()

        // Champion (Position 1)
        prizes.add(PrizesLeaguesEntity(
            competitionId = leagueId,
            position = 1,
            prizeMoney = championPrize,
            percentageOfChampion = 100.0,
            tier = LeaguePrizeTier.CHAMPION.value,
            description = "League Champion"
        ))

        if (numTeams >= 2) {
            prizes.add(PrizesLeaguesEntity(
                competitionId = leagueId,
                position = 2,
                prizeMoney = (championPrize * 0.5).roundToInt(),
                percentageOfChampion = 50.0,
                tier = LeaguePrizeTier.RUNNER_UP.value,
                description = "Runner-up"
            ))
        }

        if (numTeams >= 3) {
            prizes.add(PrizesLeaguesEntity(
                competitionId = leagueId,
                position = 3,
                prizeMoney = (championPrize * 0.33).roundToInt(),
                percentageOfChampion = 33.0,
                tier = LeaguePrizeTier.THIRD_PLACE.value,
                description = "Third Place"
            ))
        }

        if (numTeams >= 4) {
            prizes.add(PrizesLeaguesEntity(
                competitionId = leagueId,
                position = 4,
                prizeMoney = (championPrize * 0.25).roundToInt(),
                percentageOfChampion = 25.0,
                tier = LeaguePrizeTier.FOURTH_PLACE.value,
                description = "Fourth Place"
            ))
        }

        // Calculate remaining positions
        val remainingTeams = numTeams - 4

        if (remainingTeams > 0) {
            // Top 25% of remaining teams (positions 5-8 or up to 25% of remaining)
            val topTierCount = (remainingTeams * 0.25).roundToInt().coerceAtLeast(1)
            val topTierEnd = 4 + topTierCount

            for (pos in 5..topTierEnd) {
                if (pos <= numTeams) {
                    prizes.add(PrizesLeaguesEntity(
                        competitionId = leagueId,
                        position = pos,
                        prizeMoney = (championPrize * 0.15).roundToInt(),
                        percentageOfChampion = 15.0,
                        tier = LeaguePrizeTier.TOP_TIER.value,
                        description = "Top Half Finish"
                    ))
                }
            }

            // Middle 50% of remaining teams
            val middleTierCount = (remainingTeams * 0.5).roundToInt()
            val middleTierStart = topTierEnd + 1
            val middleTierEnd = middleTierStart + middleTierCount - 1

            for (pos in middleTierStart..middleTierEnd) {
                if (pos <= numTeams) {
                    prizes.add(PrizesLeaguesEntity(
                        competitionId = leagueId,
                        position = pos,
                        prizeMoney = (championPrize * 0.10).roundToInt(),
                        percentageOfChampion = 10.0,
                        tier = LeaguePrizeTier.MID_TIER.value,
                        description = "Mid-table Finish"
                    ))
                }
            }

            // Bottom 25% of remaining teams
            val bottomTierStart = middleTierEnd + 1
            for (pos in bottomTierStart..numTeams) {
                prizes.add(PrizesLeaguesEntity(
                    competitionId = leagueId,
                    position = pos,
                    prizeMoney = (championPrize * 0.05).roundToInt(),
                    percentageOfChampion = 5.0,
                    tier = LeaguePrizeTier.BOTTOM_TIER.value,
                    description = "Participation"
                ))
            }
        }

        return prizes
    }

    /**
     * Award league prizes at END OF SEASON based on final standings
     */
    suspend fun awardLeaguePrizes(leagueId: Int, finalStandings: List<LeagueStandingsEntity>) {
        val prizes = prizesLeaguesDao.getPrizesByCompetition(leagueId).firstOrNull() ?: return
        val league = leaguesDao.getById(leagueId) ?: return

        println("\n🏆 END OF SEASON PRIZE DISTRIBUTION FOR ${league.name}")
        println("==========================================")

        for (standing in finalStandings) {
            val prize = prizes.find { it.position == standing.position }
            prize?.let {
                // Award prize money to team
                // This would update team's finances
                println("Position ${standing.position}: ${standing.teamName} receives ${it.prizeMoney} (${it.description})")
            }
        }
    }

    // ============ CUP PRIZE CALCULATION ============
    // Paid at EACH STAGE as teams progress

    /**
     * Calculate and insert prize money for all cups based on:
     * - Champion prize money (from cups table)
     * - teams_involved field from cups table
     * - Cup format and structure
     * - Prizes awarded at each stage
     */
    suspend fun initializeCupPrizes() {
        val cups = cupsDao.getAll().firstOrNull() ?: return

        for (cup in cups) {
            // Use teams_involved from cups table
            val numTeams = cup.teamsInvolved

            if (numTeams == 0) continue

            // Calculate prize distribution based on cup type and team count
            val prizes = calculateCupPrizeDistribution(
                championPrize = cup.prizeMoney,
                cupType = cup.type,
                numTeams = numTeams,
                cupId = cup.id,
                cupName = cup.name
            )

            // Insert prizes
            prizesCupDao.deleteByCompetition(cup.id)
            prizesCupDao.insertAll(prizes)
        }
    }

    /**
     * Calculate cup prize distribution based on tournament format
     * Prizes are awarded at EACH STAGE as teams progress
     */
    fun calculateCupPrizeDistribution(
        championPrize: Int,
        cupType: String?,
        numTeams: Int,
        cupId: Int,
        cupName: String
    ): List<PrizesCupEntity> {
        return when (cupType?.lowercase()) {
            "continental" -> calculateContinentalCupPrizes(championPrize, numTeams, cupId, cupName)
            "international" -> calculateInternationalCupPrizes(championPrize, numTeams, cupId, cupName)
            "national" -> calculateNationalCupPrizes(championPrize, numTeams, cupId, cupName)
            else -> calculateStandardCupPrizes(championPrize, numTeams, cupId, cupName)
        }
    }

    private fun calculateContinentalCupPrizes(
        championPrize: Int,
        numTeams: Int,
        cupId: Int,
        cupName: String
    ): List<PrizesCupEntity> {
        val prizes = mutableListOf<PrizesCupEntity>()

        // Winner (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 1,
            prizeMoney = championPrize,
            percentageOfChampion = 100.0,
            teamsAtStage = 1,
            description = "Continental Champion - Final Stage Prize",
            paidAtStage = true
        ))

        // Runner-up (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 2,
            prizeMoney = (championPrize * 0.6).roundToInt(),  // 60% of champion
            percentageOfChampion = 60.0,
            teamsAtStage = 1,
            description = "Continental Runner-up - Final Stage Prize",
            paidAtStage = true
        ))

        // Semi-finalists (2 teams) - Paid at Semi-final Stage
        val semiPrize = (championPrize * 0.3).roundToInt()  // 30% of champion each
        for (i in 1..2) {
            prizes.add(PrizesCupEntity(
                competitionId = cupId,
                stage = "SEMI_FINAL",
                position = i,
                prizeMoney = semiPrize,
                percentageOfChampion = 30.0,
                teamsAtStage = 2,
                description = "Semi-finalist - Reaching Semi-finals",
                paidAtStage = true
            ))
        }

        // Quarter-finalists (4 teams) - Paid at Quarter-final Stage
        if (numTeams >= 8) {
            val quarterPrize = (championPrize * 0.15).roundToInt()  // 15% of champion each
            for (i in 1..4) {
                prizes.add(PrizesCupEntity(
                    competitionId = cupId,
                    stage = "QUARTER_FINAL",
                    position = i,
                    prizeMoney = quarterPrize,
                    percentageOfChampion = 15.0,
                    teamsAtStage = 4,
                    description = "Quarter-finalist - Reaching Quarter-finals",
                    paidAtStage = true
                ))
            }
        }

        // Round of 16 (8 teams) - Paid at Round of 16 Stage
        if (numTeams >= 16) {
            val round16Prize = (championPrize * 0.08).roundToInt()  // 8% of champion each
            for (i in 1..8) {
                prizes.add(PrizesCupEntity(
                    competitionId = cupId,
                    stage = "ROUND_16",
                    position = i,
                    prizeMoney = round16Prize,
                    percentageOfChampion = 8.0,
                    teamsAtStage = 8,
                    description = "Round of 16 - Reaching Round of 16",
                    paidAtStage = true
                ))
            }
        }

        // Group stage participants - Paid at Group Stage completion
        if (numTeams >= 16 && cupName.contains("Group")) {
            val groupStageTeams = numTeams - 16 // Assuming 16 advance to knockout
            val groupPrize = (championPrize * 0.04).roundToInt()  // 4% of champion each
            for (i in 1..groupStageTeams) {
                prizes.add(PrizesCupEntity(
                    competitionId = cupId,
                    stage = "GROUP_STAGE",
                    position = i,
                    prizeMoney = groupPrize,
                    percentageOfChampion = 4.0,
                    teamsAtStage = groupStageTeams,
                    description = "Group Stage Participant - Reaching Group Stage",
                    paidAtStage = true
                ))
            }
        }

        return prizes
    }

    private fun calculateInternationalCupPrizes(
        championPrize: Int,
        numTeams: Int,
        cupId: Int,
        cupName: String
    ): List<PrizesCupEntity> {
        val prizes = mutableListOf<PrizesCupEntity>()

        // Winner (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 1,
            prizeMoney = championPrize,
            percentageOfChampion = 100.0,
            teamsAtStage = 1,
            description = "World Champion - Final Stage Prize",
            paidAtStage = true
        ))

        // Runner-up (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 2,
            prizeMoney = (championPrize * 0.65).roundToInt(),  // 65% of champion
            percentageOfChampion = 65.0,
            teamsAtStage = 1,
            description = "Runner-up - Final Stage Prize",
            paidAtStage = true
        ))

        // Third place (Third Place Match)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "THIRD_PLACE",
            position = 3,
            prizeMoney = (championPrize * 0.5).roundToInt(),  // 50% of champion
            percentageOfChampion = 50.0,
            teamsAtStage = 1,
            description = "Third Place - Third Place Match",
            paidAtStage = true
        ))

        // Fourth place (Third Place Match)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "THIRD_PLACE",
            position = 4,
            prizeMoney = (championPrize * 0.4).roundToInt(),  // 40% of champion
            percentageOfChampion = 40.0,
            teamsAtStage = 1,
            description = "Fourth Place - Third Place Match",
            paidAtStage = true
        ))

        // Quarter-finalists (4 teams) - Paid at Quarter-final Stage
        val quarterPrize = (championPrize * 0.2).roundToInt()  // 20% of champion each
        for (i in 1..4) {
            prizes.add(PrizesCupEntity(
                competitionId = cupId,
                stage = "QUARTER_FINAL",
                position = i,
                prizeMoney = quarterPrize,
                percentageOfChampion = 20.0,
                teamsAtStage = 4,
                description = "Quarter-finalist - Reaching Quarter-finals",
                paidAtStage = true
            ))
        }

        // Round of 16 participants - Paid at Round of 16 Stage
        if (numTeams >= 16) {
            val round16Prize = (championPrize * 0.1).roundToInt()  // 10% of champion each
            for (i in 1..8) {
                prizes.add(PrizesCupEntity(
                    competitionId = cupId,
                    stage = "ROUND_16",
                    position = i,
                    prizeMoney = round16Prize,
                    percentageOfChampion = 10.0,
                    teamsAtStage = 8,
                    description = "Round of 16 - Reaching Round of 16",
                    paidAtStage = true
                ))
            }
        }

        return prizes
    }

    private fun calculateNationalCupPrizes(
        championPrize: Int,
        numTeams: Int,
        cupId: Int,
        cupName: String
    ): List<PrizesCupEntity> {
        val prizes = mutableListOf<PrizesCupEntity>()

        // Winner (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 1,
            prizeMoney = championPrize,
            percentageOfChampion = 100.0,
            teamsAtStage = 1,
            description = "Cup Winner - Final Stage Prize",
            paidAtStage = true
        ))

        // Runner-up (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 2,
            prizeMoney = (championPrize * 0.5).roundToInt(),  // 50% of champion
            percentageOfChampion = 50.0,
            teamsAtStage = 1,
            description = "Cup Runner-up - Final Stage Prize",
            paidAtStage = true
        ))

        // Semi-finalists (2 teams) - Paid at Semi-final Stage
        val semiPrize = (championPrize * 0.25).roundToInt()  // 25% of champion each
        for (i in 1..2) {
            prizes.add(PrizesCupEntity(
                competitionId = cupId,
                stage = "SEMI_FINAL",
                position = i,
                prizeMoney = semiPrize,
                percentageOfChampion = 25.0,
                teamsAtStage = 2,
                description = "Semi-finalist - Reaching Semi-finals",
                paidAtStage = true
            ))
        }

        // Quarter-finalists - Paid at Quarter-final Stage
        if (numTeams >= 8) {
            val quarterPrize = (championPrize * 0.12).roundToInt()  // 12% of champion each
            val quarterFinalists = (numTeams / 2).coerceAtMost(8)
            for (i in 1..quarterFinalists) {
                prizes.add(PrizesCupEntity(
                    competitionId = cupId,
                    stage = "QUARTER_FINAL",
                    position = i,
                    prizeMoney = quarterPrize,
                    percentageOfChampion = 12.0,
                    teamsAtStage = quarterFinalists,
                    description = "Quarter-finalist - Reaching Quarter-finals",
                    paidAtStage = true
                ))
            }
        }

        return prizes
    }

    private fun calculateStandardCupPrizes(
        championPrize: Int,
        numTeams: Int,
        cupId: Int,
        cupName: String
    ): List<PrizesCupEntity> {
        val prizes = mutableListOf<PrizesCupEntity>()

        // Winner (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 1,
            prizeMoney = championPrize,
            percentageOfChampion = 100.0,
            teamsAtStage = 1,
            description = "Winner - Final Stage Prize",
            paidAtStage = true
        ))

        // Runner-up (Final Stage)
        prizes.add(PrizesCupEntity(
            competitionId = cupId,
            stage = "FINAL",
            position = 2,
            prizeMoney = (championPrize * 0.55).roundToInt(),  // 55% of champion
            percentageOfChampion = 55.0,
            teamsAtStage = 1,
            description = "Runner-up - Final Stage Prize",
            paidAtStage = true
        ))

        // Semi-finalists - Paid at Semi-final Stage
        val semiPrize = (championPrize * 0.27).roundToInt()  // 27% of champion each
        for (i in 1..2) {
            prizes.add(PrizesCupEntity(
                competitionId = cupId,
                stage = "SEMI_FINAL",
                position = i,
                prizeMoney = semiPrize,
                percentageOfChampion = 27.0,
                teamsAtStage = 2,
                description = "Semi-finalist - Reaching Semi-finals",
                paidAtStage = true
            ))
        }

        return prizes
    }

    // ============ CUP PRIZE AWARDING - STAGE BY STAGE ============

    /**
     * Award cup prizes at the completion of a specific stage
     * Called immediately after each round finishes
     */
    suspend fun awardCupStagePrizes(
        cupId: Int,
        stage: String,
        advancingTeams: List<String>,  // Teams that reached this stage
        allTeamsAtStage: List<String>? = null  // All teams that participated in this stage
    ) {
        val prizes = prizesCupDao.getPrizesByCompetition(cupId).firstOrNull() ?: return
        val cup = cupsDao.getById(cupId) ?: return

        println("\n🏆 CUP STAGE PRIZES - ${cup.name} - ${stage.replace('_', ' ')}")
        println("==========================================")

        // Get prizes for this specific stage
        val stagePrizes = prizes.filter { it.stage == stage && it.paidAtStage }

        if (stagePrizes.isEmpty()) {
            println("No prizes for this stage")
            return
        }

        when (stage) {
            "FINAL" -> {
                // Winner (position 1) and Runner-up (position 2)
                val winnerPrize = stagePrizes.find { it.position == 1 }
                val runnerUpPrize = stagePrizes.find { it.position == 2 }

                if (advancingTeams.size >= 2) {
                    winnerPrize?.let {
                        println("🏆 WINNER: ${advancingTeams[0]} receives ${it.prizeMoney} - ${it.description}")
                        // Award to winner
                    }
                    runnerUpPrize?.let {
                        println("🥈 RUNNER-UP: ${advancingTeams[1]} receives ${it.prizeMoney} - ${it.description}")
                        // Award to runner-up
                    }
                }
            }

            "THIRD_PLACE" -> {
                // Third and fourth place
                val thirdPrize = stagePrizes.find { it.position == 3 }
                val fourthPrize = stagePrizes.find { it.position == 4 }

                if (advancingTeams.size >= 2) {
                    thirdPrize?.let {
                        println("🥉 THIRD PLACE: ${advancingTeams[0]} receives ${it.prizeMoney} - ${it.description}")
                    }
                    fourthPrize?.let {
                        println("4th PLACE: ${advancingTeams[1]} receives ${it.prizeMoney} - ${it.description}")
                    }
                }
            }

            "SEMI_FINAL", "QUARTER_FINAL", "ROUND_16", "GROUP_STAGE" -> {
                // All teams that reached this stage get prize money
                val teams = allTeamsAtStage ?: advancingTeams

                for ((index, team) in teams.withIndex()) {
                    val prize = stagePrizes.find { it.position == index + 1 }
                    prize?.let {
                        println("${team} receives ${it.prizeMoney} - ${it.description}")
                    }
                }
            }
        }
    }

    /**
     * Award prize for reaching a specific stage
     * Called when teams qualify for the next round
     */
    suspend fun awardStageQualificationPrize(
        cupId: Int,
        stage: String,
        teamName: String,
        position: Int = 1
    ) {
        val prize = prizesCupDao.getPrizeByCompetitionStageAndPosition(cupId, stage, position)
        prize?.let {
            println("${teamName} receives ${it.prizeMoney} for reaching the ${stage.replace('_', ' ')}!")
            // Award prize money to team
            // This would update team's finances
        }
    }

    // ============ LEAGUE TEAM COUNT HELPERS ============

    /**
     * Get actual number of teams in a league
     */
    private suspend fun getLeagueTeamCount(leagueName: String): Int {
        return teamsDao.getTeamsByLeague(leagueName).firstOrNull()?.size ?: 0
    }

    /**
     * Update prize distribution for a specific league (call when league size changes)
     */
    suspend fun refreshLeaguePrizes(leagueId: Int) {
        val league = leaguesDao.getById(leagueId) ?: return
        val numTeams = getLeagueTeamCount(league.name)

        if (numTeams > 0) {
            val prizes = calculateLeaguePrizeDistribution(
                championPrize = league.prizeMoney,
                numTeams = numTeams,
                leagueId = leagueId
            )

            prizesLeaguesDao.deleteByCompetition(leagueId)
            prizesLeaguesDao.insertAll(prizes)
        }
    }

    // ============ UTILITY FUNCTIONS ============

    /**
     * Get total prize pool for a league
     */
    suspend fun getLeagueTotalPrizePool(leagueId: Int): Int? {
        return prizesLeaguesDao.getTotalPrizePool(leagueId)
    }

    /**
     * Get total prize pool for a cup
     */
    suspend fun getCupTotalPrizePool(cupId: Int): Int? {
        return prizesCupDao.getTotalPrizePool(cupId)
    }

    /**
     * Get champion's prize for a league
     */
    suspend fun getLeagueChampionPrize(leagueId: Int): Int? {
        return prizesLeaguesDao.getWinnerPrize(leagueId)
    }

    /**
     * Get winner's prize for a cup
     */
    suspend fun getCupWinnerPrize(cupId: Int): Int? {
        return prizesCupDao.getWinnerPrize(cupId)
    }

    /**
     * Get prize for reaching a specific cup stage
     */
    suspend fun getCupStagePrize(cupId: Int, stage: String, position: Int = 1): Int? {
        return prizesCupDao.getPrizeByCompetitionStageAndPosition(cupId, stage, position)?.prizeMoney
    }

    // ============ BULK INITIALIZATION ============

    /**
     * Initialize all prizes for all competitions
     */
    suspend fun initializeAllPrizes() {
        initializeLeaguePrizes()
        initializeCupPrizes()
    }

    // ============ DASHBOARD ============

    suspend fun getPrizesDashboard(): PrizesDashboard {
        val leagueStats = prizesLeaguesDao.getLeaguePrizeStatistics().firstOrNull() ?: emptyList()
        val cupStats = prizesCupDao.getCupPrizeStatistics().firstOrNull() ?: emptyList()

        val totalLeaguePrizes = leagueStats.sumOf { it.totalPool }
        val totalCupPrizes = cupStats.sumOf { it.totalPool }

        val richestLeague = leagueStats.maxByOrNull { it.totalPool }
        val richestCup = cupStats.maxByOrNull { it.totalPool }

        return PrizesDashboard(
            totalLeaguePrizes = totalLeaguePrizes,
            totalCupPrizes = totalCupPrizes,
            totalPrizeMoney = totalLeaguePrizes + totalCupPrizes,
            numberOfLeaguesWithPrizes = leagueStats.size,
            numberOfCupsWithPrizes = cupStats.size,
            richestLeague = richestLeague?.let {
                leaguesDao.getById(it.competitionId)?.name to it.totalPool
            },
            richestCup = richestCup?.let {
                cupsDao.getById(it.competitionId)?.name to it.totalPool
            }
        )
    }
}

// ============ DATA CLASSES ============

/**
 * Dashboard data for prize statistics across all competitions
 */
data class PrizesDashboard(
    val totalLeaguePrizes: Int,
    val totalCupPrizes: Int,
    val totalPrizeMoney: Int,
    val numberOfLeaguesWithPrizes: Int,
    val numberOfCupsWithPrizes: Int,
    val richestLeague: Pair<String?, Int>?,
    val richestCup: Pair<String?, Int>?
)

/**
 * League prize statistics from database queries
 */
data class LeaguePrizeStats(
    @param:Json(name = "competition_id")
    @field:ColumnInfo(name = "competition_id")
    val competitionId: Int,

    @param:Json(name = "prize_positions")
    @field:ColumnInfo(name = "prize_positions")
    val prizePositions: Int,

    @param:Json(name = "total_pool")
    @field:ColumnInfo(name = "total_pool")
    val totalPool: Int,

    @param:Json(name = "max_prize")
    @field:ColumnInfo(name = "max_prize")
    val maxPrize: Int,

    @param:Json(name = "min_prize")
    @field:ColumnInfo(name = "min_prize")
    val minPrize: Int,

    @param:Json(name = "avg_prize")
    @field:ColumnInfo(name = "avg_prize")
    val averagePrize: Double
)

/**
 * Cup prize statistics from database queries
 */
data class CupPrizeStats(
    @param:Json(name = "competition_id")
    @field:ColumnInfo(name = "competition_id")
    val competitionId: Int,

    @param:Json(name = "prize_positions")
    @field:ColumnInfo(name = "prize_positions")
    val prizePositions: Int,

    @param:Json(name = "total_pool")
    @field:ColumnInfo(name = "total_pool")
    val totalPool: Int,

    @param:Json(name = "max_prize")
    @field:ColumnInfo(name = "max_prize")
    val maxPrize: Int,

    @param:Json(name = "min_prize")
    @field:ColumnInfo(name = "min_prize")
    val minPrize: Int
)