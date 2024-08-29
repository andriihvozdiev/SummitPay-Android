package com.glance.streamline.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.util.Base64
import android.util.DisplayMetrics
import androidx.annotation.ArrayRes
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

fun getLocale() = Locale("ru")

fun Context.getStringArray(@ArrayRes stringArray: Int): Array<out String> =
    resources.getStringArray(stringArray)

fun Context?.convertPixelsToDp(px: Float): Float {
    val metrics = if (this == null) Resources.getSystem().displayMetrics
    else resources.displayMetrics

    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context?.convertDpToPixel(dp: Float): Float {
    val metrics = if (this == null) Resources.getSystem().displayMetrics
    else resources.displayMetrics

    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun inputStreamToString(inputStream: InputStream): String {
    try {
        val bytes = ByteArray(inputStream.available())
        inputStream.read(bytes, 0, bytes.size)
        return String(bytes)
    } catch (e: IOException) {
        return ""
    }
}

fun encodeFileToBase64(filePath: String): String{
    val bytes = File(filePath).readBytes()
    return Base64.encodeToString(bytes, 0)
}

fun Context?.showDatePicker(onSetAction: (Calendar) -> Unit){
    if (this == null) return
    val calendar = Calendar.getInstance()
    lateinit var picker: DatePickerDialog

    val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        calendar.set(year, month, dayOfMonth)
        if (picker.isShowing){
            picker.dismiss()
        }
        onSetAction.invoke(calendar)
    }

    picker = DatePickerDialog(this, dateSetListener,
        calendar[Calendar.YEAR], calendar[Calendar.MONTH] , calendar[Calendar.DAY_OF_MONTH])

    picker.show()
}

fun Int.toDp(context: Context) = this / context.resources.displayMetrics.density
fun Float.toDp(context: Context) = this / context.resources.displayMetrics.density

fun Int.toPx(context: Context) = this * context.resources.displayMetrics.density
fun Float.toPx(context: Context) = this * context.resources.displayMetrics.density

