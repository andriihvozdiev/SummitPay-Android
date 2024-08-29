package com.glance.streamline.ui.fragments.main.checkout

import android.annotation.SuppressLint
import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.SafeWebServices.PaymentGateway.PGKeyedCard
import com.SafeWebServices.PaymentGateway.PGSwipeController
import com.SafeWebServices.PaymentGateway.PGSwipeDevice
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.domain.model.payment.CardTransactionRequest
import com.glance.streamline.domain.model.payment.PaymentResponseModel
import com.glance.streamline.domain.model.payment.TransactionType
import com.glance.streamline.domain.repository.payment.PaymentApiRepository
import com.glance.streamline.domain.repository.payment.PaymentRequest
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.data.entities.ReportInfo
import com.glance.streamline.data.entities.ZReportInfo
import com.glance.streamline.ui.models.CashPaymentResult
import com.glance.streamline.ui.models.PaymentResultType
import com.glance.streamline.utils.extensions.android.Success
import com.glance.streamline.utils.extensions.getSchedulers
import com.glance.streamline.utils.extensions.toISO_8601_Timezone
import com.glance.streamline.utils.payment.*
import eft.com.eftservicelib.EFTServiceLib
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

private const val SECURE_PAYMENT_KEY = "6457Thfj624V5r7WUwc5v6a68Zsd6YEm"

class PaymentViewModel @Inject constructor(
    app: Application,
    private val db: AppDatabase,
    private val paymentApiRepository: PaymentApiRepository
) : BaseViewModel(app) {

    private val swipePaymentListener = SwipePayment()
    private var swipeController: PGSwipeController? = null

    val paymentResponseLiveData = MutableLiveData<PaymentResponseModel>()
    val cashPaymentCompletedLiveData = MutableLiveData<CashPaymentResult>()

    fun initSwipeDevice() {
        initSwipeController()
        handleSwipeEvents()
    }

    @SuppressLint("CheckResult")
    fun uploadReport(paymentRequest: PaymentRequest, onSuccess: () -> Unit = {}) {
        onStartLoading()
        val reportInfo = paymentRequest.convertToReportInfo()

        paymentApiRepository.purchase(paymentRequest)
            .subscribe({
                saveLog("completed report upload")
                onStopLoading()
                onSuccess()
            }, {
                saveLog("failed report upload")
                db.reportsDao()
                    .saveReportInfoCompletable(reportInfo)
                    .call {
                        onStopLoading()
                        onSuccess()
                    }
            })
    }

//    fun purchase(paymentRequest: PaymentRequest, onSuccess: () -> Unit = {}) {
//        onStartLoading()
//        paymentApiRepository.purchase(paymentRequest)
//            .doOnComplete {
//                onSuccess()
//            }
//            .doFinally {
//                onStopLoading()
//            }
//            .call().addToDispose()
//    }

    fun saleWithCard(amount: Float, onSuccess: () -> Unit = {}) {

        saveLog("Start card payment")
        /* to run a transaction with a specific user ID or password */
        val result = EFTServiceLib.runTrans(
            getContext(),
            (amount * 100).roundToInt(),
            EFTServiceLib.TRANSACTION_TYPE_SALE,
            "",
            "",
            "",
            ""
        )

//        Timer().schedule(object : TimerTask() {
//            override fun run() {
//                //Cancel the transaction
//                EFTServiceLib.cancelTrans(getContext())
//            }
//        }, 300000)

        onSuccess()
    }

    fun refundCard(amount: Float) : Boolean{
        val result = EFTServiceLib.runTrans(
            getContext(),
            (amount * 100).roundToInt(),
            EFTServiceLib.TRANSACTION_TYPE_REFUND,
            "",
            "",
            "",
            ""
        )

        if (result) {
            saveRefundReports(amount)
        }

        return result
    }

    private fun saveRefundReports(price: Float) {

        val zReportInfo = ZReportInfo(
            Calendar.getInstance().time.toISO_8601_Timezone(),
            PaymentResultType.REFUND_PAYMENT_TYPE,
            price
        )

        db.zReportDao()
            .saveZReportInfoCompletable(zReportInfo)
            .call ()
    }

    private fun handleSwipeEvents() {
        swipePaymentListener.onSwipeEvent = { state ->
            when (state) {
                is DeviceReady -> {
                    state.device
                }
                is DeviceUnready -> {
                }
                is DeviceConnected -> {
                }
                is DeviceDisconnected -> {
                }
                is DeviceActivated -> {
                    //If always accept swipe is set to false.
                    if (!state.device.isReadyForSwipe) {
                        state.device.requestSwipe()
                    }
                }
                is DeviceDeactivated -> {
                }
                is SwipedCard -> {
                    onSuccess(Success(state.card, state.card.maskedCardNumber))
                }
            }
        }
    }

    private fun initSwipeController() {
        swipeController = PGSwipeController(
            swipePaymentListener,
            getContext(),
            PGSwipeDevice.SwipeDevice.ENTERPRISE
        ).apply {
            device.setSwipeTimeout(30)
            device.setAlwaysAcceptSwipe(false)
            device.setActivateReaderOnConnect(false)
            device.requestSwipe()
        }
    }

    fun sendPayment(
        cardNumber: String,
        cardExpirationDate: String,
        cardCvv: String,
        amount: Float
    ) {
        /* val ccnumber = "4111111111111111"
         val ccexp = "10/25"
         val cvv = "999"*/
        val card = PGKeyedCard(cardNumber, cardExpirationDate, cardCvv)
        val cardPostData = card.getDirectPostString(false)
        val encryption = PaymentGatewayEncryption.setupPaymentEncryption(card)
        val request = CardTransactionRequest(
            type = TransactionType.SALE,
            securityKey = SECURE_PAYMENT_KEY,
            encryptedData = null,
            cardNumber = cardNumber,
            cardExpirationDate = cardExpirationDate,
            cvv = cardCvv,
            amount = amount
        )

        val result = EFTServiceLib.runTrans(
            getContext(),
            (amount * 100).roundToInt(),
            EFTServiceLib.TRANSACTION_TYPE_SALE,
            "",
            "",
            "",
            ""
        )

//        paymentApiRepository.sendCardTransaction(request).call {
//            it.unWrapResult {
//                paymentResponseLiveData.f(it.value)
//            }
//        }
    }

    override fun onCleared() {
        super.onCleared()
        swipeController?.device?.stopSwipeController()
        paymentResponseLiveData.value = null
        cashPaymentCompletedLiveData.value = null
    }

    fun clears() {
        cashPaymentCompletedLiveData.value = null
    }

    private fun saveLog(message: String) {
        Log.d("PaymentViewModel", message)
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
//            Log.d("PaymentViewModel", "File write failed: $e")
//        }
    }
}
