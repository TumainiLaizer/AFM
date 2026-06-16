package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ShortlistDao
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.ShortlistEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ShortlistRepository @Inject constructor(
    private val shortlistDaoProvider: Provider<ShortlistDao>
) {
    private val shortlistDao get() = shortlistDaoProvider.get()

    fun getShortlistedPlayers(): Flow<List<PlayersEntity>> = shortlistDao.getShortlistedPlayers()

    suspend fun addToShortlist(playerId: Int, priority: Int = 1, notes: String? = null) {
        shortlistDao.addToShortlist(ShortlistEntity(playerId = playerId, priority = priority, notes = notes))
    }

    suspend fun removeFromShortlist(playerId: Int) {
        shortlistDao.removeFromShortlist(playerId)
    }

    fun isShortlisted(playerId: Int): Flow<Boolean> = shortlistDao.isShortlisted(playerId)
}
