package com.yourapp.productivity.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import com.yourapp.productivity.data.local.database.entities.Subtask
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.TaskRepository
import com.yourapp.productivity.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ToggleSubtaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(task: Task, subtask: Subtask) {
        val now = System.currentTimeMillis()
        val isNowCompleted = !subtask.isCompleted
        
        val updatedSubtask = subtask.copy(
            isCompleted = isNowCompleted,
            completedAt = if (isNowCompleted) now else null
        )
        taskRepository.updateSubtask(updatedSubtask)

        val xpEarned = (task.difficulty.xpAward * 0.1).toInt()

        val userId = firebaseAuth.currentUser?.uid ?: "mock_user_123"
        val userProgress = userRepository.getUserProgress(userId).firstOrNull()
        if (userProgress != null) {
            val xpDelta = if (isNowCompleted) xpEarned else -xpEarned
            val newTotalXp = (userProgress.totalXp + xpDelta).coerceAtLeast(0)
                
                val newLevel = calculateLevel(newTotalXp)
                
                val updatedProgress = userProgress.copy(
                    totalXp = newTotalXp,
                    currentLevel = newLevel
                )
                userRepository.updateUserProgress(updatedProgress)
            }
        
        if (isNowCompleted) {
            val history = CompletionHistory(
                taskId = subtask.id,
                completedAt = now,
                xpEarned = xpEarned
            )
            completionHistoryRepository.insert(history)
        }
    }
    
    private fun calculateLevel(totalXp: Int): Int {
        var xpNeeded = 400
        var currentLevel = 1
        var remainingXp = totalXp
        
        while (remainingXp >= xpNeeded && currentLevel < 100) {
            remainingXp -= xpNeeded
            currentLevel++
            xpNeeded += 100
        }
        return currentLevel
    }
}
