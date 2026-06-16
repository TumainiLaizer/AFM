package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.WorldStateDao
import com.fameafrica.afm.data.database.entities.WorldStateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class WorldStateRepository @Inject constructor(
    private val worldStateDaoProvider: Provider<WorldStateDao>
) {
    private val worldStateDao get() = worldStateDaoProvider.get()
    fun getWorldState(): Flow<WorldStateEntity?> = worldStateDao.getWorldState()

    suspend fun updateWorldState(worldState: WorldStateEntity) {
        worldStateDao.insertOrUpdate(worldState)
    }
}
