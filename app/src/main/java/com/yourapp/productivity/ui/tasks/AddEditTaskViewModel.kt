package com.yourapp.productivity.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.model.Difficulty
import com.yourapp.productivity.domain.model.RecurrenceType
import com.yourapp.productivity.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val difficulty: Difficulty = Difficulty.EASY,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val weeklyDays: Set<Int> = emptySet(),
    val recurrenceEndDate: Long? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String? = savedStateHandle["taskId"]

    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init {
        taskId?.let { loadTask(it) }
    }

    private fun loadTask(id: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(id)
            if (task != null) {
                _uiState.value = AddEditTaskUiState(
                    title = task.title,
                    description = task.description ?: "",
                    dueDate = task.dueDate,
                    difficulty = task.difficulty,
                    recurrenceType = task.recurrenceType,
                    weeklyDays = task.weeklyDays?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
                    recurrenceEndDate = task.recurrenceEndDate
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateDifficulty(difficulty: Difficulty) {
        _uiState.value = _uiState.value.copy(difficulty = difficulty)
    }

    fun updateDueDate(dueDate: Long?) {
        _uiState.value = _uiState.value.copy(dueDate = dueDate)
    }

    fun updateRecurrenceType(type: RecurrenceType) {
        _uiState.value = _uiState.value.copy(recurrenceType = type)
    }

    fun toggleWeeklyDay(day: Int) {
        val currentDays = _uiState.value.weeklyDays.toMutableSet()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(weeklyDays = currentDays)
    }

    fun updateRecurrenceEndDate(endDate: Long?) {
        _uiState.value = _uiState.value.copy(recurrenceEndDate = endDate)
    }

    fun saveTask() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) return
        
        _uiState.value = currentState.copy(isSaving = true)
        
        val weeklyDaysString = if (currentState.recurrenceType == RecurrenceType.WEEKLY && currentState.weeklyDays.isNotEmpty()) {
            currentState.weeklyDays.joinToString(",")
        } else null

        val task = Task(
            id = taskId ?: UUID.randomUUID().toString(),
            title = currentState.title,
            description = currentState.description.ifBlank { null },
            dueDate = currentState.dueDate,
            difficulty = currentState.difficulty,
            isCompleted = false,
            completedAt = null,
            recurrenceType = currentState.recurrenceType,
            recurrenceEndDate = currentState.recurrenceEndDate,
            weeklyDays = weeklyDaysString
        )

        viewModelScope.launch {
            if (taskId == null) {
                taskRepository.insertTask(task)
            } else {
                taskRepository.updateTask(task)
            }
            _uiState.value = currentState.copy(isSaving = false, isSaved = true)
        }
    }

    fun deleteTask() {
        if (taskId == null) return
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isDeleting = true)
        
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            if (task != null) {
                taskRepository.deleteTask(task)
            }
            _uiState.value = currentState.copy(isDeleting = false, isDeleted = true)
        }
    }
}
