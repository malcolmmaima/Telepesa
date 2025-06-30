package com.maelcolium.telepesa.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
public class GatewayConfig {

    @Value("${app.jwt.secret:default-secret-key-for-development}")
    private String jwtSecret;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8), 
            "HmacSHA256"
        );
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeExchange(authz -> authz
                // Public endpoints
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/v1/health/**").permitAll()
                .pathMatchers("/api/v1/docs/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                
                // Authentication endpoints
                .pathMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/users/login").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/users/refresh-token").permitAll()
                
                // All other endpoints require authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Service Routes
            .route("user-service", r -> r
                .path("/api/v1/users/**")
                .filters(f -> f
                    .rewritePath("/api/v1/users/(?<remaining>.*)", "/api/users/${remaining}")
                    .retry(3)
                    .circuitBreaker(config -> config
                        .setFallbackUri("forward:/fallback/user-service")))
                .uri("lb://user-service"))
            
            // Account Service Routes
            .route("account-service", r -> r
                .path("/api/v1/accounts/**")
                .filters(f -> f
                    .rewritePath("/api/v1/accounts/(?<remaining>.*)", "/api/accounts/${remaining}")
                    .retry(3)
                    .circuitBreaker(config -> config
                        .setFallbackUri("forward:/fallback/account-service")))
                .uri("lb://account-service"))
            
            // Transaction Service Routes
            .route("transaction-service", r -> r
                .path("/api/v1/transactions/**")
                .filters(f -> f
                    .rewritePath("/api/v1/transactions/(?<remaining>.*)", "/api/transactions/${remaining}")
                    .retry(3)
                    .circuitBreaker(config -> config
                        .setFallbackUri("forward:/fallback/transaction-service")))
                .uri("lb://transaction-service"))
            
            // Loan Service Routes
            .route("loan-service", r -> r
                .path("/api/v1/loans/**")
                .filters(f -> f
                    .rewritePath("/api/v1/loans/(?<remaining>.*)", "/api/loans/${remaining}")
                    .retry(3)
                    .circuitBreaker(config -> config
                        .setFallbackUri("forward:/fallback/loan-service")))
                .uri("lb://loan-service"))
            
            // Notification Service Routes
            .route("notification-service", r -> r
                .path("/api/v1/notifications/**")
                .filters(f -> f
                    .rewritePath("/api/v1/notifications/(?<remaining>.*)", "/api/notifications/${remaining}")
                    .retry(3)
                    .circuitBreaker(config -> config
                        .setFallbackUri("forward:/fallback/notification-service")))
                .uri("lb://notification-service"))
            
            // Health Check Routes
            .route("health-checks", r -> r
                .path("/api/v1/health/**")
                .filters(f -> f
                    .rewritePath("/api/v1/health/(?<remaining>.*)", "/actuator/health/${remaining}")
                    .retry(1))
                .uri("lb://user-service"))
            
            // Documentation Routes
            .route("api-docs", r -> r
                .path("/api/v1/docs/**")
                .filters(f -> f
                    .rewritePath("/api/v1/docs/(?<remaining>.*)", "/swagger-ui/${remaining}")
                    .retry(1))
                .uri("lb://user-service"))
            
            .build();
    }
}
