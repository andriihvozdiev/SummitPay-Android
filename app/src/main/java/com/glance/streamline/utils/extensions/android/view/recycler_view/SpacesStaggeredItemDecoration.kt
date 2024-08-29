package com.glance.streamline.utils.extensions.android.view.recycler_view

import android.graphics.Rect
import android.view.View
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class SpacesStaggeredItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        val spanIndex = layoutParams.spanIndex

        if (spanIndex == 0) {
            val marginParentEnd = parent.marginEnd
            layoutParams.marginEnd = marginParentEnd / 2
        } else {
            val marginParentStart = parent.marginStart
            layoutParams.marginStart = marginParentStart / 2
        }

        view.layoutParams = layoutParams
    }

   /* override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(spacing, spacing, spacing, spacing)
    }*/

    /*override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position

        if (position >= 0) {
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left =
                    spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right =
                    (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right =
                    spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        } else {
            outRect.left = 0
            outRect.right = 0
            outRect.top = 0
            outRect.bottom = 0
        }
    }*/
}