package com.glance.streamline.ui.fragments.auth.credentials

import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.glance.streamline.R
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.utils.extensions.android.injectViewModel

class CredentialsFragment : BaseFragment<CredentialsFragmentViewModel>() {

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): CredentialsFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }

    override fun layout(): Int = R.layout.fragment_credentials

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initClicks()
            initObservers()
        }
    }

    private fun initObservers() {

    }

    private fun initClicks() {

    }

}