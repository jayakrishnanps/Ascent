package com.yourapp.productivity.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatisticsUiState(
    val totalTasksCompleted: Int = 0,
    val completionRate: Float = 0f,
    val xpGainedThisWeek: Int = 0,
    val bestStreak: Int = 0,
    val weeklyActivity: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            combine(
                userRepository.getUserProgress(userId),
                completionHistoryRepository.getAllHistory()
            ) { progress, history ->
                val bestStreak = progress?.longestStreak ?: 0

                val completedTasks = history.count { !it.isMissed }
                val missedTasks = history.count { it.isMissed }
                val totalTasks = completedTasks + missedTasks
                val completionRate = if (totalTasks > 0) {
                    completedTasks.toFloat() / totalTasks.toFloat()
                } else {
                    0f
                }

                // Calculate this week's start time (Monday)
                val cal = Calendar.getInstance()
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }
                val startOfWeek = cal.timeInMillis

                val historyThisWeek = history.filter { it.completedAt >= startOfWeek }
                
                // xp gained this week can be negative if penalties exceed gains, but let's just sum it
                val xpGainedThisWeek = historyThisWeek.sumOf { it.xpEarned }

                val activityCounts = MutableList(7) { 0 }
                historyThisWeek.filter { !it.isMissed }.forEach { record ->
                    val recordCal = Calendar.getInstance().apply { timeInMillis = record.completedAt }
                    // Map Sunday (1) to 6, Monday (2) to 0, Tuesday (3) to 1, etc.
                    val dayOfWeek = (recordCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    activityCounts[dayOfWeek]++
                }

                val maxActivity = activityCounts.maxOrNull()?.toFloat() ?: 0f
                val normalizedActivity = activityCounts.map {
                    if (maxActivity > 0) it.toFloat() / maxActivity else 0f
                }

                StatisticsUiState(
                    totalTasksCompleted = completedTasks,
                    completionRate = completionRate,
                    xpGainedThisWeek = xpGainedThisWeek,
                    bestStreak = bestStreak,
                    weeklyActivity = normalizedActivity
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
