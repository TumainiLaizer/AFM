package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayerContractsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerContractsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM player_contracts ORDER BY teamName, playerName")
    fun getAll(): Flow<List<PlayerContractsEntity>>

    @Query("SELECT * FROM player_contracts WHERE id = :id")
    suspend fun getById(id: Int): PlayerContractsEntity?

    @Query("SELECT * FROM player_contracts WHERE playerName = :playerName")
    suspend fun getByPlayerName(playerName: String): PlayerContractsEntity?

    @Query("SELECT * FROM player_contracts WHERE playerId = :playerId")
    suspend fun getByPlayerId(playerId: Int): PlayerContractsEntity?

    @Query("SELECT * FROM player_contracts WHERE playerId IN (:playerIds)")
    suspend fun getByPlayerIds(playerIds: List<Int>): List<PlayerContractsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contract: PlayerContractsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contracts: List<PlayerContractsEntity>)

    @Update
    suspend fun update(contract: PlayerContractsEntity)

    @Delete
    suspend fun delete(contract: PlayerContractsEntity)

    @Query("DELETE FROM player_contracts")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM player_contracts")
    suspend fun getCount(): Int

    @Query("SELECT * FROM player_contracts")
    suspend fun getAllStatic(): List<PlayerContractsEntity>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM player_contracts WHERE teamName = :teamName ORDER BY salary DESC")
    fun getContractsByTeam(teamName: String): Flow<List<PlayerContractsEntity>>

    @Query("SELECT * FROM player_contracts WHERE teamName = :teamName AND contractStatus = 'ACTIVE' ORDER BY salary DESC")
    fun getActiveContractsByTeam(teamName: String): Flow<List<PlayerContractsEntity>>

    @Query("SELECT SUM(salary) FROM player_contracts WHERE teamName = :teamName AND contractStatus = 'ACTIVE'")
    suspend fun getTotalWageBill(teamName: String): Int?

    @Query("SELECT AVG(salary) FROM player_contracts WHERE teamName = :teamName")
    suspend fun getAverageWage(teamName: String): Double?

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM player_contracts WHERE contractStatus = 'EXPIRING' ORDER BY contractEndDate ASC")
    fun getExpiringContracts(): Flow<List<PlayerContractsEntity>>

    @Query("SELECT * FROM player_contracts WHERE contractStatus = 'EXPIRED' ORDER BY playerName")
    fun getExpiredContracts(): Flow<List<PlayerContractsEntity>>

    @Query("SELECT * FROM player_contracts WHERE contractEndDate < date('now', '+6 months') AND contractStatus = 'ACTIVE'")
    fun getContractsExpiringSoon(): Flow<List<PlayerContractsEntity>>

    @Query("UPDATE player_contracts SET contractStatus = 'EXPIRING' WHERE contractEndDate < date('now', '+6 months') AND contractStatus = 'ACTIVE'")
    suspend fun updateExpiringStatus()

    @Query("UPDATE player_contracts SET contractStatus = 'EXPIRED' WHERE contractEndDate < date('now') AND contractStatus != 'EXPIRED'")
    suspend fun updateExpiredStatus()

    // ============ SALARY QUERIES ============

    @Query("SELECT * FROM player_contracts WHERE salary >= :minSalary ORDER BY salary DESC")
    fun getHighEarners(minSalary: Int): Flow<List<PlayerContractsEntity>>

    @Query("SELECT * FROM player_contracts ORDER BY salary DESC LIMIT :limit")
    fun getTopEarners(limit: Int): Flow<List<PlayerContractsEntity>>

    // ============ RELEASE CLAUSE QUERIES ============

    @Query("SELECT * FROM player_contracts WHERE releaseClause < :maxClause AND isNegotiable = 1 ORDER BY releaseClause ASC")
    fun getPlayersWithReasonableReleaseClauses(maxClause: Int): Flow<List<PlayerContractsEntity>>

    @Query("SELECT * FROM player_contracts WHERE releaseClause >= :minClause ORDER BY releaseClause DESC")
    fun getPlayersWithHighReleaseClauses(minClause: Int): Flow<List<PlayerContractsEntity>>

    // ============ NEGOTIATION QUERIES ============

    @Query("SELECT * FROM player_contracts WHERE isNegotiable = 1 AND contractStatus IN ('ACTIVE', 'EXPIRING')")
    fun getNegotiableContracts(): Flow<List<PlayerContractsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            teamName,
            COUNT(*) as contract_count,
            SUM(salary) as total_wage_bill,
            AVG(salary) as avg_wage,
            MIN(salary) as min_wage,
            MAX(salary) as max_wage
        FROM player_contracts 
        WHERE contractStatus = 'ACTIVE'
        GROUP BY teamName
        ORDER BY total_wage_bill DESC
    """)
    fun getTeamWageStatistics(): Flow<List<TeamWageStats>>
}

data class TeamWageStats(
    @ColumnInfo(name = "teamName")
    val teamName: String,
    @ColumnInfo(name = "contract_count")
    val contractCount: Int,
    @ColumnInfo(name = "total_wage_bill")
    val totalWageBill: Int,
    @ColumnInfo(name = "avg_wage")
    val averageWage: Double,
    @ColumnInfo(name = "min_wage")
    val minimumWage: Int,
    @ColumnInfo(name = "max_wage")
    val maximumWage: Int
)
