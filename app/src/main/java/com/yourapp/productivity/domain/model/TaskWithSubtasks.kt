package com.yourapp.productivity.domain.model

import com.yourapp.productivity.data.local.database.entities.Subtask
import com.yourapp.productivity.data.local.database.entities.Task

data class TaskWithSubtasks(
    val task: Task,
    val subtasks: List<Subtask>
)
