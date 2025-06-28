package com.maelcolium.telepesa.transaction.config;

import org.junit.jupiter.api.Test;

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
