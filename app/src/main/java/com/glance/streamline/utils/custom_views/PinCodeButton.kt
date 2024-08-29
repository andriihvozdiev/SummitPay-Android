package com.glance.streamline.utils.custom_views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import com.glance.streamline.R
import com.glance.streamline.utils.convertPixelsToDp
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.visible


class PinCodeButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.layout_pin_code_button, this)

        val rootView: FrameLayout? = findViewById(R.id.pin_code_root_layout)
        val codeTextView: TextView? = findViewById(R.id.pin_code_text_view)
        val codeImageView: ImageView? = findViewById(R.id.pin_code_image_view)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PinCodeButton)

        rootView?.apply {
            ViewCompat.setBackgroundTintList(
                this, ColorStateList.valueOf(
                    attributes.getColor(
                        R.styleable.PinCodeButton_code_background_color,
                        context.getColorRes(R.color.colorPinCodeBackground)
                    )
                )
            )
        }

        codeTextView?.apply {
            text = attributes.getString(R.styleable.PinCodeButton_code_text)
                ?.let { visible(); it }
                ?: let { gone(); "" }
            setTextColor(
                attributes.getColor(
                    R.styleable.PinCodeButton_code_text_color,
                    context.getColorRes(R.color.colorWhite)
                )
            )
            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                context.convertPixelsToDp(
                    attributes.getDimensionPixelSize(
                        R.styleable.PinCodeButton_code_text_size,
                        36
                    ).toFloat()
                )
            )
        }

        codeImageView?.apply {
            attributes.getDrawable(R.styleable.PinCodeButton_code_image)
                ?.let { visible(); setImageDrawable(it) }
                ?: gone()
            attributes.getColor(
                R.styleable.PinCodeButton_code_image_tint,
                context.getColorRes(R.color.colorWhite)
            ).let {
                ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(it))
            }
            layoutParams.width =
                attributes.getDimension(R.styleable.PinCodeButton_code_image_size, 40f).toInt()
        }

        attributes.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*if (heightMeasureSpec < widthMeasureSpec) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        } else if (widthMeasureSpec < heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }*/

        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
