package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaguesDao {

    @Query("SELECT * FROM leagues ORDER BY level, country, name")
    fun getAll(): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues ORDER BY level, country, name")
    suspend fun getAllList(): List<LeaguesEntity>

    @Query("SELECT * FROM leagues WHERE id = :id")
    suspend fun getById(id: Int): LeaguesEntity?

    @Query("SELECT * FROM leagues WHERE name = :name")
    suspend fun getByName(name: String): LeaguesEntity?

    @Query("SELECT * FROM leagues WHERE name = :name")
    suspend fun getLeagueByName(name: String): LeaguesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(league: LeaguesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(leagues: List<LeaguesEntity>)

    @Update
    suspend fun update(league: LeaguesEntity)

    @Delete
    suspend fun delete(league: LeaguesEntity)

    @Query("DELETE FROM leagues")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM leagues")
    suspend fun getCount(): Int

    @Query("SELECT * FROM leagues WHERE country_id = :countryId ORDER BY level ASC")
    fun getLeaguesByCountry(countryId: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE country = :countryName ORDER BY level ASC")
    fun getLeaguesByCountryName(countryName: String): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE country_id = :countryId AND level = 1")
    suspend fun getTopDivisionByCountry(countryId: Int): LeaguesEntity?

    @Query("SELECT * FROM leagues WHERE country_id = :countryId AND level = 2")
    suspend fun getSecondDivisionByCountry(countryId: Int): LeaguesEntity?

    @Query("SELECT DISTINCT country_id FROM leagues WHERE country_id IS NOT NULL ORDER BY country_id")
    fun getDistinctCountryIds(): Flow<List<Int>>

    @Query("SELECT * FROM leagues WHERE level = :level ORDER BY prize_money DESC")
    fun getLeaguesByLevel(level: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE level = 1 ORDER BY prize_money DESC")
    fun getTopDivisionLeagues(): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE level <= :maxLevel ORDER BY level, prize_money DESC")
    fun getLeaguesUpToLevel(maxLevel: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE level = 5")
    fun getRegionalLeagues(): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE prize_money >= :minPrize ORDER BY prize_money DESC")
    fun getHighValueLeagues(minPrize: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE prize_money BETWEEN :minPrize AND :maxPrize ORDER BY prize_money DESC")
    fun getLeaguesByPrizeRange(minPrize: Int, maxPrize: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues ORDER BY prize_money DESC LIMIT :limit")
    fun getRichestLeagues(limit: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT AVG(prize_money) FROM leagues WHERE level = :level")
    suspend fun getAveragePrizeMoneyByLevel(level: Int): Double?

    @Query("SELECT SUM(prize_money) FROM leagues WHERE country_id = :countryId")
    suspend fun getTotalPrizeMoneyByCountry(countryId: Int): Long?

    @Query("SELECT * FROM leagues WHERE sponsor = :sponsorName ORDER BY prize_money DESC")
    fun getLeaguesBySponsor(sponsorName: String): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE sponsor IS NULL")
    fun getLeaguesWithoutSponsor(): Flow<List<LeaguesEntity>>

    @Query("SELECT DISTINCT sponsor FROM leagues WHERE sponsor IS NOT NULL")
    fun getDistinctSponsors(): Flow<List<String>>

    @Query("SELECT * FROM leagues WHERE name LIKE '%' || :searchQuery || '%' OR country LIKE '%' || :searchQuery || '%' ORDER BY level, prize_money DESC")
    fun searchLeagues(searchQuery: String): Flow<List<LeaguesEntity>>

    @Query("SELECT COUNT(*) FROM leagues WHERE country_id = :countryId")
    suspend fun getLeagueCountByCountry(countryId: Int): Int

    @Query("""
        SELECT 
            level,
            COUNT(*) as league_count,
            AVG(prize_money) as avg_prize,
            MIN(prize_money) as min_prize,
            MAX(prize_money) as max_prize
        FROM leagues 
        GROUP BY level
        ORDER BY level
    """)
    fun getLeagueLevelStatistics(): Flow<List<LeagueLevelStatistics>>

    @Query("""
        SELECT 
            country_id,
            COUNT(*) as league_count,
            SUM(prize_money) as total_prize,
            AVG(prize_money) as avg_prize
        FROM leagues 
        WHERE country_id IS NOT NULL
        GROUP BY country_id
        ORDER BY total_prize DESC
    """)
    fun getCountryLeagueStatistics(): Flow<List<CountryLeagueStatistics>>

    @Query("SELECT * FROM leagues WHERE level = :level ORDER BY prize_money DESC")
    fun getFeederLeagues(level: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE level = :currentLevel - 1 ORDER BY prize_money DESC")
    fun getHigherDivision(currentLevel: Int): Flow<List<LeaguesEntity>>

    @Query("SELECT * FROM leagues WHERE level = :currentLevel + 1 ORDER BY prize_money DESC")
    fun getLowerDivision(currentLevel: Int): Flow<List<LeaguesEntity>>

    @Query("""
        SELECT 
            l.*,
            n.nationality as country_nationality,
            n.fifa_code as country_fifa_code,
            n.flag_path as country_flag,
            s.name as detail_sponsor_name,
            s.logo as detail_sponsor_logo
        FROM leagues l
        LEFT JOIN nationalities n ON l.country_id = n.id
        LEFT JOIN sponsors s ON l.sponsor = s.name
        WHERE l.id = :leagueId
    """)
    suspend fun getLeagueWithDetails(leagueId: Int): LeagueWithDetails?

    @Query("""
        SELECT 
            l.*,
            n.nationality as country_nationality,
            n.fifa_code as country_fifa_code,
            n.flag_path as country_flag
        FROM leagues l
        LEFT JOIN nationalities n ON l.country_id = n.id
        ORDER BY l.level, n.nationality, l.name
    """)
    fun getAllLeaguesWithCountries(): Flow<List<LeagueWithCountry>>

    @Query("""
        SELECT l.*, n.nationality as country_nationality, n.fifa_code as country_fifa_code, n.flag_path as country_flag
        FROM leagues l
        LEFT JOIN nationalities n ON l.country_id = n.id
        ORDER BY n.nationality, l.level
    """)
    fun getLeaguesWithCountries(): Flow<List<LeagueWithCountry>>

    @Query("SELECT * FROM leagues WHERE level <= :maxLevel ORDER BY level, prize_money DESC")
    fun getTopTierLeagues(maxLevel: Int = 2): Flow<List<LeaguesEntity>>

    @Query("SELECT country_id, COUNT(*) as league_count FROM leagues GROUP BY country_id")
    fun getLeagueDistribution(): Flow<List<CountryLeagueCount>>
    @Query("SELECT * FROM leagues ORDER BY level ASC, name ASC")
    suspend fun getAllStatic(): List<LeaguesEntity>
}

data class LeagueLevelStatistics(
    @ColumnInfo(name = "level")
    val level: Int,
    @ColumnInfo(name = "league_count")
    val leagueCount: Int,
    @ColumnInfo(name = "avg_prize")
    val averagePrize: Double?,
    @ColumnInfo(name = "min_prize")
    val minimumPrize: Int?,
    @ColumnInfo(name = "max_prize")
    val maximumPrize: Int?
)

data class CountryLeagueStatistics(
    @ColumnInfo(name = "country_id")
    val countryId: Int,
    @ColumnInfo(name = "league_count")
    val leagueCount: Int,
    @ColumnInfo(name = "total_prize")
    val totalPrize: Long?,
    @ColumnInfo(name = "avg_prize")
    val averagePrize: Double?
)

data class LeagueWithCountry(
    @Embedded
    val league: LeaguesEntity,
    @ColumnInfo(name = "country_nationality")
    val countryNationality: String?,
    @ColumnInfo(name = "country_fifa_code")
    val countryFifaCode: String?,
    @ColumnInfo(name = "country_flag")
    val countryFlag: String?
)

data class LeagueWithDetails(
    @Embedded
    val league: LeaguesEntity,
    @ColumnInfo(name = "country_nationality")
    val countryNationality: String?,
    @ColumnInfo(name = "country_fifa_code")
    val countryFifaCode: String?,
    @ColumnInfo(name = "country_flag")
    val countryFlag: String?,
    @ColumnInfo(name = "detail_sponsor_name")
    val sponsorName: String?,
    @ColumnInfo(name = "detail_sponsor_logo")
    val sponsorLogo: String?
)

data class CountryLeagueCount(
    @ColumnInfo(name = "country_id")
    val countryId: Int?,
    @ColumnInfo(name = "league_count")
    val leagueCount: Int
)
