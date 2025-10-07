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
import com.telepesa.core.domain.model.ForgotPasswordRequest
import com.telepesa.core.domain.model.LoginRequest
import com.telepesa.core.domain.model.LoginResponse
import com.telepesa.core.domain.model.RegisterRequest
import com.telepesa.core.domain.model.ResetPasswordRequest
import com.telepesa.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for basic authentication operations.
 * Follows the Dependency Inversion Principle by depending on abstractions.
 * Implements the Interface Segregation Principle by providing specific authentication methods.
 */
interface AuthRepository {

    /**
     * Authenticates a user with credentials.
     * @param request The login request containing credentials
     * @return Result containing the login response or error
     */
    suspend fun login(request: LoginRequest): Result<LoginResponse>

    /**
     * Registers a new user.
     * @param request The registration request containing user details
     * @return Result containing the created user or error
     */
    suspend fun register(request: RegisterRequest): Result<User>

    /**
     * Refreshes the authentication token.
     * @param refreshToken The refresh token
     * @return Result containing new tokens or error
     */
    suspend fun refreshToken(refreshToken: String): Result<LoginResponse>

    /**
     * Logs out the current user.
     * @return Result indicating success or failure
     */
    suspend fun logout(): Result<Unit>

    /**
     * Gets the current authenticated user.
     * @return Flow of the current user or null if not authenticated
     */
    fun getCurrentUser(): Flow<Result<User?>>

    /**
     * Checks if the user is currently authenticated.
     * @return Flow of authentication status
     */
    fun isAuthenticated(): Flow<Result<Boolean>>
}

/**
 * Repository interface for password-related operations.
 */
interface PasswordRepository {

    /**
     * Initiates forgot password process.
     * @param request The forgot password request
     * @return Result indicating success or failure
     */
    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<Unit>

    /**
     * Resets user password.
     * @param request The password reset request
     * @return Result indicating success or failure
     */
    suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit>

    /**
     * Changes user password.
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return Result indicating success or failure
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}

/**
 * Repository interface for biometric authentication operations.
 */
interface BiometricRepository {

    /**
     * Validates biometric authentication.
     * @return Result indicating success or failure
     */
    suspend fun authenticateWithBiometrics(): Result<Unit>

    /**
     * Enables or disables biometric authentication.
     * @param enabled Whether to enable biometric authentication
     * @return Result indicating success or failure
     */
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>

    /**
     * Checks if biometric authentication is available and enabled.
     * @return Result containing biometric availability status
     */
    suspend fun isBiometricEnabled(): Result<Boolean>
}

/**
 * Repository interface for user profile operations.
 */
interface ProfileRepository {

    /**
     * Updates user profile information.
     * @param user The updated user information
     * @return Result containing the updated user or error
     */
    suspend fun updateProfile(user: User): Result<User>
}
