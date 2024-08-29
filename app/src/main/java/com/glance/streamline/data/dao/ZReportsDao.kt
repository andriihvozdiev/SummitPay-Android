package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.ZReportInfo
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
abstract class ZReportsDao {

    @Query("SELECT * FROM ZReportInfo")
    abstract fun getAllZReportsInfo(): Maybe<List<ZReportInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertZReportInfo(info: ZReportInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun updateZReportInfo(info: ZReportInfo): Int

    @Transaction
    protected open fun saveZReportInfo(info: ZReportInfo) {
        if (insertZReportInfo(info) == -1L) {
            updateZReportInfo(info)
        }
    }

    fun saveZReportInfoCompletable(info: ZReportInfo): Completable {
        return Completable.fromCallable { saveZReportInfo(info) }
    }

    @Query("DELETE FROM ZReportInfo")
    abstract fun deleteAllZReportInfo(): Maybe<Int>
}
