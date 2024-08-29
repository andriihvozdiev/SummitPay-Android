package com.glance.streamline.domain.repository.auth

import android.os.Parcelable
import com.glance.streamline.domain.model.LoginRecord
import com.glance.streamline.domain.model.LoginResponse
import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StreamlineApiInterface {

    @POST("/operator/log-in")
    fun logIn(@Body loginRequest: LoginRequest): Single<LoginResponse>

    @GET("/operator/logins")
    fun getLoginRecords(): Single<ArrayList<LoginRecord>>

    @GET("/device/current_user")
    fun checkLastUserLogin(@Query("device_id") deviceId: String): Completable

    @POST("/device")
    fun createDevice(@Body createDevice: CreateDeviceRequest): Single<CreateDeviceResponse>

}

@Parcelize
data class CreateDeviceResponse(
    @SerializedName("business_id")
    val businessId: String
): Parcelable