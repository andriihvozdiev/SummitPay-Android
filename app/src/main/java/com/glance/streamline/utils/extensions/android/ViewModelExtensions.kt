package com.glance.streamline.utils.extensions.android

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.navigation.NavController
import io.reactivex.Single

inline fun <reified T : ViewModel> FragmentActivity.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory)[T::class.java]
}

fun <T> LiveData<T>.observe(@NonNull owner: LifecycleOwner, onUpdate: (t: T) -> Unit) {
    this.observe(owner, Observer { data ->
        data?.let { onUpdate(data) }
    })
}

fun NavController.isStartDestination(): Boolean {
    val startDestination = graph.startDestination
    val currDestination = currentDestination?.id ?: -1
    return startDestination == currDestination
}

sealed class Result<T>
data class Success<T>(
    val value: T,
    val successMessage: String? = null,
    val wasResent: Boolean = false
) : Result<T>()

open class Failure<T>(val errorMessage: String, val errorCode: Int) : Result<T>()
data class Loading<T>(val isLoading: Boolean) : Result<T>()

data class ConnectionError<T>(val request: Single<T>, var customErrorMessage: String? = null) :
    Result<T>()

data class AuthError<T>(val request: Single<T>) : Result<T>()

class EmptyArray<T> : Result<T>()

infix fun <T> ArrayList<T>.isEmptyList(f: () -> Unit): Result<ArrayList<T>> =
    if (this.isEmpty()) {
        f()
        EmptyArray()
    } else Success(this)

infix fun <T> Result<T>.otherwise(f: (T) -> Unit) = if (this is Success) f(this.value) else Unit
