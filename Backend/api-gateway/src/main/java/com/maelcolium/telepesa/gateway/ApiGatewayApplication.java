package com.maelcolium.telepesa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * API Gateway Application for Telepesa Microservices
 * 
 * This service acts as the single entry point for all client requests.
 * It routes requests to appropriate microservices using service discovery.
 * 
 * Features:
 * - Service discovery with Eureka
 * - JWT authentication and authorization
 * - Request routing and load balancing
 * - CORS configuration
 * - Rate limiting and security
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
    "com.maelcolium.telepesa.gateway",
    "com.maelcolium.telepesa.gateway.config",
    "com.maelcolium.telepesa.security"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
} 