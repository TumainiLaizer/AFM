package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.GameStatesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM game_states ORDER BY last_played DESC")
    fun getAll(): Flow<List<GameStatesEntity>>

    @Query("SELECT * FROM game_states WHERE id = :id")
    suspend fun getById(id: Int): GameStatesEntity?

    @Query("SELECT * FROM game_states WHERE manager_id = :managerId")
    suspend fun getByManagerId(managerId: Int): GameStatesEntity?

    @Query("SELECT * FROM game_states WHERE is_valid = 1 AND is_setup_complete = 1 ORDER BY last_played DESC")
    fun getValidSaveGames(): Flow<List<GameStatesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameState: GameStatesEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(saves: List<GameStatesEntity>)

    @Update
    suspend fun update(gameState: GameStatesEntity)

    @Delete
    suspend fun delete(gameState: GameStatesEntity)

    @Query("DELETE FROM game_states WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM game_states")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM game_states WHERE is_valid = 1 AND is_setup_complete = 1")
    suspend fun getValidSaveCount(): Int

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM game_states WHERE team_id = :teamId ORDER BY last_played DESC")
    fun getSaveGamesForTeam(teamId: Int): Flow<List<GameStatesEntity>>

    @Query("SELECT * FROM game_states WHERE team_name = :teamName ORDER BY last_played DESC")
    fun getSaveGamesForTeamName(teamName: String): Flow<List<GameStatesEntity>>

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT * FROM game_states WHERE season = :season ORDER BY last_played DESC")
    fun getSaveGamesForSeason(season: String): Flow<List<GameStatesEntity>>

    @Query("SELECT DISTINCT season FROM game_states ORDER BY season DESC")
    fun getSeasonsWithSaves(): Flow<List<String>>

    // ============ VALIDITY MANAGEMENT ============

    @Query("UPDATE game_states SET is_valid = 0 WHERE id = :id")
    suspend fun invalidateSave(id: Int)

    @Query("UPDATE game_states SET is_valid = 1 WHERE id = :id")
    suspend fun validateSave(id: Int)

    @Query("UPDATE game_states SET is_setup_complete = 1 WHERE id = :id")
    suspend fun markSetupComplete(id: Int)

    @Query("UPDATE game_states SET is_setup_complete = 0 WHERE id = :id")
    suspend fun markSetupIncomplete(id: Int)

    @Query("UPDATE game_states SET last_played = :lastPlayed WHERE id = :id")
    suspend fun updateLastPlayed(id: Int, lastPlayed: String)

    @Query("UPDATE game_states SET week = :week, last_played = :lastPlayed WHERE id = :id")
    suspend fun updateProgress(id: Int, week: Int, lastPlayed: String)

    // Finds the ID of the career that was most recently updated/played
    @Query("SELECT id FROM game_states ORDER BY last_played DESC LIMIT 1")
    suspend fun getLastActiveCareerId(): Int?

    // ============ SEARCH ============

    @Query("SELECT * FROM game_states WHERE name LIKE '%' || :searchQuery || '%' ORDER BY last_played DESC")
    fun searchSaveGames(searchQuery: String): Flow<List<GameStatesEntity>>

    // ============ STATISTICS ============

    @Query("SELECT COUNT(*) FROM game_states")
    suspend fun getTotalSaveCount(): Int

    @Query("SELECT * FROM game_states")
    suspend fun getAllStatic(): List<GameStatesEntity>

    @Query("SELECT AVG(week) FROM game_states WHERE is_valid = 1 AND is_setup_complete = 1")
    suspend fun getAverageWeek(): Double?

    @Query("SELECT MAX(week) FROM game_states WHERE is_valid = 1 AND is_setup_complete = 1")
    suspend fun getMaxWeek(): Int?
}
