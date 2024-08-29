package com.glance.streamline.data.converters

import androidx.room.TypeConverter
import com.glance.streamline.domain.model.payment.PaymentHistoryModel
import com.glance.streamline.utils.extensions.fromJson
import com.glance.streamline.utils.extensions.toJson

class PaymentHistoryInfoConverters {

    @TypeConverter
    fun fromProductModel(product: List<PaymentHistoryModel.ProductModel>): String = product.toJson()

    @TypeConverter
    fun toProductModel(productJson: String): List<PaymentHistoryModel.ProductModel> =
        fromJson<List<PaymentHistoryModel.ProductModel>>(productJson) ?: arrayListOf()
}