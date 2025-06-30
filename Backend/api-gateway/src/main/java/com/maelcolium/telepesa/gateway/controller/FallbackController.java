package com.maelcolium.telepesa.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return Mono.just(createFallbackResponse("User Service", "Service temporarily unavailable"));
    }

    @GetMapping("/account-service")
    public Mono<ResponseEntity<Map<String, Object>>> accountServiceFallback() {
        return Mono.just(createFallbackResponse("Account Service", "Service temporarily unavailable"));
    }

    @GetMapping("/transaction-service")
    public Mono<ResponseEntity<Map<String, Object>>> transactionServiceFallback() {
        return Mono.just(createFallbackResponse("Transaction Service", "Service temporarily unavailable"));
    }

    @GetMapping("/loan-service")
    public Mono<ResponseEntity<Map<String, Object>>> loanServiceFallback() {
        return Mono.just(createFallbackResponse("Loan Service", "Service temporarily unavailable"));
    }

    @GetMapping("/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        return Mono.just(createFallbackResponse("Notification Service", "Service temporarily unavailable"));
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String service, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("service", service);
        response.put("message", message);
        response.put("error", "Circuit breaker activated - service is down");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
} 