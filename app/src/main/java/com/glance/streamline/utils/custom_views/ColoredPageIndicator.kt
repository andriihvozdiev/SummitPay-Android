package com.glance.streamline.utils.custom_views

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.glance.streamline.R


class ColoredPageIndicator : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setup(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setup(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        setup(attrs)
    }

    private var circleSelectors = arrayListOf<CircleIndicator>()
    private var circleColors = arrayListOf<Int>()
    private var viewPagerId: Int? = null
    private var viewPager: ViewPager? = null
    private var circleRadius: Float = 50f
    private var circlePadding: Float = 30f

    private val pageChangedListener = object : ViewPager.OnPageChangeListener {
        var lastOffset = 0

        var truePosition = 0

        var currentState = 0

        override fun onPageScrollStateChanged(state: Int) {
            Log.d(
                "STATE",
                "$state, page: ${viewPager?.currentItem}"
            )
            currentState = state
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                truePosition = viewPager?.currentItem ?: 0
            }
//            if (state == ViewPager.SCROLL_STATE_SETTLING){
//                circleSelectors.forEachIndexed { index, indicator ->
//                    indicator.paint.color = indicator.color
//                    indicator.paint.alpha = if (index == viewPager?.currentItem?:0) 255 else 0
//                }
//            }
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            /*val isTouching = (viewPager as CustomViewPager?)?.isTouching?:false
            Log.d(
                "test viewpager",
                "pixels: $positionOffsetPixels, offset: $positionOffset, page: ${position == viewPager?.currentItem}, isTouching=$isTouching"
            )


            if (circleColors.size >= circleSelectors.size && circleSelectors.size >= viewPager?.adapter?.count ?: 0 && positionOffsetPixels > 0) {
                val currentPosition = truePosition//viewPager?.currentItem?:0


                val alpha = (positionOffset * 255).toInt()


                val pageWidth = viewPager?.width ?: 0
                val currentOffset = positionOffsetPixels + (pageWidth * currentPosition)


                if (viewPager?.currentItem == position) {
                    //pos -> alpha--
                    //pos+1 -> alpha++
                } else {
                    //pos -> alpha++
                    //pos+1 -> alpha--
                }


                if (lastOffset < currentOffset) {
                    //right
                    if (currentState == ViewPager.SCROLL_STATE_SETTLING) {
                        //when page returned back to start position
                        val nextPosition = currentPosition - 1
                        if (nextPosition >= 0) {
                            Log.d(
                                "DIRECTION",
                                "right-with-settle, currentPosition: $currentPosition"
                            )
                            circleSelectors[nextPosition].paint.alpha = 255 - alpha
                        }
                        circleSelectors[currentPosition].paint.alpha = alpha
                    } else {
                        val nextPosition = currentPosition + 1
                        if (nextPosition < circleSelectors.size) {
                            Log.d(
                                "DIRECTION",
                                "right, currentPosition: $currentPosition"
                            )
                            circleSelectors[nextPosition].paint.alpha = alpha
                        }
                        circleSelectors[currentPosition].paint.alpha = 255 - alpha
                    }
                } else if (lastOffset > currentOffset) {
                    //left
                    if (currentState == ViewPager.SCROLL_STATE_SETTLING && !isTouching) {
                        //when page returned back to start position
                        val nextPosition = currentPosition + 1
                        if (nextPosition < circleSelectors.size) {
                            Log.d(
                                "DIRECTION",
                                "left-with-settle, currentPosition: $currentPosition"
                            )
                            circleSelectors[nextPosition].paint.alpha = alpha
                        }
                        circleSelectors[currentPosition].paint.alpha = 255 - alpha
                    } else {
                        val nextPosition = currentPosition - 1
                        if (nextPosition >= 0) {
                            Log.d(
                                "DIRECTION",
                                "left, currentPosition: $currentPosition"
                            )
                            circleSelectors[nextPosition].paint.alpha = 255 - alpha
                        }
                        circleSelectors[currentPosition].paint.alpha = alpha
                    }
                }

                lastOffset = currentOffset
            }*/
//            if (positionOffsetPixels == 0)
//                circleSelectors.forEachIndexed { index, indicator ->
//                    indicator.paint.color = indicator.color
//                    indicator.paint.alpha = if (index == viewPager?.currentItem?:0) 255 else 0
//                }
        }

        override fun onPageSelected(position: Int) {
//            lastOffset = viewPager?.width?:0 * position
            circleSelectors.forEachIndexed { index, indicator ->
                indicator.paint.color = indicator.color
                indicator.paint.alpha = if (index == position) 255 else 0
            }
        }

    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    private fun setup(set: AttributeSet?) {
        set.getParameter {

            val colorsId = it.getResourceId(R.styleable.ColoredPageIndicator_circle_colors, 0)
            if (colorsId > 0) {
                circleColors = resources.getIntArray(colorsId).toCollection(arrayListOf())
            }

            viewPagerId = it.getResourceId(R.styleable.ColoredPageIndicator_view_pager, 0)

            circleRadius =
                it.getDimensionPixelSize(R.styleable.ColoredPageIndicator_circle_radius, 50)
                    .toFloat()

            circlePadding =
                it.getDimensionPixelSize(R.styleable.ColoredPageIndicator_circle_padding, 30)
                    .toFloat()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewPager = findViewPager(parent)
        update()
    }

    fun update() {
        setUpSelectors()
        setCoordinates(width.toFloat(), height.toFloat())
    }

    private fun setUpSelectors() {
        circleSelectors.clear()
        viewPager?.adapter?.let { adapter ->
            if (adapter.count > 0) {
                for (i in 0 until adapter.count) {
                    val color = if (i <= circleColors.size - 1) circleColors[i] else Color.CYAN
                    circleSelectors.add(
                        CircleIndicator(
                            color
                        ).apply {
                        paint.color = getUnselectedPageColor()
                        unselectedPaint.color = getUnselectedPageColor()
                    })
                }
                if (circleSelectors.isNotEmpty()) {
                    val selectedPage = viewPager?.currentItem?:0
                    circleSelectors[selectedPage].paint.color = circleSelectors[selectedPage].color
                }
            }
        }
        viewPager?.addOnPageChangeListener(pageChangedListener)
    }

    override fun onDetachedFromWindow() {
        viewPager?.removeOnPageChangeListener(pageChangedListener)
        super.onDetachedFromWindow()
    }

    private inline fun AttributeSet?.getParameter(init: (TypedArray) -> Unit) {
        this?.let { attributes ->
            var typedArray: TypedArray? = null
            try {
                typedArray =
                    context.obtainStyledAttributes(attributes, R.styleable.ColoredPageIndicator)
                init(typedArray)
                typedArray.recycle()
            } catch (e: Resources.NotFoundException) {
                typedArray?.recycle()
            }
        }
    }

    private fun findViewPager(viewParent: ViewParent?): ViewPager? {
        val isValidParent = viewParent != null &&
                viewParent is ViewGroup &&
                viewParent.childCount > 0

        if (!isValidParent) {
            return null
        }

        val viewPager = findViewPager(viewParent as ViewGroup?, viewPagerId)

        if (viewPager != null) {
            return viewPager
        } else {
            findViewPager(viewParent?.parent)
        }

        return null
    }

    private fun findViewPager(viewGroup: ViewGroup?, id: Int?): ViewPager? {
        viewGroup?.let {
            if (it.childCount <= 0 || id == null) {
                return null
            }
            val view = it.findViewById<View>(id)
            return if (view != null && view is ViewPager) {
                view
            } else {
                null
            }
        } ?: return null
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val desiredWidth = 100
        val desiredHeight = circleRadius * 2

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize)
        } else {
            //Be whatever you want
            width = widthSize
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight.toInt(), heightSize)
        } else {
            //Be whatever you want
            height = heightSize
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    private fun setCoordinates(layoutWidth: Float, layoutHeight: Float) {
        val circlesCount = circleSelectors.let { if (it.isNotEmpty()) it.size.toFloat() else 1f }
        val circleWidth: Float = circleRadius * 2
        val circleWidthWithPadding: Float = circleWidth + circlePadding

        val offset =
            (width - circleWidthWithPadding * circlesCount + circlePadding) / 2f + circleRadius

        circleSelectors.forEachIndexed { index, circle ->
            circle.radius = circleRadius
            circle.x = offset + circleWidthWithPadding * index
            circle.y = layoutHeight / 2
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = (right - left).toFloat()
        val height = (bottom - top).toFloat()
        setCoordinates(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        circleSelectors.forEach { it.draw(canvas) }
        postInvalidateOnAnimation()
    }

    private fun getUnselectedPageColor() =
        ContextCompat.getColor(context, R.color.colorGray)

}

data class CircleIndicator(val color: Int = Color.CYAN) {

    var x = 0f
    var y = 0f
    var radius = 0f

    val paint = Paint().apply {
        color = this@CircleIndicator.color
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    val unselectedPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, radius, unselectedPaint)
        canvas.drawCircle(x, y, radius, paint)
    }

}
