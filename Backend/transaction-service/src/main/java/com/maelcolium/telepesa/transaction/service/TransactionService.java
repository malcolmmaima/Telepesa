package com.maelcolium.telepesa.transaction.service;

import com.maelcolium.telepesa.transaction.dto.CreateTransactionRequest;
import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    TransactionDto createTransaction(CreateTransactionRequest request);

    TransactionDto getTransaction(Long id);

    TransactionDto getTransactionByTransactionId(String transactionId);

    Page<TransactionDto> getTransactions(Pageable pageable);

    Page<TransactionDto> getTransactionsByUserId(Long userId, Pageable pageable);

    Page<TransactionDto> getTransactionsByAccountId(Long accountId, Pageable pageable);

    Page<TransactionDto> getTransactionsByStatus(TransactionStatus status, Pageable pageable);

    Page<TransactionDto> getTransactionsByType(TransactionType transactionType, Pageable pageable);

    Page<TransactionDto> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    TransactionDto updateTransactionStatus(Long id, TransactionStatus status);

    List<TransactionDto> getAccountTransactionHistory(Long accountId);

    BigDecimal getAccountBalance(Long accountId);

    BigDecimal getTotalDebitsByAccountId(Long accountId, LocalDateTime since);

    BigDecimal getTotalCreditsByAccountId(Long accountId, LocalDateTime since);

    long getTransactionCountByUserIdAndStatus(Long userId, TransactionStatus status);
} 