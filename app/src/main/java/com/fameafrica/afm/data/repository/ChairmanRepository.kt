package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ChairmanDao
import com.fameafrica.afm.data.database.entities.ChairmanEntity
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ChairmanRepository @Inject constructor(
    private val chairmanDaoProvider: Provider<ChairmanDao>
) {
    private val chairmanDao get() = chairmanDaoProvider.get()

    suspend fun getAvailableChairmen() = chairmanDao.getAvailableChairmen()
    
    suspend fun getAvailableChairmenByRegion(region: String) = chairmanDao.getAvailableChairmenByRegion(region)
    
    suspend fun insertChairman(chairman: ChairmanEntity) = chairmanDao.insertChairman(chairman)
    
    suspend fun updateChairman(chairman: ChairmanEntity) = chairmanDao.updateChairman(chairman)
    
    suspend fun markAsUnavailable(chairmanId: Int) = chairmanDao.markAsUnavailable(chairmanId)

    suspend fun getChairmanByTeam(teamId: Int) = chairmanDao.getByTeamId(teamId)
}
