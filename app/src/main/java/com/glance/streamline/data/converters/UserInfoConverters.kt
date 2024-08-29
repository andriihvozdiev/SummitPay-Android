package com.glance.streamline.data.converters

import androidx.room.TypeConverter
import com.glance.streamline.data.entities.DeviceModel
import com.glance.streamline.utils.extensions.fromJson
import com.glance.streamline.utils.extensions.toJson
import java.util.*

class UserInfoConverters {
    @TypeConverter
    fun fromDevicesList(devices: List<DeviceModel>): String = devices.toJson()

    @TypeConverter
    fun toDevicesList(devicesJson: String): List<DeviceModel> =
        fromJson<List<DeviceModel>>(devicesJson) ?: arrayListOf()

    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}