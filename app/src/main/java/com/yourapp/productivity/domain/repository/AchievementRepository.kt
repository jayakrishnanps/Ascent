package com.yourapp.productivity.domain.repository

import com.yourapp.productivity.data.local.database.entities.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getAchievementsForTask(taskId: String): Flow<List<Achievement>>
    suspend fun insertAchievement(achievement: Achievement)
    suspend fun updateAchievement(achievement: Achievement)
    suspend fun deleteAchievement(achievement: Achievement)
}
