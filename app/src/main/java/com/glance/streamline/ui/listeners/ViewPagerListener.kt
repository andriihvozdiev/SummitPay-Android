package com.glance.streamline.ui.listeners

import androidx.fragment.app.Fragment

interface ViewPagerListener {
    var isVisible: (fragment: Fragment) -> Boolean
    fun getSelectedPageIndex(): Int

    companion object {
        val empty = object: ViewPagerListener {
            override var isVisible: (fragment: Fragment) -> Boolean = {_ -> false}
            override fun getSelectedPageIndex() = 0
        }
    }
}