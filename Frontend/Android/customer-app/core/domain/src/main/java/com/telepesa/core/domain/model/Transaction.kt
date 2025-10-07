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
 * Transaction domain model representing a financial transaction.
 * Follows the Single Responsibility Principle by focusing only on transaction data.
 */
data class Transaction(
    val id: Long,
    val accountId: Long,
    val type: TransactionType,
    val category: TransactionCategory,
    val amount: BigDecimal,
    val balance: BigDecimal,
    val description: String,
    val reference: String,
    val status: TransactionStatus,
    val recipientName: String? = null,
    val recipientAccount: String? = null,
    val charges: BigDecimal? = null,
    val createdAt: String,
) {
    /**
     * Computed property for formatted amount with currency symbol.
     * Implements the Open/Closed Principle by extending functionality without modification.
     */
    val formattedAmount: String
        get() = when (type) {
            TransactionType.CREDIT -> "+${formatCurrency(amount)}"
            TransactionType.DEBIT -> "-${formatCurrency(amount)}"
        }

    /**
     * Computed property for net amount after charges.
     */
    val netAmount: BigDecimal
        get() = when (type) {
            TransactionType.CREDIT -> amount
            TransactionType.DEBIT -> amount + (charges ?: BigDecimal.ZERO)
        }

    /**
     * Checks if the transaction is completed successfully.
     */
    val isCompleted: Boolean
        get() = status == TransactionStatus.COMPLETED

    /**
     * Checks if the transaction is pending.
     */
    val isPending: Boolean
        get() = status == TransactionStatus.PENDING

    /**
     * Checks if the transaction failed.
     */
    val isFailed: Boolean
        get() = status == TransactionStatus.FAILED

    /**
     * Formats currency amount for display.
     */
    private fun formatCurrency(amount: BigDecimal): String {
        return "KSh ${amount.setScale(2, java.math.RoundingMode.HALF_UP)}"
    }
}

/**
 * Enum representing transaction types.
 * Implements the Interface Segregation Principle by providing specific transaction types.
 */
enum class TransactionType {
    CREDIT,
    DEBIT,
}

/**
 * Enum representing transaction categories.
 */
enum class TransactionCategory {
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL,
    PAYMENT,
    LOAN,
    FEE,
}

/**
 * Enum representing transaction status.
 */
enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED,
    CANCELLED,
}
