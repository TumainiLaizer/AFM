package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.api.BackendApi
import com.fameafrica.afm.data.api.PaymentInitiationRequest
import com.fameafrica.afm.data.api.PaymentVerificationRequest
import com.fameafrica.afm.data.database.entities.PurchaseHistoryEntity
import com.fameafrica.afm.data.repository.PurchaseHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentGatewayManager @Inject constructor(
    private val backendApi: BackendApi,
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
    private val economyManager: EconomyManager,
    private val gameManager: GameManager
) {

    suspend fun processPayment(
        method: String,
        amount: Double,
        currency: String,
        productId: String,
        coinsAwarded: Long,
        metadata: Map<String, String> = emptyMap()
    ): PaymentResult {
        return try {
            val response = backendApi.initiatePayment(
                PaymentInitiationRequest(
                    productId = productId,
                    amount = amount,
                    currency = currency,
                    paymentMethod = method,
                    metadata = metadata
                )
            )

            // Log pending transaction
            val purchase = PurchaseHistoryEntity(
                transactionId = response.transactionId,
                bundleId = productId,
                currencyType = "COINS",
                amount = amount.toLong(),
                paymentMethod = method,
                verificationStatus = "PENDING"
            )
            purchaseHistoryRepository.insertPurchase(purchase)

            if (response.status == "SUCCESS") {
                verifyTransaction(response.transactionId, coinsAwarded)
            } else {
                PaymentResult.Pending(response.transactionId, response.paymentUrl)
            }
        } catch (e: Exception) {
            PaymentResult.Error(e.message ?: "Failed to initiate payment")
        }
    }

    suspend fun verifyTransaction(transactionId: String, expectedCoins: Long): PaymentResult {
        return try {
            val response = backendApi.verifyPayment(PaymentVerificationRequest(transactionId))
            
            if (response.status == "SUCCESS") {
                val state = gameManager.gameState.value
                if (state is GameManager.GameState.Active) {
                    economyManager.addCoins(state.context.teamId, state.context.season, response.coinsAwarded.takeIf { it > 0 } ?: expectedCoins)
                }
                purchaseHistoryRepository.updateStatus(transactionId, "SUCCESS")
                PaymentResult.Success(transactionId, response.coinsAwarded)
            } else if (response.status == "FAILED") {
                purchaseHistoryRepository.updateStatus(transactionId, "FAILED")
                PaymentResult.Error("Payment verification failed")
            } else {
                PaymentResult.Pending(transactionId)
            }
        } catch (e: Exception) {
            PaymentResult.Error(e.message ?: "Verification error")
        }
    }
}
