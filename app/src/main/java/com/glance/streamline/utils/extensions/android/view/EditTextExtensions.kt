package com.glance.streamline.utils.extensions.android.view

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.inputmethod.EditorInfo
import android.widget.EditText

fun EditText.onTextChanged(onTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onTextChanged.invoke(p0.toString())
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}


fun EditText.setOnSearchActionListener(onSearch: () -> Unit) {
    setOnEditorActionListener { _, actionId, event ->
        when {
            actionId == EditorInfo.IME_ACTION_SEARCH -> {
                onSearch()
                true
            }
            event.keyCode == KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN -> {
                onSearch()
                true
            }
            else -> false
        }
    }
}
