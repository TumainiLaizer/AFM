package com.fameafrica.afm.ui.screen.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.SponsorshipDealEntity
import com.fameafrica.afm.data.repository.SponsorshipDealRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class SponsorshipViewModel @Inject constructor(
    private val sponsorshipDealRepository: SponsorshipDealRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SponsorshipUiState())
    val uiState: StateFlow<SponsorshipUiState> = _uiState.asStateFlow()

    fun generateOffers() {
        val gameState = gameManager.gameState.value
        if (gameState !is GameManager.GameState.Active) return
        
        val teamId = gameState.context.teamId
        // Logic to generate offers based on reputation
        val offers = listOf(
            SponsorOffer("Global Air", "SHIRT", 500000, 12, "Win the League"),
            SponsorOffer("Local Bank", "STADIUM", 200000, 24, "Reach Cup Semi-final"),
            SponsorOffer("Telco Plus", "MEDIA", 100000, 6, "3 Clean Sheets")
        )
        _uiState.update { it.copy(availableOffers = offers) }
    }

    fun acceptOffer(offer: SponsorOffer) {
        val gameState = gameManager.gameState.value
        if (gameState !is GameManager.GameState.Active) return

        viewModelScope.launch {
            val deal = SponsorshipDealEntity(
                teamId = gameState.context.teamId,
                sponsorName = offer.name,
                type = offer.type,
                durationMonths = offer.duration,
                startDate = gameState.context.currentDate,
                endDate = "2027-06-01", // Mock calculation
                payoutPerMonth = offer.payout,
                objectives = offer.objective
            )
            sponsorshipDealRepository.insertDeal(deal)
            _uiState.update { it.copy(availableOffers = it.availableOffers.filter { o -> o != offer }) }
        }
    }
}

data class SponsorshipUiState(
    val availableOffers: List<SponsorOffer> = emptyList()
)

data class SponsorOffer(
    val name: String,
    val type: String,
    val payout: Long,
    val duration: Int,
    val objective: String
)
