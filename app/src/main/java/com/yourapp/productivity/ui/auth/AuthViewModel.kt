package com.yourapp.productivity.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.GoogleAuthProvider
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
        // --- GOOGLE SIGN-IN DISABLED ---
        // Temporarily bypassing real Firebase Authentication
        // to unblock development since the emulator lacks a Google account.
        /*
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _userProfile.value = UserProfile(
                uid = currentUser.uid,
                displayName = currentUser.displayName,
                email = currentUser.email,
                photoUrl = currentUser.photoUrl?.toString()
            )
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
        */

        // --- MOCK USER DATA INJECTION ---
        val mockUserId = "mock_user_123"
        val mockDisplayName = "Hero User"
        val mockEmail = "hero@example.com"
        val mockPhotoUrl = null // Use placeholder avatar

        _userProfile.value = UserProfile(
            uid = mockUserId,
            displayName = mockDisplayName,
            email = mockEmail,
            photoUrl = mockPhotoUrl
        )

        // Ensure the local database has a record for our mock user so gamification works
        viewModelScope.launch {
            userRepository.createUserIfNotExists(
                userId = mockUserId,
                displayName = mockDisplayName,
                email = mockEmail,
                photoUrl = mockPhotoUrl
            )
        }

        // Force app to believe we are authenticated
        _uiState.value = _uiState.value.copy(isAuthenticated = true)
    }

    fun handleGoogleSignInResult(idToken: String) {
        // --- GOOGLE SIGN-IN DISABLED ---
        // We will never hit this while bypassed, but let's safely mock it just in case.
        /*
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                // ... Firebase logic ...
            }
        */
        _uiState.value = _uiState.value.copy(isAuthenticated = true)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}
