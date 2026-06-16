package com.fameafrica.afm.ui.screen.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.manager.PaymentGatewayManager
import com.fameafrica.afm.domain.manager.PaymentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val gameManager: GameManager,
    private val paymentGatewayManager: PaymentGatewayManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    fun onBundleSelected(bundle: ShopBundle) {
        _uiState.update { it.copy(selectedBundle = bundle, showPaymentOptions = true) }
    }

    fun onPaymentMethodSelected(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun processPayment(method: String, details: Map<String, String> = emptyMap()) {
        val bundle = _uiState.value.selectedBundle ?: return
        val gameState = gameManager.gameState.value
        if (gameState !is GameManager.GameState.Active) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val result = paymentGatewayManager.processPayment(
                method = method,
                amount = if (method == "AZAMPESA") bundle.priceTZS.toDouble() else bundle.priceUSD,
                currency = if (method == "AZAMPESA") "TZS" else "USD",
                productId = bundle.id,
                coinsAwarded = bundle.coins,
                metadata = details
            )
            
            _uiState.update { 
                it.copy(
                    isProcessing = false,
                    paymentResult = result
                )
            }
        }
    }

    fun resetState() {
        _uiState.update { ShopUiState() }
    }
}

data class ShopUiState(
    val isProcessing: Boolean = false,
    val selectedBundle: ShopBundle? = null,
    val showPaymentOptions: Boolean = false,
    val selectedPaymentMethod: String? = null,
    val paymentResult: PaymentResult? = null
)

data class ShopBundle(
    val id: String,
    val name: String,
    val coins: Long,
    val priceTZS: Long,
    val priceUSD: Double,
    val description: String,
    val badge: String? = null
)

val coinBundles = listOf(
    ShopBundle("coin_starter", "Starter Pack", 500, 2500, 0.99, "Perfect for a quick morale boost"),
    ShopBundle("coin_bronze", "Bronze Pack", 2500, 12000, 4.99, "Help your club grow faster"),
    ShopBundle("coin_silver", "Silver Pack", 6000, 24000, 9.99, "Major investment for your empire", "Best Value"),
    ShopBundle("coin_gold", "Gold Pack", 15000, 60000, 24.99, "The ultimate manager resource", "Recommended"),
    ShopBundle("coin_mega", "Mega African Pack", 40000, 150000, 59.99, "Dominate the continent")
)
