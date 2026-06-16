package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.RankingsCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RankingsDao {
    @Query("SELECT * FROM rankings_cache WHERE type = :type")
    fun getRankingsByType(type: String): Flow<RankingsCacheEntity?>

    @Query("SELECT * FROM rankings_cache WHERE type = :type")
    suspend fun getRankingsByTypeStatic(type: String): RankingsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ranking: RankingsCacheEntity)

    @Query("DELETE FROM rankings_cache")
    suspend fun deleteAll()
}
