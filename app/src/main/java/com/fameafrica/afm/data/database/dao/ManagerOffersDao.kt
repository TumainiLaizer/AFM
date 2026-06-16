package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ManagerOffersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManagerOffersDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM manager_offers ORDER BY offer_date DESC")
    fun getAll(): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE id = :id")
    suspend fun getById(id: Int): ManagerOffersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: ManagerOffersEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<ManagerOffersEntity>)

    @Update
    suspend fun update(offer: ManagerOffersEntity)

    @Delete
    suspend fun delete(offer: ManagerOffersEntity)

    @Query("DELETE FROM manager_offers")
    suspend fun deleteAll()

    @Query("SELECT * FROM manager_offers")
    suspend fun getAllStatic(): List<ManagerOffersEntity>

    @Query("DELETE FROM manager_offers WHERE status = 'expired' OR expiry_date < :currentTime")
    suspend fun deleteExpiredOffers(currentTime: Long)

    // ============ MANAGER-BASED QUERIES ============

    @Query("SELECT * FROM manager_offers WHERE manager_id = :managerId AND is_application = 1 ORDER BY offer_date DESC")
    fun getApplicationsByManager(managerId: Int): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE manager_id = :managerId AND is_application = 0 ORDER BY offer_date DESC")
    fun getOffersByManager(managerId: Int): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE manager_id = :managerId AND status = 'pending' ORDER BY offer_date DESC")
    fun getPendingOffersByManager(managerId: Int): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE manager_id = :managerId AND status = 'accepted' ORDER BY offer_date DESC")
    fun getAcceptedOffersByManager(managerId: Int): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE manager_id = :managerId AND status = 'rejected' ORDER BY offer_date DESC")
    fun getRejectedOffersByManager(managerId: Int): Flow<List<ManagerOffersEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM manager_offers WHERE offering_team = :teamName ORDER BY offer_date DESC")
    fun getOffersByTeam(teamName: String): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE offering_team_id = :teamId ORDER BY offer_date DESC")
    fun getOffersByTeamId(teamId: Int): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE offering_team_id = :teamId AND status = 'pending' ORDER BY offer_date DESC")
    fun getPendingOffersByTeamId(teamId: Int): Flow<List<ManagerOffersEntity>>

    // ============ LEAGUE LEVEL QUERIES ============

    @Query("SELECT * FROM manager_offers WHERE league_level = :level ORDER BY offer_date DESC")
    fun getOffersByLeagueLevel(level: Int): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE league_level <= :maxLevel ORDER BY league_level ASC, offered_salary DESC")
    fun getOffersByMaxLeagueLevel(maxLevel: Int): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE league_level BETWEEN :minLevel AND :maxLevel ORDER BY league_level ASC")
    fun getOffersByLeagueLevelRange(minLevel: Int, maxLevel: Int): Flow<List<ManagerOffersEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM manager_offers WHERE status = :status ORDER BY offer_date DESC")
    fun getOffersByStatus(status: String): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE status = 'pending' AND expiry_date < :currentTime")
    suspend fun getExpiredOffers(currentTime: Long): List<ManagerOffersEntity>

    // ============ OFFER TYPE QUERIES ============

    @Query("SELECT * FROM manager_offers WHERE offer_type = :offerType ORDER BY offer_date DESC")
    fun getOffersByType(offerType: String): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE offer_type = 'HEAD_COACH' ORDER BY league_level ASC")
    fun getHeadCoachOffers(): Flow<List<ManagerOffersEntity>>

    // ============ PROMOTION QUERIES ============

    @Query("SELECT * FROM manager_offers WHERE is_promotion = 1 ORDER BY league_level ASC, offered_salary DESC")
    fun getPromotionOffers(): Flow<List<ManagerOffersEntity>>

    @Query("SELECT * FROM manager_offers WHERE is_mid_season = 1 ORDER BY offer_date DESC")
    fun getMidSeasonOffers(): Flow<List<ManagerOffersEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("SELECT AVG(offered_salary) FROM manager_offers WHERE league_level = :level AND status = 'accepted'")
    suspend fun getAverageSalaryByLeagueLevel(level: Int): Double?

    @Query("""
        SELECT 
            league_level,
            COUNT(*) as offer_count,
            AVG(offered_salary) as avg_salary,
            COUNT(CASE WHEN status = 'accepted' THEN 1 END) as accepted_count
        FROM manager_offers 
        GROUP BY league_level
        ORDER BY league_level ASC
    """)
    fun getOfferStatisticsByLevel(): Flow<List<OfferLevelStatistics>>

    @Query("""
        SELECT 
            offering_team,
            COUNT(*) as offer_count,
            AVG(offered_salary) as avg_offered_salary
        FROM manager_offers 
        GROUP BY offering_team
        ORDER BY offer_count DESC
        LIMIT :limit
    """)
    fun getMostActiveOfferingTeams(limit: Int): Flow<List<TeamOfferActivity>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            mo.*,
            m.name as detail_manager_name,
            m.reputation as manager_reputation,
            m.reputation_level as manager_reputation_level,
            t.logo_path as detail_team_logo,
            t.league as detail_team_league
        FROM manager_offers mo
        LEFT JOIN managers m ON mo.manager_id = m.id
        LEFT JOIN teams t ON mo.offering_team_id = t.id
        WHERE mo.id = :offerId
    """)
    suspend fun getOfferWithDetails(offerId: Int): OfferWithDetails?
}

// ============ DATA CLASSES ============

data class OfferLevelStatistics(
    @ColumnInfo(name = "league_level")
    val leagueLevel: Int,

    @ColumnInfo(name = "offer_count")
    val offerCount: Int,

    @ColumnInfo(name = "avg_salary")
    val averageSalary: Double,

    @ColumnInfo(name = "accepted_count")
    val acceptedCount: Int
)

data class TeamOfferActivity(
    @ColumnInfo(name = "offering_team")
    val offeringTeam: String,

    @ColumnInfo(name = "offer_count")
    val offerCount: Int,

    @ColumnInfo(name = "avg_offered_salary")
    val averageOfferedSalary: Double
)

data class OfferWithDetails(
    @Embedded
    val offer: ManagerOffersEntity,

    @ColumnInfo(name = "detail_manager_name")
    val managerName: String?,

    @ColumnInfo(name = "manager_reputation")
    val managerReputation: Int?,

    @ColumnInfo(name = "manager_reputation_level")
    val managerReputationLevel: String?,

    @ColumnInfo(name = "detail_team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "detail_team_league")
    val teamLeague: String?
)
