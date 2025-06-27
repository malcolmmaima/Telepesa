package com.maelcolium.telepesa.transaction.controller;

import com.maelcolium.telepesa.transaction.dto.CreateTransactionRequest;
import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.transaction.service.TransactionService;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionDto transaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable Long id) {
        TransactionDto transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/by-transaction-id/{transactionId}")
    public ResponseEntity<TransactionDto> getTransactionByTransactionId(@PathVariable String transactionId) {
        TransactionDto transaction = transactionService.getTransactionByTransactionId(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactions(PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByUserId(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByAccountId(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByAccountId(accountId, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByStatus(
            @PathVariable TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByStatus(status, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByType(
            @PathVariable TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByType(type, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionDto> updateTransactionStatus(
            @PathVariable Long id,
            @RequestBody TransactionStatus status) {
        TransactionDto transaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/account/{accountId}/history")
    public ResponseEntity<List<TransactionDto>> getAccountTransactionHistory(@PathVariable Long accountId) {
        List<TransactionDto> transactions = transactionService.getAccountTransactionHistory(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long accountId) {
        BigDecimal balance = transactionService.getAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/account/{accountId}/debits")
    public ResponseEntity<BigDecimal> getTotalDebitsByAccountId(
            @PathVariable Long accountId,
            @RequestParam LocalDateTime since) {
        BigDecimal total = transactionService.getTotalDebitsByAccountId(accountId, since);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/account/{accountId}/credits")
    public ResponseEntity<BigDecimal> getTotalCreditsByAccountId(
            @PathVariable Long accountId,
            @RequestParam LocalDateTime since) {
        BigDecimal total = transactionService.getTotalCreditsByAccountId(accountId, since);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getTransactionCountByUserIdAndStatus(
            @PathVariable Long userId,
            @RequestParam TransactionStatus status) {
        long count = transactionService.getTransactionCountByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(count);
    }
} 