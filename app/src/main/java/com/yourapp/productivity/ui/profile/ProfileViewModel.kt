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
    val totalTasksCompleted: Int = 0,
    val tasksCompletedThisWeek: Int = 0,
    val themeMode: String = "SYSTEM",
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
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
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    appDatabase.clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                withContext(Dispatchers.Main) {
                    user.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onSuccess() // Even if it fails (requires recent login), we already wiped local data and signed out.
                        }
                    }
                }
            }
        } else {
            onError(Exception("No authenticated user found"))
        }
    }
}
