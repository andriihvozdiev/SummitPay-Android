package com.glance.streamline.domain.repository.operator

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface OperatorApiInterface {

    @GET("/operator/")
    fun getOperatorById(@Query("operatorId") operatorId: String): Single<OperatorResponse>
}

data class OperatorResponse(
    val name: String
)