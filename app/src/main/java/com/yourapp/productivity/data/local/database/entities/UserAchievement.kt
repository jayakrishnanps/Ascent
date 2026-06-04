package com.yourapp.productivity.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_achievements")
data class UserAchievement(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val achievementId: String,
    val earnedAt: Long
)
