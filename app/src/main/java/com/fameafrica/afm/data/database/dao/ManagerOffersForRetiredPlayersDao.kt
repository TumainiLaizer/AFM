package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ManagerOffersForRetiredPlayersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManagerOffersForRetiredPlayersDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM manager_offers_for_retired_players ORDER BY offer_date DESC")
    fun getAll(): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE id = :id")
    suspend fun getById(id: Int): ManagerOffersForRetiredPlayersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: ManagerOffersForRetiredPlayersEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<ManagerOffersForRetiredPlayersEntity>)

    @Update
    suspend fun update(offer: ManagerOffersForRetiredPlayersEntity)

    @Delete
    suspend fun delete(offer: ManagerOffersForRetiredPlayersEntity)

    @Query("DELETE FROM manager_offers_for_retired_players")
    suspend fun deleteAll()

    @Query("SELECT * FROM manager_offers_for_retired_players")
    suspend fun getAllStatic(): List<ManagerOffersForRetiredPlayersEntity>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE player_id = :playerId ORDER BY offer_date DESC")
    fun getOffersByPlayer(playerId: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE player_id = :playerId AND status = 'Pending' ORDER BY offer_date DESC")
    fun getPendingOffersByPlayer(playerId: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE player_id = :playerId AND status = 'Accepted' ORDER BY offer_date DESC")
    fun getAcceptedOffersByPlayer(playerId: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE offered_team_id = :teamId ORDER BY offer_date DESC")
    fun getOffersByTeamId(teamId: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE offered_team = :teamName ORDER BY offer_date DESC")
    fun getOffersByTeamName(teamName: String): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    // ============ ROLE TYPE QUERIES ============

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE role_type = :roleType ORDER BY offer_date DESC")
    fun getOffersByRoleType(roleType: String): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE role_type LIKE '%COACH%' ORDER BY offered_salary DESC")
    fun getCoachingOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE role_type = 'SCOUT' OR role_type = 'CHIEF_SCOUT' ORDER BY offered_salary DESC")
    fun getScoutingOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE role_type = 'SPORTING_DIRECTOR' OR role_type = 'TECHNICAL_DIRECTOR' ORDER BY offered_salary DESC")
    fun getDirectorOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE role_type = 'CLUB_MEDIA_OFFICER' ORDER BY offered_salary DESC")
    fun getMediaOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE role_type = 'PHYSIOTHERAPIST' ORDER BY offered_salary DESC")
    fun getMedicalOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE status = :status ORDER BY offer_date DESC")
    fun getOffersByStatus(status: String): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE status = 'Pending' AND expiry_date < :currentTime")
    suspend fun getExpiredOffers(currentTime: Long): List<ManagerOffersForRetiredPlayersEntity>

    // ============ LEAGUE LEVEL QUERIES ============

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE league_level = :level ORDER BY offer_date DESC")
    fun getOffersByLeagueLevel(level: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    @Query("SELECT * FROM manager_offers_for_retired_players WHERE league_level <= :maxLevel ORDER BY offered_salary DESC")
    fun getOffersByMaxLeagueLevel(maxLevel: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            role_type,
            COUNT(*) as offer_count,
            AVG(offered_salary) as avg_salary,
            AVG(contract_years) as avg_contract_years
        FROM manager_offers_for_retired_players 
        GROUP BY role_type
        ORDER BY offer_count DESC
    """)
    fun getRoleTypeDistribution(): Flow<List<RoleTypeDistribution>>

    @Query("""
        SELECT 
            offered_team_id,
            offered_team,
            COUNT(*) as offer_count
        FROM manager_offers_for_retired_players 
        GROUP BY offered_team_id
        ORDER BY offer_count DESC
        LIMIT :limit
    """)
    fun getMostActiveTeamsForRetiredPlayers(limit: Int): Flow<List<TeamRetiredPlayerActivity>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            mofrp.*,
            p.name as detail_player_name,
            p.position as player_position,
            p.rating as player_rating,
            p.leadership as player_leadership,
            p.media_handling as player_media_handling,
            t.logo_path as detail_team_logo
        FROM manager_offers_for_retired_players mofrp
        LEFT JOIN players p ON mofrp.player_id = p.id
        LEFT JOIN teams t ON mofrp.offered_team_id = t.id
        WHERE mofrp.id = :offerId
    """)
    suspend fun getRetiredPlayerOfferWithDetails(offerId: Int): RetiredPlayerOfferWithDetails?
}

// ============ DATA CLASSES ============

data class RoleTypeDistribution(
    @ColumnInfo(name = "role_type")
    val roleType: String,

    @ColumnInfo(name = "offer_count")
    val offerCount: Int,

    @ColumnInfo(name = "avg_salary")
    val averageSalary: Double,

    @ColumnInfo(name = "avg_contract_years")
    val averageContractYears: Double
)

data class TeamRetiredPlayerActivity(
    @ColumnInfo(name = "offered_team_id")
    val offeredTeamId: Int,

    @ColumnInfo(name = "offered_team")
    val offeredTeam: String,

    @ColumnInfo(name = "offer_count")
    val offerCount: Int
)

data class RetiredPlayerOfferWithDetails(
    @Embedded
    val offer: ManagerOffersForRetiredPlayersEntity,

    @ColumnInfo(name = "detail_player_name")
    val playerName: String?,

    @ColumnInfo(name = "player_position")
    val playerPosition: String?,

    @ColumnInfo(name = "player_rating")
    val playerRating: Int?,

    @ColumnInfo(name = "player_leadership")
    val playerLeadership: Int?,

    @ColumnInfo(name = "player_media_handling")
    val playerMediaHandling: Int?,

    @ColumnInfo(name = "detail_team_logo")
    val teamLogo: String?
)
