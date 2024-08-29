package com.glance.streamline.di.modules.view_model.fragment

import androidx.lifecycle.ViewModel
import com.glance.streamline.di.map_key.ViewModelKey
import com.glance.streamline.di.scopes.ActivityScope
import com.glance.streamline.ui.fragments.main.checkout.CheckoutFragmentViewModel
import com.glance.streamline.ui.fragments.main.checkout.PaymentViewModel
import com.glance.streamline.ui.fragments.closed_orders.ClosedOrdersFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(CheckoutFragmentViewModel::class)
    abstract fun bindCheckoutFragmentViewModel(model: CheckoutFragmentViewModel): ViewModel

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(PaymentViewModel::class)
    abstract fun bindPaymentViewModel(model: PaymentViewModel): ViewModel

    @Binds
    @ActivityScope
    @IntoMap
    @ViewModelKey(ClosedOrdersFragmentViewModel::class)
    abstract fun bindClosedOrdersFragmentViewModel(model: ClosedOrdersFragmentViewModel): ViewModel
}
