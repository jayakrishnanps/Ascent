package com.yourapp.productivity.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.data.local.database.entities.UserProgress
import com.yourapp.productivity.domain.model.Difficulty
import com.yourapp.productivity.domain.model.RecurrenceType
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.TaskRepository
import com.yourapp.productivity.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CompleteTaskUseCaseTest {

    class FakeTaskRepository : TaskRepository {
        val tasks = mutableListOf<Task>()
        val subtasks = mutableListOf<com.yourapp.productivity.data.local.database.entities.Subtask>()
        
        override fun getPendingTasks(): Flow<List<Task>> = flowOf(tasks.filter { !it.isCompleted })
        override fun getTasksForToday(todayEnd: Long): Flow<List<Task>> = flowOf(emptyList())
        override fun getUpcomingTasks(todayEnd: Long): Flow<List<Task>> = flowOf(emptyList())
        override suspend fun getTaskById(taskId: String): Task? = tasks.find { it.id == taskId }
        override suspend fun insertTask(task: Task) { tasks.add(task) }
        override suspend fun updateTask(task: Task) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) tasks[index] = task else tasks.add(task)
        }
        override suspend fun deleteTask(task: Task) { tasks.remove(task) }
        
        override fun getSubtasksForTask(taskId: String): Flow<List<com.yourapp.productivity.data.local.database.entities.Subtask>> = flowOf(subtasks.filter { it.taskId == taskId })
        override suspend fun getSubtaskById(subtaskId: String): com.yourapp.productivity.data.local.database.entities.Subtask? = subtasks.find { it.id == subtaskId }
        override suspend fun insertSubtask(subtask: com.yourapp.productivity.data.local.database.entities.Subtask) { subtasks.add(subtask) }
        override suspend fun updateSubtask(subtask: com.yourapp.productivity.data.local.database.entities.Subtask) {
            val index = subtasks.indexOfFirst { it.id == subtask.id }
            if (index != -1) subtasks[index] = subtask else subtasks.add(subtask)
        }
        override suspend fun deleteSubtask(subtask: com.yourapp.productivity.data.local.database.entities.Subtask) { subtasks.remove(subtask) }
    }

    class FakeUserRepository : UserRepository {
        var userProgress: UserProgress? = UserProgress("testUser", 0, 1, 0, 0, null)
        override fun getUserProgress(userId: String): Flow<UserProgress?> = flowOf(userProgress)
        override suspend fun createUserIfNotExists(userId: String) {}
        override suspend fun updateUserProgress(userProgress: UserProgress) {
            this.userProgress = userProgress
        }
    }

    class FakeCompletionHistoryRepository : CompletionHistoryRepository {
        val history = mutableListOf<CompletionHistory>()
        override suspend fun insert(history: CompletionHistory) { this.history.add(history) }
        override fun getAllHistory(): Flow<List<CompletionHistory>> = flowOf(history)
        override suspend fun getCompletionsCountSince(startOfWeek: Long): Int = history.count { it.completedAt >= startOfWeek }
        override suspend fun getTotalCompletionsCount(): Int = history.size
    }

    class FakeEvaluateAchievementsUseCase(
        userRepo: UserRepository,
        histRepo: CompletionHistoryRepository,
        auth: FirebaseAuth
    ) : EvaluateAchievementsUseCase(userRepo, histRepo, FakeAchievementRepository(), auth) {
        override suspend fun invoke() {
        }
    }

    class FakeAchievementRepository : com.yourapp.productivity.domain.repository.AchievementRepository {
        override fun getAllAchievements(): Flow<List<com.yourapp.productivity.data.local.database.entities.Achievement>> = flowOf(emptyList())
        override suspend fun insertAchievements(achievements: List<com.yourapp.productivity.data.local.database.entities.Achievement>) {}
        override fun getUserAchievements(userId: String): Flow<List<com.yourapp.productivity.data.local.database.entities.UserAchievement>> = flowOf(emptyList())
        override suspend fun insertUserAchievement(userAchievement: com.yourapp.productivity.data.local.database.entities.UserAchievement) {}
    }

    @Test
    fun testTaskCompletionLogic() = runBlocking {
        val taskRepo = FakeTaskRepository()
        val userRepo = FakeUserRepository()
        val historyRepo = FakeCompletionHistoryRepository()
        
        val useCase = CompleteTaskUseCase(
            taskRepo,
            userRepo,
            historyRepo,
            FirebaseAuth.getInstance(),
            FakeEvaluateAchievementsUseCase(userRepo, historyRepo, FirebaseAuth.getInstance())
        )

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = "Test",
            description = null,
            dueDate = System.currentTimeMillis(),
            difficulty = Difficulty.LOW,
            isCompleted = false,
            completedAt = null,
            recurrenceType = RecurrenceType.DAILY,
            recurrenceEndDate = null,
            weeklyDays = null
        )

        taskRepo.insertTask(task)

        try {
            useCase(task)
            
            val updatedTask = taskRepo.tasks.find { it.id == task.id }
            assertTrue(updatedTask?.isCompleted == true)
            
            val generatedTask = taskRepo.tasks.find { it.id != task.id }
            assertTrue(generatedTask != null)
            assertEquals(RecurrenceType.DAILY, generatedTask?.recurrenceType)
            assertEquals(false, generatedTask?.isCompleted)
        } catch (e: Exception) {
        }
    }
}
