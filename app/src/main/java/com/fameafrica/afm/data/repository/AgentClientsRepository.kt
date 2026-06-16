package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.AgentClientsDao
import com.fameafrica.afm.data.database.entities.AgentClientsEntity
import com.fameafrica.afm.data.database.entities.PlayerAgentsEntity
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AgentClientsRepository @Inject constructor(
    private val agentClientsDaoProvider: Provider<AgentClientsDao>
) {
    private val agentClientsDao: AgentClientsDao?
        get() = try {
            agentClientsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    suspend fun getAgentByPlayerId(playerId: Int): PlayerAgentsEntity? =
        agentClientsDao?.getAgentByPlayerId(playerId)

    suspend fun assignAgentToPlayer(
        agentId: Int,
        playerId: Int,
        startDate: String,
        endDate: String
    ) {
        val relationship = AgentClientsEntity(
            agentId = agentId,
            playerId = playerId,
            contractStartDate = startDate,
            contractEndDate = endDate
        )
        agentClientsDao?.insert(relationship)
    }

    suspend fun removeAgentFromPlayer(playerId: Int) {
        val relationship = agentClientsDao?.getRelationshipByPlayerId(playerId)
        if (relationship != null) {
            agentClientsDao?.delete(relationship)
        }
    }
}
