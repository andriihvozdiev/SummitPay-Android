package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.DeviceAssigningInfo
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
abstract class DeviceAssigningDao {

    @Query("SELECT * FROM DeviceAssigningInfo")
    abstract fun getDeviceAssigningInfo(): Maybe<DeviceAssigningInfo>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertDeviceAssigningInfo(info: DeviceAssigningInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun updateDeviceAssigningInfo(info: DeviceAssigningInfo): Int

    @Transaction
    protected open fun saveDeviceAssigningInfo(info: DeviceAssigningInfo) {
        if (insertDeviceAssigningInfo(info) == -1L) {
            updateDeviceAssigningInfo(info)
        }
    }

    fun saveDeviceAssigningInfoCompletable(info: DeviceAssigningInfo): Completable {
        return Completable.fromCallable { saveDeviceAssigningInfo(info) }
    }

    @Query("DELETE FROM DeviceAssigningInfo")
    abstract fun deleteDeviceAssigningInfo(): Maybe<Int>
}