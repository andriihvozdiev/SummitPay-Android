package com.glance.streamline.utils.extensions.android.view

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment

fun DialogFragment.hideKeyboard() {
    val inputMethodManager =
        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    dialog?.window?.currentFocus?.let { focus ->
        inputMethodManager?.hideSoftInputFromWindow(focus.windowToken, 0)
    }
}

fun DialogFragment.showKeyboard() {
    (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.toggleSoftInputFromWindow(
        dialog?.window?.decorView?.windowToken,
        InputMethodManager.SHOW_FORCED, 0
    )
}