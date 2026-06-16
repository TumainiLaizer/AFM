package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PlayerReactionDistribution
import com.fameafrica.afm.data.database.dao.PlayerReactionsDao
import com.fameafrica.afm.data.database.dao.PlayerReactivityStats
import com.fameafrica.afm.data.database.entities.PlayerReactionsEntity
import com.fameafrica.afm.data.database.entities.PlayerReactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PlayerReactionsRepository @Inject constructor(
    private val playerReactionsDaoProvider: Provider<PlayerReactionsDao>
) {

    private val playerReactionsDao: PlayerReactionsDao?
        get() = try {
            playerReactionsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllReactions(): Flow<List<PlayerReactionsEntity>> = playerReactionsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getReactionById(id: Int): PlayerReactionsEntity? = playerReactionsDao?.getById(id)

    suspend fun insertReaction(reaction: PlayerReactionsEntity) {
        playerReactionsDao?.insert(reaction)
    }

    suspend fun deleteReaction(reaction: PlayerReactionsEntity) {
        playerReactionsDao?.delete(reaction)
    }

    // ============ PLAYER-BASED ============

    fun getReactionsByPlayerId(playerId: Int): Flow<List<PlayerReactionsEntity>> =
        playerReactionsDao?.getReactionsByPlayerId(playerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getReactionsByPlayerName(playerName: String): Flow<List<PlayerReactionsEntity>> =
        playerReactionsDao?.getReactionsByPlayerName(playerName) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getLatestReactionsForPlayers(playerIds: List<Int>): List<PlayerReactionsEntity> =
        playerReactionsDao?.getLatestReactionsForPlayers(playerIds) ?: emptyList()

    suspend fun getPositiveReactionCount(playerId: Int): Int =
        playerReactionsDao?.getPositiveReactionCount(playerId) ?: 0

    suspend fun getNegativeReactionCount(playerId: Int): Int =
        playerReactionsDao?.getNegativeReactionCount(playerId) ?: 0

    // ============ REACTION CREATION ============

    suspend fun addPlayerReaction(
        playerId: Int,
        playerName: String,
        reactionType: String,
        reactionText: String
    ): PlayerReactionsEntity {
        val reaction = PlayerReactionsEntity(
            playerId = playerId,
            playerName = playerName,
            reactionType = reactionType,
            reactionText = reactionText
        )
        playerReactionsDao?.insert(reaction)
        return reaction
    }

    suspend fun addHappyReaction(playerId: Int, playerName: String, context: String): PlayerReactionsEntity {
        val text = when (context) {
            "GOAL" -> "I'm thrilled to score for the team!"
            "WIN" -> "Great team performance today!"
            "CONTRACT" -> "Happy to commit my future to the club!"
            "AWARD" -> "Honored to receive this recognition!"
            else -> "I'm feeling happy with how things are going!"
        }
        return addPlayerReaction(playerId, playerName, PlayerReactionType.HAPPY.value, text)
    }

    suspend fun addExcitedReaction(playerId: Int, playerName: String, context: String): PlayerReactionsEntity {
        val text = when (context) {
            "NEW_SIGNING" -> "Can't wait to get started with my new teammates!"
            "DERBY" -> "This is what I live for! Bring on the derby!"
            "FINAL" -> "We're in the final! Let's bring the trophy home!"
            else -> "I'm excited about what's ahead for us!"
        }
        return addPlayerReaction(playerId, playerName, PlayerReactionType.EXCITED.value, text)
    }

    suspend fun addAngryReaction(playerId: Int, playerName: String, context: String): PlayerReactionsEntity {
        val text = when (context) {
            "RED_CARD" -> "That decision was absolutely ridiculous!"
            "LOSS" -> "We should have won that game. Unacceptable."
            "BENCHED" -> "I need to prove myself and get back in the starting XI."
            else -> "I'm not happy with how things are going."
        }
        return addPlayerReaction(playerId, playerName, PlayerReactionType.ANGRY.value, text)
    }

    suspend fun addFrustratedReaction(playerId: Int, playerName: String, context: String): PlayerReactionsEntity {
        val text = when (context) {
            "IN_REGION" -> "So frustrating to be sidelined when I just found my form."
            "MISSED_CHANCE" -> "Should have scored that. I know I'm better than this."
            "TRANSFER_BLOCKED" -> "I was hoping for a move, but the club rejected the offer."
            else -> "I feel frustrated with my recent performances."
        }
        return addPlayerReaction(playerId, playerName, PlayerReactionType.FRUSTRATED.value, text)
    }

    suspend fun addDisappointedReaction(playerId: Int, playerName: String, context: String): PlayerReactionsEntity {
        val text = when (context) {
            "RELEGATION" -> "We let the fans down. This hurts."
            "ELIMINATION" -> "Out of the cup. We should have gone further."
            "POOR_FORM" -> "I know I can do better. Need to work harder."
            else -> "Disappointed with the result today."
        }
        return addPlayerReaction(playerId, playerName, PlayerReactionType.DISAPPOINTED.value, text)
    }

    suspend fun addProudReaction(playerId: Int, playerName: String, context: String): PlayerReactionsEntity {
        val text = when (context) {
            "DEBUT" -> "Dream come true to make my debut for this club!"
            "CAPTAINCY" -> "Honored to lead this team. I won't let you down."
            "TROPHY" -> "Champions! So proud of every single player in this squad."
            "RECORD" -> "To break a club record is something I'll never forget."
            else -> "So proud of what we've achieved together."
        }
        return addPlayerReaction(playerId, playerName, PlayerReactionType.PROUD.value, text)
    }

    suspend fun addThoughtfulReaction(playerId: Int, playerName: String, context: String): PlayerReactionsEntity {
        val text = when (context) {
            "FUTURE" -> "I'm focused on the next game. That's all that matters."
            "TACTICS" -> "We're trying a new system and it's starting to click."
            "YOUNG_PLAYER" -> "I remember being in his position. Happy to help him develop."
            else -> "We need to analyze what went wrong and improve."
        }
        return addPlayerReaction(playerId, playerName, PlayerReactionType.THOUGHTFUL.value, text)
    }

    // ============ STATISTICS ============

    fun getReactionTypeDistribution(): Flow<List<PlayerReactionDistribution>> =
        playerReactionsDao?.getReactionTypeDistribution() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getMostReactivePlayers(limit: Int): Flow<List<PlayerReactivityStats>> =
        playerReactionsDao?.getMostReactivePlayers(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ DASHBOARD ============

    suspend fun getPlayerReactionsDashboard(playerId: Int, playerName: String): PlayerReactionsDashboard {
        val reactions = playerReactionsDao?.getReactionsByPlayerId(playerId)?.firstOrNull() ?: emptyList()
        val positive = reactions.count { it.isPositive }
        val negative = reactions.count { it.isNegative }
        val neutral = reactions.count { it.isNeutral }

        val recentReactions = reactions.take(10)

        val sentimentScore = if (reactions.isNotEmpty()) {
            ((positive - negative).toDouble() / reactions.size * 100)
        } else 0.0

        return PlayerReactionsDashboard(
            playerName = playerName,
            totalReactions = reactions.size,
            positiveReactions = positive,
            negativeReactions = negative,
            neutralReactions = neutral,
            sentimentScore = sentimentScore,
            recentReactions = recentReactions
        )
    }
}

// ============ DATA CLASSES ============

data class PlayerReactionsDashboard(
    val playerName: String,
    val totalReactions: Int,
    val positiveReactions: Int,
    val negativeReactions: Int,
    val neutralReactions: Int,
    val sentimentScore: Double,
    val recentReactions: List<PlayerReactionsEntity>
)
