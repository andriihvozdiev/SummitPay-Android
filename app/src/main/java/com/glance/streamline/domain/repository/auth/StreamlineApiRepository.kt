package com.glance.streamline.domain.repository.auth

import android.content.Context
import com.glance.streamline.R
import com.glance.streamline.domain.model.HubItemResponse
import com.glance.streamline.domain.model.LoginResponse
import com.glance.streamline.domain.model.UserItemResponse
import com.glance.streamline.domain.repository.BaseRepository
import com.glance.streamline.data.entities.DeviceModel
import com.glance.streamline.data.entities.UserModel
import com.glance.streamline.domain.model.LoginRecord
import com.glance.streamline.utils.extensions.android.Failure
import com.glance.streamline.utils.extensions.android.Result
import com.glance.streamline.utils.extensions.android.Success
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import com.glance.streamline.utils.extensions.fromJson
import com.glance.streamline.utils.extensions.getSchedulers
import com.glance.streamline.utils.inputStreamToString
import com.google.gson.Gson
import javax.inject.Inject

class StreamlineApiRepository @Inject constructor(
    context: Context,
    private val streamlineApiInterface: StreamlineApiInterface,
    private val gson: Gson
) : BaseRepository(context) {

    class UserNotFoundError<T> : Failure<T>("User not found", 404)
    class ExceededAttemptsError<T>(val value: T?) :
        Failure<T>("You have exceeded the maximum number of attempts", 403)

    private val maxLoginAttempts = 3

//    fun login(
//        code: String, unSuccessfulAttemptsCount: Int
//    ): Single<Result<UserModel>> {
//        return Single
//            .timer(1000, TimeUnit.MILLISECONDS)
//            .getSchedulers()
//            .map { parseLocalJsonData() }
//            .map { it.findUserByCode(code, unSuccessfulAttemptsCount) }
//    }

    fun login(
        code: String,
        bussinesId: String,
        deviceToken: String, //TODO:
        onResponse: (Result<LoginResponse>) -> Unit
    ) = streamlineApiInterface.logIn(LoginRequest(code, bussinesId, deviceToken)).getSchedulers()
        .getWrapped(onResponse)

    fun getLoginRecords(
        onResponse: (Result<ArrayList<LoginRecord>>) -> Unit
    ) = streamlineApiInterface.getLoginRecords().getSchedulers().getWrapped(onResponse)

    fun createDevice(
        name: String,
        location: String,
        companyRegNum: String = "",
        deviceToken: String = context.getUniqCurrentDeviceId(),
        onResponse: (
            Result<CreateDeviceResponse>
        ) -> Unit
    ) = streamlineApiInterface.createDevice(
        CreateDeviceRequest(
            name, location, deviceToken, companyRegNum
        )
    ).getSchedulers().getWrapped(onResponse)

//    private fun List<HubItemResponse>.findUserByCode(
//        code: String, unSuccessfulAttemptsCount: Int
//    ): Result<UserModel> {
//        val isExceededAttempts = unSuccessfulAttemptsCount >= maxLoginAttempts
//
//        val user = map {
//            it.users.find { !isExceededAttempts && it.code == code || isExceededAttempts && it.code == code && it.isAdmin }
//                ?.map(it)
//        }.find { it != null }
//
//        val isLastAttempt = unSuccessfulAttemptsCount == maxLoginAttempts - 1 && user == null
//
//        return if (isLastAttempt || isExceededAttempts) ExceededAttemptsError(user)
//        else if (user == null) {
//            UserNotFoundError()
//        } else Success(user)
//
//    }

    fun checkLastUserLogin(
        deviceId: String
    ) = streamlineApiInterface.checkLastUserLogin(deviceId).getSchedulers()

//    private fun UserItemResponse.map(hub: HubItemResponse) =
//        UserModel("", id, user_name, role, hub.hubLocation, hub.devices.map { device ->
//            DeviceModel(device.deviceId, device.deviceName)
//        }, isAdmin)

    private fun parseLocalJsonData(): ArrayList<HubItemResponse>? {
        val myJson = inputStreamToString(context.resources.openRawResource(R.raw.hubs_list))
        return gson.fromJson<ArrayList<HubItemResponse>>(myJson)
    }

}

