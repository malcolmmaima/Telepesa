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
package com.telepesa.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telepesa.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Onboarding feature.
 * Follows the Single Responsibility Principle by managing only onboarding state.
 * Implements the Dependency Inversion Principle through repository injection.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /**
     * Navigate to next onboarding page.
     */
    fun nextPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage < 2) {
            _uiState.value = _uiState.value.copy(currentPage = currentPage + 1)
            Timber.d("Onboarding: Navigated to page ${currentPage + 1}")
        }
    }

    /**
     * Navigate to previous onboarding page.
     */
    fun previousPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage > 0) {
            _uiState.value = _uiState.value.copy(currentPage = currentPage - 1)
            Timber.d("Onboarding: Navigated to page ${currentPage - 1}")
        }
    }

    /**
     * Complete onboarding and mark it as completed.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Mark onboarding as completed in preferences
                // This would typically be handled by a use case or repository
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOnboardingCompleted = true,
                )
                Timber.d("Onboarding: Completed successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to complete onboarding",
                )
                Timber.e(e, "Onboarding: Failed to complete")
            }
        }
    }

    /**
     * Skip onboarding.
     */
    fun skipOnboarding() {
        completeOnboarding()
    }

    /**
     * Clear any error state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for Onboarding screen.
 * Follows the Open/Closed Principle by allowing extension without modification.
 */
data class OnboardingUiState(
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val error: String? = null,
)
