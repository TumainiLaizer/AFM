package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.LeagueContextEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueContextDao {
    @Query("SELECT * FROM league_context")
    fun getAllLeagueContexts(): Flow<List<LeagueContextEntity>>

    @Query("SELECT * FROM league_context")
    suspend fun getAllStatic(): List<LeagueContextEntity>

    @Query("SELECT * FROM league_context WHERE leagueName = :leagueName")
    suspend fun getLeagueContext(leagueName: String): LeagueContextEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(context: LeagueContextEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contexts: List<LeagueContextEntity>)
}
