package com.yourapp.productivity.data.repository

import com.yourapp.productivity.data.local.database.dao.AchievementDao
import com.yourapp.productivity.data.local.database.entities.Achievement
import com.yourapp.productivity.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements()
    }

    override fun getAchievementsForTask(taskId: String): Flow<List<Achievement>> {
        return achievementDao.getAchievementsForTask(taskId)
    }

    override suspend fun insertAchievement(achievement: Achievement) {
        achievementDao.insertAchievement(achievement)
    }

    override suspend fun updateAchievement(achievement: Achievement) {
        achievementDao.updateAchievement(achievement)
    }

    override suspend fun deleteAchievement(achievement: Achievement) {
        achievementDao.deleteAchievement(achievement)
    }
}
