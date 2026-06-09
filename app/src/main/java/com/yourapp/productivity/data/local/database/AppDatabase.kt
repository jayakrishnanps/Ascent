package com.yourapp.productivity.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourapp.productivity.data.local.database.dao.AchievementDao
import com.yourapp.productivity.data.local.database.dao.CompletionHistoryDao
import com.yourapp.productivity.data.local.database.dao.TaskDao
import com.yourapp.productivity.data.local.database.dao.UserDao
import com.yourapp.productivity.data.local.database.dao.SubtaskDao
import com.yourapp.productivity.data.local.database.entities.Achievement
import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import com.yourapp.productivity.data.local.database.entities.Subtask
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.data.local.database.entities.UserAchievement
import com.yourapp.productivity.data.local.database.entities.UserProgress

@Database(
    entities = [
        Task::class,
        Subtask::class,
        CompletionHistory::class,
        UserProgress::class,
        Achievement::class,
        UserAchievement::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun completionHistoryDao(): CompletionHistoryDao
    abstract fun userDao(): UserDao
    abstract fun achievementDao(): AchievementDao
}
