package com.glance.streamline.domain.repository.bussines

import android.content.Context
import com.glance.streamline.domain.repository.BaseRepository
import com.glance.streamline.utils.extensions.android.Result
import javax.inject.Inject

class BussinesApiRepository  @Inject constructor(
    context: Context,
    private val bussinesApiInterface: BussinesApiInterface
) : BaseRepository (context) {


    fun getBussinessById(bussines: String, onResponse: (Result<BussinesResponse>) -> Unit)
            = bussinesApiInterface.getBussinessById(bussines).getSchedulers().getWrapped<BussinesResponse, BussinesResponse>(onResponse)
}