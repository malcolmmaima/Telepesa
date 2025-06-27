package com.maelcolium.telepesa.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
        userPrincipal = new UserPrincipal(
                1L,
                "testuser",
                "test@example.com",
                "hashedPassword",
                authorities,
                true,
                true
        );
    }

    @Test
    void constructor_WithValidData_ShouldCreateUserPrincipal() {
        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getUsername()).isEqualTo("testuser");
        assertThat(userPrincipal.getEmail()).isEqualTo("test@example.com");
        assertThat(userPrincipal.getPassword()).isEqualTo("hashedPassword");
        assertThat(userPrincipal.isEnabled()).isTrue();
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue() {
        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isEnabled_WithDisabledUser_ShouldReturnFalse() {
        UserPrincipal disabledPrincipal = new UserPrincipal(
                2L, "disabled", "disabled@example.com", "password",
                Collections.emptyList(), false, true
        );
        assertThat(disabledPrincipal.isEnabled()).isFalse();
    }

    @Test
    void isAccountNonLocked_WithLockedUser_ShouldReturnFalse() {
        UserPrincipal lockedPrincipal = new UserPrincipal(
                3L, "locked", "locked@example.com", "password",
                Collections.emptyList(), true, false
        );
        assertThat(lockedPrincipal.isAccountNonLocked()).isFalse();
    }
}
