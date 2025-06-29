package com.maelcolium.telepesa.account.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Simple UserDetailsService for Account Service
 * Provides user details for JWT authentication
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // For now, create a simple user with basic authorities
        // In a real implementation, this would fetch user details from a database
        // and assign proper roles based on user permissions
        
        // Add default user role with proper ROLE_ prefix
        return new User(username, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
} 