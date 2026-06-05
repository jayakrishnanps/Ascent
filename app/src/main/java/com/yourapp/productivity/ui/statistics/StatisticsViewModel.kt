package com.yourapp.productivity.ui.statistics

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class StatisticsUiState(
    val totalTasksCompleted: Int = 42,
    val completionRate: Float = 0.85f,
    val xpGainedThisWeek: Int = 350,
    val bestStreak: Int = 12,
    val weeklyActivity: List<Float> = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.3f, 0.6f, 0.8f) // Mock activity for Mon-Sun
)

@HiltViewModel
class StatisticsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
}
