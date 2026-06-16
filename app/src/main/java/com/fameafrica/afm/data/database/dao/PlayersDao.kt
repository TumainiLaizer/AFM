package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayersDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM players ORDER BY rating DESC, name ASC")
    fun getAll(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players ORDER BY rating DESC, name ASC LIMIT :limit OFFSET :offset")
    suspend fun getAllPaged(limit: Int, offset: Int): List<PlayersEntity>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Int): PlayersEntity?

    @Query("SELECT * FROM players WHERE name = :name")
    suspend fun getByName(name: String): PlayersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: PlayersEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayersEntity>)

    @Update
    suspend fun update(player: PlayersEntity)

    @Update
    suspend fun updateAll(players: List<PlayersEntity>)

    @Delete
    suspend fun delete(player: PlayersEntity)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deleteById(playerId: Int): Int

    @Query("DELETE FROM players")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM players")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM players WHERE retired = 0")
    suspend fun getActiveCount(): Int

    @Query("SELECT team_id FROM players GROUP BY team_id HAVING COUNT(*) < :minCount")
    suspend fun getTeamIdsWithFewPlayers(minCount: Int): List<Int>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM players WHERE team_id = :teamId ORDER BY rating DESC, position_category, shirt_number")
    fun getPlayersByTeamId(teamId: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE team_id = :teamId ORDER BY rating DESC, position_category, shirt_number")
    suspend fun getPlayersByTeamIdStatic(teamId: Int): List<PlayersEntity>

    @Query("SELECT * FROM players WHERE team_id IN (:teamIds)")
    suspend fun getPlayersByTeamIdsStatic(teamIds: List<Int>): List<PlayersEntity>

    @Query("SELECT * FROM players WHERE team_name = :teamName ORDER BY rating DESC, position_category, shirt_number")
    fun getPlayersByTeamName(teamName: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE team_id = :teamId AND position_category = :category ORDER BY rating DESC")
    fun getPlayersByTeamAndCategory(teamId: Int, category: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE team_id = :teamId AND is_starting_xi = 1 ORDER BY position_category, shirt_number")
    fun getStartingXI(teamId: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE team_id = :teamId AND is_captain = 1")
    suspend fun getTeamCaptain(teamId: Int): PlayersEntity?

    @Query("SELECT * FROM players WHERE team_id = :teamId AND is_vice_captain = 1")
    suspend fun getTeamViceCaptain(teamId: Int): PlayersEntity?

    @Query("SELECT COUNT(*) FROM players WHERE team_id = :teamId AND injury_status != 'HEALTHY'")
    suspend fun getInjuredCountByTeam(teamId: Int): Int

    // ============ POSITION-BASED QUERIES ============

    @Query("SELECT * FROM players WHERE position = :position ORDER BY rating DESC")
    fun getPlayersByPosition(position: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE position_category = :category ORDER BY rating DESC")
    fun getPlayersByCategory(category: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE position_category = :category AND nationality = :nationality ORDER BY rating DESC")
    fun getPlayersByCategoryAndNationality(category: String, nationality: String): Flow<List<PlayersEntity>>

    // ============ NATIONALITY-BASED QUERIES ============

    @Query("SELECT * FROM players WHERE nationality = :nationality ORDER BY rating DESC")
    fun getPlayersByNationality(nationality: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE nationality = :nationality AND retired = 0 ORDER BY rating DESC")
    fun getActivePlayersByNationality(nationality: String): Flow<List<PlayersEntity>>

    @Query("SELECT DISTINCT nationality FROM players WHERE nationality IS NOT NULL ORDER BY nationality")
    fun getDistinctNationalities(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM players WHERE nationality = :nationality")
    suspend fun getPlayerCountByNationality(nationality: String): Int

    // ============ RATING-BASED QUERIES ============

    @Query("SELECT * FROM players WHERE rating >= :minRating ORDER BY rating DESC")
    fun getPlayersByMinRating(minRating: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE rating BETWEEN :minRating AND :maxRating ORDER BY rating DESC")
    fun getPlayersByRatingRange(minRating: Int, maxRating: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players ORDER BY rating DESC LIMIT :limit")
    fun getTopRatedPlayers(limit: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE position = :position ORDER BY rating DESC LIMIT :limit")
    fun getTopRatedByPosition(position: String, limit: Int): Flow<List<PlayersEntity>>

    // ============ POTENTIAL-BASED QUERIES ============

    @Query("SELECT * FROM players WHERE potential >= :minPotential AND age <= 23 ORDER BY potential DESC")
    fun getTopYoungPlayers(minPotential: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE age <= 23 ORDER BY (potential - rating) DESC LIMIT :limit")
    fun getBiggestPotential(limit: Int): Flow<List<PlayersEntity>>

    // ============ AGE-BASED QUERIES ============

    @Query("SELECT * FROM players WHERE age <= 21 ORDER BY rating DESC")
    fun getYouthPlayers(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE age >= 30 ORDER BY rating DESC")
    fun getVeteranPlayers(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE age BETWEEN :minAge AND :maxAge ORDER BY rating DESC")
    fun getPlayersByAgeRange(minAge: Int, maxAge: Int): Flow<List<PlayersEntity>>

    // ============ INJURY & STATUS QUERIES ============

    @Query("SELECT * FROM players WHERE injury_status != 'HEALTHY' ORDER BY recovery_time")
    fun getInjuredPlayers(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE suspended = 1")
    fun getSuspendedPlayers(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE retired = 1")
    fun getRetiredPlayers(): Flow<List<PlayersEntity>>

    // ============ CONTRACT & TRANSFER QUERIES ============

    @Query("SELECT * FROM players WHERE free_agent = 1 ORDER BY rating DESC")
    fun getFreeAgents(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE transfer_list_status = 'AVAILABLE' ORDER BY market_value DESC")
    fun getTransferListed(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE transfer_list_status = 'LOAN_LISTED' ORDER BY rating DESC")
    fun getLoanListed(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE market_value BETWEEN :minValue AND :maxValue ORDER BY market_value DESC")
    fun getPlayersByMarketValueRange(minValue: Int, maxValue: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE contract_expiry <= date('now', '+1 year') ORDER BY contract_expiry ASC")
    fun getPlayersWithExpiringContracts(): Flow<List<PlayersEntity>>

    // ============ PERSONALITY, TRAITS & ARCHETYPE QUERIES ============

    @Query("SELECT * FROM players WHERE personality_type = :personality ORDER BY rating DESC")
    fun getPlayersByPersonality(personality: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE archetype = :archetype ORDER BY rating DESC")
    fun getPlayersByArchetype(archetype: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE primary_trait = :trait OR secondary_trait = :trait ORDER BY rating DESC")
    fun getPlayersByTrait(trait: String): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE leadership >= 80 ORDER BY leadership DESC")
    fun getPotentialCaptains(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE dressing_room_influence >= 70 ORDER BY dressing_room_influence DESC")
    fun getTeamLeaders(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE media_handling >= 70 ORDER BY media_handling DESC")
    fun getMediaFriendlyPlayers(): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE fan_popularity >= 70 ORDER BY fan_popularity DESC")
    fun getFanFavorites(): Flow<List<PlayersEntity>>

    // ============ HEIGHT & PHYSICAL QUERIES ============

    @Query("SELECT * FROM players WHERE height >= :minHeight ORDER BY height DESC")
    fun getTallestPlayers(minHeight: Int): Flow<List<PlayersEntity>>

    @Query("SELECT * FROM players WHERE preferred_foot = :foot ORDER BY rating DESC")
    fun getPlayersByPreferredFoot(foot: String): Flow<List<PlayersEntity>>

    // ============ SEARCH QUERIES ============

    @Query("SELECT * FROM players WHERE name LIKE '%' || :searchQuery || '%' ORDER BY rating DESC")
    fun searchPlayers(searchQuery: String): Flow<List<PlayersEntity>>

    @Query("""
        SELECT * FROM players 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR nationality LIKE '%' || :searchQuery || '%'
        OR position LIKE '%' || :searchQuery || '%'
        OR team_name LIKE '%' || :searchQuery || '%'
        OR archetype LIKE '%' || :searchQuery || '%'
        ORDER BY rating DESC
    """)
    fun advancedSearch(searchQuery: String): Flow<List<PlayersEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("SELECT AVG(rating) FROM players WHERE team_id = :teamId")
    suspend fun getAverageRatingByTeam(teamId: Int): Double?

    @Query("SELECT SUM(market_value) FROM players WHERE team_id = :teamId")
    suspend fun getTotalMarketValueByTeam(teamId: Int): Long?

    @Query("SELECT AVG(age) FROM players WHERE team_id = :teamId")
    suspend fun getAverageAgeByTeam(teamId: Int): Double?

    @Query("SELECT AVG(height) FROM players WHERE team_id = :teamId")
    suspend fun getAverageHeightByTeam(teamId: Int): Double?

    @Query("""
        SELECT 
            position_category,
            COUNT(*) as player_count,
            AVG(rating) as avg_rating,
            AVG(age) as avg_age,
            AVG(height) as avg_height,
            AVG(market_value) as avg_value
        FROM players 
        WHERE team_id = :teamId
        GROUP BY position_category
        ORDER BY 
            CASE position_category
                WHEN 'GOALKEEPER' THEN 1
                WHEN 'DEFENDER' THEN 2
                WHEN 'MIDFIELDER' THEN 3
                WHEN 'FORWARD' THEN 4
                ELSE 5
            END
    """)
    fun getTeamSquadAnalysis(teamId: Int): Flow<List<SquadAnalysis>>

    @Query("""
        SELECT 
            nationality,
            COUNT(*) as player_count,
            AVG(rating) as avg_rating
        FROM players 
        WHERE nationality IS NOT NULL
        GROUP BY nationality
        ORDER BY player_count DESC
        LIMIT :limit
    """)
    fun getNationalityDistribution(limit: Int = 10): Flow<List<NationalityDistribution>>

    @Query("""
        SELECT 
            archetype,
            COUNT(*) as player_count,
            AVG(rating) as avg_rating
        FROM players 
        WHERE archetype IS NOT NULL
        GROUP BY archetype
        ORDER BY player_count DESC
    """)
    fun getArchetypeDistribution(): Flow<List<ArchetypeDistribution>>

    @Query("""
        SELECT 
            personality_type,
            COUNT(*) as player_count,
            AVG(rating) as avg_rating
        FROM players 
        WHERE personality_type IS NOT NULL
        GROUP BY personality_type
        ORDER BY player_count DESC
    """)
    fun getPersonalityDistribution(): Flow<List<PersonalityDistribution>>

    @Query("""
        SELECT 
            primary_trait,
            COUNT(*) as player_count
        FROM players 
        WHERE primary_trait IS NOT NULL
        GROUP BY primary_trait
        ORDER BY player_count DESC
    """)
    fun getTraitDistribution(): Flow<List<TraitDistribution>>

    // ============ NATIONAL TEAM SELECTION QUERIES ============

    @Query("""
        SELECT * FROM players 
        WHERE nationality = :nationality 
        AND retired = 0
        AND injury_status = 'HEALTHY'
        AND suspended = 0
        ORDER BY rating DESC
    """)
    fun getEligiblePlayersForNationalTeam(nationality: String): Flow<List<PlayersEntity>>

    @Query("""
        SELECT * FROM players 
        WHERE nationality = :nationality 
        AND retired = 0
        AND position = 'GK'
        AND injury_status = 'HEALTHY'
        AND suspended = 0
        ORDER BY rating DESC
    """)
    fun getEligibleGoalkeepersForNationalTeam(nationality: String): Flow<List<PlayersEntity>>

    @Query("""
        SELECT * FROM players 
        WHERE nationality = :nationality 
        AND retired = 0
        AND position_category = 'DEFENDER'
        AND injury_status = 'HEALTHY'
        AND suspended = 0
        ORDER BY rating DESC
    """)
    fun getEligibleDefendersForNationalTeam(nationality: String): Flow<List<PlayersEntity>>

    @Query("""
        SELECT * FROM players 
        WHERE nationality = :nationality 
        AND retired = 0
        AND position_category = 'MIDFIELDER'
        AND injury_status = 'HEALTHY'
        AND suspended = 0
        ORDER BY rating DESC
    """)
    fun getEligibleMidfieldersForNationalTeam(nationality: String): Flow<List<PlayersEntity>>

    @Query("""
        SELECT * FROM players 
        WHERE nationality = :nationality 
        AND retired = 0
        AND position_category = 'FORWARD'
        AND injury_status = 'HEALTHY'
        AND suspended = 0
        ORDER BY rating DESC
    """)
    fun getEligibleForwardsForNationalTeam(nationality: String): Flow<List<PlayersEntity>>

    @Query("""
        SELECT * FROM players 
        WHERE nationality = :nationality 
        AND retired = 0
        AND leadership >= 70
        AND injury_status = 'HEALTHY'
        AND suspended = 0
        ORDER BY leadership DESC
    """)
    fun getPotentialNationalTeamCaptains(nationality: String): Flow<List<PlayersEntity>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            p.*,
            t.name as detail_team_name,
            t.logo_path as detail_team_logo,
            t.league as detail_team_league,
            t.reputation as detail_team_reputation
        FROM players p
        LEFT JOIN teams t ON p.team_id = t.id
        WHERE p.id = :playerId
    """)
    suspend fun getPlayerWithDetails(playerId: Int): PlayerWithDetails?

    @Query("""
        SELECT 
            p.*,
            t.name as detail_team_name,
            t.logo_path as detail_team_logo
        FROM players p
        LEFT JOIN teams t ON p.team_id = t.id
        WHERE p.team_id = :teamId
        ORDER BY 
            CASE p.position_category
                WHEN 'GOALKEEPER' THEN 1
                WHEN 'DEFENDER' THEN 2
                WHEN 'MIDFIELDER' THEN 3
                WHEN 'FORWARD' THEN 4
                ELSE 5
            END,
            p.rating DESC
    """)
    fun getTeamSquadWithDetails(teamId: Int): Flow<List<PlayerWithTeamDetails>>

    // ============ UPDATES ============

    @Query("UPDATE players SET is_starting_xi = 0 WHERE team_id = :teamId")
    suspend fun resetStartingXI(teamId: Int)

    @Query("UPDATE players SET is_starting_xi = 1 WHERE id = :playerId")
    suspend fun setPlayerAsStarter(playerId: Int)

    @Transaction
    suspend fun updateStartingXI(teamId: Int, playerIds: List<Int>) {
        resetStartingXI(teamId)
        playerIds.forEach { setPlayerAsStarter(it) }
    }

    @Query("UPDATE players SET is_captain = 0 WHERE team_id = :teamId")
    suspend fun removeCaptain(teamId: Int)

    @Query("UPDATE players SET is_captain = 1 WHERE id = :playerId")
    suspend fun setCaptain(playerId: Int)

    @Query("UPDATE players SET is_vice_captain = 0 WHERE team_id = :teamId")
    suspend fun removeViceCaptain(teamId: Int)

    @Query("UPDATE players SET is_vice_captain = 1 WHERE id = :playerId")
    suspend fun setViceCaptain(playerId: Int)

    @Query("UPDATE players SET current_form = current_form + :change WHERE id = :playerId")
    suspend fun updatePlayerForm(playerId: Int, change: Int)

    @Query("UPDATE players SET morale = morale + :change WHERE id = :playerId")
    suspend fun updatePlayerMorale(playerId: Int, change: Int)

    @Query("UPDATE players SET experience = experience + 1 WHERE team_id = :teamId AND age <= 23")
    suspend fun incrementYoungPlayerExperience(teamId: Int)

    @Query("UPDATE players SET injury_status = :status, recovery_time = :days WHERE id = :playerId")
    suspend fun setPlayerInjury(playerId: Int, status: String, days: Int)

    @Query("UPDATE players SET injury_status = 'HEALTHY', recovery_time = 0 WHERE id = :playerId")
    suspend fun clearPlayerInjury(playerId: Int)

    @Query("UPDATE players SET suspended = 1 WHERE id = :playerId")
    suspend fun suspendPlayer(playerId: Int)

    @Query("UPDATE players SET suspended = 0 WHERE id = :playerId")
    suspend fun unsuspendPlayer(playerId: Int)

    @Transaction
    suspend fun updateBatch(players: List<PlayersEntity>) {
        players.forEach { update(it) }
    }

    @Query("UPDATE players SET manager_id = :managerId WHERE team_id = :teamId")
    suspend fun updatePlayersManager(teamId: Int, managerId: Int)

    // ============ BULK PERFORMANCE UPDATES ============

    @Query("UPDATE players SET stamina = CASE WHEN stamina + :recovery > 100 THEN 100 ELSE stamina + :recovery END WHERE retired = 0")
    suspend fun recoverAllStamina(recovery: Int)

    @Query("UPDATE players SET recovery_time = recovery_time - 7 WHERE recovery_time > 0")
    suspend fun advanceInjuryRecovery()

    @Query("UPDATE players SET recovery_time = recovery_time - 1 WHERE recovery_time > 0")
    suspend fun advanceInjuryRecoveryDaily()

    @Query("UPDATE players SET injury_status = 'HEALTHY', recovery_time = 0 WHERE recovery_time <= 0 AND injury_status != 'HEALTHY'")
    suspend fun clearRecoveredInjuries()

    @Query("SELECT * FROM players ORDER BY rating DESC, name ASC")
    suspend fun getAllStatic(): List<PlayersEntity>

    @Query("""
        SELECT p.* FROM players p
        INNER JOIN teams t ON p.team_id = t.id
        WHERE t.league = :leagueName
        ORDER BY p.rating DESC
    """)
    fun getPlayersByLeague(leagueName: String): Flow<List<PlayersEntity>>

    @Query("""
        SELECT 
            id, name, age, position, position_category, nationality, 
            team_name, team_id, rating, potential, market_value, salary, 
            morale, current_form, injury_status, suspended, retired, 
            free_agent, transfer_list_status, personality_type, 
            pace, acceleration, stamina, passing, vision, finishing, 
            defending, strength, dribbling, crossing, heading, 
            creativity, skill, agility, positioning, anticipation, 
            decisions, teamwork, composure, aggression, injury_risk, region, contract_expiry
        FROM players 
        ORDER BY rating DESC
    """)
    fun getAllSummaries(): Flow<List<PlayerSummary>>
}
