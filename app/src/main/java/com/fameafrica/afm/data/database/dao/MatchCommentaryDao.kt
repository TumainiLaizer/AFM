package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.MatchCommentaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchCommentaryDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM match_commentary WHERE match_id = :matchId ORDER BY minute, id")
    fun getCommentaryForMatch(matchId: Int): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE id = :id")
    suspend fun getById(id: Int): MatchCommentaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commentary: MatchCommentaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(commentaries: List<MatchCommentaryEntity>): List<Long>

    @Update
    suspend fun update(commentary: MatchCommentaryEntity)

    @Delete
    suspend fun delete(commentary: MatchCommentaryEntity)

    @Query("DELETE FROM match_commentary WHERE match_id = :matchId")
    suspend fun deleteByMatch(matchId: Int)

    @Query("DELETE FROM match_commentary")
    suspend fun deleteAll()

    @Query("SELECT * FROM match_commentary")
    suspend fun getAllStatic(): List<MatchCommentaryEntity>

    @Query("SELECT COUNT(*) FROM match_commentary WHERE match_id = :matchId")
    suspend fun getCommentaryCountForMatch(matchId: Int): Int

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM match_commentary WHERE match_id = :matchId AND commentary_type = :type ORDER BY minute")
    fun getCommentaryByType(matchId: Int, type: String): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE match_id = :matchId AND importance >= :minImportance ORDER BY minute")
    fun getImportantCommentary(matchId: Int, minImportance: Int): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE match_id = :matchId AND is_controversial = 1 ORDER BY minute")
    fun getControversialMoments(matchId: Int): Flow<List<MatchCommentaryEntity>>

    // ============ TIME-BASED QUERIES ============

    @Query("SELECT * FROM match_commentary WHERE match_id = :matchId AND minute BETWEEN :startMinute AND :endMinute ORDER BY minute")
    fun getCommentaryInRange(matchId: Int, startMinute: Int, endMinute: Int): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE match_id = :matchId AND minute > :minute ORDER BY minute LIMIT :limit")
    suspend fun getNextCommentary(matchId: Int, minute: Int, limit: Int): List<MatchCommentaryEntity>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM match_commentary WHERE player_id = :playerId ORDER BY match_id DESC, minute")
    fun getCommentaryForPlayer(playerId: Int): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE player_name LIKE '%' || :playerName || '%' ORDER BY match_id DESC, minute")
    fun getCommentaryForPlayerName(playerName: String): Flow<List<MatchCommentaryEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM match_commentary WHERE team_id = :teamId ORDER BY match_id DESC, minute")
    fun getCommentaryForTeam(teamId: Int): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE team_id = :teamId AND commentary_type = 'GOAL' ORDER BY match_id DESC")
    fun getGoalCommentaryForTeam(teamId: Int): Flow<List<MatchCommentaryEntity>>

    // ============ REFEREE-BASED QUERIES ============

    @Query("SELECT * FROM match_commentary WHERE referee_id = :refereeId ORDER BY match_id DESC, minute")
    fun getCommentaryForReferee(refereeId: Int): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE referee_name = :refereeName ORDER BY match_id DESC, minute")
    fun getCommentaryForRefereeName(refereeName: String): Flow<List<MatchCommentaryEntity>>

    // ============ MANAGER-BASED QUERIES ============

    @Query("SELECT * FROM match_commentary WHERE manager_id = :managerId ORDER BY match_id DESC, minute")
    fun getCommentaryForManager(managerId: Int): Flow<List<MatchCommentaryEntity>>

    // ============ FAN REACTION QUERIES ============

    @Query("SELECT * FROM match_commentary WHERE fan_reaction IS NOT NULL ORDER BY crowd_noise_level DESC")
    fun getFanReactionMoments(): Flow<List<MatchCommentaryEntity>>

    @Query("SELECT * FROM match_commentary WHERE crowd_noise_level >= 8 ORDER BY match_id, minute")
    fun getElectricAtmosphereMoments(): Flow<List<MatchCommentaryEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("SELECT COUNT(*) FROM match_commentary WHERE match_id = :matchId AND commentary_type = 'GOAL'")
    suspend fun getGoalCommentaryCount(matchId: Int): Int

    @Query("SELECT COUNT(*) FROM match_commentary WHERE match_id = :matchId AND is_controversial = 1")
    suspend fun getControversialMomentCount(matchId: Int): Int

    @Query("SELECT AVG(crowd_noise_level) FROM match_commentary WHERE match_id = :matchId")
    suspend fun getAverageCrowdNoise(matchId: Int): Double?

    @Query("""
        SELECT 
            commentary_type,
            COUNT(*) as count
        FROM match_commentary 
        WHERE match_id = :matchId
        GROUP BY commentary_type
        ORDER BY count DESC
    """)
    fun getCommentaryTypeDistribution(matchId: Int): Flow<List<CommentaryTypeDistribution>>
}

// ============ DATA CLASSES ============

data class CommentaryTypeDistribution(
    @ColumnInfo(name = "commentary_type")
    val commentaryType: String,

    @ColumnInfo(name = "count")
    val count: Int
)
