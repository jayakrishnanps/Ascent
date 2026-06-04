package com.yourapp.productivity.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "completion_history")
data class CompletionHistory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val completedAt: Long,
    val xpEarned: Int
)
