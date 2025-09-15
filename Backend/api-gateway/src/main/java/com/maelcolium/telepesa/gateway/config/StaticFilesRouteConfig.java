package com.maelcolium.telepesa.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route configuration for static files
 */
@Configuration
public class StaticFilesRouteConfig {

    @Bean
    public RouteLocator staticFilesRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("static-files", r -> r.path("/uploads/**")
                .uri("lb://user-service"))
            .build();
    }
}
