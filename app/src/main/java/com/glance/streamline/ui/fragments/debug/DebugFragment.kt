package com.glance.streamline.ui.fragments.debug

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glance.streamline.R
import com.glance.streamline.ui.adapters.recycler_view.DemoScreensAdapter
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.ui.base.EmptyViewModel
import com.glance.streamline.ui.models.DemoScreenModel
import com.glance.streamline.utils.extensions.android.injectViewModel
import kotlinx.android.synthetic.main.fragment_debug.*

class DebugFragment : BaseFragment<EmptyViewModel>() {
    override fun layout(): Int = R.layout.fragment_debug

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initScreensList()
        }
    }

    private fun initScreensList() {
        val screensList = arrayListOf(
            DemoScreenModel("Pin-code login", R.id.pinCodeFragment),
            DemoScreenModel("Credentials login", R.id.credentialsFragment)
        )
        debug_screens_list.adapter = DemoScreensAdapter(screensList, this) { id, args ->
            open(id, args)
        }
    }

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): EmptyViewModel {
        return injectViewModel(viewModelFactory)
    }

    private fun open(fragmentId: Int, args: Any? = null) {
        if (args == null){
            findNavController().navigate(fragmentId)
        }
    }
}
