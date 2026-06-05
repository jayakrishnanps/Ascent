package com.yourapp.productivity.di

import android.content.Context
import androidx.room.Room
import com.yourapp.productivity.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ascent_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase) = database.taskDao()

    @Provides
    @Singleton
    fun provideCompletionHistoryDao(database: AppDatabase) = database.completionHistoryDao()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase) = database.userDao()

    @Provides
    @Singleton
    fun provideAchievementDao(database: AppDatabase) = database.achievementDao()
}
