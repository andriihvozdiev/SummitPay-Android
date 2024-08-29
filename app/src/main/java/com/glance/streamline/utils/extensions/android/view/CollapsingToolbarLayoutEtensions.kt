package com.glance.streamline.utils.extensions.android.view

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout


fun CollapsingToolbarLayout.enableScroll() {
    val params = layoutParams as AppBarLayout.LayoutParams
    params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
    layoutParams = params
}

fun CollapsingToolbarLayout.disableScroll() {
    val params = layoutParams as AppBarLayout.LayoutParams
    params.scrollFlags = 0
    layoutParams = params
}