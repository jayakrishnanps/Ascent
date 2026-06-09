package com.yourapp.productivity.data.local.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubtasksRelation(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<Subtask>
)
