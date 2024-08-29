package com.glance.streamline.domain.repository.bussines

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface BussinesApiInterface {

    @GET("/business/")
    fun getBussinessById(@Query("bussinesId") bussinesId: String): Single<BussinesResponse>
}

data class BussinesResponse(
    val name: String
)