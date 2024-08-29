package com.glance.streamline.domain.repository.payment

import com.glance.streamline.BuildConfig
import com.glance.streamline.domain.model.payment.PaymentHistoryResponse
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface PaymentApiInterface {

//    @POST("${BuildConfig.PAYMENT_URL}/api/transact.php")
//    fun sendCardTransaction(@QueryMap request: HashMap<String, Any>): Single<ResponseBody>

    @POST("/report/purchase")
    fun purchase(@Header("Authorization") token: String, @Body paymentRequest: PaymentRequest): Completable

    @GET("/payment")
    fun getPaymentHistory(
        @Query("device_token") deviceId: String,
        @Query("page") page: Int? = 0,
        @Query("amount") amount: Int? = 10
    ): Single<PaymentHistoryResponse>

    @POST("/payment/refund")
    fun refund(@Header("Authorization") token: String, @Body refundRequest: RefundRequest): Completable
}