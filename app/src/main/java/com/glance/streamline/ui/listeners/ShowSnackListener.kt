package com.glance.streamline.ui.listeners

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.ColorRes
import com.androidadvance.topsnackbar.TSnackbar
import com.glance.streamline.R

interface ShowSnackListener {
    fun showSnack(
        attachView: View?,
        message: String,
        @ColorRes bgColorResId: Int,
        @ColorRes textColorResId: Int = R.color.colorWhite,
        length: Int = TSnackbar.LENGTH_LONG,
        width: Int? = null
    )

    companion object {
        val empty = object : ShowSnackListener {
            override fun showSnack(
                attachView: View?,
                message: String,
                bgColorResId: Int,
                textColorResId: Int,
                length: Int,
                width: Int?
            ) {
            }
        }
    }
}