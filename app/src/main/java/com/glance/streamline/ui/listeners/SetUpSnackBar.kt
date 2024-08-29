package com.glance.streamline.ui.listeners

import android.view.Gravity
import android.view.View

interface SetUpSnackBar {
    fun attachSnackBarView(): View?
    fun setSnackBarMargin(): Int
    companion object {
        val empty = object: SetUpSnackBar {
            override fun attachSnackBarView(): View? = null
            override fun setSnackBarMargin(): Int = 0
        }
    }
}