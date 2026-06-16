package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.TrainingScheduleDao
import com.fameafrica.afm.data.database.entities.TrainingDayEntity
import com.fameafrica.afm.data.database.entities.TrainingScheduleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider
import com.fameafrica.afm.data.database.dao.TrainingDayWithSchedule

@Singleton
class TrainingRepository @Inject constructor(
    private val trainingScheduleDaoProvider: Provider<TrainingScheduleDao>
) {
    private val dao get() = trainingScheduleDaoProvider.get()

    suspend fun getSchedule(teamId: Int, month: Int, year: Int): TrainingScheduleEntity? =
        dao.getSchedule(teamId, month, year)

    fun getAllSchedulesForTeam(teamId: Int): Flow<List<TrainingScheduleEntity>> =
        dao.getAllSchedulesForTeam(teamId)

    suspend fun createSchedule(schedule: TrainingScheduleEntity): Long =
        dao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: TrainingScheduleEntity) =
        dao.updateSchedule(schedule)

    fun getDaysForSchedule(scheduleId: Int): Flow<List<TrainingDayEntity>> =
        dao.getDaysForSchedule(scheduleId)

    suspend fun getTrainingWithScheduleByDate(date: String): List<TrainingDayWithSchedule> =
        dao.getTrainingWithScheduleByDate(date)

    suspend fun insertDays(days: List<TrainingDayEntity>) =
        dao.insertDays(days)

    suspend fun deleteDaysBySchedule(scheduleId: Int) =
        dao.deleteDaysBySchedule(scheduleId)
}
