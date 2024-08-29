package com.glance.streamline.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.glance.streamline.R
import com.glance.streamline.domain.model.payment.PaymentResponseModel
import com.glance.streamline.ui.base.BaseDialogFragment
import com.glance.streamline.ui.fragments.main.checkout.CheckoutFragmentViewModel
import com.glance.streamline.ui.fragments.main.checkout.PaymentViewModel
import com.glance.streamline.ui.models.ProductModelDto
import com.glance.streamline.utils.convertDpToPixel
import com.glance.streamline.utils.extensions.android.displayMetrics
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.isTabletDevice
import com.glance.streamline.utils.extensions.android.observe
import com.glance.streamline.utils.extensions.android.view.onTextChanged
import com.glance.streamline.utils.extensions.formatTwoDigitAfterDelimiter
import kotlinx.android.synthetic.main.dialog_payment.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import java.util.*


class PaymentDialogFragment : BaseDialogFragment<PaymentViewModel>() {
    companion object {
        private const val PERMISSIONS_RECORD_AUDIO = 123
    }

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory) =
        injectViewModel(viewModelFactory) as PaymentViewModel

    override fun layout() = R.layout.dialog_payment

    private val orderDto: ProductModelDto by lazy {
        navArgs<PaymentDialogFragmentArgs>().value.productModelDto
    }

    override fun initialization(view: View, isFirstInit: Boolean) {
        if (isFirstInit) {
            initClicks()
            initEditTexts()
            initViews()
            observeViewModel()
        }
    }

    override fun attachSnackBarView(): CoordinatorLayout? = root_layout

    private fun observeViewModel() {
        viewModel.paymentResponseLiveData.observe(this, ::onSuccessPayment)
    }

    private fun initViews() {
        charge_button.text = getString(R.string.charge_button_card_text, orderDto.totalSum)
    }

    private fun initEditTexts() {
        val creditCardMask = MaskImpl.createTerminated(PredefinedSlots.CARD_NUMBER_STANDARD)
        val creditCardWatcher: FormatWatcher = MaskFormatWatcher(creditCardMask)
        creditCardWatcher.installOn(card_number_edit_text)

        card_number_edit_text.onTextChanged { number ->
            card_number_text_view.text = number.getMaskedCardNumber()
            validateFields()
        }

        val cardExpirationDateMask =
            MaskImpl.createTerminated(UnderscoreDigitSlotsParser().parseSlots("__/__"))
        val cardExpirationDateWatcher: FormatWatcher = MaskFormatWatcher(cardExpirationDateMask)
        cardExpirationDateWatcher.installOn(card_date_edit_text)
        card_date_edit_text.onTextChanged { date ->
            card_date_edit_text.updateErrorState()
            card_date_text_view.text = date
            validateFields()
        }

        card_cvv_edit_text.onTextChanged { date ->
            card_cvv_text_view.text = date
            validateFields()
        }

        validateFields()

        //For demo
        card_number_edit_text.hint = "4111111111111111"
        card_date_edit_text.hint = "10/25"
        card_cvv_edit_text.hint = "999"
        //For demo
    }

    private fun String.getMaskedCardNumber(): String {
        val array = toCharArray()
        array.withIndex()
            .filter { Character.isDigit(it.value) && it.index in 5..13 }
            .forEach {
                array[it.index] = 'X'
            }
        return String(array)
    }

    private fun EditText.updateErrorState(): Boolean {
        val date = text.toString()
        val isValid = if (date.length == 5) validateExpirationDate()
        else true
        setBackgroundResource(
            if (isValid) R.drawable.bg_rounded_5px_white_selectable
            else R.drawable.bg_rounded_5px_white_error_selectable
        )
        return isValid
    }

    private fun EditText.validateExpirationDate(): Boolean {
        val date = text.toString()
        return if (date.length == 5) {
            val dateParts = date.split('/')
            var monthValidation = false
            var yearValidation = false
            if (dateParts.size == 2) {
                val calendar = Calendar.getInstance(Locale.getDefault())
                val yearTwoDigit = calendar.get(Calendar.YEAR) % 2000
                val month = dateParts[0].toInt()
                val year = dateParts[1].toInt()
                monthValidation =
                    ((month >= (calendar.get(Calendar.MONTH) + 1) && year == yearTwoDigit) || year > yearTwoDigit)
                            && month in 1..12
                yearValidation = year >= yearTwoDigit
            }
            monthValidation && yearValidation
        } else false
    }

    private fun validateFields() {
        charge_button.isEnabled = card_number_edit_text.text?.length == 19
                && card_date_edit_text.validateExpirationDate() && card_cvv_edit_text.text?.length == 3
    }

    private fun initClicks() {
        close_dialog_button.onClick {
            dismiss()
        }
        charge_button.onClick {
            toggleKeyboard(false)
            onChargeButtonClick()
        }
    }

    private fun onChargeButtonClick() {
        orderDto.let { dto ->
            val cardNumber = card_number_edit_text.text.toString().replace(" ", "")
            val cardExpirationDate = card_date_edit_text.text.toString()
            val cardCvv = card_cvv_edit_text.text.toString()
            viewModel.sendPayment(
                cardNumber,
                cardExpirationDate,
                cardCvv,
                dto.totalSum.formatTwoDigitAfterDelimiter()
            )
        }
    }

    override fun getCustomDialogSize(): Pair<Int, Int>? {
        val metrics = baseActivity.displayMetrics
        val paddingPx = baseActivity.convertDpToPixel(40f)
        return Pair(
            if (baseActivity.isTabletDevice()) baseActivity.convertDpToPixel(400f).toInt()
            else (metrics.widthPixels - paddingPx).toInt(),
            WRAP_CONTENT
        )
    }

    private fun onSuccessPayment(paymentResponse: PaymentResponseModel) {
        dismiss()
        (injectViewModel(viewModelFactory) as CheckoutFragmentViewModel)
            .paymentResponseLiveData.postValue(paymentResponse)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                baseActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_RECORD_AUDIO
            )
        } else viewModel.initSwipeDevice()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_RECORD_AUDIO) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                onBackPressed()
            else viewModel.initSwipeDevice()
        }
    }
}