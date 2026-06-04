package com.yourapp.productivity.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.model.RecurrenceType
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.TaskRepository
import com.yourapp.productivity.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val firebaseAuth: FirebaseAuth,
    private val evaluateAchievementsUseCase: EvaluateAchievementsUseCase
) {
    suspend operator fun invoke(task: Task) {
        val now = System.currentTimeMillis()
        
        // 1. Mark task as completed
        val completedTask = task.copy(isCompleted = true, completedAt = now)
        taskRepository.updateTask(completedTask)

        // 2. Insert Completion History and award XP
        val xpEarned = task.difficulty.xpAward
        val history = CompletionHistory(
            taskId = task.id,
            completedAt = now,
            xpEarned = xpEarned
        )
        completionHistoryRepository.insert(history)

        // 3. Update User Progress
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val userProgress = userRepository.getUserProgress(userId).firstOrNull()
            if (userProgress != null) {
                val newXp = userProgress.totalXp + xpEarned
                val newLevel = (newXp / 100) + 1
                
                // Calculate streak
                var currentStreak = userProgress.currentStreak
                var longestStreak = userProgress.longestStreak
                val lastCompletion = userProgress.lastCompletionDate

                if (lastCompletion != null) {
                    val lastCal = Calendar.getInstance().apply { timeInMillis = lastCompletion }
                    val currentCal = Calendar.getInstance().apply { timeInMillis = now }

                    val diffDays = (currentCal.timeInMillis - lastCal.timeInMillis) / (1000 * 60 * 60 * 24)
                    
                    if (diffDays == 1L || isNextDay(lastCal, currentCal)) {
                        currentStreak += 1
                        if (currentStreak > longestStreak) {
                            longestStreak = currentStreak
                        }
                    } else if (!isSameDay(lastCal, currentCal)) {
                        currentStreak = 1
                    }
                } else {
                    currentStreak = 1
                    longestStreak = 1
                }

                val updatedProgress = userProgress.copy(
                    totalXp = newXp,
                    currentLevel = newLevel,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    lastCompletionDate = now
                )
                userRepository.updateUserProgress(updatedProgress)
            }
        }

        // 4. Handle Recurrence
        if (task.recurrenceType != RecurrenceType.NONE) {
            generateNextRecurrence(task, now)
        }

        // 5. Evaluate achievements
        evaluateAchievementsUseCase()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isNextDay(lastCal: Calendar, currentCal: Calendar): Boolean {
        val nextDayCal = lastCal.clone() as Calendar
        nextDayCal.add(Calendar.DAY_OF_YEAR, 1)
        return isSameDay(nextDayCal, currentCal)
    }

    private suspend fun generateNextRecurrence(task: Task, currentTime: Long) {
        val nextDueDate = when (task.recurrenceType) {
            RecurrenceType.DAILY -> {
                val cal = Calendar.getInstance()
                task.dueDate?.let { cal.timeInMillis = it }
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            RecurrenceType.WEEKLY -> {
                val cal = Calendar.getInstance()
                task.dueDate?.let { cal.timeInMillis = it }
                cal.add(Calendar.DAY_OF_YEAR, 7) // Simple implementation: same day next week
                cal.timeInMillis
            }
            RecurrenceType.NONE -> null
        }

        if (nextDueDate != null) {
            // Check end date
            if (task.recurrenceEndDate == null || nextDueDate <= task.recurrenceEndDate) {
                val newTask = task.copy(
                    id = UUID.randomUUID().toString(),
                    isCompleted = false,
                    completedAt = null,
                    dueDate = nextDueDate
                )
                taskRepository.insertTask(newTask)
            }
        }
    }
}
