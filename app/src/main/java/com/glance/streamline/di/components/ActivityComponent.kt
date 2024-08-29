package com.glance.streamline.di.components

import com.glance.streamline.di.modules.ActivityModule
import com.glance.streamline.di.modules.ViewModelFactoryModule
import com.glance.streamline.di.modules.view_model.ActivityViewModelModule
import com.glance.streamline.di.modules.view_model.GeneralViewModelModule
import com.glance.streamline.di.modules.view_model.fragment.AuthViewModelModule
import com.glance.streamline.di.modules.view_model.fragment.MainViewModelModule
import com.glance.streamline.di.scopes.ActivityScope
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.ui.base.BaseActivity
import com.glance.streamline.ui.base.BaseDialogFragment
import com.glance.streamline.ui.base.BaseFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent(
    modules = [
        ActivityModule::class,
        ViewModelFactoryModule::class,
        ActivityViewModelModule::class,
        GeneralViewModelModule::class,
        AuthViewModelModule::class,
        MainViewModelModule::class
    ]
)
interface ActivityComponent {
    fun inject(target: BaseActivity<BaseViewModel>)
    fun inject(target: BaseFragment<BaseViewModel>)
    fun inject(target: BaseDialogFragment<BaseViewModel>)
}