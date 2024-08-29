package com.glance.streamline.domain.repository.operator

import android.content.Context
import com.glance.streamline.domain.repository.BaseRepository
import com.glance.streamline.utils.extensions.android.Result
import javax.inject.Inject

class OperatorApiRepository  @Inject constructor(
    context: Context,
    private val operatorApiInterface: OperatorApiInterface
) : BaseRepository (context) {


    fun getOperatorById(operatorId: String, onResponse: (Result<OperatorResponse>) -> Unit)
            = operatorApiInterface.getOperatorById(operatorId).getSchedulers().getWrapped(onResponse)

}