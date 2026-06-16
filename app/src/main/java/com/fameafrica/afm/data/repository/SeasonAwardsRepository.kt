package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.MostAwardedPlayer
import com.fameafrica.afm.data.database.dao.SeasonAwardsDao
import com.fameafrica.afm.data.database.dao.PlayersDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.entities.SeasonAwardsEntity
import com.fameafrica.afm.data.database.entities.AwardType
import com.fameafrica.afm.data.database.entities.AwardCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SeasonAwardsRepository @Inject constructor(
    private val seasonAwardsDaoProvider: Provider<SeasonAwardsDao>,
    private val playersDaoProvider: Provider<PlayersDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val managersDaoProvider: Provider<com.fameafrica.afm.data.database.dao.ManagersDao>,
    private val fixturesDaoProvider: Provider<com.fameafrica.afm.data.database.dao.FixturesDao>,
    private val matchEventsDaoProvider: Provider<com.fameafrica.afm.data.database.dao.MatchEventsDao>,
    private val trophiesRepository: TrophiesRepository
) {
    private val seasonAwardsDao get() = seasonAwardsDaoProvider.get()
    private val playersDao get() = playersDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    private val managersDao get() = managersDaoProvider.get()
    private val fixturesDao get() = fixturesDaoProvider.get()
    private val matchEventsDao get() = matchEventsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllAwards(): Flow<List<SeasonAwardsEntity>> = seasonAwardsDao.getAll()

    suspend fun getAwardById(id: Int): SeasonAwardsEntity? = seasonAwardsDao.getById(id)

    suspend fun getAward(season: String, awardType: String): SeasonAwardsEntity? =
        seasonAwardsDao.getAward(season, awardType)

    suspend fun insertAward(award: SeasonAwardsEntity) = seasonAwardsDao.insert(award)

    suspend fun updateAward(award: SeasonAwardsEntity) = seasonAwardsDao.update(award)

    suspend fun deleteAward(award: SeasonAwardsEntity) = seasonAwardsDao.delete(award)

    // ============ END OF SEASON AWARDS GENERATION ============

    suspend fun generateMonthlyAwards(season: String, month: Int) {
        val year = season.split("/").first().toIntOrNull() ?: 2025
        val monthStr = month.toString().padStart(2, '0')
        
        // Find top monthly performers using MatchEvents
        val topPerformers = matchEventsDao.getTopPerformersByMonth(season, monthStr, 1)
        val playerOfMonth = topPerformers.firstOrNull()
        
        if (playerOfMonth != null) {
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = year,
                awardType = "PLAYER_OF_THE_MONTH",
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerOfMonth.playerId,
                playerName = playerOfMonth.playerName,
                teamId = playerOfMonth.teamId,
                teamName = playerOfMonth.teamName,
                description = "Player of the Month for month $month ($monthStr) based on ${playerOfMonth.goals} goals and ${playerOfMonth.assists} assists.",
                prizeMoney = 100000
            )
            seasonAwardsDao.insert(award)
            
            // Create trophy/history entry
            val player = playersDao.getById(playerOfMonth.playerId)
            createTrophyFromAward(award, player)
        }

        // Manager of the Month Logic
        val monthFixtures = fixturesDao.getCompletedFixturesByMonth(season, monthStr)
        if (monthFixtures.isNotEmpty()) {
            val teamStats = mutableMapOf<Int, Triple<Int, Int, String>>() // teamId -> (points, matches, teamName)
            
            monthFixtures.forEach { fixture ->
                val homeId = fixture.homeTeamId
                val awayId = fixture.awayTeamId
                
                val (homePoints, awayPoints) = when {
                    fixture.homeScore > fixture.awayScore -> 3 to 0
                    fixture.awayScore > fixture.homeScore -> 0 to 3
                    else -> 1 to 1
                }
                
                val currentHome = teamStats.getOrDefault(homeId, Triple(0, 0, fixture.homeTeam))
                teamStats[homeId] = Triple(currentHome.first + homePoints, currentHome.second + 1, fixture.homeTeam)
                
                val currentAway = teamStats.getOrDefault(awayId, Triple(0, 0, fixture.awayTeam))
                teamStats[awayId] = Triple(currentAway.first + awayPoints, currentAway.second + 1, fixture.awayTeam)
            }
            
            // Calculate PPG and find best manager
            val topTeam = teamStats.filter { it.value.second >= 2 } // Min 2 matches
                .maxByOrNull { it.value.first.toDouble() / it.value.second }
                
            topTeam?.let { entry ->
                val teamId = entry.key
                val manager = managersDao.getManagerByTeam(teamId)
                if (manager != null) {
                    val ppg = entry.value.first.toDouble() / entry.value.second
                    val managerAward = SeasonAwardsEntity(
                        season = season,
                        seasonYear = year,
                        awardType = AwardType.COACH_OF_THE_MONTH.value,
                        awardCategory = AwardCategory.LEAGUE.value,
                        managerId = manager.id,
                        coachName = manager.name,
                        teamId = teamId,
                        teamName = entry.value.third,
                        description = "Manager of the Month for month $month ($monthStr) with ${String.format(Locale.getDefault(), "%.2f", ppg)} PPG.",
                        prizeMoney = 150000
                    )
                    val idLong = seasonAwardsDao.insert(managerAward)
                    
                    // Increment Manager Awards
                    val updatedManager = manager.earnAward(AwardType.COACH_OF_THE_MONTH.value)
                    managersDao.update(updatedManager)
                    
                    // Award trophy to manager
                    trophiesRepository.awardIndividualAward(
                        managerId = manager.id,
                        clubName = entry.value.third,
                        clubId = teamId,
                        awardName = "Manager of the Month - Month $month",
                        season = season,
                        seasonYear = year,
                        seasonAwardId = idLong.toInt()
                    )
                }
            }
        }
    }

    suspend fun generateEndOfSeasonAwards(season: String) {
        val year = season.split("/").first().toIntOrNull() ?: 2025
        val allPlayers = playersDao.getAll().firstOrNull() ?: return
        
        // Find top scorer
        val topScorer = allPlayers.maxByOrNull { it.goals }
        if (topScorer != null && topScorer.goals > 0) {
            generateSeasonAwards(
                season = season,
                seasonYear = year,
                leagueName = "League",
                topScorers = listOf(topScorer.id to topScorer.name),
                topAssisters = emptyList(),
                topRatedPlayers = emptyList(),
                bestGoalkeeper = null,
                bestDefender = null,
                bestMidfielder = null,
                bestForward = null,
                youngPlayer = null
            )
        }
    }

    /**
     * Generate all season awards based on player statistics
     * Automatically creates trophy entries for individual awards
     */
    suspend fun generateSeasonAwards(
        season: String,
        seasonYear: Int,
        leagueName: String,
        topScorers: List<Pair<Int, String>>, // playerId, playerName
        topAssisters: List<Pair<Int, String>>,
        topRatedPlayers: List<Pair<Int, String>>,
        bestGoalkeeper: Pair<Int, String>?,
        bestDefender: Pair<Int, String>?,
        bestMidfielder: Pair<Int, String>?,
        bestForward: Pair<Int, String>?,
        youngPlayer: Pair<Int, String>?,
        coachOfSeason: Pair<Int, String>? = null  // Added coach ID
    ): List<SeasonAwardsEntity> {

        val awards = mutableListOf<SeasonAwardsEntity>()

        // Top Scorer
        if (topScorers.isNotEmpty()) {
            val (playerId, playerName) = topScorers.first()
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.TOP_SCORER.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                goals = topScorers.firstOrNull()?.let {
                    playersDao.getById(it.first)?.goals ?: 0
                },
                description = "Top Scorer of the $season season with ${player?.goals ?: 0} goals",
                prizeMoney = calculateAwardPrize(leagueName, "TOP_SCORER")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Best Assister
        if (topAssisters.isNotEmpty()) {
            val (playerId, playerName) = topAssisters.first()
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.BEST_ASSISTER.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                assists = player?.assists ?: 0,
                description = "Best Assister of the $season season with ${player?.assists ?: 0} assists",
                prizeMoney = calculateAwardPrize(leagueName, "BEST_ASSISTER")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Player of the Season (top rated)
        if (topRatedPlayers.isNotEmpty()) {
            val (playerId, playerName) = topRatedPlayers.first()
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.PLAYER_OF_THE_SEASON.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                rating = player?.overallRating?.toDouble() ?: 0.0,
                description = "Player of the Season $season with rating ${player?.overallRating ?: 0}",
                prizeMoney = calculateAwardPrize(leagueName, "PLAYER_OF_THE_SEASON")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Best Goalkeeper
        bestGoalkeeper?.let { (playerId, playerName) ->
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.BEST_GOALKEEPER.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                cleanSheets = player?.cleanSheets,
                description = "Best Goalkeeper of the $season season with ${player?.cleanSheets ?: 0} clean sheets",
                prizeMoney = calculateAwardPrize(leagueName, "BEST_GOALKEEPER")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Best Defender
        bestDefender?.let { (playerId, playerName) ->
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.BEST_DEFENDER.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                description = "Best Defender of the $season season",
                prizeMoney = calculateAwardPrize(leagueName, "BEST_DEFENDER")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Best Midfielder
        bestMidfielder?.let { (playerId, playerName) ->
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.BEST_MIDFIELDER.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                assists = player?.assists,
                description = "Best Midfielder of the $season season",
                prizeMoney = calculateAwardPrize(leagueName, "BEST_MIDFIELDER")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Best Forward
        bestForward?.let { (playerId, playerName) ->
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.BEST_FORWARD.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                goals = player?.goals,
                description = "Best Forward of the $season season with ${player?.goals ?: 0} goals",
                prizeMoney = calculateAwardPrize(leagueName, "BEST_FORWARD")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Young Player
        youngPlayer?.let { (playerId, playerName) ->
            val player = playersDao.getById(playerId)
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.YOUNG_PLAYER.value,
                awardCategory = AwardCategory.LEAGUE.value,
                playerId = playerId,
                playerName = playerName,
                teamId = player?.teamId,
                teamName = player?.teamName,
                leagueName = leagueName,
                description = "Young Player of the Season $season (Under 21)",
                prizeMoney = calculateAwardPrize(leagueName, "YOUNG_PLAYER")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Create trophy entry for individual award
            createTrophyFromAward(insertedAward, player)
        }

        // Coach of the Season
        coachOfSeason?.let { (managerId, coachName) ->
            val award = SeasonAwardsEntity(
                season = season,
                seasonYear = seasonYear,
                awardType = AwardType.COACH_OF_THE_SEASON.value,
                awardCategory = AwardCategory.LEAGUE.value,
                coachName = coachName,
                managerId = managerId,
                leagueName = leagueName,
                description = "Coach of the Season $season",
                prizeMoney = calculateAwardPrize(leagueName, "COACH_OF_THE_SEASON")
            )
            val idLong = seasonAwardsDao.insert(award)
            val insertedAward = award.copy(id = idLong.toInt())
            awards.add(insertedAward)

            // Increment Manager Awards
            managersDao.getById(managerId)?.let { manager ->
                val updatedManager = manager.earnAward("COACH_OF_THE_SEASON")
                managersDao.update(updatedManager)
            }

            // Create trophy entry for coach award
            trophiesRepository.awardIndividualAward(
                managerId = managerId,
                clubName = "",  // Coach might not have a club at time of award
                clubId = null,
                awardName = "Coach of the Season $season - $leagueName",
                season = season,
                seasonYear = seasonYear,
                seasonAwardId = insertedAward.id
            )
        }

        return awards
    }

    /**
     * Create trophy entry from individual award
     */
    private suspend fun createTrophyFromAward(
        award: SeasonAwardsEntity,
        player: com.fameafrica.afm.data.database.entities.PlayersEntity?
    ) {
        if (award.playerId != null && player != null) {
            // Get team manager if available
            val team = player.teamId?.let { teamsDao.getById(it) }
            val managerId = team?.managerId ?: 0

            trophiesRepository.awardIndividualAward(
                managerId = managerId,
                clubName = player.teamName,
                clubId = player.teamId,
                awardName = award.awardDisplay,
                season = award.season,
                seasonYear = award.seasonYear,
                seasonAwardId = award.id
            )
        }
    }

    /**
     * Calculate prize money for awards based on league prestige
     */
    private fun calculateAwardPrize(leagueName: String, awardType: String): Int {
        return when {
            leagueName.contains("Premier") && leagueName.contains("Tanzania") -> 5000000
            leagueName.contains("Egyptian") -> 10000000
            leagueName.contains("South African") -> 8000000
            leagueName.contains("Morocco") -> 7000000
            leagueName.contains("Kenya") -> 4000000
            else -> 2000000
        }
    }

    // ============ QUERIES ============

    fun getAwardsBySeason(season: String): Flow<List<SeasonAwardsEntity>> =
        seasonAwardsDao.getAwardsBySeason(season)

    fun getAwardsBySeasonAndCategory(season: String, category: String): Flow<List<SeasonAwardsEntity>> =
        seasonAwardsDao.getAwardsBySeasonAndCategory(season, category)

    fun getPlayerAwards(playerId: Int): Flow<List<SeasonAwardsEntity>> =
        seasonAwardsDao.getPlayerAwards(playerId)

    fun getTeamAwards(teamId: Int): Flow<List<SeasonAwardsEntity>> =
        seasonAwardsDao.getTeamAwards(teamId)

    fun getCoachAwards(coachName: String): Flow<List<SeasonAwardsEntity>> =
        seasonAwardsDao.getCoachAwards(coachName)

    fun getMostAwardedPlayers(limit: Int): Flow<List<MostAwardedPlayer>> =
        seasonAwardsDao.getMostAwardedPlayers(limit)

    fun getAwardWinnersByType(): Flow<List<com.fameafrica.afm.data.database.dao.AwardWinnersByType>> =
        seasonAwardsDao.getAwardWinnersByType()

    // ============ DASHBOARD ============

    suspend fun getSeasonAwardsDashboard(season: String): SeasonAwardsDashboard {
        val awards = seasonAwardsDao.getAwardsBySeason(season).firstOrNull() ?: emptyList()

        val playerOfSeason = awards.find { it.awardType == AwardType.PLAYER_OF_THE_SEASON.value }
        val topScorer = awards.find { it.awardType == AwardType.TOP_SCORER.value }
        val bestAssister = awards.find { it.awardType == AwardType.BEST_ASSISTER.value }
        val bestGoalkeeper = awards.find { it.awardType == AwardType.BEST_GOALKEEPER.value }
        val bestDefender = awards.find { it.awardType == AwardType.BEST_DEFENDER.value }
        val bestMidfielder = awards.find { it.awardType == AwardType.BEST_MIDFIELDER.value }
        val bestForward = awards.find { it.awardType == AwardType.BEST_FORWARD.value }
        val youngPlayer = awards.find { it.awardType == AwardType.YOUNG_PLAYER.value }
        val coachOfSeason = awards.find { it.awardType == AwardType.COACH_OF_THE_SEASON.value }

        val awardsByType = awards.groupBy { it.awardType }
            .mapValues { it.value.size }

        return SeasonAwardsDashboard(
            season = season,
            playerOfSeason = playerOfSeason,
            topScorer = topScorer,
            bestAssister = bestAssister,
            bestGoalkeeper = bestGoalkeeper,
            bestDefender = bestDefender,
            bestMidfielder = bestMidfielder,
            bestForward = bestForward,
            youngPlayer = youngPlayer,
            coachOfSeason = coachOfSeason,
            awardsByType = awardsByType,
            totalAwards = awards.size,
            allAwards = awards
        )
    }

    suspend fun getPlayerAwardsDashboard(playerId: Int): PlayerAwardsDashboard {
        val awards = seasonAwardsDao.getPlayerAwards(playerId).firstOrNull() ?: emptyList()
        val player = playersDao.getById(playerId)

        val awardsByType = awards.groupBy { it.awardType }
            .mapValues { it.value.size }

        val seasons = awards.map { it.season }.distinct()

        return PlayerAwardsDashboard(
            playerId = playerId,
            playerName = player?.name ?: "Unknown",
            totalAwards = awards.size,
            awardsByType = awardsByType,
            seasonsWithAwards = seasons,
            mostRecentAward = awards.firstOrNull(),
            allAwards = awards
        )
    }
}

// ============ DATA CLASSES ============

data class SeasonAwardsDashboard(
    val season: String,
    val playerOfSeason: SeasonAwardsEntity?,
    val topScorer: SeasonAwardsEntity?,
    val bestAssister: SeasonAwardsEntity?,
    val bestGoalkeeper: SeasonAwardsEntity?,
    val bestDefender: SeasonAwardsEntity?,
    val bestMidfielder: SeasonAwardsEntity?,
    val bestForward: SeasonAwardsEntity?,
    val youngPlayer: SeasonAwardsEntity?,
    val coachOfSeason: SeasonAwardsEntity?,
    val awardsByType: Map<String, Int>,
    val totalAwards: Int,
    val allAwards: List<SeasonAwardsEntity>
)

data class PlayerAwardsDashboard(
    val playerId: Int,
    val playerName: String,
    val totalAwards: Int,
    val awardsByType: Map<String, Int>,
    val seasonsWithAwards: List<String>,
    val mostRecentAward: SeasonAwardsEntity?,
    val allAwards: List<SeasonAwardsEntity>
)
