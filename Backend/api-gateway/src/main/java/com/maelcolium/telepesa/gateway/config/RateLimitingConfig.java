package com.maelcolium.telepesa.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Configuration for API Gateway
 * 
 * This configuration provides rate limiting capabilities using Redis
 * to prevent abuse and ensure fair usage of the API endpoints.
 * 
 * @author Telepesa Development Team
 * @version 1.0
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Creates a rate limiter for public endpoints
     * Allows 100 requests per minute for public endpoints
     */
    @Bean
    @Primary
    public RedisRateLimiter publicRateLimiter() {
        return new RedisRateLimiter(100, 120, 1);
    }

    /**
     * Creates a rate limiter for authenticated endpoints
     * Allows 300 requests per minute for authenticated users
     */
    @Bean
    public RedisRateLimiter authenticatedRateLimiter() {
        return new RedisRateLimiter(300, 350, 1);
    }

    /**
     * Creates a rate limiter for admin endpoints
     * Allows 500 requests per minute for admin users
     */
    @Bean
    public RedisRateLimiter adminRateLimiter() {
        return new RedisRateLimiter(500, 600, 1);
    }

    /**
     * Key resolver for IP-based rate limiting
     * Uses client IP address as the key for rate limiting
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just(clientIp);
        };
    }

    /**
     * Key resolver for user-based rate limiting
     * Uses authenticated user ID as the key for rate limiting
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
            .cast(Object.class)
            .map(Object::toString)
            .switchIfEmpty(Mono.just("anonymous"));
    }
} 