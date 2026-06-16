package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.AgentClientsEntity
import com.fameafrica.afm.data.database.entities.PlayerAgentsEntity

@Dao
interface AgentClientsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(agentClient: AgentClientsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AgentClientsEntity>)

    @Query("SELECT * FROM agent_clients")
    suspend fun getAllStatic(): List<AgentClientsEntity>

    @Delete
    suspend fun delete(agentClient: AgentClientsEntity)

    @Query("SELECT player_agents.* FROM player_agents INNER JOIN agent_clients ON player_agents.id = agent_clients.agent_id WHERE agent_clients.player_id = :playerId")
    suspend fun getAgentByPlayerId(playerId: Int): PlayerAgentsEntity?

    @Query("SELECT * FROM agent_clients WHERE player_id = :playerId")
    suspend fun getRelationshipByPlayerId(playerId: Int): AgentClientsEntity?
}
