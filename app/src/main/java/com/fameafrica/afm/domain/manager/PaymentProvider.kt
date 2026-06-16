package com.fameafrica.afm.domain.manager

interface PaymentProvider {
    suspend fun initiatePayment(
        amount: Double,
        currency: String,
        productId: String,
        metadata: Map<String, String> = emptyMap()
    ): PaymentResult

    suspend fun verifyPayment(transactionId: String): PaymentResult
}

sealed class PaymentResult {
    data class Success(val transactionId: String, val coinsAwarded: Long = 0) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
    data class Pending(val transactionId: String, val paymentUrl: String? = null) : PaymentResult()
}
