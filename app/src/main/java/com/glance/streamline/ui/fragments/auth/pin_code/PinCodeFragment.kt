package com.glance.streamline.ui.fragments.auth.pin_code

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glance.streamline.BuildConfig
import com.glance.streamline.R
import com.glance.streamline.data.entities.UserModel
import com.glance.streamline.domain.repository.auth.StreamlineApiRepository
import com.glance.streamline.ui.activities.main.MainActivityViewModel
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.utils.extensions.android.Result
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.isTabletDevice
import com.glance.streamline.utils.extensions.android.observe
import com.glance.streamline.utils.extensions.android.view.shake
import kotlinx.android.synthetic.main.fragment_pin_code.*

class PinCodeFragment : BaseFragment<PinCodeFragmentViewModel>() {

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): PinCodeFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }

    enum class PinCodes(val code: String) {
        ONE("1"),
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        ZERO("0"),
        BACKSPACE("Backspace")
    }

    override fun layout(): Int = R.layout.fragment_pin_code

    private val mainActivityViewModel by lazy { baseActivity.injectViewModel(viewModelFactory) as MainActivityViewModel }

    private val inputsArray by lazy {
        arrayListOf(
            pin_code_input_text_view_1,
            pin_code_input_text_view_2,
            pin_code_input_text_view_3,
            pin_code_input_text_view_4
        )
    }

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initClicks()
            initObservers()
            viewModel.loadDeviceInfo()
        }
    }

    private fun initObservers() {
        viewModel.pinCodeLiveData.observe(this, ::inputCode)
        viewModel.loginResponseLiveData.observe(this, ::onSignedIn)
        viewModel.blockedDeviceLiveData.observe(this, ::changeTitleIfNeeded)
        viewModel.deviceInfoLiveData.observe(this, {
            val versionName: String = BuildConfig.VERSION_NAME
            val strDeviceInfo = it.companyNumber + "\n" + it.name + "\n" + it.location + "\nversion: " + versionName
            pin_code_info_text_view.text = strDeviceInfo
        })
        viewModel.errorMessageLiveData.observe(this, {
            if (it.isNotEmpty()) showErrorSnack(it)
        })
    }

    private fun onSignedIn(userUserModel: UserModel) {
        mainActivityViewModel.apply {
            setNewUser(userUserModel)
        }

        if (baseContext.isTabletDevice())
            findNavController().navigate(PinCodeFragmentDirections.actionSignInFragmentToCheckoutLandscapeFragment())
        else
            findNavController().navigate(PinCodeFragmentDirections.actionSignInFragmentToCheckoutPortraitFragment())
    }

    override fun onLoadingStateError(errorState: Result<*>) {
        super.onLoadingStateError(errorState)
        changeTitleIfNeeded(errorState)
    }

    private fun changeTitleIfNeeded(errorState: Result<*>?) {
        if (errorState != null
            && errorState is StreamlineApiRepository.ExceededAttemptsError
            && errorState.value == null
        ) pin_code_title_text_view.text = getString(R.string.login_pin_code_title_error)
        else pin_code_title_text_view.text = getString(R.string.login_pin_code_title)
    }

    private fun initClicks() {
        pin_code_button_1.setOnClickListener()
        pin_code_button_2.setOnClickListener()
        pin_code_button_3.setOnClickListener()
        pin_code_button_4.setOnClickListener()
        pin_code_button_5.setOnClickListener()
        pin_code_button_6.setOnClickListener()
        pin_code_button_7.setOnClickListener()
        pin_code_button_8.setOnClickListener()
        pin_code_button_9.setOnClickListener()
        pin_code_button_0.setOnClickListener()
        pin_code_button_backspace.setOnClickListener()
        pin_code_button_login.setOnClickListener()
    }

    private fun View.setOnClickListener() {
        onClick(50) {
            when (it.id) {
                pin_code_button_1.id -> inputCode(PinCodes.ONE)
                pin_code_button_2.id -> inputCode(PinCodes.TWO)
                pin_code_button_3.id -> inputCode(PinCodes.THREE)
                pin_code_button_4.id -> inputCode(PinCodes.FOUR)
                pin_code_button_5.id -> inputCode(PinCodes.FIVE)
                pin_code_button_6.id -> inputCode(PinCodes.SIX)
                pin_code_button_7.id -> inputCode(PinCodes.SEVEN)
                pin_code_button_8.id -> inputCode(PinCodes.EIGHT)
                pin_code_button_9.id -> inputCode(PinCodes.NINE)
                pin_code_button_0.id -> inputCode(PinCodes.ZERO)
                pin_code_button_backspace.id -> inputCode(PinCodes.BACKSPACE)
                pin_code_button_login.id -> {
                    signIn(getFullCode())
                }
            }
        }
    }

    private fun inputCode(pinCode: PinCodes) {
        setErrorBackground(false)
        viewModel.savePinCode(pinCode)
    }

    private fun inputCode(pinCode: String) {
        inputsArray.forEachIndexed { index, textView ->
            textView.text = pinCode.getOrNull(index)?.toString() ?: ""
        }
    }

    private fun signIn(pinCode: String) {
        if (validatePinCode(pinCode))
            viewModel.signIn(pinCode)
    }

    private fun validatePinCode(pinCode: String): Boolean {
        return if (pinCode.length == viewModel.maxCodeLength) {
            setErrorBackground(false)
            true
        } else {
            setErrorBackground(true)
            pin_code_fields_layout.shake()
            false
        }
    }

    private fun setErrorBackground(isError: Boolean) {
        inputsArray.forEach {
            it.setBackgroundResource(
                if (!isError) R.drawable.bg_rounded_5px_border_white
                else R.drawable.bg_rounded_5px_border_red
            )
        }
    }

    private fun getFullCode() = inputsArray.joinToString(separator = "") { it.text ?: "" }
}

