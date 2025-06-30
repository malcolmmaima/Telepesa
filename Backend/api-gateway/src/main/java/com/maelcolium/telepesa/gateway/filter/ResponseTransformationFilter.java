package com.maelcolium.telepesa.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
// import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response Transformation Filter for API Gateway
 * 
 * Standardizes API responses by adding metadata like request ID,
 * processing time, and gateway information. Ensures consistent
 * response format across all microservices.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Slf4j
// @Component
public class ResponseTransformationFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public ResponseTransformationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                try {
                    transformResponse(exchange, startTime);
                } catch (Exception e) {
                    log.error("Error transforming response", e);
                }
            }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    private void transformResponse(ServerWebExchange exchange, long startTime) {
        ServerHttpResponse response = exchange.getResponse();
        // Only transform JSON responses
        if (!isJsonResponse(response)) {
            return;
        }
        // Get request ID from headers
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        // Calculate processing time
        long processingTime = System.currentTimeMillis() - startTime;
        // Add response headers
        addResponseHeaders(response, requestId, processingTime);
        // TODO: For full response body transformation, use ServerHttpResponseDecorator in a WebFilter.
    }

    private boolean isJsonResponse(ServerHttpResponse response) {
        String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
    }

    private void addResponseHeaders(ServerHttpResponse response, String requestId, long processingTime) {
        HttpHeaders headers = response.getHeaders();
        
        if (requestId != null) {
            headers.add("X-Request-ID", requestId);
        }
        
        headers.add("X-Processing-Time", String.valueOf(processingTime));
        headers.add("X-Gateway-Timestamp", LocalDateTime.now().format(FORMATTER));
        headers.add("X-Gateway-Version", "1.0.0");
        headers.add("X-Gateway-Service", "api-gateway");
    }

    private String transformResponseBody(String responseBody, String requestId, long processingTime) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                
                // Add metadata
                objectNode.put("requestId", requestId != null ? requestId : "unknown");
                objectNode.put("processingTime", processingTime);
                objectNode.put("gatewayTimestamp", LocalDateTime.now().format(FORMATTER));
                objectNode.put("gatewayVersion", "1.0.0");
                objectNode.put("gatewayService", "api-gateway");
                
                return objectMapper.writeValueAsString(objectNode);
            } else {
                // For non-object responses, wrap in a standard format
                ObjectNode wrapper = objectMapper.createObjectNode();
                wrapper.set("data", jsonNode);
                wrapper.put("requestId", requestId != null ? requestId : "unknown");
                wrapper.put("processingTime", processingTime);
                wrapper.put("gatewayTimestamp", LocalDateTime.now().format(FORMATTER));
                wrapper.put("gatewayVersion", "1.0.0");
                wrapper.put("gatewayService", "api-gateway");
                
                return objectMapper.writeValueAsString(wrapper);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing response body for transformation", e);
            return responseBody; // Return original response if transformation fails
        }
    }
} 