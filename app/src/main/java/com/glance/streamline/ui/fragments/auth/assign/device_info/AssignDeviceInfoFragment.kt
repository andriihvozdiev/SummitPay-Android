package com.glance.streamline.ui.fragments.auth.assign.device_info

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.glance.streamline.R
import com.glance.streamline.domain.repository.ApiErrors
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.utils.extensions.android.displayMetrics
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.isTabletDevice
import com.glance.streamline.utils.extensions.android.observe
import com.glance.streamline.utils.extensions.android.view.onTextChanged
import com.glance.streamline.utils.extensions.hasPermission
import kotlinx.android.synthetic.main.dialog_register_device_success.*
import kotlinx.android.synthetic.main.fragment_assign_device_info.*


class AssignDeviceInfoFragment : BaseFragment<AssignDeviceInfoFragmentViewModel>() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 210
    }

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): AssignDeviceInfoFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }

    private var isSuccessDialogShowing = false

    private val companyNumber: String by lazy{  navArgs<AssignDeviceInfoFragmentArgs>().value.companyNumber.orEmpty() }

    private val locationViewModel by lazy { injectViewModel(viewModelFactory) as LocationViewModel }

    override fun layout(): Int = R.layout.fragment_assign_device_info

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initClicks()
            initEditTexts()
            validateFields()
            observeViewModel()
//            getLocation()
        }
    }

    private fun initClicks() {
        register_device_button.onClick {
            viewModel.createDevice(device_name_edit_text.text.toString(), device_location_edit_text.text.toString(), companyNumber,{
                if (isSuccessDialogShowing.not()){
                    isSuccessDialogShowing = true
                    showSuccessDialog()
                }
            },{
                if (it.contains(ApiErrors.ERROR_DEVICE_ALREADY_EXIST.message, true) ||
                    it.contains(ApiErrors.ERROR_DEVICE_ALREADY_EXIST.message, true)){
                    findNavController().navigate(AssignDeviceInfoFragmentDirections.actionToPinCodeFragment())
                }
            })
        }
    }

    private fun initEditTexts() {
        device_name_edit_text.onTextChanged { validateFields() }
        device_location_edit_text.onTextChanged { validateFields() }
    }

    private fun validateFields() {
        register_device_button.isEnabled =
            device_name_edit_text.text?.isNotBlank() == true && device_location_edit_text.text?.isNotBlank() == true
    }

    private fun observeViewModel() {
        locationViewModel.listenViewModelUpdates()
        locationViewModel.receivingLocationUpdates.observe(
            this,
            locationViewModel::startLogoutTimeoutTimer
        )
        locationViewModel.receivingLocationUpdatesTimer.observe(this, ::onLocationTimer)
        locationViewModel.locationLiveData.observe(this, ::onLocationFound)
        viewModel.onDeviceAssigningStateSaved.observe(this, ::onDeviceAssigningStateSaved)
    }

    private fun onDeviceAssigningStateSaved(isSaved: Boolean) {
        if (isSaved) {
            openPinCodeFragment()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onLocationFound(location: Location) {
        device_location_edit_text.setText("${location.latitude}, ${location.longitude}")
    }

    private fun onLocationTimer(shouldChangeAnimation: Boolean) {
        val drawable = if (shouldChangeAnimation) R.drawable.ic_location_searching
        else R.drawable.ic_location_found
        device_location_edit_text.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawable, 0)
    }

    private fun showSuccessDialog() {
        Dialog(baseActivity, R.style.FullHeightDialog).apply {
            setContentView(R.layout.dialog_register_device_success)
            if (baseActivity.isTabletDevice()) {
                val width = (baseActivity.displayMetrics.widthPixels * 0.6).toInt()
                window?.setLayout(width, WRAP_CONTENT)
            }
            setOnDismissListener {
                viewModel.saveDeviceAssigningState()
            }
            show()
            close_dialog_button.onClick {
                dismiss()
                isSuccessDialogShowing = false
            }
            sign_in_button.onClick {
                dismiss()
                isSuccessDialogShowing = false
            }
        }
    }

    private fun openPinCodeFragment() {
        findNavController().navigate(
            AssignDeviceInfoFragmentDirections.actionToPinCodeFragment()
        )
    }

    private fun getLocation() {
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        val permissionAccessCoarseLocationApproved =
            baseContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionAccessCoarseLocationApproved) {
            locationViewModel.startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkPermissionRationale() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        ) showLocationRationaleDialog()
        else showPermissionDisabledDialog()
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showLocationRationaleDialog() {
        showDialog(
            R.string.location_rationale_dialog_title,
            R.string.location_rationale_dialog_message,
            R.string.location_rationale_dialog_allow,
            ::requestLocationPermission
        )
    }

    private fun showPermissionDisabledDialog() {
        showDialog(
            R.string.location_permission_disabled_dialog_title,
            R.string.location_permission_disabled_dialog_message,
            R.string.location_permission_disabled_dialog_go_to_settings
        ) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", baseContext.packageName, null)
            }
            startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showDialog(
        @StringRes titleMessage: Int,
        @StringRes contentMessage: Int,
        @StringRes positiveButtonMessage: Int,
        onPositiveAction: () -> Unit
    ) {
        AlertDialog.Builder(baseActivity)
            .setTitle(titleMessage)
            .setMessage(contentMessage)
            .setPositiveButton(positiveButtonMessage) { _, _ ->
                onPositiveAction()
            }
            .setNegativeButton(R.string.text_cancel, null)
            .create()
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            checkLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) checkPermissionRationale()
            else locationViewModel.startLocationUpdates()
        }
    }

}