package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.SponsorshipDealDao
import com.fameafrica.afm.data.database.entities.SponsorshipDealEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SponsorshipDealRepository @Inject constructor(
    private val sponsorshipDealDaoProvider: Provider<SponsorshipDealDao>
) {
    private val sponsorshipDealDao get() = sponsorshipDealDaoProvider.get()

    fun getActiveDeals(teamId: Int): Flow<List<SponsorshipDealEntity>> = 
        sponsorshipDealDao.getActiveDealsByTeam(teamId)

    suspend fun insertDeal(deal: SponsorshipDealEntity): Long = 
        sponsorshipDealDao.insert(deal)

    suspend fun deactivateDeal(dealId: Int) = 
        sponsorshipDealDao.deactivateDeal(dealId)
}
