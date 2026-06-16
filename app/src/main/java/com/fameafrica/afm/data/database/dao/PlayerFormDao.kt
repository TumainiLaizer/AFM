package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayerFormEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerFormDao {
    @Query("SELECT * FROM player_form WHERE playerId = :playerId")
    suspend fun getByPlayerId(playerId: Int): PlayerFormEntity?

    @Query("SELECT * FROM player_form")
    fun getAll(): Flow<List<PlayerFormEntity>>

    @Query("SELECT * FROM player_form")
    suspend fun getAllStatic(): List<PlayerFormEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(form: PlayerFormEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(forms: List<PlayerFormEntity>)

    @Delete
    suspend fun delete(form: PlayerFormEntity)

    @Query("DELETE FROM player_form")
    suspend fun deleteAll()
}
