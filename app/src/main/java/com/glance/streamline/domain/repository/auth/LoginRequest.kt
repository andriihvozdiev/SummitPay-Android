package com.glance.streamline.domain.repository.auth


import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class LoginRequest(
    @SerializedName("pin_code")
    val pinCode: String = "", // 1111
    @SerializedName("business_id")
    val businessId: String = "",
    @SerializedName("device_token")
    val deviceToken: String = ""
) : Parcelable