package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.CupsEntity
import com.fameafrica.afm.data.database.entities.NationalitiesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CupsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM cups")
    fun getAll(): Flow<List<CupsEntity>>

    @Query("SELECT * FROM cups WHERE id = :id")
    suspend fun getById(id: Int): CupsEntity?

    @Query("SELECT * FROM cups WHERE name = :name")
    suspend fun getByName(name: String): CupsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cup: CupsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cups: List<CupsEntity>)

    @Update
    suspend fun update(cup: CupsEntity)

    @Delete
    suspend fun delete(cup: CupsEntity)

    @Query("DELETE FROM cups")
    suspend fun deleteAll()

    // ============ DOMESTIC CUPS (Country-based) ============

    /**
     * Get all domestic cups in a specific country
     * Used for: CRDB Federation Cup, PBZ Cup, Muungano Cup, Fame Africa Cup, Nigerian FA Cup, etc.
     */
    @Query("SELECT * FROM cups WHERE country_id = :countryId ORDER BY prize_money DESC")
    fun getDomesticCupsByCountry(countryId: Int): Flow<List<CupsEntity>>

    /**
     * Get all domestic cups with their country details
     */
    @Transaction
    @Query("""
        SELECT * FROM cups 
        WHERE country_id IS NOT NULL
    """)
    fun getDomesticCupsWithCountries(): Flow<List<CupWithCountry>>

    // ============ CONTINENTAL CUPS (No country) ============

    /**
     * Get all continental cups (CAF Champions League, CAF Confederation Cup, etc.)
     */
    @Query("SELECT * FROM cups WHERE country_id IS NULL")
    fun getContinentalCups(): Flow<List<CupsEntity>>

    /**
     * Get CAF competitions specifically
     */
    @Query("SELECT * FROM cups WHERE country_id IS NULL AND (type LIKE '%CAF%' OR name LIKE '%CAF%')")
    fun getCAFCompetitions(): Flow<List<CupsEntity>>

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM cups WHERE type = :type")
    fun getCupsByType(type: String): Flow<List<CupsEntity>>

    @Query("SELECT DISTINCT type FROM cups WHERE type IS NOT NULL")
    fun getCupTypes(): Flow<List<String>>

    // ============ PRIZE MONEY QUERIES ============

    @Query("SELECT * FROM cups WHERE prize_money >= :minPrize ORDER BY prize_money DESC")
    fun getHighValueCups(minPrize: Int): Flow<List<CupsEntity>>

    @Query("SELECT * FROM cups WHERE prize_money BETWEEN :minPrize AND :maxPrize ORDER BY prize_money DESC")
    fun getCupsByPrizeRange(minPrize: Int, maxPrize: Int): Flow<List<CupsEntity>>

    @Query("SELECT SUM(prize_money) FROM cups WHERE country_id = :countryId")
    suspend fun getTotalPrizeMoneyByCountry(countryId: Int): Long?

    // ============ TEAMS INVOLVED ============

    @Query("SELECT * FROM cups WHERE teams_involved >= :minTeams ORDER BY teams_involved DESC")
    fun getLargeTournaments(minTeams: Int): Flow<List<CupsEntity>>

    @Query("SELECT * FROM cups WHERE teams_involved <= :maxTeams")
    fun getSmallTournaments(maxTeams: Int): Flow<List<CupsEntity>>

    // ============ SPONSOR QUERIES ============

    @Query("SELECT * FROM cups WHERE sponsor = :sponsorName")
    fun getCupsBySponsor(sponsorName: String): Flow<List<CupsEntity>>

    // ============ STATISTICS ============

    @Query("SELECT COUNT(*) FROM cups WHERE country_id IS NOT NULL")
    suspend fun getDomesticCupCount(): Int

    @Query("SELECT COUNT(*) FROM cups WHERE country_id IS NULL")
    suspend fun getContinentalCupCount(): Int

    @Query("""
        SELECT 
            CASE 
                WHEN country_id IS NULL THEN 'Continental'
                ELSE 'Domestic'
            END as cup_category,
            COUNT(*) as cup_count,
            AVG(prize_money) as avg_prize,
            AVG(teams_involved) as avg_teams
        FROM cups 
        GROUP BY cup_category
    """)
    fun getCupStatistics(): Flow<List<CupStatistic>>
    @Query("SELECT * FROM cups ORDER BY name ASC")
    suspend fun getAllStatic(): List<CupsEntity>
}

// Data classes for complex results
data class CupWithCountry(
    @Embedded
    val cup: CupsEntity,

    @Relation(
        parentColumn = "country_id",
        entityColumn = "id"
    )
    val country: NationalitiesEntity?
)

data class CupStatistic(
    @ColumnInfo(name = "cup_category")
    val cupCategory: String,

    @ColumnInfo(name = "cup_count")
    val cupCount: Int,

    @ColumnInfo(name = "avg_prize")
    val averagePrize: Double,

    @ColumnInfo(name = "avg_teams")
    val averageTeams: Double
)
