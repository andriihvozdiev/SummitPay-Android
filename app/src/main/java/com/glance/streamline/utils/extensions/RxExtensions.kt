package com.glance.streamline.utils.extensions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Maybe<T>.getSchedulers(): Maybe<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
}

fun <T> Single<T>.getSchedulers(): Single<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
}

fun <T> Observable<T>.getComputationSchedulers(): Observable<T> =
    this.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.computation())

fun <T> Observable<T>.getSchedulers(): Observable<T> =
    this.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())


fun Completable.getSchedulers(): Completable =
    this.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
