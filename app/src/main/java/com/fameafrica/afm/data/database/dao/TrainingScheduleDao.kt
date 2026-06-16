package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.TrainingDayEntity
import com.fameafrica.afm.data.database.entities.TrainingScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingScheduleDao {

    // ============ SCHEDULES ============

    @Query("SELECT * FROM training_schedules WHERE team_id = :teamId AND month = :month AND year = :year")
    suspend fun getSchedule(teamId: Int, month: Int, year: Int): TrainingScheduleEntity?

    @Query("SELECT * FROM training_schedules WHERE team_id = :teamId ORDER BY year DESC, month DESC")
    fun getAllSchedulesForTeam(teamId: Int): Flow<List<TrainingScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: TrainingScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: TrainingScheduleEntity)

    // ============ DAYS ============

    @Query("SELECT * FROM training_days WHERE schedule_id = :scheduleId ORDER BY date ASC")
    fun getDaysForSchedule(scheduleId: Int): Flow<List<TrainingDayEntity>>

    @Query("SELECT * FROM training_days WHERE date = :date")
    suspend fun getTrainingDaysByDate(date: String): List<TrainingDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<TrainingDayEntity>)

    @Query("DELETE FROM training_days WHERE schedule_id = :scheduleId")
    suspend fun deleteDaysBySchedule(scheduleId: Int)

    // ============ CROSS-TABLE ============

    @Transaction
    @Query("SELECT * FROM training_days WHERE date = :date")
    suspend fun getTrainingWithScheduleByDate(date: String): List<TrainingDayWithSchedule>
}

data class TrainingDayWithSchedule(
    @Embedded val day: TrainingDayEntity,
    @Relation(
        parentColumn = "schedule_id",
        entityColumn = "id"
    )
    val schedule: TrainingScheduleEntity
)
