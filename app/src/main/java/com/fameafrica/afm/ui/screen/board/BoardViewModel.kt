package com.fameafrica.afm.ui.screen.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.BoardEvaluationEntity
import com.fameafrica.afm.data.database.entities.BoardRequestsEntity
import com.fameafrica.afm.data.database.entities.ClubVisionEntity
import com.fameafrica.afm.data.database.entities.ObjectivesEntity
import com.fameafrica.afm.data.repository.BoardEvaluationRepository
import com.fameafrica.afm.data.repository.BoardRequestsRepository
import com.fameafrica.afm.data.repository.ClubVisionRepository
import com.fameafrica.afm.data.repository.ObjectivesRepository
import com.fameafrica.afm.data.database.entities.ChairmanEntity
import com.fameafrica.afm.data.repository.ChairmanRepository
import com.fameafrica.afm.data.repository.FinancesRepository
import com.fameafrica.afm.data.repository.TeamFinanceDashboard
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardEvaluationRepository: BoardEvaluationRepository,
    private val boardRequestsRepository: BoardRequestsRepository,
    private val objectivesRepository: ObjectivesRepository,
    private val clubVisionRepository: ClubVisionRepository,
    private val chairmanRepository: ChairmanRepository,
    private val financesRepository: FinancesRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    loadBoardData(context.managerId, context.teamId, context.season)
                }
            }
        }
    }

    private fun loadBoardData(managerId: Int, teamId: Int, season: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val evaluations = boardEvaluationRepository.getAllEvaluations().first()
            val requests = boardRequestsRepository.getRequestsByTeam(teamId).first()
            val objectives = objectivesRepository.getObjectivesByTeam(teamId).first()

            val evaluation = evaluations.find { it.managerId == managerId }
            val vision = clubVisionRepository.getVisionForTeam(teamId).firstOrNull()
            val chairman = chairmanRepository.getChairmanByTeam(teamId)
            val financeDashboard = financesRepository.getTeamFinanceDashboard(teamId, season)
            
            _uiState.update { 
                it.copy(
                    evaluation = evaluation,
                    vision = vision,
                    chairman = chairman,
                    financeDashboard = financeDashboard,
                    requests = requests.filter { it.teamId == teamId },
                    objectives = objectives.filter { it.season == season },
                    isLoading = false
                )
            }
        }
    }

    fun submitRequest(type: String, description: String) {
        viewModelScope.launch {
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                val context = state.context
                val request = BoardRequestsEntity(
                    managerId = context.managerId,
                    managerName = context.managerName,
                    teamId = context.teamId,
                    teamName = context.teamName,
                    requestType = type,
                    requestDescription = description,
                    requestStatus = "Pending"
                )
                boardRequestsRepository.insertRequest(request)
            }
        }
    }
}

data class BoardUiState(
    val evaluation: BoardEvaluationEntity? = null,
    val vision: ClubVisionEntity? = null,
    val chairman: ChairmanEntity? = null,
    val financeDashboard: TeamFinanceDashboard = TeamFinanceDashboard.empty(),
    val requests: List<BoardRequestsEntity> = emptyList(),
    val objectives: List<ObjectivesEntity> = emptyList(),
    val isLoading: Boolean = false
)
