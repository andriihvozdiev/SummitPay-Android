package com.glance.streamline.utils.extensions.android.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.glance.streamline.R
import com.glance.streamline.utils.extensions.android.getDefaultBottomBarHeight


fun View?.visible() {
    this?.visibility = View.VISIBLE
}

fun View?.invisible() {
    this?.visibility = View.INVISIBLE
}

fun View?.gone() {
    this?.visibility = View.GONE
}

fun View?.enable() {
    this?.isEnabled = true
    this?.isClickable = true
}

fun View?.disable() {
    this?.isEnabled = false
    this?.isClickable = false
}

fun View.listenForDrawn(onViewDrawn: (view: View) -> Unit) {
    val view = this
    view.viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            onViewDrawn(view)
        }
    })
}

fun View.getStatusBarHeight(found: (Int) -> Unit) {
    setOnApplyWindowInsetsListener { _, insets ->
        setOnApplyWindowInsetsListener(null)
        found(insets.systemWindowInsetTop)
        insets.consumeSystemWindowInsets()
    }
}

fun View.setStatusBarTopPadding() {
    getStatusBarHeight {
        setPadding(paddingLeft, it, paddingRight, paddingBottom)
        requestLayout()
    }
}

fun View.setDefaultBottomBarMargin() {
    val bottomBarHeight = context.getDefaultBottomBarHeight()
    setMarginBottom(bottomBarHeight)
}

fun ViewGroup.setHeight(height: Int) {
    val params = layoutParams as ViewGroup.LayoutParams
    params.height = height
    layoutParams = params
}

fun View.setMarginRight(marginRight: Int) {
    val layoutParams =
        (this.layoutParams as? ViewGroup.MarginLayoutParams) ?: ViewGroup.MarginLayoutParams(
            layoutParams
        )
    layoutParams.setMargins(
        layoutParams.leftMargin, layoutParams.topMargin,
        marginRight, layoutParams.bottomMargin
    )
    this.layoutParams = layoutParams
}

fun View.setMarginTop(marginTop: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(
        menuLayoutParams.leftMargin, marginTop,
        menuLayoutParams.rightMargin, menuLayoutParams.bottomMargin
    )
    this.layoutParams = menuLayoutParams
}

fun View.setMarginBottom(marginBottom: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(
        menuLayoutParams.leftMargin, menuLayoutParams.topMargin,
        menuLayoutParams.rightMargin, marginBottom
    )
    this.layoutParams = menuLayoutParams
}

fun View.setMargins(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(left, top, right, bottom)
    this.layoutParams = menuLayoutParams
}

fun View.setMarginsDp(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val leftInDp = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        left.toFloat(),
        context.resources.displayMetrics
    ).toInt()

    val topInDp = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        top.toFloat(),
        context.resources.displayMetrics
    ).toInt()

    val rightInDp = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        right.toFloat(),
        context.resources.displayMetrics
    ).toInt()

    val bottomInDp = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        bottom.toFloat(),
        context.resources.displayMetrics
    ).toInt()

    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(leftInDp, topInDp, rightInDp, bottomInDp)
    this.layoutParams = menuLayoutParams
}

fun View.animateBounce(
    initialValue: Float = 0.9f,
    time: Long = 100,
    onAnimationEnd: () -> Unit = {}
) {
    animate().scaleX(initialValue).scaleY(initialValue).alpha(0.5f).setDuration(time)
        .setListener(getAnimationEndListener {
            animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(time)
                .setListener(getAnimationEndListener { onAnimationEnd() })
        })
}

fun View.animateOpacity(
    opacity: Float = 0.5f,
    time: Long = 100,
    onAnimationEnd: () -> Unit = {}
) {
    animate().alpha(opacity).setDuration(time)
        .setListener(getAnimationEndListener { onAnimationEnd() })
}

fun View.blink(isEnabled: Boolean, duration: Long = 500) {
    if (isEnabled) {
        val anim: Animation = AlphaAnimation(0.2f, 1.0f)
        anim.duration = duration
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        startAnimation(anim)
    } else {
        clearAnimation()
    }
}

private fun getAnimationEndListener(onAnimationEnd: () -> Unit = {}): AnimatorListenerAdapter {
    return object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            onAnimationEnd()
        }
    }
}

fun View.shake() {
    this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.shake))
}

