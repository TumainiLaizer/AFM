package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.WorldStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldStateDao {
    @Query("SELECT * FROM world_state WHERE id = 1")
    fun getWorldState(): Flow<WorldStateEntity?>

    @Query("SELECT * FROM world_state")
    suspend fun getAllStatic(): List<WorldStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(worldState: WorldStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(worldStates: List<WorldStateEntity>)
}
