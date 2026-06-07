package com.yourapp.productivity.domain.repository

import com.yourapp.productivity.data.local.database.entities.Subtask
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
    
    fun getSubtasksForTask(taskId: String): Flow<List<Subtask>>
    suspend fun getSubtaskById(subtaskId: String): Subtask?
    suspend fun insertSubtask(subtask: Subtask)
    suspend fun updateSubtask(subtask: Subtask)
    suspend fun deleteSubtask(subtask: Subtask)
}
