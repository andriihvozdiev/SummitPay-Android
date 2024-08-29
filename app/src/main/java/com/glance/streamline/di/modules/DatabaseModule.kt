package com.glance.streamline.di.modules

import android.content.Context
import androidx.room.Room
import com.glance.streamline.data.room.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    /*@Provides
    @Singleton
    fun provideRealmInstance(): Realm = Realm.getDefaultInstance()*/

    @Provides
    @Singleton
    fun provideDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "database")
//            .addMigrations(AppDatabase.MIGRATION_1_2)
//            .addMigrations(AppDatabase.MIGRATION_2_1)
            .fallbackToDestructiveMigration()
            .build()
}