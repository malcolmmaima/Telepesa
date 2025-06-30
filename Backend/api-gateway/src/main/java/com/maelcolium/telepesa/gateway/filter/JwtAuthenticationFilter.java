package com.maelcolium.telepesa.gateway.filter;

import com.maelcolium.telepesa.security.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Filter for API Gateway
 * 
 * This filter validates JWT tokens and forwards user information to downstream services.
 * It also handles public endpoints that don't require authentication.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
// @Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenUtil jwtTokenUtil;
    
    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/users/register",
        "/api/users/login",
        "/api/users/verify-email",
        "/api/users/resend-verification",
        "/api/users/forgot-password",
        "/api/users/reset-password",
        "/actuator/health",
        "/actuator/info",
        "/swagger-ui",
        "/v3/api-docs"
    );

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        log.debug("Processing request: {} {}", request.getMethod(), path);
        
        // Check if this is a public endpoint
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }
        
        // Extract JWT token from Authorization header
        String token = getJwtFromRequest(request);
        
        if (!StringUtils.hasText(token)) {
            log.warn("No JWT token found in request: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        try {
            // Validate JWT token
            if (!jwtTokenUtil.validateToken(token)) {
                log.warn("Invalid JWT token for request: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // Extract user information from token
            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            // Add user information to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", username)
                .header("X-User-Name", username)
                .header("X-Authenticated", "true")
                .build();
            
            log.debug("JWT token validated for user: {} on path: {}", username, path);
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            log.error("Error processing JWT token for request: {}", path, e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100; // High priority filter
    }
    
    private String getJwtFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream()
            .anyMatch(endpoint -> path.startsWith(endpoint) || 
                                path.contains("/swagger-ui/") || 
                                path.contains("/v3/api-docs/"));
    }
} 