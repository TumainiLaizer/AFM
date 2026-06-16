package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ClubDNAEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDNADao {

    // 🔍 SINGLE FETCH
    @Query("SELECT * FROM club_dna WHERE teamId = :teamId")
    suspend fun getClubDNA(teamId: Int): ClubDNAEntity?

    // 🔄 OBSERVE (CRITICAL FOR UI + LIVE SYSTEMS)
    @Query("SELECT * FROM club_dna WHERE teamId = :teamId")
    fun observeClubDNA(teamId: Int): Flow<ClubDNAEntity?>

    // 📊 FETCH ALL (FOR WORLD SIMULATION)
    @Query("SELECT * FROM club_dna")
    suspend fun getAllDNA(): List<ClubDNAEntity>

    @Query("SELECT * FROM club_dna")
    fun observeAllDNA(): Flow<List<ClubDNAEntity>>

    // ➕ INSERT / UPDATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dna: ClubDNAEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dnaList: List<ClubDNAEntity>)

    // =====================================================
    // ⚡ PARTIAL UPDATES (PERFORMANCE CRITICAL)
    // =====================================================

    @Query("""
        UPDATE club_dna 
        SET identity_strength = :value 
        WHERE teamId = :teamId
    """)
    suspend fun updateIdentityStrength(teamId: Int, value: Int)

    @Query("""
        UPDATE club_dna 
        SET play_style_secondary = :style 
        WHERE teamId = :teamId
    """)
    suspend fun updateSecondaryStyle(teamId: Int, style: String?)

    @Query("""
        UPDATE club_dna 
        SET transfer_policy = :policy 
        WHERE teamId = :teamId
    """)
    suspend fun updateTransferPolicy(teamId: Int, policy: String)

    @Query("""
        UPDATE club_dna 
        SET financial_behavior = :behavior 
        WHERE teamId = :teamId
    """)
    suspend fun updateFinancialBehavior(teamId: Int, behavior: String)

    // =====================================================
    // 🧠 AI QUERY HELPERS (VERY IMPORTANT)
    // =====================================================

    @Query("""
        SELECT * FROM club_dna 
        WHERE financial_behavior = :behavior
    """)
    suspend fun getClubsByFinancialBehavior(behavior: String): List<ClubDNAEntity>

    @Query("""
        SELECT * FROM club_dna 
        WHERE transfer_policy = :policy
    """)
    suspend fun getClubsByTransferPolicy(policy: String): List<ClubDNAEntity>

    @Query("""
        SELECT * FROM club_dna 
        WHERE play_style = :style
    """)
    suspend fun getClubsByPlayStyle(style: String): List<ClubDNAEntity>

    @Query("""
        SELECT * FROM club_dna 
        WHERE region = :region
    """)
    suspend fun getClubsByRegion(region: String): List<ClubDNAEntity>

    // =====================================================
    // 🔄 RESET / DEBUG (OPTIONAL BUT USEFUL)
    // =====================================================

    @Query("DELETE FROM club_dna")
    suspend fun clearAll()
    @Query("SELECT * FROM club_dna")
    suspend fun getAllStatic(): List<ClubDNAEntity>
}
