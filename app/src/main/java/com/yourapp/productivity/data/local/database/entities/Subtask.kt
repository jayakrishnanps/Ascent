package com.yourapp.productivity.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subtasks")
data class Subtask(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
