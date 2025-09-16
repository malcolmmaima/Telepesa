package com.maelcolium.telepesa.user.security;

import com.maelcolium.telepesa.security.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter to process JWT tokens in requests
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        log.debug("JWT Filter processing request: {} {}", request.getMethod(), requestUri);
        
        // Skip JWT processing for static files (uploads) and internal endpoints
        boolean shouldSkip = requestUri.startsWith("/uploads/") || requestUri.startsWith("/api/v1/users/internal/");
        log.debug("Should skip JWT for {}: {}", requestUri, shouldSkip);
        if (shouldSkip) {
            log.debug("Skipping JWT processing for: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);
            log.debug("JWT token present: {}", jwt != null);
            
            if (StringUtils.hasText(jwt)) {
                log.debug("Validating JWT token...");
                boolean isValid = jwtTokenUtil.validateToken(jwt);
                log.debug("JWT token valid: {}", isValid);
                
                if (isValid) {
                    String username = jwtTokenUtil.getUsernameFromToken(jwt);
                    log.debug("Extracted username from JWT: {}", username);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.debug("Loaded user details for: {}", username);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication in SecurityContext for user: {}", username);
                } else {
                    log.warn("JWT token validation failed for request: {}", requestUri);
                }
            } else {
                log.debug("No JWT token found in request: {}", requestUri);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context for request: {}", requestUri, ex);
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 