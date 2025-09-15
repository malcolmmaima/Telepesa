package com.maelcolium.telepesa.loan.security;

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
     * Extract user ID from JWT token in the current request
     * This assumes the user ID is stored as a claim in the JWT token
     */
    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Extract user ID from token claims
                return jwtTokenUtil.getClaimFromToken(token, claims -> {
                    Object userIdClaim = claims.get("userId");
                    if (userIdClaim instanceof Number) {
                        return ((Number) userIdClaim).longValue();
                    } else if (userIdClaim instanceof String) {
                        return Long.parseLong((String) userIdClaim);
                    }
                    return null;
                });
            } catch (Exception e) {
                // If userId claim is not present, try to derive from username
                // This is a fallback - in production, user ID should be in the token
                return null;
            }
        }
        return null;
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
