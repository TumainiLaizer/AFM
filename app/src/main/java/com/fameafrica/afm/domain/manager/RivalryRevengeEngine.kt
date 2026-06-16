package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.repository.FixturesResultsRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RivalryRevengeEngine @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val fixturesResultsRepository: FixturesResultsRepository
) {

    data class RivalryContext(
        val isDerby: Boolean,
        val revengeFactor: Int, // 0-100
        val historicalSummary: String,
        val pressureLevel: Int // 0-100
    )

    /**
     * Analyzes the historical context before a match against a rival.
     */
    suspend fun getMatchContext(homeTeamId: Int, awayTeamId: Int): RivalryContext {
        val homeTeam = teamsRepository.getTeamById(homeTeamId)
        val awayTeam = teamsRepository.getTeamById(awayTeamId)
        
        val isRivalry = homeTeam?.rivalTeamId == awayTeamId || awayTeam?.rivalTeamId == homeTeamId
        if (!isRivalry) return RivalryContext(false, 0, "", 0)
        
        // Fetch last 3 results between these teams
        val history = fixturesResultsRepository.getAllResults().firstOrNull()?.filter {
            (it.homeTeamId == homeTeamId && it.awayTeamId == awayTeamId) ||
            (it.homeTeamId == awayTeamId && it.awayTeamId == homeTeamId)
        }?.takeLast(3) ?: emptyList()
        
        var revengeFactor = 0
        val lastResult = history.lastOrNull()
        
        if (lastResult != null) {
            val userLostLast = (lastResult.homeTeamId == homeTeamId && lastResult.awayTeamWin) ||
                             (lastResult.awayTeamId == homeTeamId && lastResult.homeTeamWin)
            if (userLostLast) revengeFactor = 70
        }

        val historicalSummary = when {
            history.isEmpty() -> "First encounter of the season."
            history.size == 1 -> "Last time out, ${lastResult?.winner} took the spoils."
            else -> "A long-standing rivalry with intense history."
        }

        return RivalryContext(
            isDerby = true,
            revengeFactor = revengeFactor,
            historicalSummary = historicalSummary,
            pressureLevel = 85 // Derbies always high pressure
        )
    }
}
