package com.glance.streamline.domain.model


import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class ProductLayoutsResponse(
    @SerializedName("pages_count")
    val pagesCount: Int = 0, // 1
    @SerializedName("values")
    val values: ArrayList<ProductLayout>?
) : Parcelable