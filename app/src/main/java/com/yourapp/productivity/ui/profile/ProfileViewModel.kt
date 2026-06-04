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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProgress: UserProgress? = null,
    val totalTasksCompleted: Int = 0,
    val tasksCompletedThisWeek: Int = 0,
    val allAchievements: List<Achievement> = emptyList(),
    val earnedAchievementIds: Set<String> = emptySet(),
    val themeMode: String = "SYSTEM",
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val achievementRepository: AchievementRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val firebaseAuth: FirebaseAuth
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
                _uiState.value = _uiState.value.copy(userProgress = progress)
            }
        }

        viewModelScope.launch {
            val total = completionHistoryRepository.getTotalCompletionsCount()
            // Simplified "this week" calculation for MVP - just taking last 7 days
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
}
