package com.glance.streamline.utils.payment

import com.SafeWebServices.PaymentGateway.PGCard
import com.SafeWebServices.PaymentGateway.PGEncrypt
import com.SafeWebServices.PaymentGateway.PGEncryptedSwipedCard
import com.SafeWebServices.PaymentGateway.PGKeyedCard
import com.glance.streamline.BuildConfig

object PaymentGatewayEncryption {
    private val encryption by lazy {
        PGEncrypt().apply {
            setKey(BuildConfig.PAYMENT_ENCRIPTION_KEY)
        }
    }

    fun setupPaymentEncryption(cardNumber: String, expiration: String, cvv: String): String {
        val cardData = PGKeyedCard(cardNumber, expiration, cvv)
        val includeCVV = true
        return encryption.encrypt(cardData, includeCVV)
    }

    fun setupPaymentEncryption(card: PGCard): String {
        val includeCVV = true
        return encryption.encrypt(PGEncryptedSwipedCard(), includeCVV)
    }
}
