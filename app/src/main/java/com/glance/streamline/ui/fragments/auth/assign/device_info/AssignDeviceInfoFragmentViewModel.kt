package com.glance.streamline.ui.fragments.auth.assign.device_info

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.domain.repository.auth.StreamlineApiRepository
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.data.entities.DeviceAssigningInfo
import com.glance.streamline.utils.extensions.android.getSharedPref
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import javax.inject.Inject

const val BUSSINES_ID_KEY = "BUSSINES_ID_KEY"
class AssignDeviceInfoFragmentViewModel @Inject constructor(
    app: Application,
    private val db: AppDatabase,
    private val streamlineApiRepository: StreamlineApiRepository
) : BaseViewModel(app) {

    private lateinit var deviceAssigningInfo: DeviceAssigningInfo

    val onDeviceAssigningStateSaved = MutableLiveData<Boolean>()

    fun saveDeviceAssigningState(deviceAssigningInfo : DeviceAssigningInfo = this.deviceAssigningInfo) {
        db.deviceAssigningDao()
            .saveDeviceAssigningInfoCompletable(deviceAssigningInfo)
            .call {
                onDeviceAssigningStateSaved.postValue(true)
            }
    }

    fun createDevice(name: String, location: String, companyNumber: String, onSuccess: () -> Unit, onErrorAction: (String) ->Unit = {}) {
        streamlineApiRepository.createDevice( name, location ,companyNumber) {
            it.unWrapResult {
                getContext().getSharedPref()?.edit {
                    putString(BUSSINES_ID_KEY, it.value.businessId)
                }
                deviceAssigningInfo = DeviceAssigningInfo(
                    deviceId =  getContext().getUniqCurrentDeviceId(),
                    location = location,
                    name = name,
                    companyNumber = companyNumber,
                    businessId = it.value.businessId
                )

               // saveDeviceAssigningState(deviceAssigningInfo)
                onSuccess()
            }.doOnError (onErrorAction)
        }.doOnError { onErrorAction(it.localizedMessage ?: it.message.orEmpty()) }
            .call()
    }

    fun getLocation() {
        Log.d("Location", "success")
    }

    override fun onCleared() {
        super.onCleared()
        onDeviceAssigningStateSaved.value = null
    }
}