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
package com.telepesa.core.domain.repository

import com.telepesa.core.common.domain.Result
import com.telepesa.core.domain.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for account operations.
 * Follows the Dependency Inversion Principle by depending on abstractions.
 * Implements the Interface Segregation Principle by providing specific account methods.
 */
interface AccountRepository {

    /**
     * Gets all accounts for the current user.
     * @return Flow of list of accounts or error
     */
    fun getUserAccounts(): Flow<Result<List<Account>>>

    /**
     * Gets a specific account by ID.
     * @param accountId The account ID
     * @return Flow of account or error
     */
    fun getAccountById(accountId: Long): Flow<Result<Account?>>

    /**
     * Gets account balance summary.
     * @return Flow of balance summary or error
     */
    fun getBalanceSummary(): Flow<Result<BalanceSummary>>

    /**
     * Creates a new account.
     * @param accountType The type of account to create
     * @return Result containing the created account or error
     */
    suspend fun createAccount(accountType: String): Result<Account>

    /**
     * Updates account information.
     * @param account The account to update
     * @return Result containing the updated account or error
     */
    suspend fun updateAccount(account: Account): Result<Account>

    /**
     * Closes an account.
     * @param accountId The account ID to close
     * @return Result indicating success or failure
     */
    suspend fun closeAccount(accountId: Long): Result<Unit>

    /**
     * Blocks or unblocks an account.
     * @param accountId The account ID
     * @param blocked Whether to block or unblock
     * @return Result indicating success or failure
     */
    suspend fun setAccountBlocked(accountId: Long, blocked: Boolean): Result<Unit>

    /**
     * Validates account ownership.
     * @param accountId The account ID
     * @return Result indicating if the account belongs to the current user
     */
    suspend fun validateAccountOwnership(accountId: Long): Result<Boolean>

    /**
     * Checks if account has sufficient balance for transaction.
     * @param accountId The account ID
     * @param amount The amount to check
     * @return Result indicating if sufficient balance exists
     */
    suspend fun hasSufficientBalance(accountId: Long, amount: java.math.BigDecimal): Result<Boolean>

    /**
     * Syncs account data with the server.
     * @return Result indicating success or failure
     */
    suspend fun syncAccounts(): Result<Unit>
}

/**
 * Data class representing account balance summary.
 */
data class BalanceSummary(
    val totalBalance: java.math.BigDecimal,
    val totalAvailableBalance: java.math.BigDecimal,
    val accountCount: Int,
    val currencyCode: String,
)
