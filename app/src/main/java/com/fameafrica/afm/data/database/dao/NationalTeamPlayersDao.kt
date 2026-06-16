package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.NationalTeamPlayersEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.NationalTeamsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NationalTeamPlayersDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM national_team_players")
    fun getAll(): Flow<List<NationalTeamPlayersEntity>>

    @Query("SELECT * FROM national_team_players WHERE national_team_id = :teamId AND player_id = :playerId")
    suspend fun getById(teamId: Int, playerId: Int): NationalTeamPlayersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NationalTeamPlayersEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<NationalTeamPlayersEntity>)

    @Update
    suspend fun update(entry: NationalTeamPlayersEntity)

    @Delete
    suspend fun delete(entry: NationalTeamPlayersEntity)

    @Query("DELETE FROM national_team_players WHERE national_team_id = :teamId AND player_id = :playerId")
    suspend fun deleteById(teamId: Int, playerId: Int)

    @Query("DELETE FROM national_team_players WHERE national_team_id = :teamId")
    suspend fun deleteByNationalTeam(teamId: Int)

    @Query("DELETE FROM national_team_players WHERE player_id = :playerId")
    suspend fun deleteByPlayer(playerId: Int)

    @Query("DELETE FROM national_team_players")
    suspend fun deleteAll()

    @Query("SELECT * FROM national_team_players")
    suspend fun getAllStatic(): List<NationalTeamPlayersEntity>

    @Query("SELECT COUNT(*) FROM national_team_players WHERE national_team_id = :teamId")
    suspend fun getSquadSize(teamId: Int): Int

    // ============ NATIONAL TEAM QUERIES ============

    @Query("""
        SELECT p.* FROM players p
        INNER JOIN national_team_players ntp ON p.id = ntp.player_id
        WHERE ntp.national_team_id = :teamId
        ORDER BY 
            CASE ntp.role
                WHEN 'CAPTAIN' THEN 1
                WHEN 'STARTER' THEN 2
                WHEN 'RESERVE' THEN 3
                ELSE 4
            END,
            p.rating DESC
    """)
    fun getNationalTeamSquad(teamId: Int): Flow<List<PlayersEntity>>

    @Query("""
        SELECT p.* FROM players p
        INNER JOIN national_team_players ntp ON p.id = ntp.player_id
        WHERE ntp.national_team_id = :teamId AND ntp.role = 'STARTER'
        ORDER BY p.position_category, p.rating DESC
    """)
    fun getNationalTeamStarters(teamId: Int): Flow<List<PlayersEntity>>

    @Query("""
        SELECT p.* FROM players p
        INNER JOIN national_team_players ntp ON p.id = ntp.player_id
        WHERE ntp.national_team_id = :teamId AND ntp.role = 'RESERVE'
        ORDER BY p.rating DESC
    """)
    fun getNationalTeamReserves(teamId: Int): Flow<List<PlayersEntity>>

    @Query("""
        SELECT p.* FROM players p
        INNER JOIN national_team_players ntp ON p.id = ntp.player_id
        WHERE ntp.national_team_id = :teamId AND ntp.role = 'CAPTAIN'
    """)
    suspend fun getNationalTeamCaptain(teamId: Int): PlayersEntity?

    @Query("""
        SELECT ntp.*, p.name as player_name, p.position, p.rating, p.age
        FROM national_team_players ntp
        INNER JOIN players p ON ntp.player_id = p.id
        WHERE ntp.national_team_id = :teamId
        ORDER BY ntp.role, p.rating DESC
    """)
    fun getNationalTeamSquadWithDetails(teamId: Int): Flow<List<NationalTeamSquadEntry>>

    // ============ PLAYER QUERIES ============

    @Query("SELECT * FROM national_team_players WHERE player_id = :playerId")
    fun getNationalTeamEntriesByPlayer(playerId: Int): Flow<List<NationalTeamPlayersEntity>>

    @Query("""
        SELECT nt.* FROM national_teams nt
        INNER JOIN national_team_players ntp ON nt.id = ntp.national_team_id
        WHERE ntp.player_id = :playerId
    """)
    fun getNationalTeamsForPlayer(playerId: Int): Flow<List<NationalTeamsEntity>>

    // ============ ROLE-BASED QUERIES ============

    @Query("SELECT * FROM national_team_players WHERE role = :role")
    fun getPlayersByRole(role: String): Flow<List<NationalTeamPlayersEntity>>

    @Query("SELECT * FROM national_team_players WHERE national_team_id = :teamId AND role = 'CAPTAIN'")
    suspend fun getCaptainEntry(teamId: Int): NationalTeamPlayersEntity?

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            ntp.national_team_id,
            nt.name as team_name,
            COUNT(ntp.player_id) as squad_size,
            AVG(p.rating) as avg_rating,
            AVG(p.age) as avg_age,
            COUNT(CASE WHEN p.age <= 21 THEN 1 END) as youth_count,
            COUNT(CASE WHEN p.age >= 30 THEN 1 END) as veteran_count
        FROM national_team_players ntp
        INNER JOIN national_teams nt ON ntp.national_team_id = nt.id
        INNER JOIN players p ON ntp.player_id = p.id
        GROUP BY ntp.national_team_id
        ORDER BY avg_rating DESC
    """)
    fun getNationalTeamSquadStats(): Flow<List<NationalTeamSquadStats>>
}

// ============ DATA CLASSES ============

data class NationalTeamSquadEntry(
    @Embedded
    val entry: NationalTeamPlayersEntity,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "position")
    val position: String,

    @ColumnInfo(name = "rating")
    val rating: Int,

    @ColumnInfo(name = "age")
    val age: Int
)

data class NationalTeamSquadStats(
    @ColumnInfo(name = "national_team_id")
    val nationalTeamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "squad_size")
    val squadSize: Int,

    @ColumnInfo(name = "avg_rating")
    val averageRating: Double,

    @ColumnInfo(name = "avg_age")
    val averageAge: Double,

    @ColumnInfo(name = "youth_count")
    val youthCount: Int,

    @ColumnInfo(name = "veteran_count")
    val veteranCount: Int
)