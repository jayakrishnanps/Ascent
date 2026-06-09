package com.yourapp.productivity.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.productivity.data.local.database.entities.Achievement
import com.yourapp.productivity.data.local.database.entities.AchievementConditionType
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.repository.AchievementRepository
import com.yourapp.productivity.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import java.util.UUID

data class AddEditAchievementUiState(
    val name: String = "",
    val description: String = "",
    val conditionType: AchievementConditionType = AchievementConditionType.COMPLETE_N_TIMES,
    val targetValue: String = "10",
    val availableTasks: List<Task> = emptyList(),
    val selectedTaskId: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditAchievementViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditAchievementUiState())
    val uiState: StateFlow<AddEditAchievementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pendingTasks = taskRepository.getPendingTasks().first()
            _uiState.value = _uiState.value.copy(availableTasks = pendingTasks)
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateConditionType(type: AchievementConditionType) {
        _uiState.value = _uiState.value.copy(conditionType = type)
    }

    fun updateTargetValue(value: String) {
        _uiState.value = _uiState.value.copy(targetValue = value)
    }

    fun updateSelectedTask(taskId: String?) {
        _uiState.value = _uiState.value.copy(selectedTaskId = taskId)
    }

    fun saveAchievement(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Name cannot be empty")
            return
        }
        if (state.selectedTaskId == null) {
            _uiState.value = state.copy(error = "Must select a task")
            return
        }
        val target = state.targetValue.toIntOrNull()
        if (state.conditionType == AchievementConditionType.COMPLETE_N_TIMES && (target == null || target <= 0)) {
            _uiState.value = state.copy(error = "Target value must be a positive integer")
            return
        }

        _uiState.value = state.copy(isSaving = true, error = null)

        val newAchievement = Achievement(
            id = UUID.randomUUID().toString(),
            name = state.name.trim(),
            description = state.description.trim(),
            taskId = state.selectedTaskId,
            conditionType = state.conditionType,
            targetValue = target ?: 1,
            currentProgress = 0,
            isEarned = false
        )

        viewModelScope.launch {
            achievementRepository.insertAchievement(newAchievement)
            onSuccess()
        }
    }
}
