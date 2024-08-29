package com.glance.streamline.ui.fragments.main.checkout

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.data.entities.CategoryInfo
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.data.entities.ZReportInfo
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.domain.model.payment.PaymentResponseModel
import com.glance.streamline.domain.repository.products.ProductsApiRepository
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.ui.dialogs.refuselog.RefuseMessageDialog
import com.glance.streamline.ui.fragments.auth.assign.device_info.BUSSINES_ID_KEY
import com.glance.streamline.ui.fragments.auth.pin_code.ASSIGNED_HUB_ID_KEY
import com.glance.streamline.ui.models.PaymentResultType
import com.glance.streamline.ui.models.ProductType
import com.glance.streamline.ui.models.TableModel
import com.glance.streamline.utils.extensions.android.getSharedPref
import com.glance.streamline.utils.extensions.parseRFC3339Nano
import com.glance.streamline.utils.extensions.toISO_8601_Timezone
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

const val LAST_UPDATED_KEY = "LAST_UPDATED_KEY"
class CheckoutFragmentViewModel @Inject constructor(
    app: Application,
    private val db: AppDatabase,
    private val productsApiRepository: ProductsApiRepository
) : BaseViewModel(app) {

    private val mBussinesId: String by lazy { getContext().getSharedPref()?.getString(BUSSINES_ID_KEY, "").orEmpty() }

    private var strReceivedLastUpdated: String = ""

    private var mAssignedHubId: String = getContext().getSharedPref()?.getString(ASSIGNED_HUB_ID_KEY, "").orEmpty()

    var isRefusalLogWasShown = false
    val listOfProductsNameForRefusalLog: ArrayList<ProductType> =
        arrayListOf(ProductType.CONTAIN_ALCOHOL_DRINK, ProductType.TOBACCO)

    val currentSelectedLayoutLiveData = MutableLiveData<List<ProductButtonInfo>>()
    val lastRefusalLogResult = MutableLiveData<RefuseMessageDialog.RefuseDialogResult>()

    val categoriesLayoutListLiveData = MutableLiveData<List<CategoryInfo>>()

    val tablesLiveData = MutableLiveData<ArrayList<TableModel>>()
    val searchButtonResultsLiveData = MutableLiveData<ArrayList<ProductButtonInfo>>()
    val paymentResponseLiveData = MutableLiveData<PaymentResponseModel>()

    val customProductMultiplierLiveData = MutableLiveData<Int>()

    fun checkLastUpdated() {

        productsApiRepository.getLastUpdate {
            it.unWrapResult(null, false) {
                strReceivedLastUpdated = it.value.lastUpdate

                val lastUpdated = Date().parseRFC3339Nano(strReceivedLastUpdated)

                val savedLastUpdated = getContext().getSharedPref()?.getString(LAST_UPDATED_KEY, "").orEmpty()
                val localLastUpdated = Date().parseRFC3339Nano(savedLastUpdated)

                if (localLastUpdated == null || localLastUpdated.before(lastUpdated)) {
                    getLayoutsList()
                }
            }
        }.call()
    }

    fun getLayoutsList(
        page: Int? = null,
        amount: Int? = null
    ) {
        mAssignedHubId = getContext().getSharedPref()?.getString(ASSIGNED_HUB_ID_KEY, "").orEmpty()
        val hubId = mAssignedHubId
        productsApiRepository.getHubLayouts(hubId, page, amount) {
            it.unWrapResult {
                val categories = it.value.values ?: arrayListOf()
                db.categoriesDao()
                    .deleteCategory(hubId)
                    .call {
                        db.categoriesDao()
                            .saveCategoriesCompletable(categories)
                            .call {
                                if (categories.isNotEmpty()) {
                                    saveAllProductsToDB(categories, hubId, page, amount)
//                                  getLayoutsListFromDB(hubId, page, amount)
                                }
                            }
                    }

            }
        }.call()
    }

    fun saveCashReports(price: Float, itemCount: Int) {

        val zReportInfo = ZReportInfo(
            Calendar.getInstance().time.toISO_8601_Timezone(),
            PaymentResultType.CASH_PAYMENT_TYPE,
            price,
            itemCount
        )
        db.zReportDao()
            .saveZReportInfoCompletable(zReportInfo)
            .call {

            }

    }

    fun saveCardReports(price: Float, itemCount: Int) {

        val zReportInfo = ZReportInfo(
            Calendar.getInstance().time.toISO_8601_Timezone(),
            PaymentResultType.CARD_PAYMENT_TYPE,
            price,
            itemCount
        )

        db.zReportDao()
            .saveZReportInfoCompletable(zReportInfo)
            .call ()
    }

    fun saveAllProductsToDB(
        productLayouts: ArrayList<ProductLayout>,
        hubId: String = mAssignedHubId,
        page: Int? = null,
        amount: Int? = null
    ) {
        db.productsDao()
            .deleteAllProductButtons()
            .call {
                db.productsDao()
                    .saveProductButtonsCompletable(productLayouts)
                    .call {
                        getContext().getSharedPref()?.edit {
                            putString(LAST_UPDATED_KEY, strReceivedLastUpdated)
                        }
                        getLayoutsListFromDB(page, amount)
                    }
            }
    }

    fun getLayoutsListFromDB(
        page: Int? = null,
        amount: Int? = null
    ) {
        mAssignedHubId = getContext().getSharedPref()?.getString(ASSIGNED_HUB_ID_KEY, "").orEmpty()
        val hubId = mAssignedHubId
        db.categoriesDao()
            .getCategories(hubId)
            .call {
                categoriesLayoutListLiveData.value = it
                if (it.isNullOrEmpty()) {
                    getLayoutsList(page, amount)
                }
            }
    }

    fun getLayoutWithProducts(layoutId: String) {
        productsApiRepository.getLayoutWithProducts(layoutId) {
            it.unWrapResult {
                val buttons = it.value.buttons
                db.productsDao()
                    .deleteProductButtonInfo(layoutId)
                    .call {
                        db.productsDao()
                            .saveProductButtonsCompletable(buttons, layoutId)
                            .call {
                                if (buttons.isNotEmpty()) getLayoutWithProductsFromDB(layoutId)
                            }
                    }
            }
        }.call()
    }

    fun getLayoutWithProductsFromDB(layoutId: String) {
        db.productsDao()
            .getProductButtons(layoutId)
            .call {
                currentSelectedLayoutLiveData.postValue(it)
                if (it.isNullOrEmpty()) {
                    getLayoutWithProducts(layoutId)
                }
            }
    }

    fun getTablesList() {
        productsApiRepository.generateTablesList().call { result ->
            result.unWrapResult {
                tablesLiveData.value = it.value
            }
        }
    }

    fun searchProducts(query: String) {
        val filteredButtons = arrayListOf<ProductButtonInfo>()
        currentSelectedLayoutLiveData.value?.let {
            filteredButtons.addAll(
                it.filter {
                    it.product_name.contains(query, true) == true
                }
            )

            searchButtonResultsLiveData.value = filteredButtons
        }
    }

    fun onSearchClosed() {
        searchButtonResultsLiveData.value = null
    }

    fun setProductQuantity(quantity: Int) {
        customProductMultiplierLiveData.postValue(quantity)
    }

    fun clearAllData() {
        searchButtonResultsLiveData.value = null
        currentSelectedLayoutLiveData.value = null
        tablesLiveData.value = null
        customProductMultiplierLiveData.value = null
        paymentResponseLiveData.value = null
        lastRefusalLogResult.value = null
        categoriesLayoutListLiveData.value = null
    }

    override fun onCleared() {
        super.onCleared()
        clearAllData()
    }
}
