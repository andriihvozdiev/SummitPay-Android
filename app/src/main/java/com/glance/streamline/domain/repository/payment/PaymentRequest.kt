package com.glance.streamline.domain.repository.payment


import android.os.Parcelable
import com.glance.streamline.ui.dialogs.refuselog.RefuseMessageDialog
import com.glance.streamline.data.entities.ReportInfo
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentRequest(
    @SerializedName("payment_date")
    val paymentDate: String = "", // date-string
    @SerializedName("last_digits")
    val lastDigits: String = "",
    @SerializedName("device_token")
    val deviceToken: String = "",
    @SerializedName("refusal_log")
    val refusalLog: RefusalLogRequest = RefusalLogRequest(),
    @SerializedName("products")
    val products: List<Product> = listOf(),
//    @SerializedName("order_number")
//    val orderNumber: String = "",
    @SerializedName("provider_payment_id")
    val providerPaymentId: String = "",
    @SerializedName("card_amount")
    val cardAmount: Float = 0.0f,
    @SerializedName("cash_amount")
    val cashAmount: Float = 0.0f
) : Parcelable {

    fun convertToReportInfo(): ReportInfo {
        return ReportInfo(
            paymentDate,
            lastDigits,
            deviceToken,
            refusalLog,
            products,
//            orderNumber,
            providerPaymentId,
            cardAmount,
            cashAmount
            )
    }

    @Parcelize
    data class RefusalLogRequest(
        @SerializedName("is_id_required")
        val IsIDRequired: String? = null,
        @SerializedName("was_id_provided")
        val WasIDProvided: String? = null,
        @SerializedName("gender")
        val Gender: String? = null,
        @SerializedName("ethnic_origin")
        val EthnicOrigin: String? = null,
        @SerializedName("approx_age")
        val ApproxAge: String? = null,
        @SerializedName("height")
        val Height: String? = null,
        @SerializedName("build")
        val Build: String? = null,
        @SerializedName("hair_colour")
        val HairColour: String? = null,
        @SerializedName("was_the_customer_abusive")
        val WasTheCustomerAbusive: String? = null,
        @SerializedName("refusal_reason")
        val RefusalReason: String? = null,
        @SerializedName("comments")
        val Comments: String? = null
    ) : Parcelable {

        constructor(refusalLogResult: RefuseMessageDialog.RefuseDialogResult?) : this(
            IsIDRequired = refusalLogResult?.isIdRequered?.value,
            WasIDProvided = refusalLogResult?.wasIdProvided?.value,
            Gender = refusalLogResult?.gender?.value,
            EthnicOrigin = refusalLogResult?.gender?.value,
            ApproxAge = refusalLogResult?.approxAge?.value,
            Height = refusalLogResult?.height?.value,
            Build = refusalLogResult?.build?.value,
            HairColour = refusalLogResult?.hairColour?.value,
            WasTheCustomerAbusive = refusalLogResult?.wasTheCustomerAbusive?.value,
            RefusalReason = refusalLogResult?.refusalReason?.value,
            Comments = refusalLogResult?.comments
        )
    }

    @Parcelize
    data class Product(
        @SerializedName("product_id")
        val productId: String = "",
        @SerializedName("options")
        val optionsId: ArrayList<String> = arrayListOf()
    ) : Parcelable

}