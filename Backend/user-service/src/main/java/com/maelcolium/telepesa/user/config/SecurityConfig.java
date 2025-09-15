package com.maelcolium.telepesa.user.config;

import com.maelcolium.telepesa.security.JwtTokenUtil;
import com.maelcolium.telepesa.user.security.JwtAuthenticationEntryPoint;
import com.maelcolium.telepesa.user.security.JwtAuthenticationFilter;
import com.maelcolium.telepesa.user.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Spring Security configuration for the User Service
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Security Headers for Banking Application
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny()) // Prevent clickjacking
                .contentTypeOptions(contentTypeOptions -> {}) // Prevent MIME sniffing
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true)
                    .preload(true)
                )
                .addHeaderWriter(new ReferrerPolicyHeaderWriter(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .addHeaderWriter(new XXssProtectionHeaderWriter())
            )
            
            .authorizeHttpRequests(authz -> authz
                // Static files - uploaded content (avatars, etc.) - MUST BE FIRST
                .requestMatchers("/uploads/**").permitAll()
                
                // Public endpoints - Authentication
                .requestMatchers("/api/users/register", "/api/users/login",
                                "/api/v1/users/register", "/api/v1/users/login",
                                "/api/users/refresh", "/api/v1/users/refresh").permitAll()
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", 
                                "/api/v1/auth/refresh", "/api/auth/login", 
                                "/api/auth/register", "/api/auth/refresh").permitAll()
                .requestMatchers("/api/users/verify-email", "/api/users/resend-verification").permitAll()
                .requestMatchers("/api/users/forgot-password", "/api/users/reset-password").permitAll()
                .requestMatchers("/api/v1/users/verify-email", "/api/v1/users/resend-verification").permitAll()
                .requestMatchers("/api/v1/users/forgot-password", "/api/v1/users/reset-password").permitAll()
                
                // Actuator endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // OpenAPI documentation
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Admin-only endpoints
                .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/admin/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add custom filters
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 