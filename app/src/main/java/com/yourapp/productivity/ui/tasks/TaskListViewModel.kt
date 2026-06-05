package com.yourapp.productivity.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.model.Difficulty
import com.yourapp.productivity.domain.model.RecurrenceType
import com.yourapp.productivity.domain.repository.TaskRepository
import com.yourapp.productivity.domain.utils.DateUtils
import com.yourapp.productivity.domain.usecase.CompleteTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskListUiState(
    val isLoading: Boolean = false,
    val todayTasks: List<Task> = listOf(
        Task(id = "1", title = "Morning Meditation", description = "10 minutes of mindfulness", dueDate = System.currentTimeMillis(), difficulty = Difficulty.EASY, isCompleted = false, completedAt = null, recurrenceType = RecurrenceType.DAILY, recurrenceEndDate = null, weeklyDays = null),
        Task(id = "2", title = "Workout", description = "Gym session", dueDate = System.currentTimeMillis(), difficulty = Difficulty.HARD, isCompleted = false, completedAt = null, recurrenceType = RecurrenceType.DAILY, recurrenceEndDate = null, weeklyDays = null),
        Task(id = "3", title = "Read 20 pages", description = "Book: Clean Code", dueDate = System.currentTimeMillis(), difficulty = Difficulty.MEDIUM, isCompleted = false, completedAt = null, recurrenceType = RecurrenceType.DAILY, recurrenceEndDate = null, weeklyDays = null)
    ),
    val upcomingTasks: List<Task> = listOf(
        Task(id = "4", title = "Weekly Review", description = "Plan next week", dueDate = System.currentTimeMillis() + 86400000, difficulty = Difficulty.MEDIUM, isCompleted = false, completedAt = null, recurrenceType = RecurrenceType.WEEKLY, recurrenceEndDate = null, weeklyDays = null)
    ),
    val error: String? = null
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        // loadTasks() // Temporarily disabled for mock data
    }

    private fun loadTasks() {
        val todayEnd = DateUtils.getEndOfToday()
        
        viewModelScope.launch {
            try {
                taskRepository.getTasksForToday(todayEnd).collect { todayList ->
                    _uiState.value = _uiState.value.copy(
                        todayTasks = todayList,
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
                    _uiState.value = _uiState.value.copy(
                        upcomingTasks = upcomingList,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            try {
                completeTaskUseCase(task)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
