package com.glance.streamline.ui.listeners

interface FragmentBackPressedListener {
    fun onBackPressed()
    fun shouldExit(): Boolean
}