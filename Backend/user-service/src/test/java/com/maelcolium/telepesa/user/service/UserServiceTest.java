package com.maelcolium.telepesa.user.service;

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
import com.maelcolium.telepesa.user.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserService with security features
 * Tests authentication, registration, audit logging, and device fingerprinting
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DeviceFingerprintService deviceFingerprintService;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("SecurePass123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("SecurePass123!")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ===== USER REGISTRATION TESTS =====

    @Test
    void createUser_WithValidRequest_ShouldReturnUserDto() {
        // Given
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenReturn(testUser);
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.createUser(createUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(createUserRequest.getPassword());
    }

    @Test
    void createUser_WithDuplicateUsername_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateUserException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateUserException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithSecurity_WithValidRequest_ShouldLogAuditAndAnalyzeDevice() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenReturn(testUser);
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(deviceFingerprintService.generateDeviceFingerprint(httpRequest)).thenReturn("device-fingerprint-123");

        DeviceFingerprintService.DeviceAnalysisResult analysisResult = 
            new DeviceFingerprintService.DeviceAnalysisResult("device-fingerprint-123", true, false, null, null);
        when(deviceFingerprintService.analyzeDevice(anyString(), anyString(), anyString())).thenReturn(analysisResult);

        // When
        UserDto result = userService.createUserWithSecurity(createUserRequest, httpRequest);

        // Then
        assertThat(result).isNotNull();
        verify(auditLogService).logUserRegistration(
            eq("testuser"),
            eq("test@example.com"),
            eq("192.168.1.100"),
            eq(true),
            eq("User registration successful")
        );
        verify(deviceFingerprintService).analyzeDevice("device-fingerprint-123", "testuser", "192.168.1.100");
    }

    // ===== USER AUTHENTICATION TESTS =====

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnToken() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

        // When
        LoginResponse result = userService.authenticateUser(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token-123");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getUser()).isNotNull();
    }

    @Test
    void authenticateUser_WithInvalidPassword_ShouldThrowException() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> userService.authenticateUser(loginRequest));
    }

    @Test
    void authenticateUser_WithLockedAccount_ShouldThrowException() {
        // Given
        testUser.setAccountLocked(true);
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
            .thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> userService.authenticateUser(loginRequest));
    }

    @Test
    void authenticateUser_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.authenticateUser(loginRequest));
    }

    @Test
    void authenticateUserWithSecurity_WithValidCredentials_ShouldLogAuditAndAnalyzeDevice() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("jwt-token-123");
        when(deviceFingerprintService.generateDeviceFingerprint(httpRequest)).thenReturn("device-fingerprint-123");

        DeviceFingerprintService.DeviceAnalysisResult analysisResult = 
            new DeviceFingerprintService.DeviceAnalysisResult("device-fingerprint-123", false, false, null, null);
        when(deviceFingerprintService.analyzeDevice(anyString(), anyString(), anyString())).thenReturn(analysisResult);

        // When
        LoginResponse result = userService.authenticateUserWithSecurity(loginRequest, httpRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token-123");
        verify(auditLogService).logAuthenticationAttempt(
            eq("testuser"),
            eq("192.168.1.100"),
            eq(true),
            eq("Authentication successful")
        );
        verify(deviceFingerprintService).analyzeDevice("device-fingerprint-123", "testuser", "192.168.1.100");
    }

    @Test
    void authenticateUserWithSecurity_WithSuspiciousDevice_ShouldLogSuspiciousActivity() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("jwt-token-123");
        when(deviceFingerprintService.generateDeviceFingerprint(httpRequest)).thenReturn("device-fingerprint-123");

        DeviceFingerprintService.DeviceAnalysisResult analysisResult = 
            new DeviceFingerprintService.DeviceAnalysisResult("device-fingerprint-123", false, true, "Device sharing detected", null);
        when(deviceFingerprintService.analyzeDevice(anyString(), anyString(), anyString())).thenReturn(analysisResult);

        // When
        LoginResponse result = userService.authenticateUserWithSecurity(loginRequest, httpRequest);

        // Then
        assertThat(result).isNotNull();
        verify(auditLogService).logSuspiciousActivity(
            eq("testuser"),
            eq("192.168.1.100"),
            eq("SUSPICIOUS_DEVICE"),
            eq("Device sharing detected")
        );
    }

    // ===== USER RETRIEVAL TESTS =====

    @Test
    void getUser_WithValidId_ShouldReturnUserDto() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUser_WithInvalidId_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    void getUserByUsername_WithValidUsername_ShouldReturnUserDto() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserByUsername(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUserDto() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void getUsers_WithPagination_ShouldReturnPagedUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        Page<UserDto> result = userService.getUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    // ===== USER UPDATE TESTS =====

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() {
        // Given
        Long userId = 1L;
        CreateUserRequest updateRequest = CreateUserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("NewSecurePass123!")
                .firstName("Updated")
                .lastName("User")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(updateRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userMapper).updateUserFromRequest(updateRequest, testUser);
        verify(userRepository).save(testUser);
    }

    // ===== USER DELETION TESTS =====

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(userId);
    }

    // ===== EMAIL VERIFICATION TESTS =====

    @Test
    void verifyEmail_WithValidToken_ShouldVerifyUser() {
        // Given
        String token = "valid-token";
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));

        // When
        userService.verifyEmail(token);

        // Then
        verify(userRepository).markEmailAsVerified(testUser.getId());
    }

    @Test
    void verifyEmail_WithInvalidToken_ShouldThrowException() {
        // Given
        String token = "invalid-token";
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.verifyEmail(token));
    }

    // ===== ACCOUNT MANAGEMENT TESTS =====

    @Test
    void lockUserAccount_WithValidId_ShouldLockAccount() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.lockUserAccount(userId);

        // Then
        verify(userRepository).lockUserAccount(userId);
    }

    @Test
    void unlockUserAccount_WithValidId_ShouldUnlockAccount() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.unlockUserAccount(userId);

        // Then
        verify(userRepository).unlockUserAccount(userId);
    }
} 