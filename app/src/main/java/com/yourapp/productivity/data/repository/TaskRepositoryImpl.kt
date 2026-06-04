package com.yourapp.productivity.data.repository

import com.yourapp.productivity.data.local.database.dao.TaskDao
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()

    override fun getTasksForToday(todayEnd: Long): Flow<List<Task>> = taskDao.getTasksForToday(todayEnd)

    override fun getUpcomingTasks(todayEnd: Long): Flow<List<Task>> = taskDao.getUpcomingTasks(todayEnd)

    override suspend fun getTaskById(taskId: String): Task? = taskDao.getTaskById(taskId)

    override suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    override suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
}
