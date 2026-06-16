package com.fameafrica.afm.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface BackendApi {
    @POST("payments/initiate")
    suspend fun initiatePayment(@Body request: PaymentInitiationRequest): PaymentInitiationResponse

    @POST("payments/verify")
    suspend fun verifyPayment(@Body request: PaymentVerificationRequest): PaymentVerificationResponse
}

data class PaymentInitiationRequest(
    val productId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val metadata: Map<String, String> = emptyMap()
)

data class PaymentInitiationResponse(
    val transactionId: String,
    val paymentUrl: String?,
    val status: String
)

data class PaymentVerificationRequest(
    val transactionId: String,
    val reference: String? = null
)

data class PaymentVerificationResponse(
    val transactionId: String,
    val status: String, // SUCCESS, FAILED, PENDING
    val coinsAwarded: Long = 0
)
