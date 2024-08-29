package com.glance.streamline.ui.dialogs

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatRadioButton
import com.glance.streamline.R
import com.glance.streamline.data.entities.PaymentHistoryInfo
import com.glance.streamline.domain.model.payment.PaymentHistoryModel
import com.glance.streamline.domain.repository.payment.RefundRequest
import com.glance.streamline.utils.extensions.android.getInflater
import kotlinx.android.synthetic.main.dialog_partial_refund.*

class PartialRefundDialog(
    context: Context,
    val paymentHistoryInfo: PaymentHistoryInfo,
    val onConfirm: (RefundRequest) -> Unit = {}
) :
    AlertDialog.Builder(context) {
    private var alertDialog: AlertDialog? = null
    private var refundRequest = RefundRequest()

    fun showDialog() {
        refundRequest.paymentId = paymentHistoryInfo.id

        alertDialog?.dismiss()
        shows()
        alertDialog?.radio_group?.apply {


            paymentHistoryInfo.products.forEachIndexed { index, product ->
                if (!product.isRefunded) {
                    addView(AppCompatRadioButton(context).apply {
                        id = index
                        text = product.productName + " (£" + product.productRetailPrice + ")"
                    })
                }
            }
            setOnCheckedChangeListener { group, checkedId ->
                alertDialog?.getButton(0)?.isEnabled = true
                val product = paymentHistoryInfo.products.get(checkedId)
                refundRequest.productIds.clear()
                refundRequest.productIds.add(product.id)
                showTotalRefund(product)
            }
        }
    }

    private fun showTotalRefund(product: PaymentHistoryModel.ProductModel) {
        alertDialog?.txt_message?.apply {
            var totalRefund = "TOTAL REFUND: £" + product.productRetailPrice
            text = totalRefund
        }
    }

    private fun shows(): AlertDialog {
        val removingOrderItemsDialogView =
            context.getInflater().inflate(R.layout.dialog_partial_refund, null).apply {}
        setView(removingOrderItemsDialogView)
        var alertTitle = String.format("Partial Refund? #%04d", paymentHistoryInfo.orderNumber)

        setTitle(alertTitle)
        setPositiveButton(R.string.confirm) { _, _ ->
            when {
                isCanRefund() -> {
                    onConfirm(refundRequest)
                    this.alertDialog?.dismiss()
                }
                else -> {
                    showDialog()
                }
            }
        }
        setNegativeButton(R.string.text_cancel) { _, _ ->
            this.alertDialog?.dismiss()
        }
        setCancelable(false)
        create().apply {
            getButton(0)?.isEnabled = false
        }

        return show().also { alertDialog = it }
    }

    private fun isCanRefund(): Boolean {
        return refundRequest.productIds.size > 0
    }
}
