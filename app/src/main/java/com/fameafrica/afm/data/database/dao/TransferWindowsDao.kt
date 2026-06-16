package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.TransferWindowsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferWindowsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM transfer_windows ORDER BY start_date DESC")
    fun getAll(): Flow<List<TransferWindowsEntity>>

    @Query("SELECT * FROM transfer_windows WHERE id = :id")
    suspend fun getById(id: Int): TransferWindowsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(window: TransferWindowsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(windows: List<TransferWindowsEntity>)

    @Update
    suspend fun update(window: TransferWindowsEntity)

    @Delete
    suspend fun delete(window: TransferWindowsEntity)

    @Query("DELETE FROM transfer_windows")
    suspend fun deleteAll()

    @Query("SELECT * FROM transfer_windows")
    suspend fun getAllStatic(): List<TransferWindowsEntity>

    // ============ ACTIVE WINDOW QUERIES ============

    @Query("SELECT * FROM transfer_windows WHERE is_open = 1 ORDER BY end_date ASC")
    fun getActiveWindows(): Flow<List<TransferWindowsEntity>>

    @Query("SELECT * FROM transfer_windows WHERE is_open = 1 AND window_type = :windowType ORDER BY end_date ASC")
    fun getActiveWindowsByType(windowType: String): Flow<List<TransferWindowsEntity>>

    @Query("SELECT * FROM transfer_windows WHERE is_open = 1 AND date('now') BETWEEN start_date AND end_date")
    suspend fun getCurrentActiveWindow(): TransferWindowsEntity?

    @Query("SELECT * FROM transfer_windows WHERE date('now') BETWEEN start_date AND end_date")
    suspend fun getCurrentWindow(): TransferWindowsEntity?

    // ============ SEASON QUERIES ============

    @Query("SELECT * FROM transfer_windows WHERE season = :season ORDER BY start_date")
    fun getWindowsBySeason(season: String): Flow<List<TransferWindowsEntity>>

    @Query("SELECT * FROM transfer_windows WHERE season = :season AND window_type = 'SUMMER'")
    suspend fun getSummerWindow(season: String): TransferWindowsEntity?

    @Query("SELECT * FROM transfer_windows WHERE season = :season AND window_type = 'WINTER'")
    suspend fun getWinterWindow(season: String): TransferWindowsEntity?

    // ============ DATE QUERIES ============

    @Query("SELECT * FROM transfer_windows WHERE start_date <= :date AND end_date >= :date")
    suspend fun getWindowByDate(date: String): TransferWindowsEntity?

    @Query("SELECT * FROM transfer_windows WHERE end_date < :date ORDER BY end_date DESC LIMIT 1")
    suspend fun getLastClosedWindow(date: String): TransferWindowsEntity?

    // ============ STATUS UPDATES ============

    @Query("UPDATE transfer_windows SET is_open = 1 WHERE id = :id")
    suspend fun openWindow(id: Int)

    @Query("UPDATE transfer_windows SET is_open = 0 WHERE id = :id")
    suspend fun closeWindow(id: Int)

    @Query("UPDATE transfer_windows SET is_open = 0 WHERE end_date < date('now') AND is_open = 1")
    suspend fun autoCloseExpiredWindows()

    // ============ STATISTICS QUERIES ============

    @Query("SELECT COUNT(*) FROM transfer_windows WHERE is_open = 1")
    suspend fun getOpenWindowsCount(): Int

    @Query("SELECT DISTINCT season FROM transfer_windows ORDER BY season DESC")
    fun getSeasons(): Flow<List<String>>
}

// ============ DATA CLASSES ============

data class WindowStatus(
    @ColumnInfo(name = "is_open")
    val isOpen: Boolean,

    @ColumnInfo(name = "days_remaining")
    val daysRemaining: Int
)