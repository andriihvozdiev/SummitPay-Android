package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.UserLogoutTimeout
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
abstract class UserTimeoutDao {

    @Query("SELECT * FROM UserLogoutTimeout")
    abstract fun getTimeout(): Maybe<UserLogoutTimeout>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertTimeout(timeout: UserLogoutTimeout): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateTimeout(timeout: UserLogoutTimeout): Int

    @Transaction
    open fun saveTimeout(timeout: UserLogoutTimeout) {
        val rowId = insertTimeout(timeout)
        if (rowId == -1L) {
            updateTimeout(timeout)
        }
    }

    fun saveTimeoutCompletable(timeout: UserLogoutTimeout): Completable {
        return Completable.fromCallable { saveTimeout(timeout) }
    }

    @Query("SELECT timeLeftSeconds FROM UserLogoutTimeout")
    abstract fun getTimeLeft(): Maybe<Long>

    @Query("UPDATE UserLogoutTimeout SET timeLeftSeconds=:timeLeft")
    abstract fun updateTimeout(timeLeft: Long): Maybe<Int>

    @Query("DELETE FROM UserLogoutTimeout")
    abstract fun deleteTimeout(): Maybe<Int>

}