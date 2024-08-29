package com.glance.streamline.utils.extensions

import java.io.File


fun String.getFileSize(): Int? {
    try {
        val file = File(this)
        return (file.length() / 1024).toInt()
    } catch (e: NullPointerException) {
        return null
    }
}