package com.glance.streamline.utils.extensions.android.view

import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.TextView
import androidx.annotation.StringRes

fun TextView.allowLinkNavigation() {
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setText(@StringRes resId: Int, arg: String){
    text = context.getString(resId, arg)
}

fun TextView.removeLinkUnderlines() {
    val s = SpannableString(this.text)
    val spans = s.getSpans(0, s.length, URLSpan::class.java)
    for (span in spans) {
        val start = s.getSpanStart(span)
        val end = s.getSpanEnd(span)
        s.removeSpan(span)
        val newSpan = URLSpanNoUnderline(span.url)
        s.setSpan(newSpan, start, end, 0)
    }
    this.text = s
}

private class URLSpanNoUnderline(url: String) : URLSpan(url) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}
