package com.glance.streamline.domain.repository.products

import android.content.Context
import com.glance.streamline.domain.model.LastUpdateResponse
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.domain.model.ProductLayoutsResponse
import com.glance.streamline.domain.repository.BaseRepository
import com.glance.streamline.ui.models.TableModel
import com.glance.streamline.utils.extensions.android.Result
import com.glance.streamline.utils.extensions.android.Success
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ProductsApiRepository @Inject constructor(
    context: Context,
    private val productsApiInterface: ProductsApiInterface
) : BaseRepository(context) {

    fun getLastUpdate(onResponse: (Result<LastUpdateResponse>) -> Unit) =
        productsApiInterface.getLastUpdate().getSchedulers().getWrapped(onResponse)

    fun getLayouts(id: String, page: Int? = null, amount: Int? = 0, onResponse: (Result<ProductLayoutsResponse>) -> Unit)
            = productsApiInterface.getLayouts(id).getSchedulers().getWrapped(onResponse)

    fun getHubLayouts(hubId: String, page: Int? = null, amount: Int? = 0, onResponse: (Result<ProductLayoutsResponse>) -> Unit)
        = productsApiInterface.getHubAllLayouts(hubId).map { productLayoutsResponse ->
            productLayoutsResponse?.apply {
                this.values?.forEach {
                    for (i in it.buttons){
                        val buttonId = i.id
                        val productId = i.product?.id
                        if (i.product?.groups != null){
                            for (b in i.product.groups){
                                b.options.forEach {
                                    it.productGroupId = b.id
                                    it.parentProductId = productId.orEmpty()
                                }
                            }
                        }
                    }
                }
            }
    }.getSchedulers().getWrapped(onResponse)


    fun getButtons(layoutId: String, onResponse: (Result<List<ProductLayout.ProductButton>>) -> Unit)
     = productsApiInterface.getButtons(layoutId).getSchedulers().getWrapped(onResponse)

    fun getLayoutWithProducts(layoutId: String, onResponse: (Result<ProductLayout>) -> Unit)
    = productsApiInterface.getLayoutWithProducts(layoutId).map {
        it.apply {
            for (i in buttons){
                val buttonId = i.id
                val productId = i.product?.id
                if (i.product?.groups != null){
                    for (b in i.product.groups){
                        b.options.forEach {
                            it.productGroupId = b.id
                            it.parentProductId = productId.orEmpty()
                        }
                    }
                }
            }
        }
    }.getSchedulers().getWrapped(onResponse)


    fun generateTablesList(): Single<Result<ArrayList<TableModel>>> {
        val tables = arrayListOf<TableModel>()
        (0..2).forEach {
            tables.add(TableModel("Table ${it + 1}", arrayListOf()))
        }
        return Single.timer(1000, TimeUnit.MILLISECONDS).map { Success(tables) }
    }
}