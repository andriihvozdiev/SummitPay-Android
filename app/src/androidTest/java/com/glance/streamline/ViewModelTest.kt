package com.glance.streamline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class ViewModelTest {
    inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule val rule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, true)
}
