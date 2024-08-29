package com.glance.streamline.utils.extensions.android

import androidx.core.graphics.ColorUtils

fun Int.isDarkColor() = ColorUtils.calculateLuminance(this) < 0.5