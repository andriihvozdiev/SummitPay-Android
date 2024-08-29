package com.glance.streamline.utils.extensions

import com.glance.streamline.utils.extensions.DateFormats.HH_mm
import com.glance.streamline.utils.extensions.DateFormats.ISO_8601_Timezone
import com.glance.streamline.utils.extensions.DateFormats.RFC_3339
import com.glance.streamline.utils.extensions.DateFormats.dd_MM_yyyy
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object DateFormats {
    const val ISO_8601_Timezone = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
    const val RFC3339Nano = "yyyy-MM-dd'T'HH:mm:ss.S'Z'"
    const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss"
    const val RFC_3339 = "yyyy-MM-dd'T'HH:mm:ssZ"
    const val EEEE_d = "EEEE d"
    const val A = "a"
    const val d_MMMM_yyyy = "d MMMM yyyy"
    const val HH_mm = "HH:mm"
    const val dd_MM_yyyy = "dd/MM/yyyy"
}

fun dateRandom(locale: Locale = Locale.getDefault()): Date =
    Date(dateNow(locale).time - Random().nextInt(200_000_000))

fun dateNow(locale: Locale = Locale.getDefault()): Date = Calendar.getInstance(locale).time

fun Date.toRFC_3339_English(): String{
    return SimpleDateFormat(RFC_3339, Locale.ENGLISH).format(this)
}

fun Date.parseRFC_3339(date: String, locale: Locale = Locale.getDefault()): Date? {
    return try {
        val simpleDateFormat = SimpleDateFormat(DateFormats.RFC_3339)
        simpleDateFormat.parse(date)
    } catch (e: Exception) {
        null
    }
}

fun Date.toISO_8601_Timezone(): String{
    return SimpleDateFormat(ISO_8601_Timezone, Locale.ENGLISH).format(this)
}

fun Date.toDateString(): String{
    return SimpleDateFormat(dd_MM_yyyy, Locale.ENGLISH).format(this)
}

fun Date.toOnlyDateString(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(this)
}

fun Date.toTimeString(): String {
    return SimpleDateFormat(HH_mm, Locale.ENGLISH).format(this)
}

val Date.timestampEpochMillis
    get() = time

val Date.timestampEpochSeconds
    get() = timestampEpochMillis / 1000

fun Date.isToday(locale: Locale = Locale.getDefault()): Boolean {
    return isSameDay(dateNow(), locale)
}

fun Date.setTime(locale: Locale = Locale.getDefault(), hours: Int, minutes: Int) {
    val calendar = getCalendar(locale)
    calendar.set(Calendar.HOUR_OF_DAY, hours)
    calendar.set(Calendar.MINUTE, minutes)
    calendar.set(Calendar.SECOND, 0)
    this.time = calendar.time.time
}

fun Date.getCurrentHours(locale: Locale = Locale.getDefault()): Int {
    val calendar = getCalendar(locale)
    return calendar.get(Calendar.HOUR_OF_DAY)
}

fun Date.getCurrentMinutes(locale: Locale = Locale.getDefault()): Int {
    val calendar = getCalendar(locale)
    return calendar.get(Calendar.MINUTE)
}

fun Date.getCalendar(locale: Locale = Locale.getDefault()): Calendar {
    val date = this
    return Calendar.getInstance(locale).apply { time = date }
}

fun Date?.isAfterTwoWeeks(locale: Locale = Locale.getDefault()): Boolean {
    if (this == null) return false

    val calendar = dateNow(locale).getCalendar(locale)
    calendar.apply {
        add(Calendar.WEEK_OF_YEAR, 2)
        set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE))
        set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND))
    }
    val weekStart = calendar.time
    return weekStart <= this
}

fun Date?.isNextWeek(locale: Locale = Locale.getDefault()): Boolean {
    if (this == null) return false

    val calendar = dateNow(locale).getCalendar(locale)
    calendar.apply {
        add(Calendar.WEEK_OF_YEAR, 1)
        set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE))
        set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND))
    }
    val weekStart = calendar.time

    calendar.apply {
        add(Calendar.WEEK_OF_YEAR, 1)
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE))
        set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND))
    }
    val weekEnd = calendar.time

    return weekStart <= this && weekEnd >= this
}

fun Date?.isCurrentWeek(locale: Locale = Locale.getDefault()): Boolean {
    if (this == null) return false

    val calendar = dateNow(locale).getCalendar(locale)
    calendar.apply {
        set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE))
        set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND))
    }
    val weekStart = calendar.time

    calendar.apply {
        add(Calendar.WEEK_OF_YEAR, 1)
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE))
        set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND))
    }
    val weekEnd = calendar.time
    return weekStart <= this && weekEnd >= this
}

fun Date.isYesterday(locale: Locale = Locale.getDefault()): Boolean {
    val yesterdayCalendar = dateNow().getCalendar(locale)
    yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1)

    return isSameDay(yesterdayCalendar, getCalendar(locale))
}

fun Date.isBefore(date: Date): Boolean = this.time < date.time

fun Date.isAfter(date: Date): Boolean = !isBefore(date)

fun Date.difference(date: Date, timeUnit: TimeUnit): Long {
    val diffInMillis = abs(this.time - date.time)
    return timeUnit.convert(diffInMillis, TimeUnit.MILLISECONDS)
}

fun Date.isSameDay(date: Date, locale: Locale = Locale.getDefault()): Boolean {
    return isSameDay(getCalendar(locale), date.getCalendar(locale))
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun Date?.formatDateWithFullMonth(locale: Locale = Locale.getDefault()): String? {
    if (this == null) return null
    return format(DateFormats.d_MMMM_yyyy, locale) ?: return null
}

fun Date?.formatTime(locale: Locale = Locale.getDefault()): String? {
    if (this == null) return null
    return format(DateFormats.HH_mm, locale) ?: return null
}

fun Date?.formatTimeWithPmMarker(locale: Locale = Locale.getDefault()): String? {
    if (this == null) return null
    val amPmMarker = format(DateFormats.A)?.toLowerCase(locale) ?: return null
    val time = format(DateFormats.HH_mm, locale) ?: return null
    return time + amPmMarker
}

fun Date.parseIso8601(date: String, locale: Locale = Locale.getDefault()): Date? {
    return try {
        val simpleDateFormat = SimpleDateFormat(DateFormats.ISO_8601)
        simpleDateFormat.parse(date)
    } catch (e: Exception) {
        null
    }
}

fun Date.parseIso8601WithTimezone(date: String, locale: Locale = Locale.getDefault()): Date? {
    return try {
        SimpleDateFormat(DateFormats.ISO_8601_Timezone, locale).parse(date)
    } catch (e: Exception) {
        null
    }
}

fun Date.parseRFC3339Nano(date: String, locale: Locale = Locale.getDefault()): Date? {
    return try {
        val simpleDateFormat = SimpleDateFormat(DateFormats.RFC3339Nano)
        simpleDateFormat.parse(date)
    } catch (e: Exception) {
        null
    }
}

fun Date.formatRFC3339Nano(locale: Locale = Locale.getDefault()): String? {
    return format(DateFormats.RFC3339Nano, locale)
}

fun Date.formatIso8601(locale: Locale = Locale.getDefault()): String? {
    return format(DateFormats.ISO_8601, locale)
}

fun Date.formatIso8601WithTimezone(locale: Locale = Locale.getDefault()): String? {
    return format(DateFormats.ISO_8601_Timezone, locale)
}

fun Date.format(format: String, locale: Locale = Locale.getDefault()): String? {
    return try {
        SimpleDateFormat(format, locale).format(this)
    } catch (e: Exception) {
        null
    }
}
