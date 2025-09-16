package com.maelcolium.telepesa.transaction.controller;

import com.maelcolium.telepesa.transaction.dto.CreateTransactionRequest;
import com.maelcolium.telepesa.transaction.dto.TransactionResponse;
import com.maelcolium.telepesa.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transactions/internal")
public class InternalTransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAuthority('SERVICE_TRANSACTION_WRITE')")
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse transaction = transactionService.createTransaction(request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/health")
    @PreAuthorize("hasAuthority('SERVICE_TRANSACTION_READ') or hasAuthority('SERVICE_TRANSACTION_WRITE')")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Internal transaction service is healthy");
    }
}
