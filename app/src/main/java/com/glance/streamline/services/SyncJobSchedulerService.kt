package com.glance.streamline.services

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.glance.streamline.StreamlineApp
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.domain.repository.payment.PaymentApiRepository
import com.glance.streamline.data.entities.ReportInfo
import com.glance.streamline.utils.extensions.getSchedulers
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SyncJobSchedulerService: JobService() {

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var paymentApiRepository: PaymentApiRepository

    private val TAG = "==SyncJobSchedulerService=="

    override fun onStartJob(params: JobParameters?): Boolean {

        saveLog("start job")
        StreamlineApp[applicationContext].appComponent.inject(this)

        if (::db.isInitialized) {
            val subscribe = db.reportsDao()
                .getAllReportInfo()
                .getSchedulers()
                .subscribe { arrReports ->
                    saveLog("saved reports size: " + arrReports.size.toString())
                    uploadReports(arrReports)
                }
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    private fun uploadReports(reports: List<ReportInfo>) {
        if (::paymentApiRepository.isInitialized) {

            reports.forEach { reportInfo ->
                val paymentRequest = reportInfo.convertPaymentRequest()
                saveLog("upload report")
                paymentApiRepository.purchase(paymentRequest)
                    .subscribe({
                        deleteReportFromDB(reportInfo)
                    }, {
                        saveLog(it.toString())
                    })
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun deleteReportFromDB(reportInfo: ReportInfo) {
        saveLog(reportInfo.toString())
        db.reportsDao()
            .deleteReportInfo(reportInfo)
            .getSchedulers().subscribe {
                saveLog("delete result: $it")
            }
    }

    private fun saveLog(message: String) {
        Log.d(TAG, message)
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
//            Log.d("SyncJobSchedulerService", "File write failed: $e")
//        }
    }
}