package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayerLoansEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerLoansDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM player_loans ORDER BY start_date DESC")
    fun getAll(): Flow<List<PlayerLoansEntity>>

    @Query("SELECT * FROM player_loans WHERE id = :id")
    suspend fun getById(id: Int): PlayerLoansEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: PlayerLoansEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(loans: List<PlayerLoansEntity>)

    @Update
    suspend fun update(loan: PlayerLoansEntity)

    @Delete
    suspend fun delete(loan: PlayerLoansEntity)

    @Query("DELETE FROM player_loans")
    suspend fun deleteAll()

    @Query("SELECT * FROM player_loans")
    suspend fun getAllStatic(): List<PlayerLoansEntity>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM player_loans WHERE player_name = :playerName ORDER BY start_date DESC")
    fun getLoansByPlayer(playerName: String): Flow<List<PlayerLoansEntity>>

    @Query("SELECT * FROM player_loans WHERE player_name = :playerName AND status = 'Active'")
    suspend fun getActiveLoanByPlayer(playerName: String): PlayerLoansEntity?

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM player_loans WHERE loaning_team = :teamName AND status = 'Active' ORDER BY end_date ASC")
    fun getPlayersOutOnLoan(teamName: String): Flow<List<PlayerLoansEntity>>

    @Query("SELECT * FROM player_loans WHERE receiving_team = :teamName AND status = 'Active' ORDER BY end_date ASC")
    fun getPlayersInOnLoan(teamName: String): Flow<List<PlayerLoansEntity>>

    @Query("SELECT * FROM player_loans WHERE loaning_team = :teamName OR receiving_team = :teamName ORDER BY start_date DESC")
    fun getAllTeamLoans(teamName: String): Flow<List<PlayerLoansEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM player_loans WHERE status = 'Active' ORDER BY end_date ASC")
    fun getActiveLoans(): Flow<List<PlayerLoansEntity>>

    @Query("SELECT * FROM player_loans WHERE status = 'Active' AND end_date < date('now')")
    fun getOverdueLoans(): Flow<List<PlayerLoansEntity>>

    @Query("SELECT * FROM player_loans WHERE status = 'Active' AND option_to_buy = 1")
    fun getLoansWithBuyOption(): Flow<List<PlayerLoansEntity>>

    @Query("SELECT * FROM player_loans WHERE status = 'Completed' ORDER BY end_date DESC")
    fun getCompletedLoans(): Flow<List<PlayerLoansEntity>>

    // ============ SEASON QUERIES ============

    @Query("SELECT * FROM player_loans WHERE season = :season ORDER BY start_date")
    fun getLoansBySeason(season: String): Flow<List<PlayerLoansEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            loaning_team,
            COUNT(*) as players_out
        FROM player_loans 
        WHERE status = 'Active'
        GROUP BY loaning_team
        ORDER BY players_out DESC
    """)
    fun getTeamsWithMostPlayersOut(): Flow<List<TeamLoanStats>>

    @Query("""
        SELECT 
            receiving_team,
            COUNT(*) as players_in
        FROM player_loans 
        WHERE status = 'Active'
        GROUP BY receiving_team
        ORDER BY players_in DESC
    """)
    fun getTeamsWithMostPlayersIn(): Flow<List<TeamLoanInStats>>
}

// ============ DATA CLASSES ============

data class TeamLoanStats(
    @ColumnInfo(name = "loaning_team")
    val teamName: String,

    @ColumnInfo(name = "players_out")
    val playersOut: Int
)

data class TeamLoanInStats(
    @ColumnInfo(name = "receiving_team")
    val teamName: String,

    @ColumnInfo(name = "players_in")
    val playersIn: Int
)