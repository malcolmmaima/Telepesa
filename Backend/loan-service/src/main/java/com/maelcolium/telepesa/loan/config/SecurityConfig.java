package com.maelcolium.telepesa.loan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Loan Service
 * Allows access to Swagger UI and API docs without authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                // Allow access to Swagger UI and API docs
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Allow access to actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                // Allow access to health checks
                .requestMatchers("/health", "/info").permitAll()
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
} 