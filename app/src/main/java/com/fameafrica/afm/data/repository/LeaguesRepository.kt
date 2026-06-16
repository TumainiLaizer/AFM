package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.CountryLeagueStatistics
import com.fameafrica.afm.data.database.dao.LeagueLevelStatistics
import com.fameafrica.afm.data.database.dao.LeagueWithCountry
import com.fameafrica.afm.data.database.dao.LeagueWithDetails
import com.fameafrica.afm.data.database.dao.LeaguesDao
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.ForeignPlayerRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class LeaguesRepository @Inject constructor(
    private val leaguesDaoProvider: Provider<LeaguesDao>
) {
    private val leaguesDao get() = leaguesDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllLeagues(): Flow<List<LeaguesEntity>> = leaguesDao.getAll()

    suspend fun getAllLeaguesSync(): List<LeaguesEntity> = leaguesDao.getAllList()

    suspend fun getLeagueById(id: Int): LeaguesEntity? = leaguesDao.getById(id)

    suspend fun getLeagueByName(name: String): LeaguesEntity? = leaguesDao.getByName(name)

    suspend fun insertLeague(league: LeaguesEntity) = leaguesDao.insert(league)

    suspend fun insertAllLeagues(leagues: List<LeaguesEntity>) = leaguesDao.insertAll(leagues)

    suspend fun updateLeague(league: LeaguesEntity) = leaguesDao.update(league)

    suspend fun deleteLeague(league: LeaguesEntity) = leaguesDao.delete(league)

    suspend fun getLeaguesCount(): Int = leaguesDao.getCount()

    // ============ COUNTRY-BASED ============

    fun getLeaguesByCountry(countryId: Int): Flow<List<LeaguesEntity>> =
        leaguesDao.getLeaguesByCountry(countryId)

    fun getLeaguesByCountryName(countryName: String): Flow<List<LeaguesEntity>> =
        leaguesDao.getLeaguesByCountryName(countryName)

    suspend fun getTopDivisionByCountry(countryId: Int): LeaguesEntity? =
        leaguesDao.getTopDivisionByCountry(countryId)

    suspend fun getSecondDivisionByCountry(countryId: Int): LeaguesEntity? =
        leaguesDao.getSecondDivisionByCountry(countryId)

    suspend fun getTanzaniaLeagues(): List<LeaguesEntity> {
        return leaguesDao.getLeaguesByCountry(1).firstOrNull() ?: emptyList()
    }

    suspend fun getEgyptianLeagues(): List<LeaguesEntity> {
        return leaguesDao.getLeaguesByCountry(25).firstOrNull() ?: emptyList()
    }

    suspend fun getSouthAfricanLeagues(): List<LeaguesEntity> {
        return leaguesDao.getLeaguesByCountry(19).firstOrNull() ?: emptyList()
    }

    suspend fun getNigerianLeagues(): List<LeaguesEntity> {
        return leaguesDao.getLeaguesByCountry(12).firstOrNull() ?: emptyList()
    }

    fun getDistinctCountryIds(): Flow<List<Int>> = leaguesDao.getDistinctCountryIds()

    // ============ LEVEL-BASED ============

    fun getLeaguesByLevel(level: Int): Flow<List<LeaguesEntity>> =
        leaguesDao.getLeaguesByLevel(level)

    fun getTopDivisionLeagues(): Flow<List<LeaguesEntity>> =
        leaguesDao.getTopDivisionLeagues()

    fun getSecondDivisionLeagues(): Flow<List<LeaguesEntity>> =
        leaguesDao.getLeaguesByLevel(2)

    fun getRegionalLeagues(): Flow<List<LeaguesEntity>> =
        leaguesDao.getRegionalLeagues()

    suspend fun getAllTopDivisionLeaguesList(): List<LeaguesEntity> {
        return leaguesDao.getTopDivisionLeagues().firstOrNull() ?: emptyList()
    }

    // ============ PRIZE MONEY ============

    fun getHighValueLeagues(minPrize: Int): Flow<List<LeaguesEntity>> =
        leaguesDao.getHighValueLeagues(minPrize)

    fun getRichestLeagues(limit: Int): Flow<List<LeaguesEntity>> =
        leaguesDao.getRichestLeagues(limit)

    suspend fun getAveragePrizeByLevel(level: Int): Double? =
        leaguesDao.getAveragePrizeMoneyByLevel(level)

    suspend fun getTop10RichestLeagues(): List<LeaguesEntity> {
        return leaguesDao.getRichestLeagues(10).firstOrNull() ?: emptyList()
    }

    // ============ SPONSORS ============

    fun getLeaguesBySponsor(sponsorName: String): Flow<List<LeaguesEntity>> =
        leaguesDao.getLeaguesBySponsor(sponsorName)

    fun getLeaguesWithoutSponsor(): Flow<List<LeaguesEntity>> =
        leaguesDao.getLeaguesWithoutSponsor()

    fun getDistinctSponsors(): Flow<List<String>> = leaguesDao.getDistinctSponsors()

    // ============ SEARCH ============

    fun searchLeagues(searchQuery: String): Flow<List<LeaguesEntity>> =
        leaguesDao.searchLeagues(searchQuery)

    // ============ STATISTICS ============

    fun getLeagueLevelStatistics(): Flow<List<LeagueLevelStatistics>> =
        leaguesDao.getLeagueLevelStatistics()

    fun getCountryLeagueStatistics(): Flow<List<CountryLeagueStatistics>> =
        leaguesDao.getCountryLeagueStatistics()

    suspend fun getLeagueCountByCountry(countryId: Int): Int =
        leaguesDao.getLeagueCountByCountry(countryId)

    // ============ JOIN QUERIES ============

    suspend fun getLeagueWithDetails(leagueId: Int): LeagueWithDetails? =
        leaguesDao.getLeagueWithDetails(leagueId)

    fun getAllLeaguesWithCountries(): Flow<List<LeagueWithCountry>> =
        leaguesDao.getAllLeaguesWithCountries()

    // ============ FOREIGN PLAYER RULES ============

    /**
     * Get maximum foreign players allowed for a league
     */
    suspend fun getMaxForeignPlayersForLeague(leagueName: String): Int {
        val league = getLeagueByName(leagueName) ?: return 5
        return league.getMaxForeignPlayers()
    }

    /**
     * Get foreign player limit for a specific league by ID
     */
    suspend fun getMaxForeignPlayersForLeagueId(leagueId: Int): Int {
        val league = getLeagueById(leagueId) ?: return 5
        return league.getMaxForeignPlayers()
    }

    /**
     * Check if a player nationality is considered foreign in this league
     */
    suspend fun isPlayerForeignInLeague(playerNationality: String, leagueName: String): Boolean {
        val league = getLeagueByName(leagueName) ?: return true
        return ForeignPlayerRules.isPlayerForeign(
            playerNationality = playerNationality,
            leagueCountryId = league.countryId ?: 0
        )
    }

    // ============ LEAGUE HIERARCHY ============

    /**
     * Get the complete league pyramid for a country
     */
    suspend fun getLeaguePyramid(countryId: Int): LeaguePyramid {
        val leagues = leaguesDao.getLeaguesByCountry(countryId).firstOrNull() ?: emptyList()

        val topDivision = leagues.firstOrNull { it.level == 1 }
        val secondDivision = leagues.firstOrNull { it.level == 2 }
        val thirdDivision = leagues.firstOrNull { it.level == 3 }
        val fourthDivision = leagues.firstOrNull { it.level == 4 }
        val regionalLeagues = leagues.filter { it.level == 5 }

        return LeaguePyramid(
            countryId = countryId,
            topDivision = topDivision,
            secondDivision = secondDivision,
            thirdDivision = thirdDivision,
            fourthDivision = fourthDivision,
            regionalLeagues = regionalLeagues,
            totalLeagues = leagues.size
        )
    }

    // ============ DASHBOARD ============

    suspend fun getLeaguesDashboard(): LeaguesDashboard {
        val allLeagues = leaguesDao.getAll().firstOrNull() ?: emptyList()
        val topDivisions = allLeagues.filter { it.level == 1 }
        val secondDivisions = allLeagues.filter { it.level == 2 }
        val regionalLeagues = allLeagues.filter { it.level == 5 }

        val totalPrizeMoney = allLeagues.sumOf { it.prizeMoney.toLong() }
        val averagePrize = if (allLeagues.isNotEmpty()) totalPrizeMoney / allLeagues.size else 0

        val richestLeague = allLeagues.maxByOrNull { it.prizeMoney }
        val poorestLeague = allLeagues.minByOrNull { it.prizeMoney }

        val countriesWithLeagues = allLeagues.mapNotNull { it.countryId }.distinct().size

        return LeaguesDashboard(
            totalLeagues = allLeagues.size,
            topDivisionCount = topDivisions.size,
            secondDivisionCount = secondDivisions.size,
            regionalLeagueCount = regionalLeagues.size,
            totalPrizeMoney = totalPrizeMoney,
            averagePrizeMoney = averagePrize,
            richestLeague = richestLeague,
            poorestLeague = poorestLeague,
            countriesRepresented = countriesWithLeagues,
            top10Richest = allLeagues.sortedByDescending { it.prizeMoney }.take(10)
        )
    }
}

// ============ DATA CLASSES ============

data class LeaguePyramid(
    val countryId: Int,
    val topDivision: LeaguesEntity?,
    val secondDivision: LeaguesEntity?,
    val thirdDivision: LeaguesEntity?,
    val fourthDivision: LeaguesEntity?,
    val regionalLeagues: List<LeaguesEntity>,
    val totalLeagues: Int
)

data class LeaguesDashboard(
    val totalLeagues: Int,
    val topDivisionCount: Int,
    val secondDivisionCount: Int,
    val regionalLeagueCount: Int,
    val totalPrizeMoney: Long,
    val averagePrizeMoney: Long,
    val richestLeague: LeaguesEntity?,
    val poorestLeague: LeaguesEntity?,
    val countriesRepresented: Int,
    val top10Richest: List<LeaguesEntity>
)
