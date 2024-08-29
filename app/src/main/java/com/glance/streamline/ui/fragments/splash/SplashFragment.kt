package com.glance.streamline.ui.fragments.splash

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glance.streamline.R
import com.glance.streamline.ui.activities.main.MainActivityViewModel
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.isTabletDevice
import com.glance.streamline.utils.extensions.android.observe

class SplashFragment : BaseFragment<SplashFragmentViewModel>() {

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): SplashFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }

    override fun layout(): Int = R.layout.fragment_splash

    private val mainActivityViewModel by lazy { baseActivity.injectViewModel(viewModelFactory) as MainActivityViewModel }

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            listenUpdates()
        }
    }

    private fun listenUpdates() {
        viewModel.screenState.observe(this) {
            findNavController().navigate(
                when (it) {
                    is MainScreenState -> {
                        mainActivityViewModel.setNewUser(it.userModel)
                        if (baseContext.isTabletDevice())
                            SplashFragmentDirections.actionSplashFragmentToCheckoutLandscapeFragment()
                        else
                            SplashFragmentDirections.actionSplashFragmentToCheckoutPortraitFragment()
                    }
                    is SignInScreenState ->
                        SplashFragmentDirections.actionToPinCodeFragment()
                    is DeviceAssignScreenState ->
                        SplashFragmentDirections.actionToAssignCompanyNumberFragment()
                    is DebugScreenState ->
                        SplashFragmentDirections.actionSplashFragmentToDebugFragment()
                }
            )
        }
    }
}
