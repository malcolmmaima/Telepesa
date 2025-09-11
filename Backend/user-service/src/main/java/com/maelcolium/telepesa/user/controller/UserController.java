package com.maelcolium.telepesa.user.controller;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.dto.LoginRequest;
import com.maelcolium.telepesa.user.dto.LoginResponse;
import com.maelcolium.telepesa.user.dto.TokenRefreshRequest;
import com.maelcolium.telepesa.user.dto.TokenRefreshResponse;
import com.maelcolium.telepesa.user.dto.UpdateProfileRequest;
import com.maelcolium.telepesa.user.dto.ChangePasswordRequest;
import com.maelcolium.telepesa.user.service.UserService;
import com.maelcolium.telepesa.security.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.maelcolium.telepesa.user.security.UserPrincipal;

/**
 * REST Controller for user management operations
 */
@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
@Tag(name = "User Management", description = "User registration, authentication, and management APIs")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody CreateUserRequest request,
                                               HttpServletRequest httpRequest) {
        log.info("Registration request received for username: {}", request.getUsername());
        UserDto createdUser = userService.createUserWithSecurity(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Authenticate user and get JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest) {
        log.info("Login request received for: {}", request.getUsernameOrEmail());
        LoginResponse response = userService.authenticateUserWithSecurity(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request received");
        TokenRefreshResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getCurrentUserProfile(jakarta.servlet.http.HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.debug("Get current user profile request for user ID: {}", userId);
        UserDto user = userService.getCurrentUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        log.info("Update user profile request for user ID: {}", userId);
        UserDto updatedUser = userService.updateUserProfile(userId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", updatedUser);
        response.put("message", "Profile updated successfully!");
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change user password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password data")
    })
    @PutMapping("/me/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        log.info("Change password request for user ID: {}", userId);
        userService.changePassword(userId, request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully!");
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload user avatar/profile picture")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    })
    @PostMapping("/me/avatar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @Parameter(description = "Avatar image file") 
            @RequestParam("avatar") MultipartFile file,
            jakarta.servlet.http.HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("Avatar upload request for user ID: {}", userId);
        
        String avatarUrl = userService.uploadAvatar(userId, file);
        
        Map<String, String> response = new HashMap<>();
        response.put("avatarUrl", avatarUrl);
        response.put("message", "Profile picture updated successfully!");
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        log.debug("Get user request for username: {}", username);
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.debug("Get user request for ID: {}", id);
        UserDto user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserByEmail(
            @Parameter(description = "Email address") @PathVariable String email) {
        log.debug("Get user request for email: {}", email);
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get all users with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Get users request with pagination: {}", pageable);
        Page<UserDto> users = userService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Update user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        log.info("Update user request for ID: {}", id);
        UserDto updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Delete user request for ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verify email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid verification token")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Parameter(description = "Email verification token") 
            @RequestParam String token) {
        log.info("Email verification request received");
        userService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Resend email verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification email sent"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Email already verified")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendEmailVerification(
            @Parameter(description = "Email address") 
            @RequestParam String email) {
        log.info("Resend verification request for email: {}", email);
        userService.resendEmailVerification(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Request password reset")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Parameter(description = "Email address") 
            @RequestParam String email) {
        log.info("Password reset request for email: {}", email);
        userService.requestPasswordReset(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reset password using token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid reset token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "Password reset token") 
            @RequestParam String token,
            @Parameter(description = "New password") 
            @RequestParam String newPassword) {
        log.info("Password reset attempt with token");
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lock user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User account locked"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{id:[0-9]+}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> lockUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Lock user request for ID: {}", id);
        userService.lockUserAccount(id);
        return ResponseEntity.ok("Account locked successfully");
    }

    @Operation(summary = "Unlock user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User account unlocked"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{id:[0-9]+}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unlockUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Unlock user request for ID: {}", id);
        userService.unlockUserAccount(id);
        return ResponseEntity.ok("Account unlocked successfully");
    }


    /**
     * Extract user ID from Spring Security authentication context
     * This is more reliable than manually parsing JWT tokens
     */
    private Long getCurrentUserId(jakarta.servlet.http.HttpServletRequest request) {
        log.debug("getCurrentUserId called for request: {} {}", request.getMethod(), request.getRequestURI());
        
        try {
            // Use Spring Security's Authentication context first
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.debug("Authentication object: {}", authentication != null ? authentication.getClass().getSimpleName() : "null");
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                Object principal = authentication.getPrincipal();
                log.debug("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");
                log.debug("Authentication name: {}", authentication.getName());
                String username = null;

                // Debug the exact type and class information
                log.debug("Principal class name: {}", principal.getClass().getName());
                log.debug("Principal toString: {}", principal.toString());
                log.debug("Checking instanceof UserPrincipal: {}", principal instanceof UserPrincipal);
                
                if (principal instanceof UserPrincipal userPrincipal) {
                    // Directly get the user ID from UserPrincipal to avoid database calls
                    log.debug("Using UserPrincipal ID directly: {}", userPrincipal.getId());
                    return userPrincipal.getId();
                } else if (principal instanceof UserDetails userDetails) {
                    username = userDetails.getUsername();
                    log.debug("Using UserDetails username: {}", username);
                } else if (principal != null) {
                    // Try principal name fallback (works for custom principals)
                    username = authentication.getName();
                    log.debug("Using authentication name: {}", username);
                }

                if (username != null && !username.isBlank()) {
                    log.debug("Getting user ID for authenticated principal: {}", username);
                    UserDto user = userService.getUserByUsername(username);
                    return user.getId();
                } else {
                    log.warn("No valid username found in authentication context");
                }
            } else {
                log.warn("No valid authentication found in SecurityContext");
            }
            
            // Instead of JWT parsing fallback, let's use a simpler approach
            // Check if we have X-User-Name header from API Gateway
            String gatewayUser = request.getHeader("X-User-Name");
            if (gatewayUser != null && !gatewayUser.isBlank()) {
                log.debug("Using X-User-Name header: {}", gatewayUser);
                UserDto user = userService.getUserByUsername(gatewayUser);
                return user.getId();
            }
            
        } catch (Exception e) {
            log.error("Error extracting user ID: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid authentication token");
        }
        
        throw new RuntimeException("User not authenticated");
    }
}
