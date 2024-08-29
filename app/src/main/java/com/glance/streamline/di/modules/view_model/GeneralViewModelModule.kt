package com.glance.streamline.di.modules.view_model

import androidx.lifecycle.ViewModel
import com.glance.streamline.di.map_key.ViewModelKey
import com.glance.streamline.di.scopes.ActivityScope
import com.glance.streamline.ui.base.EmptyViewModel
import com.glance.streamline.ui.fragments.connection_error.ConnectionErrorFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class GeneralViewModelModule {

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(EmptyViewModel::class)
    abstract fun bindEmptyViewModel(model: EmptyViewModel): ViewModel

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(ConnectionErrorFragmentViewModel::class)
    abstract fun bindConnectionErrorViewModel(model: ConnectionErrorFragmentViewModel): ViewModel

}
