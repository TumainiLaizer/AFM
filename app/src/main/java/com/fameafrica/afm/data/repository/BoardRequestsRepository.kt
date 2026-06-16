package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.BoardRequestsDao
import com.fameafrica.afm.data.database.dao.ManagerRequestStatistics
import com.fameafrica.afm.data.database.dao.RequestTypeStatistics
import com.fameafrica.afm.data.database.entities.BoardRequestsEntity
import com.fameafrica.afm.data.database.entities.BoardRequestStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class BoardRequestsRepository @Inject constructor(
    private val boardRequestsDaoProvider: Provider<BoardRequestsDao>
) {

    private val boardRequestsDao: BoardRequestsDao?
        get() = try {
            boardRequestsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllRequests(): Flow<List<BoardRequestsEntity>> = boardRequestsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getRequestById(id: Int): BoardRequestsEntity? = boardRequestsDao?.getById(id)

    suspend fun insertRequest(request: BoardRequestsEntity) {
        boardRequestsDao?.insert(request)
    }

    suspend fun updateRequest(request: BoardRequestsEntity) {
        boardRequestsDao?.update(request)
    }

    suspend fun deleteRequest(request: BoardRequestsEntity) {
        boardRequestsDao?.delete(request)
    }

    // ============ MANAGER-BASED ============

    fun getRequestsByManager(managerName: String): Flow<List<BoardRequestsEntity>> =
        boardRequestsDao?.getRequestsByManager(managerName) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getPendingRequestsByManager(managerName: String): Flow<List<BoardRequestsEntity>> =
        boardRequestsDao?.getPendingRequestsByManager(managerName) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ TEAM-BASED ============

    fun getRequestsByTeam(teamId: Int): Flow<List<BoardRequestsEntity>> =
        boardRequestsDao?.getRequestsByTeam(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getPendingRequestsByTeam(teamId: Int): Flow<List<BoardRequestsEntity>> =
        boardRequestsDao?.getPendingRequestsByTeam(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ REQUEST MANAGEMENT ============

    suspend fun createRequest(
        managerId: Int,
        managerName: String,
        teamId: Int,
        teamName: String,
        requestType: String,
        description: String
    ): BoardRequestsEntity {
        val request = BoardRequestsEntity(
            requestType = requestType,
            requestDescription = description,
            requestStatus = BoardRequestStatus.PENDING.value,
            managerId = managerId,
            managerName = managerName,
            teamId = teamId,
            teamName = teamName
        )
        boardRequestsDao?.insert(request)
        return request
    }

    suspend fun approveRequest(requestId: Int): Boolean {
        val request = boardRequestsDao?.getById(requestId) ?: return false
        if (request.requestStatus != BoardRequestStatus.PENDING.value) return false

        val updated = request.copy(requestStatus = BoardRequestStatus.APPROVED.value)
        boardRequestsDao?.update(updated)
        return true
    }

    suspend fun rejectRequest(requestId: Int): Boolean {
        val request = boardRequestsDao?.getById(requestId) ?: return false
        if (request.requestStatus != BoardRequestStatus.PENDING.value) return false

        val updated = request.copy(requestStatus = BoardRequestStatus.REJECTED.value)
        boardRequestsDao?.update(updated)
        return true
    }

    suspend fun completeRequest(requestId: Int): Boolean {
        val request = boardRequestsDao?.getById(requestId) ?: return false
        if (request.requestStatus != BoardRequestStatus.APPROVED.value) return false

        val updated = request.copy(requestStatus = BoardRequestStatus.COMPLETED.value)
        boardRequestsDao?.update(updated)
        return true
    }

    // ============ STATISTICS ============

    fun getRequestTypeStatistics(): Flow<List<RequestTypeStatistics>> =
        boardRequestsDao?.getRequestTypeStatistics() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getMostRequestingManagers(limit: Int): Flow<List<ManagerRequestStatistics>> =
        boardRequestsDao?.getMostRequestingManagers(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getPendingCount(): Int = boardRequestsDao?.getPendingCount() ?: 0

    // ============ DASHBOARD ============

    suspend fun getManagerRequestsDashboard(managerName: String): ManagerRequestsDashboard {
        val allRequests = boardRequestsDao?.getRequestsByManager(managerName)?.firstOrNull() ?: emptyList()
        val pending = allRequests.filter { it.requestStatus == BoardRequestStatus.PENDING.value }
        val approved = allRequests.filter { it.requestStatus == BoardRequestStatus.APPROVED.value }
        val rejected = allRequests.filter { it.requestStatus == BoardRequestStatus.REJECTED.value }
        val completed = allRequests.filter { it.requestStatus == BoardRequestStatus.COMPLETED.value }

        val approvalRate = if (allRequests.isNotEmpty()) {
            (approved.size + completed.size).toDouble() / allRequests.size * 100
        } else 0.0

        return ManagerRequestsDashboard(
            totalRequests = allRequests.size,
            pendingRequests = pending.size,
            approvedRequests = approved.size,
            rejectedRequests = rejected.size,
            completedRequests = completed.size,
            approvalRate = approvalRate,
            recentRequests = allRequests.takeLast(5).reversed(),
            pendingRequestsList = pending
        )
    }
}

// ============ DATA CLASSES ============

data class ManagerRequestsDashboard(
    val totalRequests: Int,
    val pendingRequests: Int,
    val approvedRequests: Int,
    val rejectedRequests: Int,
    val completedRequests: Int,
    val approvalRate: Double,
    val recentRequests: List<BoardRequestsEntity>,
    val pendingRequestsList: List<BoardRequestsEntity>
)
