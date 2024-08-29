package com.glance.streamline.di.modules.view_model.fragment

import androidx.lifecycle.ViewModel
import com.glance.streamline.di.map_key.ViewModelKey
import com.glance.streamline.di.scopes.ActivityScope
import com.glance.streamline.ui.fragments.auth.assign.company_number.AssignCompanyNumberFragmentViewModel
import com.glance.streamline.ui.fragments.auth.assign.device_info.AssignDeviceInfoFragmentViewModel
import com.glance.streamline.ui.fragments.auth.assign.device_info.LocationViewModel
import com.glance.streamline.ui.fragments.auth.credentials.CredentialsFragmentViewModel
import com.glance.streamline.ui.fragments.auth.pin_code.PinCodeFragmentViewModel
import com.glance.streamline.ui.fragments.splash.SplashFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(SplashFragmentViewModel::class)
    abstract fun bindSplashFragmentViewModel(model: SplashFragmentViewModel): ViewModel

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(AssignCompanyNumberFragmentViewModel::class)
    abstract fun bindAssignCompanyNumberFragmentViewModel(model: AssignCompanyNumberFragmentViewModel): ViewModel

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(AssignDeviceInfoFragmentViewModel::class)
    abstract fun bindAssignDeviceInfoFragmentViewModel(model: AssignDeviceInfoFragmentViewModel): ViewModel


    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(PinCodeFragmentViewModel::class)
    abstract fun bindSignInFragmentViewModel(model: PinCodeFragmentViewModel): ViewModel

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(CredentialsFragmentViewModel::class)
    abstract fun bindVerificationViewModel(model: CredentialsFragmentViewModel): ViewModel

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(LocationViewModel::class)
    abstract fun bindLocationViewModel(model: LocationViewModel): ViewModel
}
