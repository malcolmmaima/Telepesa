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
 * Authentication request model for login.
 * Follows the Single Responsibility Principle by focusing only on login data.
 */
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String,
) {
    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }

    /**
     * Validates the login request data.
     * Implements the Open/Closed Principle by extending validation without modification.
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (usernameOrEmail.isBlank()) {
            errors.add("Username or email is required")
        }

        if (password.isBlank()) {
            errors.add("Password is required")
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Authentication response model containing tokens and user data.
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)

/**
 * Registration request model for new user signup.
 */
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val dateOfBirth: String? = null,
) {
    companion object {
        private const val MIN_USERNAME_LENGTH = 3
        private const val MIN_PASSWORD_LENGTH = 8
    }

    /**
     * Validates the registration request data.
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (username.isBlank()) {
            errors.add("Username is required")
        } else if (username.length < MIN_USERNAME_LENGTH) {
            errors.add("Username must be at least $MIN_USERNAME_LENGTH characters")
        }

        if (email.isBlank()) {
            errors.add("Email is required")
        } else if (!isValidEmail(email)) {
            errors.add("Invalid email format")
        }

        if (password.isBlank()) {
            errors.add("Password is required")
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }

        if (firstName.isBlank()) {
            errors.add("First name is required")
        }

        if (lastName.isBlank()) {
            errors.add("Last name is required")
        }

        if (phoneNumber.isBlank()) {
            errors.add("Phone number is required")
        } else if (!isValidPhoneNumber(phoneNumber)) {
            errors.add("Invalid phone number format")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = "^\\+?[1-9]\\d{1,14}$".toRegex()
        return phoneRegex.matches(phoneNumber.replace("\\s".toRegex(), ""))
    }
}

/**
 * Forgot password request model.
 */
data class ForgotPasswordRequest(
    val email: String,
) {
    fun validate(): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid(listOf("Email is required"))
            !email.contains("@") -> ValidationResult.Invalid(listOf("Invalid email format"))
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Password reset request model.
 */
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String,
) {
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }

    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (token.isBlank()) {
            errors.add("Reset token is required")
        }

        if (newPassword.isBlank()) {
            errors.add("New password is required")
        } else if (newPassword.length < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }

        if (confirmPassword.isBlank()) {
            errors.add("Password confirmation is required")
        } else if (newPassword != confirmPassword) {
            errors.add("Passwords do not match")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Validation result sealed class.
 * Implements the Interface Segregation Principle by providing specific validation outcomes.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()

    val isValid: Boolean
        get() = this is Valid

    val errorList: List<String>
        get() = if (this is Invalid) this.errors else emptyList()
}
