package com.maelcolium.telepesa.user.security;

import com.maelcolium.telepesa.user.model.User;
import com.maelcolium.telepesa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of UserDetailsService for Spring Security integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsernameOrEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return createUserPrincipal(user);
    }

    private UserDetails createUserPrincipal(User user) {
        Collection<GrantedAuthority> authorities = mapAuthorities(user);
        
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            authorities,
            user.isActive(),
            !user.getAccountLocked()
        );
    }

    private Collection<GrantedAuthority> mapAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add default user role
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Add admin role if user is admin (this would typically come from a roles table)
        // For now, we'll check if username is "admin"
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        return authorities;
    }
} 