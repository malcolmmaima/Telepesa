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
package com.telepesa.core.domain.model

import java.math.BigDecimal

/**
 * Account domain model representing a bank account.
 * Follows the Single Responsibility Principle by focusing only on account data.
 */
data class Account(
    val id: Long,
    val accountNumber: String,
    val accountType: AccountType,
    val balance: BigDecimal,
    val availableBalance: BigDecimal,
    val currency: String,
    val status: AccountStatus,
    val createdAt: String,
    val updatedAt: String,
) {
    companion object {
        private const val ACCOUNT_NUMBER_CHUNK_SIZE = 4
        private const val MIN_ACCOUNT_NUMBER_LENGTH = 8
        private const val ACCOUNT_NUMBER_DISPLAY_LENGTH = 4
        private const val MASK_CHARACTER = "*"
    }

    /**
     * Computed property for formatted account number.
     * Implements the Open/Closed Principle by extending functionality without modification.
     */
    val formattedAccountNumber: String
        get() = accountNumber.chunked(ACCOUNT_NUMBER_CHUNK_SIZE).joinToString(" ")

    /**
     * Computed property for masked account number for display.
     */
    val maskedAccountNumber: String
        get() = if (accountNumber.length >= MIN_ACCOUNT_NUMBER_LENGTH) {
            val start = accountNumber.take(ACCOUNT_NUMBER_DISPLAY_LENGTH)
            val end = accountNumber.takeLast(ACCOUNT_NUMBER_DISPLAY_LENGTH)
            val middle = MASK_CHARACTER.repeat(accountNumber.length - MIN_ACCOUNT_NUMBER_LENGTH)
            "$start$middle$end"
        } else {
            accountNumber
        }

    /**
     * Checks if the account is active and can be used for transactions.
     */
    val isActive: Boolean
        get() = status == AccountStatus.ACTIVE

    /**
     * Checks if the account has sufficient balance for a given amount.
     */
    fun hasSufficientBalance(amount: BigDecimal): Boolean {
        return availableBalance >= amount
    }

    /**
     * Calculates the remaining balance after a transaction.
     */
    fun calculateBalanceAfterTransaction(amount: BigDecimal): BigDecimal {
        return balance - amount
    }
}

/**
 * Enum representing different account types.
 * Implements the Interface Segregation Principle by providing specific account types.
 */
enum class AccountType {
    SAVINGS,
    CURRENT,
    LOAN,
    FIXED_DEPOSIT,
}

/**
 * Enum representing account status.
 */
enum class AccountStatus {
    ACTIVE,
    BLOCKED,
    CLOSED,
}
