package com.glance.streamline.di.components

import android.app.Application
import com.glance.streamline.di.modules.*
import com.glance.streamline.domain.repository.CallbackWrapper
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.services.SyncJobSchedulerService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    RetrofitApiModule::class,
    RepositoryModule::class,
    DatabaseModule::class
])
interface AppComponent {

    fun plus(activityModule: ActivityModule): ActivityComponent

    fun inject(app: Application)
    fun inject(target: BaseViewModel)
    fun inject(target: CallbackWrapper<Any, Any>)
    fun inject(target: SyncJobSchedulerService)
}