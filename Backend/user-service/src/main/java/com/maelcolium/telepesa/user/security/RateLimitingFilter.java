package com.maelcolium.telepesa.user.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting filter to prevent brute force attacks and API abuse
 * Implements a sliding window rate limiter with configurable limits per endpoint
 */
@Slf4j
@Component
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_LOGIN_ATTEMPTS_PER_MINUTE = 5;
    private static final int MAX_REGISTRATION_ATTEMPTS_PER_MINUTE = 3;
    private static final long WINDOW_SIZE_MINUTES = 1;
    
    // Store for tracking requests per IP
    private final ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        String clientIp = getClientIpAddress(request);
        String requestUri = request.getRequestURI();
        
        // Skip rate limiting for health checks and static resources
        if (shouldSkipRateLimit(requestUri)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        int maxRequests = getMaxRequestsForEndpoint(requestUri);
        
        if (isRateLimited(clientIp, maxRequests)) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, requestUri);
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"retryAfter\":60}"
            );
            return;
        }
        
        // Continue with the request
        filterChain.doFilter(servletRequest, servletResponse);
    }
    
    private boolean isRateLimited(String clientIp, int maxRequests) {
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (WINDOW_SIZE_MINUTES * 60 * 1000);
        
        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());
        
        // Clean old entries
        counter.cleanOldEntries(windowStart);
        
        // Check if limit exceeded
        if (counter.getCount() >= maxRequests) {
            return true;
        }
        
        // Add current request
        counter.addRequest(currentTime);
        return false;
    }
    
    private int getMaxRequestsForEndpoint(String requestUri) {
        if (requestUri.contains("/auth/login")) {
            return MAX_LOGIN_ATTEMPTS_PER_MINUTE;
        } else if (requestUri.contains("/auth/register")) {
            return MAX_REGISTRATION_ATTEMPTS_PER_MINUTE;
        }
        return MAX_REQUESTS_PER_MINUTE;
    }
    
    private boolean shouldSkipRateLimit(String requestUri) {
        return requestUri.contains("/actuator/health") || 
               requestUri.contains("/swagger-ui") || 
               requestUri.contains("/api-docs") ||
               requestUri.endsWith(".css") ||
               requestUri.endsWith(".js") ||
               requestUri.endsWith(".ico");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Request counter with sliding window implementation
     */
    private static class RequestCounter {
        private final ConcurrentHashMap<Long, AtomicInteger> timeSlots = new ConcurrentHashMap<>();
        
        public void addRequest(long timestamp) {
            long timeSlot = timestamp / (60 * 1000); // 1-minute slots
            timeSlots.computeIfAbsent(timeSlot, k -> new AtomicInteger(0)).incrementAndGet();
        }
        
        public int getCount() {
            return timeSlots.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
        }
        
        public void cleanOldEntries(long windowStart) {
            long windowStartSlot = windowStart / (60 * 1000);
            timeSlots.entrySet().removeIf(entry -> entry.getKey() < windowStartSlot);
        }
    }
} 