package com.glance.streamline.data.converters

import androidx.room.TypeConverter
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.utils.extensions.fromJson
import com.glance.streamline.utils.extensions.toJson

class ProductConverters {
    @TypeConverter
    fun fromProductGroups(productGroups: List<ProductLayout.ProductButton.ProductItem.ProductGroup>): String = productGroups.toJson()

    @TypeConverter
    fun toProductGroups(productGroupsJson: String): List<ProductLayout.ProductButton.ProductItem.ProductGroup> =
        fromJson<List<ProductLayout.ProductButton.ProductItem.ProductGroup>>(productGroupsJson) ?: arrayListOf()

    @TypeConverter
    fun fromProductCategory(productCategory: ProductLayout.ProductButton.ProductItem.ProductCategory): String = productCategory.toJson()

    @TypeConverter
    fun toProductCategory(productCategoryJson: String): ProductLayout.ProductButton.ProductItem.ProductCategory =
        fromJson<ProductLayout.ProductButton.ProductItem.ProductCategory>(productCategoryJson) ?: ProductLayout.ProductButton.ProductItem.ProductCategory()

    @TypeConverter
    fun fromAllProductOptions(allProductOptions: List<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption>): String = allProductOptions.toJson()

    @TypeConverter
    fun toAllProductOptions(allProductOptionsJson: String): List<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption> =
        fromJson<List<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption>>(allProductOptionsJson) ?: arrayListOf()
}