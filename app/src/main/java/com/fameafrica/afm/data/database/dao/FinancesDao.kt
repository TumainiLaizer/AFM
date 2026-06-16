package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.FinancesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM finances ORDER BY season DESC")
    fun getAll(): Flow<List<FinancesEntity>>

    @Query("SELECT * FROM finances WHERE id = :id")
    suspend fun getById(id: Int): FinancesEntity?

    @Query("SELECT * FROM finances WHERE team_id = :teamId AND season = :season")
    suspend fun getByTeamAndSeason(teamId: Int, season: String): FinancesEntity?

    @Query("SELECT * FROM finances WHERE team_id = :teamId AND season = :season")
    fun getTeamFinancesFlow(teamId: Int, season: String): Flow<FinancesEntity?>

    @Query("SELECT * FROM finances WHERE team_id = :teamId ORDER BY season DESC")
    fun getTeamFinances(teamId: Int): Flow<List<FinancesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(finance: FinancesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(finances: List<FinancesEntity>)

    @Update
    suspend fun update(finance: FinancesEntity)

    @Delete
    suspend fun delete(finance: FinancesEntity)

    @Query("DELETE FROM finances WHERE team_id = :teamId AND season = :season")
    suspend fun deleteByTeamAndSeason(teamId: Int, season: String)

    @Query("SELECT * FROM finances")
    suspend fun getAllStatic(): List<FinancesEntity>

    // ============ REVENUE UPDATES ============

    @Query("UPDATE finances SET sponsorship_revenue = sponsorship_revenue + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addSponsorshipRevenue(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET broadcasting_revenue = broadcasting_revenue + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addBroadcastingRevenue(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET matchday_revenue = matchday_revenue + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addMatchdayRevenue(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET merchandise_revenue = merchandise_revenue + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addMerchandiseRevenue(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET other_revenue = other_revenue + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount, bank_balance = bank_balance + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addOtherRevenue(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET prize_money = prize_money + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addPrizeMoney(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET continental_prize_money = continental_prize_money + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addContinentalPrizeMoney(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET player_sales = player_sales + :amount, revenue = revenue + :amount, profit_loss = profit_loss + :amount, bank_balance = bank_balance + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addPlayerSale(teamId: Int, season: String, amount: Long)

    // ============ EXPENSE UPDATES ============

    @Query("UPDATE finances SET wage_bill = wage_bill + :amount, expenses = expenses + :amount, profit_loss = profit_loss - :amount, bank_balance = bank_balance - :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addWages(teamId: Int, season: String, amount: Double)

    @Query("UPDATE finances SET staff_wages = staff_wages + :amount, expenses = expenses + :amount, profit_loss = profit_loss - :amount, bank_balance = bank_balance - :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addStaffWages(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET transfer_spending = transfer_spending + :amount, expenses = expenses + :amount, profit_loss = profit_loss - :amount, bank_balance = bank_balance - :amount, budget = budget - :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addTransferSpending(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET infrastructure_costs = infrastructure_costs + :amount, expenses = expenses + :amount, profit_loss = profit_loss - :amount, bank_balance = bank_balance - :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addInfrastructureCost(teamId: Int, season: String, amount: Long)

    // ============ BUDGET MANAGEMENT ============

    @Query("UPDATE finances SET budget = :newBudget WHERE team_id = :teamId AND season = :season")
    suspend fun updateBudget(teamId: Int, season: String, newBudget: Long)

    @Query("UPDATE finances SET bank_balance = bank_balance + :amount, profit_loss = profit_loss + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addToBankBalance(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET coins = coins + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addCoins(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET premium_cash = premium_cash + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addPremiumCash(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET sponsorship_income = sponsorship_income + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addSponsorshipIncome(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET ticket_revenue = ticket_revenue + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addTicketRevenue(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET infrastructure_expenses = infrastructure_expenses + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addInfrastructureExpenses(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET wage_expenses = wage_expenses + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addWageExpenses(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET transfer_budget = transfer_budget + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addTransferBudget(teamId: Int, season: String, amount: Long)

    @Query("UPDATE finances SET club_debt = club_debt + :amount WHERE team_id = :teamId AND season = :season")
    suspend fun addClubDebt(teamId: Int, season: String, amount: Long)

    // ============ RANKING QUERIES ============

    @Query("SELECT * FROM finances WHERE season = :season ORDER BY revenue DESC LIMIT :limit")
    fun getRichestTeams(season: String, limit: Int): Flow<List<FinancesEntity>>

    @Query("SELECT * FROM finances WHERE season = :season ORDER BY wage_bill DESC LIMIT :limit")
    fun getHighestWageBills(season: String, limit: Int): Flow<List<FinancesEntity>>

    @Query("SELECT * FROM finances WHERE season = :season ORDER BY profit_loss DESC LIMIT :limit")
    fun getMostProfitableTeams(season: String, limit: Int): Flow<List<FinancesEntity>>

    @Query("SELECT * FROM finances WHERE season = :season AND debt > 0 ORDER BY debt DESC")
    fun getTeamsInDebt(season: String): Flow<List<FinancesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            AVG(revenue) as avg_revenue,
            AVG(wage_bill) as avg_wage_bill,
            AVG(profit_loss) as avg_profit,
            SUM(revenue) as total_revenue
        FROM finances 
        WHERE season = :season
    """)
    suspend fun getSeasonAverages(season: String): SeasonFinanceAverages?

    @Query("""
        SELECT 
            financial_tier,
            COUNT(*) as team_count,
            AVG(revenue) as avg_revenue,
            AVG(wage_bill) as avg_wage_bill
        FROM finances 
        WHERE season = :season
        GROUP BY financial_tier
    """)
    fun getFinancialTierDistribution(season: String): Flow<List<FinancialTierStats>>
}

// ============ DATA CLASSES ============

data class SeasonFinanceAverages(
    @ColumnInfo(name = "avg_revenue")
    val averageRevenue: Double,

    @ColumnInfo(name = "avg_wage_bill")
    val averageWageBill: Double,

    @ColumnInfo(name = "avg_profit")
    val averageProfit: Double,

    @ColumnInfo(name = "total_revenue")
    val totalRevenue: Long
)

data class FinancialTierStats(
    @ColumnInfo(name = "financial_tier")
    val financialTier: String,

    @ColumnInfo(name = "team_count")
    val teamCount: Int,

    @ColumnInfo(name = "avg_revenue")
    val averageRevenue: Double,

    @ColumnInfo(name = "avg_wage_bill")
    val averageWageBill: Double
)