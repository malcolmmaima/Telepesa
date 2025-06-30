package com.maelcolium.telepesa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
        
        // Allow all origins for development
        corsConfig.addAllowedOriginPattern("*");
        
        // Allowed HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allowed headers
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
} 