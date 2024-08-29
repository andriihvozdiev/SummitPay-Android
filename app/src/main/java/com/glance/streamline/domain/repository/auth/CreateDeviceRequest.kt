package com.glance.streamline.domain.repository.auth


import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class CreateDeviceRequest(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("location")
    val location: String = "",
    @SerializedName("device_token")
    val deviceToken: String = "",
    @SerializedName("company_ref_num")
    val companyRegNum: String = ""
) : Parcelable