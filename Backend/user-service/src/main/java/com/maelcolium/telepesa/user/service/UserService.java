package com.maelcolium.telepesa.user.service;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.dto.LoginRequest;
import com.maelcolium.telepesa.user.dto.LoginResponse;
import com.maelcolium.telepesa.user.dto.TokenRefreshRequest;
import com.maelcolium.telepesa.user.dto.TokenRefreshResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for user management operations
 */
public interface UserService {

    /**
     * Create a new user account
     */
    UserDto createUser(CreateUserRequest request);

    /**
     * Create a new user account with enhanced security features
     */
    UserDto createUserWithSecurity(CreateUserRequest request, HttpServletRequest httpRequest);

    /**
     * Authenticate user and return JWT token
     */
    LoginResponse authenticateUser(LoginRequest request);

    /**
     * Authenticate user with enhanced security features (device fingerprinting, audit logging)
     */
    LoginResponse authenticateUserWithSecurity(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Refresh access token using refresh token (with rotation)
     */
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

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

    /**
     * Upload user avatar/profile picture
     */
    String uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile file);

    /**
     * Remove user avatar/profile picture and clear avatar URL
     */
    String removeAvatar(Long userId);

    /**
     * Get current user profile (based on authenticated user)
     */
    UserDto getCurrentUserProfile(Long userId);

    /**
     * Update user profile information
     */
    UserDto updateUserProfile(Long userId, com.maelcolium.telepesa.user.dto.UpdateProfileRequest request);

    /**
     * Change user password
     */
    void changePassword(Long userId, com.maelcolium.telepesa.user.dto.ChangePasswordRequest request);

    // Transaction PIN operations
    com.maelcolium.telepesa.user.dto.TransactionPinResponse createTransactionPin(Long userId, com.maelcolium.telepesa.user.dto.CreatePinRequest request);

    java.util.Map<String, Boolean> verifyTransactionPin(Long userId, com.maelcolium.telepesa.user.dto.VerifyPinRequest request);

    com.maelcolium.telepesa.user.dto.TransactionPinResponse changeTransactionPin(Long userId, com.maelcolium.telepesa.user.dto.ChangePinRequest request);

    com.maelcolium.telepesa.user.dto.TransactionPinResponse getTransactionPinStatus(Long userId);
}
