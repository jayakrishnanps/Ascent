package com.yourapp.productivity.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yourapp.productivity.data.local.database.entities.Task
import kotlinx.coroutines.flow.Flow

import androidx.room.Transaction
import com.yourapp.productivity.data.local.database.entities.TaskWithSubtasksRelation

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY startDate ASC")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (startDate <= :todayEnd OR startDate IS NULL) ORDER BY startDate ASC")
    fun getTasksForToday(todayEnd: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND startDate > :todayEnd ORDER BY startDate ASC")
    fun getUpcomingTasks(todayEnd: Long): Flow<List<Task>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (startDate <= :todayEnd OR startDate IS NULL) ORDER BY startDate ASC")
    fun getTasksWithSubtasksForToday(todayEnd: Long): Flow<List<TaskWithSubtasksRelation>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND startDate > :todayEnd ORDER BY startDate ASC")
    fun getUpcomingTasksWithSubtasks(todayEnd: Long): Flow<List<TaskWithSubtasksRelation>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
