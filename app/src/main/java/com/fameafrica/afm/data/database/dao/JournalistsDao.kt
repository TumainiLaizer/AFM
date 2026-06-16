package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.JournalistsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalistsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM journalists ORDER BY name")
    fun getAll(): Flow<List<JournalistsEntity>>

    @Query("SELECT * FROM journalists WHERE id = :id")
    suspend fun getById(id: Int): JournalistsEntity?

    @Query("SELECT * FROM journalists WHERE name = :name")
    suspend fun getByName(name: String): JournalistsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journalist: JournalistsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(journalists: List<JournalistsEntity>)

    @Update
    suspend fun update(journalist: JournalistsEntity)

    @Delete
    suspend fun delete(journalist: JournalistsEntity)

    // ============ PERSONALITY QUERIES ============

    @Query("SELECT * FROM journalists WHERE personality = :personality ORDER BY name")
    fun getByPersonality(personality: String): Flow<List<JournalistsEntity>>

    @Query("SELECT * FROM journalists WHERE personality = 'Friendly'")
    fun getFriendlyJournalists(): Flow<List<JournalistsEntity>>

    @Query("SELECT * FROM journalists WHERE personality = 'Hostile'")
    fun getHostileJournalists(): Flow<List<JournalistsEntity>>

    @Query("SELECT * FROM journalists WHERE personality = 'Sensationalist'")
    fun getSensationalistJournalists(): Flow<List<JournalistsEntity>>

    // ============ COMPANY QUERIES ============

    @Query("SELECT * FROM journalists WHERE media_company = :company ORDER BY name")
    fun getByCompany(company: String): Flow<List<JournalistsEntity>>

    // ============ EXPERTISE QUERIES ============

    @Query("SELECT * FROM journalists WHERE expertise = :expertise ORDER BY name")
    fun getByExpertise(expertise: String): Flow<List<JournalistsEntity>>

    @Query("SELECT * FROM journalists WHERE expertise = 'Transfer News'")
    fun getTransferExperts(): Flow<List<JournalistsEntity>>

    @Query("SELECT * FROM journalists WHERE expertise = 'Tactical Analysis'")
    fun getTacticalAnalysts(): Flow<List<JournalistsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            personality,
            COUNT(*) as count
        FROM journalists 
        GROUP BY personality
    """)
    fun getPersonalityDistribution(): Flow<List<JournalistPersonalityDistribution>>
    @Query("SELECT * FROM journalists ORDER BY name ASC")
    suspend fun getAllStatic(): List<JournalistsEntity>
}

// ============ DATA CLASSES ============

data class JournalistPersonalityDistribution(
    @ColumnInfo(name = "personality")
    val personality: String,

    @ColumnInfo(name = "count")
    val count: Int
)