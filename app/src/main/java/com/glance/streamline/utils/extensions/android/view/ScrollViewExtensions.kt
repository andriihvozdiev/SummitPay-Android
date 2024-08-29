package com.glance.streamline.utils.extensions.android.view

import android.widget.HorizontalScrollView
import android.widget.ScrollView

fun ScrollView.scrollToEnd() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY + height)
    smoothScrollBy(0, delta)
}

fun HorizontalScrollView.scrollToEnd() {
    val lastChild = getChildAt(childCount - 1)
    val end = lastChild.right + paddingEnd
    val delta = end - (scrollX + width)
    smoothScrollBy(delta, 0)
}