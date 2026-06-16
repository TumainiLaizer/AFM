package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.dao.FinancesDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.entities.TeamsEntity
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FanEconomyEngine @Inject constructor(
    private val financesDaoProvider: Provider<FinancesDao>,
    private val teamsDaoProvider: Provider<TeamsDao>
) {
    private val financesDao get() = financesDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()

    suspend fun processMatchdayRevenue(teamId: Int, season: String, attendance: Int, isDerby: Boolean) {
        val team = teamsDao.getById(teamId) ?: return
        
        // 1. Ticket Revenue
        val baseTicketPrice = calculateBaseTicketPrice(team)
        val derbyMultiplier = if (isDerby) 1.5 else 1.0
        val ticketRevenue = (attendance * baseTicketPrice * derbyMultiplier).toLong()
        
        financesDao.addTicketRevenue(teamId, season, ticketRevenue)
        financesDao.addToBankBalance(teamId, season, ticketRevenue)

        // 2. Merchandise Sales (Kits, Scarves, etc.)
        val merchConversionRate = 0.1 + (team.fanLoyalty / 500.0) // 10% to 30%
        val buyers = (attendance * merchConversionRate).toInt()
        val avgSpend = Random.nextInt(5, 25)
        val merchRevenue = (buyers * avgSpend).toLong()
        
        financesDao.addMerchandiseRevenue(teamId, season, merchRevenue)
        financesDao.addToBankBalance(teamId, season, merchRevenue)
    }

    suspend fun simulateFanGrowth(teamId: Int, recentPerformance: Double) {
        val team = teamsDao.getById(teamId) ?: return
        
        // Growth based on reputation and recent form
        val growthFactor = (recentPerformance * 0.05) + (team.reputation / 1000.0)
        val newFans = (team.fanLoyalty * (1.0 + growthFactor)).toInt().coerceIn(0, 100)
        
        teamsDao.updateFanLoyalty(teamId, newFans)
    }

    private fun calculateBaseTicketPrice(team: TeamsEntity): Int {
        return when {
            team.reputation > 80 -> 40
            team.reputation > 60 -> 25
            team.reputation > 40 -> 15
            else -> 8
        }
    }
}
