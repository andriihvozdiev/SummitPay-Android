package com.glance.streamline.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.*


class SquaredLinearLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount > 0) {
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            if (orientation == HORIZONTAL) {
                var totalHorizontalMargins = 0
                (0 until childCount).forEach {
                    totalHorizontalMargins += getChildAt(it).marginLeft + getChildAt(it).marginRight
                }
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((widthSize - totalHorizontalMargins) / childCount, MeasureSpec.EXACTLY)
                )
            } else {
                var totalVerticalMargins = 0
                (0 until childCount).forEach {
                    totalVerticalMargins += getChildAt(it).marginTop + getChildAt(it).marginBottom
                }
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec((heightSize - totalVerticalMargins) / childCount, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
                )
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}
