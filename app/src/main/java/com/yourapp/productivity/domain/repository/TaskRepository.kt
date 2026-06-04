package com.yourapp.productivity.domain.repository

import com.yourapp.productivity.data.local.database.entities.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getPendingTasks(): Flow<List<Task>>
    fun getTasksForToday(todayEnd: Long): Flow<List<Task>>
    fun getUpcomingTasks(todayEnd: Long): Flow<List<Task>>
    suspend fun getTaskById(taskId: String): Task?
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
}
