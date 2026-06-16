package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.CareersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareersDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM careers ORDER BY created_at DESC")
    fun getAll(): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers WHERE id = :id")
    suspend fun getById(id: Int): CareersEntity?

    @Query("SELECT * FROM careers WHERE manager_id = :managerId")
    suspend fun getByManagerId(managerId: Int): CareersEntity?

    @Insert
    suspend fun insert(career: CareersEntity): Long

    @Insert
    suspend fun insertAll(careers: List<CareersEntity>)

    @Update
    suspend fun update(career: CareersEntity)

    @Delete
    suspend fun delete(career: CareersEntity)

    @Query("DELETE FROM careers")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM careers")
    suspend fun getCount(): Int

    @Query("SELECT * FROM careers")
    suspend fun getAllStatic(): List<CareersEntity>

    // ============ ACTIVE CAREERS ============

    @Query("SELECT * FROM careers WHERE is_active = 1 ORDER BY last_updated DESC")
    fun getActiveCareers(): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers WHERE is_active = 1 AND manager_id = :managerId")
    suspend fun getActiveCareerByManager(managerId: Int): CareersEntity?

    @Query("SELECT * FROM careers WHERE is_active = 1 AND team_id = :teamId")
    suspend fun getActiveCareerByTeam(teamId: Int): CareersEntity?

    @Query("SELECT COUNT(*) FROM careers WHERE is_active = 1")
    suspend fun getActiveCareersCount(): Int

    // ============ COMPLETED CAREERS ============

    @Query("SELECT * FROM careers WHERE is_active = 0 ORDER BY last_updated DESC")
    fun getCompletedCareers(): Flow<List<CareersEntity>>

    @Query("SELECT COUNT(*) FROM careers WHERE is_active = 0")
    suspend fun getCompletedCareersCount(): Int

    // ============ MANAGER-BASED QUERIES ============

    @Query("SELECT * FROM careers WHERE manager_id = :managerId ORDER BY created_at DESC")
    fun getCareersByManager(managerId: Int): Flow<List<CareersEntity>>

    @Query("SELECT COUNT(*) FROM careers WHERE manager_id = :managerId")
    suspend fun getCareerCountByManager(managerId: Int): Int

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM careers WHERE team_id = :teamId ORDER BY created_at DESC")
    fun getCareersByTeam(teamId: Int): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers WHERE team_name LIKE '%' || :teamName || '%' ORDER BY created_at DESC")
    fun getCareersByTeamName(teamName: String): Flow<List<CareersEntity>>

    // ============ LEAGUE-BASED QUERIES ============

    @Query("SELECT * FROM careers WHERE league_name = :leagueName ORDER BY created_at DESC")
    fun getCareersByLeague(leagueName: String): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers WHERE league_level = :level ORDER BY created_at DESC")
    fun getCareersByLeagueLevel(level: Int): Flow<List<CareersEntity>>

    // ============ DIFFICULTY-BASED QUERIES ============

    @Query("SELECT * FROM careers WHERE difficulty = :difficulty ORDER BY created_at DESC")
    fun getCareersByDifficulty(difficulty: String): Flow<List<CareersEntity>>

    @Query("SELECT difficulty, COUNT(*) as count FROM careers GROUP BY difficulty")
    fun getDifficultyDistribution(): Flow<List<DifficultyDistribution>>

    // ============ MODE-BASED QUERIES ============

    @Query("SELECT * FROM careers WHERE mode = :mode ORDER BY last_updated DESC")
    fun getCareersByMode(mode: String): Flow<List<CareersEntity>>

    @Query("SELECT mode, COUNT(*) as count FROM careers GROUP BY mode")
    fun getModeDistribution(): Flow<List<ModeDistribution>>

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT * FROM careers WHERE season = :season ORDER BY created_at DESC")
    fun getCareersBySeason(season: Int): Flow<List<CareersEntity>>

    @Query("SELECT season, COUNT(*) as count FROM careers GROUP BY season ORDER BY season DESC")
    fun getSeasonDistribution(): Flow<List<SeasonDistribution>>

    // ============ STATISTICS QUERIES ============

    @Query("SELECT * FROM careers ORDER BY total_trophies DESC LIMIT :limit")
    fun getTopTrophyWinningCareers(limit: Int): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers ORDER BY total_wins DESC LIMIT :limit")
    fun getTopWinningCareers(limit: Int): Flow<List<CareersEntity>>

    /**
     * Get careers with highest win percentage (minimum 10 matches)
     * CORRECTED: WHERE clause comes before ORDER BY
     */
    @Query("""
        SELECT * FROM careers 
        WHERE total_matches >= 10 
        ORDER BY (CAST(total_wins AS REAL) / total_matches) DESC 
        LIMIT :limit
    """)
    fun getTopWinPercentageCareers(limit: Int): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers ORDER BY longest_winning_streak DESC LIMIT :limit")
    fun getLongestWinningStreakCareers(limit: Int): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers ORDER BY longest_unbeaten_streak DESC LIMIT :limit")
    fun getLongestUnbeatenStreakCareers(limit: Int): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers ORDER BY total_goals_for DESC LIMIT :limit")
    fun getHighestScoringCareers(limit: Int): Flow<List<CareersEntity>>

    @Query("SELECT AVG(total_matches) FROM careers")
    suspend fun getAverageMatchesPerCareer(): Double?

    @Query("SELECT AVG(total_trophies) FROM careers")
    suspend fun getAverageTrophiesPerCareer(): Double?

    @Query("SELECT SUM(total_matches) FROM careers")
    suspend fun getTotalMatchesPlayed(): Int?

    @Query("SELECT SUM(total_wins) FROM careers")
    suspend fun getTotalWins(): Int?

    @Query("SELECT SUM(total_trophies) FROM careers")
    suspend fun getTotalTrophiesWon(): Int?

    // ============ SEARCH ============

    @Query("""
        SELECT * FROM careers 
        WHERE manager_name LIKE '%' || :searchQuery || '%' 
           OR team_name LIKE '%' || :searchQuery || '%' 
           OR league_name LIKE '%' || :searchQuery || '%' 
        ORDER BY last_updated DESC
    """)
    fun searchCareers(searchQuery: String): Flow<List<CareersEntity>>

    // ============ DATE-RANGE QUERIES ============

    @Query("SELECT * FROM careers WHERE created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    fun getCareersInDateRange(startTime: Long, endTime: Long): Flow<List<CareersEntity>>

    @Query("SELECT * FROM careers WHERE last_updated BETWEEN :startTime AND :endTime ORDER BY last_updated DESC")
    fun getRecentlyUpdatedCareers(startTime: Long, endTime: Long): Flow<List<CareersEntity>>

    // ============ AGGREGATE STATISTICS ============

    @Query("""
        SELECT 
            SUM(total_matches) as totalMatches, 
            SUM(total_wins) as totalWins, 
            SUM(total_draws) as totalDraws, 
            SUM(total_losses) as totalLosses, 
            SUM(total_goals_for) as totalGoalsFor, 
            SUM(total_goals_against) as totalGoalsAgainst, 
            SUM(total_trophies) as totalTrophies 
        FROM careers
    """)
    suspend fun getAggregateStatistics(): AggregateCareerStatistics

    @Query("""
        SELECT 
            AVG(total_matches) as avgMatches, 
            AVG(total_wins) as avgWins, 
            AVG(total_trophies) as avgTrophies, 
            AVG(longest_winning_streak) as avgWinningStreak 
        FROM careers
    """)
    suspend fun getAverageStatistics(): AverageCareerStatistics

    // ============ UPDATE QUERIES ============

    @Query("UPDATE careers SET is_active = 0 WHERE manager_id = :managerId AND is_active = 1")
    suspend fun deactivateAllCareersForManager(managerId: Int)

    @Query("UPDATE careers SET is_active = 0 WHERE id = :careerId")
    suspend fun deactivateCareer(careerId: Int)

    @Query("UPDATE careers SET is_active = 1 WHERE id = :careerId")
    suspend fun reactivateCareer(careerId: Int)

    @Query("UPDATE careers SET last_updated = :timestamp WHERE id = :careerId")
    suspend fun updateLastUpdated(careerId: Int, timestamp: Long)

    @Query("UPDATE careers SET notes = :notes WHERE id = :careerId")
    suspend fun updateNotes(careerId: Int, notes: String?)

    @Query("UPDATE careers SET save_name = :saveName WHERE id = :careerId")
    suspend fun updateSaveName(careerId: Int, saveName: String?)

    @Query("UPDATE careers SET achievements = :achievements WHERE id = :careerId")
    suspend fun updateAchievements(careerId: Int, achievements: String?)

    @Query("""
        UPDATE careers SET 
            total_transfer_budget_used = total_transfer_budget_used + :amount 
        WHERE id = :careerId
    """)
    suspend fun addTransferBudgetUsed(careerId: Int, amount: Long)

    @Query("""
        UPDATE careers SET 
            total_wage_budget_used = total_wage_budget_used + :amount 
        WHERE id = :careerId
    """)
    suspend fun addWageBudgetUsed(careerId: Int, amount: Long)

    // ============ ADDITIONAL STATISTICS QUERIES ============

    /**
     * Get careers with best goal difference
     */
    @Query("""
        SELECT * FROM careers 
        WHERE total_matches >= 10 
        ORDER BY (total_goals_for - total_goals_against) DESC 
        LIMIT :limit
    """)
    fun getBestGoalDifferenceCareers(limit: Int): Flow<List<CareersEntity>>

    /**
     * Get careers by win count in a specific season
     */
    @Query("SELECT * FROM careers WHERE season = :season ORDER BY total_wins DESC LIMIT :limit")
    fun getTopWinningCareersBySeason(season: Int, limit: Int): Flow<List<CareersEntity>>

    /**
     * Get careers by difficulty with minimum matches
     */
    @Query("""
        SELECT * FROM careers 
        WHERE difficulty = :difficulty AND total_matches >= :minMatches 
        ORDER BY (CAST(total_wins AS REAL) / total_matches) DESC 
        LIMIT :limit
    """)
    fun getTopCareersByDifficulty(
        difficulty: String,
        minMatches: Int,
        limit: Int
    ): Flow<List<CareersEntity>>
}

// ============ DATA CLASSES FOR QUERY RESULTS ============

/**
 * Distribution of careers by difficulty level
 */
data class DifficultyDistribution(
    val difficulty: String,
    val count: Int
)

/**
 * Distribution of careers by season
 */
data class SeasonDistribution(
    val season: Int,
    val count: Int
)

/**
 * Distribution of careers by game mode
 */
data class ModeDistribution(
    val mode: String,
    val count: Int
)

/**
 * Aggregate career statistics
 */
data class AggregateCareerStatistics(
    val totalMatches: Int?,
    val totalWins: Int?,
    val totalDraws: Int?,
    val totalLosses: Int?,
    val totalGoalsFor: Int?,
    val totalGoalsAgainst: Int?,
    val totalTrophies: Int?
) {
    val totalGoalsDifference: Int?
        get() = if (totalGoalsFor != null && totalGoalsAgainst != null) {
            totalGoalsFor - totalGoalsAgainst
        } else null

    val winPercentage: Double?
        get() = if (totalMatches != null && totalMatches > 0 && totalWins != null) {
            (totalWins.toDouble() / totalMatches * 100)
        } else null
}

/**
 * Average career statistics
 */
data class AverageCareerStatistics(
    val avgMatches: Double?,
    val avgWins: Double?,
    val avgTrophies: Double?,
    val avgWinningStreak: Double?
)