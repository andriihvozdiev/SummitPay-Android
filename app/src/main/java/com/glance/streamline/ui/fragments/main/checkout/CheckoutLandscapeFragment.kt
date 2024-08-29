package com.glance.streamline.ui.fragments.main.checkout

import android.content.DialogInterface
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glance.streamline.R
import com.glance.streamline.data.entities.CategoryInfo
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.domain.model.payment.PaymentResponseType
import com.glance.streamline.domain.model.payment.PaymentResultModel
import com.glance.streamline.domain.repository.payment.PaymentRequest
import com.glance.streamline.domain.tcp.OpenTillDrawerAsyncTask
import com.glance.streamline.domain.tcp.TCPClient
import com.glance.streamline.ui.activities.main.MainActivityViewModel
import com.glance.streamline.ui.adapters.recycler_view.CategoryLayoutsListAdapter
import com.glance.streamline.ui.adapters.recycler_view.OrderItemsListAdapter
import com.glance.streamline.ui.adapters.recycler_view.ProductsListAdapter
import com.glance.streamline.ui.adapters.spinner.SpinnerTablesAdapter
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.ui.dialogs.ProductDeletingDialog
import com.glance.streamline.ui.dialogs.ProductMessageDialog
import com.glance.streamline.ui.dialogs.refuselog.RefuseMessageDialog
import com.glance.streamline.ui.models.CashPaymentResult
import com.glance.streamline.ui.models.ProductModelDto
import com.glance.streamline.ui.models.TableModel
import com.glance.streamline.utils.custom_views.ProductOptionsPopup
import com.glance.streamline.utils.custom_views.SpannedGridLayoutManager
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.observe
import com.glance.streamline.utils.extensions.android.view.blink
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.visible
import com.glance.streamline.utils.extensions.toISO_8601_Timezone
import kotlinx.android.synthetic.main.dialog_product_quantity.*
import kotlinx.android.synthetic.main.fragment_checkout_landscape.*
import java.util.*
import kotlin.collections.ArrayList

private const val ROWS_COUNT = 5

class CheckoutLandscapeFragment : BaseFragment<CheckoutFragmentViewModel>() {
    private var isRefusalLogShowing: Boolean = false

    override fun layout(): Int = R.layout.fragment_checkout_landscape
    private val mainActivityViewModel by lazy { baseActivity.injectViewModel(viewModelFactory) as MainActivityViewModel }
    private val paymentVM by lazy { baseActivity.injectViewModel(viewModelFactory) as PaymentViewModel }

    private val tablesListAdapter by lazy { tables_spinner.adapter as? SpinnerTablesAdapter }
    private val orderItemsListAdapter by lazy { order_items_list.adapter as? OrderItemsListAdapter }
    private var productLayoutListAdapter: CategoryLayoutsListAdapter? = null

    private var productOptionsPopup: ProductOptionsPopup? = null
    private var productMessageDialog: AlertDialog? = null
    private var productRemovingDialog: AlertDialog? = null
    private var productsQuantityDialog: AlertDialog? = null

    private var mSelectedLayoutId: String? = null

    override fun initialization(view: View, isFirstInit: Boolean) {
        paymentVM.listenViewModelUpdates()
        if (isFirstInit) {
            initClicks()
            initSearchView()
            initOrderItemsList()
            initSpinner()
            updateChargeButton()
            initObservers()

            mainActivityViewModel.getLogoutTimeLeft()
            mainActivityViewModel.startCheckingLastUserLogin()
            viewModel.getLayoutsList()
            viewModel.getTablesList()
        }
    }

    /*Search*/
    private fun initSearchView() {
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotBlank())
                    viewModel.searchProducts(query.trim())
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isBlank()){
                    search_view?.clearFocus()
                    viewModel.currentSelectedLayoutLiveData.value?.let { onSelectLayout(it) }
                    viewModel.onSearchClosed()
                }
                return false
            }
        })

        search_view.findViewById<View>(R.id.search_close_btn)?.onClick {
            closeSearchView()
        }
    }

    private fun closeSearchView() {
        search_view.setQuery("", false)
        search_view.clearFocus()
        viewModel.currentSelectedLayoutLiveData.value?.let { onSelectLayout(it) }
        viewModel.onSearchClosed()
    }
    /*Search*/

    /*Orders*/
    private fun updateChargeButton() {
        orderItemsListAdapter?.let {
            val price = it.getTotalPrice()
            card_payment_button_card.isEnabled = it.itemCount > 0
            card_payment_button_card.text = getString(R.string.charge_button_card_text, price)
            card_payment_button_cash.isEnabled = it.itemCount > 0
            card_payment_button_cash.text = getString(R.string.charge_button_cash_text, price)
        }
    }

    private fun addToOrderItemsList(productModel: OrderItemsListAdapter.BasketItem) {
        orderItemsListAdapter?.let { adapter ->
            val copiedOrderItem = when (productModel) {
                is OrderItemsListAdapter.BasketProduct -> productModel.copy()
                is OrderItemsListAdapter.BasketOption -> productModel.copy()
                else -> throw IllegalArgumentException("Unexpected type")
            }
            adapter.addOrderItem(copiedOrderItem, viewModel.customProductMultiplierLiveData.value ?: 0)
        }
        viewModel.setProductQuantity(0)
        checkIfListContainsProductsForRefusalLog(productModel)
    }

    private fun showRemovingOrderItemsDialog(
        totalItemsNumber: Int,
        onRemove: (itemsToRemove: Int) -> Unit
    ) {
        ProductDeletingDialog(baseActivity, this).apply {
            this.onRemove = onRemove
            productRemovingDialog = show(totalItemsNumber)
        }
    }

    private fun showOrderProductMessageDialog(productModel: OrderItemsListAdapter.BasketItem) {
        orderItemsListAdapter?.let { adapter ->
            ProductMessageDialog(baseActivity).apply {
                onMessageAdded = adapter::updateItemMessage
                productMessageDialog = show(productModel)
            }
        }
    }

    private fun initOrderItemsList() {
        order_items_list.adapter = OrderItemsListAdapter(
            this,
            onMessage = {
                showOrderProductMessageDialog(it)
            },
            onRemove = {
                onOrderItemRemoving(it)
            },
            onItemsChanged = ::onOrdersAdapterChanged
        )
    }

    private fun onOrdersAdapterChanged(itemCount: Int) {
        if (itemCount == 0) orders_list_empty_text_view.visible()
        else orders_list_empty_text_view.gone()
        updateChargeButton() //TODO:
    }

    private fun onOrderItemRemoving(orderItem: OrderItemsListAdapter.BasketItem) {
        if (orderItem.itemsCount > 1)
            showRemovingOrderItemsDialog(orderItem.itemsCount - 1) {
                orderItemsListAdapter?.onRemovingSpecificNumber(orderItem, it)
            }
        else orderItemsListAdapter?.onRemoveButtonClicked(orderItem)
    }

    private fun showQuantityDialog(currentQuantity: Int? = null) {
        productsQuantityDialog = AlertDialog.Builder(baseActivity)
            .setTitle(R.string.product_quantity_dialog_title)
            .setMessage(R.string.product_quantity_dialog_message)
            .setView(R.layout.dialog_product_quantity)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                productsQuantityDialog?.let {
                    it.product_quantity_edit_text?.text?.toString()?.let { quantityString ->
                        viewModel.setProductQuantity(quantityString.trimStart { digit -> digit == '0' }
                            .toInt())
                    }
                }
            }
            .setNegativeButton(R.string.logout_dialog_cancel) { _, _ -> }
            .create().apply {
                show()
                this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                getButton(DialogInterface.BUTTON_POSITIVE)?.let { positiveButton ->
                    val currentQuantityString =
                        if (currentQuantity == null || currentQuantity == 0) "" else currentQuantity.toString()
                    product_quantity_edit_text?.setText(currentQuantityString)

                    positiveButton.gone()
                    product_quantity_edit_text?.addTextChangedListener {
                        val quantityString = it?.toString()?.trim() ?: ""
                        if (quantityString.isNotEmpty() && quantityString.isDigitsOnly()) {
                            val quantity = quantityString.toInt()
                            if (quantity > 0) {
                                positiveButton.visible()
                                return@addTextChangedListener
                            }
                        }
                        positiveButton.gone()
                    }
                }
            }
    }

    /*Orders*/

    /*Tables*/
    private fun initSpinner() {
        tables_spinner.adapter = SpinnerTablesAdapter(baseActivity, arrayListOf())
        tables_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                tablesListAdapter?.let {
                    onTableSelected(it.getItem(position))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun onTableSelected(table: TableModel) {
        orderItemsListAdapter?.setOrderItems(table.orderItemsList.map {
            OrderItemsListAdapter.BasketProduct(it)
        } as ArrayList<OrderItemsListAdapter.BasketItem>)
        table_spinner_current_item_text_view.text = table.name
    }

    private fun setTablesList(tablesList: ArrayList<TableModel>) {
        if (tablesList.isNotEmpty()) {
            tablesListAdapter?.let {
                it.list.clear()
                it.list.addAll(tablesList)
                it.notifyDataSetChanged()
            }
            onTableSelected(tablesList[0])
        }
    }
    /*Tables*/

    /*Payment*/
    private fun onPaymentResult(paymentResult: PaymentResultModel) {

        val productQTV = hashMapOf<String, Int>()

        val paymentProducts = hashMapOf<String, PaymentRequest.Product>()
        val options = arrayListOf<OrderItemsListAdapter.BasketOption>()


        orderItemsListAdapter?.let {orderItemsListAdapter1 ->
            viewModel.isRefusalLogWasShown = false
            orderItemsListAdapter1.getItemsList().forEach {
                when (it) {
                    is OrderItemsListAdapter.BasketProduct -> {
                        paymentProducts[it.productId] =
                            PaymentRequest.Product(productId = it.productId)
                        productQTV[it.productId] = it.itemsCount
                    }
                    is OrderItemsListAdapter.BasketOption -> options.add(it)
                }
            }

            options.forEach {
                for (opt in 0..it.itemsCount){
                    paymentProducts[it.parentProductId]?.optionsId?.add(it.optionId)
                }
            }
        }

        val resultProducts = arrayListOf<PaymentRequest.Product>()
        paymentProducts.values.toList().map{
            for (prod in 0..(productQTV[it.productId] ?: 1)){
                resultProducts.add(it)
            }
        }

        val request = PaymentRequest(
            paymentDate = Calendar.getInstance().time.toISO_8601_Timezone(),
            lastDigits = if (paymentResult.cardNumber.length == 4) paymentResult.cardNumber
            else paymentResult.cardNumber.takeLast(4).orEmpty(),
            refusalLog = PaymentRequest.RefusalLogRequest(viewModel.lastRefusalLogResult.value),
            deviceToken = requireContext().getUniqCurrentDeviceId(),
            products = resultProducts
        )

        when (paymentResult.responseType) {
            PaymentResponseType.APPROVED -> {
                openTillDrawer()
                showSuccessSnack("Approved successfully!")
                paymentVM.uploadReport(request) {
                    orderItemsListAdapter?.clear()
                    viewModel.saveCardReports(paymentResult.amount, request.products.count())
                    showSuccessSnack("Done purchase")
//                    mainActivityViewModel.logout(true)
                }
            }
            PaymentResponseType.DECLINED -> {
                showErrorSnack("Declined!")
            }
            PaymentResponseType.ERROR -> {
                showErrorSnack("Error")
            }
        }

        viewModel.lastRefusalLogResult.value = null
    }
    /*Payment*/

    private fun openTillDrawer() {
        val handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when(msg.what) {
                    TCPClient.OPEN -> {
                        Log.d("==========", "In Handler's shutdown")
                    }
                }
            }
        }
        OpenTillDrawerAsyncTask(handler, "127.0.0.1", 4000, "27112025250").execute()
    }
    /*Layout*/
    private fun showPopUpProductOptions(
        viewToAnchor: View,
        product: ProductButtonInfo
    ) {
        addToOrderItemsList(OrderItemsListAdapter.BasketProduct(product))
        search_view.clearFocus()
        baseActivity.toggleKeyboard(false)
        productOptionsPopup = ProductOptionsPopup(this).apply {
            onAnchorViewShouldBeScrolledVertically = { minimumHeightToScroll ->
                //foods_scroll_view.smoothScrollBy(0, minimumHeightToScroll)
            }
            addToOrderItemsList = ::addToOrderItemsList
            onReopen = ::showPopUpProductOptions
            show(viewToAnchor, product)
        }
    }

    private fun onCategoryLayoutsLoaded(layoutList: List<CategoryInfo>) {
        productLayoutListAdapter = CategoryLayoutsListAdapter(
            layoutList, this,
            CategoryLayoutsListAdapter.AdapterType.FULL_SIZE
        ) {
            mSelectedLayoutId = it.id
            viewModel.getLayoutWithProducts(mSelectedLayoutId!!)
            showToastListener.showToast(it.name)
        }
        food_categories_list.adapter = productLayoutListAdapter

        if (layoutList.isNotEmpty()){
            viewModel.getLayoutWithProducts(mSelectedLayoutId ?: layoutList[0].id)
        }
    }

    private fun onSelectLayout(buttons: List<ProductButtonInfo>) {
        if (buttons.isNullOrEmpty()) {
            (food_lists_grid.adapter as? ProductsListAdapter)?.clear()
        }
        setProductLayout(buttons)
    }

    private fun setProductLayout(buttons: List<ProductButtonInfo>) {
        val adapter = (food_lists_grid.adapter as? ProductsListAdapter) ?: ProductsListAdapter(
            buttons as ArrayList<ProductButtonInfo>, this, onItemClicked
        )

        if (buttons.isNotEmpty()) {
            loadProductsList(buttons)
            products_list_empty_text_view.gone()
        } else {
            adapter.clear()
            products_list_empty_text_view.visible()
        }
    }

    private fun sortProductList(productLis: List<ProductButtonInfo>): ArrayList<ProductButtonInfo> {
        val productList = ArrayList(productLis)
        productList.sortBy { it.x }
        productList.sortBy { it.y }

        return productList
    }

    private fun loadProductsList(productLis: List<ProductButtonInfo>) {
        viewModel.setProductQuantity(0)

        val productList = sortProductList(productLis)
        if (productList.isEmpty()) return

        val startY = productList[0].y

        food_lists_grid.layoutManager = SpannedGridLayoutManager(
            object : SpannedGridLayoutManager.GridSpanLookup {
                override fun getSpanInfo(position: Int): SpannedGridLayoutManager.SpanInfo {
                    return when (position) {
                        else -> {
                            SpannedGridLayoutManager.SpanInfo(
                                productList[position].x,
                                productList[position].y - startY,
                                productList[position].w,
                                productList[position].h
                            )
                        }
                    }
                }
            }, ROWS_COUNT, 1f
        )
        food_lists_grid.adapter = ProductsListAdapter(
            productList, this, onItemClicked
        )
    }
    /*Layout*/

    private fun initObservers() {
        mainActivityViewModel.logoutTimeLeft.observe(this) {
            mainActivityViewModel.startLogoutTimeoutTimer(it)
        }
        //Tables
        viewModel.tablesLiveData.observe(this, ::setTablesList)
        //Layouts with products
        viewModel.currentSelectedLayoutLiveData.observe(this, ::onSelectLayout)
        //viewModel.currentSearchLayoutLiveData.observe(this, ::onSelectLayout)
        viewModel.categoriesLayoutListLiveData.observe(this, ::onCategoryLayoutsLoaded)

        //Search
        viewModel.searchButtonResultsLiveData.observe(this) {
            val adapter = (food_lists_grid.adapter as? ProductsListAdapter) ?: ProductsListAdapter(
                it  ?: arrayListOf(), this, onItemClicked
            )

            if (it.isNotEmpty()) {
                loadProductsList(it)
                products_list_empty_text_view.gone()
            } else {
                adapter.clear()
                products_list_empty_text_view.visible()
            }
        }
        //Payment
//        viewModel.paymentResponseLiveData.observe(this, ::onPaymentResult)
        mainActivityViewModel.cardPaymentResult.observe(this, ::onPaymentResult)
        viewModel.customProductMultiplierLiveData.observe(this, ::onCustomProductQuantityChanged)

        paymentVM.cashPaymentCompletedLiveData.observe(this, ::onCashPurchased)
    }

    private fun onCustomProductQuantityChanged(quantity: Int) {
        product_quantity_button.blink(quantity > 0)
    }

    private fun initClicks() {
        card_payment_button_card.onClick {
            orderItemsListAdapter?.let {
                viewModel.isRefusalLogWasShown = false

                findNavController().navigate(
                    CheckoutPortraitFragmentDirections.actionToPaymentDialogFragment(
                        ProductModelDto(it.getItemsList().map {
                            when (it) {
                                is OrderItemsListAdapter.BasketOption -> it
                                is OrderItemsListAdapter.BasketProduct -> it
                                else -> throw IllegalArgumentException("Unexpected type")
                            }
                        } as ArrayList<OrderItemsListAdapter.BasketItem>, it.getTotalPrice())
                    )
                )
            }
        }

        product_quantity_button.onClick {
            showQuantityDialog(viewModel.customProductMultiplierLiveData.value)
        }

        card_payment_button_cash.onClick {
            viewModel.isRefusalLogWasShown = false

            val productQTV = hashMapOf<String, Int>()

            val paymentProducts = hashMapOf<String, PaymentRequest.Product>()
            val options = arrayListOf<OrderItemsListAdapter.BasketOption>()

            var totalPrice = 0.0f
            orderItemsListAdapter?.let {orderItemsListAdapter1 ->
                totalPrice = orderItemsListAdapter1.getTotalPrice()

                viewModel.isRefusalLogWasShown = false
                orderItemsListAdapter1.getItemsList().forEach {
                    when (it) {
                        is OrderItemsListAdapter.BasketProduct -> {
                            paymentProducts[it.productId] =
                                PaymentRequest.Product(productId = it.productId)
                            productQTV[it.productId] = it.itemsCount
                        }
                        is OrderItemsListAdapter.BasketOption -> options.add(it)
                    }
                }

                options.forEach {
                    for (opt in 0..it.itemsCount){
                        paymentProducts[it.parentProductId]?.optionsId?.add(it.optionId)
                    }
                }
            }

            val resultProducts = arrayListOf<PaymentRequest.Product>()
            paymentProducts.values.toList().map{
                for (prod in 0..(productQTV[it.productId] ?: 1)){
                    resultProducts.add(it)
                }
            }

            val request = PaymentRequest(
                paymentDate = Calendar.getInstance().time.toISO_8601_Timezone(),
                lastDigits = "",
                refusalLog = PaymentRequest.RefusalLogRequest(viewModel.lastRefusalLogResult.value),
                deviceToken = requireContext().getUniqCurrentDeviceId(),
                products = resultProducts
            )

            findNavController().navigate(CheckoutLandscapeFragmentDirections.actionToTenderOptionsDialogFragment(totalPrice, request))

//            paymentVM.purchase(request) {
//                orderItemsListAdapter?.clear()
//                showSuccessSnack("Done purchase")
//            }
//            findNavController().navigate(CheckoutLandscapeFragmentDirections.actionToTenderOptionsDialogFragment(request))
        }
    }

    private fun onCashPurchased(cashPaymentResult: CashPaymentResult) {
        if (cashPaymentResult != null) {
            var cashCount = 0
            cashPaymentResult.cashPaymentRequest.products.map {
                cashCount += 1
            }

            viewModel.saveCashReports(cashPaymentResult.price, cashCount)
            showReceiptConfirmDialog(cashPaymentResult.cashPaymentRequest)
        }
    }

    private fun showReceiptConfirmDialog(paymentRequest: PaymentRequest) {
        AlertDialog.Builder(baseActivity)
            .setTitle(R.string.receipt_confirm_dialog_title)
            .setMessage(R.string.receipt_confirm_dialog_message)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                // print here
//                printReceipt()
                clearOrderProducts()
            }
            .setNegativeButton(R.string.no) { _, _ ->
                clearOrderProducts()
            }
            .create().apply {
                show()
            }
    }

    private fun clearOrderProducts() {
        orderItemsListAdapter?.clear()
        paymentVM.clears()
    }

    /*refusal log*/
    private fun showRefusalLog(product: OrderItemsListAdapter.BasketItem) {
        if (isRefusalLogShowing.not()) {
            isRefusalLogShowing = true
            RefuseMessageDialog(requireContext(), onCompleteSale = {
                viewModel.isRefusalLogWasShown = true
            }, onRefuse = {
                viewModel.lastRefusalLogResult.value = it
                orderItemsListAdapter?.onRemoveButtonClicked(product)
                isRefusalLogShowing = false
            }).showDialogs()
        }
    }

    private fun checkIfListContainsProductsForRefusalLog(product: OrderItemsListAdapter.BasketItem) {
        if (viewModel.isRefusalLogWasShown.not()) {
            if (viewModel.listOfProductsNameForRefusalLog.any { it == product.productType }) {
                showRefusalLog(product)
            }
        }
    }
    /*refusal log*/

    override fun onDestroyView() {
        super.onDestroyView()
        productOptionsPopup?.dismiss()
        productRemovingDialog?.dismiss()
        productMessageDialog?.dismiss()
    }

    val onItemClicked: (view: View, product: ProductButtonInfo) -> Unit =
        { view, product ->
            showPopUpProductOptions(view, product)
        }

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): CheckoutFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }
}

//class CheckoutViewDragListener(var selectedDrawable: Drawable? = null): View.OnDragListener {
//    private var isDropped = false
//    var tmpBackgroundDrawable: Drawable? = null
//
//    override fun onDrag(v: View, event: DragEvent): Boolean {
//        when (event.action) {
//            // signal for the start of a drag and drop operation
//            DragEvent.ACTION_DRAG_STARTED -> {
//                tmpBackgroundDrawable = v.background
//            }
//            // the drag point has entered the bounding box of the View
//            DragEvent.ACTION_DRAG_ENTERED -> {
//                selectedDrawable?.let {
//                    v.background = it
//                }
//            }
//            // the user has moved the drag shadow outside the bounding box of the View
//            DragEvent.ACTION_DRAG_EXITED -> {
//                v.background = tmpBackgroundDrawable
//            }
//            // the drag and drop operation has concluded
//            DragEvent.ACTION_DRAG_ENDED -> {
//                v.background = tmpBackgroundDrawable
//            }
//            DragEvent.ACTION_DROP -> {
//                isDropped = true
//                // Dropped, reassign View to ViewGroup
//                val view = event.localState as View
//                val owner = view.parent as GridLayout
//                owner.removeView(view)
//                owner.addView(view)
//            }
//        }
//
//        if (!isDropped && event.localState != null) {
//            (event.localState as View).visibility = View.VISIBLE
//        }
//        return true
//    }
//}