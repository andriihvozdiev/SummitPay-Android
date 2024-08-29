package com.glance.streamline.di.modules

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.room.Room
import com.glance.streamline.data.room.AppDatabase
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Singleton

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Provides
    @Singleton
    fun provideContext(): Context = app.applicationContext

    @Provides
    fun provideRequestsDisposable() = CompositeDisposable()

    @Provides
    @Singleton
    fun provideToast(context: Context): Toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
}