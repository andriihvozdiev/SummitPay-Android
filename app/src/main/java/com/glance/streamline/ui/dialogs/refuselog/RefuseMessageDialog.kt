package com.glance.streamline.ui.dialogs.refuselog

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatRadioButton
import com.glance.streamline.R
import com.glance.streamline.utils.extensions.android.getInflater
import com.glance.streamline.utils.extensions.android.view.afterTextChanged
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.dialog_refused_screen_five.*
import kotlinx.android.synthetic.main.dialog_refused_screen_four.*
import kotlinx.android.synthetic.main.dialog_refused_screen_one.*
import kotlinx.android.synthetic.main.dialog_refused_screen_two.*


class RefuseMessageDialog(
    context: Context,
    val onCompleteSale: (RefuseDialogResult) -> Unit,
    val onRefuse: (RefuseDialogResult) -> Unit = {}
) :
    AlertDialog.Builder(context) {
    private var alertDialog: AlertDialog? = null
    private val refuseDialogResult = RefuseDialogResult()

    fun showDialogs() {
        alertDialog?.dismiss()
        when {
            refuseDialogResult.isIdRequered == null -> {
                show(RefuseDialogType.SCREEN_ONE)
                alertDialog?.radio_group_one?.apply {
                    isIdRequered.values().forEachIndexed { index, isIdRequered ->
                        addView(AppCompatRadioButton(context).apply {
                            id = isIdRequered.ordinal
                            text = isIdRequered.value
                        }, index)
                    }
                    setOnCheckedChangeListener { group, checkedId ->
                        alertDialog?.getButton(0)?.isEnabled = true
                        refuseDialogResult.isIdRequered = when (checkedId) {
                            isIdRequered.NO_APPEARS_OVER_25.ordinal -> isIdRequered.NO_APPEARS_OVER_25
                            isIdRequered.YES.ordinal -> isIdRequered.YES
                            isIdRequered.NO_OTHER_REASON.ordinal -> isIdRequered.NO_OTHER_REASON
                            else -> null
                        }
                        gotoNextQuestion()
                    }
                }
            }
            refuseDialogResult.wasIdProvided == null -> {
                show(RefuseDialogType.SCREEN_TWO)
                alertDialog?.radio_group_two?.apply {
                    Was_ID_Provided.values().forEachIndexed { index, wasIdProvided ->
                        addView(AppCompatRadioButton(context).apply {
                            id = wasIdProvided.ordinal
                            text = wasIdProvided.value
                        }, index)
                    }
                    setOnCheckedChangeListener { group, checkedId ->
                        alertDialog?.getButton(0)?.isEnabled = true
                        refuseDialogResult.wasIdProvided = when (checkedId) {
                            Was_ID_Provided.No.ordinal -> Was_ID_Provided.No
                            Was_ID_Provided.YES.ordinal -> Was_ID_Provided.YES
                            Was_ID_Provided.YES_BUT_REFUSED.ordinal -> Was_ID_Provided.YES_BUT_REFUSED
                            else -> null
                        }
                        gotoNextQuestion()
                    }
                }
            }
            refuseDialogResult.gender == null -> {
                show(RefuseDialogType.SCREEN_GENDER)
                alertDialog?.radio_group_two?.apply {
                    Gender.values().toList().forEachIndexed { index, gender ->
                        val radioButton = RadioButton(context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                            id = gender.id
                            text = gender.value
                        }
                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.gender = gender
//                        }

                        setOnCheckedChangeListener { group, checkedId ->
                           val selected = Gender.values().first {
                                it.id == checkedId
                           }
                            refuseDialogResult.gender = selected
                            gotoNextQuestion()
                        }
                    }
                }
            }
            refuseDialogResult.ethnicOrigin == null -> {
                show(RefuseDialogType.SCREEN_ETHNIC_ORIGIN)
                alertDialog?.radio_group_two?.apply {
                    EthnicOrigin.values().toList().forEachIndexed { index, gender ->
                        val radioButton = RadioButton(context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                            id = gender.id
                            text = gender.value
                        }
                        addView(radioButton)
//                        if (index == 0){ this.check(radioButton.id)
//                            refuseDialogResult.ethnicOrigin = gender
//                        }
                        setOnCheckedChangeListener { group, checkedId ->
                            val selected = EthnicOrigin.values().first {
                                it.id == checkedId
                            }
                            refuseDialogResult.ethnicOrigin = selected
                            gotoNextQuestion()
                        }
                    }
                }
            }
            refuseDialogResult.approxAge == null -> {
                show(RefuseDialogType.SCREEN_APPROX_AGE)

                alertDialog?.radio_group_two?.apply {
                    ApproxAge.values().toList().forEachIndexed { index, gender ->
                        val radioButton = RadioButton(context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                            id = gender.id
                            text = gender.value
                        }
                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.approxAge = gender
//                        }
                        setOnCheckedChangeListener { group, checkedId ->
                            val selected = ApproxAge.values().first {
                                it.id == checkedId
                            }
                            refuseDialogResult.approxAge = selected
                            gotoNextQuestion()
                        }
                    }
                }
            }
            refuseDialogResult.build == null -> {
                show(RefuseDialogType.SCREEN_BUILD)

                alertDialog?.radio_group_two?.apply {
                    Build.values().toList().forEachIndexed { index, gender ->
                        val radioButton = RadioButton(context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                            id = gender.id
                            text = gender.value
                        }
                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.build = gender
//                        }
                        setOnCheckedChangeListener { group, checkedId ->
                            val selected = Build.values().first {
                                it.id == checkedId
                            }
                            refuseDialogResult.build = selected
                            gotoNextQuestion()
                        }
                    }
                }

            }
            refuseDialogResult.height == null -> {
                show(RefuseDialogType.SCREEN_HEIGHT)

                alertDialog?.radio_group_two?.apply {
                    Height.values().toList().forEachIndexed { index, gender ->
                        val radioButton = RadioButton(context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                            id = gender.id
                            text = gender.value
                        }
                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.height = gender
//                        }
                        setOnCheckedChangeListener { group, checkedId ->
                            val selected = Height.values().first {
                                it.id == checkedId
                            }
                            refuseDialogResult.height = selected
                            gotoNextQuestion()
                        }
                    }
                }
            }
            refuseDialogResult.hairColour == null -> {
                show(RefuseDialogType.SCREEN_HAIR_COLOUR)

                alertDialog?.radio_group_two?.apply {
                    HairColour.values().toList().forEachIndexed { index, hairColour ->
                        val radioButton = RadioButton(context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                            id = hairColour.value.hashCode()
                            text = hairColour.value
                        }
                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.hairColour = hairColour
//                        }
                        setOnCheckedChangeListener { group, checkedId ->
                            val selected = HairColour.values().first {
                                it.value.hashCode() == checkedId
                            }
                            refuseDialogResult.hairColour = selected
                            gotoNextQuestion()
                        }
                    }
                }
            }
//            listOf(
//                ,
//                ,
//                ,
//                ,
//                ,
//
//            ).any { it == null } -> {
//                show(RefuseDialogType.SCREEN_THREE)
//                alertDialog?.gender_radio_group?.apply {
//
//                    Gender.values().toList().forEachIndexed { index, gender ->
//                        val radioButton = RadioButton(context).apply {
//                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT)
//                            id = gender.id
//                            text = gender.value
//                        }
//                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.gender = gender
//                        }
//                        setOnCheckedChangeListener { group, checkedId ->
//                           val selected = Gender.values().first {
//                                it.id == checkedId
//                           }
//                            refuseDialogResult.gender = selected
//                        }
//                    }
//                }
//                alertDialog?.gender_spinner?.apply {
//                    initSpinner(Gender.toHashMap()) { refuseDialogResult.gender = it }
//                }
//                alertDialog?.ethnic_origin_radio_group?.apply {
//                    EthnicOrigin.values().toList().forEachIndexed { index, gender ->
//                        val radioButton = RadioButton(context).apply {
//                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT)
//                            id = gender.id
//                            text = gender.value
//                        }
//                        addView(radioButton)
//                        if (index == 0){ this.check(radioButton.id)
//                            refuseDialogResult.ethnicOrigin = gender
//                        }
//                        setOnCheckedChangeListener { group, checkedId ->
//                            val selected = EthnicOrigin.values().first {
//                                it.id == checkedId
//                            }
//                            refuseDialogResult.ethnicOrigin = selected
//                        }
//                    }
//                }
//                alertDialog?.approx_age_radio_group?.apply {
//                    ApproxAge.values().toList().forEachIndexed { index, gender ->
//                        val radioButton = RadioButton(context).apply {
//                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT)
//                            id = gender.id
//                            text = gender.value
//                        }
//                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.approxAge = gender
//                        }
//                        setOnCheckedChangeListener { group, checkedId ->
//                            val selected = ApproxAge.values().first {
//                                it.id == checkedId
//                            }
//                            refuseDialogResult.approxAge = selected
//                        }
//                    }
//                }
//                alertDialog?.height_radio_group?.apply {
//                    Height.values().toList().forEachIndexed { index, gender ->
//                        val radioButton = RadioButton(context).apply {
//                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT)
//                            id = gender.id
//                            text = gender.value
//                        }
//                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.height = gender
//                        }
//                        setOnCheckedChangeListener { group, checkedId ->
//                            val selected = Height.values().first {
//                                it.id == checkedId
//                            }
//                            refuseDialogResult.height = selected
//                        }
//                    }
//                }
//                alertDialog?.build_radio_group?.apply {
//                    Build.values().toList().forEachIndexed { index, gender ->
//                        val radioButton = RadioButton(context).apply {
//                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT)
//                            id = gender.id
//                            text = gender.value
//                        }
//                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.build = gender
//                        }
//                        setOnCheckedChangeListener { group, checkedId ->
//                            val selected = Build.values().first {
//                                it.id == checkedId
//                            }
//                            refuseDialogResult.build = selected
//                        }
//                    }
//                }
//                alertDialog?.hair_colour_radio_group?.apply {
//                    HairColour.values().toList().forEachIndexed { index, hairColour ->
//                        val radioButton = RadioButton(context).apply {
//                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT)
//                            id = hairColour.value.hashCode()
//                            text = hairColour.value
//                        }
//                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.hairColour = hairColour
//                        }
//                        setOnCheckedChangeListener { group, checkedId ->
//                            val selected = HairColour.values().first {
//                                it.value.hashCode() == checkedId
//                            }
//                            refuseDialogResult.hairColour = selected
//                        }
//                    }
//                }
//                alertDialog?.ethnic_origin_spinner?.apply {
//                    initSpinner(EthnicOrigin.toHashMap()) { refuseDialogResult.ethnicOrigin = it }
//                }
//                alertDialog?.approx_age_spinner?.apply {
//                    initSpinner(ApproxAge.toHashMap()) { refuseDialogResult.approxAge = it }
//                }
//                alertDialog?.height_spinner?.apply {
//                    initSpinner(Height.toHashMap()) { refuseDialogResult.height = it }
//                }
//                alertDialog?.build_spinner?.apply {
//                    initSpinner(Build.toHashMap()) { refuseDialogResult.build = it }
//                }
//                alertDialog?.hair_colour_spinner?.apply {
//                    initSpinner(HairColour.toHashMap()) { refuseDialogResult.hairColour = it }
//                }
//            }
            refuseDialogResult.wasTheCustomerAbusive == null -> {
                show(RefuseDialogType.SCREEN_FOUR)
                alertDialog?.radio_group_three?.apply {
                    WasTheCustomerAbusive.values().forEachIndexed { index, wasTheCustomerAbusive ->
                        addView(AppCompatRadioButton(context).apply {
                            id = wasTheCustomerAbusive.ordinal
                            text = wasTheCustomerAbusive.value
                        }, index)
                    }
                    setOnCheckedChangeListener { group, checkedId ->
                        alertDialog?.getButton(0)?.isEnabled = true
                        refuseDialogResult.wasTheCustomerAbusive = when (checkedId) {
                            WasTheCustomerAbusive.NO.ordinal -> WasTheCustomerAbusive.NO
                            WasTheCustomerAbusive.YES.ordinal -> WasTheCustomerAbusive.YES
                            else -> null
                        }
                    }
                }
                alertDialog?.radio_group_refusal_reason?.apply {
                    RefusalReason.values().toList().forEachIndexed { index, refusalReason ->
                        val radioButton = RadioButton(context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                            id = refusalReason.value.hashCode()
                            text = refusalReason.value
                        }
                        addView(radioButton)
//                        if (index == 0){
//                            this.check(radioButton.id)
//                            refuseDialogResult.refusalReason = refusalReason
//                        }
                        setOnCheckedChangeListener { group, checkedId ->
                            val selected = RefusalReason.values().first {
                                it.value.hashCode() == checkedId
                            }
                            refuseDialogResult.refusalReason = selected
                        }
                    }
                }
//                alertDialog?.spinner_refusal_reason?.apply {
//                    //initSpinner(RefusalReason.toHashMap()) { refuseDialogResult.refusalReason = it }
//                }
            }
            refuseDialogResult.comments == null -> {
                show(RefuseDialogType.SCREEN_FIVE)
                alertDialog?.order_message_edit_text?.afterTextChanged {
                    refuseDialogResult.comments = it
                }
            }
            else -> {
                Toast.makeText(context, "Complete", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun show(refuseDialogType: RefuseDialogType): AlertDialog {
        val removingOrderItemsDialogView =
            context.getInflater().inflate(refuseDialogType.layout, null).apply {}
        setView(removingOrderItemsDialogView)
        setTitle(refuseDialogType.title)
        setPositiveButton(R.string.confirm) { _, _ ->
            when {
                isCanCompleteSale() -> {
                    onCompleteSale(refuseDialogResult)
                    this.alertDialog?.dismiss()
                }
                refuseDialogResult.isAllFieldsNotNull() -> {
                    onRefuse(refuseDialogResult)
                }
                else -> {
                    showDialogs()
                }
            }
        }
        setCancelable(false)
        create().apply {
            getButton(0)?.isEnabled = false
        }

        return show().also { alertDialog = it }
    }

    private fun gotoNextQuestion() {
        when {
            isCanCompleteSale() -> {
                onCompleteSale(refuseDialogResult)
                this.alertDialog?.dismiss()
            }
            refuseDialogResult.isAllFieldsNotNull() -> {
                onRefuse(refuseDialogResult)
            }
            else -> {
                showDialogs()
            }
        }
    }

    private fun isCanCompleteSale(): Boolean {
        return refuseDialogResult.isIdRequered == isIdRequered.NO_APPEARS_OVER_25 ||
                refuseDialogResult.wasIdProvided == Was_ID_Provided.YES
    }

    enum class RefuseDialogType(val layout: Int, val title: String) {
        SCREEN_ONE(R.layout.dialog_refused_screen_one, "Is ID Required?"),
        SCREEN_TWO(R.layout.dialog_refused_screen_two, "Was ID Provided?"),
        //SCREEN_THREE(R.layout.dialog_refused_screen_three, "Customer Details"),
        SCREEN_GENDER(R.layout.dialog_refused_screen_two,"Gender"),
        SCREEN_ETHNIC_ORIGIN(R.layout.dialog_refused_screen_two, "Ethnic Origin"),
        SCREEN_APPROX_AGE(R.layout.dialog_refused_screen_two, "Approx Age"),
        SCREEN_BUILD(R.layout.dialog_refused_screen_two,"Build"),
        SCREEN_HEIGHT(R.layout.dialog_refused_screen_two,"Height"),
        SCREEN_HAIR_COLOUR(R.layout.dialog_refused_screen_two, "Hair Colour"),
        SCREEN_FOUR(R.layout.dialog_refused_screen_four, "Reason for Refusal"),
        SCREEN_FIVE(R.layout.dialog_refused_screen_five, "Comments")
    }

    @Parcelize
    data class RefuseDialogResult(
        @SerializedName("is_id_required")
        var isIdRequered: isIdRequered? = null,
        @SerializedName("was_id_provided")
        var wasIdProvided: Was_ID_Provided? = null,

        @SerializedName("gender")
        var gender: Gender? = null,
        @SerializedName("ethnic_origin")
        var ethnicOrigin: EthnicOrigin? = null,
        @SerializedName("approx_age")
        var approxAge: ApproxAge? = null,
        @SerializedName("height")
        var height: Height? = null,
        @SerializedName("build")
        var build: Build? = null,
        @SerializedName("hair_colour")
        var hairColour: HairColour? = null,

        @SerializedName("was_the_customer_abusive")
        var wasTheCustomerAbusive: WasTheCustomerAbusive? = null,
        @SerializedName("refusal_reason")
        var refusalReason: RefusalReason? = null,
        @SerializedName("comments")
        var comments: String? = null
    ): Parcelable {
        fun isAllFieldsNotNull() = arrayListOf(
            isIdRequered,
            wasIdProvided,
            gender,
            ethnicOrigin,
            approxAge,
            height,
            build,
            hairColour,
            wasTheCustomerAbusive,
            refusalReason,
            comments
        ).all{ it != null}
    }
}

internal fun <K, E> Spinner.initSpinner(map: HashMap<K, E>, onSelect: (E?) -> Unit) {

    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, map.keys.toList())
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            onSelect(map[adapter.getItem(position) as K])
        }
    }
}

enum class isIdRequered(val value: String) {
    NO_APPEARS_OVER_25("No appeares over 25"),
    YES("Yes"),
    NO_OTHER_REASON("No other Reason");

    fun toHashMap(): HashMap<String, isIdRequered> {
        val map = HashMap<String, isIdRequered>()
        values().forEach {
            map[it.value] = it
        }
        return map
    }
}

enum class Was_ID_Provided(val value: String) {
    YES("Yes"),
    YES_BUT_REFUSED("Yes but Refused"),
    No("No");

    fun toHashMap(): HashMap<String, Was_ID_Provided> {
        val map = HashMap<String, Was_ID_Provided>()
        values().forEach {
            map[it.value] = it
        }
        return map
    }
}

enum class Gender(val value: String, val id: Int) {
    Male("Male", 101),
    Female("Female", 102),
    PreferNotToSpecifiy("Prefer Not to Specifiy", 103);

    companion object {
        fun toHashMap(): HashMap<String, Gender> {
            val map = HashMap<String, Gender>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}

enum class EthnicOrigin(val value: String, val id: Int) {
    Unknown("Unknown", 111),
    White("White", 112),
    MixedEthnic("Mixed Ethnic", 113),
    Asian("Asian", 114),
    Black("Black", 115);

    companion object {
        fun toHashMap(): HashMap<String, EthnicOrigin> {
            val map = HashMap<String, EthnicOrigin>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}

enum class ApproxAge(val value: String, val id: Int) {
    AGE_12_14("12 - 14", 121),
    AGE_15_17("15 - 17", 122),
    AGE_18_20("18 - 20", 123),
    AGE_21_ABOVE("21 +", 124);

    companion object {
        fun toHashMap(): HashMap<String, ApproxAge> {
            val map = HashMap<String, ApproxAge>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}

enum class Height(val value: String, val id: Int) {
    SMALL("< 5'/ 5' - 5'6\"", 131),
    MEDIUM("5'7\" - 6'", 132),
    HIGH("> 6'", 133);

    companion object {
        fun toHashMap(): HashMap<String, Height> {
            val map = HashMap<String, Height>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}

enum class Build(val value: String, val id: Int) {
    Slim("Slim",141),
    Average("Average", 142),
    Athletic("Athletic", 143),
    Large("Large", 144);

    companion object {
        fun toHashMap(): HashMap<String, Build> {
            val map = HashMap<String, Build>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}

enum class HairColour(val value: String) {
    None("None"),
    Brown("Brown"),
    Blonde("Blonde"),
    Black("Black"),
    Auburn("Auburn"),
    Red("Red"),
    Grey("Grey"),
    White("White");

    companion object {
        fun toHashMap(): HashMap<String, HairColour> {
            val map = HashMap<String, HairColour>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}

enum class WasTheCustomerAbusive(val value: String) {
    YES("YES"),
    NO("NO");

    companion object {
        fun toHashMap(): HashMap<String, WasTheCustomerAbusive> {
            val map = HashMap<String, WasTheCustomerAbusive>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}

enum class RefusalReason(val value: String) {
    UnderAged("Under Aged"),
    DidNotHaveAnyID("Did not have any ID"),
    LookedYoungerThanIDShows("Looked Younger than ID Shows"),
    AttemptPurchaseForUnderage("Attempt to purchase for an underage"),
    IDAppearedToBeFaked("ID Appeared to be faked"),
    UNDER_ALCOHOL_OR_DRUGS("Appeared to be under the influence of alcohol or drugs"),
    OTHER("Other");

    companion object {
        fun toHashMap(): HashMap<String, RefusalReason> {
            val map = HashMap<String, RefusalReason>()
            values().forEach {
                map[it.value] = it
            }
            return map
        }
    }
}