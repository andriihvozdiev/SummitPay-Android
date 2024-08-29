package com.glance.streamline.ui.fragments.connection_error

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.utils.extensions.android.ConnectionError
import com.glance.streamline.utils.extensions.android.Failure
import java.io.IOException
import javax.inject.Inject

class ConnectionErrorFragmentViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    private val connectionErrorData = MutableLiveData<ConnectionError<*>>()
    val resentSuccessfullyData = MutableLiveData<Boolean>()

    fun putRequest(request: ConnectionError<*>) {
        connectionErrorData.postValue(request)
    }

    fun setClosed() {
        resentSuccessfullyData.postValue(true)
    }

    fun callRequest() {
        val failedRequest = connectionErrorData.value

        if (failedRequest == null) {
            onError(Failure<Any>("Request not found", 0))
        } else {
            failedRequest.request
                .doOnSubscribe { onStartLoading() }
                .doOnSuccess { setClosed() }
                .doOnError {
                    if (it !is IOException) {
                        setClosed()
                        onError(
                            Failure<Any>(connectionErrorData.value?.customErrorMessage ?: "", 0)
                        )
                    }
                }
                .doFinally { onStopLoading() }
                .call()
        }
    }

    fun clearAllData() {
        connectionErrorData.value = null
        resentSuccessfullyData.value = null
    }
}