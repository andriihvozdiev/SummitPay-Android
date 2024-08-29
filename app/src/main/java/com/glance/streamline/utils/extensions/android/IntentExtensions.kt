package com.glance.streamline.utils.extensions.android

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.glance.streamline.utils.extensions.timestampEpochMillis
import java.util.*

fun intentForMap(
    latitude: Float,
    longitude: Float,
    zoom: Float = 19f, /* [0](world) - [21](buildings) */
    query: String = "",
    explicitGoogleMaps: Boolean = false
): Intent {
    val maybeSearchQuery = if (query.isBlank()) "" else "&q=${query}"
    val geolocationUri: Uri = Uri.parse("geo:$latitude,$longitude?z=$zoom$maybeSearchQuery")
    return Intent(Intent.ACTION_VIEW, geolocationUri).apply {
        if (explicitGoogleMaps) setPackage("com.google.android.apps.maps")
    }
}

fun intentForCalendarEvent(
    title: String,
    description: String,
    location: String,
    beginTime: Date,
    endTime: Date = beginTime
): Intent = Intent(Intent.ACTION_INSERT).apply {
    data = CalendarContract.Events.CONTENT_URI
    putExtra(CalendarContract.Events.TITLE, title)
    putExtra(CalendarContract.Events.DESCRIPTION, description)
    putExtra(CalendarContract.Events.EVENT_LOCATION, location)
    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.timestampEpochMillis)
    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timestampEpochMillis)
}

fun intentForUrl(url: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
