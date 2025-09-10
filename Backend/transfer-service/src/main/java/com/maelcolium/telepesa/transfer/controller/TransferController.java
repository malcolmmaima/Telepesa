package com.maelcolium.telepesa.transfer.controller;

import com.maelcolium.telepesa.transfer.dto.CreateTransferRequest;
import com.maelcolium.telepesa.transfer.dto.TransferResponse;
import com.maelcolium.telepesa.transfer.entity.Transfer;
import com.maelcolium.telepesa.transfer.service.TransferService;
import com.maelcolium.telepesa.transfer.service.TransferStatsResponse;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transfer Management", description = "APIs for money transfer operations")
public class TransferController {
    
    private final TransferService transferService;
    
    @PostMapping
    @Operation(summary = "Create a new transfer")
    public ResponseEntity<TransferResponse> createTransfer(
            @Parameter(description = "Sender account ID", required = true)
            @RequestHeader("X-Account-Id") String senderAccountId,
            @Valid @RequestBody CreateTransferRequest request) {
        
        log.info("Creating transfer request from account: {}", senderAccountId);
        TransferResponse response = transferService.createTransfer(senderAccountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{transferId}")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<TransferResponse> getTransfer(
            @Parameter(description = "Transfer ID", required = true)
            @PathVariable String transferId) {
        
        TransferResponse response = transferService.getTransferById(transferId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/reference/{transferReference}")
    @Operation(summary = "Get transfer by reference")
    public ResponseEntity<TransferResponse> getTransferByReference(
            @Parameter(description = "Transfer reference", required = true)
            @PathVariable String transferReference) {
        
        TransferResponse response = transferService.getTransferByReference(transferReference);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transfers for an account (sent and received)")
    public ResponseEntity<Page<TransferResponse>> getAccountTransfers(
            @Parameter(description = "Account ID", required = true)
            @PathVariable String accountId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TransferResponse> transfers = transferService.getTransfersByAccount(accountId, pageable);
        return ResponseEntity.ok(transfers);
    }
    
    @GetMapping("/sent/{accountId}")
    @Operation(summary = "Get sent transfers for an account")
    public ResponseEntity<Page<TransferResponse>> getSentTransfers(
            @Parameter(description = "Sender account ID", required = true)
            @PathVariable String accountId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TransferResponse> transfers = transferService.getSentTransfers(accountId, pageable);
        return ResponseEntity.ok(transfers);
    }
    
    @GetMapping("/received/{accountId}")
    @Operation(summary = "Get received transfers for an account")
    public ResponseEntity<Page<TransferResponse>> getReceivedTransfers(
            @Parameter(description = "Recipient account ID", required = true)
            @PathVariable String accountId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TransferResponse> transfers = transferService.getReceivedTransfers(accountId, pageable);
        return ResponseEntity.ok(transfers);
    }
    
    @PostMapping("/{transferId}/process")
    @Operation(summary = "Process a pending transfer")
    public ResponseEntity<TransferResponse> processTransfer(
            @Parameter(description = "Transfer ID", required = true)
            @PathVariable String transferId) {
        
        log.info("Processing transfer: {}", transferId);
        TransferResponse response = transferService.processTransfer(transferId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{transferId}/cancel")
    @Operation(summary = "Cancel a transfer")
    public ResponseEntity<TransferResponse> cancelTransfer(
            @Parameter(description = "Transfer ID", required = true)
            @PathVariable String transferId,
            @RequestBody Map<String, String> cancelRequest) {
        
        String reason = cancelRequest.getOrDefault("reason", "Cancelled by user");
        log.info("Cancelling transfer: {} with reason: {}", transferId, reason);
        TransferResponse response = transferService.cancelTransfer(transferId, reason);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{transferId}/retry")
    @Operation(summary = "Retry a failed transfer")
    public ResponseEntity<TransferResponse> retryTransfer(
            @Parameter(description = "Transfer ID", required = true)
            @PathVariable String transferId) {
        
        log.info("Retrying transfer: {}", transferId);
        TransferResponse response = transferService.retryTransfer(transferId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/{accountId}")
    @Operation(summary = "Get transfer statistics for an account")
    public ResponseEntity<TransferStatsResponse> getTransferStats(
            @Parameter(description = "Account ID", required = true)
            @PathVariable String accountId,
            @Parameter(description = "Statistics since date (ISO format)")
            @RequestParam(required = false) LocalDateTime since) {
        
        LocalDateTime sinceDate = since != null ? since : LocalDateTime.now().minusDays(30);
        TransferStatsResponse stats = transferService.getTransferStats(accountId, sinceDate);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get transfers by status")
    public ResponseEntity<List<TransferResponse>> getTransfersByStatus(
            @Parameter(description = "Transfer status", required = true)
            @PathVariable Transfer.TransferStatus status,
            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "100") int limit) {
        
        List<TransferResponse> transfers = transferService.getTransfersByStatus(status, limit);
        return ResponseEntity.ok(transfers);
    }
    
    @GetMapping("/fee/calculate")
    @Operation(summary = "Calculate transfer fee")
    public ResponseEntity<Map<String, Object>> calculateFee(
            @Parameter(description = "Transfer amount", required = true)
            @RequestParam BigDecimal amount,
            @Parameter(description = "Transfer type", required = true)
            @RequestParam Transfer.TransferType transferType) {
        
        BigDecimal fee = transferService.calculateTransferFee(amount, transferType);
        BigDecimal totalAmount = amount.add(fee);
        
        Map<String, Object> response = Map.of(
            "amount", amount,
            "transferType", transferType,
            "fee", fee,
            "totalAmount", totalAmount
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "transfer-service",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
