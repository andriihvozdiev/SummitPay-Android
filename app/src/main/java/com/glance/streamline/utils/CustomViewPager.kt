package com.glance.streamline.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager


class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var mScroller: FixedSpeedScroller? = null
    private var enableds: Boolean = false
    var isTouching = false

    var onTouched: () -> Unit = {}

    init {
        this.enableds = true

        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            mScroller = FixedSpeedScroller(
                context,
                DecelerateInterpolator()
            )
            scroller.set(this, mScroller)
        } catch (ignored: Exception) {
        }

    }

    fun setScrollDuration(duration: Int) {
        mScroller?.setScrollDuration(duration)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        isTouching = when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> true
            else -> false
        }
        if (isTouching) onTouched()
        return this.enableds && super.onTouchEvent(event)

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        setScrollDuration(200)
        return this.enableds && super.onInterceptTouchEvent(event)

    }

    fun addOnPageSelectedListener(listener: (Int) -> Unit) {
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                listener(position)
            }
        })
    }

    private inner class FixedSpeedScroller(context: Context, interpolator: Interpolator) :
        Scroller(context, interpolator) {

        private var customDuration = 500


        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, customDuration)
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
            super.startScroll(startX, startY, dx, dy, customDuration)
        }

        fun setScrollDuration(duration: Int) {
            customDuration = duration
        }
    }
}
