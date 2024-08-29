package com.glance.streamline.di.modules.view_model

import androidx.lifecycle.ViewModel
import com.glance.streamline.di.map_key.ViewModelKey
import com.glance.streamline.di.scopes.ActivityScope
import com.glance.streamline.ui.activities.main.MainActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ActivityViewModelModule {

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainActivityViewModel(model: MainActivityViewModel): ViewModel
}