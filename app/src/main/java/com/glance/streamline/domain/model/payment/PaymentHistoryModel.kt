package com.glance.streamline.domain.model.payment

import android.os.Parcelable
import com.glance.streamline.data.entities.PaymentHistoryInfo
import com.glance.streamline.utils.extensions.parseRFC3339Nano
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class PaymentHistoryModel(
    @SerializedName("id")
    val id: String = "", // 5f661b5f-6e01-4dcd-aecb-550e8885d293
    @SerializedName("payment_date")
    val paymentDate: String = "", // 2021-09-13T16:13:35.742Z
    @SerializedName("payment_type")
    val paymentType: String = "", // card/cash
    @SerializedName("provider_payment_id")
    val providerPaymentId: String = "", //
    @SerializedName("order_number")
    val orderNumber: Int = 0,
    @SerializedName("user_name")
    val userName: String = "", // Test Account
    @SerializedName("is_refunded")
    val isRefunded: Boolean = false, // false
    @SerializedName("total_amount")
    val totalAmount: Float = 0.0f, // 3
    @SerializedName("card_amount")
    val cardAmount: Float = 0.0f, // 0.0
    @SerializedName("cash_amount")
    val cashAmount: Float = 0.0f, // 3.0
    @SerializedName("card_refund_amount")
    val cardRefundAmount: Float = 0.0f, // 0.0
    @SerializedName("cash_refund_amount")
    val cashRefundAmount: Float = 0.0f, // 3.0
    @SerializedName("products")
    val products: List<ProductModel> = listOf(),
) : Parcelable {

    fun toPaymentHistoryInfo() : PaymentHistoryInfo {
        val date: Date = Date().parseRFC3339Nano(paymentDate) ?: Date()
        return PaymentHistoryInfo(
            id,
            date,
            paymentType,
            providerPaymentId,
            orderNumber,
            userName,
            isRefunded,
            totalAmount,
            cardAmount,
            cashAmount,
            cardRefundAmount,
            cashRefundAmount,
            products
        )
    }

    @Parcelize
    data class ProductModel(
        @SerializedName("id")
        val id: String = "", // 26ab8598-fc73-40ea-a401-bdb6764a64b0
        @SerializedName("product_name")
        val productName: String = "", // Test C1
        @SerializedName("product_tax")
        val productTax: Int = 0, // 20
        @SerializedName("product_retail_price")
        val productRetailPrice: String = "", // "3"
        @SerializedName("is_refunded")
        var isRefunded: Boolean = false, // false
        @SerializedName("options")
        val options: List<ProductOptionModel> = listOf(), // []
    ) : Parcelable {

        @Parcelize
        data class ProductOptionModel(
            @SerializedName("id")
            val id: String = "", // 377d31f5-d9a7-41b5-9296-62e06d8807ae
            @SerializedName("option_name")
            val optionName: String = "", // Double Up
            @SerializedName("option_price")
            val optionPrice: String = "", // "1.50"
            @SerializedName("is_refunded")
            var isRefunded: Boolean = false, // false
        ) : Parcelable {

        }
    }
}
