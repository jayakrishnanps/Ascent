package com.yourapp.productivity.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.yourapp.productivity.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _userProfile.value = UserProfile(
                uid = currentUser.uid,
                displayName = currentUser.displayName,
                email = currentUser.email,
                photoUrl = currentUser.photoUrl?.toString()
            )
            
            // Ensure the local database has a record for the user so gamification works
            viewModelScope.launch {
                userRepository.createUserIfNotExists(
                    userId = currentUser.uid,
                    displayName = currentUser.displayName,
                    email = currentUser.email,
                    photoUrl = currentUser.photoUrl?.toString()
                )
            }
            
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
    }

    fun handleGoogleSignInResult(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkCurrentUser()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = task.exception?.message ?: "Authentication failed"
                    )
                }
            }
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    fun signOut() {
        firebaseAuth.signOut()
        _userProfile.value = null
        _uiState.value = _uiState.value.copy(isAuthenticated = false, error = null)
    }
}
