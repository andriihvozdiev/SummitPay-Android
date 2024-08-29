package com.glance.streamline.ui.models

import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.adapters.recycler_view.OrderItemsListAdapter
import java.io.Serializable

data class ProductModelDto(
    var orderProducts: ArrayList<OrderItemsListAdapter.BasketItem>,
    val totalSum: Float
): Serializable