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
package com.telepesa.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telepesa.core.domain.model.Account
import com.telepesa.core.domain.model.Transaction
import com.telepesa.core.domain.model.User
import com.telepesa.core.domain.repository.AccountRepository
import com.telepesa.core.domain.repository.AuthRepository
import com.telepesa.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

/**
 * ViewModel for Home feature.
 * Follows the Single Responsibility Principle by managing only home dashboard state.
 * Implements the Dependency Inversion Principle through repository injection.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Load home dashboard data.
     * Implements the Single Responsibility Principle by focusing only on data loading.
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load user data
                loadUserData()

                // Load account data
                loadAccountData()

                // Load recent transactions
                loadRecentTransactions()

                // Load statistics
                loadStatistics()

                _uiState.value = _uiState.value.copy(isLoading = false)
                Timber.d("Home: Data loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data. Please try again.",
                )
                Timber.e(e, "Home: Failed to load data")
            }
        }
    }

    /**
     * Refresh home data.
     */
    fun refreshData() {
        loadHomeData()
    }

    /**
     * Clear any error state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private suspend fun loadUserData() {
        // TODO: Implement getAuthenticatedUser method in AuthRepository
        // authRepository.getAuthenticatedUser().collect { result ->
        //     when (result) {
        //         is Result.Success -> {
        //             _uiState.value = _uiState.value.copy(user = result.data)
        //         }
        //         is Result.Error -> {
        //             Timber.e("Home: Failed to load user data - ${result.exception}")
        //         }
        //         is Result.Loading -> {
        //             // Handle loading state if needed
        //         }
        //     }
        // }
    }

    private suspend fun loadAccountData() {
        // For now, we'll use a mock account. In real implementation, this would come from the repository
        val mockAccount = Account(
            id = 1,
            accountNumber = "1234567890",
            accountType = com.telepesa.core.domain.model.AccountType.SAVINGS,
            balance = BigDecimal.valueOf(15000.0),
            availableBalance = BigDecimal.valueOf(14800.0),
            currency = "KES",
            status = com.telepesa.core.domain.model.AccountStatus.ACTIVE,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
        )

        _uiState.value = _uiState.value.copy(primaryAccount = mockAccount)
    }

    private suspend fun loadRecentTransactions() {
        // For now, we'll use mock transactions. In real implementation, this would come from the repository
        val mockTransactions = listOf(
            Transaction(
                id = 1,
                accountId = 1,
                type = com.telepesa.core.domain.model.TransactionType.CREDIT,
                category = com.telepesa.core.domain.model.TransactionCategory.DEPOSIT,
                amount = BigDecimal.valueOf(5000.0),
                balance = BigDecimal.valueOf(15000.0),
                description = "Salary Deposit",
                reference = "SAL001",
                status = com.telepesa.core.domain.model.TransactionStatus.COMPLETED,
                recipientName = null,
                recipientAccount = null,
                charges = BigDecimal.valueOf(0.0),
                createdAt = "2024-01-15T09:00:00Z",
            ),
            Transaction(
                id = 2,
                accountId = 1,
                type = com.telepesa.core.domain.model.TransactionType.DEBIT,
                category = com.telepesa.core.domain.model.TransactionCategory.TRANSFER,
                amount = BigDecimal.valueOf(200.0),
                balance = BigDecimal.valueOf(14800.0),
                description = "Transfer to John Doe",
                reference = "TRF001",
                status = com.telepesa.core.domain.model.TransactionStatus.COMPLETED,
                recipientName = "John Doe",
                recipientAccount = "0987654321",
                charges = BigDecimal.valueOf(10.0),
                createdAt = "2024-01-14T14:30:00Z",
            ),
        )

        _uiState.value = _uiState.value.copy(recentTransactions = mockTransactions)
    }

    private fun loadStatistics() {
        // For now, we'll use mock statistics. In real implementation, this would come from the repository
        val mockStatistics = listOf(
            StatisticItem(
                title = "Income",
                amount = "25,000",
                // Green
                color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
            ),
            StatisticItem(
                title = "Expenses",
                amount = "8,500",
                // Red
                color = androidx.compose.ui.graphics.Color(0xFFE53935),
            ),
            StatisticItem(
                title = "Savings",
                amount = "5,200",
                // Blue
                color = androidx.compose.ui.graphics.Color(0xFF2196F3),
            ),
        )

        _uiState.value = _uiState.value.copy(statistics = mockStatistics)
    }
}

/**
 * UI state for Home screen.
 * Follows the Open/Closed Principle by allowing extension without modification.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val primaryAccount: Account? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val statistics: List<StatisticItem> = emptyList(),
    val error: String? = null,
)

/**
 * Data class for statistic items.
 */
data class StatisticItem(
    val title: String,
    val amount: String,
    val color: androidx.compose.ui.graphics.Color,
)
