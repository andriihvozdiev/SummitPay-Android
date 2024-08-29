package com.glance.streamline.utils.extensions.android.view.recycler_view

import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.glance.streamline.utils.extensions.android.view.listenForDrawn
import me.everything.android.ui.overscroll.*
import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter

fun RecyclerView.enableOverscrollWithTopPredicate(
    absoluteTopPredicate: () -> Boolean = { false },
    absoluteBottomPredicate: () -> Boolean = { !canScrollDown() }
): OverScrollBounceEffectDecoratorBase {
    val rcv = this
    return VerticalOverScrollBounceEffectDecorator(
        object : RecyclerViewOverScrollDecorAdapter(rcv, object : Impl {
            override fun isInAbsoluteStart(): Boolean = absoluteTopPredicate.invoke()
            override fun isInAbsoluteEnd(): Boolean = absoluteBottomPredicate()
        }) {
            override fun isInAbsoluteStart(): Boolean = absoluteTopPredicate.invoke()
        }
    )
}

fun RecyclerView.getBottomOverscrollListener(onOverScroll: (Float) -> Unit): IOverScrollUpdateListener =
    object : IOverScrollUpdateListener {
        var isEndDrag = false
        var isBounceBack = false
        override fun onOverScrollUpdate(decor: IOverScrollDecor, state: Int, offset: Float) {
            if (state == IOverScrollState.STATE_DRAG_END_SIDE) {
                isEndDrag = true
                isBounceBack = false
            }
            if (isEndDrag && state == IOverScrollState.STATE_BOUNCE_BACK) {
                isBounceBack = true
            }
            if (state != IOverScrollState.STATE_BOUNCE_BACK && state != IOverScrollState.STATE_DRAG_END_SIDE) {
                isEndDrag = false
                isBounceBack = false
            }
            if ((isEndDrag || isBounceBack) && !canScrollUp()) {
                onOverScroll(offset)
            }
        }
    }

fun RecyclerView.attachSnapHelperWithListener(
    snapHelper: SnapHelper,
    onPositionChangedListener: (position: Int) -> Unit
) {
    snapHelper.attachToRecyclerView(this)
    addOnScrollListener(
        SnapOnScrollListener(
            snapHelper,
            onPositionChangedListener
        )
    )
}

fun RecyclerView.attachSnapHelperWithListener(
    snapHelper: SnapHelper,
    behavior: SnapScrollListener.Behavior = SnapScrollListener.Behavior.NOTIFY_ON_SCROLL,
    onSnapPositionChangeListener: OnSnapPositionChangeListener
) {
    snapHelper.attachToRecyclerView(this)
    val snapOnScrollListener =
        SnapScrollListener(snapHelper, behavior, onSnapPositionChangeListener)
    addOnScrollListener(snapOnScrollListener)
}

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}

private class SnapOnScrollListener(
    private val snapHelper: SnapHelper,
    private val onPositionChangedListener: (position: Int) -> Unit = {}
) : RecyclerView.OnScrollListener() {

    private var lastPosition = RecyclerView.NO_POSITION

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        checkPositionChanged(recyclerView)
    }

    private fun checkPositionChanged(recyclerView: RecyclerView) {
        val currentPosition = snapHelper.getSnapPosition(recyclerView)
        val isPositionChanged = this.lastPosition != currentPosition
        if (isPositionChanged) {
            onPositionChangedListener.invoke(currentPosition)
            this.lastPosition = currentPosition
        }
    }

    private fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

}

fun NestedScrollView.enableOverscroll(): OverScrollBounceEffectDecoratorBase {
    val nsv = this
    return VerticalOverScrollBounceEffectDecorator(
        object : IOverScrollDecoratorAdapter {
            override fun getView(): View = nsv
            override fun isInAbsoluteStart(): Boolean = nsv.scrollY == 0
            override fun isInAbsoluteEnd(): Boolean = nsv.scrollY ==
                    (nsv.getChildAt(0).measuredHeight - nsv.measuredHeight)
        }
    )
}

fun RecyclerView.preventFromScrollingWhenNotEnoughElements() {
    this.listenForDrawn {
        it.isNestedScrollingEnabled = adapter != null && canScrollVertically(1)
    }
}

// 1 = down; -1 = up; 0 = up or down
fun RecyclerView.canScrollDown() = canScrollVertically(1)

fun RecyclerView.canScrollUp() = canScrollVertically(-1)