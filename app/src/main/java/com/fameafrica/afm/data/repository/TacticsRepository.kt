package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ArchetypeCount
import com.fameafrica.afm.data.database.dao.FormationCount
import com.fameafrica.afm.data.database.dao.ManagersDao
import com.fameafrica.afm.data.database.dao.TacticsDao
import com.fameafrica.afm.data.database.entities.TacticsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TacticsRepository @Inject constructor(
    private val tacticsDaoProvider: Provider<TacticsDao>,
    private val teamsRepository: TeamsRepository,
    private val managersDaoProvider: Provider<ManagersDao>
) {
    private val tacticsDao get() = tacticsDaoProvider.get()
    private val managersDao get() = managersDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllTactics(): Flow<List<TacticsEntity>> = tacticsDao.getAll()

    suspend fun getTacticsById(id: Int): TacticsEntity? = tacticsDao.getById(id)

    suspend fun getTacticsByTeam(teamId: Int): TacticsEntity? = tacticsDao.getByTeamId(teamId)

    suspend fun getTacticsByTeamsSync(teamIds: List<Int>): List<TacticsEntity> = tacticsDao.getByTeamIds(teamIds)

    fun getTacticsByTeamFlow(teamId: Int): Flow<TacticsEntity?> = tacticsDao.getByTeamIdFlow(teamId)

    suspend fun insertTactics(tactics: TacticsEntity) = tacticsDao.insert(tactics)

    suspend fun updateTactics(tactics: TacticsEntity) = tacticsDao.update(tactics)

    suspend fun deleteTactics(tactics: TacticsEntity) = tacticsDao.delete(tactics)

    suspend fun deleteByTeam(teamId: Int) = tacticsDao.deleteByTeamId(teamId)

    suspend fun getTacticsCount(): Int = tacticsDao.getCount()

    // ============ PRESET MANAGEMENT ============

    suspend fun initializeTeamTactics(teamId: Int, teamName: String, formation: String, managerId: Int? = null) {
        val existing = tacticsDao.getByTeamId(teamId)
        if (existing != null) return

        val defaultTactics = TacticsEntity(
            teamId = teamId,
            teamName = teamName,
            managerId = managerId,
            formation = formation,
            tacticalArchetype = "BALANCED",
            playstyle = "Structured",
            isDefault = true
        )
        tacticsDao.insert(defaultTactics)
    }

    suspend fun customizeTactics(
        teamId: Int,
        teamName: String,
        formation: String,
        playstyle: String,
        archetype: String,
        defensiveThreshold: Int,
        attackingThreshold: Int,
        tempo: Int,
        width: Int,
        depth: Int,
        pressIntensity: Int,
        passingDirectness: Int,
        creativity: Int,
        autoselectLineup: Boolean = true,
        substitutionStrategy: String = "MANUAL",
        startingXiIds: List<Int>? = null,
        substituteIds: List<Int>? = null,
        captainId: Int? = null,
        viceCaptainId: Int? = null,
        penaltyTakerId: Int? = null,
        freeKickTakerId: Int? = null,
        cornerTakerId: Int? = null
    ) {
        val current = tacticsDao.getByTeamId(teamId) ?: TacticsEntity(
            teamId = teamId,
            teamName = teamName,
            formation = formation,
            tacticalArchetype = archetype,
            playstyle = playstyle
        )
        
        val updated = current.copy(
            formation = formation,
            playstyle = playstyle,
            tacticalArchetype = archetype,
            defensiveThreshold = defensiveThreshold,
            attackingThreshold = attackingThreshold,
            tempo = tempo,
            width = width,
            depth = depth,
            pressIntensity = pressIntensity,
            passingDirectness = passingDirectness,
            creativity = creativity,
            autoselectLineup = autoselectLineup,
            substitutionStrategy = substitutionStrategy,
            startingXiIds = startingXiIds,
            substituteIds = substituteIds,
            captainId = captainId,
            viceCaptainId = viceCaptainId,
            penaltyTakerId = penaltyTakerId,
            freeKickTakerId = freeKickTakerId,
            cornerTakerId = cornerTakerId,
            lastUpdated = System.currentTimeMillis().toString()
        )
        tacticsDao.insert(updated)
    }

    // ============ ANALYTICS ============

    fun getArchetypeDistribution(): Flow<List<ArchetypeCount>> = tacticsDao.getArchetypeDistribution()

    fun getFormationDistribution(): Flow<List<FormationCount>> = tacticsDao.getFormationDistribution()

    // ============ SUGGESTION ENGINE ============

    suspend fun getTacticalRecommendations(teamId: Int, opponentId: Int): List<TacticalSuggestion> {
        val teamTactics = tacticsDao.getByTeamId(teamId) ?: return emptyList()
        val opponentTactics = tacticsDao.getByTeamId(opponentId) ?: return emptyList()

        val suggestions = mutableListOf<TacticalSuggestion>()

        // 1. Countering High Pressing
        if (opponentTactics.pressIntensity >= 75) {
            suggestions.add(TacticalSuggestion(
                title = "Bypass the Press",
                description = "Opponent is pressing high. Increase passing directness and tempo to exploit space behind their line.",
                recommendedArchetype = "COUNTER",
                priority = 5
            ))
        }

        // 2. Breaking Down Defensive Teams
        if (opponentTactics.tacticalArchetype == "DEFENSIVE" || opponentTactics.defensiveThreshold >= 80) {
            suggestions.add(TacticalSuggestion(
                title = "Patient Buildup",
                description = "Opponent is sitting very deep. Use higher width and creativity to stretch their block and find gaps.",
                recommendedArchetype = "POSSESSION",
                priority = 4
            ))
        }

        // 3. Matchup Analysis
        val matchup = teamTactics.getMatchupProbabilities(opponentTactics.tacticalArchetype)
        if (matchup.third > 40) { // Loss probability high
            suggestions.add(TacticalSuggestion(
                title = "Tactical Shift Required",
                description = "Your current system struggles against their style. Consider shifting to a more balanced or pressing approach.",
                recommendedArchetype = "PRESSING",
                priority = 3
            ))
        }

        return suggestions.sortedByDescending { it.priority }
    }
}

data class TacticalSuggestion(
    val title: String,
    val description: String,
    val recommendedArchetype: String,
    val priority: Int
)
