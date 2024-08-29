package com.glance.streamline.mvvm

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.StreamlineApp
import com.glance.streamline.ui.models.TransitionAnimationStatus
import com.glance.streamline.utils.extensions.android.*
import com.glance.streamline.utils.extensions.getSchedulers
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class BaseViewModel(app: Application) : AndroidViewModel(app),
    LoadingStateListener, LifecycleObserver {

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    val loadingStateData = MutableLiveData<Result<*>>()

    val transitionAnimationStatus = MutableLiveData<TransitionAnimationStatus>()

    init {
        StreamlineApp[app].appComponent.inject(this)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    protected fun Completable.call(shouldShowLoading: Boolean = true, onSuccess: () -> Unit = {}) =
        this.getSchedulers().doOnSubscribe { if (shouldShowLoading) onStartLoading() }
            .doFinally { if (shouldShowLoading) onStopLoading() }
            .subscribe(
                onSuccess, ::onError
            ).addToDispose()

    protected fun <T> Maybe<T>.call(onSuccess: (t: T?) -> Unit = {}) =
        this.getSchedulers().subscribe(onSuccess, ::onError, { onSuccess(null) }).addToDispose()

    protected fun <T> Single<T>.call() = this.subscribe({ }, { }).addToDispose()

    protected fun <T> Single<T>.call(shouldShowLoading: Boolean = true, onSuccess: (t: T) -> Unit) =
        this.getSchedulers()
            .doOnSubscribe { if (shouldShowLoading) onStartLoading() }
            .doFinally { if (shouldShowLoading) onStopLoading() }
            .subscribe(
                onSuccess, ::onError
            ).addToDispose()

    protected fun <T> Observable<T>.call(onSuccess: (t: T) -> Unit) =
        this.getSchedulers().subscribe(onSuccess, ::onError).addToDispose()

    protected fun Disposable.addToDispose(): Disposable {
        compositeDisposable.add(this)
        return this
    }

    protected inline fun <T> Result<T>.doOnError(onErrorAction: (String) -> Unit){
        when(this){
            is Failure -> onErrorAction(this.errorMessage)
            is AuthError -> onErrorAction("AuthError")
            is ConnectionError -> onErrorAction("ConnectionError")
        }
    }

    protected inline fun <T> Result<T>.unWrapResult(
        customErrorString: String? = null,
        shouldShowLoading: Boolean = true,
        onSuccess: (Success<T>) -> Unit = {}
    ): Result<T> {
        when (this) {
            is Loading -> {
                if (shouldShowLoading) {
                    if (this.isLoading) onStartLoading()
                    else onStopLoading()
                }
            }
            is Failure -> {
                customErrorString?.let {
                    onError(Failure<T>(it, this.errorCode))
                } ?: onError(this)
            }
            is Success -> {
                onSuccess(this)
                this@BaseViewModel.onSuccess(this)
            }
            is ConnectionError -> {
                this.customErrorMessage = customErrorString
                onConnectionError(this)
            }
            is AuthError -> onAuthError(this)
        }
        return this
    }

    protected fun onAuthError(authError: AuthError<*>) {
        loadingStateData.value = authError
    }

    override fun onStartLoading() {
        loadingStateData.postValue(Loading<Any>(true))
    }

    override fun onStopLoading() {
        loadingStateData.postValue(Loading<Any>(false))
    }

    override fun onError(failure: Failure<*>) {
        loadingStateData.postValue(failure)
        onStopLoading()
    }

    override fun onConnectionError(connectionError: ConnectionError<*>) {
        loadingStateData.value = connectionError
    }

    override fun onSuccess(success: Success<*>) {
        loadingStateData.postValue(success)
    }

    override fun onSuccessfullyResent(success: Success<*>) {
        onSuccess(success)
    }

    protected fun onError(throwable: Throwable) {
        onError(Failure<Any>(throwable.message ?: "", 0))
    }

    protected fun getString(@StringRes stringResourceId: Int, vararg format: Any): String {
        return if (format.isNotEmpty())
            getApplication<StreamlineApp>().getString(stringResourceId, *format)
        else
            getApplication<StreamlineApp>().getString(stringResourceId)
    }

    fun setTransitionAnimationStatus(status: TransitionAnimationStatus) {
        transitionAnimationStatus.postValue(status)
    }

    fun getContext() = getApplication<StreamlineApp>()
}