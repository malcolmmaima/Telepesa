package com.maelcolium.telepesa.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Configuration for API Gateway
 * 
 * This configuration provides rate limiting capabilities using Redis
 * to prevent abuse and ensure fair usage of the API endpoints.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Creates a Redis-based rate limiter for general API endpoints
     * 
     * @return RedisRateLimiter with default settings
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20); // replenishRate: 10, burstCapacity: 20
    }

    /**
     * Creates a Redis-based rate limiter for authentication endpoints
     * with stricter limits to prevent brute force attacks
     * 
     * @return RedisRateLimiter with strict settings
     */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(5, 10); // replenishRate: 5, burstCapacity: 10
    }

    /**
     * Key resolver for rate limiting based on user IP address
     * This ensures rate limiting is applied per IP address
     * 
     * @return KeyResolver that uses IP address as the key
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String ipAddress = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just(ipAddress);
        };
    }

    /**
     * Key resolver for rate limiting based on JWT token (user ID)
     * This provides user-specific rate limiting when authentication is available
     * 
     * @return KeyResolver that uses JWT token as the key
     */
    @Bean
    public KeyResolver userTokenKeyResolver() {
        return exchange -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return Mono.just(token.substring(7)); // Remove "Bearer " prefix
            }
            // Fallback to IP address if no token is present
            String ipAddress = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just(ipAddress);
        };
    }
} 