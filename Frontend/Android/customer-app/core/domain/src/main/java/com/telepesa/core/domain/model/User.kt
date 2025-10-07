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

/**
 * User domain model representing a Telepesa user.
 * This follows the Single Responsibility Principle by focusing only on user data.
 */
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val profilePicture: String? = null,
    val kycStatus: KycStatus,
    val accountStatus: AccountStatus,
    val createdAt: String,
    val updatedAt: String,
) {
    /**
     * Computed property for full name.
     * Implements the Open/Closed Principle by extending functionality without modification.
     */
    val fullName: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            !lastName.isNullOrBlank() -> lastName
            else -> username
        }

    /**
     * Computed property for user initials.
     */
    val initials: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() ->
                "${firstName.first().uppercaseChar()}${lastName.first().uppercaseChar()}"
            !firstName.isNullOrBlank() -> firstName.first().uppercaseChar().toString()
            !lastName.isNullOrBlank() -> lastName.first().uppercaseChar().toString()
            else -> username.first().uppercaseChar().toString()
        }

    /**
     * Checks if the user has completed KYC verification.
     */
    val isKycVerified: Boolean
        get() = kycStatus == KycStatus.VERIFIED

    /**
     * Checks if the user account is active.
     */
    val isAccountActive: Boolean
        get() = accountStatus == AccountStatus.ACTIVE
}

/**
 * Enum representing KYC verification status.
 * Implements the Interface Segregation Principle by providing specific status values.
 */
enum class KycStatus {
    PENDING,
    VERIFIED,
    REJECTED,
}
