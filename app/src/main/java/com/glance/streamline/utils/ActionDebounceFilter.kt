package com.glance.streamline.utils

class ActionDebounceFilter() {
    private var lastClickedTime = 0L
    val _time: Long = 400
    fun filterAction(time: Long = _time): Boolean {
        val currentTimeMs = System.currentTimeMillis()
        val diff = currentTimeMs - lastClickedTime
        return if (diff >= time) {
            lastClickedTime = currentTimeMs
            true
        } else {
            false
        }
    }
}
