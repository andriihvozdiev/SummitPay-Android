package com.glance.streamline.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.glance.streamline.R
import com.glance.streamline.domain.repository.payment.PaymentRequest
import com.glance.streamline.ui.base.BaseDialogFragment
import com.glance.streamline.ui.fragments.main.checkout.PaymentViewModel
import com.glance.streamline.ui.fragments.main.checkout.RESULT_PRODUCTS_KEY
import com.glance.streamline.ui.models.CashPaymentResult
import com.glance.streamline.utils.convertDpToPixel
import com.glance.streamline.utils.extensions.android.displayMetrics
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.isTabletDevice
import com.glance.streamline.utils.extensions.toISO_8601_Timezone
import com.glance.streamline.utils.extensions.toJson
import kotlinx.android.synthetic.main.dialog_tender_options.*
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.util.*

class TenderOptionsDialogFragment : BaseDialogFragment<PaymentViewModel>() {

    private var amount = 0
    private var strAmount: StringBuilder = StringBuilder("")

    override fun layout(): Int = R.layout.dialog_tender_options

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): PaymentViewModel {
        return injectViewModel(viewModelFactory)
    }

    private val args: TenderOptionsDialogFragmentArgs by navArgs()

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initViews()
            initClicks()
        }
    }

    override fun getCustomDialogSize(): Pair<Int, Int>? {
        val metrics = baseActivity.displayMetrics
        val paddingPx = baseActivity.convertDpToPixel(40f)
        return Pair(
            if (baseActivity.isTabletDevice()) baseActivity.convertDpToPixel(400f).toInt()
            else (metrics.widthPixels - paddingPx).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initViews() {
        showAmount()
    }

    private fun showAmount() {
        dialog_txt_cost.text = String.format("£ %.02f", amount / 100.0f)

        val cashAmount = amount / 100.0f

        dialog_tender_options_txt_total_cost.text = String.format("(£%.02f)", args.price)
        if (args.price > cashAmount) {
            dialog_txt_require_cost.text = String.format("£%.02f left to pay", args.price - cashAmount)
        } else {
            dialog_txt_require_cost.text = ""
        }

    }

    private fun purchase(price: Float) {
        viewModel.uploadReport(args.paymentRequest) {
            val cashPaymentResult = CashPaymentResult(price, args.paymentRequest)
            viewModel.cashPaymentCompletedLiveData.postValue(cashPaymentResult)
            dismiss()
        }
    }

    private fun initClicks() {
        dialog_tender_options_btn_close.onClick {
            dismiss()
        }

        btn_cash.onClick {
            val cashAmount = amount / 100.0f
            if (cashAmount > args.price) {
                AlertDialog.Builder(context)
                    .setTitle("Completed")
                    .setMessage(String.format("change given £%.02f", cashAmount - args.price))
                    .setNegativeButton(R.string.yes) { _, _ ->
                        purchase(args.price)
                    }
                    .setCancelable(false)
                    .create().apply {
                        show()
                    }
            } else if (cashAmount == args.price) {
                purchase(args.price)
            } else {
                val remainAmount = args.price - cashAmount
                AlertDialog.Builder(context)
                    .setTitle("Not Completed")
                    .setMessage(String.format("£%.02f left to pay.\nPlease pay with cash or card payment.", remainAmount))
                    .setNegativeButton("Card Payment") { _, _ ->
                        saveProducts(remainAmount, cashAmount)
                        viewModel.saleWithCard(remainAmount)
                    }
                    .setPositiveButton("Cash Payment") { _, _ ->
                        purchase(args.price)
                    }
                    .setCancelable(false)
                    .create().apply {
                        show()
                    }

            }

        }

        initDialButtonClicks()

    }

    private fun initDialButtonClicks() {
        btn_delete.onClick {
            amount = 0
            strAmount.clear()
            showAmount()
        }

        btn_option_5.onClick {
            amount = 500
            strAmount.clear()
            showAmount()
        }

        btn_option_10.onClick {
            amount = 1000
            strAmount.clear()
            showAmount()
        }

        btn_option_20.onClick {
            amount = 2000
            strAmount.clear()
            showAmount()
        }

        btn_number_0.onClick {
            strAmount.append("0")
            showAmountFromString()
        }
        btn_number_00.onClick {
            strAmount.append("00")
            showAmountFromString()
        }
        btn_number_1.onClick {
            strAmount.append("1")
            showAmountFromString()
        }
        btn_number_2.onClick {
            strAmount.append("2")
            showAmountFromString()
        }
        btn_number_3.onClick {
            strAmount.append("3")
            showAmountFromString()
        }
        btn_number_4.onClick {
            strAmount.append("4")
            showAmountFromString()
        }
        btn_number_5.onClick {
            strAmount.append("5")
            showAmountFromString()
        }
        btn_number_6.onClick {
            strAmount.append("6")
            showAmountFromString()
        }
        btn_number_7.onClick {
            strAmount.append("7")
            showAmountFromString()
        }
        btn_number_8.onClick {
            strAmount.append("8")
            showAmountFromString()
        }
        btn_number_9.onClick {
            strAmount.append("9")
            showAmountFromString()
        }
    }

    private fun saveProducts(cardAmount: Float = 0f, cashAmount: Float = 0f) {
        val paymentRequest = PaymentRequest(
            paymentDate = Calendar.getInstance().time.toISO_8601_Timezone(),
            deviceToken = requireContext().getUniqCurrentDeviceId(),
            products = args.paymentRequest.products,
            cardAmount = cardAmount,
            cashAmount = cashAmount
        )

        val sharedPreferences = baseActivity.getSharedPreferences(baseActivity.packageName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(RESULT_PRODUCTS_KEY, paymentRequest.toJson())
        editor.apply()
        editor.commit()
    }

    private fun showAmountFromString() {
        try {
            amount = strAmount.toString().toInt()
        } catch (ex: NumberFormatException) {
            strAmount.clear()
            amount = 0
        }
        showAmount()
    }

}