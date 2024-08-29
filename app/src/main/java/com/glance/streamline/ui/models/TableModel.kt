package com.glance.streamline.ui.models

import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout

data class TableModel(
    val name: String,
    val orderItemsList: ArrayList<ProductButtonInfo>
)