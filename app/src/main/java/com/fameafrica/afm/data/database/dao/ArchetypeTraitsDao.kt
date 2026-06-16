package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ArchetypeTraitsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchetypeTraitsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM archetype_traits ORDER BY archetype_name")
    fun getAll(): Flow<List<ArchetypeTraitsEntity>>

    @Query("SELECT * FROM archetype_traits WHERE id = :id")
    suspend fun getById(id: Int): ArchetypeTraitsEntity?

    @Query("SELECT * FROM archetype_traits WHERE archetype_name = :name")
    suspend fun getByName(name: String): ArchetypeTraitsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(archetype: ArchetypeTraitsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(archetypes: List<ArchetypeTraitsEntity>)

    @Update
    suspend fun update(archetype: ArchetypeTraitsEntity)

    @Delete
    suspend fun delete(archetype: ArchetypeTraitsEntity)

    @Query("DELETE FROM archetype_traits")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM archetype_traits")
    suspend fun getCount(): Int

    // ============ TRAIT-BASED QUERIES ============

    @Query("SELECT * FROM archetype_traits WHERE primary_trait = :trait OR secondary_trait = :trait")
    fun getByTrait(trait: String): Flow<List<ArchetypeTraitsEntity>>

    @Query("SELECT * FROM archetype_traits WHERE primary_trait = :trait")
    fun getByPrimaryTrait(trait: String): Flow<List<ArchetypeTraitsEntity>>

    // ============ FOCUS-BASED QUERIES ============

    @Query("SELECT * FROM archetype_traits WHERE gameplay_focus LIKE '%' || :focus || '%'")
    fun getByGameplayFocus(focus: String): Flow<List<ArchetypeTraitsEntity>>

    // ============ SEARCH QUERIES ============

    @Query("SELECT * FROM archetype_traits WHERE archetype_name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%'")
    fun searchArchetypes(searchQuery: String): Flow<List<ArchetypeTraitsEntity>>
    @Query("SELECT * FROM archetype_traits ORDER BY archetype_name ASC")
    suspend fun getAllStatic(): List<ArchetypeTraitsEntity>
}

// ============ DATA CLASSES ============

data class ArchetypeWithStats(
    @Embedded
    val archetype: ArchetypeTraitsEntity,

    @ColumnInfo(name = "player_count")
    val playerCount: Int,

    @ColumnInfo(name = "avg_rating")
    val averageRating: Double
)