package com.yourapp.productivity.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey
    val userId: String,
    val totalXp: Int,
    val currentLevel: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletionDate: Long?,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)
