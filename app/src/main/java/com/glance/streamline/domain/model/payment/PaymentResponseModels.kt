package com.glance.streamline.domain.model.payment

import androidx.annotation.StringRes
import com.glance.streamline.R
import com.google.gson.annotations.SerializedName

data class PaymentResultModel(
    val responseType: PaymentResponseType,
    val cardNumber: String,
    val cardType: String,
    val authCode: String,
    val amount: Float,
    val orderNumber: String
)

data class PaymentResponseModel(
    val response: PaymentResponseType,
    @SerializedName("responsetext")
    val responseText: String,
    @SerializedName("authcode")
    val authCode: String,
    @SerializedName("transactionid")
    val transactionId: String,
    @SerializedName("avsresponse")
    val avsResponse: String?,
    @SerializedName("cvvresponse")
    val cvvResponse: CvvResponseType?,
    @SerializedName("orderid")
    val orderId: String?,
    val responseCode: Int
)

enum class PaymentResponseType {
    @SerializedName("1") APPROVED,
    @SerializedName("2") DECLINED,
    @SerializedName("3") ERROR
}

enum class CvvResponseType(@StringRes val messageId: Int) {
    @SerializedName("M") CVV_MATCH(R.string.payment_error_cvv_message_m),
    @SerializedName("N") CVV_NOT_MATCH(R.string.payment_error_cvv_message_n),
    @SerializedName("P") NOT_PROCESSED(R.string.payment_error_cvv_message_p),
    @SerializedName("S") CVV_NOT_PRESENT_ON_CARD(R.string.payment_error_cvv_message_s),
    @SerializedName("U") ISSUER_NOT_CERTIFIED(R.string.payment_error_cvv_message_u)
}