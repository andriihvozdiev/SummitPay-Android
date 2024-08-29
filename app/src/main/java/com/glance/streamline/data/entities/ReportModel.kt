package com.glance.streamline.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.glance.streamline.domain.repository.payment.PaymentRequest
import com.glance.streamline.ui.models.PaymentResultType

@Entity
data class ReportInfo(
    @PrimaryKey
    val paymentDate: String = "", // date-string
    val lastDigits: String = "",
    val deviceToken: String = "",
    val refusalLog: PaymentRequest.RefusalLogRequest = PaymentRequest.RefusalLogRequest(),
    val products: List<PaymentRequest.Product> = listOf(),
    val providerPaymentId: String = "",
    val cardAmount: Float = 0f,
    val cashAmount: Float = 0f
) {
    fun convertPaymentRequest(): PaymentRequest {
        return PaymentRequest(
            paymentDate,
            lastDigits,
            deviceToken,
            refusalLog,
            products,
            providerPaymentId,
            cardAmount,
            cashAmount
        )
    }

}

@Entity
data class ZReportInfo(
    @PrimaryKey
    val paymentDate: String = "", // date-string
    val paymentType: Int = PaymentResultType.CASH_PAYMENT_TYPE, // 0 -> cash, 1 -> card
    val price: Float = 0.0f,
    val itemCount: Int = 0,
)
