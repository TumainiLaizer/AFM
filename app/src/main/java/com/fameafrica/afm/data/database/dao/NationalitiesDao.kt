package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.NationalitiesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NationalitiesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM nationalities ORDER BY nationality")
    fun getAll(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE id = :id")
    suspend fun getById(id: Int): NationalitiesEntity?

    @Query("SELECT * FROM nationalities WHERE nationality = :nationality")
    suspend fun getByNationality(nationality: String): NationalitiesEntity?

    @Query("SELECT * FROM nationalities WHERE fifa_code = :fifaCode")
    suspend fun getByFifaCode(fifaCode: String): NationalitiesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nationality: NationalitiesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nationalities: List<NationalitiesEntity>)

    @Update
    suspend fun update(nationality: NationalitiesEntity)

    @Delete
    suspend fun delete(nationality: NationalitiesEntity)

    @Query("DELETE FROM nationalities")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM nationalities")
    suspend fun getCount(): Int

    // ============ FIFA CODE LOOKUPS ============

    @Query("SELECT id FROM nationalities WHERE fifa_code = :fifaCode")
    suspend fun getIdByFifaCode(fifaCode: String): Int?

    @Query("SELECT fifa_code FROM nationalities WHERE id = :id")
    suspend fun getFifaCodeById(id: Int): String?

    @Query("SELECT nationality FROM nationalities WHERE fifa_code = :fifaCode")
    suspend fun getNationalityByFifaCode(fifaCode: String): String?

    // ============ CONFEDERATION QUERIES ============

    @Query("SELECT * FROM nationalities WHERE confederation = :confederation ORDER BY nationality")
    fun getByConfederation(confederation: String): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE confederation = 'CAF' ORDER BY nationality")
    fun getAfricanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE confederation = 'UEFA' ORDER BY nationality")
    fun getEuropeanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE confederation = 'CONMEBOL' ORDER BY nationality")
    fun getSouthAmericanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE confederation = 'CONCACAF' ORDER BY nationality")
    fun getNorthAmericanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE confederation = 'AFC' ORDER BY nationality")
    fun getAsianNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE confederation = 'OFC' ORDER BY nationality")
    fun getOceanianNations(): Flow<List<NationalitiesEntity>>

    // ============ AFRICAN REGION QUERIES ============

    @Query("SELECT * FROM nationalities WHERE region = :region ORDER BY nationality")
    fun getByAfricanRegion(region: String): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE region = 'North Africa' ORDER BY nationality")
    fun getNorthAfricanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE region = 'West Africa' ORDER BY nationality")
    fun getWestAfricanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE region = 'East Africa' ORDER BY nationality")
    fun getEastAfricanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE region = 'Central Africa' ORDER BY nationality")
    fun getCentralAfricanNations(): Flow<List<NationalitiesEntity>>

    @Query("SELECT * FROM nationalities WHERE region = 'Southern Africa' ORDER BY nationality")
    fun getSouthernAfricanNations(): Flow<List<NationalitiesEntity>>

    // ============ SEARCH QUERIES ============

    @Query("SELECT * FROM nationalities WHERE nationality LIKE '%' || :searchQuery || '%' OR fifa_code LIKE '%' || :searchQuery || '%' ORDER BY nationality")
    fun searchNationalities(searchQuery: String): Flow<List<NationalitiesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            confederation,
            COUNT(*) as country_count
        FROM nationalities 
        WHERE confederation IS NOT NULL
        GROUP BY confederation
        ORDER BY country_count DESC
    """)
    fun getConfederationDistribution(): Flow<List<ConfederationDistribution>>

    @Query("""
        SELECT 
            region,
            COUNT(*) as country_count
        FROM nationalities 
        WHERE region IS NOT NULL
        GROUP BY region
        ORDER BY country_count DESC
    """)
    fun getAfricanRegionDistribution(): Flow<List<RegionDistribution>>
    @Query("SELECT * FROM nationalities ORDER BY nationality ASC")
    suspend fun getAllStatic(): List<NationalitiesEntity>
}

// ============ DATA CLASSES ============

data class ConfederationDistribution(
    @ColumnInfo(name = "confederation")
    val confederation: String,

    @ColumnInfo(name = "country_count")
    val countryCount: Int
)

data class RegionDistribution(
    @ColumnInfo(name = "region")
    val region: String,

    @ColumnInfo(name = "country_count")
    val countryCount: Int
)