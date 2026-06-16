package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PersonalityTypesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalityTypesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM personality_types ORDER BY name")
    fun getAll(): Flow<List<PersonalityTypesEntity>>

    @Query("SELECT * FROM personality_types WHERE id = :id")
    suspend fun getById(id: Int): PersonalityTypesEntity?

    @Query("SELECT * FROM personality_types WHERE name = :name")
    suspend fun getByName(name: String): PersonalityTypesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personality: PersonalityTypesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(personalities: List<PersonalityTypesEntity>)

    @Update
    suspend fun update(personality: PersonalityTypesEntity)

    @Delete
    suspend fun delete(personality: PersonalityTypesEntity)

    @Query("DELETE FROM personality_types")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM personality_types")
    suspend fun getCount(): Int

    // ============ EFFECT-BASED QUERIES ============

    @Query("SELECT * FROM personality_types WHERE morale_effect >= 1.1 ORDER BY morale_effect DESC")
    fun getHighMoralePersonalities(): Flow<List<PersonalityTypesEntity>>

    @Query("SELECT * FROM personality_types WHERE morale_effect <= 0.9 ORDER BY morale_effect ASC")
    fun getLowMoralePersonalities(): Flow<List<PersonalityTypesEntity>>

    @Query("SELECT * FROM personality_types WHERE form_consistency >= 1.1 ORDER BY form_consistency DESC")
    fun getConsistentPersonalities(): Flow<List<PersonalityTypesEntity>>

    @Query("SELECT * FROM personality_types WHERE form_consistency <= 0.9 ORDER BY form_consistency ASC")
    fun getInconsistentPersonalities(): Flow<List<PersonalityTypesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            p.name as personality_name,
            COUNT(pl.id) as player_count,
            AVG(pl.rating) as avg_rating,
            AVG(pl.morale) as avg_morale
        FROM personality_types p
        LEFT JOIN players pl ON p.name = pl.personality_type
        GROUP BY p.name
        ORDER BY player_count DESC
    """)
    fun getPersonalityStatistics(): Flow<List<PersonalityStatistics>>
    @Query("SELECT * FROM personality_types ORDER BY name ASC")
    suspend fun getAllStatic(): List<PersonalityTypesEntity>
}

// ============ DATA CLASSES ============

data class PersonalityStatistics(
    @ColumnInfo(name = "personality_name")
    val personalityName: String,

    @ColumnInfo(name = "player_count")
    val playerCount: Int,

    @ColumnInfo(name = "avg_rating")
    val averageRating: Double,

    @ColumnInfo(name = "avg_morale")
    val averageMorale: Double
)