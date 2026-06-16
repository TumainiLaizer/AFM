package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ClubLegendsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubLegendsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM club_legends ORDER BY major_titles_won DESC, years_played DESC")
    fun getAll(): Flow<List<ClubLegendsEntity>>

    @Query("SELECT * FROM club_legends WHERE id = :id")
    suspend fun getById(id: Int): ClubLegendsEntity?

    @Query("SELECT * FROM club_legends WHERE player_name = :playerName")
    suspend fun getByPlayerName(playerName: String): ClubLegendsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(legend: ClubLegendsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(legends: List<ClubLegendsEntity>)

    @Update
    suspend fun update(legend: ClubLegendsEntity)

    @Delete
    suspend fun delete(legend: ClubLegendsEntity)

    @Query("DELETE FROM club_legends")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM club_legends")
    suspend fun getCount(): Int

    // ============ CLUB-BASED QUERIES ============

    @Query("SELECT * FROM club_legends WHERE team_id = :teamId ORDER BY major_titles_won DESC, years_played DESC")
    fun getLegendsByClub(teamId: Int): Flow<List<ClubLegendsEntity>>

    @Query("SELECT * FROM club_legends WHERE team_id = :teamId AND status = 'Active' ORDER BY years_played DESC")
    fun getActiveLegendsByClub(teamId: Int): Flow<List<ClubLegendsEntity>>

    @Query("SELECT * FROM club_legends WHERE team_id = :teamId AND status = 'Retired' ORDER BY major_titles_won DESC")
    fun getRetiredLegendsByClub(teamId: Int): Flow<List<ClubLegendsEntity>>

    @Query("SELECT COUNT(*) FROM club_legends WHERE team_id = :teamId")
    suspend fun getLegendCountByClub(teamId: Int): Int

    @Query("SELECT SUM(major_titles_won) FROM club_legends WHERE team_id = :teamId")
    suspend fun getTotalTitlesByLegends(teamId: Int): Int?

    @Query("SELECT * FROM club_legends WHERE team_id = :teamId ORDER BY major_titles_won DESC LIMIT 10")
    fun getTopLegendsByClub(teamId: Int): Flow<List<ClubLegendsEntity>>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM club_legends WHERE player_name = :playerName ORDER BY club_name")
    fun getClubsForLegend(playerName: String): Flow<List<ClubLegendsEntity>>

    // ============ TITLES-BASED QUERIES ============

    @Query("SELECT * FROM club_legends WHERE major_titles_won >= :minTitles ORDER BY major_titles_won DESC")
    fun getLegendsByMinTitles(minTitles: Int): Flow<List<ClubLegendsEntity>>

    @Query("SELECT * FROM club_legends ORDER BY major_titles_won DESC LIMIT :limit")
    fun getMostDecoratedLegends(limit: Int): Flow<List<ClubLegendsEntity>>

    @Query("SELECT * FROM club_legends WHERE years_played >= :minYears ORDER BY years_played DESC")
    fun getLongestServingLegends(minYears: Int): Flow<List<ClubLegendsEntity>>

    // ============ STATUS-BASED QUERIES ============

    @Query("SELECT * FROM club_legends WHERE status = :status ORDER BY major_titles_won DESC")
    fun getLegendsByStatus(status: String): Flow<List<ClubLegendsEntity>>

    // ============ SEARCH QUERIES ============

    @Query("SELECT * FROM club_legends WHERE player_name LIKE '%' || :searchQuery || '%' OR club_name LIKE '%' || :searchQuery || '%' ORDER BY major_titles_won DESC")
    fun searchLegends(searchQuery: String): Flow<List<ClubLegendsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            club_name,
            COUNT(*) as legend_count,
            AVG(major_titles_won) as avg_titles,
            SUM(major_titles_won) as total_titles,
            AVG(years_played) as avg_years
        FROM club_legends 
        GROUP BY club_name
        ORDER BY total_titles DESC
    """)
    fun getClubLegendStatistics(): Flow<List<ClubLegendStatistics>>

    @Query("""
        SELECT 
            status,
            COUNT(*) as count,
            AVG(major_titles_won) as avg_titles
        FROM club_legends 
        GROUP BY status
    """)
    fun getLegendStatusDistribution(): Flow<List<LegendStatusDistribution>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            cl.*,
            p.position as player_position,
            p.nationality as player_nationality,
            p.rating as player_rating,
            t.logo_path as club_logo,
            t.league as club_league
        FROM club_legends cl
        LEFT JOIN players p ON cl.player_id = p.id
        LEFT JOIN teams t ON cl.team_id = t.id
        WHERE cl.id = :legendId
    """)
    suspend fun getLegendWithDetails(legendId: Int): LegendWithDetails?

    @Query("""
        SELECT 
            cl.*,
            p.position as player_position,
            p.nationality as player_nationality,
            p.rating as player_rating,
            t.logo_path as club_logo,
            t.league as club_league
        FROM club_legends cl
        LEFT JOIN players p ON cl.player_id = p.id
        LEFT JOIN teams t ON cl.team_id = t.id
        WHERE cl.team_id = :teamId
        ORDER BY cl.major_titles_won DESC
    """)
    fun getClubLegendsWithDetails(teamId: Int): Flow<List<LegendWithDetails>>
    @Query("SELECT * FROM club_legends ORDER BY player_name ASC")
    suspend fun getAllStatic(): List<ClubLegendsEntity>
}

// ============ DATA CLASSES ============

data class ClubLegendStatistics(
    @ColumnInfo(name = "club_name")
    val clubName: String,

    @ColumnInfo(name = "legend_count")
    val legendCount: Int,

    @ColumnInfo(name = "avg_titles")
    val averageTitles: Double,

    @ColumnInfo(name = "total_titles")
    val totalTitles: Int,

    @ColumnInfo(name = "avg_years")
    val averageYears: Double
)

data class LegendStatusDistribution(
    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "avg_titles")
    val averageTitles: Double
)

data class LegendWithDetails(
    @Embedded
    val legend: ClubLegendsEntity,

    @ColumnInfo(name = "player_position")
    val playerPosition: String?,

    @ColumnInfo(name = "player_nationality")
    val playerNationality: String?,

    @ColumnInfo(name = "player_rating")
    val playerRating: Int?,

    @ColumnInfo(name = "club_logo")
    val clubLogo: String?,

    @ColumnInfo(name = "club_league")
    val clubLeague: String?
)
