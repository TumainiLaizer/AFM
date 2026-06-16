package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.BoardEvaluationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardEvaluationDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM board_evaluation ORDER BY board_satisfaction DESC")
    fun getAll(): Flow<List<BoardEvaluationEntity>>

    @Query("SELECT * FROM board_evaluation WHERE id = :id")
    suspend fun getById(id: Int): BoardEvaluationEntity?

    @Query("SELECT * FROM board_evaluation WHERE manager_id = :managerId")
    suspend fun getByManagerId(managerId: Int): BoardEvaluationEntity?

    @Query("SELECT * FROM board_evaluation WHERE manager_name = :managerName")
    suspend fun getByManagerName(managerName: String): BoardEvaluationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(evaluation: BoardEvaluationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(evaluations: List<BoardEvaluationEntity>)

    @Update
    suspend fun update(evaluation: BoardEvaluationEntity)

    @Delete
    suspend fun delete(evaluation: BoardEvaluationEntity)

    @Query("DELETE FROM board_evaluation")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM board_evaluation")
    suspend fun getCount(): Int

    @Query("SELECT * FROM board_evaluation")
    suspend fun getAllStatic(): List<BoardEvaluationEntity>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM board_evaluation WHERE status = :status ORDER BY board_satisfaction DESC")
    fun getByStatus(status: String): Flow<List<BoardEvaluationEntity>>

    @Query("SELECT * FROM board_evaluation WHERE board_satisfaction <= :threshold")
    fun getPoorPerformers(threshold: Int = 30): Flow<List<BoardEvaluationEntity>>

    @Query("SELECT * FROM board_evaluation WHERE board_satisfaction >= :threshold")
    fun getTopPerformers(threshold: Int = 80): Flow<List<BoardEvaluationEntity>>

    @Query("SELECT * FROM board_evaluation WHERE status IN ('On Thin Ice', 'Critical')")
    fun getManagersAtRisk(): Flow<List<BoardEvaluationEntity>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            be.*,
            m.nationality as manager_nationality,
            m.reputation as manager_reputation,
            t.name as detail_team_name,
            t.logo_path as detail_team_logo,
            t.league as detail_team_league
        FROM board_evaluation be
        LEFT JOIN managers m ON be.manager_id = m.id
        LEFT JOIN teams t ON m.team_id = t.id
        WHERE be.manager_id = :managerId
    """)
    suspend fun getBoardEvaluationWithDetails(managerId: Int): EvaluationWithDetails?

    @Query("""
        SELECT 
            be.*,
            m.nationality as manager_nationality,
            m.reputation as manager_reputation,
            t.name as detail_team_name,
            t.logo_path as detail_team_logo,
            t.league as detail_team_league
        FROM board_evaluation be
        LEFT JOIN managers m ON be.manager_id = m.id
        LEFT JOIN teams t ON m.team_id = t.id
        ORDER BY be.board_satisfaction DESC
    """)
    fun getAllEvaluationsWithDetails(): Flow<List<EvaluationWithDetails>>
}

// ============ DATA CLASSES ============

data class EvaluationWithDetails(
    @Embedded
    val evaluation: BoardEvaluationEntity,

    @ColumnInfo(name = "manager_nationality")
    val managerNationality: String?,

    @ColumnInfo(name = "manager_reputation")
    val managerReputation: Int?,

    @ColumnInfo(name = "detail_team_name")
    val teamName: String?,

    @ColumnInfo(name = "detail_team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "detail_team_league")
    val teamLeague: String?
)
