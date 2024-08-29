package com.glance.streamline.utils.extensions.android

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.provider.Settings
import android.view.LayoutInflater
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.glance.streamline.BuildConfig
import com.glance.streamline.R

fun Context.getColorsArray(@ArrayRes colorArrayResource: Int): ArrayList<Int> {
    val colors = resources.obtainTypedArray(colorArrayResource)
    val colorsList = arrayListOf<Int>()
    for (i in 0 until colors.length())
        colorsList.add(colors.getResourceId(i, android.R.color.white))
    colors.recycle()
    return colorsList
}

fun Context.getColorRes(@ColorRes colorResourceId: Int) =
    ContextCompat.getColor(this, colorResourceId)

fun Context.getDefaultBottomBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

fun Context.calculateStatusBarHeightByResources(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

fun Context?.getSharedPref() = this?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

fun Context.isTabletDevice() = resources.getBoolean(R.bool.isTablet)

fun Context.getInflater() = LayoutInflater.from(this)

@SuppressLint("HardwareIds")
fun Context.getUniqCurrentDeviceId(): String {
    //return "bfe20ac34334664b"
   return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID).let {
       if (it.isNullOrBlank()){
           val wm = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
           wm.connectionInfo.macAddress
       }else{ it }
   }
}