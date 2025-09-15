package com.maelcolium.telepesa.transaction.service;

import com.maelcolium.telepesa.transaction.dto.CreateTransactionRequest;
import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.transaction.mapper.TransactionMapper;
import com.maelcolium.telepesa.transaction.model.Transaction;
import com.maelcolium.telepesa.transaction.repository.TransactionRepository;
import com.maelcolium.telepesa.transaction.service.impl.TransactionServiceImpl;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Additional unit tests to improve code coverage for TransactionService
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceAdditionalTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .transactionId("TXN-12345678")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transaction")
                .referenceNumber("REF-123456")
                .userId(10L)
                .feeAmount(new BigDecimal("1.00"))
                .totalAmount(new BigDecimal("101.00"))
                .processedAt(LocalDateTime.now())
                .build();

        transactionDto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-12345678")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transaction")
                .referenceNumber("REF-123456")
                .userId(10L)
                .feeAmount(new BigDecimal("1.00"))
                .totalAmount(new BigDecimal("101.00"))
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTransaction_WithDepositType_ShouldHaveZeroFee() {
        // Given
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("500.00"))
                .transactionType(TransactionType.DEPOSIT)
                .description("Deposit transaction")
                .userId(10L)
                .build();

        Transaction depositTransaction = Transaction.builder()
                .transactionId("TXN-DEPOSIT")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("500.00"))
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .description("Deposit transaction")
                .userId(10L)
                .feeAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(depositTransaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.createTransaction(request);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> 
            t.getFeeAmount().equals(BigDecimal.ZERO) && 
            t.getTransactionType() == TransactionType.DEPOSIT));
        verify(transactionMapper).toDto(any(Transaction.class));
    }

    @Test
    void createTransaction_WithWithdrawalType_ShouldHaveFixedFee() {
        // Given
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("200.00"))
                .transactionType(TransactionType.WITHDRAWAL)
                .description("Withdrawal transaction")
                .userId(10L)
                .build();

        Transaction withdrawalTransaction = Transaction.builder()
                .transactionId("TXN-WITHDRAWAL")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("200.00"))
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .description("Withdrawal transaction")
                .userId(10L)
                .feeAmount(new BigDecimal("50"))
                .totalAmount(new BigDecimal("250.00"))
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(withdrawalTransaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.createTransaction(request);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> 
            t.getFeeAmount().equals(new BigDecimal("50")) && 
            t.getTransactionType() == TransactionType.WITHDRAWAL));
        verify(transactionMapper).toDto(any(Transaction.class));
    }

    @Test
    void getTransactionByTransactionId_ShouldReturnTransaction_WhenExists() {
        // Given
        String transactionId = "TXN-12345678";
        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.getTransactionByTransactionId(transactionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(transactionId);
        verify(transactionRepository).findByTransactionId(transactionId);
        verify(transactionMapper).toDto(transaction);
    }

    @Test
    void getTransactionByTransactionId_ShouldThrowException_WhenNotFound() {
        // Given
        String transactionId = "TXN-NOTFOUND";
        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionByTransactionId(transactionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with transaction ID: " + transactionId);

        verify(transactionRepository).findByTransactionId(transactionId);
        verify(transactionMapper, never()).toDto(any());
    }

    @Test
    void getTransactions_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
        
        when(transactionRepository.findAll(pageable)).thenReturn(transactionPage);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactions(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(transactionRepository).findAll(pageable);
    }

    @Test
    void getTransactionsByUserId_ShouldReturnUserTransactions() {
        // Given
        Long userId = 10L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
        
        when(transactionRepository.findByUserId(userId, pageable)).thenReturn(transactionPage);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByUserId(userId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByUserId(userId, pageable);
    }

    @Test
    void getTransactionsByAccountId_ShouldReturnAccountTransactions() {
        // Given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
        
        when(transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable))
                .thenReturn(transactionPage);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByAccountId(accountId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByFromAccountIdOrToAccountId(accountId, accountId, pageable);
    }

    @Test
    void getTransactionsByStatus_ShouldReturnFilteredTransactions() {
        // Given
        TransactionStatus status = TransactionStatus.COMPLETED;
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
        
        when(transactionRepository.findByStatus(status, pageable)).thenReturn(transactionPage);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByStatus(status, pageable);
    }

    @Test
    void getTransactionsByType_ShouldReturnFilteredTransactions() {
        // Given
        TransactionType type = TransactionType.TRANSFER;
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
        
        when(transactionRepository.findByTransactionType(type, pageable)).thenReturn(transactionPage);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByType(type, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByTransactionType(type, pageable);
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnFilteredTransactions() {
        // Given
        Long userId = 10L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
        
        when(transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable))
                .thenReturn(transactionPage);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByDateRange(userId, startDate, endDate, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByUserIdAndDateRange(userId, startDate, endDate, pageable);
    }

    @Test
    void updateTransactionStatus_ToCompleted_ShouldUpdateProcessedAt() {
        // Given
        Long transactionId = 1L;
        TransactionStatus newStatus = TransactionStatus.COMPLETED;
        
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.updateTransactionStatus(transactionId, newStatus);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).save(argThat(t -> 
            t.getStatus() == TransactionStatus.COMPLETED && 
            t.getProcessedAt() != null));
        verify(transactionMapper).toDto(any(Transaction.class));
    }

    @Test
    void updateTransactionStatus_ToFailed_ShouldNotUpdateProcessedAt() {
        // Given
        Long transactionId = 1L;
        TransactionStatus newStatus = TransactionStatus.FAILED;
        LocalDateTime originalProcessedAt = transaction.getProcessedAt();
        
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.updateTransactionStatus(transactionId, newStatus);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).save(argThat(t -> 
            t.getStatus() == TransactionStatus.FAILED));
        verify(transactionMapper).toDto(any(Transaction.class));
    }

    @Test
    void getAccountTransactionHistory_ShouldReturnAllTransactions() {
        // Given
        Long accountId = 1L;
        List<Transaction> transactions = Arrays.asList(transaction);
        
        when(transactionRepository.findAllByAccountId(accountId)).thenReturn(transactions);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        List<TransactionDto> result = transactionService.getAccountTransactionHistory(accountId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(transactionRepository).findAllByAccountId(accountId);
    }

    @Test
    void getAccountBalance_WithNullCreditsAndDebits_ShouldReturnZero() {
        // Given
        Long accountId = 1L;
        LocalDateTime since = LocalDateTime.of(2020, 1, 1, 0, 0);
        
        when(transactionRepository.getTotalCreditsByAccountId(accountId, since)).thenReturn(null);
        when(transactionRepository.getTotalDebitsByAccountId(accountId, since)).thenReturn(null);

        // When
        BigDecimal result = transactionService.getAccountBalance(accountId);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(transactionRepository).getTotalCreditsByAccountId(accountId, since);
        verify(transactionRepository).getTotalDebitsByAccountId(accountId, since);
    }

    @Test
    void getAccountBalance_WithValidCreditsAndDebits_ShouldCalculateCorrectly() {
        // Given
        Long accountId = 1L;
        LocalDateTime since = LocalDateTime.of(2020, 1, 1, 0, 0);
        BigDecimal credits = new BigDecimal("1000.00");
        BigDecimal debits = new BigDecimal("300.00");
        
        when(transactionRepository.getTotalCreditsByAccountId(accountId, since)).thenReturn(credits);
        when(transactionRepository.getTotalDebitsByAccountId(accountId, since)).thenReturn(debits);

        // When
        BigDecimal result = transactionService.getAccountBalance(accountId);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("700.00"));
        verify(transactionRepository).getTotalCreditsByAccountId(accountId, since);
        verify(transactionRepository).getTotalDebitsByAccountId(accountId, since);
    }

    @Test
    void getTotalDebitsByAccountId_WithNullResult_ShouldReturnZero() {
        // Given
        Long accountId = 1L;
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        
        when(transactionRepository.getTotalDebitsByAccountId(accountId, since)).thenReturn(null);

        // When
        BigDecimal result = transactionService.getTotalDebitsByAccountId(accountId, since);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(transactionRepository).getTotalDebitsByAccountId(accountId, since);
    }

    @Test
    void getTotalCreditsByAccountId_WithNullResult_ShouldReturnZero() {
        // Given
        Long accountId = 1L;
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        
        when(transactionRepository.getTotalCreditsByAccountId(accountId, since)).thenReturn(null);

        // When
        BigDecimal result = transactionService.getTotalCreditsByAccountId(accountId, since);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(transactionRepository).getTotalCreditsByAccountId(accountId, since);
    }

    @Test
    void getTransactionCountByUserIdAndStatus_ShouldReturnCount() {
        // Given
        Long userId = 10L;
        TransactionStatus status = TransactionStatus.COMPLETED;
        long expectedCount = 5L;
        
        when(transactionRepository.countByUserIdAndStatus(userId, status)).thenReturn(expectedCount);

        // When
        long result = transactionService.getTransactionCountByUserIdAndStatus(userId, status);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(transactionRepository).countByUserIdAndStatus(userId, status);
    }
}
