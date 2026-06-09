package com.yourapp.productivity.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.productivity.data.local.database.entities.Subtask
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
    val startDate: Long? = null,
    val difficulty: Difficulty = Difficulty.LOW,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val weeklyDays: Set<Int> = emptySet(),
    val recurrenceEndDate: Long? = null,
    val subtasks: List<Subtask> = emptyList(),
    val newSubtaskTitle: String = "",
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
    private val generatedTaskId = taskId ?: UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init {
        taskId?.let { loadTask(it) }
    }

    private fun loadTask(id: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(id)
            if (task != null) {
                taskRepository.getSubtasksForTask(id).collect { subtasks ->
                    _uiState.value = _uiState.value.copy(
                        title = task.title,
                        description = task.description ?: "",
                        startDate = task.startDate,
                        difficulty = task.difficulty,
                        recurrenceType = task.recurrenceType,
                        weeklyDays = task.weeklyDays?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
                        recurrenceEndDate = task.recurrenceEndDate,
                        subtasks = subtasks
                    )
                }
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

    fun updateStartDate(startDate: Long?) {
        val adjusted = startDate?.let {
            val tz = java.util.TimeZone.getDefault()
            it - tz.getOffset(it)
        }
        _uiState.value = _uiState.value.copy(startDate = adjusted)
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
        val adjusted = endDate?.let {
            val tz = java.util.TimeZone.getDefault()
            it - tz.getOffset(it)
        }
        _uiState.value = _uiState.value.copy(recurrenceEndDate = adjusted)
    }

    fun updateNewSubtaskTitle(title: String) {
        _uiState.value = _uiState.value.copy(newSubtaskTitle = title)
    }

    fun addSubtask() {
        val title = _uiState.value.newSubtaskTitle.trim()
        if (title.isNotEmpty()) {
            val newSubtask = Subtask(taskId = generatedTaskId, title = title)
            val currentSubtasks = _uiState.value.subtasks.toMutableList()
            currentSubtasks.add(newSubtask)
            _uiState.value = _uiState.value.copy(subtasks = currentSubtasks, newSubtaskTitle = "")
        }
    }

    fun removeSubtask(subtask: Subtask) {
        val currentSubtasks = _uiState.value.subtasks.toMutableList()
        currentSubtasks.remove(subtask)
        _uiState.value = _uiState.value.copy(subtasks = currentSubtasks)
        if (taskId != null) {
            viewModelScope.launch {
                taskRepository.deleteSubtask(subtask)
            }
        }
    }

    fun saveTask() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) return
        
        _uiState.value = currentState.copy(isSaving = true)
        
        val weeklyDaysString = if (currentState.recurrenceType == RecurrenceType.WEEKLY && currentState.weeklyDays.isNotEmpty()) {
            currentState.weeklyDays.joinToString(",")
        } else null

        var initialStartDate = currentState.startDate
        if (currentState.recurrenceType == RecurrenceType.WEEKLY && currentState.weeklyDays.isNotEmpty() && initialStartDate != null) {
            // Check if initialStartDate is on a selected day
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = initialStartDate
            val currentDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0)
            
            if (!currentState.weeklyDays.contains(currentDayOfWeek)) {
                // Find next valid day
                for (i in 1..7) {
                    val nextDay = (currentDayOfWeek + i) % 7
                    if (currentState.weeklyDays.contains(nextDay)) {
                        cal.add(java.util.Calendar.DAY_OF_YEAR, i)
                        initialStartDate = cal.timeInMillis
                        break
                    }
                }
            }
        }

        val task = Task(
            id = generatedTaskId,
            title = currentState.title,
            description = currentState.description.ifBlank { null },
            startDate = initialStartDate,
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
            
            currentState.subtasks.forEach { subtask ->
                taskRepository.insertSubtask(subtask)
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
