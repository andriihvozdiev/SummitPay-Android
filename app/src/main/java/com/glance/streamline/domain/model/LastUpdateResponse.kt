package com.glance.streamline.domain.model

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class LastUpdateResponse(
    @SerializedName("last_update")
    val lastUpdate: String = ""
) : Parcelable