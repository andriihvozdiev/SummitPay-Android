package com.glance.streamline.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class UserModel(
    val token: String,
    @PrimaryKey val id: Int,
    val user_name: String,
    val role: String,
    val hub: String,
    val devices: List<DeviceModel>,
    val isAdmin: Boolean
)

data class DeviceModel(
    val id: String,
    val name: String
)

@Entity
data class UserLogoutTimeout(
    var timeoutSeconds: Long,
    val startDate: Date
) {
    @PrimaryKey
    var id: Int = 1230

    var timeLeftSeconds: Long = 0
}

@Entity
data class DeviceAssigningInfo(
    @PrimaryKey
    val deviceId: String = "1",
    val location: String,
    val name: String,
    val companyNumber: String,
    var businessId: String = "",
) {
    fun isDeviceAssigned(): Boolean = businessId.isNotBlank()
}
