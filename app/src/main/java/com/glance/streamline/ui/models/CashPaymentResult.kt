package com.glance.streamline.ui.models

import com.glance.streamline.domain.repository.payment.PaymentRequest

data class CashPaymentResult(
    val price: Float,
    val cashPaymentRequest: PaymentRequest
)

object PaymentResultType {
    const val CASH_PAYMENT_TYPE = 0
    const val CARD_PAYMENT_TYPE = 1
    const val REFUND_PAYMENT_TYPE = 2
}