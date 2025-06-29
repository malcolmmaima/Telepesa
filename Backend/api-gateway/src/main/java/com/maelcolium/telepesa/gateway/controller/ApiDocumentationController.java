package com.maelcolium.telepesa.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified API Documentation Controller
 * 
 * Provides a central hub for accessing all microservice documentation
 * through the API Gateway. This controller serves as the main entry point
 * for API documentation and provides links to individual service docs.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/docs")
public class ApiDocumentationController {

    /**
     * Main documentation hub that provides links to all service documentation
     * 
     * @return Map containing documentation links and service information
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDocumentationHub() {
        Map<String, Object> documentationHub = new HashMap<>();
        
        documentationHub.put("timestamp", LocalDateTime.now().toString());
        documentationHub.put("gateway", "Telepesa API Gateway");
        documentationHub.put("version", "1.0.0");
        documentationHub.put("description", "Unified API Documentation Hub for Telepesa Microservices");
        
        // Service documentation links
        Map<String, Object> services = new HashMap<>();
        
        // User Service
        Map<String, String> userService = new HashMap<>();
        userService.put("name", "User Service");
        userService.put("description", "User management and authentication");
        userService.put("swagger-ui", "/api/v1/docs/user-service/swagger-ui.html");
        userService.put("openapi-spec", "/api/v1/openapi/user-service/v3/api-docs");
        userService.put("health", "/api/v1/health/user-service");
        services.put("user-service", userService);
        
        // Account Service
        Map<String, String> accountService = new HashMap<>();
        accountService.put("name", "Account Service");
        accountService.put("description", "Bank account management and operations");
        accountService.put("swagger-ui", "/api/v1/docs/account-service/swagger-ui.html");
        accountService.put("openapi-spec", "/api/v1/openapi/account-service/v3/api-docs");
        accountService.put("health", "/api/v1/health/account-service");
        services.put("account-service", accountService);
        
        // Transaction Service
        Map<String, String> transactionService = new HashMap<>();
        transactionService.put("name", "Transaction Service");
        transactionService.put("description", "Payment processing and transaction management");
        transactionService.put("swagger-ui", "/api/v1/docs/transaction-service/swagger-ui.html");
        transactionService.put("openapi-spec", "/api/v1/openapi/transaction-service/v3/api-docs");
        transactionService.put("health", "/api/v1/health/transaction-service");
        services.put("transaction-service", transactionService);
        
        // Loan Service
        Map<String, String> loanService = new HashMap<>();
        loanService.put("name", "Loan Service");
        loanService.put("description", "Loan management and credit operations");
        loanService.put("swagger-ui", "/api/v1/docs/loan-service/swagger-ui.html");
        loanService.put("openapi-spec", "/api/v1/openapi/loan-service/v3/api-docs");
        loanService.put("health", "/api/v1/health/loan-service");
        services.put("loan-service", loanService);
        
        // Notification Service
        Map<String, String> notificationService = new HashMap<>();
        notificationService.put("name", "Notification Service");
        notificationService.put("description", "Email, SMS, and push notifications");
        notificationService.put("swagger-ui", "/api/v1/docs/notification-service/swagger-ui.html");
        notificationService.put("openapi-spec", "/api/v1/openapi/notification-service/v3/api-docs");
        notificationService.put("health", "/api/v1/health/notification-service");
        services.put("notification-service", notificationService);
        
        documentationHub.put("services", services);
        
        // Quick access links
        Map<String, String> quickLinks = new HashMap<>();
        quickLinks.put("gateway-health", "/actuator/health");
        quickLinks.put("gateway-metrics", "/actuator/metrics");
        quickLinks.put("gateway-info", "/actuator/info");
        quickLinks.put("eureka-dashboard", "http://localhost:8761");
        documentationHub.put("quick-links", quickLinks);
        
        // API Information
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("base-url", "http://localhost:8080/api/v1");
        apiInfo.put("authentication", "JWT Bearer Token");
        apiInfo.put("rate-limiting", "Enabled per endpoint");
        apiInfo.put("cors", "Enabled for trusted origins");
        documentationHub.put("api-info", apiInfo);
        
        log.info("Documentation hub accessed");
        return ResponseEntity.ok(documentationHub);
    }

    /**
     * Health check endpoint for the documentation service
     * 
     * @return Health status of the documentation service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getDocumentationHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "API Documentation Hub");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Get available documentation formats
     * 
     * @return Map of available documentation formats
     */
    @GetMapping("/formats")
    public ResponseEntity<Map<String, Object>> getDocumentationFormats() {
        Map<String, Object> formats = new HashMap<>();
        
        Map<String, String> swaggerUi = new HashMap<>();
        swaggerUi.put("description", "Interactive API documentation");
        swaggerUi.put("url-pattern", "/api/v1/docs/{service-name}/swagger-ui.html");
        formats.put("swagger-ui", swaggerUi);
        
        Map<String, String> openApi = new HashMap<>();
        openApi.put("description", "OpenAPI 3.0 specification");
        openApi.put("url-pattern", "/api/v1/openapi/{service-name}/v3/api-docs");
        formats.put("openapi-spec", openApi);
        
        Map<String, String> json = new HashMap<>();
        json.put("description", "JSON format API specification");
        json.put("url-pattern", "/api/v1/openapi/{service-name}/v3/api-docs.json");
        formats.put("json", json);
        
        Map<String, String> yaml = new HashMap<>();
        yaml.put("description", "YAML format API specification");
        yaml.put("url-pattern", "/api/v1/openapi/{service-name}/v3/api-docs.yaml");
        formats.put("yaml", yaml);
        
        return ResponseEntity.ok(formats);
    }
} 