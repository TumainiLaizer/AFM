package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.ShortlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortlistDao {
    @Query("""
        SELECT p.* FROM players p
        INNER JOIN shortlist s ON p.id = s.player_id
        ORDER BY s.added_date DESC
    """)
    fun getShortlistedPlayers(): Flow<List<PlayersEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToShortlist(shortlist: ShortlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shortlist: List<ShortlistEntity>)

    @Query("SELECT * FROM shortlist")
    suspend fun getAllStatic(): List<ShortlistEntity>

    @Query("DELETE FROM shortlist WHERE player_id = :playerId")
    suspend fun removeFromShortlist(playerId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM shortlist WHERE player_id = :playerId)")
    fun isShortlisted(playerId: Int): Flow<Boolean>
}
