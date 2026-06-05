package com.yourapp.productivity.domain.repository

import com.yourapp.productivity.data.local.database.entities.UserProgress
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProgress(userId: String): Flow<UserProgress?>
    suspend fun createUserIfNotExists(userId: String, displayName: String? = null, email: String? = null, photoUrl: String? = null)
    suspend fun updateUserProgress(userProgress: UserProgress)
}
