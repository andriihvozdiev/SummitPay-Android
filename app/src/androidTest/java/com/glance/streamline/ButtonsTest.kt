package com.glance.streamline

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.ActivityTestRule
import org.junit.Rule

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ButtonsTest {

    @get:Rule val rule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, true)

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("android.test.mvvmproject", appContext.packageName)
    }


}
