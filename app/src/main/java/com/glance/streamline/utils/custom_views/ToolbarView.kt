package com.glance.streamline.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.glance.streamline.R
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.invisible
import com.glance.streamline.utils.extensions.android.view.listenForDrawn
import com.glance.streamline.utils.extensions.android.view.visible


class ToolbarView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    var isRequestLayoutCalled = false
    var showDoneButton = false
    var rightTextView: TextView? = null
    var titleTextView: TextView? = null

    init {
        inflate(context, R.layout.layout_toolbar, this)

        val root: LinearLayout? = findViewById(R.id.toolbar_root_layout)
        val leftButtonLayout: LinearLayout? = findViewById(R.id.toolbar_left_button_layout)
        val leftArrow: ImageView? = findViewById(R.id.toolbar_left_button_arrow)
        val leftTextView: TextView? = findViewById(R.id.toolbar_left_button_text_view)
        titleTextView = findViewById(R.id.toolbar_screen_title_text_view)
        titleTextView.invisible()

        rightTextView = findViewById(R.id.toolbar_right_button_text_view)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ToolbarView)
        val showBackButton = attributes.getBoolean(R.styleable.ToolbarView_show_back_button, false)
        val showCancelButton = attributes.getBoolean(R.styleable.ToolbarView_show_cancel_button, false)
        showDoneButton = attributes.getBoolean(R.styleable.ToolbarView_show_done_button, false)
        val titleText = attributes.getString(R.styleable.ToolbarView_title_text) ?: ""
        val titleTextColor = attributes.getColor(R.styleable.ToolbarView_title_text_color, context.getColorRes(R.color.colorPrimary))
        val textColor = attributes.getColor(R.styleable.ToolbarView_text_color, context.getColorRes(R.color.colorPrimary))
        attributes.recycle()

        leftTextView?.setTextColor(textColor)
        titleTextView?.setTextColor(titleTextColor)
        rightTextView?.setTextColor(textColor)
        leftArrow?.setColorFilter(textColor)

        leftButtonLayout.visible()
        if (showBackButton) {
            leftArrow.visible()
            leftTextView?.text = context.getString(R.string.text_back)
        }
        else if (showCancelButton) {
            leftArrow.gone()
            leftTextView?.text = context.getString(R.string.text_cancel)
        }
        else leftButtonLayout.invisible()

        if (titleText.isNotBlank())
            titleTextView?.text = titleText

        if (!showBackButton && !showCancelButton && !showDoneButton && titleText.isNotBlank()) {
            leftButtonLayout.gone()
            rightTextView.invisible()
        }

        leftButtonLayout?.listenForDrawn {
            rightTextView?.layoutParams?.width = it.measuredWidth
            isRequestLayoutCalled = true
            root?.requestLayout()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (isRequestLayoutCalled){
            titleTextView.visible()
            if (showDoneButton) rightTextView?.text = context.getString(R.string.text_done)
            else rightTextView.invisible()
            isRequestLayoutCalled = false
        }
    }

    fun getBackButton() = findViewById<LinearLayout>(R.id.toolbar_left_button_layout)
    fun getDoneButton() = findViewById<TextView>(R.id.toolbar_right_button_text_view)
}
