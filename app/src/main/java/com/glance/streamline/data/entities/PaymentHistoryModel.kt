package com.glance.streamline.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.glance.streamline.domain.model.payment.PaymentHistoryModel
import com.glance.streamline.domain.repository.payment.PaymentRequest
import java.util.*

@Entity
data class PaymentHistoryInfo (
    @PrimaryKey
    val id: String = "",
    val paymentDate: Date = Date(),
    val paymentType: String = "",
    val providerPaymentId: String = "",
    val orderNumber: Int = 0,
    var userName: String = "",
    var isRefunded: Boolean = false,
    var totalAmount: Float = 0.0f,
    var cardAmount: Float = 0.0f,
    var cashAmount: Float = 0.0f,
    var cardRefundAmount: Float = 0.0f,
    var cashRefundAmount: Float = 0.0f,
    var products: List<PaymentHistoryModel.ProductModel> = listOf()
) {

}