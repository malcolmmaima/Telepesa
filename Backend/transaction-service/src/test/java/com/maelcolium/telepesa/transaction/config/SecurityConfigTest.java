package com.maelcolium.telepesa.transaction.config;

import com.maelcolium.telepesa.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void securityConfig_ShouldBeInstantiable() {
        // Given & When
        SecurityConfig config = new SecurityConfig();

        // Then
        assertThat(config).isNotNull();
    }

    @Test
    void securityConfig_ShouldHaveFilterChainMethod() throws Exception {
        // Given
        SecurityConfig config = new SecurityConfig();

        // When & Then
        assertThat(config.getClass().getDeclaredMethod("filterChain", 
            org.springframework.security.config.annotation.web.builders.HttpSecurity.class))
            .isNotNull();
    }
}
