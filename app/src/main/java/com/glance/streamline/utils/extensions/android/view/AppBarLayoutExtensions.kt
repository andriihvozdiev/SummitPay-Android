package com.glance.streamline.utils.extensions.android.view

import com.google.android.material.appbar.AppBarLayout

fun AppBarLayout?.addExpandedListener(onExpandedChanged: (isExpanded: Boolean) -> Unit) {
    this?.addOffsetListener { offset -> onExpandedChanged.invoke(offset == 0) }
}

fun AppBarLayout?.addOffsetListener(onOffsetChanged: (offset: Int) -> Unit) {
    this?.addOnOffsetChangedListener(
        AppBarLayout.OnOffsetChangedListener { _, offset -> onOffsetChanged.invoke(offset) }
    )
}