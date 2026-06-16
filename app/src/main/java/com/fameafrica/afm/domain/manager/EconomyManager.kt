package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.dao.FinancesDao
import com.fameafrica.afm.data.database.dao.SponsorshipDealDao
import com.fameafrica.afm.data.database.entities.SponsorshipDealEntity
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class EconomyManager @Inject constructor(
    private val financesDaoProvider: Provider<FinancesDao>,
    private val sponsorshipDealDaoProvider: Provider<SponsorshipDealDao>
) {
    private val financesDao get() = financesDaoProvider.get()
    private val sponsorshipDealDao get() = sponsorshipDealDaoProvider.get()

    // ============ CURRENCY MANAGEMENT ============

    suspend fun addCoins(teamId: Int, season: String, amount: Long) {
        financesDao.addCoins(teamId, season, amount)
    }

    suspend fun spendCoins(teamId: Int, season: String, amount: Long): Boolean {
        val finances = financesDao.getByTeamAndSeason(teamId, season) ?: return false
        if (finances.coins >= amount) {
            financesDao.addCoins(teamId, season, -amount)
            return true
        }
        return false
    }

    suspend fun addCash(teamId: Int, season: String, amount: Long) {
        financesDao.addToBankBalance(teamId, season, amount)
    }

    suspend fun injectEmergencyCash(teamId: Int, season: String, coinsAmount: Long, cashAmount: Long): Boolean {
        if (spendCoins(teamId, season, coinsAmount)) {
            addCash(teamId, season, cashAmount)
            return true
        }
        return false
    }

    // ============ MONTHLY PROCESSING ============

    suspend fun processEndOfMonth(teamId: Int, season: String) {
        val finances = financesDao.getByTeamAndSeason(teamId, season) ?: return

        // 1. Sponsorship Payouts
        val activeDeals = sponsorshipDealDao.getAllByTeamSync(teamId).filter { it.isActive }
        var totalSponsorshipIncome = 0L
        activeDeals.forEach { deal ->
            totalSponsorshipIncome += deal.payoutPerMonth
            // TODO: Check objectives and performance bonuses
        }
        financesDao.addSponsorshipIncome(teamId, season, totalSponsorshipIncome)
        financesDao.addToBankBalance(teamId, season, totalSponsorshipIncome)

        // 2. Wage Expenses
        val totalWages = finances.wageBill / 12 + finances.staffWages / 12
        financesDao.addWageExpenses(teamId, season, totalWages)
        financesDao.addToBankBalance(teamId, season, -totalWages)

        // 3. Operational Costs
        val operationalCosts = finances.operationalCosts / 12
        financesDao.addToBankBalance(teamId, season, -operationalCosts)

        // 4. Update Financial Tier & Health
        // This is partially handled in Repository but we can add more logic here
    }

    // ============ INFLATION & ECONOMY SCALING ============

    suspend fun applyInflation(season: String) {
        // African context: Inflation can be significant in some regions
        val inflationRate = Random.nextDouble(0.02, 0.15) // 2% to 15%
        // TODO: Apply to wages, ticket prices, etc. for next season
    }
}
