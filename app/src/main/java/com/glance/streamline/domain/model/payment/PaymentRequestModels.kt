package com.glance.streamline.domain.model.payment

import com.google.gson.annotations.SerializedName

data class CardTransactionRequest(
    val type: TransactionType,
    val securityKey: String,
    val encryptedData: String?,
    @SerializedName("ccnumber")
    val cardNumber: String?,
    @SerializedName("ccexp")
    val cardExpirationDate: String?, //Format: MMYY
    val cvv: String?,
    val amount: Float //Format: x.xx
)

enum class TransactionType {
    @SerializedName("sale") SALE,
    @SerializedName("auth") AUTH,
    @SerializedName("credit") CREDIT,
    @SerializedName("validate") VALIDATE,
    @SerializedName("offline") OFFLINE
}