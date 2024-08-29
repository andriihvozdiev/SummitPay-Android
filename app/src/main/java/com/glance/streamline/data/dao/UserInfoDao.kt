package com.glance.streamline.data.dao

import androidx.room.*
import com.glance.streamline.data.entities.UserModel
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
abstract class UserInfoDao {
    companion object {
        private const val queryGetUser = "SELECT * FROM userModel"
        private const val queryDeleteUser = "DELETE FROM userModel"
    }

    @Query(queryGetUser)
    abstract fun getUser(): Maybe<UserModel>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertUser(user: UserModel): Long

    fun insertUserSingle(user: UserModel) = Single.fromCallable { insertUser(user) }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateUser(user: UserModel): Single<Int>

    fun updateUserSingle(user: UserModel) = Single.fromCallable { updateUser(user) }

    @Transaction
    open fun saveUser(user: UserModel) {
        if (insertUser(user) == -1L) {
            updateUser(user)
        }
    }

    fun saveUserCompletable(user: UserModel): Completable {
        return Completable.fromCallable { saveUser(user) }
    }

    @Delete
    abstract fun deleteUser(user: UserModel): Single<Int>

    @Query(queryDeleteUser)
    abstract fun deleteUser(): Maybe<Int>
}