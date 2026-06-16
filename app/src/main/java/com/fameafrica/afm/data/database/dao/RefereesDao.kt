package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.RefereesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RefereesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM referees ORDER BY rating DESC")
    fun getAll(): Flow<List<RefereesEntity>>

    @Query("SELECT * FROM referees WHERE referee_id = :id")
    suspend fun getById(id: Int): RefereesEntity?

    @Query("SELECT * FROM referees WHERE name = :name")
    suspend fun getByName(name: String): RefereesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(referee: RefereesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(referees: List<RefereesEntity>)

    @Update
    suspend fun update(referee: RefereesEntity)

    @Delete
    suspend fun delete(referee: RefereesEntity)

    @Query("DELETE FROM referees")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM referees")
    suspend fun getCount(): Int

    // ============ NATIONALITY-BASED QUERIES ============

    /**
     * Get referees by nationality
     * Used for: Domestic leagues and cups - referees MUST be from same country
     */
    @Query("SELECT * FROM referees WHERE nationality_id = :nationId ORDER BY rating DESC")
    fun getRefereesByNationality(nationId: Int): Flow<List<RefereesEntity>>

    /**
     * Get referees by nationality with minimum rating
     * Used for: League matches requiring certain quality threshold
     */
    @Query("""
        SELECT * FROM referees 
        WHERE nationality_id = :nationId 
        AND rating >= :minRating 
        ORDER BY rating DESC
    """)
    fun getRefereesByNationalityWithRating(nationId: Int, minRating: Int): Flow<List<RefereesEntity>>

    /**
     * Get referee count by nationality
     * Used for: Statistics and referee distribution
     */
    @Query("""
        SELECT nationality_id, COUNT(*) as referee_count 
        FROM referees 
        GROUP BY nationality_id 
        ORDER BY referee_count DESC
    """)
    fun getRefereeCountByNationality(): Flow<List<NationalityRefereeCount>>

    // ============ LEAGUE MATCH REFEREES ============
    // Rule: League matches MUST use referees from the SAME country as the league

    /**
     * Get referees eligible for a league match
     * Criteria:
     * - Same nationality as league country
     * - Minimum rating based on league level
     */
    @Query("""
        SELECT r.* FROM referees r
        WHERE r.nationality_id = :leagueCountryId
        AND r.rating >= :minRating
        ORDER BY r.rating DESC, r.strictness DESC
    """)
    suspend fun getRefereesForLeague(
        leagueCountryId: Int,
        minRating: Int = 60
    ): List<RefereesEntity>

    /**
     * Get strict referees for derby matches
     * Criteria:
     * - Same nationality as league country
     * - High strictness (≥70)
     * - Good rating (≥70)
     */
    @Query("""
        SELECT r.* FROM referees r
        WHERE r.nationality_id = :leagueCountryId
        AND r.strictness >= 70
        AND r.rating >= 70
        ORDER BY r.strictness DESC, r.rating DESC
    """)
    suspend fun getStrictRefereesForDerby(leagueCountryId: Int): List<RefereesEntity>

    /**
     * Get top referees for final matches
     * Criteria:
     * - Same nationality as league country
     * - Highest rated (≥75)
     */
    @Query("""
        SELECT r.* FROM referees r
        WHERE r.nationality_id = :leagueCountryId
        AND r.rating >= 75
        ORDER BY r.rating DESC
        LIMIT 5
    """)
    suspend fun getRefereesForFinal(leagueCountryId: Int): List<RefereesEntity>

    // ============ DOMESTIC CUP REFEREES ============
    // Rule: Domestic cup matches MUST use referees from the SAME country as the cup

    /**
     * Get referees eligible for a domestic cup match
     * Criteria:
     * - Same nationality as cup host country
     * - Minimum rating based on cup stage
     */
    @Query("""
        SELECT r.* FROM referees r
        WHERE r.nationality_id = :cupCountryId
        AND r.rating >= :minRating
        ORDER BY r.rating DESC
    """)
    suspend fun getRefereesForDomesticCup(
        cupCountryId: Int,
        minRating: Int = 65
    ): List<RefereesEntity>

    // ============ NATIONAL TEAM MATCH REFEREES ============
    // Rule: National team matches MUST use NEUTRAL referees
    // Referee nationality != both teams' nationalities

    /**
     * Get neutral referees for national team matches
     * Criteria:
     * - Nationality != home nation
     * - Nationality != away nation
     * - Minimum rating based on match importance
     */
    @Query("""
        SELECT r.* FROM referees r
        WHERE r.nationality_id != :homeNationId 
        AND r.nationality_id != :awayNationId
        AND r.rating >= :minRating
        ORDER BY r.rating DESC, r.bias ASC
    """)
    suspend fun getNeutralRefereesForNationalMatch(
        homeNationId: Int,
        awayNationId: Int,
        minRating: Int = 75
    ): List<RefereesEntity>

    // ============ CAF CONTINENTAL COMPETITION REFEREES ============
    // Rule: CAF matches MUST use NEUTRAL AFRICAN referees
    // Referee nationality != both teams' nationalities
    // Referee MUST be from an African nation

    /**
     * Get neutral African referees for CAF competitions
     * Criteria:
     * - Nationality != home nation
     * - Nationality != away nation
     * - Nationality is African (via subquery to nationalities)
     * - Minimum rating based on competition stage
     */
    @Query("""
        SELECT r.* FROM referees r
        INNER JOIN nationalities n ON r.nationality_id = n.id
        WHERE r.nationality_id != :homeNationId 
        AND r.nationality_id != :awayNationId
        AND n.fifa_code IN (
            'ALG', 'ANG', 'BEN', 'BOT', 'BFA', 'BDI', 'CPV', 'CMR', 'CAF', 'CHA', 
            'COM', 'CGO', 'COD', 'CIV', 'DJI', 'EGY', 'EQG', 'ERI', 'SWZ', 'ETH', 
            'GAB', 'GAM', 'GHA', 'GUI', 'GNB', 'KEN', 'LES', 'LBR', 'LBY', 'MAD', 
            'MWI', 'MLI', 'MRT', 'MRI', 'MAR', 'MOZ', 'NAM', 'NIG', 'NGA', 'RWA', 
            'STP', 'SEN', 'SEY', 'SLE', 'SOM', 'RSA', 'SSD', 'SDN', 'TAN', 'TOG', 
            'TUN', 'UGA', 'ZAM', 'ZWE'
        )
        AND r.rating >= :minRating
        ORDER BY r.rating DESC, r.bias ASC
    """)
    suspend fun getNeutralAfricanRefereesForCAF(
        homeNationId: Int,
        awayNationId: Int,
        minRating: Int = 75
    ): List<RefereesEntity>

    /**
     * Get elite CAF referees for CAF finals (bias 0-9)
     * Criteria:
     * - Neutral African referee
     * - FIFA/AFCON Elite (bias ≤ 9)
     * - High rating (≥85)
     */
    @Query("""
        SELECT r.* FROM referees r
        INNER JOIN nationalities n ON r.nationality_id = n.id
        WHERE r.nationality_id != :homeNationId 
        AND r.nationality_id != :awayNationId
        AND n.fifa_code IN (
            'ALG', 'ANG', 'BEN', 'BOT', 'BFA', 'BDI', 'CPV', 'CMR', 'CAF', 'CHA', 
            'COM', 'CGO', 'COD', 'CIV', 'DJI', 'EGY', 'EQG', 'ERI', 'SWZ', 'ETH', 
            'GAB', 'GAM', 'GHA', 'GUI', 'GNB', 'KEN', 'LES', 'LBR', 'LBY', 'MAD', 
            'MWI', 'MLI', 'MRT', 'MRI', 'MAR', 'MOZ', 'NAM', 'NIG', 'NGA', 'RWA', 
            'STP', 'SEN', 'SEY', 'SLE', 'SOM', 'RSA', 'SSD', 'SDN', 'TAN', 'TOG', 
            'TUN', 'UGA', 'ZAM', 'ZWE'
        )
        AND r.bias <= 9
        AND r.rating >= 85
        ORDER BY r.rating DESC, r.bias ASC
    """)
    suspend fun getEliteCAFRefereesForFinal(
        homeNationId: Int,
        awayNationId: Int
    ): List<RefereesEntity>

    // ============ INTERNATIONAL FRIENDLY REFEREES ============
    // Rule: International friendlies PREFER neutral referees, but can use any if needed

    /**
     * Get referees for international friendly
     * First priority: Neutral referees
     * Fallback: Any qualified referee
     */
    @Query("""
        SELECT r.* FROM referees r
        WHERE r.nationality_id != :homeNationId 
        AND r.nationality_id != :awayNationId
        AND r.rating >= 68
        ORDER BY r.rating DESC
    """)
    suspend fun getNeutralRefereesForFriendly(
        homeNationId: Int,
        awayNationId: Int
    ): List<RefereesEntity>

    // ============ TIER-BASED QUERIES (Based on Bias) ============

    @Query("SELECT * FROM referees WHERE bias BETWEEN 0 AND 9 ORDER BY rating DESC")
    fun getFIFAEliteReferees(): Flow<List<RefereesEntity>>

    @Query("SELECT * FROM referees WHERE bias BETWEEN 10 AND 19 ORDER BY rating DESC")
    fun getContinentalReferees(): Flow<List<RefereesEntity>>

    @Query("SELECT * FROM referees WHERE bias BETWEEN 20 AND 39 ORDER BY rating DESC")
    fun getTopLeagueReferees(): Flow<List<RefereesEntity>>

    @Query("SELECT * FROM referees WHERE bias BETWEEN 40 AND 59 ORDER BY rating DESC")
    fun getAverageReferees(): Flow<List<RefereesEntity>>

    @Query("SELECT * FROM referees WHERE bias >= 60 ORDER BY rating DESC")
    fun getLocalReferees(): Flow<List<RefereesEntity>>

    // ============ STATISTICS & PERFORMANCE ============

    /**
     * Update referee stats after a match
     */
    @Query("""
        UPDATE referees 
        SET matches_officiated = matches_officiated + 1,
            yellow_cards = yellow_cards + :yellowCount,
            red_cards = red_cards + :redCount
        WHERE referee_id = :refereeId
    """)
    suspend fun updateMatchStats(refereeId: Int, yellowCount: Int, redCount: Int)

    /**
     * Update referee rating based on performance
     */
    @Query("UPDATE referees SET rating = :newRating WHERE referee_id = :refereeId")
    suspend fun updateRating(refereeId: Int, newRating: Int)

    /**
     * Get most controversial referees (highest cards per match)
     */
    @Query("""
        SELECT *, 
        CAST((yellow_cards + red_cards * 3) AS REAL) / matches_officiated as cards_per_match 
        FROM referees 
        WHERE matches_officiated > 0
        ORDER BY cards_per_match DESC 
        LIMIT :limit
    """)
    fun getMostControversialReferees(limit: Int = 10): Flow<List<RefereeWithStats>>

    /**
     * Get referees with best rating
     */
    @Query("SELECT * FROM referees ORDER BY rating DESC LIMIT :limit")
    fun getTopRatedReferees(limit: Int = 10): Flow<List<RefereesEntity>>

    /**
     * Get referees with most experience
     */
    @Query("SELECT * FROM referees ORDER BY matches_officiated DESC LIMIT :limit")
    fun getMostExperiencedReferees(limit: Int = 10): Flow<List<RefereesEntity>>

    // ============ AVAILABILITY ============

    /**
     * Get referees not assigned to any match on a specific date
     * Requires fixtures table with referee_id
     */
    @Query("""
        SELECT r.* FROM referees r
        WHERE r.referee_id NOT IN (
            SELECT referee_id FROM fixtures 
            WHERE match_date = :matchDate AND referee_id IS NOT NULL
        )
        ORDER BY r.rating DESC
    """)
    suspend fun getAvailableRefereesForDate(matchDate: String): List<RefereesEntity>

    // ============ SEARCH ============

    @Query("SELECT * FROM referees WHERE name LIKE '%' || :searchQuery || '%' ORDER BY rating DESC")
    fun searchReferees(searchQuery: String): Flow<List<RefereesEntity>>

    // ============ JOIN QUERIES ============

    /**
     * Get referee with full nationality details
     */
    @Query("""
        SELECT r.*, n.nationality as nationality_name, n.fifa_code as nationality_fifa_code, n.flag_path
        FROM referees r
        INNER JOIN nationalities n ON r.nationality_id = n.id
        WHERE r.referee_id = :refereeId
    """)
    suspend fun getRefereeWithNationality(refereeId: Int): RefereeWithNationality?

    /**
     * Get all referees with their nationality details
     */
    @Query("""
        SELECT r.*, n.nationality as nationality_name, n.fifa_code as nationality_fifa_code, n.flag_path
        FROM referees r
        INNER JOIN nationalities n ON r.nationality_id = n.id
        ORDER BY n.nationality, r.rating DESC
    """)
    fun getAllRefereesWithNationalities(): Flow<List<RefereeWithNationality>>
    @Query("SELECT * FROM referees ORDER BY rating DESC, name ASC")
    suspend fun getAllStatic(): List<RefereesEntity>
}

// ============ DATA CLASSES FOR COMPLEX QUERIES ============

data class NationalityRefereeCount(
    @ColumnInfo(name = "nationality_id")
    val nationalityId: Int,

    @ColumnInfo(name = "referee_count")
    val refereeCount: Int
)

data class RefereeWithStats(
    @Embedded
    val referee: RefereesEntity,

    @ColumnInfo(name = "cards_per_match")
    val cardsPerMatch: Double
)

data class RefereeWithNationality(
    @Embedded
    val referee: RefereesEntity,

    @ColumnInfo(name = "nationality_name")
    val nationalityName: String,

    @ColumnInfo(name = "nationality_fifa_code")
    val nationalityFifaCode: String,

    @ColumnInfo(name = "flag_path")
    val flagPath: String?
)