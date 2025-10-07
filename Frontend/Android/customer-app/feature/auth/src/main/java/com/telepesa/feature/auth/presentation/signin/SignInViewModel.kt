/*
 * Copyright (c) 2025 Telepesa. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.telepesa.feature.auth.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telepesa.core.common.domain.Result
import com.telepesa.core.domain.model.LoginRequest
import com.telepesa.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Sign In feature.
 * Follows the Single Responsibility Principle by managing only sign-in state and logic.
 * Implements the Dependency Inversion Principle through repository injection.
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    /**
     * Sign in with email/username and password.
     * Implements the Single Responsibility Principle by focusing only on authentication.
     */
    fun signIn(email: String, password: String) {
        // Validate input
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        _uiState.value = _uiState.value.copy(
            emailError = emailError,
            passwordError = passwordError,
        )

        if (emailError != null || passwordError != null) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
            )

            try {
                val loginRequest = LoginRequest(
                    usernameOrEmail = email.trim(),
                    password = password,
                )

                val result = authRepository.login(loginRequest)
                when (result) {
                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is Result.Success<*> -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignInSuccessful = true,
                        )
                        Timber.d("SignIn: Successful authentication")
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exception?.message ?: "Sign in failed. Please try again.",
                        )
                        Timber.e("SignIn: Authentication failed - ${result.exception}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again.",
                )
                Timber.e(e, "SignIn: Unexpected error during authentication")
            }
        }
    }

    /**
     * Clear any error state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear email error.
     */
    fun clearEmailError() {
        _uiState.value = _uiState.value.copy(emailError = null)
    }

    /**
     * Clear password error.
     */
    fun clearPasswordError() {
        _uiState.value = _uiState.value.copy(passwordError = null)
    }

    /**
     * Validate email or username input.
     * Follows the Single Responsibility Principle by focusing only on email validation.
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email or username is required"
            email.length < 3 -> "Email or username must be at least 3 characters"
            else -> null
        }
    }

    /**
     * Validate password input.
     * Follows the Single Responsibility Principle by focusing only on password validation.
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
}

/**
 * UI state for Sign In screen.
 * Follows the Open/Closed Principle by allowing extension without modification.
 */
data class SignInUiState(
    val isLoading: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
)
