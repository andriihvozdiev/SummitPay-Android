package com.glance.streamline.ui.listeners

import android.view.View
import io.reactivex.disposables.Disposable

interface FilteredClickListener {
    fun setFilteredClickListener(
        view: View,
        durationMillis: Long = 400,
        onClick: (view: View) -> Unit
    ): Disposable?
}