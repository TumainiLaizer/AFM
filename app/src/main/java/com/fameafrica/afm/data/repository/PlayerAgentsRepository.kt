package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.AgentNationalityStats
import com.fameafrica.afm.data.database.dao.AgentSpecializationStats
import com.fameafrica.afm.data.database.dao.PlayerAgentsDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

@Singleton
class PlayerAgentsRepository @Inject constructor(
    private val playerAgentsDaoProvider: Provider<PlayerAgentsDao>,
    private val nationalitiesRepository: NationalitiesRepository,
    private val agentClientsRepository: AgentClientsRepository,
    private val newsRepository: NewsRepository
) {
    private val playerAgentsDao get() = playerAgentsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllAgents(): Flow<List<PlayerAgentsEntity>> = playerAgentsDao.getAll()

    suspend fun getAgentById(id: Int): PlayerAgentsEntity? = playerAgentsDao.getById(id)

    suspend fun getAgentByName(agentName: String): PlayerAgentsEntity? =
        playerAgentsDao.getByAgentName(agentName)

    suspend fun insertAgent(agent: PlayerAgentsEntity) = playerAgentsDao.insert(agent)

    suspend fun updateAgent(agent: PlayerAgentsEntity) = playerAgentsDao.update(agent)

    suspend fun deleteAgent(agent: PlayerAgentsEntity) = playerAgentsDao.delete(agent)

    // ============ AGENT CREATION ============

    /**
     * Create a new independent agent with randomized stats based on negotiation power
     */
    suspend fun createAgent(
        agentName: String,
        agency: String? = null,
        negotiationPower: Int = 50,
        commissionRate: Int = 10,
        personality: AgentPersonality = AgentPersonality.BALANCED,
        specialization: AgentSpecialization = AgentSpecialization.ALL_ROUNDER,
        nationality: String? = null
    ): PlayerAgentsEntity {

        val agent = PlayerAgentsEntity(
            agentName = agentName,
            agency = agency,
            negotiationPower = negotiationPower.coerceIn(0, 100),
            commissionRate = commissionRate.coerceIn(5, 20),
            reputation = calculateAgentReputation(negotiationPower),
            personality = personality.value,
            specialization = specialization.value,
            nationality = nationality,
            yearsExperience = (negotiationPower / 5) + Random.nextInt(0, 5),
            activeClients = 0,
            successfulDeals = Random.nextInt(0, 20),
            totalDealValue = 0L
        )

        playerAgentsDao.insert(agent)
        return agent
    }

    private fun calculateAgentReputation(negotiationPower: Int): Int {
        return (negotiationPower * 0.8 + Random.nextInt(0, 20)).toInt().coerceIn(0, 100)
    }

    /**
     * Update negotiation power (after successful deals)
     */
    suspend fun updateNegotiationPower(agentId: Int, newPower: Int): PlayerAgentsEntity? {
        val agent = playerAgentsDao.getById(agentId) ?: return null

        val updated = agent.copy(
            negotiationPower = newPower.coerceIn(0, 100),
            reputation = calculateAgentReputation(newPower)
        )

        playerAgentsDao.update(updated)
        return updated
    }

    // ============ 1. AFRICAN REALISM ENGINE ============

    /**
     * Finds the best suited agent for a player based on African cultural bias and regional dominance.
     */
    suspend fun findBestAgentForPlayer(
        playerNationality: String,
        playerReputation: Int
    ): PlayerAgentsEntity? {
        val allAgents = playerAgentsDao.getAll().firstOrNull() ?: return null
        val playerRegion = nationalitiesRepository.getRegionByNationality(playerNationality)

        return allAgents.shuffled().minByOrNull { agent ->
            var score = 100.0

            // Priority 1: Same Nationality (Strong cultural/family bonds in African football)
            if (agent.nationality == playerNationality) score -= 60

            // Priority 2: Regional Dominance (e.g. West African agents controlling West African stars)
            val agentRegion = nationalitiesRepository.getRegionByNationality(agent.nationality ?: "")
            if (agentRegion != null && agentRegion == playerRegion) score -= 25

            // Priority 3: Reputation Alignment (AI Decision Logic)
            // Big stars want Super Agents, low-tier players want local agents
            val repDiff = abs(agent.reputation - playerReputation)
            score += repDiff * 0.8

            // Personality Bias - Loyal agents are more attractive to local/younger players
            if (agent.personality == AgentPersonality.LOYAL.value && playerReputation < 50) score -= 15
            
            score
        }
    }

    // ============ 2. DYNAMIC TRANSFER ECONOMY ============

    /**
     * Big agents and "Greedy" personalities inflate transfer fees significantly.
     */
    fun calculateInflatedFee(baseFee: Long, agent: PlayerAgentsEntity?): Long {
        if (agent == null) return baseFee
        var multiplier = 1.0
        
        // Super Agent status adds massive market premium
        if (agent.reputation >= 85) multiplier += 0.35
        else if (agent.reputation >= 70) multiplier += 0.15

        // Personality-based greed inflation
        multiplier += when (agent.personality) {
            AgentPersonality.GREEDY.value -> 0.25
            AgentPersonality.AGGRESSIVE.value -> 0.15
            AgentPersonality.BALANCED.value -> 0.05
            else -> 0.0
        }

        return (baseFee * multiplier).toLong()
    }

    /**
     * Calculates how much an agent will inflate a player's wage demands.
     */
    fun calculateWageDemandMultiplier(agent: PlayerAgentsEntity?): Double {
        if (agent == null) return 1.0
        return when (agent.personality) {
            AgentPersonality.GREEDY.value -> 1.50
            AgentPersonality.AGGRESSIVE.value -> 1.30
            AgentPersonality.DEVELOPMENT_FOCUSED.value -> 0.85
            AgentPersonality.LOYAL.value -> 0.90
            else -> 1.10
        }
    }

    // ============ 3. SMART AI DECISIONS & 4. SUPER AGENT SYSTEM ============

    /**
     * Auto-assigns an agent to a player using the Realism Engine
     */
    suspend fun autoAssignAgent(
        playerId: Int,
        playerNationality: String,
        playerReputation: Int,
        gameDate: String
    ): PlayerAgentsEntity? {
        val selectedAgent = findBestAgentForPlayer(playerNationality, playerReputation) ?: return null
        
        // Link via relationship system
        agentClientsRepository.assignAgentToPlayer(
            agentId = selectedAgent.id,
            playerId = playerId,
            startDate = gameDate,
            endDate = "2029-06-30" // Default 3 years
        )
        
        playerAgentsDao.incrementClientCount(selectedAgent.id)
        return selectedAgent
    }

    suspend fun getSuperAgents(): List<PlayerAgentsEntity> = playerAgentsDao.getSuperAgents()

    /**
     * Checks if agent has enough influence to manipulate regional market prices.
     */
    fun isMarketInfluencer(agent: PlayerAgentsEntity): Boolean {
        return agent.reputation >= 85 && agent.activeClients >= 15
    }

    // ============ 5. DRAMA SYSTEM ============

    /**
     * Aggressive and Greedy agents can force a move if player morale or happiness is low.
     */
    fun shouldForceTransfer(agent: PlayerAgentsEntity, playerMorale: Int, playerHappiness: Int): Boolean {
        if (agent.personality == AgentPersonality.LOYAL.value) return false
        
        val threshold = when (agent.personality) {
            AgentPersonality.GREEDY.value -> 75 // Wants that transfer commission
            AgentPersonality.AGGRESSIVE.value -> 65
            else -> 45
        }
        
        return playerMorale < threshold || playerHappiness < threshold
    }

    /**
     * Personality-driven dialogue for media interaction and contract talks.
     */
    fun getNegotiationDialogue(agent: PlayerAgentsEntity, context: String): String {
        return when (agent.personality) {
            AgentPersonality.GREEDY.value -> when (context) {
                "INITIAL" -> "My client is a superstar. His wage must reflect his global market value."
                "REJECTED" -> "This is an insult. We have three bigger clubs ready to double this."
                "NEGOTIATION_START" -> "Let's make this quick. We know what he's worth on the global market."
                else -> "The commission needs to be paid upfront. No exceptions."
            }
            AgentPersonality.AGGRESSIVE.value -> when (context) {
                "INITIAL" -> "We aren't here to play games. Show us a serious project or we leave."
                "REJECTED" -> "You're testing my patience. My client is too big for this league."
                "NEGOTIATION_START" -> "I'm here to get the best deal. Don't expect any favors."
                else -> "We want a release clause for Europe. Don't waste my time."
            }
            AgentPersonality.LOYAL.value -> "We want to stay and build a legacy here. Money is secondary to respect."
            AgentPersonality.DEVELOPMENT_FOCUSED.value -> "Playing time is non-negotiable. He needs minutes to reach the European stage."
            else -> "We are open to a fair deal that benefits both parties."
        }
    }

    // ============ NEGOTIATION SIMULATION ENGINE ============

    /**
     * AI logic for agent's step-by-step decision logic for transfer/contract offers.
     */
    fun evaluateNegotiationStep(
        agent: PlayerAgentsEntity,
        offeredSalary: Int,
        currentSalary: Int,
        clubReputation: Int
    ): NegotiationAction {
        val salaryIncrease = if (currentSalary > 0) offeredSalary.toDouble() / currentSalary.toDouble() else 1.5
        var chance = 50.0
        
        chance += (salaryIncrease - 1.0) * 100
        chance += (clubReputation - 50) * 0.5
        
        // Personality modifiers
        when (agent.personality) {
            AgentPersonality.GREEDY.value -> if (salaryIncrease < 1.4) chance -= 30
            AgentPersonality.AGGRESSIVE.value -> if (salaryIncrease < 1.2) return NegotiationAction.THREATEN_EXIT
            AgentPersonality.LOYAL.value -> chance += 25
        }

        return when {
            chance >= 85 -> NegotiationAction.ACCEPT
            chance >= 40 -> NegotiationAction.COUNTER_OFFER
            else -> NegotiationAction.WALK_AWAY
        }
    }

    enum class NegotiationAction { ACCEPT, COUNTER_OFFER, WALK_AWAY, THREATEN_EXIT }

    // ============ DYNAMIC GROWTH ============

    suspend fun recordSuccessfulDeal(agentId: Int, dealValue: Long) {
        val agent = playerAgentsDao.getById(agentId) ?: return

        val updated = agent.copy(
            successfulDeals = agent.successfulDeals + 1,
            totalDealValue = agent.totalDealValue + dealValue,
            reputation = (agent.reputation + 2).coerceAtMost(100),
            negotiationPower = (agent.negotiationPower + 1).coerceAtMost(100)
        )

        playerAgentsDao.update(updated)
        
        // Report big deals to news
        if (dealValue > 10_000_000) {
            newsRepository.createNewsArticle(
                headline = "MARKET SHOCK: ${agent.agentName} brokers record deal!",
                content = "Insiders say this deal could reset the entire transfer landscape in the region.",
                category = "TRANSFER",
                journalistName = "African Football Financials",
                isTopNews = true
            )
        }
    }

    // ============ QUERIES ============

    fun getTopAgents(minReputation: Int): Flow<List<PlayerAgentsEntity>> =
        playerAgentsDao.getTopAgents(minReputation)

    fun getBestNegotiators(minPower: Int): Flow<List<PlayerAgentsEntity>> =
        playerAgentsDao.getBestNegotiators(minPower)

    fun getAgentsBySpecialization(specialization: String): Flow<List<PlayerAgentsEntity>> =
        playerAgentsDao.getAgentsBySpecialization(specialization)

    fun getAgentSpecializationStats(): Flow<List<AgentSpecializationStats>> =
        playerAgentsDao.getAgentSpecializationStats()

    fun getAgentNationalityStats(): Flow<List<AgentNationalityStats>> =
        playerAgentsDao.getAgentNationalityStats()

    suspend fun getAgentDashboard(): AgentDashboard {
        val allAgents = playerAgentsDao.getAll().firstOrNull() ?: emptyList()
        val topAgents = allAgents.sortedByDescending { it.reputation }.take(10)
        
        val totalDealValue = allAgents.sumOf { it.totalDealValue }
        val totalSuccessfulDeals = allAgents.sumOf { it.successfulDeals }

        return AgentDashboard(
            totalAgents = allAgents.size,
            topAgents = topAgents,
            bestNegotiators = allAgents.sortedByDescending { it.negotiationPower }.take(10),
            totalDealValue = totalDealValue,
            totalSuccessfulDeals = totalSuccessfulDeals
        )
    }
}

// ============ DATA CLASSES ============

data class AgentDashboard(
    val totalAgents: Int,
    val topAgents: List<PlayerAgentsEntity>,
    val bestNegotiators: List<PlayerAgentsEntity>,
    val totalDealValue: Long,
    val totalSuccessfulDeals: Int
)
