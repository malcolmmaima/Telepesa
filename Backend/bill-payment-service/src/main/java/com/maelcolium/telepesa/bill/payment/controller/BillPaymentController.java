package com.maelcolium.telepesa.bill.payment.controller;

import com.maelcolium.telepesa.bill.payment.dto.BillPaymentResponse;
import com.maelcolium.telepesa.bill.payment.dto.CreateBillPaymentRequest;
import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import com.maelcolium.telepesa.bill.payment.service.BillPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bill Payment Management", description = "APIs for utility bill payment operations")
public class BillPaymentController {
    
    private final BillPaymentService billPaymentService;
    
    @PostMapping
    @Operation(summary = "Create a new bill payment")
    public ResponseEntity<BillPaymentResponse> createPayment(
            @Parameter(description = "Account ID", required = true)
            @RequestHeader("X-Account-Id") String accountId,
            @Valid @RequestBody CreateBillPaymentRequest request) {
        
        log.info("Creating bill payment request from account: {}", accountId);
        BillPaymentResponse response = billPaymentService.createPayment(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get bill payment by ID")
    public ResponseEntity<BillPaymentResponse> getPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable String paymentId) {
        
        BillPaymentResponse response = billPaymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/reference/{paymentReference}")
    @Operation(summary = "Get bill payment by reference")
    public ResponseEntity<BillPaymentResponse> getPaymentByReference(
            @Parameter(description = "Payment reference", required = true)
            @PathVariable String paymentReference) {
        
        BillPaymentResponse response = billPaymentService.getPaymentByReference(paymentReference);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get bill payments for an account")
    public ResponseEntity<Page<BillPaymentResponse>> getAccountPayments(
            @Parameter(description = "Account ID", required = true)
            @PathVariable String accountId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BillPaymentResponse> payments = billPaymentService.getPaymentsByAccount(accountId, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/type/{billType}")
    @Operation(summary = "Get bill payments by type")
    public ResponseEntity<Page<BillPaymentResponse>> getPaymentsByType(
            @Parameter(description = "Bill type", required = true)
            @PathVariable BillPayment.BillType billType,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BillPaymentResponse> payments = billPaymentService.getPaymentsByType(billType, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a bill payment")
    public ResponseEntity<BillPaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable String paymentId,
            @RequestBody Map<String, String> cancelRequest) {
        
        String reason = cancelRequest.getOrDefault("reason", "Cancelled by user");
        log.info("Cancelling bill payment: {} with reason: {}", paymentId, reason);
        BillPaymentResponse response = billPaymentService.cancelPayment(paymentId, reason);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{paymentId}/retry")
    @Operation(summary = "Retry a failed bill payment")
    public ResponseEntity<BillPaymentResponse> retryPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable String paymentId) {
        
        log.info("Retrying bill payment: {}", paymentId);
        BillPaymentResponse response = billPaymentService.retryPayment(paymentId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/fee/calculate")
    @Operation(summary = "Calculate service fee for bill payment")
    public ResponseEntity<Map<String, Object>> calculateFee(
            @Parameter(description = "Payment amount", required = true)
            @RequestParam BigDecimal amount,
            @Parameter(description = "Bill type", required = true)
            @RequestParam BillPayment.BillType billType,
            @Parameter(description = "Service provider", required = true)
            @RequestParam String provider) {
        
        BigDecimal serviceFee = billPaymentService.calculateServiceFee(amount, billType, provider);
        BigDecimal totalAmount = amount.add(serviceFee);
        
        Map<String, Object> response = Map.of(
            "amount", amount,
            "billType", billType,
            "provider", provider,
            "serviceFee", serviceFee,
            "totalAmount", totalAmount
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "bill-payment-service",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
