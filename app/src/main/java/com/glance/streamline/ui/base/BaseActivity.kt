package com.glance.streamline.ui.base

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.androidadvance.topsnackbar.TSnackbar
import com.glance.streamline.R
import com.glance.streamline.StreamlineApp
import com.glance.streamline.di.components.ActivityComponent
import com.glance.streamline.di.modules.ActivityModule
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.ui.listeners.*
import com.glance.streamline.ui.models.TransitionAnimationStatus
import com.glance.streamline.utils.ActionDebounceFilter
import com.glance.streamline.utils.extensions.android.findViewAt
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.extensions.android.hideKeyboard
import com.glance.streamline.utils.extensions.android.showKeyboard
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class BaseActivity<V : BaseViewModel> : AppCompatActivity(),
    ShowToastListener,
    ShowSnackListener,
    FilteredClickListener,
    ActivityBackPressedListener,
    FragmentTransitionProgressListener,
    SetUpSnackBar {

    protected lateinit var viewModel: V

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val activityComponent: ActivityComponent by lazy {
        StreamlineApp[this].appComponent.plus(
            ActivityModule(this as BaseActivity<BaseViewModel>)
        )
    }

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var toast: Toast

    private val clicksFilter = ActionDebounceFilter()

//    private var statusBarHeight = 0

    @LayoutRes
    abstract fun layout(): Int

    abstract fun initialization()
    abstract fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): V
    abstract fun provideNavigationContainerId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initFragmentComponent()

        viewModel = provideViewModel(viewModelFactory)

        if (layout() != 0) {
            setContentView(layout())
            initialization()
        }

//        window.decorView.findViewById<View>(android.R.code.content)?.getStatusBarHeight {
//            statusBarHeight = it
//        }

    }

    private fun initFragmentComponent() {
        activityComponent.inject(this as BaseActivity<BaseViewModel>)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (provideNavigationContainerId() > 0) {
            try {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(provideNavigationContainerId())
                navHostFragment?.childFragmentManager?.fragments?.get(0)?.let { fragment ->
                    if (fragment is FragmentBackPressedListener && fragment.shouldExit()) {
                        fragment.onBackPressed()
                    }
                }
            } catch (e: IllegalStateException) {
                super.onBackPressed()
            }
        } else super.onBackPressed()
    }

    override fun onBackPressed(fragment: Fragment) {
        performBackNavigation(fragment.findNavController())
    }

    private fun performBackNavigation(navController: NavController) {
        navController.apply {
            if (graph.startDestination == currentDestination?.id)
                super.onBackPressed()
            else {
                listenForPoppingBackAccessibility {
                    val successExit = popBackStack()
                    if (!successExit)
                        super.onBackPressed()
                }
            }
        }
    }

    override fun onFragmentTransitionProgressChanged(status: TransitionAnimationStatus) {
        viewModel.setTransitionAnimationStatus(status)
    }

    private fun listenForPoppingBackAccessibility(popBack: () -> Unit) {
//        viewModel.transitionAnimationStatus.value?.let {
//            if (it == TransitionAnimationStatus.ENDED || it == TransitionAnimationStatus.ANIMATION_NULL) {
//                popBack()
//            }
//        } ?: popBack()
        popBack()
    }

    fun toggleKeyboard(show: Boolean) {
        if (!show) hideKeyboard()
        else showKeyboard()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val touchedView =
            (window.decorView.rootView as? ViewGroup)?.findViewAt(ev.x.toInt(), ev.y.toInt())
        if (touchedView != null && touchedView !is EditText && touchedView.id != R.id.search_close_btn) {
            currentFocus?.apply {
                toggleKeyboard(false)
                if (this is EditText) {
                    this.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun View.onClick(
        durationMillis: Long = 400,
        onClick: (view: View) -> Unit = {}
    ) {
        setFilteredClickListener(this, durationMillis, onClick)?.addToDispose()
    }

    override fun setFilteredClickListener(
        view: View,
        durationMillis: Long,
        onClick: (view: View) -> Unit
    ): Disposable? {
        return RxView.clicks(view)
            .filter { clicksFilter.filterAction(durationMillis) }
            .subscribe(
                { onClick(view) },
                { showErrorSnack(it.message ?: getString(R.string.error_click_operation)) })
    }

    override fun showToast(message: String) {
        if (::toast.isInitialized && toast.view.windowVisibility == View.VISIBLE)
            toast.cancel()
        toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    protected fun showErrorSnack(message: String) {
        showSnack(attachSnackBarView(), message, R.color.colorRed)
    }

    protected fun showSuccessSnack(message: String) {
        showSnack(attachSnackBarView(), message, R.color.colorGreen)
    }

    protected fun showShortErrorSnack(message: String) {
        showSnack(attachSnackBarView(), message, R.color.colorRed, TSnackbar.LENGTH_SHORT)
    }

    protected fun showShortSuccessSnack(message: String) {
        showSnack(attachSnackBarView(), message, R.color.colorGreen, TSnackbar.LENGTH_SHORT)
    }

    override fun attachSnackBarView(): View? = window.decorView.findViewById(android.R.id.content)
    override fun setSnackBarMargin() = 0

    override fun showSnack(
        attachView: View?,
        message: String,
        bgColorResId: Int,
        textColorResId: Int,
        length: Int,
        width: Int?
    ) {
        attachView?.let { view ->
            Snackbar.make(view, message, length).apply {
                this.view.apply {
                    setBackgroundColor(context.getColorRes(bgColorResId))
                    width?.let { layoutParams.width = it }
                    findViewById<TextView>(R.id.snackbar_text)?.apply {
                        maxLines = 5
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                        } else {
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                        setTextColor(context.getColorRes(textColorResId))
                    }
                }
                show()
            }
        }
    }

    protected fun Disposable.addToDispose() {
        compositeDisposable.add(this)
    }
}
