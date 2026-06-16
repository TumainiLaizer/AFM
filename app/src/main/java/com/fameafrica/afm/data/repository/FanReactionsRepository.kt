package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.FanReactionsDao
import com.fameafrica.afm.data.database.dao.ReactionTypeDistribution
import com.fameafrica.afm.data.database.dao.SentimentDistribution
import com.fameafrica.afm.data.database.dao.TeamReactionStatistics
import com.fameafrica.afm.data.database.entities.FanReactionsEntity
import com.fameafrica.afm.data.database.entities.FanSentiment
import com.fameafrica.afm.data.database.entities.FanReactionType
import com.fameafrica.afm.utils.FanCommentGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FanReactionsRepository @Inject constructor(
    private val fanReactionsDaoProvider: Provider<FanReactionsDao>,
    private val teamsRepository: TeamsRepository
) {

    private val fanReactionsDao: FanReactionsDao?
        get() = try {
            fanReactionsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllReactions(): Flow<List<FanReactionsEntity>> = fanReactionsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getReactionById(id: Int): FanReactionsEntity? = fanReactionsDao?.getById(id)

    suspend fun insertReaction(reaction: FanReactionsEntity) {
        fanReactionsDao?.insert(reaction)
    }

    suspend fun deleteReaction(reaction: FanReactionsEntity) {
        fanReactionsDao?.delete(reaction)
    }

    // ============ TEAM-BASED ============

    fun getReactionsByTeam(teamId: Int): Flow<List<FanReactionsEntity>> =
        fanReactionsDao?.getReactionsByTeam(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getReactionsByTeamAndSentiment(teamId: Int, sentiment: String): Flow<List<FanReactionsEntity>> =
        fanReactionsDao?.getReactionsByTeamAndSentiment(teamId, sentiment) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getPositiveReactionCount(teamId: Int): Int =
        fanReactionsDao?.getPositiveReactionCount(teamId) ?: 0

    suspend fun getNegativeReactionCount(teamId: Int): Int =
        fanReactionsDao?.getNegativeReactionCount(teamId) ?: 0

    // ============ REACTION CREATION ============

    suspend fun addPositiveReaction(
        teamId: Int,
        teamName: String,
        reaction: String = FanReactionType.CHEER.value
    ): FanReactionsEntity {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val reactionEntity = FanReactionsEntity(
            teamId = teamId,
            teamName = teamName,
            reaction = reaction,
            sentiment = FanSentiment.POSITIVE.value,
            timestamp = timestamp
        )

        fanReactionsDao?.insert(reactionEntity)
        return reactionEntity
    }

    suspend fun addNegativeReaction(
        teamId: Int,
        teamName: String,
        reaction: String = FanReactionType.PROTEST.value
    ): FanReactionsEntity {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val reactionEntity = FanReactionsEntity(
            teamId = teamId,
            teamName = teamName,
            reaction = reaction,
            sentiment = FanSentiment.NEGATIVE.value,
            timestamp = timestamp
        )

        fanReactionsDao?.insert(reactionEntity)
        return reactionEntity
    }

    suspend fun addNeutralReaction(
        teamId: Int,
        teamName: String,
        reaction: String = FanReactionType.CHANT.value
    ): FanReactionsEntity {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val reactionEntity = FanReactionsEntity(
            teamId = teamId,
            teamName = teamName,
            reaction = reaction,
            sentiment = FanSentiment.NEUTRAL.value,
            timestamp = timestamp
        )

        fanReactionsDao?.insert(reactionEntity)
        return reactionEntity
    }

    // ============ REACTION GENERATION ============

    suspend fun generateWeeklyFanReactions(teamId: Int, teamName: String, season: String) {
        val rand = Random.nextInt(100)
        when {
            rand < 10 -> addPositiveReaction(teamId, teamName, "Fans are excited about the season progress!")
            rand < 20 -> addNegativeReaction(teamId, teamName, "Supporters are demanding more investment.")
            rand < 30 -> addNeutralReaction(teamId, teamName, "The atmosphere at the stadium is electric.")
        }
    }

    suspend fun generateReactionFromResult(
        teamId: Int,
        teamName: String,
        isWin: Boolean,
        isDraw: Boolean,
        isLoss: Boolean,
        isUpset: Boolean = false,
        bestPlayerName: String? = null
    ): FanReactionsEntity {
        val sentiment = when {
            isWin -> "POSITIVE"
            isLoss -> "NEGATIVE"
            else -> "NEUTRAL"
        }
        val comment = FanCommentGenerator.generateMatchComment(sentiment, bestPlayerName)
        
        return when (sentiment) {
            "POSITIVE" -> addPositiveReaction(teamId, teamName, comment)
            "NEGATIVE" -> addNegativeReaction(teamId, teamName, comment)
            else -> addNeutralReaction(teamId, teamName, comment)
        }
    }

    suspend fun generateReactionFromBoardDecision(
        teamId: Int,
        teamName: String,
        isPositive: Boolean
    ): FanReactionsEntity {
        return if (isPositive) {
            addPositiveReaction(teamId, teamName, "Board Approval")
        } else {
            addNegativeReaction(teamId, teamName, "Board Disapproval")
        }
    }

    // ============ STATISTICS ============

    fun getSentimentDistribution(): Flow<List<SentimentDistribution>> =
        fanReactionsDao?.getSentimentDistribution() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTeamReactionStatistics(): Flow<List<TeamReactionStatistics>> =
        fanReactionsDao?.getTeamReactionStatistics() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getReactionTypeDistribution(): Flow<List<ReactionTypeDistribution>> =
        fanReactionsDao?.getReactionTypeDistribution() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ CLEANUP ============

    suspend fun deleteOldReactions(daysToKeep: Int = 30) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysToKeep)
        val cutoffDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        fanReactionsDao?.deleteOldReactions(cutoffDate)
    }

    // ============ DASHBOARD ============

    suspend fun getFanReactionsDashboard(teamId: Int): FanReactionsDashboard {
        val allReactions = fanReactionsDao?.getReactionsByTeam(teamId)?.firstOrNull() ?: emptyList()
        val positive = allReactions.count { it.sentiment == FanSentiment.POSITIVE.value }
        val negative = allReactions.count { it.sentiment == FanSentiment.NEGATIVE.value }
        val neutral = allReactions.count { it.sentiment == FanSentiment.NEUTRAL.value }

        val recentReactions = allReactions.take(10)

        val sentimentScore = if (allReactions.isNotEmpty()) {
            ((positive - negative).toDouble() / allReactions.size * 100)
        } else 0.0

        val teamName = teamsRepository.getTeamById(teamId)?.name ?: "Unknown Team"

        return FanReactionsDashboard(
            teamName = teamName,
            totalReactions = allReactions.size,
            positiveReactions = positive,
            negativeReactions = negative,
            neutralReactions = neutral,
            sentimentScore = sentimentScore,
            recentReactions = recentReactions
        )
    }
}

// ============ DATA CLASSES ============

data class FanReactionsDashboard(
    val teamName: String,
    val totalReactions: Int,
    val positiveReactions: Int,
    val negativeReactions: Int,
    val neutralReactions: Int,
    val sentimentScore: Double,
    val recentReactions: List<FanReactionsEntity>
)
