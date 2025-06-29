package com.maelcolium.telepesa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for API Gateway
 * 
 * This configuration handles Cross-Origin Resource Sharing (CORS)
 * to allow frontend applications to communicate with the API Gateway.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * Creates a CORS web filter for the API Gateway
     * 
     * @return CorsWebFilter with configured CORS settings
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allowed origins for different environments
        List<String> allowedOrigins = Arrays.asList(
            "http://localhost:3000",      // React development server
            "http://localhost:8080",      // API Gateway
            "https://telepesa.com",       // Production domain
            "https://dashboard.telepesa.com" // Dashboard domain
        );
        corsConfig.setAllowedOriginPatterns(allowedOrigins);
        
        // Allowed HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allowed headers
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Exposed headers
        corsConfig.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
} 