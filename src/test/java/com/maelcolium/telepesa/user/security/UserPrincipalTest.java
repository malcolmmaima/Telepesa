package com.maelcolium.telepesa.user.security;

import com.maelcolium.telepesa.models.enums.UserStatus;
import com.maelcolium.telepesa.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserPrincipal
 * Tests Spring Security UserDetails implementation
 */
class UserPrincipalTest {

    private User testUser;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lastLoginAttempt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    void create_WithValidUser_ShouldReturnUserPrincipal() {
        // When
        UserPrincipal result = UserPrincipal.create(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    void getAuthorities_ShouldReturnEmptyCollection() {
        // When
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).isEmpty();
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue() {
        // When
        boolean result = userPrincipal.isAccountNonExpired();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isAccountNonLocked_WithActiveUser_ShouldReturnTrue() {
        // When
        boolean result = userPrincipal.isAccountNonLocked();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isAccountNonLocked_WithLockedUser_ShouldReturnFalse() {
        // Given
        User lockedUser = User.builder()
                .id(2L)
                .username("lockeduser")
                .email("locked@example.com")
                .password("hashedPassword")
                .status(UserStatus.LOCKED)
                .build();

        UserPrincipal lockedPrincipal = UserPrincipal.create(lockedUser);

        // When
        boolean result = lockedPrincipal.isAccountNonLocked();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // When
        boolean result = userPrincipal.isCredentialsNonExpired();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isEnabled_WithActiveUser_ShouldReturnTrue() {
        // When
        boolean result = userPrincipal.isEnabled();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isEnabled_WithInactiveUser_ShouldReturnFalse() {
        // Given
        User inactiveUser = User.builder()
                .id(3L)
                .username("inactiveuser")
                .email("inactive@example.com")
                .password("hashedPassword")
                .status(UserStatus.INACTIVE)
                .build();

        UserPrincipal inactivePrincipal = UserPrincipal.create(inactiveUser);

        // When
        boolean result = inactivePrincipal.isEnabled();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isEnabled_WithPendingUser_ShouldReturnFalse() {
        // Given
        User pendingUser = User.builder()
                .id(4L)
                .username("pendinguser")
                .email("pending@example.com")
                .password("hashedPassword")
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        UserPrincipal pendingPrincipal = UserPrincipal.create(pendingUser);

        // When
        boolean result = pendingPrincipal.isEnabled();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        // Then
        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getUsername()).isEqualTo("testuser");
        assertThat(userPrincipal.getEmail()).isEqualTo("test@example.com");
        assertThat(userPrincipal.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    void equals_WithSameId_ShouldReturnTrue() {
        // Given
        User anotherUser = User.builder()
                .id(1L) // Same ID
                .username("anotheruser")
                .email("another@example.com")
                .password("anotherPassword")
                .status(UserStatus.ACTIVE)
                .build();

        UserPrincipal anotherPrincipal = UserPrincipal.create(anotherUser);

        // When & Then
        assertThat(userPrincipal).isEqualTo(anotherPrincipal);
    }

    @Test
    void equals_WithDifferentId_ShouldReturnFalse() {
        // Given
        User anotherUser = User.builder()
                .id(2L) // Different ID
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .status(UserStatus.ACTIVE)
                .build();

        UserPrincipal anotherPrincipal = UserPrincipal.create(anotherUser);

        // When & Then
        assertThat(userPrincipal).isNotEqualTo(anotherPrincipal);
    }

    @Test
    void hashCode_WithSameId_ShouldReturnSameHashCode() {
        // Given
        User anotherUser = User.builder()
                .id(1L) // Same ID
                .username("anotheruser")
                .email("another@example.com")
                .password("anotherPassword")
                .status(UserStatus.ACTIVE)
                .build();

        UserPrincipal anotherPrincipal = UserPrincipal.create(anotherUser);

        // When & Then
        assertThat(userPrincipal.hashCode()).isEqualTo(anotherPrincipal.hashCode());
    }
} 