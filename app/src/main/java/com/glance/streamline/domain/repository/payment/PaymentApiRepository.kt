package com.glance.streamline.domain.repository.payment

import android.content.Context
import android.net.Uri
import android.util.Log
import com.glance.streamline.R
import com.glance.streamline.di.modules.RetrofitApiModule
import com.glance.streamline.domain.model.ProductLayoutsResponse
import com.glance.streamline.domain.model.payment.CardTransactionRequest
import com.glance.streamline.domain.model.payment.PaymentHistoryResponse
import com.glance.streamline.domain.model.payment.PaymentResponseModel
import com.glance.streamline.domain.model.payment.PaymentResponseType
import com.glance.streamline.domain.repository.ApiErrors
import com.glance.streamline.domain.repository.BaseRepository
import com.glance.streamline.utils.extensions.android.Failure
import com.glance.streamline.utils.extensions.android.Result
import com.glance.streamline.utils.extensions.android.Success
import com.glance.streamline.utils.extensions.android.getSharedPref
import com.glance.streamline.utils.extensions.fromJson
import com.glance.streamline.utils.extensions.getSchedulers
import com.glance.streamline.utils.extensions.toJson
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import java.net.URI
import javax.inject.Inject


class PaymentApiRepository @Inject constructor(
    context: Context,
    private val paymentApiInterface: PaymentApiInterface,
    private val gson: Gson
) : BaseRepository(context) {

    fun purchase(request: PaymentRequest/*, onResponse: (Result<Any>) -> Unit*/): Completable {
        val token: String? by lazy { context.getSharedPref()?.getString(RetrofitApiModule.JWT_TOKEN_KEY, null) }
        return paymentApiInterface.purchase("Bearer "+token, request).getSchedulers()/*.getWrapped(onResponse)*/
    }

    fun getPaymentHistories(deviceId: String, page: Int? = 0, amount: Int? = 30, onResponse: (Result<PaymentHistoryResponse>) -> Unit)
            = paymentApiInterface.getPaymentHistory(deviceId, page, amount).getSchedulers().getWrapped(onResponse)

    fun refund(request: RefundRequest): Completable {
        val token: String? by lazy { context.getSharedPref()?.getString(RetrofitApiModule.JWT_TOKEN_KEY, null) }
        return paymentApiInterface.refund("Bearer "+token, request).getSchedulers()
    }
}