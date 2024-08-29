package com.glance.streamline.utils.extensions

import android.text.Spanned
import android.util.Patterns
import androidx.core.text.HtmlCompat
import java.net.URLConnection
import java.util.regex.Pattern

fun String.fromHtml(): Spanned {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun String.validateEmail(): Boolean {
    return isNotBlank() && length >= 10 && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.validatePhone(): Boolean {
    return isNotBlank() && (length in 11 until 16) && Patterns.PHONE.matcher(this).matches()
}

fun String.validateName(): Boolean {
    val name = Pattern.compile(
        "(\\+[A-z]+[\\- \\']*)?" +// sdd = space,' ,or dash
                "(\\([A-z]+\\)[\\- \\']*)?" +// <later><later|sdd>+<later>
                "([A-z][A-z\\- \\']+[A-z])"// <later><later|sdd>+<later>

    )
    return isNotBlank() && (length in 3 until 33) && name.matcher(this).matches()
}

fun String.hasSpaces(): Boolean = any { it.isWhitespace() }

fun String.hasNotProperPassLength(): Boolean = length < 8

fun String.isPathToImage(): Boolean {
    val mimeType = URLConnection.guessContentTypeFromName(this)
    return mimeType != null && mimeType.startsWith("image")
}

fun Float.formatTwoDigitAfterDelimiter(): Float = "%.02f".format(this).toFloat()