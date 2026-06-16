package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.data.repository.FinancesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialEngine @Inject constructor(
    private val financesRepository: FinancesRepository
) {

    /**
     * Calculates weekly income and expenses for a team.
     * Returns a map of transaction types to amounts.
     */
    fun calculateWeeklyFinancials(
        team: TeamsEntity,
        fanHappiness: Int,
        ticketPrice: Int,
        merchandisePrice: Int
    ): FinancialReport {
        // 1. Ticket Sales (Matchday Revenue)
        // Only if it's a home game week, but for AFC we can average it out or trigger on matchday
        val attendance = (team.stadiumCapacity * (fanHappiness / 100.0) * (1.0 - (ticketPrice / 100.0))).toInt()
            .coerceIn(0, team.stadiumCapacity)
        val matchdayRevenue = attendance * ticketPrice.toLong()

        // 2. Merchandise Sales
        val merchRevenue = (attendance * 0.2 * merchandisePrice).toLong()

        // 3. Wage Deductions
        val totalWages = team.wageBudget ?: 0L

        // 4. Facility Maintenance
        val maintenanceCost = (team.stadiumCapacity * 0.5).toLong()

        return FinancialReport(
            income = matchdayRevenue + merchRevenue,
            expenses = totalWages + maintenanceCost,
            breakdown = mapOf(
                "Tickets" to matchdayRevenue,
                "Merchandise" to merchRevenue,
                "Wages" to -totalWages,
                "Maintenance" to -maintenanceCost
            )
        )
    }

    data class FinancialReport(
        val income: Long,
        val expenses: Long,
        val breakdown: Map<String, Long>
    ) {
        val profitOrLoss: Long get() = income - expenses
    }
}
