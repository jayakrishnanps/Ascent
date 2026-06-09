package com.yourapp.productivity.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.data.local.database.entities.Achievement
import com.yourapp.productivity.data.local.database.entities.UserProgress
import com.yourapp.productivity.data.local.datastore.PreferencesDataStore
import com.yourapp.productivity.domain.repository.AchievementRepository
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import com.yourapp.productivity.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.yourapp.productivity.data.local.database.AppDatabase
import javax.inject.Inject

data class ProfileUiState(
    val userProgress: UserProgress? = UserProgress(
        userId = "mock_user",
        totalXp = 1250,
        currentLevel = 13,
        currentStreak = 5,
        longestStreak = 12,
        lastCompletionDate = System.currentTimeMillis()
    ),
    val totalTasksCompleted: Int = 42,
    val tasksCompletedThisWeek: Int = 12,
    val allAchievements: List<Achievement> = listOf(
        Achievement("1", "Early Bird", "Complete 5 tasks before 8 AM", "early_bird", 5, 0),
        Achievement("2", "Productivity Ninja", "Complete 10 tasks in a day", "daily_tasks", 10, 0),
        Achievement("3", "Streak Master", "Maintain a 7-day streak", "streak", 7, 0),
        Achievement("4", "Task Master", "Complete 50 tasks total", "total_tasks", 50, 0)
    ),
    val earnedAchievementIds: Set<String> = setOf("1", "2"),
    val themeMode: String = "SYSTEM",
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val achievementRepository: AchievementRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
        observeTheme()
    }

    private fun loadProfileData() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            userRepository.getUserProgress(userId).collect { progress ->
                if (progress != null) {
                    _uiState.value = _uiState.value.copy(userProgress = progress)
                }
            }
        }

        viewModelScope.launch {
            val total = completionHistoryRepository.getTotalCompletionsCount()
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val thisWeek = completionHistoryRepository.getCompletionsCountSince(weekAgo)
            
            _uiState.value = _uiState.value.copy(
                totalTasksCompleted = total,
                tasksCompletedThisWeek = thisWeek
            )
        }

        viewModelScope.launch {
            achievementRepository.getAllAchievements().collect { achievements ->
                _uiState.value = _uiState.value.copy(allAchievements = achievements)
            }
        }

        viewModelScope.launch {
            achievementRepository.getUserAchievements(userId).collect { earned ->
                _uiState.value = _uiState.value.copy(
                    earnedAchievementIds = earned.map { it.achievementId }.toSet(),
                    isLoading = false
                )
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            preferencesDataStore.themeMode.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(mode)
        }
    }
    
    fun signOut() {
        firebaseAuth.signOut()
    }
    
    fun deleteAccount(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            appDatabase.clearAllTables()
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                onError(e)
                            }
                        }
                    }
                } else {
                    onError(task.exception ?: Exception("Failed to delete user account"))
                }
            }
        } else {
            onError(Exception("No authenticated user found"))
        }
    }
}
