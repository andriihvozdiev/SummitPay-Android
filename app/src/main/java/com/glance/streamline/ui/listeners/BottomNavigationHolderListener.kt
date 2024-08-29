package com.glance.streamline.ui.listeners

import com.glance.streamline.ui.base.BaseFragment
import kotlin.reflect.KClass

interface BottomNavigationHolderListener {
    fun setSelectedTab(tabStartFragmentClass: KClass<out BaseFragment<*>>)
    fun setBottomNavigationViewVisibility(visibility: Int)
    fun getSelectedNavHostId(): Int

    companion object {
        val empty = object : BottomNavigationHolderListener {
            override fun setSelectedTab(tabStartFragmentClass: KClass<out BaseFragment<*>>) = Unit
            override fun setBottomNavigationViewVisibility(visibility: Int) = Unit
            override fun getSelectedNavHostId() = 0
        }
    }
}