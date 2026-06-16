package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.FanExpectationsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FanExpectationsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM fan_expectations")
    fun getAll(): Flow<List<FanExpectationsEntity>>

    @Query("SELECT * FROM fan_expectations WHERE id = :id")
    suspend fun getById(id: Int): FanExpectationsEntity?

    @Query("SELECT * FROM fan_expectations")
    suspend fun getAllStatic(): List<FanExpectationsEntity>

    @Query("SELECT * FROM fan_expectations WHERE team_id = :teamId")
    suspend fun getByTeamId(teamId: Int): FanExpectationsEntity?

    @Query("SELECT * FROM fan_expectations WHERE team_name = :teamName")
    suspend fun getByTeamName(teamName: String): FanExpectationsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expectation: FanExpectationsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expectations: List<FanExpectationsEntity>)

    @Update
    suspend fun update(expectation: FanExpectationsEntity)

    @Delete
    suspend fun delete(expectation: FanExpectationsEntity)

    @Query("DELETE FROM fan_expectations")
    suspend fun deleteAll()

    // ============ CONFIDENCE QUERIES ============

    @Query("SELECT * FROM fan_expectations WHERE confidence_level >= :minConfidence ORDER BY confidence_level DESC")
    fun getHighConfidence(minConfidence: Int): Flow<List<FanExpectationsEntity>>

    @Query("SELECT * FROM fan_expectations WHERE confidence_level <= :maxConfidence ORDER BY confidence_level ASC")
    fun getLowConfidence(maxConfidence: Int): Flow<List<FanExpectationsEntity>>

    @Query("SELECT AVG(confidence_level) FROM fan_expectations")
    suspend fun getAverageConfidence(): Double?

    // ============ TRUST QUERIES ============

    @Query("SELECT * FROM fan_expectations WHERE board_trust >= :minTrust ORDER BY board_trust DESC")
    fun getHighTrust(minTrust: Int): Flow<List<FanExpectationsEntity>>

    @Query("SELECT * FROM fan_expectations WHERE board_trust <= :maxTrust ORDER BY board_trust ASC")
    fun getLowTrust(maxTrust: Int): Flow<List<FanExpectationsEntity>>

    @Query("SELECT AVG(board_trust) FROM fan_expectations")
    suspend fun getAverageTrust(): Double?

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            CASE 
                WHEN confidence_level >= 60 AND board_trust >= 60 THEN 'Positive'
                WHEN confidence_level <= 40 OR board_trust <= 40 THEN 'Negative'
                ELSE 'Neutral'
            END as mood,
            COUNT(*) as team_count
        FROM fan_expectations 
        GROUP BY mood
    """)
    fun getFanMoodDistribution(): Flow<List<FanMoodDistribution>>
}

// ============ DATA CLASSES ============

data class FanMoodDistribution(
    @ColumnInfo(name = "mood")
    val mood: String,

    @ColumnInfo(name = "team_count")
    val teamCount: Int
)
