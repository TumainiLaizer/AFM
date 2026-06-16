package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.FanExpectationsDao
import com.fameafrica.afm.data.database.entities.FanExpectationsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FanExpectationsRepository @Inject constructor(
    private val fanExpectationsDaoProvider: Provider<FanExpectationsDao>
) {
    private val fanExpectationsDao get() = fanExpectationsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllExpectations(): Flow<List<FanExpectationsEntity>> = fanExpectationsDao.getAll()

    suspend fun getExpectationsByTeam(teamName: String): FanExpectationsEntity? =
        fanExpectationsDao.getByTeamName(teamName)

    suspend fun getExpectationsByTeamId(teamId: Int): FanExpectationsEntity? =
        fanExpectationsDao.getByTeamId(teamId)

    suspend fun insertExpectations(expectations: FanExpectationsEntity) =
        fanExpectationsDao.insert(expectations)

    suspend fun updateExpectations(expectations: FanExpectationsEntity) =
        fanExpectationsDao.update(expectations)

    // ============ CONFIDENCE MANAGEMENT ============

    suspend fun updateConfidenceLevel(teamName: String, newConfidence: Int) {
        val expectations = fanExpectationsDao.getByTeamName(teamName)
        expectations?.let {
            val updated = it.copy(confidenceLevel = newConfidence.coerceIn(0, 100))
            fanExpectationsDao.update(updated)
        }
    }

    suspend fun adjustConfidenceLevel(teamName: String, adjustment: Int) {
        val expectations = fanExpectationsDao.getByTeamName(teamName)
        expectations?.let {
            val newConfidence = (expectations.confidenceLevel + adjustment).coerceIn(0, 100)
            val updated = it.copy(confidenceLevel = newConfidence)
            fanExpectationsDao.update(updated)
        }
    }

    // ============ BOARD TRUST MANAGEMENT ============

    suspend fun updateBoardTrust(teamName: String, newTrust: Int) {
        val expectations = fanExpectationsDao.getByTeamName(teamName)
        expectations?.let {
            val updated = it.copy(boardTrust = newTrust.coerceIn(0, 100))
            fanExpectationsDao.update(updated)
        }
    }

    suspend fun adjustBoardTrust(teamName: String, adjustment: Int) {
        val expectations = fanExpectationsDao.getByTeamName(teamName)
        expectations?.let {
            val newTrust = (expectations.boardTrust + adjustment).coerceIn(0, 100)
            val updated = it.copy(boardTrust = newTrust)
            fanExpectationsDao.update(updated)
        }
    }

    // ============ RECENT PERFORMANCE ============

    suspend fun updateRecentPerformance(teamName: String, performanceJson: String) {
        val expectations = fanExpectationsDao.getByTeamName(teamName)
        expectations?.let {
            val updated = it.copy(recentPerformance = performanceJson)
            fanExpectationsDao.update(updated)
        }
    }

    // ============ INITIALIZATION ============

    suspend fun initializeFanExpectations(teamId: Int, teamName: String): FanExpectationsEntity {
        val existing = fanExpectationsDao.getByTeamId(teamId)

        return if (existing != null) {
            existing
        } else {
            val expectations = FanExpectationsEntity(
                teamId = teamId,
                teamName = teamName,
                confidenceLevel = 50,
                boardTrust = 50
            )
            fanExpectationsDao.insert(expectations)
            expectations
        }
    }

    // ============ DASHBOARD ============

    suspend fun getFanExpectationsDashboard(teamName: String): FanExpectationsDashboard {
        val expectations = fanExpectationsDao.getByTeamName(teamName)
            ?: return FanExpectationsDashboard.empty()

        return FanExpectationsDashboard(
            teamName = expectations.teamName,
            confidenceLevel = expectations.confidenceLevel,
            confidenceLevelString = expectations.confidenceLevelString,
            boardTrust = expectations.boardTrust,
            trustLevelString = expectations.trustLevelString,
            overallMood = expectations.overallMood,
            isPositive = expectations.isPositive,
            isNegative = expectations.isNegative,
            isCritical = expectations.isCritical
        )
    }
}

// ============ DATA CLASSES ============

data class FanMoodDistribution(
    val mood: String,
    val teamCount: Int
)

data class FanExpectationsDashboard(
    val teamName: String,
    val confidenceLevel: Int,
    val confidenceLevelString: String,
    val boardTrust: Int,
    val trustLevelString: String,
    val overallMood: String,
    val isPositive: Boolean,
    val isNegative: Boolean,
    val isCritical: Boolean
) {
    companion object {
        fun empty(): FanExpectationsDashboard = FanExpectationsDashboard(
            teamName = "",
            confidenceLevel = 0,
            confidenceLevelString = "",
            boardTrust = 0,
            trustLevelString = "",
            overallMood = "",
            isPositive = false,
            isNegative = false,
            isCritical = false
        )
    }
}
