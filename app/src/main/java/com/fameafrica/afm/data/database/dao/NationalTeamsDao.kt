package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.NationalTeamsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NationalTeamsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM national_teams ORDER BY name")
    fun getAll(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE id = :id")
    suspend fun getById(id: Int): NationalTeamsEntity?

    @Query("SELECT * FROM national_teams WHERE name = :name")
    suspend fun getByName(name: String): NationalTeamsEntity?

    @Query("SELECT * FROM national_teams WHERE fifa_code = :fifaCode")
    suspend fun getByFifaCode(fifaCode: String): NationalTeamsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: NationalTeamsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teams: List<NationalTeamsEntity>)

    @Update
    suspend fun update(team: NationalTeamsEntity)

    @Delete
    suspend fun delete(team: NationalTeamsEntity)

    @Query("DELETE FROM national_teams")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM national_teams")
    suspend fun getCount(): Int

    // ============ IDENTITY RESOLUTION ============


    // ============ CONFEDERATION QUERIES ============

    @Query("SELECT * FROM national_teams WHERE confederation = :confederation ORDER BY name")
    fun getByConfederation(confederation: String): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = 'CAF' ORDER BY name")
    fun getAfricanTeams(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = 'UEFA' ORDER BY name")
    fun getEuropeanTeams(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = 'CONMEBOL' ORDER BY name")
    fun getSouthAmericanTeams(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = 'CONCACAF' ORDER BY name")
    fun getNorthAmericanTeams(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = 'AFC' ORDER BY name")
    fun getAsianTeams(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = 'OFC' ORDER BY name")
    fun getOceanianTeams(): Flow<List<NationalTeamsEntity>>

    // ============ RANKING QUERIES ============

    @Query("SELECT * FROM national_teams ORDER BY elo_rating DESC")
    fun getTeamsByEloRanking(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = :confederation ORDER BY elo_rating DESC")
    fun getConfederationRanking(confederation: String): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams ORDER BY CASE WHEN fifa_ranking IS NULL THEN 1 ELSE 0 END, fifa_ranking ASC")
    fun getTeamsByFifaRanking(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams ORDER BY reputation DESC")
    fun getTeamsByReputation(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams ORDER BY elo_rating DESC LIMIT :limit")
    fun getTopTeams(limit: Int): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE confederation = :confederation ORDER BY elo_rating DESC LIMIT :limit")
    fun getTopConfederationTeams(confederation: String, limit: Int): Flow<List<NationalTeamsEntity>>

    // ============ TITLES & APPEARANCES QUERIES ============

    @Query("SELECT * FROM national_teams ORDER BY continental_titles DESC")
    fun getTeamsByContinentalTitles(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE world_cup_appearances > 0 ORDER BY world_cup_appearances DESC")
    fun getWorldCupParticipants(): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE best_finish = :finish ORDER BY world_cup_appearances DESC")
    fun getTeamsWithBestFinish(finish: String): Flow<List<NationalTeamsEntity>>

    // ============ MANAGER QUERIES ============

    @Query("SELECT * FROM national_teams WHERE manager_id = :managerId")
    suspend fun getTeamByManager(managerId: Int): NationalTeamsEntity?

    @Query("SELECT * FROM national_teams WHERE manager_id IS NULL")
    fun getTeamsWithoutManager(): Flow<List<NationalTeamsEntity>>

    @Query("UPDATE national_teams SET manager_id = :managerId WHERE id = :teamId")
    suspend fun assignManager(teamId: Int, managerId: Int?)

    // ============ RIVALRY QUERIES ============

    @Query("SELECT * FROM national_teams WHERE rival_fifa_code = :fifaCode OR fifa_code = :rivalFifaCode")
    fun getRivalryTeams(fifaCode: String, rivalFifaCode: String): Flow<List<NationalTeamsEntity>>

    @Query("""
        SELECT * FROM national_teams 
        WHERE id IN (SELECT target_id FROM identity_mappings WHERE source_name = (SELECT name FROM national_teams WHERE id = :teamId) AND category = 'NATIONAL_RIVAL')
    """)
    fun getRivals(teamId: Int): Flow<List<NationalTeamsEntity>>

    @Query("SELECT * FROM national_teams WHERE rival_fifa_code IS NOT NULL")
    fun getTeamsWithRivals(): Flow<List<NationalTeamsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            confederation,
            COUNT(*) as team_count,
            AVG(elo_rating) as avg_elo,
            AVG(reputation) as avg_reputation,
            SUM(continental_titles) as total_continental_titles,
            SUM(world_cup_appearances) as total_world_cup_appearances
        FROM national_teams 
        GROUP BY confederation
        ORDER BY avg_elo DESC
    """)
    fun getConfederationStatistics(): Flow<List<ConfederationStats>>

    @Query("""
        SELECT 
            AVG(elo_rating) as avg_elo,
            AVG(reputation) as avg_reputation,
            AVG(fan_loyalty) as avg_fan_loyalty,
            AVG(avg_attacking_ability) as avg_attack,
            AVG(avg_defence_ability) as avg_defence,
            AVG(avg_playmaking_ability) as avg_playmaking
        FROM national_teams 
        WHERE confederation = :confederation
    """)
    suspend fun getConfederationAverages(confederation: String): ConfederationAverages?

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            nt.*,
            n.nationality as country_name,
            n.flag_path as country_flag,
            m.name as manager_name,
            m.nationality as manager_nationality,
            rn.nationality as rival_country_name
        FROM national_teams nt
        LEFT JOIN nationalities n ON nt.fifa_code = n.fifa_code
        LEFT JOIN managers m ON nt.manager_id = m.id
        LEFT JOIN national_teams rnt ON rnt.id IN (SELECT target_id FROM identity_mappings WHERE source_name = nt.name AND category = 'NATIONAL_RIVAL')
        LEFT JOIN nationalities rn ON rnt.nationality_id = rn.id
        WHERE nt.id = :teamId
    """)
    suspend fun getNationalTeamWithDetails(teamId: Int): NationalTeamWithDetails?

    @Query("""
        SELECT 
            nt.*,
            n.flag_path as country_flag,
            COUNT(ntp.player_id) as current_squad_size
        FROM national_teams nt
        LEFT JOIN nationalities n ON nt.fifa_code = n.fifa_code
        LEFT JOIN national_team_players ntp ON nt.id = ntp.national_team_id
        GROUP BY nt.id
        ORDER BY nt.elo_rating DESC
    """)
    fun getAllNationalTeamsWithDetails(): Flow<List<NationalTeamWithFlags>>

    @Query("""
        SELECT 
            nt.*,
            n.flag_path as country_flag,
            AVG(p.rating) as avg_player_rating
        FROM national_teams nt
        LEFT JOIN nationalities n ON nt.fifa_code = n.fifa_code
        LEFT JOIN national_team_players ntp ON nt.id = ntp.national_team_id
        LEFT JOIN players p ON ntp.player_id = p.id
        WHERE nt.id = :teamId
        GROUP BY nt.id
    """)
    suspend fun getNationalTeamWithPlayerStats(teamId: Int): NationalTeamWithPlayerStats?
    @Query("SELECT * FROM national_teams ORDER BY name ASC")
    suspend fun getAllStatic(): List<NationalTeamsEntity>
}

// ============ DATA CLASSES ============

data class ConfederationStats(
    @ColumnInfo(name = "confederation")
    val confederation: String,

    @ColumnInfo(name = "team_count")
    val teamCount: Int,

    @ColumnInfo(name = "avg_elo")
    val averageElo: Double,

    @ColumnInfo(name = "avg_reputation")
    val averageReputation: Double,

    @ColumnInfo(name = "total_continental_titles")
    val totalContinentalTitles: Int,

    @ColumnInfo(name = "total_world_cup_appearances")
    val totalWorldCupAppearances: Int
)

data class ConfederationAverages(
    @ColumnInfo(name = "avg_elo")
    val averageElo: Double,

    @ColumnInfo(name = "avg_reputation")
    val averageReputation: Double,

    @ColumnInfo(name = "avg_fan_loyalty")
    val averageFanLoyalty: Double,

    @ColumnInfo(name = "avg_attack")
    val averageAttack: Double?,

    @ColumnInfo(name = "avg_defence")
    val averageDefence: Double?,

    @ColumnInfo(name = "avg_playmaking")
    val averagePlaymaking: Double?
)

data class NationalTeamWithDetails(
    @Embedded
    val team: NationalTeamsEntity,

    @ColumnInfo(name = "country_name")
    val countryName: String?,

    @ColumnInfo(name = "country_flag")
    val countryFlag: String?,

    @ColumnInfo(name = "manager_name")
    val managerName: String?,

    @ColumnInfo(name = "manager_nationality")
    val managerNationality: String?,

    @ColumnInfo(name = "rival_country_name")
    val rivalCountryName: String?
)

data class NationalTeamWithFlags(
    @Embedded
    val team: NationalTeamsEntity,

    @ColumnInfo(name = "country_flag")
    val countryFlag: String?,

    @ColumnInfo(name = "current_squad_size")
    val currentSquadSize: Int
)

data class NationalTeamWithPlayerStats(
    @Embedded
    val team: NationalTeamsEntity,

    @ColumnInfo(name = "country_flag")
    val countryFlag: String?,

    @ColumnInfo(name = "avg_player_rating")
    val averagePlayerRating: Double?
)