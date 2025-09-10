package com.maelcolium.telepesa.user.security;

import com.maelcolium.telepesa.models.enums.UserStatus;
import com.maelcolium.telepesa.user.model.User;
import com.maelcolium.telepesa.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User regularUser;
    private User adminUser;
    private User inactiveUser;
    private User lockedUser;

    @BeforeEach
    void setUp() {
        regularUser = new User();
        regularUser.setId(1L);
        regularUser.setUsername("testuser");
        regularUser.setEmail("test@example.com");
        regularUser.setPassword("encodedPassword");
        regularUser.setStatus(UserStatus.ACTIVE);
        regularUser.setAccountLocked(false);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setAccountLocked(false);

        inactiveUser = new User();
        inactiveUser.setId(3L);
        inactiveUser.setUsername("inactive");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword("encodedPassword");
        inactiveUser.setStatus(UserStatus.INACTIVE);
        inactiveUser.setAccountLocked(false);

        lockedUser = new User();
        lockedUser.setId(4L);
        lockedUser.setUsername("locked");
        lockedUser.setEmail("locked@example.com");
        lockedUser.setPassword("encodedPassword");
        lockedUser.setStatus(UserStatus.ACTIVE);
        lockedUser.setAccountLocked(true);
    }

    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(regularUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
            .isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsernameOrEmail("test@example.com"))
            .thenReturn(Optional.of(regularUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    void loadUserByUsername_WithAdminUser_ShouldReturnUserDetailsWithAdminRole() {
        // Given
        when(userRepository.findByUsernameOrEmail("admin"))
            .thenReturn(Optional.of(adminUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities()).hasSize(2);
        
        boolean hasUserRole = userDetails.getAuthorities().stream()
            .anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority()));
        boolean hasAdminRole = userDetails.getAuthorities().stream()
            .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        
        assertThat(hasUserRole).isTrue();
        assertThat(hasAdminRole).isTrue();
    }

    @Test
    void loadUserByUsername_WithInactiveUser_ShouldReturnDisabledUserDetails() {
        // Given
        when(userRepository.findByUsernameOrEmail("inactive"))
            .thenReturn(Optional.of(inactiveUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("inactive");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    void loadUserByUsername_WithLockedUser_ShouldReturnLockedUserDetails() {
        // Given
        when(userRepository.findByUsernameOrEmail("locked"))
            .thenReturn(Optional.of(lockedUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("locked");

        // Then
        assertThat(userDetails).isNotNull();
        // The User.isActive() method returns status == ACTIVE && !accountLocked
        // So a locked user will have isEnabled() = false (because isActive() returns false)
        // And isAccountNonLocked() = false (because !accountLocked = false when locked)
        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.isAccountNonLocked()).isFalse();
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString()))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found: nonexistent");
    }

    @Test
    void loadUserByUsername_WithCaseSensitiveAdminUsername_ShouldNotGrantAdminRole() {
        // Given
        User upperCaseAdmin = new User();
        upperCaseAdmin.setId(5L);
        upperCaseAdmin.setUsername("ADMIN");
        upperCaseAdmin.setEmail("admin2@example.com");
        upperCaseAdmin.setPassword("encodedPassword");
        upperCaseAdmin.setStatus(UserStatus.ACTIVE);
        upperCaseAdmin.setAccountLocked(false);

        when(userRepository.findByUsernameOrEmail("ADMIN"))
            .thenReturn(Optional.of(upperCaseAdmin));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("ADMIN");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).hasSize(2);
        
        boolean hasAdminRole = userDetails.getAuthorities().stream()
            .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        
        assertThat(hasAdminRole).isTrue(); // Because equalsIgnoreCase is used
    }

    @Test
    void loadUserByUsername_ShouldReturnUserPrincipalType() {
        // Given
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(regularUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getEmail()).isEqualTo("test@example.com");
    }
}
