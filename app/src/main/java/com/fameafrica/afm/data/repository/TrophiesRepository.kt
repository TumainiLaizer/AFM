package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ClubTrophyStats
import com.fameafrica.afm.data.database.dao.ManagerTrophyStats
import com.fameafrica.afm.data.database.dao.TrophiesDao
import com.fameafrica.afm.data.database.dao.ManagersDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.dao.SeasonAwardsDao
import com.fameafrica.afm.data.database.dao.SeasonHistoryDao
import com.fameafrica.afm.data.database.dao.TrophyLevelDistribution
import com.fameafrica.afm.data.database.dao.TrophyTypeDistribution
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TrophiesRepository @Inject constructor(
    private val trophiesDaoProvider: Provider<TrophiesDao>,
    private val managersDaoProvider: Provider<ManagersDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val seasonAwardsDaoProvider: Provider<SeasonAwardsDao>,
    private val seasonHistoryDaoProvider: Provider<SeasonHistoryDao>
) {
    private val trophiesDao get() = trophiesDaoProvider.get()
    private val managersDao get() = managersDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    private val seasonAwardsDao get() = seasonAwardsDaoProvider.get()
    private val seasonHistoryDao get() = seasonHistoryDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllTrophies(): Flow<List<TrophiesEntity>> = trophiesDao.getAll()

    suspend fun getTrophyById(id: Int): TrophiesEntity? = trophiesDao.getById(id)

    suspend fun insertTrophy(trophy: TrophiesEntity) = trophiesDao.insert(trophy)

    suspend fun insertAllTrophies(trophies: List<TrophiesEntity>) = trophiesDao.insertAll(trophies)

    suspend fun updateTrophy(trophy: TrophiesEntity) = trophiesDao.update(trophy)

    suspend fun deleteTrophy(trophy: TrophiesEntity) = trophiesDao.delete(trophy)

    suspend fun deleteAllTrophies() = trophiesDao.deleteAll()

    suspend fun getTrophiesCount(): Int = trophiesDao.getCount()

    // ============ TROPHY CREATION ============

    /**
     * Award league title to champion
     */
    suspend fun awardLeagueTitle(
        managerId: Int,
        clubName: String,
        clubId: Int?,
        leagueName: String,
        leagueId: Int?,
        season: String,
        seasonYear: Int,
        points: Int,
        goalDifference: Int
    ): TrophiesEntity {

        val manager = managersDao.getById(managerId)
        val team = teamsDao.getByName(clubName)

        val trophy = TrophiesEntity(
            managerId = managerId,
            managerName = manager?.name,
            clubName = clubName,
            clubId = clubId ?: team?.id,
            trophyName = "$leagueName Champions",
            trophyType = TrophyType.LEAGUE_TITLE.value,
            competitionId = leagueId,
            competitionName = leagueName,
            competitionLevel = "Domestic",
            season = season,
            seasonYear = seasonYear,
            notes = "Won with $points points, GD: $goalDifference",
            iconPath = "trophies/league_title.webp",
            dateWon = getCurrentDate()
        )

        trophiesDao.insert(trophy)

        // Update manager's trophy count
        manager?.let { managersDao.update(it.winTrophy()) }

        return trophy
    }

    /**
     * Award cup title to winner
     */
    suspend fun awardCupTitle(
        managerId: Int,
        clubName: String,
        clubId: Int?,
        cupName: String,
        cupId: Int?,
        season: String,
        seasonYear: Int,
        opponent: String?,
        matchScore: String?,
        winType: String?,
        isContinental: Boolean = false
    ): TrophiesEntity {

        val manager = managersDao.getById(managerId)
        val team = teamsDao.getByName(clubName)
        val level = when {
            isContinental -> "Continental"
            cupName.contains("CAF") || cupName.contains("AFCON") -> "Continental"
            else -> "Domestic"
        }

        val trophy = TrophiesEntity(
            managerId = managerId,
            managerName = manager?.name,
            clubName = clubName,
            clubId = clubId ?: team?.id,
            trophyName = "$cupName Winner",
            trophyType = TrophyType.CUP_TITLE.value,
            competitionId = cupId,
            competitionName = cupName,
            competitionLevel = level,
            season = season,
            seasonYear = seasonYear,
            opponent = opponent,
            matchPlayed = matchScore,
            winType = winType,
            notes = "Cup victory",
            iconPath = if (isContinental) "trophies/continental_cup.webp" else "trophies/cup.webp",
            dateWon = getCurrentDate()
        )

        trophiesDao.insert(trophy)

        // Update manager's trophy count
        manager?.let { managersDao.update(it.winTrophy()) }

        return trophy
    }

    /**
     * Award continental title (CAF Champions League, Confederation Cup, etc.)
     */
    suspend fun awardContinentalTitle(
        managerId: Int,
        clubName: String,
        clubId: Int?,
        competitionName: String,
        competitionId: Int?,
        season: String,
        seasonYear: Int,
        opponent: String?,
        matchScore: String?
    ): TrophiesEntity {

        return awardCupTitle(
            managerId = managerId,
            clubName = clubName,
            clubId = clubId,
            cupName = competitionName,
            cupId = competitionId,
            season = season,
            seasonYear = seasonYear,
            opponent = opponent,
            matchScore = matchScore,
            winType = null,
            isContinental = true
        )
    }

    /**
     * Award super cup
     */
    suspend fun awardSuperCup(
        managerId: Int,
        clubName: String,
        clubId: Int?,
        superCupName: String,
        superCupId: Int?,
        season: String,
        seasonYear: Int,
        opponent: String,
        matchScore: String
    ): TrophiesEntity {

        val manager = managersDao.getById(managerId)
        val team = teamsDao.getByName(clubName)

        val trophy = TrophiesEntity(
            managerId = managerId,
            managerName = manager?.name,
            clubName = clubName,
            clubId = clubId ?: team?.id,
            trophyName = "$superCupName Winner",
            trophyType = TrophyType.SUPER_CUP.value,
            competitionId = superCupId,
            competitionName = superCupName,
            competitionLevel = "Domestic",
            season = season,
            seasonYear = seasonYear,
            opponent = opponent,
            matchPlayed = matchScore,
            notes = "Super Cup victory",
            iconPath = "trophies/super_cup.webp",
            dateWon = getCurrentDate()
        )

        trophiesDao.insert(trophy)

        // Update manager's trophy count
        manager?.let { managersDao.update(it.winTrophy()) }

        return trophy
    }

    /**
     * Award individual award (from season_awards)
     */
    suspend fun awardIndividualAward(
        managerId: Int,
        clubName: String,
        clubId: Int?,
        awardName: String,
        season: String,
        seasonYear: Int,
        seasonAwardId: Int
    ): TrophiesEntity? {

        val seasonAward = seasonAwardsDao.getById(seasonAwardId) ?: return null
        val manager = managersDao.getById(managerId)
        val team = teamsDao.getByName(clubName)

        val trophy = TrophiesEntity(
            managerId = managerId,
            managerName = manager?.name,
            clubName = clubName,
            clubId = clubId ?: team?.id,
            trophyName = awardName,
            trophyType = TrophyType.AWARD.value,
            competitionLevel = "Domestic",
            season = season,
            seasonYear = seasonYear,
            seasonAwardId = seasonAwardId,
            notes = seasonAward.description ?: "Individual award",
            iconPath = "trophies/award.webp",
            dateWon = getCurrentDate()
        )

        trophiesDao.insert(trophy)

        return trophy
    }

    // ============ END OF SEASON PROCESSING ============

    /**
     * Process all trophies at end of season from season_history
     */
    suspend fun processEndOfSeasonTrophies(season: String, seasonYear: Int) {
        val seasonHistories = seasonHistoryDao.getSeasonStandings(season).firstOrNull() ?: return

        // Award league titles to position 1 teams
        val champions = seasonHistories.filter { it.position == 1 }
        champions.forEach { history ->
            history.teamId?.let { teamId ->
                val team = teamsDao.getById(teamId)
                val managerId = team?.managerId

                if (managerId != null) {
                    awardLeagueTitle(
                        managerId = managerId,
                        clubName = history.teamName,
                        clubId = teamId,
                        leagueName = history.leagueName ?: "League",
                        leagueId = null,
                        season = season,
                        seasonYear = seasonYear,
                        points = history.points ?: 0,
                        goalDifference = history.goalDifference ?: 0
                    )
                }
            }
        }
    }

    // ============ MANAGER-BASED ============

    fun getTrophiesByManager(managerId: Int): Flow<List<TrophiesEntity>> =
        trophiesDao.getTrophiesByManager(managerId)

    suspend fun getTrophyCountByManager(managerId: Int): Int =
        trophiesDao.getTrophyCountByManager(managerId)

    suspend fun getManagerTrophyBreakdown(managerId: Int): ManagerTrophyBreakdown {
        val leagueTitles = trophiesDao.getLeagueTitlesByManager(managerId)
        val cupTitles = trophiesDao.getCupTitlesByManager(managerId)
        val continentalTitles = trophiesDao.getContinentalTitlesByManager(managerId)
        val superCups = trophiesDao.getSuperCupsByManager(managerId)
        val total = leagueTitles + cupTitles + continentalTitles + superCups

        return ManagerTrophyBreakdown(
            managerId = managerId,
            totalTrophies = total,
            leagueTitles = leagueTitles,
            cupTitles = cupTitles,
            continentalTitles = continentalTitles,
            superCups = superCups
        )
    }

    // ============ CLUB-BASED ============

    fun getTrophiesByClub(clubId: Int): Flow<List<TrophiesEntity>> =
        trophiesDao.getTrophiesByClubId(clubId)

    suspend fun getTrophyCountByClub(clubId: Int): Int =
        trophiesDao.getTrophyCountByClubId(clubId)

    suspend fun getClubTrophyBreakdown(clubId: Int): ClubTrophyBreakdown {
        val leagueTitles = trophiesDao.getLeagueTitlesByClubId(clubId)
        val cupTitles = trophiesDao.getCupTitlesByClubId(clubId)
        val continentalTitles = trophiesDao.getContinentalTitlesByClubId(clubId)
        val total = leagueTitles + cupTitles + continentalTitles

        return ClubTrophyBreakdown(
            clubName = clubId.toString(),
            totalTrophies = total,
            leagueTitles = leagueTitles,
            cupTitles = cupTitles,
            continentalTitles = continentalTitles
        )
    }

    // ============ STATISTICS ============

    fun getManagerTrophyRankings(): Flow<List<ManagerTrophyStats>> =
        trophiesDao.getManagerTrophyRankings()

    fun getClubTrophyRankings(): Flow<List<ClubTrophyStats>> =
        trophiesDao.getClubTrophyRankings()

    fun getTrophiesByLevelDistribution(): Flow<List<TrophyLevelDistribution>> =
        trophiesDao.getTrophiesByLevelDistribution()

    fun getTrophyTypeDistribution(): Flow<List<TrophyTypeDistribution>> =
        trophiesDao.getTrophyTypeDistribution()

    fun getRecentTrophies(limit: Int = 10): Flow<List<TrophiesEntity>> =
        trophiesDao.getRecentTrophies(limit)

    // ============ COMPETITION-BASED ============

    fun getTrophiesByCompetition(competitionName: String): Flow<List<TrophiesEntity>> =
        trophiesDao.getTrophiesByCompetition(competitionName)

    // ============ SEASON INTEGRATION ============

    /**
     * Link trophy to season history
     */
    suspend fun linkToSeasonHistory(trophyId: Int, seasonHistoryId: Int): TrophiesEntity? {
        val trophy = trophiesDao.getById(trophyId) ?: return null
        val updated = trophy.copy(seasonHistoryId = seasonHistoryId)
        trophiesDao.update(updated)
        return updated
    }

    /**
     * Get all trophies for a specific season
     */
    fun getTrophiesBySeason(season: String): Flow<List<TrophiesEntity>> =
        trophiesDao.getTrophiesBySeason(season)

    // ============ DASHBOARD ============

    suspend fun getTrophiesDashboard(): TrophiesDashboard {
        val allTrophies = trophiesDao.getAll().firstOrNull() ?: emptyList()
        val recentTrophies = allTrophies.sortedByDescending { it.season }.take(10)

        val managerRankings = trophiesDao.getManagerTrophyRankings().firstOrNull() ?: emptyList()
        val clubRankings = trophiesDao.getClubTrophyRankings().firstOrNull() ?: emptyList()

        val leagueTitles = allTrophies.count { it.isLeagueTitle }
        val cupTitles = allTrophies.count { it.isCupTitle }
        val continentalTitles = allTrophies.count { it.isContinentalTitle }
        val superCups = allTrophies.count { it.isSuperCup }
        val individualAwards = allTrophies.count { it.isIndividualAward }

        val seasons = allTrophies.map { it.season }.distinct().sortedDescending()

        val topManager = managerRankings.firstOrNull()
        val topClub = clubRankings.firstOrNull()

        return TrophiesDashboard(
            totalTrophies = allTrophies.size,
            leagueTitles = leagueTitles,
            cupTitles = cupTitles,
            continentalTitles = continentalTitles,
            superCups = superCups,
            individualAwards = individualAwards,
            seasonsWithTrophies = seasons.size,
            mostRecentTrophy = recentTrophies.firstOrNull(),
            recentTrophies = recentTrophies,
            topManager = topManager,
            topClub = topClub,
            managerRankings = managerRankings,
            clubRankings = clubRankings
        )
    }

    suspend fun getClubTrophyRoom(clubId: Int, clubName: String): ClubTrophyRoom {
        val trophies = trophiesDao.getTrophiesByClubId(clubId).firstOrNull() ?: emptyList()
        val breakdown = getClubTrophyBreakdown(clubId)

        val bySeason = trophies.groupBy { it.season }
            .map { (season, seasonTrophies) ->
                season to seasonTrophies
            }
            .sortedByDescending { it.first }

        val leagueTrophies = trophies.filter { it.isLeagueTitle }
        val cupTrophies = trophies.filter { it.isCupTitle }
        val continentalTrophies = trophies.filter { it.isContinentalTitle }
        val superCups = trophies.filter { it.isSuperCup }

        val mostRecent = trophies.maxByOrNull { it.season }
        val firstTrophy = trophies.minByOrNull { it.season }

        return ClubTrophyRoom(
            clubName = clubName,
            totalTrophies = trophies.size,
            leagueTitles = breakdown.leagueTitles,
            cupTitles = breakdown.cupTitles,
            continentalTitles = breakdown.continentalTitles,
            leagueTrophies = leagueTrophies,
            cupTrophies = cupTrophies,
            continentalTrophies = continentalTrophies,
            superCups = superCups,
            trophiesBySeason = bySeason,
            mostRecentTrophy = mostRecent,
            firstTrophy = firstTrophy,
            allTrophies = trophies
        )
    }

    // ============ UTILITY ============

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

// ============ DATA CLASSES ============

data class ManagerTrophyBreakdown(
    val managerId: Int,
    val totalTrophies: Int,
    val leagueTitles: Int,
    val cupTitles: Int,
    val continentalTitles: Int,
    val superCups: Int
)

data class ClubTrophyBreakdown(
    val clubName: String,
    val totalTrophies: Int,
    val leagueTitles: Int,
    val cupTitles: Int,
    val continentalTitles: Int
)

data class TrophiesDashboard(
    val totalTrophies: Int,
    val leagueTitles: Int,
    val cupTitles: Int,
    val continentalTitles: Int,
    val superCups: Int,
    val individualAwards: Int,
    val seasonsWithTrophies: Int,
    val mostRecentTrophy: TrophiesEntity?,
    val recentTrophies: List<TrophiesEntity>,
    val topManager: ManagerTrophyStats?,
    val topClub: ClubTrophyStats?,
    val managerRankings: List<ManagerTrophyStats>,
    val clubRankings: List<ClubTrophyStats>
)

data class ClubTrophyRoom(
    val clubName: String,
    val totalTrophies: Int,
    val leagueTitles: Int,
    val cupTitles: Int,
    val continentalTitles: Int,
    val leagueTrophies: List<TrophiesEntity>,
    val cupTrophies: List<TrophiesEntity>,
    val continentalTrophies: List<TrophiesEntity>,
    val superCups: List<TrophiesEntity>,
    val trophiesBySeason: List<Pair<String, List<TrophiesEntity>>>,
    val mostRecentTrophy: TrophiesEntity?,
    val firstTrophy: TrophiesEntity?,
    val allTrophies: List<TrophiesEntity>
)
