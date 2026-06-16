package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.SponsorsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SponsorsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM sponsors ORDER BY name")
    fun getAll(): Flow<List<SponsorsEntity>>

    @Query("SELECT * FROM sponsors WHERE id = :id")
    suspend fun getById(id: Int): SponsorsEntity?

    @Query("SELECT * FROM sponsors WHERE name = :name")
    suspend fun getByName(name: String): SponsorsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sponsor: SponsorsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sponsors: List<SponsorsEntity>)

    @Update
    suspend fun update(sponsor: SponsorsEntity)

    @Delete
    suspend fun delete(sponsor: SponsorsEntity)

    @Query("DELETE FROM sponsors")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM sponsors")
    suspend fun getCount(): Int

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM sponsors WHERE team_id = :teamId AND is_active = 1")
    fun getTeamSponsors(teamId: Int): Flow<List<SponsorsEntity>>

    @Query("SELECT * FROM sponsors WHERE team_id = :teamId AND sponsor_type = 'CLUB_SPONSOR' AND is_active = 1")
    fun getClubSponsors(teamId: Int): Flow<List<SponsorsEntity>>

    @Query("SELECT * FROM sponsors WHERE team_id IS NULL AND sponsor_type = 'TITLE_SPONSOR' AND is_active = 1")
    fun getTitleSponsors(): Flow<List<SponsorsEntity>>

    @Query("SELECT * FROM sponsors WHERE team_id = :teamId AND can_fund_transfers = 1 AND is_active = 1")
    fun getTransferFundingSponsors(teamId: Int): Flow<List<SponsorsEntity>>

    @Query("SELECT SUM(sponsorship_value) FROM sponsors WHERE team_id = :teamId AND is_active = 1")
    suspend fun getTotalSponsorshipValue(teamId: Int): Long?

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM sponsors WHERE sponsor_type = :sponsorType AND is_active = 1")
    fun getSponsorsByType(sponsorType: String): Flow<List<SponsorsEntity>>

    @Query("SELECT * FROM sponsors WHERE can_fund_transfers = 1 AND is_active = 1")
    fun getTransferFunders(): Flow<List<SponsorsEntity>>

    @Query("SELECT * FROM sponsors WHERE sponsor_type = 'PLACEHOLDER'")
    fun getPlaceholderSponsors(): Flow<List<SponsorsEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM sponsors WHERE is_active = 1 AND contract_end_date < date('now')")
    fun getExpiredSponsors(): Flow<List<SponsorsEntity>>

    @Query("SELECT * FROM sponsors WHERE is_active = 1 AND contract_end_date >= date('now')")
    fun getActiveSponsors(): Flow<List<SponsorsEntity>>

    @Query("UPDATE sponsors SET is_active = 0 WHERE contract_end_date < date('now')")
    suspend fun deactivateExpiredSponsors()

    // ============ TRANSFER FUNDING QUERIES ============

    @Query("UPDATE sponsors SET transfer_funding_used = transfer_funding_used + :amount WHERE id = :sponsorId")
    suspend fun updateTransferFundingUsed(sponsorId: Int, amount: Long)

    @Query("SELECT * FROM sponsors WHERE id = :sponsorId AND (transfer_funding_limit IS NULL OR transfer_funding_used < transfer_funding_limit)")
    suspend fun hasTransferFundingAvailable(sponsorId: Int): SponsorsEntity?

    @Query("SELECT SUM(transfer_funding_limit - transfer_funding_used) FROM sponsors WHERE team_id = :teamId AND can_fund_transfers = 1 AND is_active = 1")
    suspend fun getTotalAvailableTransferFunding(teamId: Int): Long?

    // ============ NEGOTIATION QUERIES ============

    @Query("UPDATE sponsors SET sponsorship_value = :newValue WHERE id = :sponsorId")
    suspend fun updateSponsorshipValue(sponsorId: Int, newValue: Long)

    // ============ RENEWAL QUERIES ============

    @Query("SELECT * FROM sponsors WHERE contract_end_date BETWEEN date('now') AND date('now', '+3 months') AND is_active = 1")
    fun getSponsorsNeedingRenewal(): Flow<List<SponsorsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            sponsor_type,
            COUNT(*) as sponsor_count,
            AVG(sponsorship_value) as avg_value,
            SUM(sponsorship_value) as total_value
        FROM sponsors 
        WHERE is_active = 1
        GROUP BY sponsor_type
    """)
    fun getSponsorTypeStatistics(): Flow<List<SponsorTypeStats>>

    @Query("""
        SELECT 
            team_id,
            COUNT(*) as sponsor_count,
            SUM(sponsorship_value) as total_sponsorship
        FROM sponsors 
        WHERE team_id IS NOT NULL AND is_active = 1
        GROUP BY team_id
        ORDER BY total_sponsorship DESC
    """)
    fun getTeamSponsorshipRankings(): Flow<List<TeamSponsorshipRanking>>
    @Query("SELECT * FROM sponsors ORDER BY name ASC")
    suspend fun getAllStatic(): List<SponsorsEntity>
}

// ============ DATA CLASSES ============

data class SponsorTypeStats(
    @ColumnInfo(name = "sponsor_type")
    val sponsorType: String,

    @ColumnInfo(name = "sponsor_count")
    val sponsorCount: Int,

    @ColumnInfo(name = "avg_value")
    val averageValue: Double,

    @ColumnInfo(name = "total_value")
    val totalValue: Long
)

data class TeamSponsorshipRanking(
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "sponsor_count")
    val sponsorCount: Int,

    @ColumnInfo(name = "total_sponsorship")
    val totalSponsorship: Long
)
