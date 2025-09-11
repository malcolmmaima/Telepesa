package com.maelcolium.telepesa.transaction.config;

import com.maelcolium.telepesa.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void securityConfig_ShouldBeInstantiable() {
        // Given 
        JwtTokenUtil mockJwtUtil = Mockito.mock(JwtTokenUtil.class);
        UserDetailsService mockUserDetailsService = Mockito.mock(UserDetailsService.class);
        
        // When
        SecurityConfig config = new SecurityConfig(mockJwtUtil, mockUserDetailsService);

        // Then
        assertThat(config).isNotNull();
    }

    @Test
    void securityConfig_ShouldHaveFilterChainMethod() throws Exception {
        // Given
        JwtTokenUtil mockJwtUtil = Mockito.mock(JwtTokenUtil.class);
        UserDetailsService mockUserDetailsService = Mockito.mock(UserDetailsService.class);
        SecurityConfig config = new SecurityConfig(mockJwtUtil, mockUserDetailsService);

        // When & Then
        assertThat(config.getClass().getDeclaredMethod("filterChain", 
            org.springframework.security.config.annotation.web.builders.HttpSecurity.class))
            .isNotNull();
    }
}
