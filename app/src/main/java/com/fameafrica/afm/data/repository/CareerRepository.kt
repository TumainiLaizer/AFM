package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.CareersDao
import com.fameafrica.afm.data.database.dao.PlayersDao
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CareerRepository @Inject constructor(
    private val careersDaoProvider: Provider<CareersDao>,
    private val teamsRepository: TeamsRepository,
    private val managersRepository: ManagersRepository,
    private val leaguesRepository: LeaguesRepository,
    private val managerOffersRepository: ManagerOffersRepository,
    private val gameStatesRepository: GameStatesRepository,
    private val playersDaoProvider: Provider<PlayersDao>,
    private val gameDateManager: GameDateManager
) {
    private val careersDao get() = careersDaoProvider.get()
    private val playersDao get() = playersDaoProvider.get()

    fun getAllCareers(): Flow<List<CareersEntity>> = careersDao.getAll()
    suspend fun getCareerById(id: Int): CareersEntity? = careersDao.getById(id)
    suspend fun insertCareer(career: CareersEntity): Long = careersDao.insert(career)
    suspend fun getCareersCount(): Int = careersDao.getCount()
    suspend fun isFirstCareer(): Boolean = careersDao.getCount() == 0

    private fun getGameStartDate(): String = gameDateManager.formatGameDateForDb(1)

    suspend fun createCareer(
        manager: ManagersEntity,
        teamId: Int,
        difficulty: String
    ): Long {
        val managerId: Long = managersRepository.insertManager(manager)
        handleExistingTeamManager(teamId, managerId.toInt())

        val team = teamsRepository.getTeamById(teamId)
            ?: throw IllegalArgumentException("Team not found with ID: $teamId")

        val userLeague = leaguesRepository.getLeagueByName(team.league)
            ?: throw IllegalArgumentException("League not found: ${team.league}")

        val career = CareersEntity(
            managerId = managerId.toInt(),
            managerName = manager.name,
            teamId = teamId,
            teamName = team.name,
            leagueName = team.league,
            leagueLevel = userLeague.level,
            difficulty = difficulty,
            season = GameDateManager.START_YEAR,
            startDate = getGameStartDate(),
            isActive = 1,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        insertCareer(career)

        // Create GameState record starting at Week 1
        val gameState = gameStatesRepository.createNewSave(
            managerId = managerId.toInt(),
            managerName = manager.name,
            teamId = teamId,
            teamName = team.name,
            saveName = "${manager.name}'s ${team.name} career",
            week = 1
        )

        if (isFirstCareer()) {
            managerOffersRepository.generateInitialOfferForNewUser(managerId.toInt())
        }

        return gameState.id.toLong()
    }

    private suspend fun handleExistingTeamManager(teamId: Int, newManagerId: Int?) {
        val team = teamsRepository.getTeamById(teamId) ?: return
        val existingManagerId = team.managerId

        if (existingManagerId != null) {
            // Unlink existing manager from the team
            managersRepository.leaveClub(existingManagerId)
        }

        if (newManagerId != null) {
            // Link team to the new manager and update team ID in manager record
            teamsRepository.assignManager(teamId, newManagerId)

            // Ensure manager record has the teamId
            val newManager = managersRepository.getManagerById(newManagerId)
            if (newManager != null && newManager.teamId != teamId) {
                managersRepository.updateManager(newManager.copy(teamId = teamId))
            }

            playersDao.updatePlayersManager(teamId, newManagerId)
        }
    }

    suspend fun startCareer(
        managerName: String,
        managerAge: Int,
        nationality: String,
        coachingLicense: String,
        managerStyle: String,
        preferredFormation: String,
        youthDevelopment: Int,
        mediaHandling: Int,
        tacticalFlexibility: Int,
        playerMotivation: Int,
        disciplineLevel: Int,
        adaptability: Int,
        teamId: Int,
        difficulty: String
    ): Long {
        val manager = ManagersEntity(
            id = teamId,
            name = managerName,
            nationality = nationality,
            age = managerAge,
            teamId = teamId,
            coachingLicense = coachingLicense,
            reputation = 30,
            reputationLevel = "Local",
            preferredFormation = preferredFormation,
            style = managerStyle,
            youthDevelopmentFocus = youthDevelopment,
            mediaHandling = mediaHandling,
            tacticalFlexibility = tacticalFlexibility,
            playerMotivation = playerMotivation,
            disciplineLevel = disciplineLevel,
            adaptability = adaptability
        )

        return createCareer(manager, teamId, difficulty)
    }
}
