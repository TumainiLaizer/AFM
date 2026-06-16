package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.GameStatesDao
import com.fameafrica.afm.data.database.entities.GameStatesEntity
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class GameStatesRepository @Inject constructor(
    private val gameStatesDaoProvider: Provider<GameStatesDao>,
    private val gameDateManager: GameDateManager,
    private val seasonHistoryRepository: SeasonHistoryRepository
) {
    private val gameStatesDao get() = gameStatesDaoProvider.get()

    fun getAllSaveGames(): Flow<List<GameStatesEntity>> = gameStatesDao.getAll()

    fun getValidSaveGames(): Flow<List<GameStatesEntity>> = gameStatesDao.getValidSaveGames()

    suspend fun getGameStateById(id: Int): GameStatesEntity? = gameStatesDao.getById(id)

    suspend fun getGameStateByManagerId(managerId: Int): GameStatesEntity? = gameStatesDao.getByManagerId(managerId)

    suspend fun createNewSave(
        managerId: Int,
        managerName: String,
        teamId: Int,
        teamName: String,
        saveName: String,
        week: Int = 1
    ): GameStatesEntity {
        val newState = GameStatesEntity(
            managerId = managerId,
            managerName = managerName,
            teamId = teamId,
            teamName = teamName,
            name = saveName,
            week = week,
            season = gameDateManager.getSeasonString(week),
            lastPlayed = System.currentTimeMillis().toString()
        )
        val id = gameStatesDao.insert(newState)
        return newState.copy(id = id.toInt())
    }

    suspend fun saveGame(id: Int, currentWeek: Int) {
        gameStatesDao.updateProgress(id, currentWeek, System.currentTimeMillis().toString())
    }

    suspend fun deleteSave(id: Int) {
        gameStatesDao.deleteById(id)
    }

    suspend fun getLastActiveCareerId(): Int? = gameStatesDao.getLastActiveCareerId()

    suspend fun getValidSaveCount(): Int = gameStatesDao.getValidSaveCount()
}
