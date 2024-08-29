package com.glance.streamline.ui.listeners

import android.view.View

interface FragmentViewCreatedListener {
    fun onFragmentViewCreated(view: View)
    companion object {
        val empty = object: FragmentViewCreatedListener {
            override fun onFragmentViewCreated(view: View) = Unit
        }
    }
}