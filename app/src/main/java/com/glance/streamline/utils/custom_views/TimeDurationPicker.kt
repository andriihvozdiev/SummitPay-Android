package com.glance.streamline.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.glance.streamline.R
import kotlinx.android.synthetic.main.time_duration_picker.view.*
import java.util.*

class TimeDurationPicker(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var onZeroSelected = { isSelected: Boolean -> }

    init {
        inflate(context, R.layout.time_duration_picker, this)
        setTimePickerInterval()
    }

    private fun setTimePickerInterval() {
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        val displayedMinutes = ArrayList<String>()
        for (i in 0..59) {
            displayedMinutes.add(String.format("%d", i))
        }
        minutePicker.displayedValues = displayedMinutes.toTypedArray()
        minutePicker.wrapSelectorWheel = true
        minutePicker.setOnValueChangedListener { _, i, i2 ->
            if (minutePicker?.value == 0 && i2 == 0)
                onZeroSelected(true)
            else onZeroSelected(false)
        }

        secondPicker.minValue = 0
        secondPicker.maxValue = 11
        val displayedSeconds = ArrayList<String>()
        for (i in 0..55 step 5) {
            displayedSeconds.add(String.format("%02d", i))
        }
        secondPicker.displayedValues = displayedSeconds.toTypedArray()
        secondPicker.wrapSelectorWheel = true
        secondPicker.setOnValueChangedListener { _, i, i2 ->
            if (minutePicker?.value == 0 && i2 == 0)
                onZeroSelected(true)
            else onZeroSelected(false)
        }
    }

    fun setMinutes(minute: Int) {
        if (minute <= 60) {
            minutePicker?.value = minute
        }
    }

    fun setSeconds(second: Int) {
        secondPicker?.value = second / 5
    }

    private fun getMinutes() = minutePicker?.value ?: 0
    private fun getSeconds() = (secondPicker?.value ?: 0) * 5
    fun getDurationSeconds() = getMinutes() * 60L + getSeconds()

}
