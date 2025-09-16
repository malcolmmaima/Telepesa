package com.maelcolium.telepesa.transfer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "transaction-service", url = "${services.transaction-service.url:http://localhost:8083}")
public interface TransactionServiceClient {

    @PostMapping("/api/v1/transactions")
    TransactionResponse createTransaction(@RequestBody CreateTransactionRequest request);

    record CreateTransactionRequest(
        Long accountId,
        BigDecimal amount,
        String transactionType,
        String description,
        Long recipientAccountId,
        String recipientAccountNumber,
        String referenceNumber,
        BigDecimal feeAmount,
        BigDecimal totalAmount,
        String currencyCode
    ) {}

    record TransactionResponse(
        Long id,
        String transactionId,
        Long accountId,
        BigDecimal amount,
        String transactionType,
        String status,
        String description,
        String referenceNumber,
        BigDecimal feeAmount,
        BigDecimal totalAmount,
        String currencyCode,
        String createdAt
    ) {}
}
