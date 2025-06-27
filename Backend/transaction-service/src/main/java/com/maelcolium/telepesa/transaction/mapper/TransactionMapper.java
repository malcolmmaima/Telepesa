package com.maelcolium.telepesa.transaction.mapper;

import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.transaction.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionDto.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .referenceNumber(transaction.getReferenceNumber())
                .processedAt(transaction.getProcessedAt())
                .userId(transaction.getUserId())
                .feeAmount(transaction.getFeeAmount())
                .totalAmount(transaction.getTotalAmount())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    public Transaction toEntity(TransactionDto transactionDto) {
        if (transactionDto == null) {
            return null;
        }

        return Transaction.builder()
                .transactionId(transactionDto.getTransactionId())
                .fromAccountId(transactionDto.getFromAccountId())
                .toAccountId(transactionDto.getToAccountId())
                .amount(transactionDto.getAmount())
                .transactionType(transactionDto.getTransactionType())
                .status(transactionDto.getStatus())
                .description(transactionDto.getDescription())
                .referenceNumber(transactionDto.getReferenceNumber())
                .processedAt(transactionDto.getProcessedAt())
                .userId(transactionDto.getUserId())
                .feeAmount(transactionDto.getFeeAmount())
                .totalAmount(transactionDto.getTotalAmount())
                .build();
    }
} 