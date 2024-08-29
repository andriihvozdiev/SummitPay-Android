package com.glance.streamline.domain.repository.products

import com.glance.streamline.domain.model.LastUpdateResponse
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.domain.model.ProductLayoutsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductsApiInterface {

    @GET("/layout/business/{id}")
    fun getLayouts(@Path("id") id: String): Single<ProductLayoutsResponse>

    @GET("/layout/hub/{id}")
    fun getHubLayouts(@Path("id") id: String): Single<ProductLayoutsResponse>

    @GET("/layout/hub/products/{id}")
    fun getHubAllLayouts(@Path("id") id: String): Single<ProductLayoutsResponse>

    @GET("/layout/button/{layoutId}")
    fun getButtons(@Path("layoutId") layoutId: String): Single<List<ProductLayout.ProductButton>>

    @GET("/layout/products/{layoutId}")
    fun getLayoutWithProducts(@Path("layoutId") layoutId: String): Single<ProductLayout>

    @GET("/last_update")
    fun getLastUpdate(): Single<LastUpdateResponse>
}