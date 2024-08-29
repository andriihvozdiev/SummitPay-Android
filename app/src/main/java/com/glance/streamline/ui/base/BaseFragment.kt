package com.glance.streamline.ui.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.androidadvance.topsnackbar.TSnackbar
import com.glance.streamline.R
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.ui.fragments.connection_error.ConnectionErrorFragment
import com.glance.streamline.ui.fragments.connection_error.ConnectionErrorFragmentViewModel
import com.glance.streamline.ui.listeners.*
import com.glance.streamline.ui.models.TransitionAnimationStatus
import com.glance.streamline.utils.ActionDebounceFilter
import com.glance.streamline.utils.extensions.android.*
import com.glance.streamline.utils.extensions.fromJson
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_progress.*
import javax.inject.Inject


abstract class BaseFragment<V : BaseViewModel> : Fragment(),
    FragmentBackPressedListener, FilteredClickListener {

    protected var showToastListener = ShowToastListener.empty
    protected var showSnackListener = ShowSnackListener.empty
    protected var backListener = ActivityBackPressedListener.empty
    protected var setUpSnackBar = SetUpSnackBar.empty
    private var transitionProgressListener = FragmentTransitionProgressListener.empty
    protected var bottomNavigationHolderListener = BottomNavigationHolderListener.empty
    var fragmentViewCreatedListener = FragmentViewCreatedListener.empty
    var isPageVisible: (fragment: Fragment) -> Boolean = { false }

    protected lateinit var viewModel: V

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var baseContext: Context

    @Inject
    lateinit var baseActivity: BaseActivity<*>

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private val clicksFilter = ActionDebounceFilter()

    protected var rootView: View? = null

    protected val connectionErrorViewModel: ConnectionErrorFragmentViewModel by lazy {
        baseActivity.injectViewModel<ConnectionErrorFragmentViewModel>(viewModelFactory)
    }

    @LayoutRes
    protected abstract fun layout(): Int

    protected abstract fun initialization(view: View, isFirstInit: Boolean)
    abstract fun provideViewModel(viewModelFactory: ViewModelProvider.Factory): V

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>)
            context.activityComponent.inject(this as BaseFragment<BaseViewModel>)
        if (context is ShowToastListener)
            showToastListener = context
        if (context is ShowSnackListener)
            showSnackListener = context
        if (context is ActivityBackPressedListener)
            backListener = context
        if (context is SetUpSnackBar)
            setUpSnackBar = context
        if (context is FragmentTransitionProgressListener)
            transitionProgressListener = context
        if (context is BottomNavigationHolderListener)
            bottomNavigationHolderListener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = provideViewModel(viewModelFactory)
        lifecycle.addObserver(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container?.removeAllViews() //Fix for crash on backing up

        val view = if (layout() != 0)
            addProgressView(inflater.inflate(layout(), container, false))
        else
            super.onCreateView(inflater, container, savedInstanceState)
        return if (rootView == null) view else rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentViewCreatedListener.onFragmentViewCreated(view)
        initialization(view, rootView == null)
        rootView = view
        viewModel.listenViewModelUpdates()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun addProgressView(rootView: View): View {
        val rootLayout = FrameLayout(baseContext)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        rootLayout.layoutParams = layoutParams
        rootLayout.addView(rootView)

        baseActivity.layoutInflater.inflate(R.layout.dialog_progress, rootLayout)
        return rootLayout
    }

    protected fun showProgress(show: Boolean) {
        progress_layout?.visibility = if (show) View.VISIBLE else View.GONE
    }

    protected fun BaseViewModel.listenViewModelUpdates() {
        loadingStateData.observe(viewLifecycleOwner, ::listenLoadingStateUpdates)
    }

    private fun listenLoadingStateUpdates(result: Result<*>) {
        Log.d("BaseFragment", result.toString())
        when (result) {
            is Loading -> onLoadingStateLoading(result)
            is Success -> onLoadingStateSuccess(result)
            else -> onLoadingStateError(result)
        }
    }

    protected open fun onLoadingStateLoading(loadingState: Loading<*>) {
        showProgress(loadingState.isLoading)
    }

    protected open fun onLoadingStateSuccess(successState: Success<*>) {
//        if (successState.wasResent) closeConnectionErrorScreen()
        successState.successMessage?.let(::showSuccessSnack)
    }

    protected open fun onLoadingStateError(errorState: Result<*>) {
        when (errorState) {
            is Failure -> showErrorSnack(errorState.errorMessage)
            is ConnectionError -> openConnectionErrorScreen(errorState)
            is AuthError -> openAuthActivity()
        }
    }

    private fun openAuthActivity() {
        // AuthorizationActivity.start(baseActivity, true)
    }

    protected open fun openConnectionErrorScreen(request: ConnectionError<*>) {
        if (this !is ConnectionErrorFragment) {
            connectionErrorViewModel.putRequest(request)
            //  findNavController().navigate(R.id.open_connection_error_screen)
        }
    }

    protected fun closeConnectionErrorScreen() {
        connectionErrorViewModel.setClosed()
    }

    override fun onDestroy() {
        baseActivity.toggleKeyboard(false)
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onBackPressed() {
        backListener.onBackPressed(this)
    }

    override fun shouldExit() = true

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
                { onClick(view) }, {
                    setFilteredClickListener(view, durationMillis, onClick)
                    showErrorSnack(it.message ?: getString(R.string.error_click_operation))
                })
    }

    protected inline fun <reified T> getJsonArgument(key: String, defaultJson: String? = null): T? {
        return fromJson(
            if (defaultJson.isNullOrBlank()) arguments?.getString(key) ?: ""
            else defaultJson
        )
    }

    protected open fun attachSnackBarView(): View? = setUpSnackBar.attachSnackBarView()

    protected open fun showErrorSnack(message: String) {
        showSnackListener.showSnack(attachSnackBarView(), message, R.color.colorRed)
    }

    protected fun showSuccessSnack(message: String) {
        showSnackListener.showSnack(attachSnackBarView(), message, R.color.colorGreen)
    }

    protected fun showShortErrorSnack(message: String) {
        showSnackListener.showSnack(
            attachSnackBarView(),
            message,
            R.color.colorRed,
            length = TSnackbar.LENGTH_SHORT
        )
    }

    protected fun showShortSuccessSnack(message: String) {
        showSnackListener.showSnack(
            attachSnackBarView(),
            message,
            R.color.colorGreen,
            length = TSnackbar.LENGTH_SHORT
        )
    }

    protected fun Disposable.addToDispose() {
        compositeDisposable.add(this)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim != 0) {
            val anim = AnimationUtils.loadAnimation(activity, nextAnim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    baseActivity.onFragmentTransitionProgressChanged(TransitionAnimationStatus.STARTED)
                }

                override fun onAnimationRepeat(animation: Animation) {
                    baseActivity.onFragmentTransitionProgressChanged(TransitionAnimationStatus.REPEATED)
                }

                override fun onAnimationEnd(animation: Animation) {
                    baseActivity.onFragmentTransitionProgressChanged(TransitionAnimationStatus.ENDED)
                }
            })
            return anim
        } else baseActivity.onFragmentTransitionProgressChanged(TransitionAnimationStatus.ANIMATION_NULL)
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

}
