package com.glance.streamline.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlin.math.min


class SnackBarBehavior : CoordinatorLayout.Behavior<ViewGroup> {
    constructor() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        val diff = dependency.translationY - dependency.height
        val translationY = Math.min(0f, diff)
        child.translationY = translationY
//        Log.d("Snackbar", translationY.toString())
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: ViewGroup, dependency: View) {
        val diff = dependency.translationY - dependency.height
        val translationY = Math.min(0f, diff)
        child.translationY = translationY
    }

}