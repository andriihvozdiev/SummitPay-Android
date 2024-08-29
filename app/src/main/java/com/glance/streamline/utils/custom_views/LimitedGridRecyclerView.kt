package com.glance.streamline.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LimitedGridRecyclerView(context: Context, attrs: AttributeSet) :
    RecyclerView(context, attrs) {

    var decorationsSpacing = 8

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        (layoutManager as? GridLayoutManager)?.let {
            if (it.childCount > 0 && it.childCount > it.spanCount * 2) {
                it.getChildAt(0)?.let { child ->
                    val maxHeight =
                        (child.height + child.marginBottom + decorationsSpacing * 2) * 2 +
                                child.marginTop + paddingTop + paddingBottom + child.height / 2
                    super.onMeasure(
                        widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY)
                    )
                    return
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
