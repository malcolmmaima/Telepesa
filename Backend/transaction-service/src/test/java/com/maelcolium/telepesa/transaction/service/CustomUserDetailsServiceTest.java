package com.maelcolium.telepesa.transaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Given
        String username = "testuser";

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_WithNullUsername_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    void loadUserByUsername_WithEmptyUsername_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    void loadUserByUsername_WithDifferentUsernames_ShouldReturnCorrectUsername() {
        // Given
        String username1 = "user1";
        String username2 = "user2";

        // When
        UserDetails result1 = customUserDetailsService.loadUserByUsername(username1);
        UserDetails result2 = customUserDetailsService.loadUserByUsername(username2);

        // Then
        assertThat(result1.getUsername()).isEqualTo(username1);
        assertThat(result2.getUsername()).isEqualTo(username2);
        assertThat(result1.getPassword()).isEqualTo(result2.getPassword());
        assertThat(result1.getAuthorities()).isEqualTo(result2.getAuthorities());
    }
}
