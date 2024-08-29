package com.glance.streamline.utils

import android.view.GestureDetector
import android.view.MotionEvent

class OnSwipeTouchListener(private val onTap: (MotionEvent) -> Unit) :

    GestureDetector.SimpleOnGestureListener() {
    override fun onDown(e: MotionEvent?): Boolean {
        return super.onDown(e)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return super.onDoubleTap(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
        return super.onContextClick(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return super.onSingleTapConfirmed(e)
    }

    override fun onShowPress(e: MotionEvent?) {
        super.onShowPress(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return super.onDoubleTapEvent(e)
    }

    override fun onLongPress(e: MotionEvent) {
        onTap(e)
        super.onLongPress(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onTap(e)
        return super.onSingleTapConfirmed(e)
    }
}