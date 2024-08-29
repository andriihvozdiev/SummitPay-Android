package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.data.entities.CategoryInfo
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
abstract class CategoriesDao {

    @Query("SELECT * FROM CategoryInfo WHERE hub_id=:hubId")
    abstract fun getCategories(hubId: String): Single<List<CategoryInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertCategory(category: CategoryInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateCategory(category: CategoryInfo): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateCategoryMaybe(category: CategoryInfo): Maybe<Int>

    @Transaction
    open fun saveCategories(categories: List<CategoryInfo>) {
        categories.forEach {
            if (insertCategory(it) == -1L) {
                updateCategory(it)
            }
        }
    }

    fun saveCategoriesCompletable(categories: List<CategoryInfo>): Completable {
        return Completable.fromCallable { saveCategories(categories) }
    }

    @Transaction
    open fun saveCategories(categories: ArrayList<ProductLayout>) {
        categories.forEach {
            val categoryInfo = it.toCategoryInfo()
            if (insertCategory(categoryInfo) == -1L) {
                updateCategory(categoryInfo)
            }
        }
    }

    fun saveCategoriesCompletable(categories: ArrayList<ProductLayout>): Completable {
        return Completable.fromCallable { saveCategories(categories) }
    }

    @Query("DELETE FROM CategoryInfo")
    abstract fun deleteCategory(): Maybe<Int>

    @Query("DELETE FROM CategoryInfo WHERE hub_id=:hubId")
    abstract fun deleteCategory(hubId: String): Maybe<Int>
}