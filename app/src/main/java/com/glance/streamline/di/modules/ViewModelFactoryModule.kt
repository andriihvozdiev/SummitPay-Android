package com.glance.streamline.di.modules

import com.glance.streamline.di.scopes.ActivityScope
import com.glance.streamline.mvvm.DaggerViewModelFactory
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {
    @Binds
    @ActivityScope
    abstract fun bindViewModelFactory(viewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory
}