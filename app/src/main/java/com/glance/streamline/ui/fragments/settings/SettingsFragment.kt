package com.glance.streamline.ui.fragments.settings

import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.glance.streamline.R
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.ui.base.EmptyViewModel
import com.glance.streamline.utils.extensions.android.injectViewModel

class SettingsFragment : BaseFragment<EmptyViewModel>(){

    override fun layout(): Int = R.layout.fragment_settings

    override fun initialization(view: View, isFirstInit: Boolean) {
        initObservers()
        initClicks()
    }

    private fun initClicks() {

    }


    private fun initObservers() {
        //checkoutFragmentViewModel.productLayoutLiveData.observe(this, ::initProductCategoriesDialog)
        //checkoutFragmentViewModel.selectedLayoutLiveData.observe(this, ::onProductCategoryAdded)
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): EmptyViewModel {
        return injectViewModel(viewModelFactory)
    }
}