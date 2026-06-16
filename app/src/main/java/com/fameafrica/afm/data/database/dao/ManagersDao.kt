package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ManagersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManagersDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM managers ORDER BY reputation DESC, name ASC")
    fun getAll(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE id = :id")
    suspend fun getById(id: Int): ManagersEntity?

    @Query("SELECT * FROM managers WHERE name = :name")
    suspend fun getByName(name: String): ManagersEntity?

    @Query("SELECT * FROM managers WHERE team_id = :teamId")
    suspend fun getByTeamId(teamId: Int): ManagersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(manager: ManagersEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(managers: List<ManagersEntity>)

    @Update
    suspend fun update(manager: ManagersEntity)

    @Delete
    suspend fun delete(manager: ManagersEntity)

    @Query("DELETE FROM managers")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM managers")
    suspend fun getCount(): Int

    // ============ EMPLOYMENT STATUS ============

    @Query("SELECT * FROM managers WHERE team_id IS NULL ORDER BY reputation DESC")
    fun getAvailableManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE team_id IS NOT NULL ORDER BY reputation DESC")
    fun getEmployedManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE team_id = :teamId")
    suspend fun getManagerByTeam(teamId: Int): ManagersEntity?

    @Query("SELECT * FROM managers WHERE team_id IS NULL AND reputation BETWEEN :minRep AND :maxRep")
    fun getAvailableManagersByReputation(minRep: Int, maxRep: Int): Flow<List<ManagersEntity>>

    // ============ REPUTATION-BASED QUERIES ============

    @Query("SELECT * FROM managers WHERE reputation_level = :level ORDER BY reputation DESC")
    fun getManagersByReputationLevel(level: String): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE reputation >= :minReputation ORDER BY reputation DESC")
    fun getHighReputationManagers(minReputation: Int): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE reputation <= :maxReputation ORDER BY reputation DESC")
    fun getLowReputationManagers(maxReputation: Int): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers ORDER BY reputation DESC LIMIT :limit")
    fun getTopManagers(limit: Int): Flow<List<ManagersEntity>>

    // ============ NATIONALITY-BASED QUERIES ============

    @Query("SELECT * FROM managers WHERE nationality = :nationality ORDER BY reputation DESC")
    fun getManagersByNationality(nationality: String): Flow<List<ManagersEntity>>

    @Query("SELECT DISTINCT nationality FROM managers WHERE nationality IS NOT NULL ORDER BY nationality")
    fun getDistinctNationalities(): Flow<List<String>>

    // ============ AGE-BASED QUERIES ============

    @Query("SELECT * FROM managers WHERE age <= 40 ORDER BY reputation DESC")
    fun getYoungManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE age BETWEEN 41 AND 55 ORDER BY reputation DESC")
    fun getPrimeAgeManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE age >= 56 ORDER BY reputation DESC")
    fun getVeteranManagers(): Flow<List<ManagersEntity>>

    // ============ PERFORMANCE QUERIES ============

    /**
     * Get managers sorted by win percentage (calculated from wins / matches_managed)
     * Only includes managers with at least 1 match managed
     */
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *, 
        CASE 
            WHEN matches_managed > 0 
            THEN CAST(wins AS REAL) / matches_managed * 100 
            ELSE 0 
        END AS win_percentage 
        FROM managers 
        WHERE matches_managed > 0 
        ORDER BY win_percentage DESC 
        LIMIT :limit
    """)
    fun getManagersByWinPercentage(limit: Int): Flow<List<ManagersEntity>>

    /**
     * Get managers sorted by win percentage (all managers, including those with 0 matches)
     */
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *, 
        CASE 
            WHEN matches_managed > 0 
            THEN CAST(wins AS REAL) / matches_managed * 100 
            ELSE 0 
        END AS win_percentage 
        FROM managers 
        ORDER BY win_percentage DESC 
        LIMIT :limit
    """)
    fun getAllManagersByWinPercentage(limit: Int): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers ORDER BY trophies_won DESC LIMIT :limit")
    fun getMostTrophyWinningManagers(limit: Int): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers ORDER BY matches_managed DESC LIMIT :limit")
    fun getMostExperiencedManagers(limit: Int): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE performance_rating >= 80 ORDER BY performance_rating DESC")
    fun getHighPerformingManagers(): Flow<List<ManagersEntity>>

    // ============ LICENSE & ABILITY QUERIES ============

    @Query("SELECT * FROM managers WHERE coaching_license IN (:licenses) ORDER BY reputation DESC")
    fun getManagersByLicense(licenses: List<String>): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE special_ability = :ability ORDER BY reputation DESC")
    fun getManagersBySpecialAbility(ability: String): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE youth_development_focus >= 70 ORDER BY youth_development_focus DESC")
    fun getYouthDevelopmentSpecialists(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE media_handling >= 70 ORDER BY media_handling DESC")
    fun getMediaFriendlyManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE tactical_flexibility >= 70 ORDER BY tactical_flexibility DESC")
    fun getTacticallyFlexibleManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE player_motivation >= 70 ORDER BY player_motivation DESC")
    fun getMotivationalManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE discipline_level >= 70 ORDER BY discipline_level DESC")
    fun getStrictManagers(): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE adaptability >= 70 ORDER BY adaptability DESC")
    fun getAdaptableManagers(): Flow<List<ManagersEntity>>

    // ============ FORMATION & STYLE QUERIES ============

    @Query("SELECT * FROM managers WHERE preferred_formation = :formation ORDER BY reputation DESC")
    fun getManagersByPreferredFormation(formation: String): Flow<List<ManagersEntity>>

    @Query("SELECT * FROM managers WHERE style = :style ORDER BY reputation DESC")
    fun getManagersByStyle(style: String): Flow<List<ManagersEntity>>

    // ============ SEARCH QUERIES ============

    @Query("SELECT * FROM managers WHERE name LIKE '%' || :searchQuery || '%' ORDER BY reputation DESC")
    fun searchManagers(searchQuery: String): Flow<List<ManagersEntity>>

    @Query("""
        SELECT * FROM managers 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR nationality LIKE '%' || :searchQuery || '%'
        OR reputation_level LIKE '%' || :searchQuery || '%'
        ORDER BY reputation DESC
    """)
    fun advancedSearch(searchQuery: String): Flow<List<ManagersEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("SELECT AVG(reputation) FROM managers")
    suspend fun getAverageReputation(): Double?

    @Query("SELECT AVG(age) FROM managers")
    suspend fun getAverageAge(): Double?

    @Query("SELECT AVG(matches_managed) FROM managers WHERE team_id IS NOT NULL")
    suspend fun getAverageMatchesManaged(): Double?

    @Query("""
        SELECT AVG(CAST(wins AS REAL) / matches_managed * 100) as avg_win_percentage
        FROM managers 
        WHERE matches_managed > 0
    """)
    suspend fun getAverageWinPercentage(): Double?

    @Query("""
        SELECT 
            reputation_level,
            COUNT(*) as manager_count,
            AVG(reputation) as avg_reputation,
            AVG(age) as avg_age,
            AVG(CAST(wins AS REAL) / NULLIF(matches_managed, 0) * 100) as avg_win_percentage
        FROM managers 
        GROUP BY reputation_level
        ORDER BY 
            CASE reputation_level
                WHEN 'Local' THEN 1
                WHEN 'National' THEN 2
                WHEN 'Continental' THEN 3
                WHEN 'World Class' THEN 4
            END
    """)
    fun getManagerDistributionByLevel(): Flow<List<ManagerLevelDistribution>>

    @Query("""
        SELECT 
            style,
            COUNT(*) as manager_count,
            AVG(reputation) as avg_reputation,
            AVG(CAST(wins AS REAL) / NULLIF(matches_managed, 0) * 100) as avg_win_percentage
        FROM managers 
        WHERE style IS NOT NULL AND matches_managed > 0
        GROUP BY style
        ORDER BY manager_count DESC
    """)
    fun getManagerStyleDistribution(): Flow<List<ManagerStyleDistribution>>

    // ============ WIN PERCENTAGE RANKINGS ============

    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *, 
        CAST(wins AS REAL) / matches_managed * 100 as win_percentage
        FROM managers 
        WHERE matches_managed >= :minMatches
        ORDER BY win_percentage DESC 
        LIMIT :limit
    """)
    fun getManagersByWinPercentageWithMinMatches(minMatches: Int, limit: Int): Flow<List<ManagersEntity>>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *, 
        CAST(wins AS REAL) / matches_managed * 100 as win_percentage
        FROM managers 
        WHERE matches_managed >= 50 AND team_id IS NOT NULL
        ORDER BY win_percentage DESC 
        LIMIT :limit
    """)
    fun getTopActiveManagersByWinPercentage(limit: Int): Flow<List<ManagersEntity>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            m.*,
            t.name as team_name,
            t.logo_path as team_logo,
            t.league as team_league,
            n.nationality as nationality_name,
            n.fifa_code as nationality_fifa_code,
            n.flag_path as nationality_flag,
            CASE 
                WHEN m.matches_managed > 0 
                THEN CAST(m.wins AS REAL) / m.matches_managed * 100 
                ELSE 0 
            END as win_percentage
        FROM managers m
        LEFT JOIN teams t ON m.team_id = t.id
        LEFT JOIN nationalities n ON m.nationality = n.nationality
        WHERE m.id = :managerId
    """)
    suspend fun getManagerWithDetails(managerId: Int): ManagerWithDetails?

    @Query("""
        SELECT 
            m.*,
            t.name as team_name,
            t.logo_path as team_logo,
            t.league as team_league,
            CASE 
                WHEN m.matches_managed > 0 
                THEN CAST(m.wins AS REAL) / m.matches_managed * 100 
                ELSE 0 
            END as win_percentage
        FROM managers m
        LEFT JOIN teams t ON m.team_id = t.id
        WHERE m.team_id IS NOT NULL
        ORDER BY win_percentage DESC, m.reputation DESC
    """)
    fun getAllEmployedManagersWithTeams(): Flow<List<EmployedManagerWithTeam>>
    @Query("SELECT * FROM managers ORDER BY reputation DESC, name ASC")
    suspend fun getAllStatic(): List<ManagersEntity>

    @Query("SELECT * FROM managers WHERE team_id IN (:teamIds)")
    suspend fun getByTeamIds(teamIds: List<Int>): List<ManagersEntity>
}

// ============ DATA CLASSES ============

data class ManagerLevelDistribution(
    @ColumnInfo(name = "reputation_level")
    val reputationLevel: String,

    @ColumnInfo(name = "manager_count")
    val managerCount: Int,

    @ColumnInfo(name = "avg_reputation")
    val averageReputation: Double,

    @ColumnInfo(name = "avg_age")
    val averageAge: Double,

    @ColumnInfo(name = "avg_win_percentage")
    val averageWinPercentage: Double?
)

data class ManagerStyleDistribution(
    @ColumnInfo(name = "style")
    val style: String,

    @ColumnInfo(name = "manager_count")
    val managerCount: Int,

    @ColumnInfo(name = "avg_reputation")
    val averageReputation: Double,

    @ColumnInfo(name = "avg_win_percentage")
    val averageWinPercentage: Double?
)

data class ManagerWithDetails(
    @Embedded
    val manager: ManagersEntity,

    @ColumnInfo(name = "team_name")
    val teamName: String?,

    @ColumnInfo(name = "team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "team_league")
    val teamLeague: String?,

    @ColumnInfo(name = "nationality_name")
    val nationalityName: String?,

    @ColumnInfo(name = "nationality_fifa_code")
    val nationalityFifaCode: String?,

    @ColumnInfo(name = "nationality_flag")
    val nationalityFlag: String?,

    @ColumnInfo(name = "win_percentage")
    val winPercentage: Double
)

data class EmployedManagerWithTeam(
    @Embedded
    val manager: ManagersEntity,

    @ColumnInfo(name = "team_name")
    val teamName: String?,

    @ColumnInfo(name = "team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "team_league")
    val teamLeague: String?,

    @ColumnInfo(name = "win_percentage")
    val winPercentage: Double
)
