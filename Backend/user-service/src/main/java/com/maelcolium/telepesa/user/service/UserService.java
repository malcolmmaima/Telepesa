package com.maelcolium.telepesa.user.service;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.dto.LoginRequest;
import com.maelcolium.telepesa.user.dto.LoginResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for user management operations
 */
public interface UserService {

    /**
     * Create a new user account
     */
    UserDto createUser(CreateUserRequest request);

    /**
     * Authenticate user and return JWT token
     */
    LoginResponse authenticateUser(LoginRequest request);

    /**
     * Get user by ID
     */
    UserDto getUser(Long id);

    /**
     * Get user by username
     */
    UserDto getUserByUsername(String username);

    /**
     * Get user by email
     */
    UserDto getUserByEmail(String email);

    /**
     * Get all users with pagination
     */
    Page<UserDto> getUsers(Pageable pageable);

    /**
     * Update user information
     */
    UserDto updateUser(Long id, CreateUserRequest request);

    /**
     * Delete user account
     */
    void deleteUser(Long id);

    /**
     * Verify user email
     */
    void verifyEmail(String token);

    /**
     * Resend email verification
     */
    void resendEmailVerification(String email);

    /**
     * Request password reset
     */
    void requestPasswordReset(String email);

    /**
     * Reset password using token
     */
    void resetPassword(String token, String newPassword);

    /**
     * Lock user account
     */
    void lockUserAccount(Long id);

    /**
     * Unlock user account
     */
    void unlockUserAccount(Long id);
} 