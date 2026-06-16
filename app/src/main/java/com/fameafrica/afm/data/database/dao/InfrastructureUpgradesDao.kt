package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.InfrastructureUpgradesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InfrastructureUpgradesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM infrastructure_upgrades ORDER BY start_date DESC")
    fun getAll(): Flow<List<InfrastructureUpgradesEntity>>

    @Query("SELECT * FROM infrastructure_upgrades WHERE id = :id")
    suspend fun getById(id: Int): InfrastructureUpgradesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(upgrade: InfrastructureUpgradesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(upgrades: List<InfrastructureUpgradesEntity>)

    @Update
    suspend fun update(upgrade: InfrastructureUpgradesEntity)

    @Delete
    suspend fun delete(upgrade: InfrastructureUpgradesEntity)

    @Query("DELETE FROM infrastructure_upgrades WHERE team_id = :teamId")
    suspend fun deleteByTeam(teamId: Int)

    @Query("SELECT * FROM infrastructure_upgrades")
    suspend fun getAllStatic(): List<InfrastructureUpgradesEntity>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM infrastructure_upgrades WHERE team_id = :teamId ORDER BY start_date DESC")
    fun getUpgradesByTeam(teamId: Int): Flow<List<InfrastructureUpgradesEntity>>

    @Query("SELECT * FROM infrastructure_upgrades WHERE team_id = :teamId AND status = 'Completed' ORDER BY completion_date DESC")
    fun getCompletedUpgradesByTeam(teamId: Int): Flow<List<InfrastructureUpgradesEntity>>

    @Query("SELECT * FROM infrastructure_upgrades WHERE team_id = :teamId AND status IN ('Pending', 'In Progress') ORDER BY start_date")
    fun getActiveUpgradesByTeam(teamId: Int): Flow<List<InfrastructureUpgradesEntity>>

    @Query("SELECT * FROM infrastructure_upgrades WHERE team_id = :teamId AND upgrade_type = :upgradeType ORDER BY upgrade_level DESC LIMIT 1")
    suspend fun getLatestUpgradeByType(teamId: Int, upgradeType: String): InfrastructureUpgradesEntity?

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM infrastructure_upgrades WHERE status = 'In Progress' ORDER BY completion_date")
    fun getInProgressUpgrades(): Flow<List<InfrastructureUpgradesEntity>>

    @Query("SELECT * FROM infrastructure_upgrades WHERE status = 'Pending' ORDER BY start_date")
    fun getPendingUpgrades(): Flow<List<InfrastructureUpgradesEntity>>

    @Query("SELECT * FROM infrastructure_upgrades WHERE completion_date < date('now') AND status = 'In Progress'")
    fun getOverdueUpgrades(): Flow<List<InfrastructureUpgradesEntity>>

    // ============ TYPE QUERIES ============

    @Query("SELECT * FROM infrastructure_upgrades WHERE upgrade_type = :upgradeType ORDER BY cost DESC")
    fun getUpgradesByType(upgradeType: String): Flow<List<InfrastructureUpgradesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            upgrade_type,
            COUNT(*) as upgrade_count,
            AVG(cost) as avg_cost,
            SUM(cost) as total_cost
        FROM infrastructure_upgrades 
        WHERE status = 'Completed'
        GROUP BY upgrade_type
    """)
    fun getUpgradeStatistics(): Flow<List<UpgradeStatistics>>

    @Query("""
        SELECT 
            team_id,
            team_name,
            COUNT(*) as upgrade_count,
            SUM(cost) as total_spent
        FROM infrastructure_upgrades 
        WHERE status = 'Completed'
        GROUP BY team_id
        ORDER BY total_spent DESC
        LIMIT :limit
    """)
    fun getBiggestSpendersOnInfrastructure(limit: Int): Flow<List<TeamInfrastructureSpending>>
}

// ============ DATA CLASSES ============

data class UpgradeStatistics(
    @ColumnInfo(name = "upgrade_type")
    val upgradeType: String,

    @ColumnInfo(name = "upgrade_count")
    val upgradeCount: Int,

    @ColumnInfo(name = "avg_cost")
    val averageCost: Double,

    @ColumnInfo(name = "total_cost")
    val totalCost: Long
)

data class TeamInfrastructureSpending(
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "upgrade_count")
    val upgradeCount: Int,

    @ColumnInfo(name = "total_spent")
    val totalSpent: Long
)
