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
import com.yourapp.productivity.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.pow
import javax.inject.Inject

data class TaskListUiState(
    val isLoading: Boolean = false,
    val todayTasks: List<TaskWithSubtasks> = emptyList(),
    val upcomingTasks: List<TaskWithSubtasks> = emptyList(),
    val error: String? = null,
    val userLevel: Int = 1,
    val userXpProgress: Float = 0f
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val toggleSubtaskUseCase: ToggleSubtaskUseCase,
    private val evaluateMissedTasksUseCase: com.yourapp.productivity.domain.usecase.EvaluateMissedTasksUseCase,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState(isLoading = true))
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            evaluateMissedTasksUseCase()
            loadTasks()
            loadUserProgress()
        }
    }

    private fun loadUserProgress() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserProgress(userId).collect { progress ->
                if (progress != null) {
                    val currentLevelXp = if (progress.currentLevel == 1) 0 else ((progress.currentLevel - 1) * 100 * 1.5.pow((progress.currentLevel - 2).toDouble())).toInt()
                    val nextLevelXp = (progress.currentLevel * 100 * 1.5.pow((progress.currentLevel - 1).toDouble())).toInt()
                    
                    val xpIntoLevel = progress.totalXp - currentLevelXp
                    val xpRequiredForNextLevel = nextLevelXp - currentLevelXp
                    val xpProgress = if (xpRequiredForNextLevel > 0) {
                        (xpIntoLevel.toFloat() / xpRequiredForNextLevel.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    _uiState.value = _uiState.value.copy(
                        userLevel = progress.currentLevel,
                        userXpProgress = xpProgress
                    )
                }
            }
        }
    }

    private fun loadTasks() {
        val todayEnd = DateUtils.getEndOfToday()
        
        viewModelScope.launch {
            try {
                taskRepository.getTasksWithSubtasksForToday(todayEnd).collect { todayList ->
                    val withSubtasks = todayList.map { relation ->
                        TaskWithSubtasks(relation.task, relation.subtasks)
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
                taskRepository.getUpcomingTasksWithSubtasks(todayEnd).collect { upcomingList ->
                    val withSubtasks = upcomingList.map { relation ->
                        TaskWithSubtasks(relation.task, relation.subtasks)
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
