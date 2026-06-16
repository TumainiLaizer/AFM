package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.SettingsHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsHistoryDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM settings_history ORDER BY changed_at DESC")
    fun getAll(): Flow<List<SettingsHistoryEntity>>

    @Query("SELECT * FROM settings_history WHERE id = :id")
    suspend fun getById(id: Int): SettingsHistoryEntity?

    @Query("SELECT * FROM settings_history")
    suspend fun getAllStatic(): List<SettingsHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SettingsHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<SettingsHistoryEntity>)

    @Update
    suspend fun update(history: SettingsHistoryEntity)

    @Delete
    suspend fun delete(history: SettingsHistoryEntity)

    @Query("DELETE FROM settings_history")
    suspend fun deleteAll()

    // ============ SETTINGS-BASED QUERIES ============

    @Query("SELECT * FROM settings_history WHERE settings_id = :settingsId ORDER BY changed_at DESC")
    fun getHistoryForSettings(settingsId: Int): Flow<List<SettingsHistoryEntity>>

    @Query("SELECT * FROM settings_history WHERE settings_id = :settingsId AND changed_field = :field ORDER BY changed_at DESC")
    fun getHistoryForField(settingsId: Int, field: String): Flow<List<SettingsHistoryEntity>>

    // ============ TIME-BASED QUERIES ============

    @Query("SELECT * FROM settings_history WHERE changed_at >= :startTime ORDER BY changed_at DESC")
    fun getHistorySince(startTime: Long): Flow<List<SettingsHistoryEntity>>

    @Query("SELECT * FROM settings_history WHERE changed_at BETWEEN :startTime AND :endTime ORDER BY changed_at DESC")
    fun getHistoryBetween(startTime: Long, endTime: Long): Flow<List<SettingsHistoryEntity>>

    // ============ USER-BASED QUERIES ============

    @Query("SELECT * FROM settings_history WHERE changed_by = :changedBy ORDER BY changed_at DESC")
    fun getHistoryByUser(changedBy: String): Flow<List<SettingsHistoryEntity>>

    // ============ STATISTICS ============

    @Query("SELECT COUNT(*) FROM settings_history WHERE settings_id = :settingsId")
    suspend fun getChangeCount(settingsId: Int): Int

    @Query("SELECT changed_field, COUNT(*) as change_count FROM settings_history WHERE settings_id = :settingsId GROUP BY changed_field ORDER BY change_count DESC")
    fun getChangeFrequency(settingsId: Int): Flow<List<ChangeFrequency>>

    @Query("SELECT MAX(changed_at) FROM settings_history WHERE settings_id = :settingsId")
    suspend fun getLastChangeTime(settingsId: Int): Long?

    // ============ CLEANUP ============

    @Query("DELETE FROM settings_history WHERE changed_at < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)
}

// ============ DATA CLASSES ============

data class ChangeFrequency(
    @ColumnInfo(name = "changed_field")
    val changedField: String,

    @ColumnInfo(name = "change_count")
    val changeCount: Int
)