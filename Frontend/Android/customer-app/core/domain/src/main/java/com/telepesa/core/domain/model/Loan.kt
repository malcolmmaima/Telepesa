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

data class Loan(
    val id: Int,
    val userId: Int,
    val accountId: Int,
    val amount: Double,
    val interestRate: Double,
    // in months
    val term: Int,
    val monthlyPayment: Double,
    val remainingBalance: Double,
    val status: LoanStatus,
    val applicationDate: String,
    val approvalDate: String?,
    val disbursementDate: String?,
    val maturityDate: String?,
    val purpose: String?,
    val collateral: String?,
)

enum class LoanStatus {
    PENDING,
    APPROVED,
    DISBURSED,
    ACTIVE,
    COMPLETED,
    DEFAULTED,
    CANCELLED,
}
