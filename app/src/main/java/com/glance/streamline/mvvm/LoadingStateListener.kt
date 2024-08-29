package com.glance.streamline.mvvm

import com.glance.streamline.utils.extensions.android.ConnectionError
import com.glance.streamline.utils.extensions.android.Failure
import com.glance.streamline.utils.extensions.android.Success

interface LoadingStateListener {
    fun onStartLoading()
    fun onStopLoading()
    fun onSuccess(success: Success<*>)
    fun onSuccessfullyResent(success: Success<*>)
    fun onError(failure: Failure<*>)
    fun onConnectionError(connectionError: ConnectionError<*>)
}