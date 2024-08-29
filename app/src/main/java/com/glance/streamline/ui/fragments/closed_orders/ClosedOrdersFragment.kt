package com.glance.streamline.ui.fragments.closed_orders

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder
import com.glance.streamline.R
import com.glance.streamline.data.entities.PaymentHistoryInfo
import com.glance.streamline.domain.repository.payment.RefundRequest
import com.glance.streamline.ui.activities.main.MainActivity
import com.glance.streamline.ui.activities.main.MainActivityViewModel
import com.glance.streamline.ui.adapters.recycler_view.PaymentProductsListAdapter
import com.glance.streamline.ui.adapters.recycler_view.PaymentsListAdapter
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.ui.dialogs.PartialRefundDialog
import com.glance.streamline.ui.fragments.main.checkout.PaymentViewModel
import com.glance.streamline.utils.extensions.*
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.visible
import com.pax.dal.exceptions.PrinterDevException
import com.pax.neptunelite.api.NeptuneLiteUser
import kotlinx.android.synthetic.main.fragment_closed_orders.*
import kotlinx.android.synthetic.main.fragment_closed_orders.order_items_list
import kotlinx.android.synthetic.main.fragment_closed_orders.total_sum_text_view
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class ClosedOrdersFragment : BaseFragment<ClosedOrdersFragmentViewModel>() {

    private var mSelectedPaymentHistoryInfo: PaymentHistoryInfo? = null

    private var searchView: SearchView? = null
    private var clearOrderMenuItem: MenuItem? = null

    private var mSearchQuery: String? = null

    private var ordersListAdapter: PaymentsListAdapter? = null
    private val orderItemsListAdapter by lazy { order_items_list.adapter as? PaymentProductsListAdapter }

    private var orderProductsClearingDialog: AlertDialog? = null

    private var timer: Timer = Timer()
    private var isTimerRunning = false

    private val mainActivityViewModel by lazy { baseActivity.injectViewModel(viewModelFactory) as MainActivityViewModel }
    private val paymentVM by lazy { baseActivity.injectViewModel(viewModelFactory) as PaymentViewModel }

    override fun layout(): Int = R.layout.fragment_closed_orders

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): ClosedOrdersFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }

    override fun initialization(view: View, isFirstInit: Boolean) {
        setHasOptionsMenu(true)
        if (isFirstInit) {
            initViews()
            initClicks()
            initObservers()
        }
    }

    override fun onResume() {
        super.onResume()
        orderItemsListAdapter?.clear()

        viewModel.getPaymentHistories()

        isTimerRunning = false
        timer.cancel()
    }

    private fun initViews() {
        showTotalAmount()

        orders_list.let {
            val llm = LinearLayoutManager(this.baseContext)
            llm.orientation = LinearLayoutManager.VERTICAL
            it.layoutManager = llm
        }

        order_items_list.let {
            val llm = LinearLayoutManager(this.baseContext)
            llm.orientation = LinearLayoutManager.VERTICAL
            it.layoutManager = llm
        }

        order_items_list.adapter = PaymentProductsListAdapter(
            this,
                onItemsChanged = ::onOrdersAdapterChanged)
        onOrdersAdapterChanged(0)
    }

    private fun initObservers() {
        viewModel.paymentHistoriesLiveData.observe(this, ::onPaymentHistoriesLoaded)
        viewModel.searchPaymentsResultsLiveData.observe(this) {
            loadPaymentHistoryList(it)
        }
        viewModel.selectedPaymentHistory.observe(this) {
            mSelectedPaymentHistoryInfo = it
            showSelectedPaymentItems()
        }
    }

    private fun onOrdersAdapterChanged(itemCount: Int) {
        if (itemCount == 0) txt_selected_payment_products_list_empty.visible()
        else txt_selected_payment_products_list_empty.gone()
        updateClearOrderButton(itemCount == 0)
        updateChargeButton()
    }

    private fun updateChargeButton() {
        orderItemsListAdapter?.let {
            btn_print_receipt.isEnabled = it.itemCount > 0

            var isFullRefunded = true
            it.currentList.forEach { product ->
                if (!product.isRefunded) isFullRefunded = false
            }
            btn_refund_full.isEnabled = !isFullRefunded
            btn_refund_partial.isEnabled = !isFullRefunded

        }
    }

    private fun onPaymentHistoriesLoaded(paymentHistories: List<PaymentHistoryInfo>?) {
        loadPaymentHistoryList(paymentHistories)
        if (!isTimerRunning) refresh()

        mSearchQuery?.let{
            viewModel.searchPaymentHistory(it)
        }
    }

    private fun loadPaymentHistoryList(paymentHistories: List<PaymentHistoryInfo>?) {
        orderItemsListAdapter?.clear()
        ordersListAdapter = PaymentsListAdapter(paymentHistories ?: arrayListOf(), this) {
            mSelectedPaymentHistoryInfo = it
            showSelectedPaymentItems()
            showTotalAmount()
        }
        orders_list.adapter = ordersListAdapter
    }

    private fun showSelectedPaymentItems() {
        mSelectedPaymentHistoryInfo?.let { paymentHistory ->
            val products = paymentHistory.products
            orderItemsListAdapter.let {
                it?.setProductItems(products)
            }
        }
    }

    private fun showTotalAmount() {
        mSelectedPaymentHistoryInfo?.let { paymentHistory ->
            val totalAmount = paymentHistory.totalAmount - paymentHistory.cardRefundAmount - paymentHistory.cashRefundAmount

            total_sum_text_view.text = String.format("Total £%.02f", totalAmount)
            select_order_text_view.gone()
            payment_detail_view.visible()
            order_number_text_view.text = String.format("#%04d", paymentHistory.orderNumber)

            val strDate = SimpleDateFormat("HH:mm dd/MM/yy", Locale.ENGLISH).format(paymentHistory.paymentDate)
            payment_date_text_view.text = strDate
            payment_username_text_view.text = paymentHistory.userName
        } ?: run {
            total_sum_text_view.text = String.format("Total £0.00")
            select_order_text_view.visible()
            payment_detail_view.gone()
        }
    }

    private fun initClicks() {
        btn_logout.setOnClickListener {
            showLogoutDialog()
        }

        btn_plus.setOnClickListener {

        }

        btn_refund_partial.setOnClickListener {
            showRefundPartialDailog()
        }
        btn_refund_full.setOnClickListener {
            showRefundFullDialog()
        }
        btn_print_receipt.setOnClickListener {
            printReceipt(generateReceipt())
//            generateReceipt()
        }
    }

    private fun showLogoutDialog() {
        val mainActivity = baseActivity as MainActivity
        mainActivity.showLogoutDialog()
    }

    private fun showRefundPartialDailog() {
        mSelectedPaymentHistoryInfo?.let { paymentHistory ->
            PartialRefundDialog(requireContext(), paymentHistory) {
                refundPartial(it)
            }.showDialog()
        }
    }

    private fun showRefundFullDialog() {
        var message = StringBuilder()
        mSelectedPaymentHistoryInfo?.let { paymentHistory ->
            var totalAmount = 0f
            paymentHistory.products.forEach { product ->
                if (!product.isRefunded) {
                    totalAmount += product.productRetailPrice.toFloat()
                }
            }

            message.append(String.format("Order #%04d<br/>", paymentHistory.orderNumber))

            val strDate = SimpleDateFormat("HH:mm dd/MM/yy", Locale.ENGLISH).format(paymentHistory.paymentDate)
            message.append("$strDate<br/>")
            message.append(paymentHistory.userName + "<br/>")
            message.append("Reprint Receipt <br/><br/>")
            message.append(String.format("<b>TOTAL REFUND: £%.02f</b>", totalAmount))

            AlertDialog.Builder(baseActivity)
                .setTitle(R.string.refund_full_order)
                .setMessage(Html.fromHtml(message.toString()))
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    refundFullOrder()
                }
                .setNegativeButton(R.string.text_cancel) { _, _ -> }
                .setCancelable(false)
                .create().apply {
                    show()
                }
        }
    }

    private fun refundFullOrder() {
        mSelectedPaymentHistoryInfo?.let { paymentHistory ->
            val cardRefundAmount = paymentHistory.cardAmount - paymentHistory.cardRefundAmount

            if (cardRefundAmount > 0) {
                if (paymentVM.refundCard(cardRefundAmount)) {
                    viewModel.refundAll(paymentHistory)
                }
            } else {
                viewModel.refundAll(paymentHistory)
            }
        }
    }

    private fun refundPartial(refundRequest: RefundRequest) {
        mSelectedPaymentHistoryInfo?.let { paymentHistory ->
            val totalRefundAmount = paymentHistory.cardAmount + paymentHistory.cashAmount - (paymentHistory.cardRefundAmount + paymentHistory.cashRefundAmount)
            var amount = 0f
            paymentHistory.products.forEach { product ->
                if (refundRequest.productIds.contains(product.id)) {
                    amount += product.productRetailPrice.toFloat()
                }
            }

            val cardRefundAmount = paymentHistory.cardAmount - paymentHistory.cardRefundAmount
            if (cardRefundAmount > 0) {
                if (cardRefundAmount >= amount) {
                    if (paymentVM.refundCard(amount)) {
                        refundRequest.cardAmount = amount
                        refundRequest.cashAmount = 0f
                        viewModel.refundPartial(paymentHistory, refundRequest)
                    }
                } else {
                    if (paymentVM.refundCard(cardRefundAmount)) {
                        refundRequest.cardAmount = cardRefundAmount
                        refundRequest.cashAmount = amount - cardRefundAmount
                        viewModel.refundPartial(paymentHistory, refundRequest)
                    }
                }
            } else {
                refundRequest.cardAmount = 0f
                refundRequest.cashAmount = amount
                viewModel.refundPartial(paymentHistory, refundRequest)
            }
        }
    }

    private fun generateReceipt(): Bitmap? {
        val receipt = ReceiptBuilder(1200)
        mSelectedPaymentHistoryInfo?.let { paymentHistory ->
            val strPaymentDate = paymentHistory.paymentDate.toDateString()
            val strPaymentTime = paymentHistory.paymentDate.toTimeString()

            val current =
                Date().formatIso8601()?.let { it1 -> Date().parseIso8601(it1, Locale.ENGLISH) }
            val strCurrentDate = current?.toDateString()
            val strCurrentTime = current?.toTimeString()

            val userModel = mainActivityViewModel.userLiveData.value ?: return null
            val userName = userModel.user_name
            val hubName = userModel.devices.joinToString { it.name }
            val location = userModel.hub

            receipt.setMargin(100, 80)
                .setAlign(Paint.Align.CENTER)
                .setColor(Color.BLACK)
                .setTextSize(70.0f)
                .setTypeface(baseContext, "rubik_medium.ttf")
                .addText(hubName)
                .addParagraph()
                .addBlankSpace(20)
                .setTextSize(50.0f)
                .setTypeface(baseContext, "rubik_regular.ttf")
                .setAlign(Paint.Align.LEFT)
                .addText(strCurrentDate, false)
                .setAlign(Paint.Align.RIGHT)
                .addText(strCurrentTime, false)
                .addParagraph()
                .addBlankSpace(5)
                .addLine()
                .addParagraph()
                .setTypeface(baseContext, "rubik_medium.ttf")
                .setAlign(Paint.Align.CENTER)
                .addText("Sale")
                .addParagraph()
                .addBlankSpace(20)
                .setTextSize(40.0f)
                .setTypeface(baseContext, "rubik_regular.ttf")
                .setAlign(Paint.Align.RIGHT)
                .addText(String.format("%s %s", strPaymentTime, strPaymentDate), false)
                .addParagraph()
                .addBlankSpace(5)

                .addParagraph()
                .setTextSize(50.0f)
//                .setTypeface(baseContext, "rubik_regular.ttf")

            var isRefunded = false
            var totalAmount = 0f
            var refundAmount = 0f

            paymentHistory.products.forEach { product ->
                if (!product.isRefunded) {
                    receipt.setAlign(Paint.Align.LEFT)
                        .addText(product.productName, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText("£" + product.productRetailPrice)
                        .addBlankSpace(5)
                    totalAmount += product.productRetailPrice.toFloat()
                } else {
                    isRefunded = true
                }
            }

            if (isRefunded) {
                receipt
                    .addBlankSpace(30)
                    .addParagraph()
                    .addLine()
                    .addParagraph()
                    .setTypeface(baseContext, "rubik_medium.ttf")
                    .setAlign(Paint.Align.CENTER)
                    .addText("Refund")
                    .addParagraph()
                    .addBlankSpace(20)
                    .setTypeface(baseContext, "rubik_regular.ttf")

                paymentHistory.products.forEach {
                    if (it.isRefunded) {
                        receipt.setAlign(Paint.Align.LEFT)
                            .addText(it.productName, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText("£" + it.productRetailPrice)
                            .addBlankSpace(5)
                        refundAmount += it.productRetailPrice.toFloat()
                    } else {
                        isRefunded = true
                    }
                }
            }

            receipt.setAlign(Paint.Align.LEFT)
                .addBlankSpace(30)
                .addParagraph()
                .addLine()
                .addParagraph()
                .setTypeface(baseContext, "rubik_medium.ttf")
                .setTextSize(50.0f)
                .setAlign(Paint.Align.RIGHT)
                .addText(String.format("Refund            £%.02f", refundAmount))
                .addBlankSpace(5)
                .addText(String.format("Total            £%.02f", totalAmount))
                .addParagraph()

            receipt
                .addLine()
                .addParagraph()
                .setTextSize(40.0f)
                .setTypeface(baseContext, "rubik_regular.ttf")
                .setAlign(Paint.Align.CENTER)
                .addText("You were served by $userName")
                .addParagraph()
                .addText(hubName)
                .addParagraph()
                .addText(location)
                .addParagraph()
        }
        val bitmap = receipt.build()

//        imgTest.setImageBitmap(bitmap)
//        imgTest.visible()
//        imgTest.setOnClickListener {
//            imgTest.gone()
//        }

        return bitmap
    }

    private fun printReceipt(receipt: Bitmap?) {
        if (receipt == null) return

        val paxIdal = NeptuneLiteUser.getInstance().getDal(context)
        val prn = paxIdal.printer;
        try {
            prn.init()
            prn.printBitmap(receipt)
            prn.step(50)

            var apiResult = prn.start()

            when(apiResult) {
                0 -> {
                    // Submission successfully made.
                }
                1 -> {
                    // Busy, so far so good.
                }
                2 -> {
                    // Out of paper.
                }
                else -> {

                }
            }

            // Thread this.
            do {
                // Check every quarter-second for result of print.
                Thread.sleep(250)
                apiResult = prn.getStatus()
            } while (apiResult == 1)

        } catch (ex: PrinterDevException) {
            showErrorSnack("Receipt Reprint Failed")
            ex.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_closed_orders, menu)
        menu.findItem(R.id.action_products_search)?.let { searchItem ->
            searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem) = true
                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    closeSearchView()
                    return true
                }
            })
            searchView = searchItem.actionView as SearchView
            initSearchView()
        }
        clearOrderMenuItem = menu.findItem(R.id.action_clear_order_items).apply {
            setOnMenuItemClickListener {
                showOrderClearingDialog()
                true
            }
        }
        updateClearOrderButton(orderItemsListAdapter?.itemCount == 0)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showOrderClearingDialog() {
        if (orderItemsListAdapter?.itemCount != 0)
            orderProductsClearingDialog = AlertDialog.Builder(baseActivity)
                .setTitle(R.string.clear_order_item_dialog_title)
                .setMessage(R.string.clear_order_item_dialog_message)
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    clearOrderItems()
                }
                .setNegativeButton(R.string.text_cancel) { _, _ -> }
                .setCancelable(false)
                .create().apply {
                    show()
                }
    }

    private fun clearOrderItems() {
        mSelectedPaymentHistoryInfo = null
        orderItemsListAdapter?.clear()
        ordersListAdapter?.onClearSelected()
        showTotalAmount()
    }

    private fun updateClearOrderButton(isOrderItemsListEmpty: Boolean) {
        clearOrderMenuItem?.apply {
            icon.alpha = if (isOrderItemsListEmpty) 128 else 255
            isEnabled = !isOrderItemsListEmpty
        }
    }

    /*Search*/
    private fun initSearchView() {
        searchView?.apply {
            queryHint = getString(R.string.products_search_view_hint)
            maxWidth = Integer.MAX_VALUE
            isIconified = true
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (query.isNotBlank()) {
                        mSearchQuery = query.trim()
                        viewModel.searchPaymentHistory(query.trim())
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isBlank()) {
                        searchView?.clearFocus()
                        mSearchQuery = null
                        viewModel.onSearchClosed()
                        viewModel.searchPaymentHistory("")
                    } else {
                        mSearchQuery = newText.trim()
                        viewModel.searchPaymentHistory(mSearchQuery!!)
                    }

                    return false
                }
            })

            findViewById<View>(R.id.search_close_btn)?.onClick {
                closeSearchView()
            }
        }
    }

    private fun closeSearchView() {
        searchView?.clearFocus()
        searchView?.setQuery("", false)
        mSearchQuery = null
        viewModel.onSearchClosed()
    }
    /*Search*/

    private fun refresh() {
        isTimerRunning = true
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                viewModel.checkLastUpdated()
            }
        }, 10 * 1000L,  10 * 60 * 1000L)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderProductsClearingDialog?.dismiss()
        timer.cancel()
        isTimerRunning = false
        viewModel.clearAllData()
    }
}