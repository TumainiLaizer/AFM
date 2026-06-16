package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ScoutingMissionsDao
import com.fameafrica.afm.data.database.entities.MissionStatus
import com.fameafrica.afm.data.database.entities.MissionType
import com.fameafrica.afm.data.database.entities.ScoutingMissionsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ScoutingMissionsRepository @Inject constructor(
    private val scoutingMissionsDaoProvider: Provider<ScoutingMissionsDao>,
    private val staffRepository: StaffRepository,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository
) {
    private val scoutingMissionsDao get() = scoutingMissionsDaoProvider.get()
    fun getAllMissions(): Flow<List<ScoutingMissionsEntity>> = scoutingMissionsDao.getAll()

    fun getMissionsByScout(scoutId: Int): Flow<List<ScoutingMissionsEntity>> = 
        scoutingMissionsDao.getByScout(scoutId)

    fun getActiveMissions(): Flow<List<ScoutingMissionsEntity>> = 
        scoutingMissionsDao.getActiveMissions()

    suspend fun assignMission(
        scoutId: Int,
        type: MissionType,
        target: String,
        priority: String = "Normal"
    ): Boolean {
        // Check if scout already has an active mission
        val existing = scoutingMissionsDao.getActiveMissionByScout(scoutId)
        if (existing != null) return false

        val scout = staffRepository.getStaffById(scoutId) ?: return false

        val mission = ScoutingMissionsEntity(
            scoutId = scoutId,
            scoutName = scout.name,
            missionType = type.value,
            targetIdentifier = target,
            priority = priority,
            status = MissionStatus.ACTIVE.value
        )
        
        scoutingMissionsDao.insert(mission)
        return true
    }

    suspend fun cancelMission(missionId: Int) {
        scoutingMissionsDao.updateStatus(missionId, MissionStatus.CANCELLED.value, System.currentTimeMillis())
    }

    suspend fun completeMission(missionId: Int) {
        scoutingMissionsDao.updateStatus(missionId, MissionStatus.COMPLETED.value, System.currentTimeMillis())
    }

    /**
     * Process active missions weekly.
     * Domestic missions (same region) get a 1.5x efficiency boost.
     */
    suspend fun processWeeklyMissions() {
        val activeMissionsList = scoutingMissionsDao.getActiveMissions().firstOrNull() ?: return
        
        for (mission in activeMissionsList) {
            val scout = staffRepository.getStaffById(mission.scoutId) ?: continue
            val team = teamsRepository.getTeamById(scout.teamId) ?: continue
            
            // Domestic preference: Faster results in home region/country
            val isDomestic = when (mission.missionType) {
                MissionType.REGION.value -> mission.targetIdentifier == team.region
                MissionType.COUNTRY.value -> mission.targetIdentifier == team.country
                else -> false
            }
            
            val baseChance = (scout.impactRating / 5) + 10 // 20-30% base
            val findChance = if (isDomestic) (baseChance * 1.5).toInt() else baseChance
            
            if (Random.nextInt(100) < findChance) {
                scoutingMissionsDao.incrementFoundCount(mission.id)
            }
            
            // Auto-complete missions after 4 weeks (simplified)
            val durationWeeks = (System.currentTimeMillis() - mission.startDate) / (1000 * 60 * 60 * 24 * 7)
            if (durationWeeks >= 4L) {
                completeMission(mission.id)
            }
        }
    }
}
