package com.maelcolium.telepesa.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Documentation Service for API Gateway
 * 
 * Provides programmatic access to documentation information,
 * service discovery, and health status across all microservices.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class DocumentationService {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    
    @Value("${server.port:8080}")
    private String gatewayPort;
    
    @Value("${eureka.client.service-url.defaultZone:http://localhost:8761/eureka/}")
    private String eurekaUrl;

    public DocumentationService(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    /**
     * Get comprehensive documentation information for all services
     * 
     * @return Map containing documentation information
     */
    public Map<String, Object> getDocumentationInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("gateway", "Telepesa API Gateway");
        info.put("version", "1.0.0");
        
        // Service discovery information
        Map<String, Object> services = getServiceDiscoveryInfo();
        info.put("services", services);
        
        // Documentation endpoints
        Map<String, String> endpoints = getDocumentationEndpoints();
        info.put("endpoints", endpoints);
        
        // Health status
        Map<String, Object> health = getHealthStatus();
        info.put("health", health);
        
        return info;
    }

    /**
     * Get service discovery information
     * 
     * @return Map of service discovery information
     */
    private Map<String, Object> getServiceDiscoveryInfo() {
        Map<String, Object> services = new HashMap<>();
        
        List<String> serviceNames = discoveryClient.getServices();
        for (String serviceName : serviceNames) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            
            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("name", serviceName);
            serviceInfo.put("instances", instances.size());
            serviceInfo.put("status", instances.isEmpty() ? "DOWN" : "UP");
            
            if (!instances.isEmpty()) {
                ServiceInstance instance = instances.get(0);
                serviceInfo.put("host", instance.getHost());
                serviceInfo.put("port", instance.getPort());
                serviceInfo.put("uri", instance.getUri());
            }
            
            services.put(serviceName, serviceInfo);
        }
        
        return services;
    }

    /**
     * Get documentation endpoints for all services
     * 
     * @return Map of documentation endpoints
     */
    private Map<String, String> getDocumentationEndpoints() {
        Map<String, String> endpoints = new HashMap<>();
        
        // Gateway documentation
        endpoints.put("gateway-swagger-ui", "http://localhost:" + gatewayPort + "/swagger-ui.html");
        endpoints.put("gateway-openapi", "http://localhost:" + gatewayPort + "/v3/api-docs");
        endpoints.put("gateway-health", "http://localhost:" + gatewayPort + "/actuator/health");
        
        // Service-specific documentation
        String[] serviceNames = {"user-service", "account-service", "transaction-service", "loan-service", "notification-service"};
        
        for (String serviceName : serviceNames) {
            String baseUrl = "http://localhost:" + gatewayPort + "/api/v1/docs/" + serviceName;
            endpoints.put(serviceName + "-swagger-ui", baseUrl + "/swagger-ui.html");
            endpoints.put(serviceName + "-openapi", "http://localhost:" + gatewayPort + "/api/v1/openapi/" + serviceName + "/v3/api-docs");
        }
        
        // Eureka dashboard
        endpoints.put("eureka-dashboard", eurekaUrl.replace("/eureka/", ""));
        
        return endpoints;
    }

    /**
     * Get health status of all services
     * 
     * @return Map of health status information
     */
    private Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        health.put("gateway", "UP");
        health.put("timestamp", LocalDateTime.now().toString());
        
        // Check service health asynchronously
        Map<String, Object> serviceHealth = new HashMap<>();
        String[] serviceNames = {"user-service", "account-service", "transaction-service", "loan-service", "notification-service"};
        
        for (String serviceName : serviceNames) {
            serviceHealth.put(serviceName, checkServiceHealth(serviceName));
        }
        
        health.put("services", serviceHealth);
        return health;
    }

    /**
     * Check health status of a specific service
     * 
     * @param serviceName Name of the service to check
     * @return Health status string
     */
    private String checkServiceHealth(String serviceName) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (instances.isEmpty()) {
                return "DOWN";
            }
            
            ServiceInstance instance = instances.get(0);
            String healthUrl = instance.getUri() + "/actuator/health";
            
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    restTemplate.getForObject(healthUrl, String.class);
                    return "UP";
                } catch (Exception e) {
                    log.warn("Health check failed for {}: {}", serviceName, e.getMessage());
                    return "DOWN";
                }
            });
            
            return future.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Health check failed for {}: {}", serviceName, e.getMessage());
            return "DOWN";
        }
    }

    /**
     * Get service statistics
     * 
     * @return Map of service statistics
     */
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<String> services = discoveryClient.getServices();
        stats.put("total-services", services.size());
        stats.put("active-services", services.stream()
                .mapToInt(service -> discoveryClient.getInstances(service).size())
                .sum());
        stats.put("timestamp", LocalDateTime.now().toString());
        
        // Service breakdown
        Map<String, Integer> serviceCounts = new HashMap<>();
        for (String service : services) {
            serviceCounts.put(service, discoveryClient.getInstances(service).size());
        }
        stats.put("service-breakdown", serviceCounts);
        
        return stats;
    }

    /**
     * Get documentation formats available
     * 
     * @return Map of available documentation formats
     */
    public Map<String, Object> getDocumentationFormats() {
        Map<String, Object> formats = new HashMap<>();
        
        Map<String, String> swaggerUi = new HashMap<>();
        swaggerUi.put("description", "Interactive API documentation with Swagger UI");
        swaggerUi.put("url-pattern", "/api/v1/docs/{service-name}/swagger-ui.html");
        swaggerUi.put("features", Arrays.asList("Interactive testing", "Request/Response examples", "Authentication support"));
        formats.put("swagger-ui", swaggerUi);
        
        Map<String, String> openApi = new HashMap<>();
        openApi.put("description", "OpenAPI 3.0 specification in JSON format");
        openApi.put("url-pattern", "/api/v1/openapi/{service-name}/v3/api-docs");
        openApi.put("features", Arrays.asList("Machine-readable", "Code generation", "API client generation"));
        formats.put("openapi-json", openApi);
        
        Map<String, String> openApiYaml = new HashMap<>();
        openApiYaml.put("description", "OpenAPI 3.0 specification in YAML format");
        openApiYaml.put("url-pattern", "/api/v1/openapi/{service-name}/v3/api-docs.yaml");
        openApiYaml.put("features", Arrays.asList("Human-readable", "Version control friendly", "Easy editing"));
        formats.put("openapi-yaml", openApiYaml);
        
        return formats;
    }
} 