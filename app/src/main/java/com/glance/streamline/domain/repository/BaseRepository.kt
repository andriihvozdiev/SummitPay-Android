package com.glance.streamline.domain.repository

import android.content.Context
import android.util.Base64
import com.glance.streamline.utils.extensions.android.Loading
import com.glance.streamline.utils.extensions.android.Result
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference


abstract class BaseRepository(protected val context: Context) {

    private fun <T, V> initRequestSingleForResult(
        request: Single<T>,
        onResult: (Result<V>) -> Unit
    ): Single<T> {
        val callback = CallbackWrapper<T, V>(
            WeakReference(context),
            onResult
        )
        val requestSingle = request.getSchedulers()
            .doOnSubscribe {
                onResult(Loading(true))
            }
            .doOnSuccess {
                callback.onSuccess(it)
            }
            .doOnError {
                callback.onError(it)
            }
            .doFinally {
                onResult(Loading(false))
            }
        callback.request = requestSingle
        return requestSingle
    }

    protected fun <T, V> Single<T>.getWrapped(
        onResult: (Result<V>) -> Unit
    ): Single<T> {
        return initRequestSingleForResult(this, onResult)
    }

    protected fun <T> Single<T>.getSchedulers(): Single<T> {
        return this.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    protected fun <T> Single<T>.getComputationSchedulers(): Single<T> {
        return this.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
    }

    protected fun zip(requests: ArrayList<Single<*>>, onLoadingEnded: () -> Unit): Disposable {
        return Single.zip(requests) { }.subscribe({ onLoadingEnded() }, {})
    }

    protected fun getJson(data: Map<String, Any>): RequestBody {
        return RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            (JSONObject(data)).toString()
        )
    }

    protected fun File.getBase64String() =
        Base64.encodeToString(readBytes(), Base64.DEFAULT)

}