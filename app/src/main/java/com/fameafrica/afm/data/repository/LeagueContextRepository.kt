package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.LeagueContextDao
import com.fameafrica.afm.data.database.entities.LeagueContextEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class LeagueContextRepository @Inject constructor(
    private val leagueContextDaoProvider: Provider<LeagueContextDao>
) {
    private val leagueContextDao get() = leagueContextDaoProvider.get()
    fun getAllLeagueContexts(): Flow<List<LeagueContextEntity>> = leagueContextDao.getAllLeagueContexts()

    suspend fun getLeagueContext(leagueName: String): LeagueContextEntity? = leagueContextDao.getLeagueContext(leagueName)

    suspend fun updateLeagueContext(context: LeagueContextEntity) {
        leagueContextDao.insertOrUpdate(context)
    }
}
