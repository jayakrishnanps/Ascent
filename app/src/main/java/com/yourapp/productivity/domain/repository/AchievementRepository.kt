package com.yourapp.productivity.domain.repository

import com.yourapp.productivity.data.local.database.entities.Achievement
import com.yourapp.productivity.data.local.database.entities.UserAchievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    suspend fun insertAchievements(achievements: List<Achievement>)
    fun getUserAchievements(userId: String): Flow<List<UserAchievement>>
    suspend fun insertUserAchievement(userAchievement: UserAchievement)
}
