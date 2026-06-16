package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayerReactionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerReactionsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM player_reactions ORDER BY id DESC")
    fun getAll(): Flow<List<PlayerReactionsEntity>>

    @Query("SELECT * FROM player_reactions WHERE id = :id")
    suspend fun getById(id: Int): PlayerReactionsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reaction: PlayerReactionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reactions: List<PlayerReactionsEntity>)

    @Update
    suspend fun update(reaction: PlayerReactionsEntity)

    @Delete
    suspend fun delete(reaction: PlayerReactionsEntity)

    @Query("DELETE FROM player_reactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM player_reactions")
    suspend fun getAllStatic(): List<PlayerReactionsEntity>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM player_reactions WHERE player_id = :playerId ORDER BY id DESC")
    fun getReactionsByPlayerId(playerId: Int): Flow<List<PlayerReactionsEntity>>

    @Query("SELECT * FROM player_reactions WHERE player_name = :playerName ORDER BY id DESC")
    fun getReactionsByPlayerName(playerName: String): Flow<List<PlayerReactionsEntity>>

    @Query("SELECT * FROM player_reactions WHERE id IN (SELECT MAX(id) FROM player_reactions WHERE player_id IN (:playerIds) GROUP BY player_id)")
    suspend fun getLatestReactionsForPlayers(playerIds: List<Int>): List<PlayerReactionsEntity>

    @Query("SELECT COUNT(*) FROM player_reactions WHERE player_id = :playerId AND reaction_type IN ('Happy', 'Excited', 'Proud')")
    suspend fun getPositiveReactionCount(playerId: Int): Int

    @Query("SELECT COUNT(*) FROM player_reactions WHERE player_id = :playerId AND reaction_type IN ('Angry', 'Frustrated', 'Disappointed', 'Sad')")
    suspend fun getNegativeReactionCount(playerId: Int): Int

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            reaction_type,
            COUNT(*) as count
        FROM player_reactions 
        GROUP BY reaction_type
        ORDER BY count DESC
    """)
    fun getReactionTypeDistribution(): Flow<List<PlayerReactionDistribution>>

    @Query("""
        SELECT 
            player_id,
            player_name,
            COUNT(*) as reaction_count,
            COUNT(CASE WHEN reaction_type IN ('Happy', 'Excited', 'Proud') THEN 1 END) as positive_count,
            COUNT(CASE WHEN reaction_type IN ('Angry', 'Frustrated', 'Disappointed', 'Sad') THEN 1 END) as negative_count
        FROM player_reactions 
        GROUP BY player_id
        ORDER BY reaction_count DESC
        LIMIT :limit
    """)
    fun getMostReactivePlayers(limit: Int): Flow<List<PlayerReactivityStats>>
}

// ============ DATA CLASSES ============

data class PlayerReactionDistribution(
    @ColumnInfo(name = "reaction_type")
    val reactionType: String,

    @ColumnInfo(name = "count")
    val count: Int
)

data class PlayerReactivityStats(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "reaction_count")
    val reactionCount: Int,

    @ColumnInfo(name = "positive_count")
    val positiveCount: Int,

    @ColumnInfo(name = "negative_count")
    val negativeCount: Int
)
