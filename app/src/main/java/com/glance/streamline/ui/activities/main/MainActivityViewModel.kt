package com.glance.streamline.ui.activities.main

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.data.entities.UserLogoutTimeout
import com.glance.streamline.data.entities.UserModel
import com.glance.streamline.data.entities.ZReportInfo
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.di.modules.RetrofitApiModule
import com.glance.streamline.domain.model.LoginRecord
import com.glance.streamline.domain.model.payment.PaymentResultModel
import com.glance.streamline.domain.repository.auth.StreamlineApiRepository
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.ui.fragments.splash.DeviceAssignScreenState
import com.glance.streamline.ui.fragments.splash.SignInScreenState
import com.glance.streamline.ui.fragments.splash.SplashScreenState
import com.glance.streamline.utils.extensions.android.getSharedPref
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import com.pax.dal.IDAL
import com.pax.dal.IScanner
import com.pax.dal.entity.EScannerType
import com.pax.neptunelite.api.NeptuneLiteUser
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainActivityViewModel @Inject constructor(
    val app: Application,
    private val db: AppDatabase,
    private val streamlineApiRepository: StreamlineApiRepository
) : BaseViewModel(app) {

    lateinit var dal: IDAL

    val logoutLiveData = MutableLiveData<SplashScreenState>()
    val userLiveData = MutableLiveData<UserModel>()
    val logoutTimeLeft = MutableLiveData<Long>()
    val logoutTimerData = MutableLiveData<UserLogoutTimeout>()
    val barcodeData = MutableLiveData<String>()
    val cardPaymentResult = MutableLiveData<PaymentResultModel>()
    val xReportResults = MutableLiveData<List<ZReportInfo>>()
    val zReportResults = MutableLiveData<List<ZReportInfo>>()

    private var timerDisposable: Disposable? = null
    private var checkUserLoginTimer: Disposable? = null

    private val defaultTimeout = /*TimeUnit.SECONDS.toSeconds(60)*/TimeUnit.HOURS.toSeconds(4)

    fun getLoginRecords() {
        streamlineApiRepository.getLoginRecords {
            it.unWrapResult(null, false) {
                saveLoginRecords(it.value)
            }
        }.call()
    }

    private fun saveLoginRecords(arrLoginRecords: ArrayList<LoginRecord>) {

        if (arrLoginRecords.isNotEmpty()) {
            db.loginRecordsDao()
                .deleteLoginRecords()
                .call {
                    db.loginRecordsDao()
                        .saveLoginRecordsCompletable(arrLoginRecords)
                        .call()
                }
        }
    }

    fun getLogoutTimeoutData() {
        db.userTimeoutDao().getTimeout().call {
            logoutTimerData.value = it
        }
    }

    fun getLogoutTimeLeft() {
        db.userTimeoutDao().getTimeLeft().call {
            it?.let { setTimeoutData(it) } ?: setLogoutTimeout(defaultTimeout)
        }
    }

    fun logout(shouldReassignDevice: Boolean = false) {
        checkUserLoginTimer?.dispose()
        val requests = arrayListOf(
            db.userInfoDao().deleteUser(),
//            db.userTimeoutDao().deleteTimeout()
        )

        if (app.getSharedPref()?.contains(RetrofitApiModule.JWT_TOKEN_KEY) == true) {
            app.getSharedPref()?.edit {
                remove(RetrofitApiModule.JWT_TOKEN_KEY)
            }
        }

        if (shouldReassignDevice)
            requests.add(db.deviceAssigningDao().deleteDeviceAssigningInfo())
        Maybe.zip(requests) {}.call {
            logoutLiveData.postValue(
                if (shouldReassignDevice) DeviceAssignScreenState
                else SignInScreenState
            )
        }
    }

    fun startCheckingLastUserLogin() {
        checkUserLoginTimer?.dispose()
        checkUserLoginTimer = Observable.interval(6L, TimeUnit.SECONDS)
            .call {

                val keyguardManager = getContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

                if (keyguardManager.inKeyguardRestrictedInputMode()) {
                    logout()
                } else {
                    streamlineApiRepository.checkLastUserLogin(
                        getContext().getUniqCurrentDeviceId()
                    ).doOnError {
                        when {
                            it is HttpException && it.code() == 403 -> {
                                logout()
                            }
                            else -> {
                                // onError(it)
                            }
                        }
                    }.call(shouldShowLoading = false)
                }
            }

    }

    fun setNewUser(userModel: UserModel) {
        userLiveData.value = userModel
    }

    fun isAdmin(): Boolean {
        userLiveData.value?.let {
            if (it.role == "admin") {
                return true
            }
        }
        return false
    }

    fun setLogoutTimeout(timeout: Long) {
        UserLogoutTimeout(timeout, Date()).let {
            db.userTimeoutDao().saveTimeoutCompletable(it).call {
                setTimeoutData(timeout)
            }
        }
    }

    private fun setTimeoutData(timeLeft: Long) {
        logoutTimeLeft.value = timeLeft
    }

    private fun setLogoutTimeoutTimeLeft(timeLeft: Long) {
        db.userTimeoutDao().updateTimeout(timeLeft).call()
    }

    fun startLogoutTimeoutTimer(timeoutSeconds: Long) {
        timerDisposable?.dispose()
        timerDisposable = Observable.interval(1L, TimeUnit.SECONDS)
            .take(timeoutSeconds)
            .map { timeoutSeconds - it - 1 }
            .call {
                setLogoutTimeoutTimeLeft(it)
                if (it == 0L) logout()
                else logoutTimeLeft.value = it
            }
    }

    // read barcode scanner
    fun readScanner() {
        dal = NeptuneLiteUser.getInstance().getDal(getContext())

        val scanner = dal.getScanner(EScannerType.REAR)

        scanner.setContinuousTimes(0)
        scanner.setContinuousInterval(100)
        scanner.setTimeOut(0)
        scanner.open()

        scanner.start(object : IScanner.IScanListener {
            override fun onFinish() {
            }

            override fun onCancel() {

            }

            override fun onRead(p0: String?) {
                barcodeData.postValue(p0 ?: "")
            }
        })

    }

    fun printXReport() {
        db.zReportDao()
            .getAllZReportsInfo()
            .call {
                it?.let { xReportInfos ->
                    xReportResults.postValue(xReportInfos)
                }
            }
    }

    fun printZReport() {
        db.zReportDao()
            .getAllZReportsInfo()
            .call {
                it?.let { zReportInfos ->
                    zReportResults.postValue(zReportInfos)
                }
            }
    }

    fun resetZReport() {
        db.zReportDao().deleteAllZReportInfo().call()
    }

    override fun onCleared() {
        super.onCleared()
        logoutLiveData.value = null
        userLiveData.value = null
        logoutTimeLeft.value = null
        logoutTimerData.value = null
        xReportResults.value = null
        zReportResults.value = null
        timerDisposable?.dispose()
        checkUserLoginTimer?.dispose()
    }
}