package com.glance.streamline.ui.fragments.auth.pin_code

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.R
import com.glance.streamline.data.entities.DeviceAssigningInfo
import com.glance.streamline.data.entities.DeviceModel
import com.glance.streamline.data.entities.UserModel
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.di.modules.RetrofitApiModule
import com.glance.streamline.domain.repository.auth.StreamlineApiRepository
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.ui.fragments.auth.assign.device_info.BUSSINES_ID_KEY
import com.glance.streamline.utils.extensions.android.Success
import com.glance.streamline.utils.extensions.android.getSharedPref
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import com.glance.streamline.utils.extensions.getSchedulers
import retrofit2.HttpException
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

const val ASSIGNED_HUB_ID_KEY = "ASSIGNED_HUB_ID_KEY"
class PinCodeFragmentViewModel @Inject constructor(
    val app: Application,
    private val authRepo: StreamlineApiRepository,
    private val db: AppDatabase
) : BaseViewModel(app) {
    private val mBussinesId: String by lazy { getContext().getSharedPref()?.getString(BUSSINES_ID_KEY, "").orEmpty() }

    private var unSuccessfulAttemptsCount = 0
    val maxCodeLength = 4

    val pinCodeLiveData = MutableLiveData<String>()
    val loginResponseLiveData = MutableLiveData<UserModel>()
    val blockedDeviceLiveData = MutableLiveData<StreamlineApiRepository.ExceededAttemptsError<UserModel>>()

    val deviceInfoLiveData = MutableLiveData<DeviceAssigningInfo>()

    val errorMessageLiveData = MutableLiveData<String>("")

    fun loadDeviceInfo() {
        db.deviceAssigningDao()
            .getDeviceAssigningInfo()
            .call { deviceAssignInfo ->
                deviceAssignInfo?.let {
                    deviceInfoLiveData.postValue(it)
                }
            }
    }

    fun savePinCode(pinCode: PinCodeFragment.PinCodes) {
        var newCode = pinCodeLiveData.value ?: ""
        if (pinCode == PinCodeFragment.PinCodes.BACKSPACE)
            newCode = newCode.dropLast(1)
        else if (newCode.length < maxCodeLength)
            newCode += pinCode.code
        pinCodeLiveData.postValue(newCode)
    }

//    fun signIn(pinCode: String) {
//        authRepo.login(pinCode, unSuccessfulAttemptsCount).call { result ->
//            if (result is StreamlineApiRepository.UserNotFoundError) onUserNotFound()
//            if (result is StreamlineApiRepository.ExceededAttemptsError) onExceededAttempts(result)
//            else onLoginSuccess(result)
//        }
//    }

    fun signIn(pinCode: String) {
        authRepo.login(pinCode, bussinesId = mBussinesId, deviceToken = app.getUniqCurrentDeviceId(), onResponse = {
            it.unWrapResult{ result ->
                if (result is StreamlineApiRepository.UserNotFoundError<*>) onUserNotFound()
                //if (result is StreamlineApiRepository.ExceededAttemptsError<*>) onExceededAttempts(result)
                else {
                    getContext().getSharedPref()?.edit {
                        putString(RetrofitApiModule.JWT_TOKEN_KEY, result.value.jwt)
                    }
                    getContext().getSharedPref()?.edit {
                        putString(ASSIGNED_HUB_ID_KEY, result.value.hub_id)
                    }
                    db.deviceAssigningDao().getDeviceAssigningInfo().getSchedulers().call {
                        it?.let {
                            onLoginSuccess(
                                UserModel(
                                    token = result.value.jwt,
                                    id = 0,
                                    user_name = result.value.user_name,//it.name,
                                    role = result.value.role,
                                    devices = listOf(DeviceModel(it.deviceId, it.name)),
                                    hub = it.location,
                                    isAdmin = false
                                )
                            )
                        }
                    }
                }
            }
        }).doOnError {
            Log.d("=======", it.toString())
            if (it is ConnectException) {
                signInWithDB(pinCode)
            } else if (it is HttpException){
                val t = Throwable(message = "Login Failed. Try again!")
                onError(t)
                errorMessageLiveData.postValue("Login Failed. Try again!")
            }
        }.call()
    }

    private fun signInWithDB(pinCode: String) {
        onStartLoading()
        db.loginRecordsDao()
            .getLoginRecords()
            .call { arrLoginRecords ->
                arrLoginRecords.forEach { loginRecordInfo ->
                    val saltBase64Decode = Base64.decode(loginRecordInfo.salt, Base64.DEFAULT)
                    val sha = getSHA512PinCode(pinCode, saltBase64Decode)
                    val encodedString = Base64.encodeToString(sha, Base64.DEFAULT).replace("\n", "")
                    if (encodedString == loginRecordInfo.pinHash) {

                        val assignedHubId = getContext().getSharedPref()?.getString(ASSIGNED_HUB_ID_KEY, "").orEmpty()

                        if (assignedHubId == loginRecordInfo.assignedTo) {
                            db.deviceAssigningDao().getDeviceAssigningInfo().getSchedulers().call {
                                it?.let {
                                    onLoginSuccess(
                                        UserModel(
                                            token = "", //loginRecordInfo.jwt,
                                            id = 0,
                                            user_name = loginRecordInfo.name,
                                            role = loginRecordInfo.role,
                                            devices = listOf(DeviceModel(it.deviceId, it.name)),
                                            hub = it.location,
                                            isAdmin = false
                                        )
                                    )
                                }
                            }
                        } else {
                            errorMessageLiveData.postValue("Login Failed. Try again!")
                        }
                        return@forEach
                    }
                }
                onStopLoading()
            }

    }

    private fun getSHA512PinCode(pinCode: String, saltByteArray: ByteArray): ByteArray {
        var generatedPassword: ByteArray
        try {
            val md = MessageDigest.getInstance("SHA-512")
            val pinCodeByte = pinCode.toByteArray(StandardCharsets.UTF_8)
            generatedPassword = md.digest(pinCodeByte.plus(saltByteArray))
        } catch (e: NoSuchAlgorithmException) {
            generatedPassword = ByteArray(0)
            e.printStackTrace()
        }
        return generatedPassword
    }

    private fun onUserNotFound() {
        unSuccessfulAttemptsCount++
    }

    private fun onExceededAttempts(blockedError: StreamlineApiRepository.ExceededAttemptsError<UserModel>) {
        val result = if (blockedError.value != null && blockedError.value.isAdmin) {
            unSuccessfulAttemptsCount = 0
            Success(
                blockedError.value,
                getString(R.string.login_pin_code_unblock_success, blockedError.value.user_name)
            )
        } else {
            unSuccessfulAttemptsCount++
            blockedError
        }
        result.unWrapResult()
        blockedDeviceLiveData.postValue(blockedError)
    }

    private fun onLoginSuccess(result: UserModel) {
        //result.unWrapResult {
            db.userInfoDao().saveUserCompletable(result).call {
                unSuccessfulAttemptsCount = 0
                loginResponseLiveData.postValue(result)
            }
        //}
    }

    override fun onCleared() {
        super.onCleared()
        loginResponseLiveData.value = null
        blockedDeviceLiveData.value = null
        pinCodeLiveData.value = null
        unSuccessfulAttemptsCount = 0
        errorMessageLiveData.value = ""
    }
}
