package com.glance.streamline.domain.model


import android.os.Parcelable
import com.glance.streamline.data.entities.CategoryInfo
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.ui.models.ProductType
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductLayout(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "", // Layouts test
    @SerializedName("color")
    val color: String = "", // #46CBC5
    @SerializedName("category")
    val category: String = "", // Drinks
    @SerializedName("buttons_vertical")
    val buttons: List<ProductButton> = listOf(),
    @SerializedName("buttons_horizontal")
    val buttonsHorizontal: List<ProductButton> = listOf(),
    @SerializedName("business_id")
    val businessId: String = "",
    @SerializedName("hub_id")
    val hubId: String = ""
) : Parcelable {

    var isSelected: Boolean = true

    fun toCategoryInfo(): CategoryInfo {
        return CategoryInfo(
            id,
            name,
            color,
            businessId,
            hubId
        )
    }

    @Parcelize
    data class ProductButton(
        @SerializedName("id")
        val id: String = "", // fd7832f5-aaf0-4b77-824b-fed0a366a288
        @SerializedName("x")
        val x: Int = 0, // 1
        @SerializedName("y")
        val y: Int = 0, // 2
        @SerializedName("h")
        val h: Int = 0, // 3
        @SerializedName("w")
        val w: Int = 0, // 5
        @SerializedName("product")
        val product: ProductItem? = null
    ) : Parcelable {
        var isSelected: Boolean = false
        var swipeOffset = 0
        var itemsCount = 1
        var message: String = ""

        fun toProductButtonInfo(): ProductButtonInfo {
            return ProductButtonInfo(
                "",
                id,
                x,
                y,
                h,
                w,
                isSelected,
                product ?: ProductItem(),
            )
        }

        @Parcelize
        data class ProductItem(
            @SerializedName("id")
            val id: String = "", // fb0ebba6-084c-428a-b16e-7f2246840840
            @SerializedName("name")
            val name: String = "", // Best product
            @SerializedName("description")
            val description: String = "", // crabsburger
            @SerializedName("sku")
            val sku: String = "", // 12337
            @SerializedName("tax")
            val tax: Int = 0, // 15
            @SerializedName("takeout_tax_rate")
            val takeoutTaxRate: Int = 0, // 15
            @SerializedName("retail_price")
            val retailPrice: String = "", // 1337
            @SerializedName("takeout_price")
            val takeoutPrice: String = "", // 1337
            @SerializedName("cost_price")
            val costPrice: String = "", // 123
            @SerializedName("promotions")
            val promotions: Boolean = false, // true
            @SerializedName("stock")
            val stock: String = "", // 80lvl
            @SerializedName("status")
            val status: Boolean = false, // true
            @SerializedName("groups")
            val groups: List<ProductGroup> = listOf(),
            @SerializedName("category")
            val category: ProductCategory = ProductCategory("")
        ) : Parcelable {

            val productType: ProductType by lazy { generateProductTypeByName(name) }

            val allProductOptions: List<ProductGroup.ProductOption> by lazy {
                val list = arrayListOf<ProductGroup.ProductOption>()
                groups.forEachIndexed { index, productGroup ->
                    productGroup.options.forEach {
                        it.productGroupId = productGroup.id
                        list.add(it)
                    }
                }
                list
            }

            @Parcelize
            data class ProductGroup(
                @SerializedName("id")
                val id: String = "", // 2a377141-6fdb-488b-85a5-696199a7d351
                @SerializedName("name")
                val name: String = "", // Drinks
                @SerializedName("status")
                val status: Boolean = false, // false
                @SerializedName("created_at")
                val createdAt: String = "", // 2020-05-19T12:20:23.475721Z
                @SerializedName("options")
                val options: List<ProductOption> = listOf()
            ) : Parcelable {
                @Parcelize
                data class ProductOption(
                    @SerializedName("id")
                    val id: String = "", // 7ee59c76-55e5-4ce1-8565-c9f172cd1f7c
                    @SerializedName("name")
                    val name: String = "", // Drinkoption
                    @SerializedName("status")
                    val status: Boolean = false, // false
                    @SerializedName("price")
                    val price: String = "", // 123.12
                    @SerializedName("cost")
                    val cost: String = "" // 1231.12
                ) : Parcelable {
                    var productGroupId: String = ""
                    var parentProductId: String = ""
                }
            }

            @Parcelize
            data class ProductCategory(
                @SerializedName("id")
                val id: String = "", // 0117e1bb-33f6-4e5c-b26b-f04a7f611216
                @SerializedName("title")
                val title: String = "", // Drinks
                @SerializedName("color")
                val color: String = "", // #46CBC5
                @SerializedName("department")
                val department: String = "", // Drinks
                @SerializedName("created_at")
                val createdAt: String = "", // 2020-05-18T15:50:30.50243Z
                @SerializedName("is_restricted")
                val isRestricted: Boolean = false
            ) : Parcelable
        }
    }

    companion object {

        fun generateProductTypeByName(foodName: String): ProductType {
            val alcoholNames = listOf("Bar", "Alcohol", "Vodka", "Wine", "Beer", "Cider", "Mead", "Pulque", "Barley", "Whisky", "Scotch", "Gin")
            val tobaccoNames = listOf("Tobacco")
            return when {
                alcoholNames.any { foodName.contains(it) } -> ProductType.CONTAIN_ALCOHOL_DRINK
                tobaccoNames.any { foodName.contains(it) } -> ProductType.TOBACCO
                foodName.startsWith("Drink", true) -> ProductType.NO_ALCOHOL_DRINK
                foodName.startsWith("Coffee", true) -> ProductType.NO_ALCOHOL_DRINK
                else -> ProductType.FOOD
            }
        }
    }
}
