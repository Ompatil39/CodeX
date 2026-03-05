package com.snapbudget.ocr.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapbudget.ocr.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.launch

/**
 * Manages authentication UI state for [LoginActivity].
 *
 * Uses a sealed [AuthState] class so the UI can react declaratively
 * to loading, success, and error states.
 */
class AuthViewModel : ViewModel() {

    private val authRepo = FirebaseAuthRepository()

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    /** True when a network call is in flight. */
    val isLoading: Boolean
        get() = _authState.value is AuthState.Loading

    // ─── Actions ─────────────────────────────────────────────────────────

    fun register(email: String, password: String, displayName: String) {
        if (isLoading) return
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepo.register(email, password, displayName)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success },
                onFailure = { AuthState.Error(it.localizedMessage ?: "Registration failed") }
            )
        }
    }

    fun login(email: String, password: String) {
        if (isLoading) return
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepo.login(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success },
                onFailure = { AuthState.Error(it.localizedMessage ?: "Login failed") }
            )
        }
    }

    fun logout() {
        authRepo.logout()
        _authState.value = AuthState.Idle
    }

    /** Reset to Idle so the UI can clear previous error messages. */
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // ─── State ───────────────────────────────────────────────────────────

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
