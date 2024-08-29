package com.glance.streamline.utils.extensions.android

import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.glance.streamline.R
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.ui.base.BaseFragment


fun <T : BaseViewModel> BaseFragment<T>.getColorRes(@ColorRes colorResourceId: Int) =
    ContextCompat.getColor(baseContext, colorResourceId)

fun <T : BaseViewModel> BaseFragment<T>.setStatusBarColor(@ColorRes colorResourceId: Int) {
    val window: Window = baseActivity.window
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = baseContext.getColorRes(colorResourceId)
}

fun <V : BaseViewModel> BaseFragment<V>.replaceFragmentWithFadeInOut(fragment: Fragment) {
    baseActivity.supportFragmentManager.beginTransaction()
        .setCustomAnimations(
            R.anim.nav_default_enter_anim,
            R.anim.nav_default_exit_anim,
            R.anim.nav_default_pop_enter_anim,
            R.anim.nav_default_pop_exit_anim
        )
        .replace(this.id, fragment)
        .setPrimaryNavigationFragment(fragment)
        .commit()
}