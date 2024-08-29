package com.glance.streamline.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.domain.repository.payment.PaymentRequest
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
data class CategoryInfo(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val color: String = "",
    val business_id: String = "",
    val hub_id: String = "",
    var isSelected: Boolean = true,
    var lastUpdated: Date = Date()
)

@Entity
data class ProductButtonInfo(
    var layoutId: String = "",
    @PrimaryKey
    val id: String = "",
    val x: Int = 0,
    val y: Int = 0,
    val h: Int = 0,
    val w: Int = 0,
    var isSelected: Boolean = false,
    val product_id: String = "",
    val product_name: String = "",
    val product_description: String = "",
    val product_sku: String = "",
    val product_tax: Int = 0,
    val product_takeoutTaxRate: Int = 0,
    val product_retailPrice: String = "",
    val product_takeoutPrice: String = "",
    val product_costPrice: String = "",
    val product_promotions: Boolean = false,
    val product_stock: String = "",
    val product_status: Boolean = false,
    val product_groups: List<ProductLayout.ProductButton.ProductItem.ProductGroup> = listOf(),
    val product_category: ProductLayout.ProductButton.ProductItem.ProductCategory =
        ProductLayout.ProductButton.ProductItem.ProductCategory(),
    val allProductOptions: List<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption> = arrayListOf(),
    val lastUpdated: Date = Date()
) {
    constructor(layoutId: String, id: String, x: Int, y: Int, h: Int, w: Int, isSelected: Boolean,
                product: ProductLayout.ProductButton.ProductItem) : this(
        layoutId,
        id,
        x, y, h, w,
        isSelected,
        product_id = product.id,
        product_name = product.name,
        product_description = product.description,
        product_sku = product.sku,
        product_tax = product.tax,
        product_takeoutTaxRate = product.takeoutTaxRate,
        product_retailPrice = product.retailPrice,
        product_takeoutPrice = product.takeoutPrice,
        product_costPrice = product.costPrice,
        product_promotions = product.promotions,
        product_stock = product.stock,
        product_status = product.status,
        product_groups = product.groups,
        product_category = product.category,
        allProductOptions = product.allProductOptions
    )

}


