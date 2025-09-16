package com.maelcolium.telepesa.transfer.client;

import com.maelcolium.telepesa.models.enums.TransactionType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "transaction-service", url = "${services.transaction-service.url:http://transaction-service:8083}", configuration = com.maelcolium.telepesa.transfer.config.FeignConfig.class)
public interface TransactionServiceClient {

    @PostMapping("/api/v1/transactions")
    TransactionResponse createTransaction(@RequestBody CreateTransactionRequest request);

    record CreateTransactionRequest(
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        TransactionType transactionType,
        String description,
        Long userId,
        BigDecimal feeAmount
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
