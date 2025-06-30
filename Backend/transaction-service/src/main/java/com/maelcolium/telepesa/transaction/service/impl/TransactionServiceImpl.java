package com.maelcolium.telepesa.transaction.service.impl;

import com.maelcolium.telepesa.transaction.dto.CreateTransactionRequest;
import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.transaction.mapper.TransactionMapper;
import com.maelcolium.telepesa.transaction.model.Transaction;
import com.maelcolium.telepesa.transaction.repository.TransactionRepository;
import com.maelcolium.telepesa.transaction.service.TransactionService;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @CacheEvict(value = {"transactions", "transaction-history", "account-balances"}, allEntries = true)
    public TransactionDto createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction for user: {}, amount: {}", request.getUserId(), request.getAmount());

        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .referenceNumber(generateReferenceNumber())
                .userId(request.getUserId())
                .feeAmount(calculateFee(request.getAmount(), request.getTransactionType()))
                .totalAmount(request.getAmount().add(calculateFee(request.getAmount(), request.getTransactionType())))
                .processedAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getTransactionId());

        return transactionMapper.toDto(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transactions", key = "#id")
    public TransactionDto getTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return transactionMapper.toDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transactions", key = "'transactionId:' + #transactionId")
    public TransactionDto getTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with transaction ID: " + transactionId));
        return transactionMapper.toDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-history", key = "'user:' + #userId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<TransactionDto> getTransactionsByUserId(Long userId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        return transactions.map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-history", key = "'account:' + #accountId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<TransactionDto> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable);
        return transactions.map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-history", key = "'status:' + #status + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<TransactionDto> getTransactionsByStatus(TransactionStatus status, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByStatus(status, pageable);
        return transactions.map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-history", key = "'type:' + #transactionType + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<TransactionDto> getTransactionsByType(TransactionType transactionType, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByTransactionType(transactionType, pageable);
        return transactions.map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-history", key = "'user:' + #userId + ':dateRange:' + #startDate + ':' + #endDate + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<TransactionDto> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable);
        return transactions.map(transactionMapper::toDto);
    }

    @Override
    @CacheEvict(value = {"transactions", "transaction-history", "account-balances"}, allEntries = true)
    public TransactionDto updateTransactionStatus(Long id, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        transaction.setStatus(status);
        if (status == TransactionStatus.COMPLETED) {
            transaction.setProcessedAt(LocalDateTime.now());
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction status updated to {} for transaction ID: {}", status, updatedTransaction.getTransactionId());

        return transactionMapper.toDto(updatedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-history", key = "'accountHistory:' + #accountId")
    public List<TransactionDto> getAccountTransactionHistory(Long accountId) {
        List<Transaction> transactions = transactionRepository.findAllByAccountId(accountId);
        return transactions.stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "account-balances", key = "#accountId")
    public BigDecimal getAccountBalance(Long accountId) {
        // Calculate balance by subtracting debits from credits
        BigDecimal credits = transactionRepository.getTotalCreditsByAccountId(accountId, LocalDateTime.of(2020, 1, 1, 0, 0));
        BigDecimal debits = transactionRepository.getTotalDebitsByAccountId(accountId, LocalDateTime.of(2020, 1, 1, 0, 0));
        
        credits = credits != null ? credits : BigDecimal.ZERO;
        debits = debits != null ? debits : BigDecimal.ZERO;
        
        return credits.subtract(debits);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-limits", key = "'debits:' + #accountId + ':' + #since")
    public BigDecimal getTotalDebitsByAccountId(Long accountId, LocalDateTime since) {
        BigDecimal total = transactionRepository.getTotalDebitsByAccountId(accountId, since);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-limits", key = "'credits:' + #accountId + ':' + #since")
    public BigDecimal getTotalCreditsByAccountId(Long accountId, LocalDateTime since) {
        BigDecimal total = transactionRepository.getTotalCreditsByAccountId(accountId, since);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-limits", key = "'count:' + #userId + ':' + #status")
    public long getTransactionCountByUserIdAndStatus(Long userId, TransactionStatus status) {
        return transactionRepository.countByUserIdAndStatus(userId, status);
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateReferenceNumber() {
        return "REF-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private BigDecimal calculateFee(BigDecimal amount, TransactionType transactionType) {
        // Simple fee calculation - in real implementation, this would be more complex
        switch (transactionType) {
            case TRANSFER:
                return amount.multiply(new BigDecimal("0.01")); // 1% fee
            case DEPOSIT:
                return BigDecimal.ZERO; // No fee for deposits
            case WITHDRAWAL:
                return new BigDecimal("50"); // Fixed fee
            default:
                return BigDecimal.ZERO;
        }
    }
}
