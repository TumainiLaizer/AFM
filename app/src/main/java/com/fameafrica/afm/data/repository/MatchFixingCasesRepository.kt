package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.LeagueLevelFixingStats
import com.fameafrica.afm.data.database.dao.LowerLeagueFixingStatistics
import com.fameafrica.afm.data.database.dao.MatchFixingCasesDao
import com.fameafrica.afm.data.database.dao.TeamFixingStats
import com.fameafrica.afm.data.database.entities.MatchFixingCasesEntity
import com.fameafrica.afm.data.database.entities.MatchFixingStatus
import com.fameafrica.afm.data.database.entities.VerdictType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class MatchFixingCasesRepository @Inject constructor(
    private val matchFixingCasesDaoProvider: Provider<MatchFixingCasesDao>,
    private val teamsRepository: TeamsRepository
) {
    private val matchFixingCasesDao get() = matchFixingCasesDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllCases(): Flow<List<MatchFixingCasesEntity>> = matchFixingCasesDao.getAll()

    suspend fun getCaseById(id: Int): MatchFixingCasesEntity? = matchFixingCasesDao.getById(id)

    suspend fun insertCase(case: MatchFixingCasesEntity) = matchFixingCasesDao.insert(case)

    suspend fun updateCase(case: MatchFixingCasesEntity) = matchFixingCasesDao.update(case)

    suspend fun deleteCase(case: MatchFixingCasesEntity) = matchFixingCasesDao.delete(case)

    // ============ LEAGUE LEVEL RESTRICTED QUERIES ============
    // MATCH FIXING CASES ONLY OCCUR IN LEVELS 4 & 5 (LOWER LEAGUES)

    fun getLowerLeagueCases(): Flow<List<MatchFixingCasesEntity>> =
        matchFixingCasesDao.getLowerLeagueCases()

    fun getLevel4Cases(): Flow<List<MatchFixingCasesEntity>> =
        matchFixingCasesDao.getLevel4Cases()

    fun getLevel5Cases(): Flow<List<MatchFixingCasesEntity>> =
        matchFixingCasesDao.getLevel5Cases()

    fun getUpperLeagueCases(): Flow<List<MatchFixingCasesEntity>> =
        matchFixingCasesDao.getUpperLeagueCases()  // Should always be empty

    // ============ CASE CREATION ============
    // ONLY ALLOWED FOR LEAGUE LEVELS 4 & 5

    suspend fun createMatchFixingCase(
        teamInvolved: String,
        teamInvolvedId: Int,
        managerId: Int?,
        managerName: String,
        leagueName: String,
        leagueLevel: Int,
        season: String,
        allegationDetails: String,
        evidenceDescription: String? = null
    ): MatchFixingCasesEntity? {

        // CRITICAL: Match fixing can ONLY occur in League Levels 4 and 5
        if (leagueLevel !in 4..5) {
            return null  // Higher leagues are too serious for match fixing scandals
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val case = MatchFixingCasesEntity(
            teamInvolved = teamInvolved,
            teamInvolvedId = teamInvolvedId,
            managerId = managerId,
            managerName = managerName,
            leagueName = leagueName,
            leagueLevel = leagueLevel,
            season = season,
            allegationDate = currentDate,
            allegationDetails = allegationDetails,
            evidenceDescription = evidenceDescription,
            status = MatchFixingStatus.INVESTIGATING.value,
            isInvestigationComplete = false,
            investigationFindings = ""
        )

        matchFixingCasesDao.insert(case)
        return case
    }

    // ============ INVESTIGATION MANAGEMENT ============

    suspend fun updateInvestigationFindings(
        caseId: Int,
        findings: String,
        isComplete: Boolean = false
    ): Boolean {
        val case = matchFixingCasesDao.getById(caseId) ?: return false

        val updatedCase = case.copy(
            investigationFindings = findings,
            isInvestigationComplete = isComplete
        )

        matchFixingCasesDao.update(updatedCase)
        return true
    }

    suspend fun completeInvestigation(
        caseId: Int,
        verdict: String,
        punishment: String? = null,
        pointsDeducted: Int? = null,
        fineAmount: Int? = null,
        managerBanned: Boolean = false,
        managerBanDuration: String? = null
    ): Boolean {
        val case = matchFixingCasesDao.getById(caseId) ?: return false

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val resolutionDate = dateFormat.format(Date())

        val status = when (verdict) {
            VerdictType.GUILTY.value -> MatchFixingStatus.PROVEN.value
            VerdictType.NOT_GUILTY.value -> MatchFixingStatus.NOT_PROVEN.value
            else -> MatchFixingStatus.CLOSED.value
        }

        val updatedCase = case.copy(
            status = status,
            verdict = verdict,
            punishment = punishment,
            pointsDeducted = pointsDeducted,
            fineAmount = fineAmount,
            managerBanned = managerBanned,
            managerBanDuration = managerBanDuration,
            isInvestigationComplete = true,
            resolutionDate = resolutionDate
        )

        matchFixingCasesDao.update(updatedCase)
        return true
    }

    suspend fun closeCaseWithoutVerdict(
        caseId: Int,
        reason: String
    ): Boolean {
        val case = matchFixingCasesDao.getById(caseId) ?: return false

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val resolutionDate = dateFormat.format(Date())

        val updatedCase = case.copy(
            status = MatchFixingStatus.CLOSED.value,
            investigationFindings = reason,
            isInvestigationComplete = true,
            resolutionDate = resolutionDate
        )

        matchFixingCasesDao.update(updatedCase)
        return true
    }

    // ============ LEAGUE LEVEL VERIFICATION ============

    suspend fun isLeagueLevelEligibleForMatchFixing(leagueLevel: Int): Boolean {
        return leagueLevel == 4 || leagueLevel == 5
    }

    suspend fun getCaseCountByLeagueLevel(leagueLevel: Int): Int {
        return when (leagueLevel) {
            4 -> matchFixingCasesDao.getLevel4Cases().firstOrNull()?.size ?: 0
            5 -> matchFixingCasesDao.getLevel5Cases().firstOrNull()?.size ?: 0
            else -> 0
        }
    }

    // ============ STATISTICS ============

    suspend fun getLowerLeagueStatistics(): LowerLeagueFixingStatistics? =
        matchFixingCasesDao.getLowerLeagueStatistics()

    fun getLeagueLevelStatistics(): Flow<List<LeagueLevelFixingStats>> =
        matchFixingCasesDao.getLeagueLevelStatistics()

    fun getMostFrequentlyGuiltyTeams(limit: Int): Flow<List<TeamFixingStats>> =
        matchFixingCasesDao.getMostFrequentlyGuiltyTeams(limit)

    // ============ SEASONAL ============

    fun getCasesBySeason(season: String): Flow<List<MatchFixingCasesEntity>> =
        matchFixingCasesDao.getCasesBySeason(season)

    fun getSeasonsWithCases(): Flow<List<String>> =
        matchFixingCasesDao.getSeasonsWithCases()

    // ============ DASHBOARD ============

    suspend fun getMatchFixingDashboard(): MatchFixingDashboard {
        val lowerLeagueCases = matchFixingCasesDao.getLowerLeagueCases().firstOrNull() ?: emptyList()
        val level4Cases = lowerLeagueCases.filter { it.leagueLevel == 4 }
        val level5Cases = lowerLeagueCases.filter { it.leagueLevel == 5 }

        val activeInvestigations = lowerLeagueCases.filter { it.status == MatchFixingStatus.INVESTIGATING.value }
        val provenCases = lowerLeagueCases.filter { it.status == MatchFixingStatus.PROVEN.value }
        val totalPointsDeducted = provenCases.sumOf { it.pointsDeducted ?: 0 }
        val totalFines = provenCases.sumOf { it.fineAmount ?: 0 }

        val recentCases = lowerLeagueCases.sortedByDescending { it.allegationDate }.take(10)

        return MatchFixingDashboard(
            totalCases = lowerLeagueCases.size,
            level4Cases = level4Cases.size,
            level5Cases = level5Cases.size,
            activeInvestigations = activeInvestigations.size,
            provenCases = provenCases.size,
            totalPointsDeducted = totalPointsDeducted,
            totalFines = totalFines,
            recentCases = recentCases,
            isUpperLeagueClean = matchFixingCasesDao.getUpperLeagueCases().firstOrNull()?.isEmpty() ?: true
        )
    }
}

// ============ DATA CLASSES ============

data class MatchFixingDashboard(
    val totalCases: Int,
    val level4Cases: Int,
    val level5Cases: Int,
    val activeInvestigations: Int,
    val provenCases: Int,
    val totalPointsDeducted: Int,
    val totalFines: Int,
    val recentCases: List<MatchFixingCasesEntity>,
    val isUpperLeagueClean: Boolean
)