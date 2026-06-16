package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.MatchFixingCasesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchFixingCasesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM match_fixing_cases ORDER BY allegation_date DESC")
    fun getAll(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE id = :id")
    suspend fun getById(id: Int): MatchFixingCasesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(case: MatchFixingCasesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cases: List<MatchFixingCasesEntity>)

    @Update
    suspend fun update(case: MatchFixingCasesEntity)

    @Query("UPDATE match_fixing_cases SET team_involved_id = :teamId WHERE team_involved = :teamName")
    suspend fun updateTeamIdByName(teamName: String, teamId: Int)

    @Delete
    suspend fun delete(case: MatchFixingCasesEntity)

    @Query("DELETE FROM match_fixing_cases")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM match_fixing_cases")
    suspend fun getCount(): Int

    @Query("SELECT * FROM match_fixing_cases")
    suspend fun getAllStatic(): List<MatchFixingCasesEntity>

    // ============ LEAGUE LEVEL QUERIES ============
    // MATCH FIXING ONLY OCCURS IN LEVELS 4 & 5 - LOWEST PROFESSIONAL TIERS

    @Query("SELECT * FROM match_fixing_cases WHERE league_level = 4 OR league_level = 5 ORDER BY allegation_date DESC")
    fun getLowerLeagueCases(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE league_level = 4 ORDER BY allegation_date DESC")
    fun getLevel4Cases(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE league_level = 5 ORDER BY allegation_date DESC")
    fun getLevel5Cases(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE league_level <= 3")
    fun getUpperLeagueCases(): Flow<List<MatchFixingCasesEntity>>  // Should be empty

    @Query("""
        SELECT 
            league_level,
            COUNT(*) as case_count
        FROM match_fixing_cases 
        GROUP BY league_level
        ORDER BY league_level
    """)
    fun getCaseDistributionByLevel(): Flow<List<LeagueLevelCaseDistribution>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM match_fixing_cases WHERE status = :status ORDER BY allegation_date DESC")
    fun getCasesByStatus(status: String): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE status = 'Investigating' ORDER BY allegation_date ASC")
    fun getActiveInvestigations(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE is_investigation_complete = 0 ORDER BY allegation_date ASC")
    fun getOngoingInvestigations(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE verdict = 'Guilty' ORDER BY resolution_date DESC")
    fun getGuiltyVerdicts(): Flow<List<MatchFixingCasesEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM match_fixing_cases WHERE team_involved_id = :teamId ORDER BY allegation_date DESC")
    fun getCasesByTeamId(teamId: Int): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE team_involved = :teamName ORDER BY allegation_date DESC")
    fun getCasesByTeam(teamName: String): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE manager_id = :managerId ORDER BY allegation_date DESC")
    fun getCasesByManagerId(managerId: Int): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE manager_name = :managerName ORDER BY allegation_date DESC")
    fun getCasesByManagerName(managerName: String): Flow<List<MatchFixingCasesEntity>>

    // ============ SEASON QUERIES ============

    @Query("SELECT * FROM match_fixing_cases WHERE season = :season ORDER BY allegation_date DESC")
    fun getCasesBySeason(season: String): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT DISTINCT season FROM match_fixing_cases ORDER BY season DESC")
    fun getSeasonsWithCases(): Flow<List<String>>

    // ============ PUNISHMENT QUERIES ============

    @Query("SELECT * FROM match_fixing_cases WHERE points_deducted IS NOT NULL AND points_deducted > 0")
    fun getCasesWithPointsDeduction(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE fine_amount IS NOT NULL AND fine_amount > 0 ORDER BY fine_amount DESC")
    fun getCasesWithFines(): Flow<List<MatchFixingCasesEntity>>

    @Query("SELECT * FROM match_fixing_cases WHERE manager_banned = 1")
    fun getCasesWithManagerBans(): Flow<List<MatchFixingCasesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            COUNT(*) as total_cases,
            COUNT(CASE WHEN status = 'Investigating' THEN 1 END) as ongoing,
            COUNT(CASE WHEN verdict = 'Guilty' THEN 1 END) as proven_cases,
            AVG(points_deducted) as avg_points_deducted,
            AVG(fine_amount) as avg_fine
        FROM match_fixing_cases 
        WHERE league_level IN (4, 5)
    """)
    suspend fun getLowerLeagueStatistics(): LowerLeagueFixingStatistics?

    @Query("""
        SELECT 
            league_level,
            COUNT(*) as case_count,
            COUNT(CASE WHEN verdict = 'Guilty' THEN 1 END) as guilty_count
        FROM match_fixing_cases 
        GROUP BY league_level
    """)
    fun getLeagueLevelStatistics(): Flow<List<LeagueLevelFixingStats>>

    @Query("""
        SELECT 
            team_involved_id,
            team_involved,
            COUNT(*) as case_count,
            SUM(points_deducted) as total_points_deducted,
            SUM(fine_amount) as total_fines
        FROM match_fixing_cases 
        WHERE verdict = 'Guilty'
        GROUP BY team_involved_id
        ORDER BY case_count DESC
        LIMIT :limit
    """)
    fun getMostFrequentlyGuiltyTeams(limit: Int): Flow<List<TeamFixingStats>>
}

// ============ DATA CLASSES ============

data class LeagueLevelCaseDistribution(
    @ColumnInfo(name = "league_level")
    val leagueLevel: Int,

    @ColumnInfo(name = "case_count")
    val caseCount: Int
)

data class LowerLeagueFixingStatistics(
    @ColumnInfo(name = "total_cases")
    val totalCases: Int,

    @ColumnInfo(name = "ongoing")
    val ongoingInvestigations: Int,

    @ColumnInfo(name = "proven_cases")
    val provenCases: Int,

    @ColumnInfo(name = "avg_points_deducted")
    val averagePointsDeducted: Double?,

    @ColumnInfo(name = "avg_fine")
    val averageFine: Double?
)

data class LeagueLevelFixingStats(
    @ColumnInfo(name = "league_level")
    val leagueLevel: Int,

    @ColumnInfo(name = "case_count")
    val caseCount: Int,

    @ColumnInfo(name = "guilty_count")
    val guiltyCount: Int
)

data class TeamFixingStats(
    @ColumnInfo(name = "team_involved_id")
    val teamId: Int,

    @ColumnInfo(name = "team_involved")
    val teamName: String,

    @ColumnInfo(name = "case_count")
    val caseCount: Int,

    @ColumnInfo(name = "total_points_deducted")
    val totalPointsDeducted: Int?,

    @ColumnInfo(name = "total_fines")
    val totalFines: Int?
)
