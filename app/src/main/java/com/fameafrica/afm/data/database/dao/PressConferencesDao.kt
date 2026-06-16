package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PressConferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PressConferencesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM press_conferences ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PressConferencesEntity>>

    @Query("SELECT * FROM press_conferences WHERE id = :id")
    suspend fun getById(id: Int): PressConferencesEntity?

    @Query("SELECT * FROM press_conferences")
    suspend fun getAllStatic(): List<PressConferencesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pressConference: PressConferencesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pressConferences: List<PressConferencesEntity>)

    @Update
    suspend fun update(pressConference: PressConferencesEntity)

    @Delete
    suspend fun delete(pressConference: PressConferencesEntity)

    // ============ MANAGER-BASED QUERIES ============

    @Query("SELECT * FROM press_conferences WHERE manager_id = :managerId ORDER BY timestamp DESC")
    fun getByManager(managerId: Int): Flow<List<PressConferencesEntity>>

    @Query("SELECT * FROM press_conferences WHERE manager_id = :managerId AND is_published = 1 ORDER BY timestamp DESC")
    fun getPublishedByManager(managerId: Int): Flow<List<PressConferencesEntity>>

    @Query("SELECT * FROM press_conferences WHERE manager_id = :managerId AND selected_response IS NULL ORDER BY timestamp ASC")
    fun getPendingPressConferences(managerId: Int): Flow<List<PressConferencesEntity>>

    // ============ JOURNALIST QUERIES ============

    @Query("SELECT * FROM press_conferences WHERE journalist_name = :journalistName ORDER BY timestamp DESC")
    fun getByJournalist(journalistName: String): Flow<List<PressConferencesEntity>>

    // ============ CATEGORY QUERIES ============

    @Query("SELECT * FROM press_conferences WHERE question_category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: String): Flow<List<PressConferencesEntity>>

    // ============ IMPACT QUERIES ============

    @Query("SELECT * FROM press_conferences WHERE impact_on_team >= 5 ORDER BY timestamp DESC")
    fun getHighImpactPressConferences(): Flow<List<PressConferencesEntity>>

    @Query("SELECT * FROM press_conferences WHERE impact_on_team <= -5 ORDER BY timestamp DESC")
    fun getNegativeImpactPressConferences(): Flow<List<PressConferencesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            response_type_a as response_type,
            COUNT(*) as count
        FROM press_conferences 
        WHERE selected_response IS NOT NULL
        GROUP BY response_type_a
    """)
    fun getResponseTypeDistribution(): Flow<List<ResponseTypeDistribution>>

    @Query("""
        SELECT 
            AVG(impact_on_team) as avg_impact,
            AVG(reputation_change) as avg_reputation_change
        FROM press_conferences 
        WHERE manager_id = :managerId
    """)
    suspend fun getManagerPressStats(managerId: Int): ManagerPressStats?
}

// ============ DATA CLASSES ============

data class ResponseTypeDistribution(
    @ColumnInfo(name = "response_type")
    val responseType: String,

    @ColumnInfo(name = "count")
    val count: Int
)

data class ManagerPressStats(
    @ColumnInfo(name = "avg_impact")
    val averageImpact: Double,

    @ColumnInfo(name = "avg_reputation_change")
    val averageReputationChange: Double
)