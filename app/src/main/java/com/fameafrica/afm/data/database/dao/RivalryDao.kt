package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.RivalryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RivalryDao {
    @Query("SELECT * FROM rivalries")
    fun getAll(): Flow<List<RivalryEntity>>

    @Query("SELECT * FROM rivalries")
    suspend fun getAllStatic(): List<RivalryEntity>

    @Query("SELECT * FROM rivalries WHERE team_a_id = :teamId OR team_b_id = :teamId")
    fun getRivalriesForTeam(teamId: Int): Flow<List<RivalryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rivalry: RivalryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rivalries: List<RivalryEntity>)

    @Delete
    suspend fun delete(rivalry: RivalryEntity)
}
