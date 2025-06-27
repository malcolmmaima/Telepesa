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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;
    private TransactionDto transactionDto;
    private CreateTransactionRequest createRequest;

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
                .referenceNumber("REF-1234")
                .userId(10L)
                .feeAmount(new BigDecimal("1.00"))
                .totalAmount(new BigDecimal("101.00"))
                .processedAt(LocalDateTime.now())
                .build();
        transaction.setId(1L);

        transactionDto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-12345678")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transaction")
                .referenceNumber("REF-1234")
                .userId(10L)
                .feeAmount(new BigDecimal("1.00"))
                .totalAmount(new BigDecimal("101.00"))
                .processedAt(LocalDateTime.now())
                .build();

        createRequest = CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .description("Test transaction")
                .userId(10L)
                .build();
    }

    @Test
    void createTransaction_WithValidRequest_ShouldReturnTransactionDto() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.createTransaction(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("TXN-12345678");
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).toDto(any(Transaction.class));
    }

    @Test
    void createTransaction_WithTransferType_ShouldCalculateCorrectFee() {
        // Given
        createRequest.setTransactionType(TransactionType.TRANSFER);
        createRequest.setAmount(new BigDecimal("1000.00"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.createTransaction(createRequest);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        assertThat(result).isNotNull();
    }

    @Test
    void createTransaction_WithDepositType_ShouldHaveZeroFee() {
        // Given
        createRequest.setTransactionType(TransactionType.DEPOSIT);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.createTransaction(createRequest);

        // Then
        verify(transactionRepository).save(argThat(t -> 
            t.getFeeAmount().equals(BigDecimal.ZERO)
        ));
    }

    @Test
    void getTransaction_WithValidId_ShouldReturnTransactionDto() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.getTransaction(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("TXN-12345678");
        verify(transactionRepository).findById(1L);
        verify(transactionMapper).toDto(transaction);
    }

    @Test
    void getTransaction_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransaction(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: 999");
        verify(transactionRepository).findById(999L);
        verify(transactionMapper, never()).toDto(any());
    }

    @Test
    void getTransactionByTransactionId_WithValidId_ShouldReturnTransactionDto() {
        // Given
        when(transactionRepository.findByTransactionId("TXN-12345678")).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.getTransactionByTransactionId("TXN-12345678");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("TXN-12345678");
        verify(transactionRepository).findByTransactionId("TXN-12345678");
    }

    @Test
    void getTransactionByTransactionId_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Given
        when(transactionRepository.findByTransactionId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionByTransactionId("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with transaction ID: INVALID");
    }

    @Test
    void getTransactions_ShouldReturnPagedResults() {
        // Given
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactions(PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionId()).isEqualTo("TXN-12345678");
    }

    @Test
    void getTransactionsByUserId_ShouldReturnUserTransactions() {
        // Given
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByUserId(10L, PageRequest.of(0, 10))).thenReturn(page);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByUserId(10L, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByUserId(10L, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByAccountId_ShouldReturnAccountTransactions() {
        // Given
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByFromAccountIdOrToAccountId(1L, 1L, PageRequest.of(0, 10))).thenReturn(page);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByAccountId(1L, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByFromAccountIdOrToAccountId(1L, 1L, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByStatus_ShouldReturnFilteredTransactions() {
        // Given
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByStatus(TransactionStatus.PENDING, PageRequest.of(0, 10))).thenReturn(page);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByStatus(TransactionStatus.PENDING, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByStatus(TransactionStatus.PENDING, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByType_ShouldReturnFilteredTransactions() {
        // Given
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByTransactionType(TransactionType.TRANSFER, PageRequest.of(0, 10))).thenReturn(page);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByType(TransactionType.TRANSFER, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByTransactionType(TransactionType.TRANSFER, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnFilteredTransactions() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByUserIdAndDateRange(10L, startDate, endDate, PageRequest.of(0, 10))).thenReturn(page);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        Page<TransactionDto> result = transactionService.getTransactionsByDateRange(10L, startDate, endDate, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByUserIdAndDateRange(10L, startDate, endDate, PageRequest.of(0, 10));
    }

    @Test
    void updateTransactionStatus_WithValidId_ShouldUpdateStatus() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.updateTransactionStatus(1L, TransactionStatus.COMPLETED);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getStatus() == TransactionStatus.COMPLETED));
    }

    @Test
    void updateTransactionStatus_WithCompletedStatus_ShouldSetProcessedAt() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        TransactionDto result = transactionService.updateTransactionStatus(1L, TransactionStatus.COMPLETED);

        // Then
        verify(transactionRepository).save(argThat(t -> t.getProcessedAt() != null));
    }

    @Test
    void updateTransactionStatus_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransactionStatus(999L, TransactionStatus.COMPLETED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: 999");
    }

    @Test
    void getAccountTransactionHistory_ShouldReturnTransactionList() {
        // Given
        when(transactionRepository.findAllByAccountId(1L)).thenReturn(List.of(transaction));
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        // When
        List<TransactionDto> result = transactionService.getAccountTransactionHistory(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionId()).isEqualTo("TXN-12345678");
        verify(transactionRepository).findAllByAccountId(1L);
    }

    @Test
    void getAccountBalance_ShouldReturnCorrectBalance() {
        // Given
        when(transactionRepository.getTotalCreditsByAccountId(eq(1L), any(LocalDateTime.class))).thenReturn(new BigDecimal("200.00"));
        when(transactionRepository.getTotalDebitsByAccountId(eq(1L), any(LocalDateTime.class))).thenReturn(new BigDecimal("50.00"));

        // When
        BigDecimal balance = transactionService.getAccountBalance(1L);

        // Then
        assertThat(balance).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void getAccountBalance_WithNullCredits_ShouldReturnCorrectBalance() {
        // Given
        when(transactionRepository.getTotalCreditsByAccountId(eq(1L), any(LocalDateTime.class))).thenReturn(null);
        when(transactionRepository.getTotalDebitsByAccountId(eq(1L), any(LocalDateTime.class))).thenReturn(new BigDecimal("50.00"));

        // When
        BigDecimal balance = transactionService.getAccountBalance(1L);

        // Then
        assertThat(balance).isEqualTo(new BigDecimal("-50.00"));
    }

    @Test
    void getAccountBalance_WithNullDebits_ShouldReturnCorrectBalance() {
        // Given
        when(transactionRepository.getTotalCreditsByAccountId(eq(1L), any(LocalDateTime.class))).thenReturn(new BigDecimal("200.00"));
        when(transactionRepository.getTotalDebitsByAccountId(eq(1L), any(LocalDateTime.class))).thenReturn(null);

        // When
        BigDecimal balance = transactionService.getAccountBalance(1L);

        // Then
        assertThat(balance).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    void getTotalDebitsByAccountId_ShouldReturnCorrectAmount() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        when(transactionRepository.getTotalDebitsByAccountId(1L, since)).thenReturn(new BigDecimal("100.00"));

        // When
        BigDecimal result = transactionService.getTotalDebitsByAccountId(1L, since);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void getTotalDebitsByAccountId_WithNullResult_ShouldReturnZero() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        when(transactionRepository.getTotalDebitsByAccountId(1L, since)).thenReturn(null);

        // When
        BigDecimal result = transactionService.getTotalDebitsByAccountId(1L, since);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getTotalCreditsByAccountId_ShouldReturnCorrectAmount() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        when(transactionRepository.getTotalCreditsByAccountId(1L, since)).thenReturn(new BigDecimal("200.00"));

        // When
        BigDecimal result = transactionService.getTotalCreditsByAccountId(1L, since);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    void getTotalCreditsByAccountId_WithNullResult_ShouldReturnZero() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        when(transactionRepository.getTotalCreditsByAccountId(1L, since)).thenReturn(null);

        // When
        BigDecimal result = transactionService.getTotalCreditsByAccountId(1L, since);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getTransactionCountByUserIdAndStatus_ShouldReturnCorrectCount() {
        // Given
        when(transactionRepository.countByUserIdAndStatus(10L, TransactionStatus.COMPLETED)).thenReturn(5L);

        // When
        long result = transactionService.getTransactionCountByUserIdAndStatus(10L, TransactionStatus.COMPLETED);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(transactionRepository).countByUserIdAndStatus(10L, TransactionStatus.COMPLETED);
    }
} 