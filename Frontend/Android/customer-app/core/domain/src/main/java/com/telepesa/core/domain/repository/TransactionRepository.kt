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
import com.telepesa.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction-related operations.
 * Follows the Interface Segregation Principle by defining specific transaction operations.
 * Implements the Dependency Inversion Principle by abstracting data access.
 */
interface TransactionRepository {

    /**
     * Get recent transactions for a user.
     */
    fun getRecentTransactions(userId: Int, limit: Int = 10): Flow<Result<List<Transaction>>>

    /**
     * Get all transactions for a user with pagination.
     */
    fun getAllTransactions(userId: Int, page: Int = 0, pageSize: Int = 20): Flow<Result<List<Transaction>>>

    /**
     * Get transactions for a specific account.
     */
    fun getAccountTransactions(accountId: Int, page: Int = 0, pageSize: Int = 20): Flow<Result<List<Transaction>>>

    /**
     * Get transaction details by ID.
     */
    fun getTransactionById(transactionId: Int): Flow<Result<Transaction>>

    /**
     * Search transactions by description or reference.
     */
    fun searchTransactions(
        userId: Int,
        query: String,
        page: Int = 0,
        pageSize: Int = 20,
    ): Flow<Result<List<Transaction>>>

    /**
     * Get transactions by date range.
     */
    fun getTransactionsByDateRange(userId: Int, startDate: String, endDate: String): Flow<Result<List<Transaction>>>

    /**
     * Get transactions by type (credit/debit).
     */
    fun getTransactionsByType(
        userId: Int,
        type: com.telepesa.core.domain.model.TransactionType,
    ): Flow<Result<List<Transaction>>>

    /**
     * Get transactions by category.
     */
    fun getTransactionsByCategory(
        userId: Int,
        category: com.telepesa.core.domain.model.TransactionCategory,
    ): Flow<Result<List<Transaction>>>
}

