package com.yourapp.productivity.di

import com.yourapp.productivity.data.repository.AchievementRepositoryImpl
import com.yourapp.productivity.data.repository.CompletionHistoryRepositoryImpl
import com.yourapp.productivity.data.repository.TaskRepositoryImpl
import com.yourapp.productivity.data.repository.UserRepositoryImpl
import com.yourapp.productivity.domain.repository.AchievementRepository
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.TaskRepository
import com.yourapp.productivity.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindCompletionHistoryRepository(
        impl: CompletionHistoryRepositoryImpl
    ): CompletionHistoryRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(
        impl: AchievementRepositoryImpl
    ): AchievementRepository
}

