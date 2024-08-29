package com.glance.streamline.ui.activities.main

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.core.view.drawToBitmap
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder
import com.glance.streamline.MainNavigationGraphDirections
import com.glance.streamline.R
import com.glance.streamline.data.entities.UserLogoutTimeout
import com.glance.streamline.data.entities.UserModel
import com.glance.streamline.data.entities.ZReportInfo
import com.glance.streamline.domain.model.payment.PaymentResponseType
import com.glance.streamline.domain.model.payment.PaymentResultModel
import com.glance.streamline.receiver.TransactionResultReceiver
import com.glance.streamline.services.SyncJobSchedulerService
import com.glance.streamline.ui.base.BaseActivity
import com.glance.streamline.ui.fragments.splash.DeviceAssignScreenState
import com.glance.streamline.ui.fragments.splash.SignInScreenState
import com.glance.streamline.ui.fragments.splash.SplashScreenState
import com.glance.streamline.ui.models.PaymentResultType
import com.glance.streamline.utils.extensions.*
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.extensions.android.injectViewModel
import com.glance.streamline.utils.extensions.android.isTabletDevice
import com.glance.streamline.utils.extensions.android.observe
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.visible
import com.google.android.material.navigation.NavigationView
import com.pax.dal.exceptions.PrinterDevException
import com.pax.neptunelite.api.NeptuneLiteUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_logout_admin.*
import kotlinx.android.synthetic.main.dialog_timeout.view.*
import kotlinx.android.synthetic.main.drawer_header_layout.view.*
import kotlinx.android.synthetic.main.layout_x_report_template.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : BaseActivity<MainActivityViewModel>(),
    NavigationView.OnNavigationItemSelectedListener {

    private final val JOB_ID = 100

    init {
        System.loadLibrary("DeviceConfig")
    }

    override fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): MainActivityViewModel {
        return injectViewModel(viewModelFactory)
    }

    override fun provideNavigationContainerId(): Int = R.id.nav_host_container

    override fun layout(): Int = R.layout.activity_main

    private val startStackFragmentIds = setOf(
        R.id.splashFragment,
        R.id.debugFragment,
        R.id.pinCodeFragment,
        R.id.credentialsFragment,
        R.id.checkoutLandscapeFragment,
        R.id.checkoutPortraitFragment,
        R.id.refundFragment,
        R.id.closedOrdersFragment,
        R.id.settingsFragment
    )
    private val excludeToolbarFragmentIds = setOf(
        R.id.splashFragment,
        R.id.pinCodeFragment,
        R.id.credentialsFragment,
        R.id.assignCompanyNumberFragment,
        R.id.assignDeviceInfoFragment
    )
    private val navController by lazy { findNavController(provideNavigationContainerId()) }
    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(startStackFragmentIds, drawer_layout)
    }

    private var timeoutDialog: AlertDialog? = null
    private var logoutDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setDefaultOrientation()
        listenDeviceOrientationChanges()
        super.onCreate(savedInstanceState)

        val paymentResponseTypeString: String? = intent.getStringExtra("paymentResponseType")

        if (paymentResponseTypeString != null) {
            saveLog("MainActivity: $paymentResponseTypeString")
            val paymentResponseType = when(paymentResponseTypeString) {
                "approved" -> PaymentResponseType.APPROVED
                "declined" -> PaymentResponseType.DECLINED
                else -> PaymentResponseType.ERROR
            }

            val cardNumber = intent.getStringExtra("cardNumber") ?: ""
            val cardType = intent.getStringExtra("cardType") ?: ""
            val authCode = intent.getStringExtra("authCode") ?: ""
            val amount = intent.getLongExtra("amount", 0L) / 100.0f
            val receiptNumber = intent.getIntExtra("receiptNumber", 0)
            val orderNumber = "card-${receiptNumber}"

            val paymentResult = PaymentResultModel(
                paymentResponseType,
                cardNumber,
                cardType,
                authCode,
                amount,
                orderNumber
            )
            viewModel.cardPaymentResult.postValue(paymentResult)
        }

//        startJob()
        viewModel.getLoginRecords()
    }

    override fun onResume() {
        super.onResume()

        startJob()
    }

    private fun startJob() {
        var component = ComponentName(this, SyncJobSchedulerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isJobServiceOn()) {
            var jobInfo = JobInfo.Builder(JOB_ID, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(15 * 60 * 1000L)
                .build()

            var jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancelAll()
            jobScheduler.schedule(jobInfo)
        }
    }

    fun isJobServiceOn(): Boolean {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        var hasBeenScheduled = false
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == JOB_ID) {
                hasBeenScheduled = true
                break
            }
        }
        return hasBeenScheduled
    }

    fun stopJob() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
        } else {

        }
    }

    private fun saveLog(message: String) {
        Log.d("==MainActivity==", message)
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
//            Log.d("MainActivity", "File write failed: $e")
//        }
    }

    override fun initialization() {
        setupDrawer()
        initNavigationListeners()
        observeViewModel()
        registerReceiver(TransactionResultReceiver(), IntentFilter("eft.com.TRANSACTION_RESULT"))
    }

    private fun initNavigationListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            checkMenuItem(destination.id)
            if (excludeToolbarFragmentIds.contains(destination.id)) {
                drawer_layout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                main_toolbar.gone()
            } else {
                drawer_layout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                main_toolbar.visible()
            }
        }

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_transactions -> {
                openTransactions()
                return true
            }
            R.id.menu_refund -> openRefund()
            R.id.menu_closed_orders -> {
                openTransactions()
                openClosedOrders()
            }
            R.id.menu_reports -> {
                openReports()
                return true
            }
            R.id.menu_x_report -> {
                viewModel.printXReport()
//                EFTServiceLib.executeXAndZReports(this, EFTServiceEvent.EventType.X_REPORT, true, true)
            }
            R.id.menu_z_report -> {
                viewModel.printZReport()
//                EFTServiceLib.executeXAndZReports(this, EFTServiceEvent.EventType.Z_REPORT, true, true)
            }
            R.id.menu_home -> {
                moveTaskToBack(true)
            }
            R.id.menu_logout -> showLogoutDialog()
            R.id.menu_settings -> {
                openSettings()
                return true
            }
            R.id.menu_timeout -> viewModel.getLogoutTimeoutData()
        }

        val destination = when (item.itemId) {
            R.id.menu_checkout -> {
                if (baseContext.isTabletDevice())
                    R.id.checkoutLandscapeFragment
                else
                    R.id.checkoutPortraitFragment
            }
            else -> item.itemId
        }

        if (destination != navController.currentDestination?.id ?: 0) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(navController.currentDestination?.id ?: 0, true)
                .build()
            try {
                navController.navigate(destination, null, navOptions)
            } catch (e: IllegalArgumentException) {
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

    private fun openTransactions() {
        Handler().post(Runnable {
            val isVisible = nav_view.menu.findItem(R.id.menu_closed_orders).isVisible
            closeAllGroupMenus()

            nav_view.menu.setGroupVisible(R.id.group_menu_transactions, !isVisible)
            if (viewModel.isAdmin()) {
                nav_view.menu.findItem(R.id.menu_refund).isVisible = false
            } else {
                nav_view.menu.findItem(R.id.menu_refund).isVisible = false
            }
        })
    }


    private fun openRefund() {
        navController.navigate(MainNavigationGraphDirections.actionGlobalRefundFragment())
    }

    private fun openClosedOrders() {
        val destination = R.id.closedOrdersFragment
        if (destination != navController.currentDestination?.id ?: 0) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(navController.currentDestination?.id ?: 0, true)
                .build()
            try {
                navController.navigate(destination, null, navOptions)
            } catch (e: IllegalArgumentException) { }
        }
    }

    private fun openReports() {
        Handler().post(Runnable {
            val isVisible = nav_view.menu.findItem(R.id.menu_x_report).isVisible
            closeAllGroupMenus()

            if (viewModel.isAdmin()) {
                nav_view.menu.setGroupVisible(R.id.group_menu_reports, !isVisible)
            } else {
                nav_view.menu.setGroupVisible(R.id.group_menu_reports, false)
            }
        })
    }

    private fun openSettings() {
//        navController.navigate(MainNavigationGraphDirections.actionGlobalSettingsFragment())

        Handler().post(Runnable {
            val isVisible = nav_view.menu.findItem(R.id.menu_logout).isVisible
            closeAllGroupMenus()

            nav_view.menu.setGroupVisible(R.id.group_menu_settings, !isVisible)
            if (viewModel.isAdmin()) {
                nav_view.menu.findItem(R.id.menu_timeout).isVisible = !isVisible
                nav_view.menu.findItem(R.id.menu_home).isVisible = !isVisible
            } else {
                nav_view.menu.findItem(R.id.menu_timeout).isVisible = false
                nav_view.menu.findItem(R.id.menu_home).isVisible = false
            }
        })

    }

    private fun checkMenuItem(destinationId: Int) {
        when (destinationId) {
            R.id.checkoutLandscapeFragment,
            R.id.checkoutPortraitFragment -> {

                nav_view.menu.findItem(R.id.menu_checkout)?.isChecked = true

                nav_view.menu.findItem(R.id.menu_refund).isVisible = false
                nav_view.menu.findItem(R.id.menu_timeout).isVisible = viewModel.isAdmin()
                nav_view.menu.findItem(R.id.menu_home).isVisible = viewModel.isAdmin()
                closeAllGroupMenus()
            }
        }
    }

    private fun closeAllGroupMenus() {
        nav_view.menu.setGroupVisible(R.id.group_menu_transactions, false)
        nav_view.menu.setGroupVisible(R.id.group_menu_reports, false)
        nav_view.menu.setGroupVisible(R.id.group_menu_settings, false)
    }

    private fun observeViewModel() {
        viewModel.logoutLiveData.observe(this, ::onLogout)
        viewModel.userLiveData.observe(this, ::onUserLoggedIn)
        viewModel.logoutTimeLeft.observe(this, ::onLogoutTimerTick)
        viewModel.logoutTimerData.observe(this, ::showTimeoutDialog)
        viewModel.xReportResults.observe(this, {
            printXReceipt(it, false)
        })
        viewModel.zReportResults.observe(this, {
            printXReceipt(it, true)
        })
    }

    private fun listenDeviceOrientationChanges() {
        val orientationChangeListener =
            object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation >= 0)
                        when {
                            orientation <= 45 -> {
                                setDeviceOrientation(
                                    isReverse = false,
                                    isLandscape = false
                                )//ORIENTATION_PORTRAIT
                                Log.d("Orientation", "$orientation, PORTRAIT")
                            }
                            orientation <= 135 -> {
                                setDeviceOrientation(
                                    isReverse = true,
                                    isLandscape = true
                                )//ORIENTATION_REVERSE_LANDSCAPE
                                Log.d("Orientation", "$orientation, REVERSE_LANDSCAPE")
                            }
                            orientation <= 225 -> {
                                setDeviceOrientation(
                                    isReverse = true,
                                    isLandscape = false
                                )//ORIENTATION_REVERSE_PORTRAIT
                                Log.d("Orientation", "$orientation, REVERSE_PORTRAIT")
                            }
                            orientation <= 315 -> {
                                setDeviceOrientation(
                                    isReverse = false,
                                    isLandscape = true
                                ) //ORIENTATION_LANDSCAPE
                                Log.d("Orientation", "$orientation, LANDSCAPE")
                            }
                        }
                }
            }
        if (orientationChangeListener.canDetectOrientation())
            orientationChangeListener.enable()
    }

    private fun setDeviceOrientation(isReverse: Boolean, isLandscape: Boolean) {
        if (baseContext.isTabletDevice() && isLandscape) {
            requestedOrientation = if (!isReverse) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        } else if (!baseContext.isTabletDevice() && !isLandscape) {
            requestedOrientation = if (!isReverse) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setDefaultOrientation() {
        val currentOrientation = when (resources.configuration.orientation) {
            0 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            1 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            2 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            3 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
        if (baseContext.isTabletDevice()) {
            if (currentOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            if (currentOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun setupDrawer() {
        setSupportActionBar(main_toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        nav_view.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
//        setMenuItemColor(R.id.nav_logout, R.color.colorRed)
    }

    private fun setMenuItemColor(@IdRes itemId: Int, @ColorRes colorId: Int) {
        nav_view.menu.findItem(itemId)?.let { item ->
            val s = SpannableString(item.title)
            s.setSpan(ForegroundColorSpan(getColorRes(colorId)), 0, s.length, 0)
            item.title = s
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                x_report_view.drawToBitmap(Bitmap.Config.ARGB_8888)
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> true
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun closeAllDialogs() {
        timeoutDialog?.dismiss()
        logoutDialog?.dismiss()
    }

    private fun onLogout(screenState: SplashScreenState) {
        viewModel.logoutLiveData.value = null
        closeAllDialogs()
        when (screenState) {
            is SignInScreenState -> navController.navigate(R.id.action_to_pinCodeFragment)
            is DeviceAssignScreenState -> navController.navigate(R.id.action_to_assignCompanyNumberFragment)
        }
    }

    private fun onUserLoggedIn(user: UserModel) {
        nav_view.getHeaderView(0)?.apply {
            post {
                drawer_user_name_text_view.text = getString(R.string.drawer_user_name, user.user_name)
                drawer_hub_name_text_view.text = getString(R.string.drawer_hub_name, user.hub)
                drawer_devices_available_text_view.text =
                    getString(R.string.drawer_devices, user.devices.joinToString { it.name })
            }
        }
    }

    private fun onLogoutTimerTick(timeSeconds: Long) {
        nav_view.getHeaderView(0)?.apply {
            val hours = timeSeconds / 3600
            val minutes = (timeSeconds / 60) % 60
            val seconds = timeSeconds % 60
            val hoursString = if (hours != 0L) "$hours h " else ""
            val minutesString = if (minutes != 0L) "$minutes min " else ""
            val secondsString = "$seconds sec"
            val formattedDuration = "Logout timeout: $hoursString$minutesString$secondsString"
            drawer_logout_timeout_text_view.text = formattedDuration
        }
    }

    fun showLogoutDialog() {
        val isAdmin = viewModel.isAdmin()

        val logoutLayout = when(isAdmin) {
            true -> R.layout.dialog_logout_admin
            false -> R.layout.dialog_logout
        }

        logoutDialog = AlertDialog.Builder(this)
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_message)
            .setView(logoutLayout)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                var shouldReassignDevice = false
                if (isAdmin) {
                    shouldReassignDevice = (dialog as AlertDialog).reassign_device_checkbox.isChecked
                }
                viewModel.logout(shouldReassignDevice)
            }
            .setNegativeButton(R.string.logout_dialog_cancel) { _, _ -> }
            .create().apply {
                show()
            }
    }

    private fun showTimeoutDialog(timeoutModel: UserLogoutTimeout) {
        val timeoutDialogView = layoutInflater.inflate(R.layout.dialog_timeout, null)
        timeoutDialog = AlertDialog.Builder(this)
            .setView(timeoutDialogView)
            .setTitle(R.string.timeout_dialog_title)
            .setMessage(R.string.timeout_dialog_message)
            .setPositiveButton(R.string.confirm) { _, _ -> }
            .setNegativeButton(R.string.logout_dialog_cancel) { _, _ -> }
            .create().apply {
                timeoutDialogView.duration_picker?.let {
                    timeoutModel.timeoutSeconds.let { timeout ->
                        it.setMinutes((timeout / 60).toInt())
                        it.setSeconds((timeout).toInt() % 60)
                    }
                    it.onZeroSelected = { isZeroTime ->
                        if (isZeroTime) getButton(DialogInterface.BUTTON_POSITIVE)?.gone()
                        else getButton(DialogInterface.BUTTON_POSITIVE)?.visible()
                    }
                }
                show()
                getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    timeoutDialogView.duration_picker?.let {
                        if (it.getDurationSeconds() > 0) {
                            viewModel.setLogoutTimeout(it.getDurationSeconds())
                            dismiss()
                        }
                    }
                }
            }
    }

    private fun printXReceipt(xReportInfos: List<ZReportInfo>, isZReport: Boolean) {

        val strDate = Date().toOnlyDateString()
        val strTime = Date().toTimeString()
        var numberOfCard = 0
        var priceOfCard = 0.0f
        var numberOfCash = 0
        var priceOfCash = 0.0f
        var numberOfRefund = 0
        var priceOfRefund = 0.0f

        var itemSales = 0
        var itemRefunds = 0
        xReportInfos.forEach {
            if (it.paymentType == PaymentResultType.CARD_PAYMENT_TYPE) {
                numberOfCard += 1
                priceOfCard += it.price
                itemSales += it.itemCount
            }
            if (it.paymentType == PaymentResultType.CASH_PAYMENT_TYPE) {
                numberOfCash += 1
                priceOfCash += it.price
                itemSales += it.itemCount
            }
            if (it.paymentType == PaymentResultType.REFUND_PAYMENT_TYPE) {
                numberOfRefund += 1
                priceOfRefund += it.price
                itemRefunds += it.itemCount
            }
        }

        val strTitle = if (isZReport) {
            "Z REPORT"
        } else {
            "X REPORT"
        }

        val totalNumber = numberOfCard + numberOfCash - numberOfRefund
        val totalSales = priceOfCard + priceOfCash
        val totalPrice = totalSales - priceOfRefund

        txt_report_title.text = strTitle
        txt_report_terminal.text = "Terminal 1 Payments"
        txt_report_time.text = strTime
        txt_report_date.text = strDate

        txt_report_no_sales.text = "$totalNumber"
        txt_report_item_sales.text = "$itemSales"
        txt_report_item_refunds.text = "$itemRefunds"
        txt_report_sales.text = "£$totalSales"
        txt_report_refunds.text = "£$priceOfRefund"
        txt_report_total.text = "£$totalPrice"

        txt_report_card.text = String.format("($numberOfCard) £%.02f", priceOfCard)
        txt_report_cash.text = String.format("($numberOfCash) £%.02f", priceOfCash)

        txt_report_cash_paid_out.text = "£0.00"

        val bitmap = x_report_view.drawToBitmap(Bitmap.Config.ARGB_8888)
//        imgTestReceipt.setImageBitmap(bitmap)
//        imgTestReceipt.visible()
//        imgTestReceipt.setOnClickListener {
//            imgTestReceipt.gone()
//        }
//        return

        val paxIdal = NeptuneLiteUser.getInstance().getDal(baseContext)
        val prn = paxIdal.printer
        try {
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


//            // Paper cutter.
//            val cutMode = prn.cutMode
//            if ((cutMode == 0) || (cutMode ==2)) {
//                // 0=full, or 2=partial/full => full cut.
//                prn.cutPaper(0)
//            } else if (cutMode == 1) {
//                // 1=partial only => partial cut.
//                prn.cutPaper(1)
//            }

            if (isZReport) {
                viewModel.resetZReport()
            }

        } catch (ex: PrinterDevException) {
            showErrorSnack("Receipt Print Failed")
            saveLog(ex.toString())
            ex.printStackTrace()
        }
    }

    private fun loadBitmapFromView(v: View): Bitmap? {
        val specWidth: Int =
            View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY)
        v.measure(1200, ViewGroup.LayoutParams.WRAP_CONTENT)
        val questionWidth: Int = v.measuredWidth
        val b = Bitmap.createBitmap(questionWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawColor(Color.WHITE)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }

    override fun onDestroy() {
        stopJob()
        super.onDestroy()
    }
}