package com.glance.streamline.domain.repository

import android.content.Context
import com.glance.streamline.R
import com.glance.streamline.StreamlineApp
import com.glance.streamline.domain.model.BaseResponse
import com.glance.streamline.utils.extensions.android.*
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.ref.WeakReference
import javax.inject.Inject

class CallbackWrapper<T, V>(
    private val contextWeakReference: WeakReference<Context>? = null,
    private var onResult: (Result<V>) -> Unit
) {

    @Inject //@field:Named(RetrofitApiModule.MAIN_RETROFIT)
    lateinit var retrofit: Retrofit

    var request: Single<T>? = null
    private var repeatingUnauthorizedRequestCount = 0

    init {
        contextWeakReference?.get()?.let {
            StreamlineApp[it].appComponent.inject(this as CallbackWrapper<Any, Any>)
        }
    }

    private var wasResent = false

    fun onSuccess(response: T) {
        onResult(Success(response as V, null, wasResent))
    }

    fun onError(t: Throwable) {
        var errorMessage: String = ""
        var errorCode: Int = 666
        wasResent = false

        if (t is IOException) {
            errorMessage =
                contextWeakReference?.get()?.getString(R.string.error_no_internet_connection) ?: ""
            errorCode =
                ApiErrors.ERROR_INTERNET_CONNECTION_CODE.code
            request?.let {
                wasResent = true
                onResult(ConnectionError(it as Single<V>))
                return
            }
        } else if (t is HttpException) {
            when (t.code()) {
                ApiErrors.ERROR_UNAUTHORIZED.code -> {
                    request?.let {
                        wasResent = true
                        onResult(AuthError(it as Single<V>))
                        return
                    }
                    return
                }
                else -> {
                    val errorWrapper = ErrorWrapper()
                    val error = errorWrapper.parseError(retrofit, t.response() ?: return)
                    if (error != null) {
                        errorMessage = error.error ?: ""
                        errorCode = t.code()
                    } else {
                        errorMessage =
                            contextWeakReference?.get()?.getString(R.string.error_unexpected) ?: ""
                        errorCode =
                            ApiErrors.ERROR_UNEXPECTED_CODE.code
                    }
                }
            }
        } else if (t is IllegalStateException) {
            errorMessage =
                contextWeakReference?.get()?.getString(R.string.error_converting_data) ?: ""
            errorCode =
                ApiErrors.ERROR_CONVERTING_DATA_CODE.code
        } else {
            errorMessage = t.message ?: t.toString()
            errorCode = ApiErrors.ERROR_UNEXPECTED_CODE.code
        }
        onResult(Failure(errorMessage, errorCode))
    }

    private inner class ErrorWrapper {
        fun parseError(retrofit: Retrofit, response: Response<*>): BaseResponse? {
            val converter: Converter<ResponseBody, BaseResponse> = retrofit
                .responseBodyConverter(BaseResponse::class.java, emptyArray())
            try {
                response.errorBody()?.let {
                    return converter.convert(it)
                }
            } catch (e: Exception) {
                return BaseResponse()
            }
            return BaseResponse()
        }
    }
}