package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.CupGroupStandingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CupGroupStandingsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM cup_group_standings")
    fun getAll(): Flow<List<CupGroupStandingsEntity>>

    @Query("SELECT * FROM cup_group_standings WHERE id = :id")
    suspend fun getById(id: Int): CupGroupStandingsEntity?

    @Query("SELECT * FROM cup_group_standings WHERE team_id = :teamId AND cup_name = :cupName AND season_year = :seasonYear")
    suspend fun getTeamStanding(teamId: Int, cupName: String, seasonYear: Int): CupGroupStandingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(standing: CupGroupStandingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(standings: List<CupGroupStandingsEntity>)

    @Update
    suspend fun update(standing: CupGroupStandingsEntity)

    @Delete
    suspend fun delete(standing: CupGroupStandingsEntity)

    @Query("DELETE FROM cup_group_standings WHERE cup_name = :cupName AND season_year = :seasonYear")
    suspend fun deleteByCupAndSeason(cupName: String, seasonYear: Int)

    @Query("DELETE FROM cup_group_standings WHERE cup_name = :cupName AND group_name = :groupName AND season_year = :seasonYear")
    suspend fun deleteByGroup(cupName: String, groupName: String, seasonYear: Int)

    @Query("DELETE FROM cup_group_standings")
    suspend fun deleteAll()

    @Query("SELECT * FROM cup_group_standings")
    suspend fun getAllStatic(): List<CupGroupStandingsEntity>

    // ============ GROUP STANDINGS QUERIES ============

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE cup_name = :cupName AND season_year = :seasonYear 
        ORDER BY group_name ASC, points DESC, goal_difference DESC, goals_scored DESC
    """)
    fun getAllGroupStandings(cupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>>

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE cup_name = :cupName AND group_name = :groupName AND season_year = :seasonYear 
        ORDER BY points DESC, goal_difference DESC, goals_scored DESC
    """)
    fun getGroupStandings(cupName: String, groupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>>

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE cup_name = :cupName AND group_name = :groupName AND season_year = :seasonYear 
        ORDER BY position ASC
    """)
    fun getStandingsByPosition(cupName: String, groupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>>

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE cup_name = :cupName AND group_name = :groupName AND season_year = :seasonYear 
        AND position <= 2
        ORDER BY position ASC
    """)
    fun getQualifiedTeams(cupName: String, groupName: String, seasonYear: Int): Flow<List<CupGroupStandingsEntity>>

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE cup_name = :cupName AND group_name = :groupName AND season_year = :seasonYear 
        AND position = 1
    """)
    suspend fun getGroupWinner(cupName: String, groupName: String, seasonYear: Int): CupGroupStandingsEntity?

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE cup_name = :cupName AND season_year = :seasonYear 
        AND team_id = :teamId
    """)
    suspend fun getTeamPosition(cupName: String, seasonYear: Int, teamId: Int): CupGroupStandingsEntity?

    // ============ TEAM HISTORY QUERIES ============

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE team_id = :teamId 
        ORDER BY season_year DESC
    """)
    fun getTeamCupHistory(teamId: Int): Flow<List<CupGroupStandingsEntity>>

    @Query("""
        SELECT * FROM cup_group_standings 
        WHERE team_id = :teamId AND position = 1
        ORDER BY season_year DESC
    """)
    fun getTeamGroupWins(teamId: Int): Flow<List<CupGroupStandingsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            AVG(points) as avg_points,
            AVG(goals_scored) as avg_goals_for,
            AVG(goals_conceded) as avg_goals_against
        FROM cup_group_standings 
        WHERE cup_name = :cupName AND group_name = :groupName AND season_year = :seasonYear
    """)
    suspend fun getCupGroupStatistics(cupName: String, groupName: String, seasonYear: Int): CupGroupStatistics?

    @Query("""
        SELECT 
            team_id,
            team_name,
            COUNT(CASE WHEN position = 1 THEN 1 END) as group_wins
        FROM cup_group_standings 
        WHERE cup_name = :cupName
        GROUP BY team_id
        ORDER BY group_wins DESC
    """)
    fun getMostGroupWins(cupName: String): Flow<List<GroupWinsStats>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            cgs.*,
            t.logo_path as team_logo,
            t.home_stadium as team_stadium,
            t.elo_rating as team_elo,
            c.type as cup_type,
            c.country as cup_country
        FROM cup_group_standings cgs
        LEFT JOIN teams t ON cgs.team_id = t.id
        LEFT JOIN cups c ON cgs.cup_name = c.name
        WHERE cgs.cup_name = :cupName AND cgs.group_name = :groupName AND cgs.season_year = :seasonYear
        ORDER BY cgs.position ASC
    """)
    fun getFullGroupStandings(cupName: String, groupName: String, seasonYear: Int): Flow<List<FullGroupStandingEntry>>
}

// ============ DATA CLASSES ============

data class CupGroupStatistics(
    @ColumnInfo(name = "avg_points")
    val averagePoints: Double,

    @ColumnInfo(name = "avg_goals_for")
    val averageGoalsFor: Double,

    @ColumnInfo(name = "avg_goals_against")
    val averageGoalsAgainst: Double
)

data class GroupWinsStats(
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "group_wins")
    val groupWins: Int
)

data class FullGroupStandingEntry(
    @Embedded
    val standing: CupGroupStandingsEntity,

    @ColumnInfo(name = "team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "team_stadium")
    val teamStadium: String?,

    @ColumnInfo(name = "team_elo")
    val teamElo: Int?,

    @ColumnInfo(name = "cup_type")
    val cupType: String?,

    @ColumnInfo(name = "cup_country")
    val cupCountry: String?
)
