package com.maelcolium.telepesa.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Logging Filter for API Gateway
 * 
 * Logs all incoming requests and outgoing responses for monitoring,
 * debugging, and audit purposes. Includes request ID tracking and
 * performance metrics.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String requestId = generateRequestId();
        
        // Add request ID to headers
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("X-Request-ID", requestId)
            .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate().request(request).build();
        
        // Log incoming request
        logIncomingRequest(modifiedExchange, requestId);
        
        return chain.filter(modifiedExchange)
            .doFinally(signalType -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                // Log outgoing response
                logOutgoingResponse(modifiedExchange, requestId, duration);
            });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void logIncomingRequest(ServerWebExchange exchange, String requestId) {
        ServerHttpRequest request = exchange.getRequest();
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String remoteAddress = request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String contentType = request.getHeaders().getFirst("Content-Type");
        
        log.info("INCOMING REQUEST - ID: {}, Time: {}, Method: {}, Path: {}, Remote IP: {}, User-Agent: {}, Content-Type: {}", 
            requestId, timestamp, method, path, remoteAddress, 
            userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) : "unknown",
            contentType != null ? contentType : "unknown");
        
        // Log sensitive headers (without values)
        if (request.getHeaders().containsKey("Authorization")) {
            log.debug("INCOMING REQUEST - ID: {}, Authorization header present", requestId);
        }
    }

    private void logOutgoingResponse(ServerWebExchange exchange, String requestId, long duration) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int statusCode = exchange.getResponse().getStatusCode() != null 
            ? exchange.getResponse().getStatusCode().value() 
            : 0;
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        
        // Determine log level based on status code
        if (statusCode >= 400) {
            log.warn("OUTGOING RESPONSE - ID: {}, Time: {}, Method: {}, Path: {}, Status: {}, Duration: {}ms", 
                requestId, timestamp, method, path, statusCode, duration);
        } else {
            log.info("OUTGOING RESPONSE - ID: {}, Time: {}, Method: {}, Path: {}, Status: {}, Duration: {}ms", 
                requestId, timestamp, method, path, statusCode, duration);
        }
        
        // Log performance metrics for slow requests
        if (duration > 1000) {
            log.warn("SLOW REQUEST - ID: {}, Method: {}, Path: {}, Duration: {}ms (threshold: 1000ms)", 
                requestId, method, path, duration);
        }
    }
} 