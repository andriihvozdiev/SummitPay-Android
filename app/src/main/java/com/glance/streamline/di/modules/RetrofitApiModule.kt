package com.glance.streamline.di.modules

import android.app.Application
import android.content.Context
import com.glance.streamline.BuildConfig
import com.glance.streamline.utils.extensions.android.getSharedPref
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class RetrofitApiModule {
    companion object {
        private const val CONNECTION_TIMEOUT_SEC = 30L
        const val MAIN_RETROFIT = "MAIN_RETROFIT"
        const val PAYMENT_RETROFIT = "PAYMENT_RETROFIT"
        const val JWT_TOKEN_KEY = "JWT_TOKEN_KEY"
    }

    @Provides
    @Singleton
    fun provideHttpCache(application: Application): Cache {
        val cacheSize = 10L * 1024 * 1024
        return Cache(application.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return gsonBuilder.create()
    }


    @Provides
    @Singleton
    fun provideOkhttpClient(context: Context, cache: Cache): OkHttpClient {
        val token: String

        val retrofit = OkHttpClient.Builder().apply {
            cache(cache)

            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            cookieJar(JavaNetCookieJar(cookieManager))

            connectTimeout(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS)
            readTimeout(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS)
            writeTimeout(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)

            addInterceptor { chain ->
                val request = chain.request()

                val change = request.newBuilder()
                    .header("Content-Type", "application/json")

                change.header("Authorization", "Bearer " +context.getSharedPref()?.getString(JWT_TOKEN_KEY, null))

                val response = chain.proceed(change.build())
                response
            }

            if (BuildConfig.DEBUG) {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(httpLoggingInterceptor)
            }
        }.build()

        return retrofit
    }

    private fun bodyToString(request: RequestBody?): String {
        try {
            val buffer = Buffer()
            if (request != null)
                request.writeTo(buffer)
            else
                return ""
            return buffer.readUtf8()
        } catch (e: IOException) {
            return "did not work"
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Provides
    @Singleton
    fun provideMainRetrofit(retrofitBuilder: Retrofit.Builder) =
        retrofitBuilder.baseUrl(BuildConfig.SERVER_URL).build()

    @Provides
    @Singleton
    @Named(PAYMENT_RETROFIT)
    fun providePaymentRetrofit(retrofitBuilder: Retrofit.Builder) =
        retrofitBuilder.baseUrl(BuildConfig.PAYMENT_URL).build()

}