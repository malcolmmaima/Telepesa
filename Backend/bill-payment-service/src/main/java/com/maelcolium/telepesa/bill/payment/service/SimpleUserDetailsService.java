package com.maelcolium.telepesa.bill.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Simple UserDetailsService for Bill Payment Service
 * Since this service doesn't manage users directly, we provide a minimal implementation
 * that works with JWT token validation
 */
@Service
@Slf4j
public class SimpleUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        // For bill payment service, we don't need detailed user information
        // We just need to provide a valid UserDetails object for JWT validation
        // The actual user validation is done by the JWT token
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        
        return User.builder()
            .username(username)
            .password("") // Empty password since we're using JWT
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }
}
