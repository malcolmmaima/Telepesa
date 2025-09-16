package com.maelcolium.telepesa.loan.security;

import com.maelcolium.telepesa.loan.service.UserService;
import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Security utilities for extracting user information from JWT tokens
 */
@Component
public class SecurityUtils {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserService userService;

    /**
     * Get the current authenticated username
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Extract user ID from the authenticated user context
     * Calls user service to get user ID by username
     */
    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        
        // Call user service to get user details by username
        UserDto user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }

    /**
     * Validate that the current user can access resources for the given user ID
     */
    public boolean canAccessUserResources(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
