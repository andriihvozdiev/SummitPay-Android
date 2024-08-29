package com.glance.streamline.data.converters

import androidx.room.TypeConverter
import com.glance.streamline.domain.repository.payment.PaymentRequest
import com.glance.streamline.utils.extensions.fromJson
import com.glance.streamline.utils.extensions.toJson

class ReportInfoConverters {

    @TypeConverter
    fun fromRefusalLog(refusalLog: PaymentRequest.RefusalLogRequest): String = refusalLog.toJson()

    @TypeConverter
    fun toRefusalLog(refusalLogJson: String): PaymentRequest.RefusalLogRequest =
        fromJson<PaymentRequest.RefusalLogRequest>(refusalLogJson) ?: PaymentRequest.RefusalLogRequest()

    @TypeConverter
    fun fromProductsList(products: List<PaymentRequest.Product>): String = products.toJson()

    @TypeConverter
    fun toProductsList(productsJson: String): List<PaymentRequest.Product> =
        fromJson<List<PaymentRequest.Product>>(productsJson) ?: arrayListOf()
}