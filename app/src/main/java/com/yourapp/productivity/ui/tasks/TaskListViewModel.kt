package com.yourapp.productivity.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.productivity.data.local.database.entities.Subtask
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.model.TaskWithSubtasks
import com.yourapp.productivity.domain.repository.TaskRepository
import com.yourapp.productivity.domain.utils.DateUtils
import com.yourapp.productivity.domain.usecase.CompleteTaskUseCase
import com.yourapp.productivity.domain.usecase.ToggleSubtaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskListUiState(
    val isLoading: Boolean = false,
    val todayTasks: List<TaskWithSubtasks> = emptyList(),
    val upcomingTasks: List<TaskWithSubtasks> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val toggleSubtaskUseCase: ToggleSubtaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState(isLoading = true))
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        val todayEnd = DateUtils.getEndOfToday()
        
        viewModelScope.launch {
            try {
                taskRepository.getTasksForToday(todayEnd).collect { todayList ->
                    val withSubtasks = todayList.map { task ->
                        val subtasks = taskRepository.getSubtasksForTask(task.id).first()
                        TaskWithSubtasks(task, subtasks)
                    }
                    _uiState.value = _uiState.value.copy(
                        todayTasks = withSubtasks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }

        viewModelScope.launch {
            try {
                taskRepository.getUpcomingTasks(todayEnd).collect { upcomingList ->
                    val withSubtasks = upcomingList.map { task ->
                        val subtasks = taskRepository.getSubtasksForTask(task.id).first()
                        TaskWithSubtasks(task, subtasks)
                    }
                    _uiState.value = _uiState.value.copy(
                        upcomingTasks = withSubtasks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun completeTask(taskWithSubtasks: TaskWithSubtasks) {
        val allSubtasksCompleted = taskWithSubtasks.subtasks.all { it.isCompleted }
        if (!allSubtasksCompleted) return
        
        viewModelScope.launch {
            try {
                completeTaskUseCase(taskWithSubtasks.task)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun toggleSubtask(task: Task, subtask: Subtask) {
        viewModelScope.launch {
            try {
                toggleSubtaskUseCase(task, subtask)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
