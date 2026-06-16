package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.*
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.TacticsEntity
import com.fameafrica.afm.data.database.model.*
import com.fameafrica.afm.utils.GameDateManager
import com.fameafrica.afm.utils.tactics.LineupUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PlayersRepository @Inject constructor(
    private val playersDaoProvider: Provider<PlayersDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val tacticsDaoProvider: Provider<TacticsDao>,
    private val gameDateManager: GameDateManager
) {
    private val playersDao get() = playersDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    private val tacticsDao get() = tacticsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllPlayers(): Flow<List<PlayersEntity>> = playersDao.getAll()

    suspend fun getAllPlayersPaged(limit: Int, offset: Int): List<PlayersEntity> =
        playersDao.getAllPaged(limit, offset)

    fun getAllPlayerSummaries(): Flow<List<PlayerSummary>> = playersDao.getAllSummaries()

    suspend fun getPlayerById(id: Int): PlayersEntity? = playersDao.getById(id)

    suspend fun getPlayerByName(name: String): PlayersEntity? = playersDao.getByName(name)

    suspend fun insertPlayer(player: PlayersEntity) = playersDao.insert(player)

    suspend fun insertAllPlayers(players: List<PlayersEntity>) = playersDao.insertAll(players)

    suspend fun updatePlayer(player: PlayersEntity) = playersDao.update(player)

    suspend fun updatePlayersBatch(players: List<PlayersEntity>) = playersDao.updateBatch(players)

    suspend fun deletePlayer(player: PlayersEntity) = playersDao.delete(player)

    suspend fun deletePlayerById(playerId: Int) = playersDao.deleteById(playerId)

    suspend fun getPlayersCount(): Int = playersDao.getCount()

    suspend fun getActivePlayersCount(): Int = playersDao.getActiveCount()

    suspend fun getTeamIdsWithFewPlayers(minCount: Int): List<Int> =
        playersDao.getTeamIdsWithFewPlayers(minCount)

    // ============ TEAM-BASED ============

    fun getPlayersByTeamId(teamId: Int): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByTeamId(teamId)

    suspend fun getPlayersByTeamIdSync(teamId: Int): List<PlayersEntity> =
        playersDao.getPlayersByTeamIdStatic(teamId)

    suspend fun getPlayersByTeamIdsSync(teamIds: List<Int>): List<PlayersEntity> =
        playersDao.getPlayersByTeamIdsStatic(teamIds)

    fun getPlayersByTeamName(teamName: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByTeamName(teamName)

    fun getPlayersByTeamAndCategory(teamId: Int, category: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByTeamAndCategory(teamId, category)

    fun getStartingXI(teamId: Int): Flow<List<PlayersEntity>> =
        playersDao.getStartingXI(teamId)

    suspend fun getTeamCaptain(teamId: Int): PlayersEntity? =
        playersDao.getTeamCaptain(teamId)

    suspend fun getTeamViceCaptain(teamId: Int): PlayersEntity? =
        playersDao.getTeamViceCaptain(teamId)

    suspend fun getInjuredCountByTeam(teamId: Int): Int =
        playersDao.getInjuredCountByTeam(teamId)

    // ============ POSITION-BASED ============

    fun getPlayersByPosition(position: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByPosition(position)

    fun getPlayersByCategory(category: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByCategory(category)

    fun getPlayersByCategoryAndNationality(category: String, nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByCategoryAndNationality(category, nationality)

    // ============ NATIONALITY-BASED ============

    fun getPlayersByNationality(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByNationality(nationality)

    fun getActivePlayersByNationality(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getActivePlayersByNationality(nationality)

    fun getPlayersByLeague(leagueName: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByLeague(leagueName)

    fun getDistinctNationalities(): Flow<List<String>> =
        playersDao.getDistinctNationalities()

    suspend fun getPlayerCountByNationality(nationality: String): Int =
        playersDao.getPlayerCountByNationality(nationality)

    // ============ RATING-BASED ============

    fun getPlayersByMinRating(minRating: Int): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByMinRating(minRating)

    fun getPlayersByRatingRange(minRating: Int, maxRating: Int): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByRatingRange(minRating, maxRating)

    fun getTopRatedPlayers(limit: Int): Flow<List<PlayersEntity>> =
        playersDao.getTopRatedPlayers(limit)

    fun getTopRatedByPosition(position: String, limit: Int): Flow<List<PlayersEntity>> =
        playersDao.getTopRatedByPosition(position, limit)

    // ============ POTENTIAL-BASED ============

    fun getTopYoungPlayers(minPotential: Int): Flow<List<PlayersEntity>> =
        playersDao.getTopYoungPlayers(minPotential)

    fun getBiggestPotential(limit: Int): Flow<List<PlayersEntity>> =
        playersDao.getBiggestPotential(limit)

    // ============ AGE-BASED ============

    fun getYouthPlayers(): Flow<List<PlayersEntity>> = playersDao.getYouthPlayers()

    fun getVeteranPlayers(): Flow<List<PlayersEntity>> = playersDao.getVeteranPlayers()

    fun getPlayersByAgeRange(minAge: Int, maxAge: Int): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByAgeRange(minAge, maxAge)

    // ============ HEIGHT & PHYSICAL ============

    fun getTallestPlayers(minHeight: Int): Flow<List<PlayersEntity>> =
        playersDao.getTallestPlayers(minHeight)

    fun getPlayersByPreferredFoot(foot: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByPreferredFoot(foot)

    // ============ INJURY & STATUS ============

    fun getInjuredPlayers(): Flow<List<PlayersEntity>> = playersDao.getInjuredPlayers()

    fun getSuspendedPlayers(): Flow<List<PlayersEntity>> = playersDao.getSuspendedPlayers()

    fun getRetiredPlayers(): Flow<List<PlayersEntity>> = playersDao.getRetiredPlayers()

    // ============ CONTRACT & TRANSFER ============

    fun getFreeAgents(): Flow<List<PlayersEntity>> = playersDao.getFreeAgents()

    fun getTransferListed(): Flow<List<PlayersEntity>> = playersDao.getTransferListed()

    fun getLoanListed(): Flow<List<PlayersEntity>> = playersDao.getLoanListed()

    fun getPlayersByMarketValueRange(minValue: Int, maxValue: Int): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByMarketValueRange(minValue, maxValue)

    fun getPlayersWithExpiringContracts(): Flow<List<PlayersEntity>> =
        playersDao.getPlayersWithExpiringContracts()

    // ============ PERSONALITY, TRAITS & ARCHETYPE ============

    fun getPlayersByPersonality(personality: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByPersonality(personality)

    fun getPlayersByArchetype(archetype: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByArchetype(archetype)

    fun getPlayersByTrait(trait: String): Flow<List<PlayersEntity>> =
        playersDao.getPlayersByTrait(trait)

    fun getPotentialCaptains(): Flow<List<PlayersEntity>> = playersDao.getPotentialCaptains()

    fun getTeamLeaders(): Flow<List<PlayersEntity>> = playersDao.getTeamLeaders()

    fun getMediaFriendlyPlayers(): Flow<List<PlayersEntity>> = playersDao.getMediaFriendlyPlayers()

    fun getFanFavorites(): Flow<List<PlayersEntity>> = playersDao.getFanFavorites()

    // ============ SEARCH ============

    fun searchPlayers(searchQuery: String): Flow<List<PlayersEntity>> =
        playersDao.searchPlayers(searchQuery)

    fun advancedSearch(searchQuery: String): Flow<List<PlayersEntity>> =
        playersDao.advancedSearch(searchQuery)

    // ============ STATISTICS ============

    suspend fun getAverageRatingByTeam(teamId: Int): Double? =
        playersDao.getAverageRatingByTeam(teamId)

    suspend fun getTotalMarketValueByTeam(teamId: Int): Long? =
        playersDao.getTotalMarketValueByTeam(teamId)

    suspend fun getAverageAgeByTeam(teamId: Int): Double? =
        playersDao.getAverageAgeByTeam(teamId)

    suspend fun getAverageHeightByTeam(teamId: Int): Double? =
        playersDao.getAverageHeightByTeam(teamId)

    fun getTeamSquadAnalysis(teamId: Int): Flow<List<SquadAnalysis>> =
        playersDao.getTeamSquadAnalysis(teamId)

    fun getNationalityDistribution(limit: Int = 10): Flow<List<NationalityDistribution>> =
        playersDao.getNationalityDistribution(limit)

    fun getArchetypeDistribution(): Flow<List<ArchetypeDistribution>> =
        playersDao.getArchetypeDistribution()

    fun getPersonalityDistribution(): Flow<List<PersonalityDistribution>> =
        playersDao.getPersonalityDistribution()

    fun getTraitDistribution(): Flow<List<TraitDistribution>> =
        playersDao.getTraitDistribution()

    // ============ NATIONAL TEAM ELIGIBILITY ============

    fun getEligiblePlayersForNationalTeam(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getEligiblePlayersForNationalTeam(nationality)

    fun getEligibleGoalkeepersForNationalTeam(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getEligibleGoalkeepersForNationalTeam(nationality)

    fun getEligibleDefendersForNationalTeam(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getEligibleDefendersForNationalTeam(nationality)

    fun getEligibleMidfieldersForNationalTeam(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getEligibleMidfieldersForNationalTeam(nationality)

    fun getEligibleForwardsForNationalTeam(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getEligibleForwardsForNationalTeam(nationality)

    fun getPotentialNationalTeamCaptains(nationality: String): Flow<List<PlayersEntity>> =
        playersDao.getPotentialNationalTeamCaptains(nationality)

    // ============ JOIN QUERIES ============

    suspend fun getPlayerWithDetails(playerId: Int): PlayerWithDetails? =
        playersDao.getPlayerWithDetails(playerId)

    fun getTeamSquadWithDetails(teamId: Int): Flow<List<PlayerWithTeamDetails>> =
        playersDao.getTeamSquadWithDetails(teamId)

    // ============ 🔥 PLAYER ATTRIBUTE UPDATES ============

    /**
     * Update specific player attributes using the Game World Timeline.
     */
    suspend fun updatePlayerAttributes(
        playerId: Int,
        attributeUpdates: Map<String, Int>,
        currentWeek: Int
    ): PlayersEntity? {

        val player = playersDao.getById(playerId) ?: return null
        var updatedPlayer = player

        attributeUpdates.forEach { (attribute, value) ->
            updatedPlayer = when (attribute.lowercase()) {
                "finishing" -> updatedPlayer.copy(finishing = value.coerceIn(1, 99))
                "passing" -> updatedPlayer.copy(passing = value.coerceIn(1, 99))
                "dribbling" -> updatedPlayer.copy(dribbling = value.coerceIn(1, 99))
                "skill" -> updatedPlayer.copy(skill = value.coerceIn(1, 99))
                "crossing" -> updatedPlayer.copy(crossing = value.coerceIn(1, 99))
                "defending" -> updatedPlayer.copy(defending = value.coerceIn(1, 99))
                "heading" -> updatedPlayer.copy(heading = value.coerceIn(1, 99))
                "long_shots", "longshots" -> updatedPlayer.copy(longShots = value.coerceIn(1, 99))
                "pace" -> updatedPlayer.copy(pace = value.coerceIn(1, 99))
                "stamina" -> updatedPlayer.copy(stamina = value.coerceIn(1, 99))
                "strength" -> updatedPlayer.copy(strength = value.coerceIn(1, 99))
                "acceleration" -> updatedPlayer.copy(acceleration = value.coerceIn(1, 99))
                "agility" -> updatedPlayer.copy(agility = value.coerceIn(1, 99))
                "aggression" -> updatedPlayer.copy(aggression = value.coerceIn(1, 99))
                "leadership" -> updatedPlayer.copy(leadership = value.coerceIn(1, 99))
                "motivation" -> updatedPlayer.copy(motivation = value.coerceIn(1, 99))
                "composure" -> updatedPlayer.copy(composure = value.coerceIn(1, 99))
                "vision" -> updatedPlayer.copy(vision = value.coerceIn(1, 99))
                "positioning" -> updatedPlayer.copy(positioning = value.coerceIn(1, 99))
                "anticipation" -> updatedPlayer.copy(anticipation = value.coerceIn(1, 99))
                "decisions" -> updatedPlayer.copy(decisions = value.coerceIn(1, 99))
                "creativity" -> updatedPlayer.copy(creativity = value.coerceIn(1, 99))
                "teamwork" -> updatedPlayer.copy(teamwork = value.coerceIn(1, 99))
                "goalkeeping" -> updatedPlayer.copy(goalkeeping = value.coerceIn(1, 99))
                "reflexes" -> updatedPlayer.copy(reflexes = value.coerceIn(1, 99))
                "handling" -> updatedPlayer.copy(handling = value.coerceIn(1, 99))
                "aerial_ability", "aerialability" -> updatedPlayer.copy(aerialAbility = value.coerceIn(1, 99))
                "command_of_area", "commandofarea" -> updatedPlayer.copy(commandOfArea = value.coerceIn(1, 99))
                "kicking" -> updatedPlayer.copy(kicking = value.coerceIn(1, 99))
                "rating" -> updatedPlayer.copy(rating = value.coerceIn(1, 99))
                "potential" -> updatedPlayer.copy(potential = value.coerceIn(1, 99))
                "current_form", "form" -> updatedPlayer.copy(currentForm = value.coerceIn(1, 10).toInt())
                "experience" -> updatedPlayer.copy(experience = value.coerceIn(0, 100))
                "morale" -> updatedPlayer.copy(morale = value.coerceIn(0, 100))
                else -> updatedPlayer
            }
        }

        // Use Game Date instead of System Date
        updatedPlayer = updatedPlayer.copy(
            updatedAt = gameDateManager.formatGameDate(currentWeek)
        )

        playersDao.update(updatedPlayer)
        return updatedPlayer
    }

    // ============ PLAYER MANAGEMENT ============

    suspend fun updatePlayerAfterMatch(
        playerId: Int,
        goalsScored: Int,
        assistsMade: Int,
        isManOfMatch: Boolean,
        matchRating: Double
    ) {
        val player = playersDao.getById(playerId) ?: return
        
        // 1-10 Form Scaling: 7.0 is the pivot
        val formDelta = when {
            isManOfMatch -> 2.0
            matchRating >= 8.5 -> 1.5
            matchRating >= 7.5 -> 0.5
            matchRating < 5.0 -> -1.5
            matchRating < 6.0 -> -0.5
            else -> 0.0
        }
        
        val newForm = (player.currentForm + formDelta).coerceIn(1.0, 10.0).toInt()
        
        val updatedPlayer = player.copy(
            matches = player.matches + 1,
            goals = player.goals + goalsScored,
            assists = player.assists + assistsMade,
            manOfMatch = player.manOfMatch + (if (isManOfMatch) 1 else 0),
            currentForm = newForm,
            experience = player.experience + 1
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun incrementPlayerGoals(playerId: Int) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.copy(
            goals = player.goals + 1,
            currentForm = (player.currentForm + 1).coerceIn(1, 10)
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun incrementPlayerAssists(playerId: Int) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.copy(
            assists = player.assists + 1,
            currentForm = (player.currentForm + 1).coerceIn(1, 10)
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun incrementPlayerCleanSheets(playerId: Int) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.copy(
            cleanSheets = player.cleanSheets + 1,
            currentForm = (player.currentForm + 1).coerceIn(1, 10)
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun incrementPlayerYellowCards(playerId: Int) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.copy(
            yellowCards = player.yellowCards + 1,
            currentForm = (player.currentForm - 1).coerceIn(1, 10),
            morale = (player.morale - 2).coerceIn(0, 100)
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun incrementPlayerRedCards(playerId: Int) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.copy(
            redCards = player.redCards + 1,
            currentForm = (player.currentForm - 2).coerceIn(1, 10),
            morale = (player.morale - 5).coerceIn(0, 100),
            suspended = true
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun setPlayerInjury(playerId: Int, injuryType: String, recoveryDays: Int, currentWeek: Int) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.copy(
            injuryStatus = injuryType,
            recoveryTime = recoveryDays,
            updatedAt = gameDateManager.formatGameDate(currentWeek)
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun recoverPlayerFromInjury(playerId: Int, currentWeek: Int) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.copy(
            injuryStatus = "HEALTHY",
            recoveryTime = 0,
            updatedAt = gameDateManager.formatGameDate(currentWeek)
        )
        playersDao.update(updatedPlayer)
    }

    suspend fun transferPlayer(playerId: Int, newTeamId: Int, newTeamName: String, newMarketValue: Int? = null) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.transferTo(newTeamId, newTeamName, newMarketValue)
        playersDao.update(updatedPlayer)
    }

    suspend fun renewContract(playerId: Int, newSalary: Double, newExpiry: String) {
        val player = playersDao.getById(playerId) ?: return
        val updatedPlayer = player.renewContract(newSalary, newExpiry)
        playersDao.update(updatedPlayer)
    }

    suspend fun updatePlayerMorale(playerId: Int, change: Int) {
        playersDao.updatePlayerMorale(playerId, change)
    }

    suspend fun assignCaptain(teamId: Int, playerId: Int) {
        playersDao.removeCaptain(teamId)
        playersDao.setCaptain(playerId)
    }

    suspend fun assignViceCaptain(teamId: Int, playerId: Int) {
        playersDao.removeViceCaptain(teamId)
        playersDao.setViceCaptain(playerId)
    }

    suspend fun updatePlayersManager(teamId: Int, managerId: Int) {
        playersDao.updatePlayersManager(teamId, managerId)
    }

    suspend fun setStartingXI(teamId: Int, playerIds: List<Int>) {
        playersDao.updateStartingXI(teamId, playerIds)
    }

    suspend fun ensureFullLineupsForAllTeams() {
        val teams = teamsDao.getAll().firstOrNull() ?: return
        for (team in teams) {
            val players = playersDao.getPlayersByTeamId(team.id).firstOrNull() ?: continue
            val starters = players.filter { it.isStartingXi }
            if (starters.size < 11) {
                val tactics = tacticsDao.getByTeamId(team.id) ?: TacticsEntity(
                    teamId = team.id,
                    teamName = team.name,
                    formation = team.formation ?: "4-4-2",
                    tacticalArchetype = "BALANCED",
                    playstyle = "Balanced"
                )
                val autoXiIds = LineupUtils.autoSelectLineup(players, tactics.formation)
                if (autoXiIds.isNotEmpty()) {
                    setStartingXI(team.id, autoXiIds)
                }
            }
        }
    }

    // ============ BULK PERFORMANCE UPDATES ============

    suspend fun recoverAllStamina(recovery: Int) = playersDao.recoverAllStamina(recovery)

    suspend fun advanceInjuryRecovery() = playersDao.advanceInjuryRecovery()
    
    suspend fun advanceInjuryRecoveryDaily() = playersDao.advanceInjuryRecoveryDaily()

    suspend fun clearRecoveredInjuries() = playersDao.clearRecoveredInjuries()
}
