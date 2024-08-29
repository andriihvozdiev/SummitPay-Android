package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.LoginRecordInfo
import com.glance.streamline.domain.model.LoginRecord
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
abstract class LoginRecordsDao {

    @Query("SELECT * FROM LoginRecordInfo")
    abstract fun getLoginRecords(): Single<List<LoginRecordInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertLoginRecord(loginRecord: LoginRecordInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateLoginRecord(loginRecord: LoginRecordInfo): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateLoginRecordMaybe(loginRecord: LoginRecordInfo): Maybe<Int>

    @Transaction
    open fun saveLoginRecords(loginRecords: List<LoginRecordInfo>) {
        loginRecords.forEach {
            if (insertLoginRecord(it) == -1L) {
                updateLoginRecord(it)
            }
        }
    }

    fun saveLoginRecordsCompletable(categories: List<LoginRecordInfo>): Completable {
        return Completable.fromCallable { saveLoginRecords(categories) }
    }

    @Transaction
    open fun saveLoginRecords(loginRecords: ArrayList<LoginRecord>) {
        loginRecords.forEach {
            val loginRecordInfo = it.toLoginRecordInfo()
            if (insertLoginRecord(loginRecordInfo) == -1L) {
                updateLoginRecord(loginRecordInfo)
            }
        }
    }

    fun saveLoginRecordsCompletable(loginRecords: ArrayList<LoginRecord>): Completable {
        return Completable.fromCallable { saveLoginRecords(loginRecords) }
    }

    @Query("DELETE FROM LoginRecordInfo")
    abstract fun deleteLoginRecords(): Maybe<Int>
}