package com.glance.streamline.di.modules

import com.glance.streamline.domain.repository.auth.StreamlineApiInterface
import com.glance.streamline.domain.repository.bussines.BussinesApiInterface
import com.glance.streamline.domain.repository.payment.PaymentApiInterface
import com.glance.streamline.domain.repository.products.ProductsApiInterface
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [AppModule::class, RetrofitApiModule::class])
class RepositoryModule {

    @Provides
    @Singleton
    fun provideStreamlineApiInterface(retrofit: Retrofit): StreamlineApiInterface =
        retrofit.create(StreamlineApiInterface::class.java)

    @Provides
    @Singleton
    fun provideBussinesApiInterface(retrofit: Retrofit): BussinesApiInterface =
        retrofit.create(BussinesApiInterface::class.java)

    @Provides
    @Singleton
    fun provideProductsApiInterface(retrofit: Retrofit): ProductsApiInterface =
        retrofit.create(ProductsApiInterface::class.java)

    @Provides
    @Singleton
    fun providePaymentApiInterface(retrofit: Retrofit): PaymentApiInterface =
        retrofit.create(PaymentApiInterface::class.java)


}