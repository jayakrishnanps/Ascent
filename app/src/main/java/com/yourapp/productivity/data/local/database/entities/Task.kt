package com.yourapp.productivity.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourapp.productivity.domain.model.Difficulty
import com.yourapp.productivity.domain.model.RecurrenceType
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val difficulty: Difficulty,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val recurrenceType: RecurrenceType,
    val recurrenceEndDate: Long?,
    val weeklyDays: String?
)
