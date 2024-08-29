package com.glance.streamline.domain.repository.payment


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RefundRequest(
    @SerializedName("payment_id")
    var paymentId: String = "",
    @SerializedName("product_ids")
    val productIds: ArrayList<String> = arrayListOf(),
    @SerializedName("option_ids")
    val optionIds: List<String> = listOf(),
    @SerializedName("cash_amount")
    var cashAmount: Float = 0f,
    @SerializedName("card_amount")
    var cardAmount: Float = 0f
) : Parcelable {

}