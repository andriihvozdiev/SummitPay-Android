package com.glance.streamline.ui.fragments.refund

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glance.streamline.R
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.ui.fragments.main.checkout.PaymentViewModel
import com.glance.streamline.utils.extensions.android.injectViewModel
import kotlinx.android.synthetic.main.fragment_refund.*
import java.lang.NumberFormatException
import java.lang.StringBuilder

class RefundFragment : BaseFragment<PaymentViewModel>() {

    private var amount = 0
    private var strAmount: StringBuilder = StringBuilder("")

    override fun layout(): Int = R.layout.fragment_refund

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): PaymentViewModel {
        return injectViewModel(viewModelFactory)
    }

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initViews()
            initClicks()
        }
    }

    private fun initViews() {
        showAmount()
    }

    private fun showAmount() {
        refund_dialog_txt_cost.text = String.format("£ %.02f", amount / 100.0f)
    }

    private fun initClicks() {
        refund_dialog_btn_cancel.onClick {
            findNavController().popBackStack()
        }

        refund_dialog_btn_done.onClick {
            if (viewModel.refundCard(amount / 100.0f)) {
                amount = 0
                strAmount.clear()
                showAmount()
            }
        }

        refund_dialog_btn_delete.onClick {
            amount = 0
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
        btn_number_000.onClick {
            strAmount.append("000")
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