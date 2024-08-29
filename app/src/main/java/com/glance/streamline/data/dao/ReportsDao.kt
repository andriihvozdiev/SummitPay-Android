package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.ReportInfo
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
abstract class ReportsDao {

    @Query("SELECT * FROM ReportInfo")
    abstract fun getAllReportInfo(): Maybe<List<ReportInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertReportInfo(info: ReportInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun updateReportInfo(info: ReportInfo): Int

    @Transaction
    protected open fun saveReportInfo(info: ReportInfo) {
        if (insertReportInfo(info) == -1L) {
            updateReportInfo(info)
        }
    }

    fun saveReportInfoCompletable(info: ReportInfo): Completable {
        return Completable.fromCallable { saveReportInfo(info) }
    }

    @Delete
    abstract fun deleteReportInfo(reportInfo: ReportInfo): Maybe<Int>

    @Query("DELETE FROM ReportInfo")
    abstract fun deleteAllReportInfo(): Maybe<Int>
}