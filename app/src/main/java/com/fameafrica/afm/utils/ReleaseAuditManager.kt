package com.fameafrica.afm.utils

import android.util.Log
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates the game state and data integrity before a production release or final build.
 * Checks for placeholder values, missing critical data, and system readiness.
 */
@Singleton
class ReleaseAuditManager @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val leaguesRepository: LeaguesRepository,
    private val gameManager: GameManager
) {

    data class AuditResult(
        val isReady: Boolean,
        val issues: List<String>,
        val warnings: List<String>
    )

    suspend fun performReleaseAudit(): AuditResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 1. Data Integrity Checks
        val teamCount = teamsRepository.getTotalTeamCount()
        if (teamCount == 0) {
            issues.add("CRITICAL: No teams found in the database.")
        } else if (teamCount < 100) {
            warnings.add("LOW DATA: Total team count ($teamCount) is unusually low for production.")
        }

        val playerCount = playersRepository.getPlayersCount()
        if (playerCount == 0) {
            issues.add("CRITICAL: No players found in the database.")
        }

        // 2. League Readiness
        val leagues = leaguesRepository.getAllLeaguesSync()
        if (leagues.isEmpty()) {
            issues.add("CRITICAL: No leagues configured.")
        } else {
            leagues.forEach { league ->
                val teamsInLeague = teamsRepository.getTeamsByLeagueSync(league.name)
                if (teamsInLeague.isEmpty()) {
                    issues.add("CONFIG ERROR: League '${league.name}' has 0 teams assigned.")
                }
            }
        }

        // 3. System Readiness
        val gameState = gameManager.gameState.value
        if (gameState is GameManager.GameState.Error) {
            issues.add("SYSTEM ERROR: GameManager is in Error state: ${gameState.message}")
        }

        val isReady = issues.isEmpty()
        
        Log.d("AFM_AUDIT", "Audit Completed. Ready: $isReady. Issues: ${issues.size}, Warnings: ${warnings.size}")
        
        return AuditResult(isReady, issues, warnings)
    }
}
