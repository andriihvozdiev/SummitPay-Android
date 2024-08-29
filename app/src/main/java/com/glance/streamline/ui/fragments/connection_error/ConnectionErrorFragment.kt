package com.glance.streamline.ui.fragments.connection_error

import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.observe
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.glance.streamline.R
import kotlinx.android.synthetic.main.fragment_connection_error.*

class ConnectionErrorFragment : BaseFragment<ConnectionErrorFragmentViewModel>() {

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): ConnectionErrorFragmentViewModel {
        return baseActivity.injectViewModel(viewModelFactory)
    }

    override fun layout() = R.layout.fragment_connection_error

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initClicks()
            listenUpdates()
        }
    }

    override fun shouldExit() = false

    private fun initClicks(){
        retry_button.onClick {
            viewModel.callRequest()
        }
    }

    private fun listenUpdates(){
        viewModel.resentSuccessfullyData.observe(this){
            if (it) onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearAllData()
    }
}