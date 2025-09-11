package com.maelcolium.telepesa.transaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService for Transaction Service
 * Provides user authentication details for JWT validation
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        // In a real application, you would fetch user details from a database
        // For now, we'll create a simple user with ROLE_USER authority
        // The actual user validation is done by JWT token validation
        
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }
        
        // Create user with basic authority
        // The username comes from the JWT token, so we trust it at this point
        return User.builder()
                .username(username)
                .password("") // Password is not used since we rely on JWT
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
