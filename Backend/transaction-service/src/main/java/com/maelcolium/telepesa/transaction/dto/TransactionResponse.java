package com.maelcolium.telepesa.transaction.dto;

import com.maelcolium.telepesa.transaction.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction operations.
 * Provides a clean API response structure for transaction data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private Long id;
    private String transactionId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String description;
    private String status;
    private String transactionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String referenceNumber;
    private LocalDateTime processedAt;
    private Long userId;
    private BigDecimal feeAmount;
    private BigDecimal totalAmount;
    
    /**
     * Creates a TransactionResponse from a Transaction entity.
     * 
     * @param transaction the transaction entity
     * @return TransactionResponse instance
     */
    public static TransactionResponse fromTransaction(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .transactionType(transaction.getTransactionType().name())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .referenceNumber(transaction.getReferenceNumber())
                .processedAt(transaction.getProcessedAt())
                .userId(transaction.getUserId())
                .feeAmount(transaction.getFeeAmount())
                .totalAmount(transaction.getTotalAmount())
                .build();
    }
}
