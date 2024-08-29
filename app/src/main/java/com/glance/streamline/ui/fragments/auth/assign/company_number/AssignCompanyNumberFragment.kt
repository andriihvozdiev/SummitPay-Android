package com.glance.streamline.ui.fragments.auth.assign.company_number

import android.view.View
import androidx.core.text.trimmedLength
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glance.streamline.R
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.view.onTextChanged
import kotlinx.android.synthetic.main.fragment_assign_company_number.*

class AssignCompanyNumberFragment : BaseFragment<AssignCompanyNumberFragmentViewModel>() {

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): AssignCompanyNumberFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }

    override fun layout(): Int = R.layout.fragment_assign_company_number

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initClicks()
            initEditTexts()
            validateFields()
        }
    }

    private fun initClicks() {
        next_button.onClick {
            findNavController().navigate(
                AssignCompanyNumberFragmentDirections.actionToAssignDeviceInfoFragment(company_number_edit_text.text.toString())
            )
        }
    }

    private fun initEditTexts() {
        company_number_edit_text.onTextChanged { validateFields() }
    }

    private fun validateFields() {
        next_button.isEnabled = company_number_edit_text.text?.trimmedLength() == 10
    }
}