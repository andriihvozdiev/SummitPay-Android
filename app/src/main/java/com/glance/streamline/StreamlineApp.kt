package com.glance.streamline

import android.app.Activity
import android.app.Application
import android.content.Context
import com.glance.streamline.di.components.AppComponent
import com.glance.streamline.di.components.DaggerAppComponent
import com.glance.streamline.di.modules.AppModule

class StreamlineApp : Application() {

    companion object {
        operator fun get(activity: Activity): StreamlineApp {
            return activity.application as StreamlineApp
        }

        operator fun get(context: Context): StreamlineApp {
            return context as StreamlineApp
        }
    }

    val appComponent: AppComponent by lazy {
        initDagger()
    }

    private fun initDagger(): AppComponent =
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
}