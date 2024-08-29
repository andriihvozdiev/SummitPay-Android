package com.glance.streamline.domain.model

data class HubItemResponse(
    val hubId: Int,
    val hubLocation: String,
    val users: List<UserItemResponse>,
    val devices: List<DeviceItemResponse>
) : BaseResponse()

data class UserItemResponse(
    val id: Int,
    val user_name: String,
    val role: String,
    val code: String,
    val isAdmin: Boolean
)

data class DeviceItemResponse(
    val deviceId: String,
    val deviceName: String
)