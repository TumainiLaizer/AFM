package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.TrophiesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrophiesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM trophies ORDER BY season DESC, date_won DESC")
    fun getAll(): Flow<List<TrophiesEntity>>

    @Query("SELECT * FROM trophies WHERE id = :id")
    suspend fun getById(id: Int): TrophiesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trophy: TrophiesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trophies: List<TrophiesEntity>)

    @Update
    suspend fun update(trophy: TrophiesEntity)

    @Delete
    suspend fun delete(trophy: TrophiesEntity)

    @Query("DELETE FROM trophies")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM trophies")
    suspend fun getCount(): Int

    @Query("SELECT * FROM trophies")
    suspend fun getAllStatic(): List<TrophiesEntity>

    // ============ MANAGER-BASED QUERIES ============

    @Query("SELECT * FROM trophies WHERE manager_id = :managerId ORDER BY season DESC")
    fun getTrophiesByManager(managerId: Int): Flow<List<TrophiesEntity>>

    @Query("SELECT COUNT(*) FROM trophies WHERE manager_id = :managerId")
    suspend fun getTrophyCountByManager(managerId: Int): Int

    @Query("SELECT * FROM trophies WHERE manager_id = :managerId AND trophy_type = :trophyType ORDER BY season DESC")
    fun getTrophiesByManagerAndType(managerId: Int, trophyType: String): Flow<List<TrophiesEntity>>

    @Query("SELECT COUNT(*) FROM trophies WHERE manager_id = :managerId AND trophy_type = 'LEAGUE_TITLE'")
    suspend fun getLeagueTitlesByManager(managerId: Int): Int

    @Query("SELECT COUNT(*) FROM trophies WHERE manager_id = :managerId AND trophy_type = 'CUP_TITLE'")
    suspend fun getCupTitlesByManager(managerId: Int): Int

    @Query("SELECT COUNT(*) FROM trophies WHERE manager_id = :managerId AND trophy_type = 'CONTINENTAL_TITLE'")
    suspend fun getContinentalTitlesByManager(managerId: Int): Int

    @Query("SELECT COUNT(*) FROM trophies WHERE manager_id = :managerId AND trophy_type = 'SUPER_CUP'")
    suspend fun getSuperCupsByManager(managerId: Int): Int

    // ============ CLUB-BASED QUERIES ============

    @Query("SELECT * FROM trophies WHERE club_name = :clubName ORDER BY season DESC")
    fun getTrophiesByClub(clubName: String): Flow<List<TrophiesEntity>>

    @Query("SELECT * FROM trophies WHERE club_id = :clubId ORDER BY season DESC")
    fun getTrophiesByClubId(clubId: Int): Flow<List<TrophiesEntity>>

    @Query("SELECT COUNT(*) FROM trophies WHERE club_id = :clubId")
    suspend fun getTrophyCountByClubId(clubId: Int): Int

    @Query("SELECT COUNT(*) FROM trophies WHERE club_id = :clubId AND trophy_type = 'LEAGUE_TITLE'")
    suspend fun getLeagueTitlesByClubId(clubId: Int): Int

    @Query("SELECT COUNT(*) FROM trophies WHERE club_id = :clubId AND trophy_type = 'CUP_TITLE'")
    suspend fun getCupTitlesByClubId(clubId: Int): Int

    @Query("SELECT COUNT(*) FROM trophies WHERE club_id = :clubId AND trophy_type = 'CONTINENTAL_TITLE'")
    suspend fun getContinentalTitlesByClubId(clubId: Int): Int

    @Query("SELECT * FROM trophies WHERE club_name = :clubName AND season = :season")
    suspend fun getTrophiesByClubAndSeason(clubName: String, season: String): List<TrophiesEntity>

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT * FROM trophies WHERE season = :season ORDER BY trophy_type, club_name")
    fun getTrophiesBySeason(season: String): Flow<List<TrophiesEntity>>

    @Query("SELECT * FROM trophies WHERE season_year = :seasonYear ORDER BY season, trophy_type")
    fun getTrophiesBySeasonYear(seasonYear: Int): Flow<List<TrophiesEntity>>

    @Query("SELECT DISTINCT season FROM trophies ORDER BY season DESC")
    fun getSeasons(): Flow<List<String>>

    // ============ COMPETITION-BASED QUERIES ============

    @Query("SELECT * FROM trophies WHERE competition_name = :competitionName ORDER BY season DESC")
    fun getTrophiesByCompetition(competitionName: String): Flow<List<TrophiesEntity>>

    @Query("SELECT * FROM trophies WHERE competition_level = :level ORDER BY season DESC")
    fun getTrophiesByLevel(level: String): Flow<List<TrophiesEntity>>

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM trophies WHERE trophy_type = :trophyType ORDER BY season DESC")
    fun getTrophiesByType(trophyType: String): Flow<List<TrophiesEntity>>

    // ============ INTEGRATION WITH SEASON AWARDS ============

    @Query("SELECT * FROM trophies WHERE season_award_id = :seasonAwardId")
    suspend fun getTrophyBySeasonAward(seasonAwardId: Int): TrophiesEntity?

    @Query("SELECT * FROM trophies WHERE season_history_id = :seasonHistoryId")
    suspend fun getTrophiesBySeasonHistory(seasonHistoryId: Int): List<TrophiesEntity>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            manager_id,
            manager_name,
            COUNT(*) as total_trophies,
            COUNT(CASE WHEN trophy_type = 'LEAGUE_TITLE' THEN 1 END) as league_titles,
            COUNT(CASE WHEN trophy_type = 'CUP_TITLE' THEN 1 END) as cup_titles,
            COUNT(CASE WHEN trophy_type = 'CONTINENTAL_TITLE' THEN 1 END) as continental_titles,
            COUNT(CASE WHEN trophy_type = 'SUPER_CUP' THEN 1 END) as super_cups
        FROM trophies 
        GROUP BY manager_id
        ORDER BY total_trophies DESC
    """)
    fun getManagerTrophyRankings(): Flow<List<ManagerTrophyStats>>

    @Query("""
        SELECT 
            club_name,
            COUNT(*) as total_trophies,
            COUNT(CASE WHEN trophy_type = 'LEAGUE_TITLE' THEN 1 END) as league_titles,
            COUNT(CASE WHEN trophy_type = 'CUP_TITLE' THEN 1 END) as cup_titles,
            COUNT(CASE WHEN trophy_type = 'CONTINENTAL_TITLE' THEN 1 END) as continental_titles,
            MAX(season) as last_trophy_season
        FROM trophies 
        GROUP BY club_name
        ORDER BY total_trophies DESC
    """)
    fun getClubTrophyRankings(): Flow<List<ClubTrophyStats>>

    @Query("""
        SELECT 
            competition_level,
            COUNT(*) as trophy_count
        FROM trophies 
        GROUP BY competition_level
        ORDER BY trophy_count DESC
    """)
    fun getTrophiesByLevelDistribution(): Flow<List<TrophyLevelDistribution>>

    @Query("""
        SELECT 
            trophy_type,
            COUNT(*) as trophy_count
        FROM trophies 
        GROUP BY trophy_type
        ORDER BY trophy_count DESC
    """)
    fun getTrophyTypeDistribution(): Flow<List<TrophyTypeDistribution>>

    @Query("SELECT * FROM trophies ORDER BY season DESC LIMIT :limit")
    fun getRecentTrophies(limit: Int): Flow<List<TrophiesEntity>>
}

// ============ DATA CLASSES ============

data class ManagerTrophyStats(
    @ColumnInfo(name = "manager_id")
    val managerId: Int,

    @ColumnInfo(name = "manager_name")
    val managerName: String?,

    @ColumnInfo(name = "total_trophies")
    val totalTrophies: Int,

    @ColumnInfo(name = "league_titles")
    val leagueTitles: Int,

    @ColumnInfo(name = "cup_titles")
    val cupTitles: Int,

    @ColumnInfo(name = "continental_titles")
    val continentalTitles: Int,

    @ColumnInfo(name = "super_cups")
    val superCups: Int
)

data class ClubTrophyStats(
    @ColumnInfo(name = "club_name")
    val clubName: String,

    @ColumnInfo(name = "total_trophies")
    val totalTrophies: Int,

    @ColumnInfo(name = "league_titles")
    val leagueTitles: Int,

    @ColumnInfo(name = "cup_titles")
    val cupTitles: Int,

    @ColumnInfo(name = "continental_titles")
    val continentalTitles: Int,

    @ColumnInfo(name = "last_trophy_season")
    val lastTrophySeason: String?
)

data class TrophyLevelDistribution(
    @ColumnInfo(name = "competition_level")
    val competitionLevel: String,

    @ColumnInfo(name = "trophy_count")
    val trophyCount: Int
)

data class TrophyTypeDistribution(
    @ColumnInfo(name = "trophy_type")
    val trophyType: String,

    @ColumnInfo(name = "trophy_count")
    val trophyCount: Int
)