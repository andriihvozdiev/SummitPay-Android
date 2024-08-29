package com.glance.streamline.ui.fragments.splash

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.data.entities.UserModel
import io.reactivex.Single
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragmentViewModel @Inject constructor(
    app: Application,
    private val db: AppDatabase
) : BaseViewModel(app) {

    val screenState = MutableLiveData<SplashScreenState>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun startTimer() {
        Single.timer(1500, TimeUnit.MILLISECONDS)
            .call(false) {
                checkUser()
            }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stopWork() {

    }

    private fun checkUser() {
        saveLog("checkUser")
        db.userInfoDao().getUser().call { user ->
            user?.let { checkLogoutTimeout(it) } ?: checkDeviceAssigningState()
        }
    }

    private fun checkLogoutTimeout(user: UserModel) {
        saveLog("checkLogoutTimeout")
        db.userTimeoutDao().getTimeout().call {
            it?.let {
                val timeLeft = TimeUnit.MILLISECONDS.toSeconds(
                    (it.startDate.time + TimeUnit.SECONDS.toMillis(it.timeoutSeconds)) - Date().time
                )
                saveLog("timeLeft: $timeLeft")
                if (timeLeft > 0) {
                    db.userTimeoutDao().updateTimeout(timeLeft).call {
                        if (it != null) goToScreen(MainScreenState(user))
                        else checkDeviceAssigningState()
                    }
                } else {
                    db.userTimeoutDao().deleteTimeout().call {
                        checkDeviceAssigningState()
                    }
                }
            } ?: goToScreen(MainScreenState(user))
        }
    }

    private fun checkDeviceAssigningState() {
        db.deviceAssigningDao().getDeviceAssigningInfo().call { info ->
            if (info?.isDeviceAssigned() == true) goToScreen(SignInScreenState)
            else goToScreen(DeviceAssignScreenState)
        }
    }

    private fun goToScreen(screen: SplashScreenState) {
        screenState.value = screen
    }

    override fun onCleared() {
        super.onCleared()
        screenState.value = null
    }

    private fun saveLog(message: String) {
        Log.d("==SplashFragmentViewModel==", message)
//        try {
//            val fos: FileOutputStream
//            val path = Environment.getExternalStorageDirectory().absolutePath
//            val FILENAME = "$path/streamline.txt"
//            val file = File(FILENAME)
//            if (!file.exists()) {
//                file.createNewFile()
//            }
//            val fis = FileReader(FILENAME)
//            val br = BufferedReader(fis)
//            val sb = StringBuffer()
//            var line: String?
//            while (br.readLine().also { line = it } != null) {
//                sb.append(line)
//                sb.append('\n')
//            }
//            val format = SimpleDateFormat("MM/dd HH:mm:ss")
//            val current = format.format(Date())
//            sb.append("$current : $message")
//            fos = FileOutputStream(FILENAME)
//            val myOutWriter = OutputStreamWriter(fos)
//            myOutWriter.append(sb.toString())
//            myOutWriter.close()
//            fos.close()
//        } catch (e: IOException) {
//            Log.d("SplashFragmentViewModel", "File write failed: $e")
//        }
    }
}
