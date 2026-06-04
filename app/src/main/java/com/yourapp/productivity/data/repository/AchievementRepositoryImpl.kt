package com.yourapp.productivity.data.repository

import com.yourapp.productivity.data.local.database.dao.AchievementDao
import com.yourapp.productivity.data.local.database.entities.Achievement
import com.yourapp.productivity.data.local.database.entities.UserAchievement
import com.yourapp.productivity.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao
) : AchievementRepository {
    override fun getAllAchievements(): Flow<List<Achievement>> = dao.getAllAchievements()
    override suspend fun insertAchievements(achievements: List<Achievement>) = dao.insertAchievements(achievements)
    override fun getUserAchievements(userId: String): Flow<List<UserAchievement>> = dao.getUserAchievements(userId)
    override suspend fun insertUserAchievement(userAchievement: UserAchievement) = dao.insertUserAchievement(userAchievement)
}
