package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.IdentityMappingEntity

@Dao
interface IdentityMappingDao {
    @Query("SELECT * FROM identity_mappings WHERE category = :category")
    suspend fun getByCategory(category: String): List<IdentityMappingEntity>

    @Query("SELECT target_id FROM identity_mappings WHERE source_name = :sourceName AND category = :category LIMIT 1")
    suspend fun getTargetId(sourceName: String, category: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: IdentityMappingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mappings: List<IdentityMappingEntity>)

    @Query("DELETE FROM identity_mappings")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM identity_mappings")
    suspend fun getCount(): Int

    @Query("SELECT * FROM identity_mappings")
    suspend fun getAllStatic(): List<IdentityMappingEntity>
}
