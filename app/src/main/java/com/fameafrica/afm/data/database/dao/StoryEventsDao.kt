package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.StoryEventsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryEventsDao {
    @Query("SELECT * FROM story_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<StoryEventsEntity>>

    @Query("SELECT * FROM story_events WHERE teamId = :teamId AND isResolved = 0")
    fun getUnresolvedByTeam(teamId: Int): Flow<List<StoryEventsEntity>>

    @Query("SELECT * FROM story_events WHERE isGlobal = 1 AND isResolved = 0")
    fun getUnresolvedGlobal(): Flow<List<StoryEventsEntity>>

    @Query("SELECT * FROM story_events WHERE (:teamId IS NULL AND isGlobal = 1 AND isResolved = 0) OR (teamId = :teamId AND isResolved = 0)")
    fun getUnresolved(teamId: Int?): Flow<List<StoryEventsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: StoryEventsEntity): Long

    @Update
    suspend fun update(event: StoryEventsEntity)

    @Query("UPDATE story_events SET isResolved = 1 WHERE id = :eventId")
    suspend fun resolveEvent(eventId: Int)

    @Query("SELECT * FROM story_events")
    suspend fun getAllStatic(): List<StoryEventsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<StoryEventsEntity>)
}
