package com.glance.streamline.ui.fragments.closed_orders

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.data.entities.PaymentHistoryInfo
import com.glance.streamline.data.entities.ReportInfo
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.domain.model.payment.PaymentHistoryModel
import com.glance.streamline.domain.repository.payment.PaymentApiRepository
import com.glance.streamline.domain.repository.payment.RefundRequest
import com.glance.streamline.domain.repository.products.ProductsApiRepository
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.utils.extensions.*
import com.glance.streamline.utils.extensions.android.getSharedPref
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

const val PAYMENT_LAST_UPDATED_KEY = "PAYMENT_LAST_UPDATED_KEY"
class ClosedOrdersFragmentViewModel @Inject constructor(
    app: Application,
    private val db: AppDatabase,
    private val paymentApiRepository: PaymentApiRepository,
    private val productsApiRepository: ProductsApiRepository
) : BaseViewModel(app) {

    private var strReceivedLastUpdated: String = ""

    private var mDeviceId: String = getContext().getUniqCurrentDeviceId()

    val paymentHistoriesLiveData = MutableLiveData<List<PaymentHistoryInfo>?>()

    val searchPaymentsResultsLiveData = MutableLiveData<List<PaymentHistoryInfo>?>()
    val selectedPaymentHistory = MutableLiveData<PaymentHistoryInfo?>()

    fun checkLastUpdated() {
        productsApiRepository.getLastUpdate {
            it.unWrapResult(null, false) {
                strReceivedLastUpdated = it.value.lastUpdate

                val lastUpdated = Date().parseRFC3339Nano(strReceivedLastUpdated)

                val savedLastUpdated = getContext().getSharedPref()?.getString(PAYMENT_LAST_UPDATED_KEY, "").orEmpty()
                val localLastUpdated = Date().parseRFC3339Nano(savedLastUpdated)

                if (localLastUpdated == null || localLastUpdated.before(lastUpdated)) {
                    getPaymentHistories()
                }
            }
        }.call()
    }

    fun getPaymentHistories(
        page: Int? = 0,
        amount: Int? = 1
    ) {
        mDeviceId = getContext().getUniqCurrentDeviceId()
        val deviceId = mDeviceId

        paymentApiRepository.getPaymentHistories(deviceId) {
            it.unWrapResult {
                val paymentHistories = it.value.values ?: arrayListOf()
                if (paymentHistories.isNotEmpty()) {
                    saveAllPaymentHistoriesToDB(paymentHistories, deviceId, page, amount)
                } else {
                    getPaymentHistoriesFromDB()
                }
            }

            it.doOnError {
                getPaymentHistoriesFromDB()
            }
        }.call()
    }

    private fun getPaymentHistoriesFromDB(
        page: Int? = null,
        amount: Int? = null
    ) {
        val calendar = Calendar.getInstance()
		calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
		calendar.add(Calendar.DAY_OF_YEAR, -1)

        val from = calendar.time

        db.paymentHistoryDao()
            .getPaymentHistories(from)
            .call {
                paymentHistoriesLiveData.value = it
            }
    }

    private fun saveAllPaymentHistoriesToDB(
        paymentHistories: ArrayList<PaymentHistoryModel>,
        deviceId: String = mDeviceId,
        page: Int? = null,
        amount: Int? = null
    ) {
        db.paymentHistoryDao()
            .savePaymentHistoriesCompletable(paymentHistories)
            .call {
                getContext().getSharedPref()?.edit {
                    putString(PAYMENT_LAST_UPDATED_KEY, strReceivedLastUpdated)
                }
                getPaymentHistoriesFromDB()
            }
    }

    fun searchPaymentHistory(query: String) {
        val filteredPaymentHistories = arrayListOf<PaymentHistoryInfo>()
        paymentHistoriesLiveData.value?.let {
            filteredPaymentHistories.addAll(
                it.filter {
                    it.userName.contains(query, true) == true
                }
            )

            searchPaymentsResultsLiveData.value = filteredPaymentHistories
        }
    }

    fun onSearchClosed() {
        searchPaymentsResultsLiveData.value = paymentHistoriesLiveData.value
    }

    fun refundPartial(paymentHistory: PaymentHistoryInfo, refundRequest: RefundRequest) {
        var isFullRefunded = true
        paymentHistory.products.forEach { product ->
            if (refundRequest.productIds.contains(product.id)) {
                product.isRefunded = true
            }
            if (!product.isRefunded) isFullRefunded = false
        }

        if (isFullRefunded) {
            paymentHistory.isRefunded = true
        }

        paymentHistory.cardRefundAmount += refundRequest.cardAmount
        paymentHistory.cashRefundAmount += refundRequest.cashAmount

        paymentApiRepository.refund(refundRequest)
            .subscribe({
                if (isFullRefunded) updatePaymentHistoryFromDB(paymentHistory)
                else refundProductFromDB(paymentHistory)
            }, {
                Log.d("=", it.toString())
            })
    }

    private fun refundProductFromDB(paymentHistory: PaymentHistoryInfo) {
        db.paymentHistoryDao()
            .updatePaymentHistoryMaybe(paymentHistory)
            .call {
                selectedPaymentHistory.value = paymentHistory
            }
    }

    fun refundAll(paymentHistory: PaymentHistoryInfo) {
        val productIds = ArrayList<String>()
        val optionIds = ArrayList<String>()
        paymentHistory.products.forEach { product ->
            productIds.add(product.id)
        }

        var totalRefundAmount = paymentHistory.cardAmount + paymentHistory.cashAmount - (paymentHistory.cardRefundAmount + paymentHistory.cashRefundAmount)
        val cardRefundAmount = paymentHistory.cardAmount - paymentHistory.cardRefundAmount
        val cashRefundAmount = totalRefundAmount - cardRefundAmount

        val refundRequest = RefundRequest(
            paymentHistory.id,
            productIds,
            optionIds,
            cashRefundAmount,
            cardRefundAmount
        )

        paymentHistory.isRefunded = true
        paymentHistory.products.forEach { product ->
            product.isRefunded = true
            product.options.forEach { option ->
                option.isRefunded = true
            }
        }

        Log.d("========", refundRequest.toString());
        paymentApiRepository.refund(refundRequest)
            .subscribe({
                updatePaymentHistoryFromDB(paymentHistory)
            }, {
                Log.d("=", it.toString())
            })
    }

    private fun updatePaymentHistoryFromDB(paymentHistory: PaymentHistoryInfo) {
        db.paymentHistoryDao()
            .updatePaymentHistoryMaybe(paymentHistory)
            .call {
                getPaymentHistoriesFromDB()
            }
    }

    fun clearAllData() {
        searchPaymentsResultsLiveData.value = null
        paymentHistoriesLiveData.value = null
        selectedPaymentHistory.value = null
    }

    override fun onCleared() {
        super.onCleared()
        clearAllData()
    }
}
