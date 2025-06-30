package com.maelcolium.telepesa.user.service.impl;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.models.enums.UserStatus;
import com.maelcolium.telepesa.security.JwtTokenUtil;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.dto.LoginRequest;
import com.maelcolium.telepesa.user.dto.LoginResponse;
import com.maelcolium.telepesa.user.exception.DuplicateUserException;
import com.maelcolium.telepesa.user.exception.UserNotFoundException;
import com.maelcolium.telepesa.user.mapper.UserMapper;
import com.maelcolium.telepesa.user.model.User;
import com.maelcolium.telepesa.user.repository.UserRepository;
import com.maelcolium.telepesa.user.service.AuditLogService;
import com.maelcolium.telepesa.user.service.DeviceFingerprintService;
import com.maelcolium.telepesa.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of UserService with comprehensive user management functionality
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final AuditLogService auditLogService;
    private final DeviceFingerprintService deviceFingerprintService;

    @Value("${app.user.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.user.account-lock-duration:30}")
    private int accountLockDurationMinutes;

    public UserServiceImpl(UserRepository userRepository,
                          UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          JwtTokenUtil jwtTokenUtil,
                          UserDetailsService userDetailsService,
                          AuditLogService auditLogService,
                          DeviceFingerprintService deviceFingerprintService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.auditLogService = auditLogService;
        this.deviceFingerprintService = deviceFingerprintService;
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("username", request.getUsername());
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email", request.getEmail());
        }

        // Check for duplicate phone number if provided
        if (request.getPhoneNumber() != null && 
            userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateUserException("phone number", request.getPhoneNumber());
        }

        // Create user entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerificationToken(generateToken());

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // TODO: Send email verification
        // emailService.sendEmailVerification(savedUser.getEmail(), savedUser.getEmailVerificationToken());

        return userMapper.toDto(savedUser);
    }

    @Override
    public LoginResponse authenticateUser(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
            .orElseThrow(() -> new UserNotFoundException("username or email", request.getUsernameOrEmail()));

        // Check if account is locked
        if (user.getAccountLocked()) {
            throw new BadCredentialsException("Account is locked");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new BadCredentialsException("Account is not active");
        }

        // Reset failed login attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            userRepository.updateFailedLoginAttempts(user.getId(), 0);
        }

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);

        log.info("User authenticated successfully: {}", user.getUsername());

        return LoginResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .user(userMapper.toDto(user))
            .build();
    }

    @Override
    public UserDto createUserWithSecurity(CreateUserRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("Creating new user with username: {} from IP: {}", request.getUsername(), ipAddress);

        try {
            UserDto user = createUser(request);
            
            // Log successful registration
            auditLogService.logUserRegistration(
                request.getUsername(), 
                request.getEmail(), 
                ipAddress, 
                true, 
                "User registration successful"
            );
            
            // Generate and analyze device fingerprint
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(httpRequest);
            deviceFingerprintService.analyzeDevice(deviceFingerprint, request.getUsername(), ipAddress);
            
            return user;
        } catch (Exception e) {
            // Log failed registration
            auditLogService.logUserRegistration(
                request.getUsername(), 
                request.getEmail(), 
                ipAddress, 
                false, 
                "Registration failed: " + e.getMessage()
            );
            throw e;
        }
    }

    @Override
    public LoginResponse authenticateUserWithSecurity(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("Authenticating user: {} from IP: {}", request.getUsernameOrEmail(), ipAddress);

        try {
            // Generate device fingerprint
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(httpRequest);
            
            // Perform authentication
            LoginResponse response = authenticateUser(request);
            String username = response.getUser().getUsername();
            
            // Analyze device for suspicious patterns
            DeviceFingerprintService.DeviceAnalysisResult deviceAnalysis = 
                deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);
            
            // Log successful authentication
            auditLogService.logAuthenticationAttempt(username, ipAddress, true, "Authentication successful");
            
            // Handle suspicious device activity
            if (deviceAnalysis.isSuspicious()) {
                auditLogService.logSuspiciousActivity(
                    username, 
                    ipAddress, 
                    "SUSPICIOUS_DEVICE", 
                    deviceAnalysis.getSuspiciousReason()
                );
                
                // In production, you might want to:
                // 1. Require additional authentication (MFA)
                // 2. Send security alert email
                // 3. Temporarily restrict account
                // 4. Force device verification
            }
            
            if (deviceAnalysis.isNewDevice()) {
                log.info("New device detected for user: {} - Device fingerprint: {}", 
                    username, deviceFingerprint);
                // In production, send new device notification email
            }
            
            return response;
            
        } catch (Exception e) {
            // Log failed authentication
            auditLogService.logAuthenticationAttempt(
                request.getUsernameOrEmail(), 
                ipAddress, 
                false, 
                "Authentication failed: " + e.getMessage()
            );
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserDto getUser(Long id) {
        log.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'username:' + #username")
    public UserDto getUserByUsername(String username) {
        log.debug("Fetching user with username: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("username", username));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'email:' + #email")
    public UserDto getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("email", email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        log.debug("Fetching users with pagination: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toDto);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserDto updateUser(Long id, CreateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        // Check for duplicate username (if changed)
        if (!existingUser.getUsername().equals(request.getUsername()) &&
            userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("username", request.getUsername());
        }

        // Check for duplicate email (if changed)
        if (!existingUser.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email", request.getEmail());
        }

        // Update user fields
        userMapper.updateUserFromRequest(request, existingUser);
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getId());

        return userMapper.toDto(updatedUser);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);

        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        userRepository.markEmailAsVerified(user.getId());
        
        // Update status to active if phone is also verified or not required
        if (user.getPhoneVerified() || user.getPhoneNumber() == null) {
            userRepository.updateUserStatus(user.getId(), UserStatus.ACTIVE);
        }

        log.info("Email verified for user: {}", user.getId());
    }

    @Override
    public void resendEmailVerification(String email) {
        log.info("Resending email verification for: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("email", email));

        if (user.getEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        String newToken = generateToken();
        user.setEmailVerificationToken(newToken);
        userRepository.save(user);

        // TODO: Send email verification
        // emailService.sendEmailVerification(user.getEmail(), newToken);

        log.info("Email verification resent for user: {}", user.getId());
    }

    @Override
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("email", email));

        String resetToken = generateToken();
        user.setPasswordResetToken(resetToken);
        userRepository.save(user);

        // TODO: Send password reset email
        // emailService.sendPasswordReset(user.getEmail(), resetToken);

        log.info("Password reset token generated for user: {}", user.getId());
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token: {}", token);

        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.clearPasswordResetToken(user.getId());

        log.info("Password reset successfully for user: {}", user.getId());
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void lockUserAccount(Long id) {
        log.info("Locking user account: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.lockUserAccount(id);
        log.info("User account locked: {}", id);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void unlockUserAccount(Long id) {
        log.info("Unlocking user account: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.unlockUserAccount(id);
        log.info("User account unlocked: {}", id);
    }

    /**
     * Handle failed login attempts and account locking
     */
    private void handleFailedLogin(User user) {
        int newFailedAttempts = user.getFailedLoginAttempts() + 1;
        userRepository.updateFailedLoginAttempts(user.getId(), newFailedAttempts);

        if (newFailedAttempts >= maxFailedAttempts) {
            userRepository.lockUserAccount(user.getId());
            log.warn("User account locked due to {} failed attempts: {}", 
                    newFailedAttempts, user.getUsername());
        }
    }

    /**
     * Generate a secure random token
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extract client IP address from HTTP request with proxy support
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 