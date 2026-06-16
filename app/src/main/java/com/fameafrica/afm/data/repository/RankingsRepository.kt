package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.RankingsDao
import com.fameafrica.afm.data.database.entities.RankingsCacheEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider

@Singleton
class RankingsRepository @Inject constructor(
    private val rankingsDaoProvider: Provider<RankingsDao>
) {
    private val rankingsDao: RankingsDao?
        get() = try { rankingsDaoProvider.get() } catch (e: Exception) { null }

    fun getRankingsByType(type: String): Flow<RankingsCacheEntity?> {
        return rankingsDao?.getRankingsByType(type) ?: kotlinx.coroutines.flow.flowOf(null)
    }

    suspend fun saveRankings(type: String, jsonData: String) {
        val entity = RankingsCacheEntity(
            type = type,
            jsonData = jsonData,
            lastUpdated = System.currentTimeMillis()
        )
        rankingsDao?.insert(entity)
    }

    suspend fun shouldUpdateRankings(type: String, intervalDays: Int): Boolean {
        val cached = rankingsDao?.getRankingsByTypeStatic(type) ?: return true
        val diff = System.currentTimeMillis() - cached.lastUpdated
        val intervalMillis = intervalDays * 24 * 60 * 60 * 1000L
        return diff > intervalMillis
    }
}
