package com.maelcolium.telepesa.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Documentation Configuration for API Gateway
 * 
 * Configures Swagger/OpenAPI documentation for the API Gateway itself,
 * providing information about the gateway's capabilities and routing.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Configuration
public class DocumentationConfig {

    /**
     * Configure OpenAPI documentation for the API Gateway
     * 
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Telepesa API Gateway")
                        .description("""
                                # Telepesa API Gateway Documentation
                                
                                ## Overview
                                The Telepesa API Gateway serves as the central entry point for all microservice APIs.
                                It provides unified routing, authentication, rate limiting, and monitoring capabilities.
                                
                                ## Features
                                - **Unified Routing**: Single entry point for all microservices
                                - **JWT Authentication**: Centralized authentication and authorization
                                - **Rate Limiting**: Per-endpoint rate limiting with Redis
                                - **CORS Support**: Cross-origin resource sharing configuration
                                - **Health Monitoring**: Centralized health checks and metrics
                                - **API Documentation**: Unified access to all service documentation
                                
                                ## Microservices
                                - **User Service** (Port 8081): User management and authentication
                                - **Account Service** (Port 8082): Bank account operations
                                - **Transaction Service** (Port 8083): Payment processing
                                - **Loan Service** (Port 8084): Credit and loan management
                                - **Notification Service** (Port 8085): Communication services
                                
                                ## Authentication
                                All protected endpoints require a valid JWT token in the Authorization header:
                                ```
                                Authorization: Bearer <your-jwt-token>
                                ```
                                
                                ## Rate Limiting
                                Rate limiting is applied per endpoint and user. Limits vary by endpoint type:
                                - Authentication endpoints: 5 requests per minute
                                - API endpoints: 100 requests per minute
                                - Documentation endpoints: 50 requests per minute
                                
                                ## Error Handling
                                The gateway provides consistent error responses across all services:
                                - 401 Unauthorized: Invalid or missing authentication
                                - 403 Forbidden: Insufficient permissions
                                - 429 Too Many Requests: Rate limit exceeded
                                - 502 Bad Gateway: Service unavailable
                                - 503 Service Unavailable: Service temporarily unavailable
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Telepesa Development Team")
                                .email("contact@telepesa.com")
                                .url("https://telepesa.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.telepesa.com")
                                .description("Production Server")
                ));
    }
} 