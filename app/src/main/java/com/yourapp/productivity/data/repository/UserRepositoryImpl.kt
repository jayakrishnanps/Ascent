package com.yourapp.productivity.data.repository

import com.yourapp.productivity.data.local.database.dao.UserDao
import com.yourapp.productivity.data.local.database.entities.UserProgress
import com.yourapp.productivity.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getUserProgress(userId: String): Flow<UserProgress?> {
        return userDao.getUserProgress(userId)
    }

    override suspend fun createUserIfNotExists(userId: String) {
        val existingUser = userDao.getUserProgress(userId).firstOrNull()
        if (existingUser == null) {
            val newUser = UserProgress(
                userId = userId,
                totalXp = 0,
                currentLevel = 1,
                currentStreak = 0,
                longestStreak = 0,
                lastCompletionDate = null
            )
            userDao.insertUserProgress(newUser)
        }
    }

    override suspend fun updateUserProgress(userProgress: UserProgress) {
        userDao.updateUserProgress(userProgress)
    }
}
