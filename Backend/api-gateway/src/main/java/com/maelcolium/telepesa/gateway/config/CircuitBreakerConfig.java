package com.maelcolium.telepesa.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit Breaker Configuration for API Gateway
 * 
 * Provides circuit breaker patterns to handle service failures gracefully
 * and prevent cascading failures across the microservices architecture.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Configuration
public class CircuitBreakerConfig {

    /**
     * Customizes the circuit breaker factory with specific configurations
     * for different types of services and endpoints.
     * 
     * @return Customizer for ReactiveResilience4JCircuitBreakerFactory
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build())
            .build());
    }

    /**
     * Custom circuit breaker configuration for authentication endpoints
     * with stricter failure thresholds due to security requirements.
     * 
     * @return Customizer for authentication-specific circuit breakers
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> authCircuitBreakerCustomizer() {
        return factory -> factory.configure(builder -> builder
            .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(20)
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slowCallRateThreshold(30)
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(2))
                .build())
            .build(), "auth-circuit-breaker");
    }

    /**
     * Custom circuit breaker configuration for critical banking operations
     * with more lenient thresholds to ensure service availability.
     * 
     * @return Customizer for banking operations circuit breakers
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> bankingCircuitBreakerCustomizer() {
        return factory -> factory.configure(builder -> builder
            .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(50)
                .failureRateThreshold(70)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(10)
                .slowCallRateThreshold(60)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .build())
            .build(), "banking-circuit-breaker");
    }

    /**
     * Custom circuit breaker configuration for read-only operations
     * with more aggressive failure handling for better performance.
     * 
     * @return Customizer for read-only operations circuit breakers
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> readOnlyCircuitBreakerCustomizer() {
        return factory -> factory.configure(builder -> builder
            .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(30)
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(8)
                .slowCallRateThreshold(40)
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build())
            .build(), "readonly-circuit-breaker");
    }
} 