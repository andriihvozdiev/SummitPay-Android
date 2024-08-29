package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
abstract class ProductButtonsDao {

    @Query("SELECT * FROM ProductButtonInfo WHERE layoutId=:layoutId")
    abstract fun getProductButtons(layoutId: String): Maybe<List<ProductButtonInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertProductButton(productButtonInfo: ProductButtonInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateProductButton(productButtonInfo: ProductButtonInfo): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateProductButtonMaybe(productButtonInfo: ProductButtonInfo): Maybe<Int>

//    @Transaction
//    open fun saveProductButtons(productButtonInfo: List<ProductButtonInfo>) {
//        productButtonInfo.forEach {
//            if (insertProductButton(it) == -1L) {
//                updateProductButton(it)
//            }
//        }
//    }
//
//    fun saveProductButtonsCompletable(productButtons: List<ProductButtonInfo>): Completable {
//        return Completable.fromCallable { saveProductButtons(productButtons) }
//    }

    @Transaction
    open fun saveProductButtons(productButtons: List<ProductLayout.ProductButton>, layoutId: String) {
        productButtons.forEach {
            val productButtonInfo = it.toProductButtonInfo()
            productButtonInfo.layoutId = layoutId
            if (insertProductButton(productButtonInfo) == -1L) {
                updateProductButton(productButtonInfo)
            }
        }
    }

    fun saveProductButtonsCompletable(productButtons: List<ProductLayout.ProductButton>, layoutId: String): Completable {
        return Completable.fromCallable {
            saveProductButtons(productButtons, layoutId)
        }
    }

    @Transaction
    open fun saveProductButtons(productLayouts: List<ProductLayout>) {
        productLayouts.forEach { productLayout ->
            val layoutId = productLayout.id
            productLayout.buttons.forEach { productButton ->
                val productButtonInfo = productButton.toProductButtonInfo()
                productButtonInfo.layoutId = layoutId
                if (insertProductButton(productButtonInfo) == -1L) {
                    updateProductButton(productButtonInfo)
                }
            }
        }
    }

    fun saveProductButtonsCompletable(productButtons: List<ProductLayout>): Completable {
        return Completable.fromCallable {
            saveProductButtons(productButtons)
        }
    }

    @Query("DELETE FROM ProductButtonInfo")
    abstract fun deleteAllProductButtons(): Maybe<Int>

    @Query("DELETE FROM ProductButtonInfo WHERE layoutId=:layoutId")
    abstract fun deleteProductButtonInfo(layoutId: String): Maybe<Int>
}