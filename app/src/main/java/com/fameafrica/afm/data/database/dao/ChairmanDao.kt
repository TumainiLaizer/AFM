package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ChairmanEntity

@Dao
interface ChairmanDao {
    @Query("SELECT * FROM chairmen WHERE is_available = 1")
    suspend fun getAvailableChairmen(): List<ChairmanEntity>

    @Query("SELECT * FROM chairmen WHERE is_available = 1 AND preferred_region = :region")
    suspend fun getAvailableChairmenByRegion(region: String): List<ChairmanEntity>

    @Query("SELECT * FROM chairmen WHERE team_id = :teamId LIMIT 1")
    suspend fun getByTeamId(teamId: Int): ChairmanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChairman(chairman: ChairmanEntity)

    @Update
    suspend fun updateChairman(chairman: ChairmanEntity)

    // ➕ INSERT / UPDATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(chairman: ChairmanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chairmanList: List<ChairmanEntity>)

    @Query("UPDATE chairmen SET is_available = 0 WHERE id = :chairmanId")
    suspend fun markAsUnavailable(chairmanId: Int)

    @Query("DELETE FROM chairmen")
    suspend fun deleteAll()
    @Query("SELECT * FROM chairmen ORDER BY name ASC")
    suspend fun getAllStatic(): List<ChairmanEntity>
}
