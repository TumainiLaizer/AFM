package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_history")
data class PurchaseHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "transaction_id")
    val transactionId: String,

    @ColumnInfo(name = "bundle_id")
    val bundleId: String,

    @ColumnInfo(name = "currency_type")
    val currencyType: String, // COINS, CASH, COMBO

    @ColumnInfo(name = "amount")
    val amount: Long,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String, // VISA, MASTERCARD, AZAMPESA

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "verification_status")
    val verificationStatus: String // PENDING, SUCCESS, FAILED
)
