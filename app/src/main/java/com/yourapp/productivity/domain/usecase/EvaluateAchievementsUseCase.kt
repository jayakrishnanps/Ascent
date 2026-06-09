package com.yourapp.productivity.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.data.local.database.entities.UserAchievement
import com.yourapp.productivity.domain.repository.AchievementRepository
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class EvaluateAchievementsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val achievementRepository: AchievementRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke() {
        val userId = firebaseAuth.currentUser?.uid ?: "mock_user_123"
        
        val userProgress = userRepository.getUserProgress(userId).firstOrNull() ?: return
        val totalCompletions = completionHistoryRepository.getTotalCompletionsCount()
        
        val allAchievements = achievementRepository.getAllAchievements().firstOrNull() ?: emptyList()
        val earnedAchievements = achievementRepository.getUserAchievements(userId).firstOrNull()?.map { it.achievementId } ?: emptyList()

        for (achievement in allAchievements) {
            if (earnedAchievements.contains(achievement.id)) continue

            var conditionMet = false
            when (achievement.conditionType) {
                "TASK_COUNT" -> {
                    if (totalCompletions >= achievement.conditionValue) conditionMet = true
                }
                "LEVEL" -> {
                    if (userProgress.currentLevel >= achievement.conditionValue) conditionMet = true
                }
                "STREAK" -> {
                    if (userProgress.currentStreak >= achievement.conditionValue) conditionMet = true
                }
                "HARD_TASKS" -> {
                }
            }

            if (conditionMet) {
                val newEarned = UserAchievement(
                    userId = userId,
                    achievementId = achievement.id,
                    earnedAt = System.currentTimeMillis()
                )
                achievementRepository.insertUserAchievement(newEarned)
            }
        }
    }
}
