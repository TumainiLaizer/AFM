package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.FinancesDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.dao.LeaguesDao
import com.fameafrica.afm.data.database.dao.InfrastructureUpgradesDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FinancesRepository @Inject constructor(
    private val financesDaoProvider: Provider<FinancesDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val leaguesDaoProvider: Provider<LeaguesDao>,
    private val infrastructureUpgradesDaoProvider: Provider<InfrastructureUpgradesDao>,
    private val chairmanRepository: ChairmanRepository
) {
    private val financesDao get() = financesDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    private val leaguesDao get() = leaguesDaoProvider.get()
    private val infrastructureUpgradesDao get() = infrastructureUpgradesDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllFinances(): Flow<List<FinancesEntity>> = financesDao.getAll()

    suspend fun getFinancesById(id: Int): FinancesEntity? = financesDao.getById(id)

    suspend fun getTeamFinances(teamId: Int, season: String): FinancesEntity? =
        financesDao.getByTeamAndSeason(teamId, season)

    fun getTeamFinancesFlow(teamId: Int, season: String): Flow<FinancesEntity?> =
        financesDao.getTeamFinancesFlow(teamId, season)

    suspend fun getTeamFinancesHistory(teamId: Int): Flow<List<FinancesEntity>> =
        financesDao.getTeamFinances(teamId)

    suspend fun insertFinances(finances: FinancesEntity) = financesDao.insert(finances)

    suspend fun updateFinances(finances: FinancesEntity) = financesDao.update(finances)

    // ============ INITIALIZE FINANCES FOR NEW SEASON ============

    suspend fun initializeSeasonFinances(season: String) {
        val teams = teamsDao.getAll().firstOrNull() ?: return

        for (team in teams) {
            val existing = financesDao.getByTeamAndSeason(team.id, season)
            if (existing != null) continue

            val league = leaguesDao.getLeagueByName(team.league)
            val financialTier = determineFinancialTier(team, league)

            val baseRevenue = calculateBaseRevenue(team, league, financialTier)
            val baseBudget = calculateBaseBudget(baseRevenue, team.reputation)

            val finances = FinancesEntity(
                teamId = team.id,
                teamName = team.name,
                season = season,
                revenue = baseRevenue,
                budget = baseBudget,
                bankBalance = baseBudget / 2,
                financialTier = financialTier.value,
                sponsorshipRevenue = calculateSponsorshipRevenue(team, league, financialTier),
                broadcastingRevenue = calculateBroadcastingRevenue(league, financialTier),
                matchdayRevenue = calculateMatchdayRevenue(team, league, financialTier),
                merchandiseRevenue = calculateMerchandiseRevenue(team, financialTier),
                membershipRevenue = calculateMembershipRevenue(team, financialTier),
                wageBill = calculateWageBill(team, league, financialTier),
                staffWages = calculateStaffWages(team, financialTier),
                operationalCosts = calculateOperationalCosts(financialTier),
                lastUpdated = getCurrentDate()
            )

            val updatedFinances = finances.copy(
                expenses = finances.wageBill + finances.staffWages + finances.operationalCosts,
                profitLoss = finances.revenue - (finances.wageBill + finances.staffWages + finances.operationalCosts)
            )

            financesDao.insert(updatedFinances)
        }
    }

    private fun determineFinancialTier(team: TeamsEntity, league: LeaguesEntity?): FinancialTier {
        return when {
            team.name.contains("Al Ahly") || team.name.contains("Zamalek SC") ||
                    team.name.contains("Pyramids FC") || team.name.contains("Wydad Athletic Club") ||
                    team.name.contains("Espérance") || team.name.contains("RSB Berkane") ||
                    team.name.contains("Mamelodi Sundowns") -> FinancialTier.RICH
            (team.reputation >= 75 && league?.level == 1) ||
                    team.name.contains("Young Africans") || team.name.contains("Simba SC") ||
                    team.name.contains("Kaizer Chiefs") || team.name.contains("Orlando Pirates") ||
                    team.name.contains("Enyimba FC") || team.name.contains("TP Mazembe") ||
                    team.name.contains("AS Vita Club") -> FinancialTier.UPPER_MIDDLE
            league?.level == 1 && team.reputation >= 50 -> FinancialTier.MIDDLE
            league?.level == 2 || (league?.level == 1 && team.reputation < 50) -> FinancialTier.LOWER
            else -> FinancialTier.POOR
        }
    }

    private fun calculateBaseRevenue(team: TeamsEntity, league: LeaguesEntity?, tier: FinancialTier): Long {
        return when (tier) {
            FinancialTier.RICH -> Random.nextLong(10_000_000, 30_000_000)
            FinancialTier.UPPER_MIDDLE -> Random.nextLong(3_000_000, 10_000_000)
            FinancialTier.MIDDLE -> Random.nextLong(800_000, 3_000_000)
            FinancialTier.LOWER -> Random.nextLong(200_000, 800_000)
            FinancialTier.POOR -> Random.nextLong(20_000, 200_000)
        }
    }

    private fun calculateBaseBudget(revenue: Long, reputation: Int): Long {
        val budgetPercentage = 0.6 + (reputation / 500.0)
        return (revenue * budgetPercentage).toLong()
    }

    private fun calculateSponsorshipRevenue(team: TeamsEntity, league: LeaguesEntity?, tier: FinancialTier): Long {
        return when (tier) {
            FinancialTier.RICH -> Random.nextLong(10_000_000, 30_000_000)
            FinancialTier.UPPER_MIDDLE -> Random.nextLong(5_000_000, 12_000_000)
            FinancialTier.MIDDLE -> Random.nextLong(2_000_000, 6_000_000)
            FinancialTier.LOWER -> Random.nextLong(500_000, 2_000_000)
            FinancialTier.POOR -> Random.nextLong(100_000, 500_000)
        }
    }

    private fun calculateBroadcastingRevenue(league: LeaguesEntity?, tier: FinancialTier): Long {
        return when (tier) {
            FinancialTier.RICH -> Random.nextLong(8_000_000, 20_000_000)
            FinancialTier.UPPER_MIDDLE -> Random.nextLong(3_000_000, 8_000_000)
            FinancialTier.MIDDLE -> Random.nextLong(1_000_000, 3_000_000)
            FinancialTier.LOWER -> Random.nextLong(300_000, 1_000_000)
            FinancialTier.POOR -> Random.nextLong(50_000, 300_000)
        }
    }

    private suspend fun calculateMatchdayRevenue(team: TeamsEntity, league: LeaguesEntity?, tier: FinancialTier): Long {
        val stadiumCapacity = team.stadiumCapacity
        val fanLoyalty = team.fanLoyalty
        
        // Infrastructure impact: Stadium and Fan Zone levels
        val stadiumLevel = infrastructureUpgradesDao.getLatestUpgradeByType(team.id, "STADIUM")?.upgradeLevel ?: 1
        val fanZoneLevel = infrastructureUpgradesDao.getLatestUpgradeByType(team.id, "FAN_ZONE")?.upgradeLevel ?: 1
        
        // African Context: Multi-functional stadium use and matchday stalls/markets (Fan Zone)
        val infraMultiplier = 1.0 + (stadiumLevel * 0.15) + (fanZoneLevel * 0.25)
        
        val averageTicketPrice = when (tier) {
            FinancialTier.RICH -> 50
            FinancialTier.UPPER_MIDDLE -> 30
            FinancialTier.MIDDLE -> 20
            FinancialTier.LOWER -> 10
            FinancialTier.POOR -> 5
        }
        val homeMatches = 19 
        val averageAttendance = (stadiumCapacity * fanLoyalty / 100.0).toInt()
        
        return (averageAttendance * averageTicketPrice * homeMatches * infraMultiplier).toLong()
    }

    private fun calculateMerchandiseRevenue(team: TeamsEntity, tier: FinancialTier): Long {
        val fanLoyalty = team.fanLoyalty
        return when (tier) {
            FinancialTier.RICH -> (fanLoyalty * 200_000).toLong()
            FinancialTier.UPPER_MIDDLE -> (fanLoyalty * 100_000).toLong()
            FinancialTier.MIDDLE -> (fanLoyalty * 50_000).toLong()
            FinancialTier.LOWER -> (fanLoyalty * 20_000).toLong()
            FinancialTier.POOR -> (fanLoyalty * 5_000).toLong()
        }
    }

    private fun calculateMembershipRevenue(team: TeamsEntity, tier: FinancialTier): Long {
        val fanLoyalty = team.fanLoyalty
        return when (tier) {
            FinancialTier.RICH -> (fanLoyalty * 50_000).toLong()
            FinancialTier.UPPER_MIDDLE -> (fanLoyalty * 25_000).toLong()
            FinancialTier.MIDDLE -> (fanLoyalty * 10_000).toLong()
            FinancialTier.LOWER -> (fanLoyalty * 5_000).toLong()
            FinancialTier.POOR -> (fanLoyalty * 1_000).toLong()
        }
    }

    private fun calculateWageBill(team: TeamsEntity, league: LeaguesEntity?, tier: FinancialTier): Long {
        val squadSize = 25
        val averageWage = when (tier) {
            FinancialTier.RICH -> Random.nextLong(500_000, 2_500_000)
            FinancialTier.UPPER_MIDDLE -> Random.nextLong(200_000, 500_000)
            FinancialTier.MIDDLE -> Random.nextLong(80_000, 200_000)
            FinancialTier.LOWER -> Random.nextLong(30_000, 80_000)
            FinancialTier.POOR -> Random.nextLong(10_000, 30_000)
        }
        return averageWage * squadSize
    }

    private fun calculateStaffWages(team: TeamsEntity, tier: FinancialTier): Long {
        val staffSize = when (tier) {
            FinancialTier.RICH -> 30
            FinancialTier.UPPER_MIDDLE -> 20
            FinancialTier.MIDDLE -> 15
            FinancialTier.LOWER -> 10
            FinancialTier.POOR -> 5
        }
        val averageStaffWage = when (tier) {
            FinancialTier.RICH -> 100_000
            FinancialTier.UPPER_MIDDLE -> 60_000
            FinancialTier.MIDDLE -> 35_000
            FinancialTier.LOWER -> 20_000
            FinancialTier.POOR -> 10_000
        }
        return (averageStaffWage * staffSize).toLong()
    }

    private fun calculateOperationalCosts(tier: FinancialTier): Long {
        return when (tier) {
            FinancialTier.RICH -> Random.nextLong(5_000_000, 10_000_000)
            FinancialTier.UPPER_MIDDLE -> Random.nextLong(2_000_000, 5_000_000)
            FinancialTier.MIDDLE -> Random.nextLong(1_000_000, 2_000_000)
            FinancialTier.LOWER -> Random.nextLong(300_000, 1_000_000)
            FinancialTier.POOR -> Random.nextLong(100_000, 300_000)
        }
    }

    // ============ REVENUE UPDATES ============

    suspend fun addRevenue(teamId: Int, season: String, amount: Double, source: String) {
        when (source) {
            "Gate Receipts" -> financesDao.addMatchdayRevenue(teamId, season, amount.toLong())
            "Sponsorship" -> financesDao.addSponsorshipRevenue(teamId, season, amount.toLong())
            "Broadcasting" -> financesDao.addBroadcastingRevenue(teamId, season, amount.toLong())
            "Prize Money" -> financesDao.addPrizeMoney(teamId, season, amount.toLong())
            "Other" -> financesDao.addOtherRevenue(teamId, season, amount.toLong())
            else -> financesDao.addSponsorshipRevenue(teamId, season, amount.toLong())
        }
        updateFinancialTier(teamId, season)
    }

    suspend fun addOtherRevenue(teamId: Int, season: String, amount: Long) {
        financesDao.addOtherRevenue(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addSponsorshipRevenue(teamId: Int, season: String, amount: Long) {
        financesDao.addSponsorshipRevenue(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addBroadcastingRevenue(teamId: Int, season: String, amount: Long) {
        financesDao.addBroadcastingRevenue(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addMatchdayRevenue(teamId: Int, season: String, amount: Long) {
        financesDao.addMatchdayRevenue(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addPrizeMoney(teamId: Int, season: String, amount: Long) {
        financesDao.addPrizeMoney(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addContinentalPrizeMoney(teamId: Int, season: String, amount: Long) {
        financesDao.addContinentalPrizeMoney(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addPlayerSale(teamId: Int, season: String, amount: Long) {
        financesDao.addPlayerSale(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    // ============ EXPENSE UPDATES ============

    suspend fun addWages(teamId: Int, season: String, amount: Double) {
        financesDao.addWages(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addStaffWages(teamId: Int, season: String, amount: Long) {
        financesDao.addStaffWages(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addTransferSpending(teamId: Int, season: String, amount: Long) {
        financesDao.addTransferSpending(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    suspend fun addInfrastructureCost(teamId: Int, season: String, amount: Long) {
        financesDao.addInfrastructureCost(teamId, season, amount)
        updateFinancialTier(teamId, season)
    }

    // ============ CHAIRMAN IMPACT ============

    /**
     * Applies financial boosts based on the chairman's personality and attributes.
     * Wealthy chairmen may inject direct funds, while business-savvy ones boost sponsorships.
     */
    suspend fun processChairmanCommercialBoost(teamId: Int, season: String) {
        val chairman = chairmanRepository.getChairmanByTeam(teamId) ?: return

        // 1. Direct Investment (Wealth Level Impact)
        // High wealth chairmen have a chance to inject capital if bank balance is low
        val currentFinances = financesDao.getByTeamAndSeason(teamId, season)
        if (chairman.wealthLevel > 70 && (currentFinances?.bankBalance ?: 0) < 5_000_000) {
            val investmentChance = if (chairman.wealthLevel > 90) 0.15 else 0.05
            if (Random.nextDouble() < investmentChance) {
                val amount = (chairman.wealthLevel * 50_000L) * (chairman.ambitionLevel / 50)
                addOtherRevenue(teamId, season, amount.toLong())
            }
        }

        // 2. Commercial/Sponsorship Boost (Business Skill Impact)
        // Highly skilled business chairmen attract better commercial deals
        if (chairman.businessSkill > 60) {
            val boostBase = when {
                chairman.businessSkill > 90 -> 25_000L
                chairman.businessSkill > 80 -> 15_000L
                else -> 5_000L
            }
            val totalBoost = boostBase * (chairman.businessSkill / 50)
            addSponsorshipRevenue(teamId, season, totalBoost.toLong())
        }
    }

    // ============ BUDGET MANAGEMENT ============

    suspend fun updateTransferBudget(teamId: Int, season: String, newBudget: Long) {
        financesDao.updateBudget(teamId, season, newBudget)
    }

    suspend fun canAffordTransfer(teamId: Int, season: String, fee: Long, wages: Long): Boolean {
        val finances = financesDao.getByTeamAndSeason(teamId, season) ?: return false
        return finances.budget >= fee && (finances.bankBalance >= fee + (wages * 12))
    }

    // ============ FINANCIAL FAIR PLAY (FFP) ============

    suspend fun getFFPStatus(teamId: Int, season: String): FFPStatus {
        val finances = financesDao.getByTeamAndSeason(teamId, season) ?: return FFPStatus.empty()
        
        // FFP Logic: Infrastructure & Youth costs are often exempt to encourage growth
        val ffpRelevantExpenses = finances.expenses - finances.infrastructureCosts
        val ffpProfitLoss = finances.revenue - ffpRelevantExpenses
        
        // Allowed loss threshold (e.g., $5M)
        val threshold = 5_000_000L
        val isCompliant = ffpProfitLoss >= -threshold
        
        return FFPStatus(
            isCompliant = isCompliant,
            ffpProfitLoss = ffpProfitLoss,
            allowedLoss = threshold,
            infrastructureExemptions = finances.infrastructureCosts,
            wageTurnoverRatio = if (finances.revenue > 0) (finances.wageBill.toDouble() / finances.revenue) else 0.0
        )
    }

    // ============ FINANCIAL TIER UPDATE ============

    private suspend fun updateFinancialTier(teamId: Int, season: String) {
        val finances = financesDao.getByTeamAndSeason(teamId, season) ?: return
        val newTier = when {
            finances.revenue >= 50_000_000 -> FinancialTier.RICH
            finances.revenue >= 20_000_000 -> FinancialTier.UPPER_MIDDLE
            finances.revenue >= 8_000_000 -> FinancialTier.MIDDLE
            finances.revenue >= 2_000_000 -> FinancialTier.LOWER
            else -> FinancialTier.POOR
        }
        financesDao.update(finances.copy(financialTier = newTier.value))
    }

    // ============ END OF SEASON PROCESSING ============

    suspend fun processEndOfSeason(oldSeason: String, newSeason: String) {
        val teams = teamsDao.getAll().firstOrNull() ?: return
        for (team in teams) {
            val oldFinances = financesDao.getByTeamAndSeason(team.id, oldSeason) ?: continue
            val finalProfitLoss = oldFinances.revenue - oldFinances.expenses
            val updatedOld = oldFinances.copy(
                profitLoss = finalProfitLoss,
                bankBalance = oldFinances.bankBalance + finalProfitLoss
            )
            financesDao.update(updatedOld)
            val league = leaguesDao.getLeagueByName(team.league)
            val tier = determineFinancialTier(team, league)
            val newFinances = FinancesEntity(
                teamId = team.id, teamName = team.name, season = newSeason,
                revenue = calculateBaseRevenue(team, league, tier),
                bankBalance = (updatedOld.bankBalance * 0.8).toLong(),
                financialTier = tier.value,
                sponsorshipRevenue = calculateSponsorshipRevenue(team, league, tier),
                broadcastingRevenue = calculateBroadcastingRevenue(league, tier),
                matchdayRevenue = calculateMatchdayRevenue(team, league, tier),
                merchandiseRevenue = calculateMerchandiseRevenue(team, tier),
                membershipRevenue = calculateMembershipRevenue(team, tier),
                lastUpdated = getCurrentDate()
            )
            val budget = calculateBaseBudget(newFinances.revenue, team.reputation)
            val finalNew = newFinances.copy(
                budget = budget,
                wageBill = calculateWageBill(team, league, tier),
                staffWages = calculateStaffWages(team, tier),
                operationalCosts = calculateOperationalCosts(tier)
            )
            financesDao.insert(finalNew)
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun getRichestTeams(season: String, limit: Int = 10): Flow<List<FinancesEntity>> =
        financesDao.getRichestTeams(season, limit)

    fun getHighestWageBills(season: String, limit: Int = 10): Flow<List<FinancesEntity>> =
        financesDao.getHighestWageBills(season, limit)

    fun getMostProfitableTeams(season: String, limit: Int = 10): Flow<List<FinancesEntity>> =
        financesDao.getMostProfitableTeams(season, limit)

    fun getTeamsInDebt(season: String): Flow<List<FinancesEntity>> =
        financesDao.getTeamsInDebt(season)

    suspend fun getTeamFinanceDashboard(teamId: Int, season: String): TeamFinanceDashboard {
        val finances = financesDao.getByTeamAndSeason(teamId, season) ?: return TeamFinanceDashboard.empty()
        val ffpStatus = getFFPStatus(teamId, season)
        
        val revenueBreakdown = mapOf(
            "Sponsorship" to finances.sponsorshipRevenue,
            "Broadcasting" to finances.broadcastingRevenue,
            "Matchday" to finances.matchdayRevenue,
            "Merchandise" to finances.merchandiseRevenue,
            "Prize Money" to (finances.prizeMoney + finances.continentalPrizeMoney),
            "Player Sales" to finances.playerSales,
            "Membership" to finances.membershipRevenue
        )
        val expenseBreakdown = mapOf(
            "Player Wages" to finances.wageBill,
            "Staff Wages" to finances.staffWages,
            "Transfer Spending" to finances.transferSpending,
            "Infrastructure" to finances.infrastructureCosts,
            "Operational" to finances.operationalCosts
        )
        return TeamFinanceDashboard(
            teamId = teamId, teamName = finances.teamName, season = season,
            revenue = finances.revenue, expenses = finances.expenses, profitLoss = finances.profitLoss,
            budget = finances.budget, bankBalance = finances.bankBalance,
            financialTier = finances.financialTier ?: "Unknown",
            financialHealth = finances.financialHealth,
            revenueBreakdown = revenueBreakdown.filter { it.value > 0 },
            expenseBreakdown = expenseBreakdown.filter { it.value > 0 },
            isProfitable = finances.isProfitable,
            ffpStatus = ffpStatus
        )
    }
}

// ============ DATA CLASSES ============

data class FFPStatus(
    val isCompliant: Boolean,
    val ffpProfitLoss: Long,
    val allowedLoss: Long,
    val infrastructureExemptions: Long,
    val wageTurnoverRatio: Double
) {
    companion object {
        fun empty() = FFPStatus(true, 0, 0, 0, 0.0)
    }
}

data class TeamFinanceDashboard(
    val teamId: Int, val teamName: String, val season: String,
    val revenue: Long, val expenses: Long, val profitLoss: Long,
    val budget: Long, val bankBalance: Long, val financialTier: String,
    val financialHealth: String, val revenueBreakdown: Map<String, Long>,
    val expenseBreakdown: Map<String, Long>, val isProfitable: Boolean,
    val ffpStatus: FFPStatus = FFPStatus.empty()
) {
    companion object {
        fun empty(): TeamFinanceDashboard = TeamFinanceDashboard(
            teamId = 0, teamName = "", season = "",
            revenue = 0, expenses = 0, profitLoss = 0, budget = 0, bankBalance = 0,
            financialTier = "", financialHealth = "", revenueBreakdown = emptyMap(),
            expenseBreakdown = emptyMap(), isProfitable = false, ffpStatus = FFPStatus.empty()
        )
    }
}
