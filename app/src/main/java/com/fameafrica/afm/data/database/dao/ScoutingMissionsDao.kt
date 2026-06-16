package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ScoutingMissionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoutingMissionsDao {
    @Query("SELECT * FROM scouting_missions ORDER BY start_date DESC")
    fun getAll(): Flow<List<ScoutingMissionsEntity>>

    @Query("SELECT * FROM scouting_missions WHERE scout_id = :scoutId ORDER BY start_date DESC")
    fun getByScout(scoutId: Int): Flow<List<ScoutingMissionsEntity>>

    @Query("SELECT * FROM scouting_missions WHERE status = 'Active'")
    fun getActiveMissions(): Flow<List<ScoutingMissionsEntity>>

    @Query("SELECT * FROM scouting_missions WHERE scout_id = :scoutId AND status = 'Active' LIMIT 1")
    suspend fun getActiveMissionByScout(scoutId: Int): ScoutingMissionsEntity?

    @Query("SELECT * FROM scouting_missions")
    suspend fun getAllStatic(): List<ScoutingMissionsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: ScoutingMissionsEntity): Long

    @Update
    suspend fun update(mission: ScoutingMissionsEntity)

    @Delete
    suspend fun delete(mission: ScoutingMissionsEntity)

    @Query("UPDATE scouting_missions SET status = :status, end_date = :endDate WHERE id = :missionId")
    suspend fun updateStatus(missionId: Int, status: String, endDate: Long)

    @Query("UPDATE scouting_missions SET found_players_count = found_players_count + 1 WHERE id = :missionId")
    suspend fun incrementFoundCount(missionId: Int)
}
