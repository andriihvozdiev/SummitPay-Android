package com.glance.streamline.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.glance.streamline.data.converters.PaymentHistoryInfoConverters
import com.glance.streamline.data.converters.ProductConverters
import com.glance.streamline.data.converters.ReportInfoConverters
import com.glance.streamline.data.converters.UserInfoConverters
import com.glance.streamline.data.dao.*
import com.glance.streamline.data.entities.*

@Database(
    entities = [
        UserModel::class,
        UserLogoutTimeout::class,
        DeviceAssigningInfo::class,
        ReportInfo::class,
        CategoryInfo::class,
        ProductButtonInfo::class,
        LoginRecordInfo::class,
        ZReportInfo::class,
        PaymentHistoryInfo::class
    ], version = 18
)
@TypeConverters(UserInfoConverters::class, ProductConverters::class, ReportInfoConverters::class, PaymentHistoryInfoConverters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE UserModel ADD COLUMN testMigration INTEGER DEFAULT 333 NOT NULL")
            }
        }
        val MIGRATION_2_1 = object : Migration(2, 1) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }
    }

    abstract fun userInfoDao(): UserInfoDao
    abstract fun userTimeoutDao(): UserTimeoutDao
    abstract fun productsDao(): ProductButtonsDao
    abstract fun deviceAssigningDao(): DeviceAssigningDao
    abstract fun reportsDao(): ReportsDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun loginRecordsDao(): LoginRecordsDao
    abstract fun zReportDao(): ZReportsDao
    abstract fun paymentHistoryDao(): PaymentHistoryDao
}