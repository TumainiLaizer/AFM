package com.fameafrica.afm.ui.screen.negotiation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.agent.AgentDialogueContext
import com.fameafrica.afm.domain.agent.AgentDialogueEngine
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NegotiationViewModel @Inject constructor(
    private val playerContractsRepository: PlayerContractsRepository,
    private val playerLoansRepository: PlayerLoansRepository,
    private val transfersRepository: TransfersRepository,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val playerAgentsRepository: PlayerAgentsRepository,
    private val agentClientsRepository: AgentClientsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NegotiationUiState())
    val uiState: StateFlow<NegotiationUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    _uiState.update { it.copy(
                        currentWeek = context.week,
                        currentTeamName = context.teamName
                    ) }
                    loadNegotiationData(context.teamId, context.teamName)
                }
            }
        }
    }

    private fun loadNegotiationData(teamId: Int, teamName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                playerContractsRepository.getAllContracts(),
                transfersRepository.getAllTransfersByTeam(teamId),
                playerLoansRepository.getAllLoans()
            ) { contracts, transfers, loans ->
                val teamContracts = contracts.filter { it.teamName == teamName }
                val teamLoans = loans.filter { it.loaningTeam == teamName || it.receivingTeam == teamName }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        contracts = teamContracts,
                        expiringContracts = teamContracts.filter { it.contractStatus == ContractStatus.EXPIRING.value },
                        activeTransfers = transfers.filter {
                            it.transferStatus != TransferStatus.COMPLETED.value && 
                            it.transferStatus != TransferStatus.REJECTED.value &&
                            it.transferStatus != TransferStatus.PENDING.value
                        },
                        pendingTransfers = transfers.filter { it.transferStatus == TransferStatus.PENDING.value },
                        activeLoans = teamLoans.filter { it.status == LoanStatus.ACTIVE.value },
                        pendingLoans = teamLoans.filter { it.status == LoanStatus.PENDING.value }
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message) }
            }.collect()
        }
    }

    fun onEvent(event: NegotiationEvent) {
        when (event) {
            is NegotiationEvent.SelectTransfer -> selectTransfer(event.transferId)
            is NegotiationEvent.UpdateOfferPrice -> updatePrice(event.newAmount)
            is NegotiationEvent.SubmitOffer -> submitOffer()
            is NegotiationEvent.WalkAway -> dismissDialog()
            is NegotiationEvent.ChangeTab -> _uiState.update { it.copy(selectedTab = event.tabIndex) }
            is NegotiationEvent.SelectContract -> selectContract(event.contractId)
            is NegotiationEvent.SelectLoan -> selectLoan(event.loanId)
            else -> { /* Handle remaining events */ }
        }
    }

    private fun selectContract(contractId: Int) {
        viewModelScope.launch {
            val contract = playerContractsRepository.getContractById(contractId)
            val player = contract?.playerId?.let { playersRepository.getPlayerById(it) }
            _uiState.update { 
                it.copy(
                    selectedContract = contract,
                    selectedPlayer = player,
                    showDetailDialog = true
                )
            }
        }
    }

    private fun selectLoan(loanId: Int) {
        viewModelScope.launch {
            val loan = playerLoansRepository.getLoanById(loanId)
            val player = loan?.playerId?.let { playersRepository.getPlayerById(it) }
            _uiState.update { 
                it.copy(
                    selectedLoan = loan,
                    selectedPlayer = player,
                    showDetailDialog = true
                )
            }
        }
    }

    private fun selectTransfer(transferId: Int) {
        viewModelScope.launch {
            val transfer = transfersRepository.getTransferById(transferId) ?: return@launch
            val player = playersRepository.getPlayerById(transfer.playerId)
            val agent = agentClientsRepository.getAgentByPlayerId(transfer.playerId)
            
            _uiState.update { 
                it.copy(
                    selectedTransfer = transfer,
                    selectedPlayer = player,
                    selectedAgent = agent,
                    proposedAmount = transfer.transferFee,
                    showDetailDialog = true,
                    negotiationHistory = listOf(
                        ChatMessage(
                            text = agent?.let { a -> 
                                AgentDialogueEngine.generateDialogue(
                                    AgentDialogueContext(
                                        agentPersonality = a.personality,
                                        playerName = player?.name ?: "Player",
                                        playerHappiness = player?.morale ?: 50,
                                        offerAmount = transfer.transferFee,
                                        expectedAmount = player?.marketValue?.toLong() ?: 0L,
                                        isSuperAgent = playerAgentsRepository.isMarketInfluencer(a),
                                        negotiationRounds = 0
                                    )
                                )
                            } ?: "We are ready to listen to your proposal.",
                            isFromAgent = true
                        )
                    )
                )
            }
        }
    }

    private fun updatePrice(amount: Long) {
        _uiState.update { it.copy(proposedAmount = amount) }
    }

    private fun submitOffer() {
        viewModelScope.launch {
            val state = _uiState.value
            val transfer = state.selectedTransfer ?: return@launch
            val agent = state.selectedAgent
            val rounds = state.negotiationHistory.count { !it.isFromAgent } + 1
            
            val response = agent?.let { 
                playerAgentsRepository.evaluateNegotiationStep(
                    it, state.proposedAmount.toInt(), (state.selectedPlayer?.marketValue ?: 1000000).toInt(), 70
                )
            } ?: PlayerAgentsRepository.NegotiationAction.ACCEPT

            val agentMessage = when(response) {
                PlayerAgentsRepository.NegotiationAction.ACCEPT -> "We have reached an agreement. Excellent business."
                PlayerAgentsRepository.NegotiationAction.COUNTER_OFFER -> "We need a better package than this. Let's talk about more realistic numbers."
                PlayerAgentsRepository.NegotiationAction.THREATEN_EXIT -> "This is a waste of time. My client has better options elsewhere."
                PlayerAgentsRepository.NegotiationAction.WALK_AWAY -> "We are walking away. The valuation is too far apart."
            }

            _uiState.update { 
                it.copy(
                    negotiationHistory = it.negotiationHistory + ChatMessage("I'm offering ${state.proposedAmount}", false) + ChatMessage(agentMessage, true)
                )
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun dismissDialog() = _uiState.update { it.copy(showDetailDialog = false) }
}

data class ChatMessage(val text: String, val isFromAgent: Boolean, val timestamp: Long = System.currentTimeMillis())

data class NegotiationUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val proposedAmount: Long = 0,
    val currentTeamName: String = "",
    val currentWeek: Int = 1,
    val contracts: List<PlayerContractsEntity> = emptyList(),
    val expiringContracts: List<PlayerContractsEntity> = emptyList(),
    val activeTransfers: List<TransfersEntity> = emptyList(),
    val pendingTransfers: List<TransfersEntity> = emptyList(),
    val activeLoans: List<PlayerLoansEntity> = emptyList(),
    val pendingLoans: List<PlayerLoansEntity> = emptyList(),
    val selectedContract: PlayerContractsEntity? = null,
    val selectedTransfer: TransfersEntity? = null,
    val selectedLoan: PlayerLoansEntity? = null,
    val selectedPlayer: PlayersEntity? = null,
    val selectedAgent: PlayerAgentsEntity? = null,
    val negotiationHistory: List<ChatMessage> = emptyList(),
    val showDetailDialog: Boolean = false,
    val snackbarMessage: String? = null
)

data class TransferOffer(
    val playerId: Int = 0,
    val playerName: String = "",
    val type: String = "TRANSFER",
    val fee: Long = 0L,
    val wage: Long = 0L,
    val length: Int = 3,
    val bonus: Long = 0L,
    val releaseClause: Long = 0L
)

data class LoanRequest(
    val playerName: String,
    val playerId: Int,
    val loaningTeam: String,
    val loaningTeamId: Int,
    val receivingTeam: String,
    val receivingTeamId: Int,
    val season: String,
    val durationMonths: Int,
    val loanFee: Long? = null,
    val wageContribution: Int = 100,
    val optionToBuy: Boolean = false,
    val buyOptionFee: Long? = null,
    val recallOption: Boolean = false
)

sealed class NegotiationEvent {
    data class ChangeTab(val tabIndex: Int) : NegotiationEvent()
    data class SelectContract(val contractId: Int) : NegotiationEvent()
    data class SelectTransfer(val transferId: Int) : NegotiationEvent()
    data class SelectLoan(val loanId: Int) : NegotiationEvent()
    data class RenewContract(val contractId: Int, val newSalary: Long, val newLength: Int) : NegotiationEvent()
    data class TerminateContract(val contractId: Int, val reason: String) : NegotiationEvent()
    data class AcceptTransfer(val transferId: Int) : NegotiationEvent()
    data class RejectTransfer(val transferId: Int) : NegotiationEvent()
    data class NegotiateTransfer(val transferId: Int, val newFee: Long) : NegotiationEvent()
    data class CompleteTransfer(val transferId: Int) : NegotiationEvent()
    data class CreateLoan(val loanRequest: LoanRequest) : NegotiationEvent()
    data class TriggerBuyOption(val loanId: Int) : NegotiationEvent()
    data class EarlyReturnLoan(val loanId: Int, val reason: String) : NegotiationEvent()
    data class UpdateOffer(val field: String, val value: String) : NegotiationEvent()
    data class StartNewNegotiation(val playerId: Int, val type: String) : NegotiationEvent()
    data class UpdateOfferPrice(val newAmount: Long) : NegotiationEvent()
    object SubmitOffer : NegotiationEvent()
    object WalkAway : NegotiationEvent()
    object SendCounterOffer : NegotiationEvent()
    object AcceptCounterOffer : NegotiationEvent()
    object RejectCounterOffer : NegotiationEvent()
}
