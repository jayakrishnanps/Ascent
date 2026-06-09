package com.yourapp.productivity.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.model.RecurrenceType
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.TaskRepository
import com.yourapp.productivity.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject

class EvaluateMissedTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke() {
        val now = System.currentTimeMillis()
        val startOfToday = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val pendingTasks = taskRepository.getPendingTasks().first()
        val missedRecurringTasks = pendingTasks.filter { 
            it.recurrenceType != RecurrenceType.NONE && 
            it.startDate != null && 
            it.startDate < startOfToday 
        }

        if (missedRecurringTasks.isEmpty()) return

        val userId = firebaseAuth.currentUser?.uid ?: return
        var userProgress = userRepository.getUserProgress(userId).firstOrNull() ?: return

        for (task in missedRecurringTasks) {
            // Apply Penalty
            val penalty = task.difficulty.xpAward
            val newTotalXp = maxOf(0, userProgress.totalXp - penalty)
            val newLevel = calculateLevel(newTotalXp)

            userProgress = userProgress.copy(
                totalXp = newTotalXp,
                currentLevel = newLevel,
                currentStreak = 0 // Reset streak
            )

            // Record Miss
            val history = CompletionHistory(
                taskId = task.id,
                completedAt = now,
                xpEarned = -penalty,
                isMissed = true
            )
            completionHistoryRepository.insert(history)

            // Advance task date
            val nextValidDate = calculateNextValidDate(task, startOfToday)
            
            if (task.recurrenceEndDate != null && nextValidDate > task.recurrenceEndDate) {
                // If the next valid date is after the end date, just complete/delete it
                taskRepository.deleteTask(task)
            } else {
                val updatedTask = task.copy(startDate = nextValidDate)
                taskRepository.updateTask(updatedTask)
            }
        }

        userRepository.updateUserProgress(userProgress)
    }

    private fun calculateNextValidDate(task: Task, startOfToday: Long): Long {
        var cal = Calendar.getInstance()
        task.startDate?.let { cal.timeInMillis = it }
        
        while (cal.timeInMillis < startOfToday) {
            when (task.recurrenceType) {
                RecurrenceType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                RecurrenceType.WEEKLY -> {
                    val weeklyDaysStr = task.weeklyDays
                    val weeklyDays = weeklyDaysStr?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
                    
                    if (weeklyDays.isNotEmpty()) {
                        var currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                        var daysToAdd = 7
                        for (i in 1..7) {
                            val nextDay = (currentDayOfWeek + i) % 7
                            if (weeklyDays.contains(nextDay)) {
                                daysToAdd = i
                                break
                            }
                        }
                        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    } else {
                        cal.add(Calendar.DAY_OF_YEAR, 7)
                    }
                }
                else -> break
            }
        }
        return cal.timeInMillis
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
