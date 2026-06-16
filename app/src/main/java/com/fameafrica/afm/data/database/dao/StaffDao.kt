package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.StaffEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM staff ORDER BY role, name")
    fun getAll(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getById(id: Int): StaffEntity?

    @Query("SELECT * FROM staff WHERE name = :name")
    suspend fun getByName(name: String): StaffEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staff: StaffEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(staffList: List<StaffEntity>)

    @Update
    suspend fun update(staff: StaffEntity)

    @Delete
    suspend fun delete(staff: StaffEntity)

    @Query("DELETE FROM staff WHERE team_id = :teamId")
    suspend fun deleteByTeam(teamId: Int)

    @Query("DELETE FROM staff")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM staff")
    suspend fun getCount(): Int

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM staff WHERE team_id = :teamId ORDER BY role, impact_rating DESC")
    fun getStaffByTeam(teamId: Int): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE team_id = :teamId")
    suspend fun getStaffByTeamSync(teamId: Int): List<StaffEntity>

    @Query("SELECT * FROM staff WHERE team_id = :teamId AND staff_type = :staffType ORDER BY impact_rating DESC")
    fun getStaffByTeamAndType(teamId: Int, staffType: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE team_id = :teamId AND role = :role")
    suspend fun getStaffByRole(teamId: Int, role: String): List<StaffEntity>

    @Query("SELECT * FROM staff WHERE team_id = :teamId AND is_head_of_department = 1")
    fun getDepartmentHeads(teamId: Int): Flow<List<StaffEntity>>

    @Query("SELECT COUNT(*) FROM staff WHERE team_id = :teamId")
    suspend fun getStaffCountByTeam(teamId: Int): Int

    // ============ ROLE-BASED QUERIES ============

    @Query("SELECT * FROM staff WHERE role = :role ORDER BY impact_rating DESC")
    fun getStaffByRole(role: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE staff_type = :staffType ORDER BY impact_rating DESC")
    fun getStaffByType(staffType: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE role LIKE '%COACH%' ORDER BY impact_rating DESC")
    fun getAllCoaches(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE role LIKE '%SCOUT%' ORDER BY impact_rating DESC")
    fun getAllScouts(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE role LIKE '%PHYSIO%' OR role LIKE '%MEDICAL%' ORDER BY impact_rating DESC")
    fun getMedicalStaff(): Flow<List<StaffEntity>>

    // ============ SPECIALIZATION QUERIES ============

    @Query("SELECT * FROM staff WHERE specialization = :specialization ORDER BY impact_rating DESC")
    fun getStaffBySpecialization(specialization: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE specialization = 'Youth Development' ORDER BY impact_rating DESC")
    fun getYouthDevelopmentSpecialists(): Flow<List<StaffEntity>>

    // ============ IMPACT RATING QUERIES ============

    @Query("SELECT * FROM staff WHERE impact_rating >= :minRating ORDER BY impact_rating DESC")
    fun getHighImpactStaff(minRating: Int): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE team_id = :teamId ORDER BY impact_rating DESC LIMIT :limit")
    fun getTopStaffByTeam(teamId: Int, limit: Int): Flow<List<StaffEntity>>

    @Query("SELECT AVG(impact_rating) FROM staff WHERE team_id = :teamId")
    suspend fun getAverageImpactRating(teamId: Int): Double?

    // ============ EXPERIENCE QUERIES ============

    @Query("SELECT * FROM staff WHERE experience_level >= :minYears ORDER BY experience_level DESC")
    fun getExperiencedStaff(minYears: Int): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE experience_level <= :maxYears ORDER BY experience_level ASC")
    fun getInexperiencedStaff(maxYears: Int): Flow<List<StaffEntity>>

    // ============ FORMER PLAYER QUERIES ============

    @Query("SELECT * FROM staff WHERE previous_player_id IS NOT NULL")
    fun getFormerPlayers(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE previous_player_id = :playerId")
    suspend fun getStaffByFormerPlayer(playerId: Int): StaffEntity?

    // ============ SEARCH QUERIES ============

    @Query("SELECT * FROM staff WHERE name LIKE '%' || :searchQuery || '%' OR specialization LIKE '%' || :searchQuery || '%' ORDER BY impact_rating DESC")
    fun searchStaff(searchQuery: String): Flow<List<StaffEntity>>

    // ============ AVAILABILITY QUERIES ============

    @Query("SELECT * FROM staff WHERE team_id = :teamId AND role = :role")
    suspend fun getStaffByRoleAndTeam(teamId: Int, role: String): StaffEntity?

    @Query("SELECT * FROM staff WHERE team_id = :teamId AND role = 'ASSISTANT_MANAGER'")
    suspend fun getAssistantManager(teamId: Int): StaffEntity?

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            staff_type,
            COUNT(*) as count,
            AVG(impact_rating) as avg_impact,
            AVG(salary) as avg_salary
        FROM staff 
        WHERE team_id = :teamId
        GROUP BY staff_type
    """)
    fun getStaffStatisticsByTeam(teamId: Int): Flow<List<StaffTypeStatistics>>

    @Query("""
        SELECT 
            role,
            COUNT(*) as count,
            AVG(impact_rating) as avg_impact
        FROM staff 
        GROUP BY role
        ORDER BY count DESC
    """)
    fun getRoleDistribution(): Flow<List<RoleDistribution>>

    @Query("""
        SELECT 
            specialization,
            COUNT(*) as count,
            AVG(impact_rating) as avg_impact
        FROM staff 
        WHERE specialization != 'General'
        GROUP BY specialization
        ORDER BY count DESC
    """)
    fun getSpecializationDistribution(): Flow<List<SpecializationDistribution>>
    @Query("SELECT * FROM staff ORDER BY name ASC")
    suspend fun getAllStatic(): List<StaffEntity>
}

// ============ DATA CLASSES ============

data class StaffTypeStatistics(
    @ColumnInfo(name = "staff_type")
    val staffType: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "avg_impact")
    val averageImpact: Double,

    @ColumnInfo(name = "avg_salary")
    val averageSalary: Double
)

data class RoleDistribution(
    @ColumnInfo(name = "role")
    val role: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "avg_impact")
    val averageImpact: Double
)

data class SpecializationDistribution(
    @ColumnInfo(name = "specialization")
    val specialization: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "avg_impact")
    val averageImpact: Double
)