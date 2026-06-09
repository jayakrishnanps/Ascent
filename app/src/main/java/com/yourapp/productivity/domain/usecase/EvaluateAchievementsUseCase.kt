package com.yourapp.productivity.domain.usecase

import com.yourapp.productivity.data.local.database.entities.AchievementConditionType
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class EvaluateAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(task: Task) {
        val achievements = achievementRepository.getAchievementsForTask(task.id).firstOrNull() ?: emptyList()

        for (achievement in achievements) {
            if (achievement.isEarned) continue

            var newProgress = achievement.currentProgress
            var isEarned = false
            
            when (achievement.conditionType) {
                AchievementConditionType.COMPLETE_N_TIMES -> {
                    newProgress += 1
                    if (newProgress >= achievement.targetValue) {
                        isEarned = true
                        newProgress = achievement.targetValue
                    }
                }
                AchievementConditionType.COMPLETE_UNTIL_END_DATE -> {
                    // For COMPLETE_UNTIL_END_DATE, we assume targetValue = 1.
                    // If the task has a recurrenceEndDate and we've reached or passed it
                    if (task.recurrenceEndDate != null && System.currentTimeMillis() >= task.recurrenceEndDate) {
                        isEarned = true
                        newProgress = 1
                    }
                }
            }

            if (newProgress != achievement.currentProgress || isEarned) {
                val updatedAchievement = achievement.copy(
                    currentProgress = newProgress,
                    isEarned = isEarned,
                    earnedAt = if (isEarned) System.currentTimeMillis() else null
                )
                achievementRepository.updateAchievement(updatedAchievement)
            }
        }
    }
}
