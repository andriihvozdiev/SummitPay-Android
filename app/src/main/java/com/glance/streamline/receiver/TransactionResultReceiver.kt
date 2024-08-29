package com.glance.streamline.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.glance.streamline.BuildConfig
import com.glance.streamline.ui.activities.main.MainActivity
import com.pax.dal.IPrinter
import com.pax.dal.exceptions.PrinterDevException
import com.pax.neptunelite.api.NeptuneLiteUser
import eft.com.eftservicelib.EFTServiceLib
import eft.com.eftservicelib.HistoryTransResult
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class TransactionResultReceiver: BroadcastReceiver() {

    val TRANSACTION_RESULT_EVENT = "eft.com.TRANSACTION_RESULT"
    val TRANSACTION_RECEIPT_EVENT = "eft.com.TRANSACTION_RECEIPT_EVENT"
    val TRANS_IN_BATCH_RESPONSE_EVENT = "eft.com.TRANS_IN_BATCH_RESPONSE_EVENT"
    val RECEIPT_NUMBER_KEY = "RECEIPT_NUMBER_KEY"

    private val psFailureUnknown = 99999 // A random high Value for now
    private val psSuccess = 0
    private val psBusy = 1
    var resultType: String? = null

    private val HISTORY_REPORTS = "History Reports"
    private val REPORTS = "Reports"

    private val TAG = "==TransactionResultReceiver=="

    override fun onReceive(context: Context?, intent: Intent?) {

        log("StreamLine App version:" + BuildConfig.VERSION_NAME + intent?.action)

        if (TRANSACTION_RECEIPT_EVENT == intent?.action) {
//            val merchant = BitmapFactory.decodeByteArray(
//                intent!!.getByteArrayExtra("ReceiptDataMerchant"),
//                0,
//                intent!!.getByteArrayExtra("ReceiptDataMerchant").size
//            )
//            val cardholder = BitmapFactory.decodeByteArray(
//                intent!!.getByteArrayExtra("ReceiptDataCardholder"),
//                0,
//                intent!!.getByteArrayExtra("ReceiptDataCardholder").size
//            )
//
//            printBitmap(context, merchant)
//            printBitmap(context, cardholder)
        }

        if (TRANS_IN_BATCH_RESPONSE_EVENT == intent?.action) {
            if (intent != null) {
                log("TransInBatch :" + intent.getIntExtra("TransInBatch", 0))
            }
        } else if (TRANSACTION_RESULT_EVENT == intent?.action) {
            /* unpack the transaction result */
            /* this will return null of the event was not for us, or if there was an error */
            if (intent!!.hasExtra("ReceiverResultType")) {
                resultType = intent.getStringExtra("ReceiverResultType")
                if (resultType != null && resultType == HISTORY_REPORTS) {
                    DisplayHistoryLogs(intent)
                } else if (resultType != null && resultType == REPORTS) {
                    displayReportLogs(intent)
                } else if (resultType != null && resultType == "Submit Trans") {
                    log("Is batch upload successful = " + intent.getBooleanExtra("IsSuccessful", true))
                    log("Number of transactions uploaded :" + intent.getIntExtra("BatchCount", 0))
                }
            }

            val result = EFTServiceLib.unpackResult(context, intent)

            var receiptNumber = 0
            var responseType = ""
            var cardNumber: String? = null
            var cardType: String? = null
            var authCode: String? = null
            var amount = 0L

            if (result != null) {
                log("Using EFTServiceLib version:" + EFTServiceLib.getVersion())

                val transFound = intent?.getBooleanExtra("TransResponse", false)

                if (transFound == true) {
                    /* Unpack the transaction results */
                    val rrn = result.rrn
                    val approved = result.isApproved
                    receiptNumber = result.receiptNumber
                    log("rrn = $rrn")
                    log("receiptNumber = $receiptNumber")

                    if (approved) {
                        responseType = "approved"
                    } else {
                        responseType = "declined"
                    }

                   /* Debug the result */
                    log("Transaction Type = " + result.transType)
//                    if (intent!!.hasExtra("UTI")) MainActivity.lastReceivedUTI =
//                        intent!!.getStringExtra("UTI")

                    log("Amount = " + result.amount)
                    amount = result.amount
                    log("MsgStatus= " + intent.getStringExtra("MsgStatus"))

                    /* all the additional extra data you can get */
                    log("Approved = " + intent.getBooleanExtra("Approved", false))
                    log("Cancelled = " + intent.getBooleanExtra("Cancelled", false))
                    log("LastStatus(FOR ADDITIONAL INFO ONLY) = " + intent.getStringExtra("LastStatus"))
                    log("SigRequired = " + intent.getBooleanExtra("SigRequired", false))
                    log("PINVerified = " + intent.getBooleanExtra("PINVerified", false))
                    log("AuthMode = " + intent.getStringExtra("AuthMode"))
                    log("Currency = " + intent.getStringExtra("Currency"))
                    log("Tid = " + intent.getStringExtra("Tid"))
                    log("Mid = " + intent.getStringExtra("Mid"))
                    log("Version = " + intent.getStringExtra("Version"))

                    if (intent.getBooleanExtra("TransactionDetails", false)) {
                        authCode = intent.getStringExtra("AuthCode")
//                        responseCode = intent.getStringExtra("ResponseCode")
                        log("Transaction Details:")
                        log("FlightReference = " + intent.getStringExtra("FlightReference"))
                        log("ReceiptNumber = " + intent.getIntExtra("ReceiptNumber", 0))
                        log("RRN = " + intent.getStringExtra("RRN"))
                        log("ResponseCode = " + intent.getStringExtra("ResponseCode"))
                        log("Stan = " + intent.getIntExtra("Stan", 0))
                        log("AuthCode = " + intent.getStringExtra("AuthCode"))
                        log("MerchantTokenId = " + intent.getStringExtra("MerchantTokenId"))
                        if (intent.hasExtra("CardType")) {
                            cardType = intent.getStringExtra("CardType")
                            log("CardType = $cardType")
                            if (cardType.compareTo("EMV") == 0 || cardType.compareTo("CTLS") == 0) {
                                log("AID = " + intent.getStringExtra("AID"))
                                log("TSI = " + intent.getStringExtra("TSI"))
                                log("TVR = " + intent.getStringExtra("TVR"))
                                log("CardHolder = " + intent.getStringExtra("CardHolder"))
                                log("Cryptogram = " + byteArrayToHexString(
                                        intent.getByteArrayExtra(
                                            "Cryptogram"
                                        )
                                    )
                                )
                                log("CryptogramType = " + intent.getStringExtra("CryptogramType"))
                            }

                            log("PAN = " + intent.getStringExtra("PAN"))
                            cardNumber = intent.getStringExtra("PAN")
                            log("ExpiryDate = " + intent.getStringExtra("ExpiryDate"))
                            log("StartDate = " + intent.getStringExtra("StartDate"))
                            log("Scheme = " + intent.getStringExtra("Scheme"))
                            log("PSN = " + intent.getIntExtra("PSN", 0))
                        }
                    }
                } else {
                    log("Transaction not found")
                }
            }

            if (context != null) {
                bringToFront(context, receiptNumber, responseType, cardNumber, cardType, authCode, amount)
            } else {
                log("Cannot start Activity without context")
            }
        }
    }

    private fun displayReportLogs(intent: Intent?) {
        if (intent != null) {
            log("Report Type = " + intent.getStringExtra("ReportType"))
            log("Sale Count = " + intent.getLongExtra("SaleCount", 0))
            log("Sale Amount = " + intent.getLongExtra("SaleAmount", 0) / 100.0)
            log("Refund Count = " + intent.getLongExtra("RefundCount", 0))
            log("Refund Amount = " + intent.getLongExtra("RefundAmount", 0) / 100.0)
            log("Cashback Count = " + intent.getLongExtra("CashbackCount", 0))
            log("CashBack Amount = " + intent.getLongExtra("CashbackAmount", 0) / 100.0)
            log("Gratuity Count= " + intent.getLongExtra("GratuityCount", 0))
            log("Gratuity Amount = " + intent.getLongExtra("GratuityAmount", 0) / 100.0)
            log("Voucher Count= " + intent.getLongExtra("VoucherCount", 0))
            log("Voucher Amount = " + intent.getLongExtra("VoucherAmount", 0) / 100.0)
        }
    }


    private fun DisplayHistoryLogs(intent: Intent) {
        val transHistoryList: ArrayList<HistoryTransResult> =
            intent.getParcelableArrayListExtra("HistoryList")
        if (transHistoryList != null) {
            for (historyItem in transHistoryList) {
                log("TransType = " + historyItem.transType)
                log("Amount = " + historyItem.transAmount / 100)
                log("Currency = " + historyItem.currency)
                log("Status = " + historyItem.transApproved)
                log("Date Time = " + historyItem.transDate)
                log("PAN = " + historyItem.transPan)
                log("RRN = " + historyItem.rnn)
                log("Receipt No = " + historyItem.receiptNo)
                log("PID = " + historyItem.pid)
                log("CVM method = " + historyItem.cvmMethod)
                log("------------------------------------------------------------")
            }
        }
    }


    private fun bringToFront(context: Context, receiptNumber: Int, paymentResultType: String,
                             cardNumber: String?, cardType: String?, authCode: String?, amount: Long) {

        log("bringToFront start")
        /* Bring this application back into the foreground */
        val sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val oldReceiptNumber = sharedPreferences.getInt(RECEIPT_NUMBER_KEY, 0)
        if (oldReceiptNumber == receiptNumber) return

        val editor = sharedPreferences.edit()
        editor.putInt(RECEIPT_NUMBER_KEY, receiptNumber)
        editor.apply()
        editor.commit()

        val notificationIntent = Intent(context, MainActivity::class.java)

        log("payment result type: $paymentResultType")
        notificationIntent.putExtra("paymentResponseType", paymentResultType)
        notificationIntent.putExtra("cardNumber", cardNumber)
        notificationIntent.putExtra("cardType", cardType)
        notificationIntent.putExtra("authCode", authCode)
        notificationIntent.putExtra("amount", amount)
        notificationIntent.putExtra("receiptNumber", receiptNumber)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(notificationIntent)
        log("Bring MainActivity to the front")
    }

    fun byteArrayToHexString(byteArray: ByteArray?): String? {
        if (byteArray == null) {
            return ""
            //throw new IllegalArgumentException("Argument 'byteArray' cannot be null");
        }
        val readBytes = byteArray.size
        val hexData = StringBuilder()
        var onebyte: Int
        for (i in 0 until readBytes) {
            onebyte = 0x000000ff and byteArray[i].toInt() or -0x100
            hexData.append(Integer.toHexString(onebyte).substring(6))
        }
        return hexData.toString().toUpperCase()
    }

    private fun sleep(iTimeoutMS: Int) {
        try {
            Thread.sleep(iTimeoutMS.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun start(printer: IPrinter): Int {
        try {
            while (true) {
                val ret = printer.start()
                // printer is busy, please wait
                if (ret == 1) {
                    sleep(1000)
                    continue
                } else if (ret == 2) {
                    log("Printer is Out of Paper")
                    return -1
                } else if (ret == 8) {
                    log("Printer is too hot")
                    return -1
                } else if (ret == 9) {
                    log("Voltage is too low!")
                    return -1
                } else if (ret != 0) {
                    return -1
                }
                return 0
            }
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            return 0
        }
    }

    fun getPrinterStatus(iPaxPrinter: IPrinter): Int {
        try {
            var status = iPaxPrinter.status
            if (status == -4) { // Pax library error returned -4 (font not installed) when it shouldn't (raised a case with pax)
                status = psSuccess
            }
            return status
        } catch (ex: java.lang.Exception) {
        }
        return psFailureUnknown
    }

    fun printBitmap(context: Context?, printable: Bitmap?): Int {
        var status: Int = psSuccess
        try {
            val iPaxDal = NeptuneLiteUser.getInstance().getDal(context)
            val iPaxPrinter = iPaxDal.printer
            iPaxPrinter.init()
            iPaxPrinter.setGray(4)
            iPaxPrinter.printBitmap(printable)
            /*Spool Out the receipt*/iPaxPrinter.step(150)
            start(iPaxPrinter)
            /*Wait for the Printing to Occur*/
            do {
                status = this.getPrinterStatus(iPaxPrinter)
                if (status == psBusy) {
                    /*Printer is doing its job*/
                    Thread.sleep(200)
                } else {
                    break
                }
            } while (status != psSuccess)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return status
    }


    private fun log(message: String) {
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
//            Log.d(TAG, "File write failed: $e")
//        }
    }
}