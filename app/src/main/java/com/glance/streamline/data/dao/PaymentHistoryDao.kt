package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.PaymentHistoryInfo
import com.glance.streamline.domain.model.payment.PaymentHistoryModel
import io.reactivex.Completable
import io.reactivex.Maybe
import java.util.*
import kotlin.collections.ArrayList

@Dao
abstract class PaymentHistoryDao {

    @Query("SELECT * FROM PaymentHistoryInfo ORDER BY paymentDate DESC")
    abstract fun getAllPaymentHistories(): Maybe<List<PaymentHistoryInfo>>

    @Query("SELECT * FROM PaymentHistoryInfo WHERE paymentDate >= :from AND paymentDate <= :to ORDER BY paymentDate DESC")
    abstract fun getPaymentHistories(from: Date, to: Date): Maybe<List<PaymentHistoryInfo>>

    @Query("SELECT * FROM PaymentHistoryInfo WHERE paymentDate >= :from ORDER BY paymentDate DESC")
    abstract fun getPaymentHistories(from: Date): Maybe<List<PaymentHistoryInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertPaymentHistory(paymentHistory: PaymentHistoryInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updatePaymentHistory(paymentHistory: PaymentHistoryInfo): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updatePaymentHistoryMaybe(paymentHistory: PaymentHistoryInfo): Maybe<Int>

    @Transaction
    open fun savePaymentHistories(paymentHistories: List<PaymentHistoryInfo>) {
        paymentHistories.forEach {
            if (insertPaymentHistory(it) == -1L) {
                updatePaymentHistory(it)
            }
        }
    }

    fun savePaymentHistoriesCompletable(paymentHistories: List<PaymentHistoryInfo>): Completable {
        return Completable.fromCallable { savePaymentHistories(paymentHistories) }
    }

    @Transaction
    open fun savePaymentHistories(paymentHistories: ArrayList<PaymentHistoryModel>) {
        paymentHistories.forEach {
            val categoryInfo = it.toPaymentHistoryInfo()
            if (insertPaymentHistory(categoryInfo) == -1L) {
                updatePaymentHistory(categoryInfo)
            }
        }
    }

    fun savePaymentHistoriesCompletable(paymentHistories: ArrayList<PaymentHistoryModel>): Completable {
        return Completable.fromCallable { savePaymentHistories(paymentHistories) }
    }

    @Query("DELETE FROM PaymentHistoryInfo")
    abstract fun deleteAllPaymentHistories(): Maybe<Int>

    @Query("DELETE FROM PaymentHistoryInfo WHERE id=:id")
    abstract fun deletePaymentHistory(id: String): Maybe<Int>
}