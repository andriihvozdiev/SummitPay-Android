package com.glance.streamline.ui.fragments.main.checkout

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glance.streamline.R
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.domain.model.payment.PaymentResponseType
import com.glance.streamline.domain.model.payment.PaymentResultModel
import com.glance.streamline.domain.repository.payment.PaymentRequest
import com.glance.streamline.domain.tcp.OpenTillDrawerAsyncTask
import com.glance.streamline.domain.tcp.TCPClient.Companion.OPEN
import com.glance.streamline.ui.activities.main.MainActivity
import com.glance.streamline.ui.activities.main.MainActivityViewModel
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.ui.dialogs.ProductCategoriesBottomSheetDialog
import com.glance.streamline.ui.dialogs.ProductDeletingDialog
import com.glance.streamline.ui.dialogs.ProductMessageDialog
import com.glance.streamline.ui.dialogs.ProductOptionsBottomSheetDialog
import com.glance.streamline.ui.dialogs.refuselog.RefuseMessageDialog
import com.glance.streamline.data.entities.UserModel
import com.glance.streamline.utils.custom_views.SpannedGridLayoutManager
import com.glance.streamline.utils.extensions.android.getUniqCurrentDeviceId
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.observe
import com.glance.streamline.utils.extensions.android.view.blink
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.pax.dal.exceptions.PrinterDevException
import com.pax.neptunelite.api.NeptuneLiteUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_logout_admin.*
import kotlinx.android.synthetic.main.dialog_product_quantity.*
import kotlinx.android.synthetic.main.drawer_header_layout.view.*
import kotlinx.android.synthetic.main.fragment_checkout_portrait.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder
import com.glance.streamline.data.entities.CategoryInfo
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.ui.adapters.recycler_view.*
import com.glance.streamline.ui.models.CashPaymentResult
import com.glance.streamline.utils.extensions.*
import kotlinx.android.synthetic.main.list_item_product.*

private const val ROWS_COUNT = 5
const val RESULT_PRODUCTS_KEY = "result_products"
const val CASH_ORDER_NUMBER = "cash_order_number"

class CheckoutPortraitFragment : BaseFragment<CheckoutFragmentViewModel>() {

    private var isRefusalLogShowing: Boolean = false

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): CheckoutFragmentViewModel {
        return injectViewModel(viewModelFactory)
    }

    override fun layout(): Int = R.layout.fragment_checkout_portrait

    private val mainActivityViewModel by lazy { baseActivity.injectViewModel(viewModelFactory) as MainActivityViewModel }
    private val paymentVM by lazy { baseActivity.injectViewModel(viewModelFactory) as PaymentViewModel }

    private val orderItemsListAdapter by lazy { order_items_list.adapter as? OrderItemsListAdapter }

    private var productLayoutListAdapter: CategoryLayoutsListAdapter? = null
    private var productMainListAdapter: ProductMainAdapter? = null

    private val productCategoriesDialog by lazy {
        ProductCategoriesBottomSheetDialog(baseActivity, this).apply {
//            onCategorySelected = viewModel::addSelectedProductCategory
        }
    }
    private var productMessageDialog: AlertDialog? = null
    private var productRemovingDialog: AlertDialog? = null
    private var productOptionsDialog: BottomSheetDialog? = null
    private var productsQuantityDialog: AlertDialog? = null
    private var orderProductsClearingDialog: AlertDialog? = null

    private var searchView: SearchView? = null
    private var clearOrderMenuItem: MenuItem? = null

    private var mLayoutList: ArrayList<CategoryInfo> = ArrayList()
    private var mSelectedLayoutId: String? = null
    private var mSearchQuery: String? = null


    private var timer: Timer = Timer()
    private var isTimerRunning = false
    private var currentLayoutLastUpdated: Date = Date()
    private var currentLayoutId: String = ""

    override fun initialization(view: View, isFirstInit: Boolean) {
        paymentVM.listenViewModelUpdates()
        setHasOptionsMenu(true)
        if (isFirstInit) {
            initClicks()
//            initProductCategoriesList()
            initOrderItemsList()
            updateChargeButton()
            initObservers()

            mainActivityViewModel.getLogoutTimeLeft()
            mainActivityViewModel.startCheckingLastUserLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        orderItemsListAdapter?.clear()
        viewModel.isRefusalLogWasShown = false

        viewModel.getLayoutsListFromDB()
        viewModel.getTablesList()

        isTimerRunning = false
        timer.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_checkout, menu)
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
        clearOrderMenuItem = menu.findItem(R.id.action_abandon_order).apply {
            setOnMenuItemClickListener {
                showOrderClearingDialog()
                true
            }
        }
        updateClearOrderButton(orderItemsListAdapter?.itemCount == 0)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initObservers() {

        mainActivityViewModel.logoutTimeLeft.observe(this) {
            mainActivityViewModel.startLogoutTimeoutTimer(it)
        }

        mainActivityViewModel.userLiveData.observe(this, ::onUserLoggedIn)

        //Search
        viewModel.searchButtonResultsLiveData.observe(this){
            val adapter = (food_lists_grid.adapter as? ProductsListAdapter) ?: ProductsListAdapter(
                it ?: arrayListOf(), this
            ){ _, product -> showOptionsPopUp(product)}

            if (it.isNotEmpty()) {
                loadProductsList(it)
                products_list_empty_text_view.gone()
            } else {
                adapter.clear()
                products_list_empty_text_view.visible()
            }
        }

        //Payments
        mainActivityViewModel.cardPaymentResult.observe(this, ::onCardPaymentResult)

        //Layouts with products
        viewModel.currentSelectedLayoutLiveData.observe(this, ::onSelectLayout)
        viewModel.categoriesLayoutListLiveData.observe(this, ::onCategoryLayoutsLoaded)

        viewModel.customProductMultiplierLiveData.observe(this, ::onCustomProductQuantityChanged)

        paymentVM.cashPaymentCompletedLiveData.observe(this, ::onCashPurchased)
    }

    private fun initClicks() {
        categories_menu_button.onClick {
            productCategoriesDialog.show()
        }

        val onQuickActionClick = { item: MenuItem ->
            (baseActivity as? NavigationView.OnNavigationItemSelectedListener)
                ?.onNavigationItemSelected(item)
            Unit
        }
        logout_button.onClick {
            showLogoutDialog()
        }
        quick_action_2_button.apply {
            clickListener = this@CheckoutPortraitFragment
            onClick = onQuickActionClick
        }
        product_quantity_button.onClick {
            showQuantityDialog(viewModel.customProductMultiplierLiveData.value)
        }
        card_payment_button_card.onClick {
            orderItemsListAdapter?.let {
                val mainActivity = baseActivity as MainActivity
                mainActivity.stopJob()

                saveProducts(it.getTotalPrice(), 0f)
                paymentVM.saleWithCard(it.getTotalPrice())
            }
        }
        card_payment_button_cash.onClick {
//            openTillDrawer("1")
            val productQTV = hashMapOf<String, Int>()

            val paymentProducts = hashMapOf<String, PaymentRequest.Product>()
            val options = arrayListOf<OrderItemsListAdapter.BasketOption>()

            var totalPrice = 0.0f
            orderItemsListAdapter?.let { orderItemsListAdapter1 ->
                totalPrice = orderItemsListAdapter1.getTotalPrice()

                viewModel.isRefusalLogWasShown = false
                orderItemsListAdapter1.getItemsList().forEach {
                    when (it) {
                        is OrderItemsListAdapter.BasketProduct -> {
                            paymentProducts[it.productId] =
                                PaymentRequest.Product(productId = it.productId)
                            productQTV[it.productId] = it.itemsCount
                        }
                        is OrderItemsListAdapter.BasketOption -> {
                            options.add(it)
                        }
                    }
                }
            }

            val resultProducts = arrayListOf<PaymentRequest.Product>()
            paymentProducts.values.toList().map{
                for (prod in 1..(productQTV[it.productId] ?: 1)){
                    resultProducts.add(PaymentRequest.Product(productId = it.productId))
                }
            }

            options.forEach {
                for (opt in 1..it.itemsCount){
                    val test = resultProducts.filter { product -> it.parentProductId == product.productId }
                    for (product in test) {
                        if (!product.optionsId?.contains(it.optionId)) {
                            product.optionsId?.add(it.optionId)
                            break
                        }
                    }
                }
            }

            val sharedPreferences = baseActivity.getSharedPreferences(baseActivity.packageName, Context.MODE_PRIVATE)
            val cashOrderNumber = sharedPreferences.getInt(CASH_ORDER_NUMBER, 0)
            val orderNumber = "cash-${cashOrderNumber}"

            val request = PaymentRequest(
                paymentDate = Calendar.getInstance().time.toISO_8601_Timezone(),
                lastDigits = "",
                refusalLog = PaymentRequest.RefusalLogRequest(viewModel.lastRefusalLogResult.value),
                deviceToken = requireContext().getUniqCurrentDeviceId(),
                products = resultProducts,
//                orderNumber = orderNumber,
                cardAmount = 0f,
                cashAmount = totalPrice
            )

            findNavController().navigate(
                CheckoutPortraitFragmentDirections.actionToTenderOptionsDialogFragment(totalPrice, request)
            )
        }
    }

    private fun saveProducts(cardAmount: Float = 0f, cashAmount: Float = 0f) {
        val productQTV = hashMapOf<String, Int>()

        val paymentProducts = hashMapOf<String, PaymentRequest.Product>()
        val options = arrayListOf<OrderItemsListAdapter.BasketOption>()

        orderItemsListAdapter?.let { orderItemsListAdapter1 ->
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
        }

        val resultProducts = arrayListOf<PaymentRequest.Product>()
        paymentProducts.values.toList().map{
            for (prod in 1..(productQTV[it.productId] ?: 1)){
                resultProducts.add(PaymentRequest.Product(productId = it.productId))
            }
        }

        options.forEach {
            for (opt in 1..it.itemsCount){
                val test = resultProducts.filter { product -> it.parentProductId == product.productId }
                for (product in test) {
                    if (!product.optionsId?.contains(it.optionId)) {
                        product.optionsId?.add(it.optionId)
                        break
                    }
                }
            }
        }

        val paymentRequest = PaymentRequest(
            paymentDate = Calendar.getInstance().time.toISO_8601_Timezone(),
            refusalLog = PaymentRequest.RefusalLogRequest(viewModel.lastRefusalLogResult.value),
            deviceToken = requireContext().getUniqCurrentDeviceId(),
            products = resultProducts,
            cardAmount = cardAmount,
            cashAmount = cashAmount
        )

        val sharedPreferences = baseActivity.getSharedPreferences(baseActivity.packageName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(RESULT_PRODUCTS_KEY, paymentRequest.toJson())
        editor.apply()
        editor.commit()
    }

    private fun showLogoutDialog() {
        val mainActivity = baseActivity as MainActivity
        mainActivity.showLogoutDialog()
    }

    private fun onCashPurchased(cashPaymentResult: CashPaymentResult) {
        if (cashPaymentResult != null) {
            var cashCount = 0
            cashPaymentResult.cashPaymentRequest.products.map {
                cashCount += 1
            }

            viewModel.saveCashReports(cashPaymentResult.price, cashCount)

            val sharedPreferences = baseActivity.getSharedPreferences(baseActivity.packageName, Context.MODE_PRIVATE)
            val cashOrderNumber = sharedPreferences.getInt(CASH_ORDER_NUMBER, 0)
            val editor = sharedPreferences.edit()
            editor.putInt(CASH_ORDER_NUMBER, cashOrderNumber + 1)
            editor.apply()
            editor.commit()

            showReceiptConfirmDialog(cashPaymentResult.cashPaymentRequest)
//            mainActivityViewModel.logout(true)
        }
    }

    private fun showReceiptConfirmDialog(paymentRequest: PaymentRequest) {
        AlertDialog.Builder(baseActivity)
            .setTitle(R.string.receipt_confirm_dialog_title)
            .setMessage(R.string.receipt_confirm_dialog_message)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                // print here
                printReceipt(paymentRequest)
                showSuccessSnack("Done purchase")
                clearOrderProducts()
            }
            .setNegativeButton(R.string.no) { _, _ ->
                showSuccessSnack("Done purchase")
                clearOrderProducts()
            }
            .setCancelable(false)
            .create().apply {
                show()
            }
    }

    private fun printReceipt(paymentRequest: PaymentRequest) {

        val date = Date().parseIso8601WithTimezone(paymentRequest.paymentDate, Locale.ENGLISH)
        val strDate = date?.toDateString()
        val strTime = date?.toTimeString()

        val userModel = mainActivityViewModel.userLiveData.value ?: return
        val userName = userModel.user_name
        val hubName = userModel.devices.joinToString { it.name }
        val location = userModel.hub

        val receipt = ReceiptBuilder(1200)
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
            .addText(strDate, false)
            .setAlign(Paint.Align.RIGHT)
            .addText(strTime, false)
            .addParagraph()
            .addBlankSpace(5)
            .addLine()
            .addParagraph()
            .setTypeface(baseContext, "rubik_medium.ttf")
            .setAlign(Paint.Align.CENTER)
            .addText("Sale")
            .addParagraph()
            .setTypeface(baseContext, "rubik_regular.ttf")

        orderItemsListAdapter?.let { orderItemsListAdapter1 ->
            orderItemsListAdapter1.getItemsList().forEach {
                when (it) {
                    is OrderItemsListAdapter.BasketProduct -> {
                        val itemCount = it.itemsCount
                        if (itemCount > 1) {
                            receipt.setAlign(Paint.Align.LEFT)
                                .addText("$itemCount" + "x " + it.name, false)
                                .setAlign(Paint.Align.RIGHT)
                                .addText("$itemCount"+"x £" + it.price)
                                .addBlankSpace(5)
                        } else {
                            receipt.setAlign(Paint.Align.LEFT)
                                .addText("$itemCount" + "x " + it.name, false)
                                .setAlign(Paint.Align.RIGHT)
                                .addText("£" + it.price)
                                .addBlankSpace(5)
                        }
                    }
                }
            }

            val totalPrice = orderItemsListAdapter1.getTotalPrice()

            receipt.setAlign(Paint.Align.LEFT)
                .addBlankSpace(30)
                .addParagraph()
                .addLine()
                .addParagraph()
                .setTypeface(baseContext, "rubik_medium.ttf")
                .setTextSize(50.0f)
                .setAlign(Paint.Align.RIGHT)
                .addText("Refund            £0.0")
                .addBlankSpace(5)
                .addText("Total            £$totalPrice")
                .addParagraph()
        }

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

        val bitmap = receipt.build()

//        imgTest.setImageBitmap(bitmap)
//        imgTest.visible()
//        imgTest.setOnClickListener {
//            imgTest.gone()
//        }
//        return

        try {
            val paxIdal = NeptuneLiteUser.getInstance().getDal(context)
            val prn = paxIdal.printer
            prn.init()
            prn.printBitmap(bitmap)
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


            // Paper cutter.
//            val cutMode = prn.cutMode
//            if ((cutMode == 0) || (cutMode ==2)) {
//                // 0=full, or 2=partial/full => full cut.
//                prn.cutPaper(0)
//            } else if (cutMode == 1) {
//                // 1=partial only => partial cut.
//                prn.cutPaper(1)
//            }

        } catch (ex: PrinterDevException) {
            showErrorSnack("Receipt Print Failed")
            saveLog(ex.toString())
            ex.printStackTrace()
        } catch (e: Exception) {
            showErrorSnack("Receipt Print Failed")
            saveLog(e.toString())
            e.printStackTrace()
        }
    }

    private fun onUserLoggedIn(user: UserModel) {
        user_name_text_view.text = user.user_name
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
                        viewModel.searchProducts(query.trim())
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isBlank()) {
                        searchView?.clearFocus()
                        viewModel.currentSelectedLayoutLiveData.value?.let { onSelectLayout(it) }
                        mSearchQuery = null
                        viewModel.onSearchClosed()
                        viewModel.searchProducts("")
                    } else {
                        mSearchQuery = newText.trim()
                        viewModel.searchProducts(mSearchQuery!!)
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
        viewModel.currentSelectedLayoutLiveData.value?.let { onSelectLayout(it) }
        mSearchQuery = null
        viewModel.onSearchClosed()
    }
    /*Search*/


    /*Orders*/
//    private fun initProductCategoriesDialog(layouts: ArrayList<ProductLayout>) {
//        productCategoriesDialog.init(layouts)
//        // viewModel.setSelectedProductCategories(layouts) TODO:
//    }

//    private fun initProductCategoriesList() {
//        productCategoriesListAdapter = ProductMainAdapter(
//            arrayListOf(), this,
//            ProductMainAdapter.AdapterType.HALF_SIZE
//        ) {
//            //loadProductsList(it.products ?: arrayListOf())TODO:
//        }
//        food_categories_list.adapter = productCategoriesListAdapter
//    }

//    private fun onProductCategoryAdded(layouts: ArrayList<ProductLayout>) {
//        if (layouts.isEmpty()) (food_lists_grid.adapter as? ProductsListAdapter)?.clear()
//        setProductCategories(layouts)
//    }
//
//    private fun setProductCategories(layouts: ArrayList<ProductLayout>) {
//        productCategoriesListAdapter?.apply {
//            //submitList(layouts.toList())TODO:
//            //selectedCategoryId = layouts.getOrNull(0)?.id
//        }
////        if (layouts.isNotEmpty()) {
////            loadProductsList(layouts[0].products?: arrayListOf())
////        } else products_list_empty_text_view.visible()
//    }

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

        food_lists_grid.layoutManager =
            SpannedGridLayoutManager(
                object :
                    SpannedGridLayoutManager.GridSpanLookup {
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
        food_lists_grid.adapter =
            ProductsListAdapter(productList, this) { _, product -> showOptionsPopUp(product) }
    }

    private fun initOrderItemsList() {
        order_items_list.setOnTouchListener { _, _ ->
            baseActivity.toggleKeyboard(false)
            baseActivity.currentFocus?.let { if (it is EditText) it.clearFocus() }
            false
        }

        order_items_list.adapter = OrderItemsListAdapter(
            this,
            onMessage = ::showOrderProductMessageDialog,
            onRemove = ::onOrderItemRemoving,
            onItemsChanged = ::onOrdersAdapterChanged
        )
        onOrdersAdapterChanged(0)
    }

    private fun onOrdersAdapterChanged(itemCount: Int) {
        if (itemCount == 0) orders_list_empty_text_view.visible()
        else orders_list_empty_text_view.gone()
        updateClearOrderButton(itemCount == 0)
        updateChargeButton()
    }

    private fun onCustomProductQuantityChanged(quantity: Int) {
        product_quantity_button.blink(quantity > 0)
    }

    private fun onOrderItemRemoving(orderItem: OrderItemsListAdapter.BasketItem) {
        if (orderItem.itemsCount > 1)
            showRemovingOrderItemsDialog(orderItem.itemsCount - 1) {
                orderItemsListAdapter?.onRemovingSpecificNumber(orderItem, it)
            }
        else orderItemsListAdapter?.onRemoveButtonClicked(orderItem)
    }


    private fun addToOrderItemsList(productModel: OrderItemsListAdapter.BasketItem) {
        orderItemsListAdapter?.let { adapter ->
            val copiedOrderItem = when(productModel){
                is OrderItemsListAdapter.BasketProduct -> productModel.copy()
                is OrderItemsListAdapter.BasketOption -> productModel.copy()
                else -> throw IllegalArgumentException("Unexpected type")
            }
            adapter.addOrderItem(
                copiedOrderItem,
                viewModel.customProductMultiplierLiveData.value ?: 0
            )
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

    private fun showOrderClearingDialog() {
        if (orderItemsListAdapter?.itemCount != 0)
            orderProductsClearingDialog = AlertDialog.Builder(baseActivity)
                .setTitle(R.string.order_clearing_dialog_title)
                .setMessage(R.string.order_clearing_dialog_message)
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    clearOrderProducts()
                }
                .setNegativeButton(R.string.logout_dialog_cancel) { _, _ -> }
                .setCancelable(false)
                .create().apply {
                    show()
                }
    }

    private fun clearOrderProducts() {
        saveLog("clear order products")
        orderItemsListAdapter?.clear()
        paymentVM.clears()
        viewModel.isRefusalLogWasShown = false
        isRefusalLogShowing = false
    }

    private fun updateClearOrderButton(isOrderItemsListEmpty: Boolean) {
        clearOrderMenuItem?.apply {
            icon.alpha = if (isOrderItemsListEmpty) 128 else 255
            isEnabled = !isOrderItemsListEmpty
        }
    }
    /*Orders*/

    /*Payment*/
    private fun onCardPaymentResult(paymentResult: PaymentResultModel) {

        saveLog("onCardPaymentResult")

        val sharedPreferences = baseActivity.getSharedPreferences(baseActivity.packageName, Context.MODE_PRIVATE)
        val savedRequestString = sharedPreferences.getString(RESULT_PRODUCTS_KEY, "")
        if (savedRequestString.equals("")) {
            clearOrderProducts()
            viewModel.lastRefusalLogResult.value = null
            return
        }

        val savedRequest = fromJson<PaymentRequest>(savedRequestString)?: PaymentRequest()

        val request = PaymentRequest(
            paymentDate = Calendar.getInstance().time.toISO_8601_Timezone(),
            lastDigits = if (paymentResult.cardNumber.length == 4) paymentResult.cardNumber
                else paymentResult.cardNumber.takeLast(4).orEmpty(),
            refusalLog = PaymentRequest.RefusalLogRequest(viewModel.lastRefusalLogResult.value),
            deviceToken = requireContext().getUniqCurrentDeviceId(),
            products = savedRequest.products,
//            orderNumber = paymentResult.orderNumber,
            cardAmount = savedRequest.cardAmount,
            cashAmount = savedRequest.cashAmount
        )

        when(paymentResult.responseType){
            PaymentResponseType.APPROVED -> {
                openTillDrawer()
                showSuccessSnack("Approved successfully!")
                saveLog("Approved successfully!")

                paymentVM.uploadReport(request) {
                    saveLog("call purchase and clear list")
                    viewModel.saveCardReports(paymentResult.amount, request.products.count())
                    clearOrderProducts()
                    clearUploadedReport()
                    showSuccessSnack("Done purchase")
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

    private fun clearUploadedReport() {
        val sharedPreferences = baseActivity.getSharedPreferences(baseActivity.packageName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(RESULT_PRODUCTS_KEY, "")
        editor.apply()
    }

    /***
     * open/close till drawer
     * data: 1 is for opening drawer and 0 is for closing
     */
    private fun openTillDrawer(data: String) {
        val file = File("/sys/class/gpio/gpio14/value")
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            val bytes = data.toByteArray()
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openTillDrawer() {
        val handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when(msg.what) {
                    OPEN -> {
                        Log.d("==========", "In Handler's shutdown")
                    }
                }
            }
        }
        OpenTillDrawerAsyncTask(handler, "127.0.0.1", 4000, "27112025250").execute()
    }

    /*Payment*/

    /*Layout*/
    private fun onSelectLayout(buttons: List<ProductButtonInfo>) {
        if (buttons.isNullOrEmpty()) {
                (food_lists_grid.adapter as? ProductsListAdapter)?.clear()
        } else {
            val firstButtonInfo = buttons[0]
            currentLayoutId = firstButtonInfo.layoutId
            currentLayoutLastUpdated = firstButtonInfo.lastUpdated
        }
        setProductLayout(buttons!!)

        if (!isTimerRunning) refresh()

        mSearchQuery?.let{
            viewModel.searchProducts(it)
        }
    }

    private fun setProductLayout(buttons: List<ProductButtonInfo>) {
        productMainListAdapter?.apply {
            submitList(buttons)
            selectedCategoryId = buttons.getOrNull(0)?.id
        }
        if (buttons.isNotEmpty()) {
            loadProductsList(buttons)
            products_list_empty_text_view.gone()
        } else products_list_empty_text_view.visible()
    }

    private fun onCategoryLayoutsLoaded(layoutList: List<CategoryInfo>) {
        mLayoutList.clear()
        mLayoutList.addAll(layoutList)

        productLayoutListAdapter = CategoryLayoutsListAdapter(
            mLayoutList, this,
            CategoryLayoutsListAdapter.AdapterType.FULL_SIZE
        ) {
            mSelectedLayoutId = it.id
            viewModel.getLayoutWithProductsFromDB(mSelectedLayoutId!!)
            showToastListener.showToast(it.name)
        }
        food_categories_list.adapter = productLayoutListAdapter

        getLayoutWithProducts()
    }

    private fun getLayoutWithProducts() {
        if (mLayoutList.isNotEmpty()){
            mSelectedLayoutId = mSelectedLayoutId ?: mLayoutList[0].id
            if (mSelectedLayoutId != null) {
                viewModel.getLayoutWithProductsFromDB(mSelectedLayoutId!!)
            }
        }
    }

    private fun refresh() {
        isTimerRunning = true
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                viewModel.checkLastUpdated()
            }
        }, 10 * 1000L,  5 * 60 * 1000L)
    }

    private fun updateChargeButton() {
        orderItemsListAdapter?.let {
            card_payment_button_card.isEnabled = it.itemCount > 0
            card_payment_button_cash.isEnabled = it.itemCount > 0
            val price = it.getTotalPrice()
            total_sum_text_view.text = getString(R.string.total_sum_text, price)
        }
    }

    private fun showOptionsPopUp(product: ProductButtonInfo) {
        val basketitem = OrderItemsListAdapter.BasketProduct(product)
        addToOrderItemsList(basketitem)
        productOptionsDialog = ProductOptionsBottomSheetDialog(baseActivity, this).apply {
            onReopen = ::showOptionsPopUp
            onAddToOrderItemsList = ::addToOrderItemsList
            show(product)
        }
        //checkIfListContainsProductsForRefusalLog(basketitem)
    }
    /*Layout*/


    /*refusal log*/
    private fun checkIfListContainsProductsForRefusalLog(product: OrderItemsListAdapter.BasketItem) {
        if (viewModel.isRefusalLogWasShown.not()) {
            if (viewModel.listOfProductsNameForRefusalLog.any { it == product.productType }) {
                showRefusalLog(product)
            } else if (product is OrderItemsListAdapter.BasketProduct) {
                if (product.isRestricted) {
                    showRefusalLog(product)
                }
            }
        }
    }

    private fun showRefusalLog(product: OrderItemsListAdapter.BasketItem) {
        if (isRefusalLogShowing.not()){
            isRefusalLogShowing = true
            RefuseMessageDialog(requireContext(), onCompleteSale = {
//                viewModel.isRefusalLogWasShown = true
            }, onRefuse = {
                viewModel.lastRefusalLogResult.value = it
                orderItemsListAdapter?.onRemoveButtonClicked(product)
                isRefusalLogShowing = false
            }).showDialogs()
        }
    }
    /*refusal log*/


    override fun onDestroyView() {
        super.onDestroyView()
        productCategoriesDialog.dismiss()
        productRemovingDialog?.dismiss()
        productOptionsDialog?.dismiss()
        productMessageDialog?.dismiss()
        productsQuantityDialog?.dismiss()
        orderProductsClearingDialog?.dismiss()
        timer.cancel()
        isTimerRunning = false
        viewModel.clearAllData()
    }

    private fun saveLog(message: String) {
        Log.d("==CheckoutPortrait==", message)
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
//            Log.d("CheckoutPortrait", "File write failed: $e")
//        }
    }
}