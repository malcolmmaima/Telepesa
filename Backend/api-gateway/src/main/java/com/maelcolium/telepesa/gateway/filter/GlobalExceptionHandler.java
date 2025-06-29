package com.maelcolium.telepesa.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for API Gateway
 * 
 * Provides consistent error responses for various failure scenarios
 * including service unavailability, authentication failures, and routing errors.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // Set response headers
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // Determine HTTP status and error details
        HttpStatus status = determineHttpStatus(ex);
        Map<String, Object> errorResponse = createErrorResponse(ex, status, exchange);
        
        // Log the error
        logError(ex, exchange, status);
        
        // Write error response
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
            } catch (JsonProcessingException e) {
                log.error("Error serializing error response", e);
                return bufferFactory.wrap("{\"error\":\"Internal Server Error\"}".getBytes());
            }
        }));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof NotFoundException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof ResponseStatusException) {
            return ((ResponseStatusException) ex).getStatusCode();
        } else if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof SecurityException) {
            return HttpStatus.UNAUTHORIZED;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private Map<String, Object> createErrorResponse(Throwable ex, HttpStatus status, ServerWebExchange exchange) {
        Map<String, Object> errorResponse = new HashMap<>();
        
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("method", exchange.getRequest().getMethod().name());
        
        // Add specific error details based on exception type
        if (ex instanceof NotFoundException) {
            errorResponse.put("message", "Service temporarily unavailable. Please try again later.");
            errorResponse.put("errorCode", "SERVICE_UNAVAILABLE");
        } else if (ex instanceof ResponseStatusException) {
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("errorCode", "HTTP_ERROR");
        } else if (ex instanceof IllegalArgumentException) {
            errorResponse.put("message", "Invalid request parameters");
            errorResponse.put("errorCode", "INVALID_REQUEST");
        } else if (ex instanceof SecurityException) {
            errorResponse.put("message", "Authentication required");
            errorResponse.put("errorCode", "UNAUTHORIZED");
        } else {
            errorResponse.put("message", "An unexpected error occurred");
            errorResponse.put("errorCode", "INTERNAL_ERROR");
        }
        
        // Add request ID for tracking
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        if (requestId != null) {
            errorResponse.put("requestId", requestId);
        }
        
        return errorResponse;
    }

    private void logError(Throwable ex, ServerWebExchange exchange, HttpStatus status) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null 
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
        
        if (status.is5xxServerError()) {
            log.error("Gateway error - Method: {}, Path: {}, Remote IP: {}, Status: {}, Error: {}", 
                method, path, remoteAddress, status.value(), ex.getMessage(), ex);
        } else {
            log.warn("Gateway warning - Method: {}, Path: {}, Remote IP: {}, Status: {}, Error: {}", 
                method, path, remoteAddress, status.value(), ex.getMessage());
        }
    }
} 