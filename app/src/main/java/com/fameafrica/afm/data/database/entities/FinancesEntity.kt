package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "finances",
    indices = [
        Index(value = ["team_id", "season"], unique = true),
        Index(value = ["season"]),
        Index(value = ["financial_tier"])
    ]
)
data class FinancesEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "team_id")
    @field:ColumnInfo(name = "team_id")
    val teamId: Int,

    @param:Json(name = "team_name")
    @field:ColumnInfo(name = "team_name")
    val teamName: String,

    @field:ColumnInfo(name = "season")
    val season: String,

    @field:ColumnInfo(name = "revenue", defaultValue = "0")
    val revenue: Long = 0,

    @field:ColumnInfo(name = "expenses", defaultValue = "0")
    val expenses: Long = 0,

    @field:ColumnInfo(name = "budget", defaultValue = "0")
    val budget: Long = 0,

    @param:Json(name = "profit_loss")
    @field:ColumnInfo(name = "profit_loss", defaultValue = "0")
    val profitLoss: Long = 0,

    @param:Json(name = "bank_balance")
    @field:ColumnInfo(name = "bank_balance", defaultValue = "0")
    val bankBalance: Long = 0,

    @param:Json(name = "sponsorship_revenue")
    @field:ColumnInfo(name = "sponsorship_revenue", defaultValue = "0")
    val sponsorshipRevenue: Long = 0,

    @param:Json(name = "broadcasting_revenue")
    @field:ColumnInfo(name = "broadcasting_revenue", defaultValue = "0")
    val broadcastingRevenue: Long = 0,

    @param:Json(name = "matchday_revenue")
    @field:ColumnInfo(name = "matchday_revenue", defaultValue = "0")
    val matchdayRevenue: Long = 0,

    @param:Json(name = "merchandise_revenue")
    @field:ColumnInfo(name = "merchandise_revenue", defaultValue = "0")
    val merchandiseRevenue: Long = 0,

    @param:Json(name = "prize_money")
    @field:ColumnInfo(name = "prize_money", defaultValue = "0")
    val prizeMoney: Long = 0,

    @param:Json(name = "continental_prize_money")
    @field:ColumnInfo(name = "continental_prize_money", defaultValue = "0")
    val continentalPrizeMoney: Long = 0,

    @param:Json(name = "player_sales")
    @field:ColumnInfo(name = "player_sales", defaultValue = "0")
    val playerSales: Long = 0,

    @param:Json(name = "loan_income")
    @field:ColumnInfo(name = "loan_income", defaultValue = "0")
    val loanIncome: Long = 0,

    @param:Json(name = "membership_revenue")
    @field:ColumnInfo(name = "membership_revenue", defaultValue = "0")
    val membershipRevenue: Long = 0,

    @param:Json(name = "other_revenue")
    @field:ColumnInfo(name = "other_revenue", defaultValue = "0")
    val otherRevenue: Long = 0,

    @param:Json(name = "wage_bill")
    @field:ColumnInfo(name = "wage_bill", defaultValue = "0")
    val wageBill: Long = 0,

    @param:Json(name = "staff_wages")
    @field:ColumnInfo(name = "staff_wages", defaultValue = "0")
    val staffWages: Long = 0,

    @param:Json(name = "transfer_spending")
    @field:ColumnInfo(name = "transfer_spending", defaultValue = "0")
    val transferSpending: Long = 0,

    @param:Json(name = "loan_fees")
    @field:ColumnInfo(name = "loan_fees", defaultValue = "0")
    val loanFees: Long = 0,

    @param:Json(name = "agent_fees")
    @field:ColumnInfo(name = "agent_fees", defaultValue = "0")
    val agentFees: Long = 0,

    @param:Json(name = "infrastructure_costs")
    @field:ColumnInfo(name = "infrastructure_costs", defaultValue = "0")
    val infrastructureCosts: Long = 0,

    @param:Json(name = "youth_academy_costs")
    @field:ColumnInfo(name = "youth_academy_costs", defaultValue = "0")
    val youthAcademyCosts: Long = 0,

    @param:Json(name = "travel_costs")
    @field:ColumnInfo(name = "travel_costs", defaultValue = "0")
    val travelCosts: Long = 0,

    @param:Json(name = "operational_costs")
    @field:ColumnInfo(name = "operational_costs", defaultValue = "0")
    val operationalCosts: Long = 0,

    @field:ColumnInfo(name = "taxes", defaultValue = "0")
    val taxes: Long = 0,

    @param:Json(name = "other_expenses")
    @field:ColumnInfo(name = "other_expenses", defaultValue = "0")
    val otherExpenses: Long = 0,

    @param:Json(name = "financial_tier")
    @field:ColumnInfo(name = "financial_tier")
    val financialTier: String? = null,

    @field:ColumnInfo(name = "debt", defaultValue = "0")
    val debt: Long = 0,

    @param:Json(name = "credit_rating")
    @field:ColumnInfo(name = "credit_rating")
    val creditRating: Int? = null,

    @param:Json(name = "last_updated")
    @field:ColumnInfo(name = "last_updated")
    val lastUpdated: String? = null,

    @field:ColumnInfo(name = "coins", defaultValue = "0")
    val coins: Long = 0,

    @param:Json(name = "premium_cash")
    @field:ColumnInfo(name = "premium_cash", defaultValue = "0")
    val premiumCash: Long = 0,

    @param:Json(name = "sponsorship_income")
    @field:ColumnInfo(name = "sponsorship_income", defaultValue = "0")
    val sponsorshipIncome: Long = 0,

    @param:Json(name = "ticket_revenue")
    @field:ColumnInfo(name = "ticket_revenue", defaultValue = "0")
    val ticketRevenue: Long = 0,

    @param:Json(name = "infrastructure_expenses")
    @field:ColumnInfo(name = "infrastructure_expenses", defaultValue = "0")
    val infrastructureExpenses: Long = 0,

    @param:Json(name = "wage_expenses")
    @field:ColumnInfo(name = "wage_expenses", defaultValue = "0")
    val wageExpenses: Long = 0,

    @param:Json(name = "transfer_budget")
    @field:ColumnInfo(name = "transfer_budget", defaultValue = "0")
    val transferBudget: Long = 0,

    @param:Json(name = "club_debt")
    @field:ColumnInfo(name = "club_debt", defaultValue = "0")
    val clubDebt: Long = 0
) {
    val revenueInMillions: Double get() = revenue / 1_000_000.0
    val expensesInMillions: Double get() = expenses / 1_000_000.0
    val profitLossInMillions: Double get() = profitLoss / 1_000_000.0
    val budgetInMillions: Double get() = budget / 1_000_000.0
    val isProfitable: Boolean get() = profitLoss > 0
    val isInDebt: Boolean get() = debt > 0
    val financialHealth: String get() = when {
        bankBalance > 50_000_000 && debt == 0L -> "Excellent"
        bankBalance > 20_000_000 && debt < 5_000_000 -> "Good"
        bankBalance > 5_000_000 && debt < 10_000_000 -> "Stable"
        bankBalance > 0 && debt < 20_000_000 -> "Fair"
        else -> "Concerning"
    }
}

enum class FinancialTier(val value: String, val minRevenue: Long) {
    RICH("Rich", 50_000_000),
    UPPER_MIDDLE("Upper Middle", 20_000_000),
    MIDDLE("Middle", 8_000_000),
    LOWER("Lower", 2_000_000),
    POOR("Poor", 0)
}

enum class RevenueSource(val value: String) {
    SPONSORSHIP("Sponsorship"),
    BROADCASTING("Broadcasting"),
    MATCHDAY("Matchday"),
    MERCHANDISE("Merchandise"),
    PRIZE_MONEY("Prize Money"),
    CONTINENTAL("Continental"),
    PLAYER_SALES("Player Sales"),
    MEMBERSHIP("Membership")
}
