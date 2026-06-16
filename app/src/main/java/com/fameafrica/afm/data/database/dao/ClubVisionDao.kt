package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ClubVisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubVisionDao {

    // 🔍 GETTERS
    @Query("SELECT * FROM club_vision WHERE teamId = :teamId")
    fun getVisionForTeam(teamId: Int): Flow<ClubVisionEntity?>

    @Query("SELECT * FROM club_vision")
    fun getAllVisions(): Flow<List<ClubVisionEntity>>

    @Query("SELECT * FROM club_vision")
    suspend fun getAllStatic(): List<ClubVisionEntity>

    // ➕ INSERT / UPDATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vision: ClubVisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visions: List<ClubVisionEntity>)

    @Update
    suspend fun update(vision: ClubVisionEntity)

    // 🎯 CORE MANAGER METRICS

    @Query("""
        UPDATE club_vision 
        SET philosophy_score = :score 
        WHERE teamId = :teamId
    """)
    suspend fun updatePhilosophyScore(teamId: Int, score: Int)

    @Query("""
        UPDATE club_vision 
        SET vision_alignment = :alignment 
        WHERE teamId = :teamId
    """)
    suspend fun updateVisionAlignment(teamId: Int, alignment: Int)

    // 🔥 NEW: BOARD PRESSURE SYSTEM

    @Query("""
        UPDATE club_vision 
        SET board_pressure = :pressure 
        WHERE teamId = :teamId
    """)
    suspend fun updateBoardPressure(teamId: Int, pressure: Int)

    @Query("""
        UPDATE club_vision 
        SET job_security = :security 
        WHERE teamId = :teamId
    """)
    suspend fun updateJobSecurity(teamId: Int, security: Int)

    // ⚡ COMBINED UPDATE (VERY IMPORTANT FOR PERFORMANCE)

    @Query("""
        UPDATE club_vision 
        SET 
            philosophy_score = :philosophy,
            vision_alignment = :alignment,
            board_pressure = :pressure,
            job_security = :security
        WHERE teamId = :teamId
    """)
    suspend fun updateManagerState(
        teamId: Int,
        philosophy: Int,
        alignment: Int,
        pressure: Int,
        security: Int
    )

    // 🧠 EXPECTATION ADJUSTMENTS (DYNAMIC SYSTEM)

    @Query("""
        UPDATE club_vision 
        SET 
            youth_expectation = :youth,
            financial_discipline = :discipline
        WHERE teamId = :teamId
    """)
    suspend fun updateExpectations(
        teamId: Int,
        youth: Int,
        discipline: Int
    )

    // 🔄 RESET / INITIALIZATION

    @Query("""
        UPDATE club_vision 
        SET 
            board_pressure = 50,
            job_security = 70,
            philosophy_score = 50,
            vision_alignment = 50
        WHERE teamId = :teamId
    """)
    suspend fun resetVisionState(teamId: Int)
}