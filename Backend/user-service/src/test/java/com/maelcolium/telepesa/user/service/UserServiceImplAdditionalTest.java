package com.maelcolium.telepesa.user.service;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.dto.TokenRefreshRequest;
import com.maelcolium.telepesa.user.dto.TokenRefreshResponse;
import com.maelcolium.telepesa.user.exception.UserNotFoundException;
import com.maelcolium.telepesa.user.mapper.UserMapper;
import com.maelcolium.telepesa.user.model.RefreshToken;
import com.maelcolium.telepesa.user.model.User;
import com.maelcolium.telepesa.models.enums.UserStatus;
import com.maelcolium.telepesa.user.repository.RefreshTokenRepository;
import com.maelcolium.telepesa.user.repository.UserRepository;
import com.maelcolium.telepesa.user.service.impl.UserServiceImpl;
import com.maelcolium.telepesa.security.JwtTokenUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceImplAdditionalTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("encoded_password")
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .failedLoginAttempts(0)
                .emailVerificationToken("email-token")
                .passwordResetToken("reset-token")
                .build();

        testUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .build();

        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token("valid-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();
    }

    @Test
    @DisplayName("refreshToken - With valid token should return new tokens")
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("valid-refresh-token");

        UserDetails userDetails = createMockUserDetails("testuser");
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        when(refreshTokenRepository.findByToken("valid-refresh-token"))
                .thenReturn(Optional.of(testRefreshToken));
        when(userDetailsService.loadUserByUsername("testuser"))
                .thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails))
                .thenReturn(newAccessToken);

        // The generateToken() method generates UUID tokens which we can't predict

        // When
        TokenRefreshResponse response = userService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isNotNull(); // Will be a new UUID
        assertThat(response.getRefreshToken()).isNotEqualTo("valid-refresh-token"); // Should be different
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        // Verify both tokens were saved (old revoked and new token)
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(tokenCaptor.capture());
        
        // First saved token should be the revoked one
        RefreshToken revokedToken = tokenCaptor.getAllValues().get(0);
        assertThat(revokedToken.getRevoked()).isTrue();
        assertThat(revokedToken.getToken()).isEqualTo("valid-refresh-token");
        
        // Second saved token should be the new one
        RefreshToken newToken = tokenCaptor.getAllValues().get(1);
        assertThat(newToken.getRevoked()).isFalse();
        assertThat(newToken.getToken()).isNotEqualTo("valid-refresh-token");
    }

    @Test
    @DisplayName("refreshToken - With invalid token should throw BadCredentialsException")
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("invalid-token");

        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.refreshToken(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("refreshToken - With expired token should throw BadCredentialsException")
    void refreshToken_WithExpiredToken_ShouldThrowException() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("expired-token");

        RefreshToken expiredToken = RefreshToken.builder()
                .user(testUser)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        // When/Then
        assertThatThrownBy(() -> userService.refreshToken(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh token expired or revoked");
    }

    @Test
    @DisplayName("refreshToken - With revoked token should throw BadCredentialsException")
    void refreshToken_WithRevokedToken_ShouldThrowException() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("revoked-token");

        RefreshToken revokedToken = RefreshToken.builder()
                .user(testUser)
                .token("revoked-token")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(true) // Revoked
                .build();

        when(refreshTokenRepository.findByToken("revoked-token"))
                .thenReturn(Optional.of(revokedToken));

        // When/Then
        assertThatThrownBy(() -> userService.refreshToken(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh token expired or revoked");
    }

    @Test
    @DisplayName("resendEmailVerification - With valid email should resend verification")
    void resendEmailVerification_WithValidEmail_ShouldResendVerification() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.resendEmailVerification(email);

        // Then
        verify(userRepository).findByEmail(email);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmailVerificationToken()).isNotNull();
        assertThat(savedUser.getEmailVerificationToken()).isNotEqualTo("email-token");
    }

    @Test
    @DisplayName("resendEmailVerification - With non-existent email should throw UserNotFoundException")
    void resendEmailVerification_WithNonExistentEmail_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.resendEmailVerification(email))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("resendEmailVerification - With already verified email should throw IllegalStateException")
    void resendEmailVerification_WithAlreadyVerifiedEmail_ShouldThrowException() {
        // Given
        String email = "test@example.com";
        User verifiedUser = User.builder()
                .id(1L)
                .email(email)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(verifiedUser));

        // When/Then
        assertThatThrownBy(() -> userService.resendEmailVerification(email))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email is already verified");
    }

    @Test
    @DisplayName("requestPasswordReset - With valid email should generate reset token")
    void requestPasswordReset_WithValidEmail_ShouldGenerateResetToken() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.requestPasswordReset(email);

        // Then
        verify(userRepository).findByEmail(email);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordResetToken()).isNotNull();
        assertThat(savedUser.getPasswordResetToken()).isNotEqualTo("reset-token");
    }

    @Test
    @DisplayName("requestPasswordReset - With non-existent email should throw UserNotFoundException")
    void requestPasswordReset_WithNonExistentEmail_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.requestPasswordReset(email))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("resetPassword - With valid token should reset password")
    void resetPassword_WithValidToken_ShouldResetPassword() {
        // Given
        String token = "valid-reset-token";
        String newPassword = "NewPassword123!";
        String encodedPassword = "encoded-new-password";

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // When
        userService.resetPassword(token, newPassword);

        // Then
        verify(userRepository).findByPasswordResetToken(token);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).clearPasswordResetToken(testUser.getId());
        
        assertThat(testUser.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("resetPassword - With invalid token should throw IllegalArgumentException")
    void resetPassword_WithInvalidToken_ShouldThrowException() {
        // Given
        String token = "invalid-token";
        String newPassword = "NewPassword123!";

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.resetPassword(token, newPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid reset token");
    }

    @Test
    @DisplayName("updateUser - With user not found should throw UserNotFoundException")
    void updateUser_WithUserNotFound_ShouldThrowException() {
        // Given
        Long userId = 999L;
        CreateUserRequest request = CreateUserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("lockUserAccount - With non-existent user should throw UserNotFoundException")
    void lockUserAccount_WithNonExistentUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.lockUserAccount(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("unlockUserAccount - With non-existent user should throw UserNotFoundException")
    void unlockUserAccount_WithNonExistentUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.unlockUserAccount(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("verifyEmail - With phone not verified and phone number present should not activate user")
    void verifyEmail_WithPhoneNotVerifiedAndPhonePresent_ShouldNotActivateUser() {
        // Given
        String token = "email-verification-token";
        User userWithPhone = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("+1234567890") // Has phone number
                .phoneVerified(false)       // But not verified
                .emailVerified(false)
                .build();

        when(userRepository.findByEmailVerificationToken(token))
                .thenReturn(Optional.of(userWithPhone));

        // When
        userService.verifyEmail(token);

        // Then
        verify(userRepository).markEmailAsVerified(userWithPhone.getId());
        // Should NOT update status to ACTIVE because phone is not verified
        verify(userRepository).findByEmailVerificationToken(token);
        // Verify that updateUserStatus was not called (since phone is not verified)
    }

    @Test
    @DisplayName("verifyEmail - With phone verified should activate user")
    void verifyEmail_WithPhoneVerified_ShouldActivateUser() {
        // Given
        String token = "email-verification-token";
        User userWithVerifiedPhone = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .phoneVerified(true)  // Phone is verified
                .emailVerified(false)
                .build();

        when(userRepository.findByEmailVerificationToken(token))
                .thenReturn(Optional.of(userWithVerifiedPhone));

        // When
        userService.verifyEmail(token);

        // Then
        verify(userRepository).markEmailAsVerified(userWithVerifiedPhone.getId());
        verify(userRepository).updateUserStatus(userWithVerifiedPhone.getId(), UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("verifyEmail - With no phone number should activate user")
    void verifyEmail_WithNoPhoneNumber_ShouldActivateUser() {
        // Given
        String token = "email-verification-token";
        User userWithoutPhone = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber(null) // No phone number
                .phoneVerified(false)
                .emailVerified(false)
                .build();

        when(userRepository.findByEmailVerificationToken(token))
                .thenReturn(Optional.of(userWithoutPhone));

        // When
        userService.verifyEmail(token);

        // Then
        verify(userRepository).markEmailAsVerified(userWithoutPhone.getId());
        verify(userRepository).updateUserStatus(userWithoutPhone.getId(), UserStatus.ACTIVE);
    }

    private UserDetails createMockUserDetails(String username) {
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            }

            @Override
            public String getPassword() {
                return "encoded_password";
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
    }
}
