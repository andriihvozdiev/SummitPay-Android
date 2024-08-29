package com.glance.streamline.domain.model.payment

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentHistoryResponse(
    @SerializedName("pages_count")
    val pagesCount: Int = 0, // 1
    @SerializedName("values")
    val values: ArrayList<PaymentHistoryModel>?
) : Parcelable