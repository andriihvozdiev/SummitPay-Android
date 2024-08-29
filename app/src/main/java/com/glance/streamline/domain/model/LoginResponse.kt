package com.glance.streamline.domain.model


import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable
import com.glance.streamline.data.entities.LoginRecordInfo
import com.glance.streamline.utils.extensions.parseRFC_3339
import java.util.*

@Parcelize
data class LoginResponse(
    @SerializedName("jwt")
    val jwt: String = "", // eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc19hZG1pbiI6ZmFsc2UsInVzZXJfaWQiOiIyYzlhYjRjYy1hNmI2LTQzYmUtODE4Ni03ZTQ1YTI3ZDZhOWIifQ.NdxKwUNPHf2_xGQD9-gRNIknScmSR0ISEtWNdoSsxM0
    @SerializedName("role")
    val role: String = "", // operator
    @SerializedName("user_name")
    val user_name: String = "", // Jack
    @SerializedName("id")
    val id: String = "",
    @SerializedName("hub_id")
    val hub_id: String = ""
) : Parcelable

@Parcelize
data class LoginRecord(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("pin_hash")
    val pin_hash: String = "",
    @SerializedName("salt")
    val salt: String = "",
    @SerializedName("role")
    val role: String = "",
    @SerializedName("assigned_to")
    val assignedTo: String = "",
    @SerializedName("expiry_date")
    val expiryDate: String = ""
) : Parcelable {
    fun toLoginRecordInfo() = LoginRecordInfo(
        id,
        name,
        pin_hash,
        salt,
        role,
        assignedTo,
        expiryDate
    )
}