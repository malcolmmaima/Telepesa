package com.maelcolium.telepesa.transaction.repository;

import com.maelcolium.telepesa.transaction.model.Transaction;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findByTransactionId_WithExistingTransaction_ShouldReturnTransaction() {
        // Given
        Transaction transaction = createTestTransaction();
        entityManager.persistAndFlush(transaction);

        // When
        Optional<Transaction> result = transactionRepository.findByTransactionId("TXN-12345678");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-12345678");
    }

    @Test
    void findByTransactionId_WithNonExistingTransaction_ShouldReturnEmpty() {
        // When
        Optional<Transaction> result = transactionRepository.findByTransactionId("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByReferenceNumber_WithExistingTransaction_ShouldReturnTransaction() {
        // Given
        Transaction transaction = createTestTransaction();
        transaction.setReferenceNumber("REF-1234-1");
        entityManager.persistAndFlush(transaction);

        // When
        Optional<Transaction> result = transactionRepository.findByReferenceNumber("REF-1234-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getReferenceNumber()).isEqualTo("REF-1234-1");
    }

    @Test
    void findByUserId_ShouldReturnUserTransactions() {
        // Given
        Transaction transaction1 = createTestTransaction();
        transaction1.setUserId(10L);
        transaction1.setReferenceNumber("REF-1234-2");
        Transaction transaction2 = createTestTransaction();
        transaction2.setTransactionId("TXN-87654321");
        transaction2.setUserId(10L);
        transaction2.setReferenceNumber("REF-1234-3");
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);

        // When
        Page<Transaction> result = transactionRepository.findByUserId(10L, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.getUserId().equals(10L));
    }

    @Test
    void findByFromAccountId_ShouldReturnFromAccountTransactions() {
        // Given
        Transaction transaction = createTestTransaction();
        transaction.setFromAccountId(1L);
        transaction.setReferenceNumber("REF-1234-4");
        entityManager.persistAndFlush(transaction);

        // When
        Page<Transaction> result = transactionRepository.findByFromAccountId(1L, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFromAccountId()).isEqualTo(1L);
    }

    @Test
    void findByToAccountId_ShouldReturnToAccountTransactions() {
        // Given
        Transaction transaction = createTestTransaction();
        transaction.setToAccountId(2L);
        transaction.setReferenceNumber("REF-1234-5");
        entityManager.persistAndFlush(transaction);

        // When
        Page<Transaction> result = transactionRepository.findByToAccountId(2L, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getToAccountId()).isEqualTo(2L);
    }

    @Test
    void findByFromAccountIdOrToAccountId_ShouldReturnAccountTransactions() {
        // Given
        Transaction transaction1 = createTestTransaction();
        transaction1.setFromAccountId(1L);
        transaction1.setToAccountId(2L);
        transaction1.setReferenceNumber("REF-1234-6");
        Transaction transaction2 = createTestTransaction();
        transaction2.setTransactionId("TXN-87654321");
        transaction2.setFromAccountId(2L);
        transaction2.setToAccountId(3L);
        transaction2.setReferenceNumber("REF-1234-7");
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);

        // When
        Page<Transaction> result = transactionRepository.findByFromAccountIdOrToAccountId(1L, 1L, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFromAccountId()).isEqualTo(1L);
    }

    @Test
    void findByStatus_ShouldReturnStatusFilteredTransactions() {
        // Given
        Transaction transaction = createTestTransaction();
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setReferenceNumber("REF-1234-8");
        entityManager.persistAndFlush(transaction);

        // When
        Page<Transaction> result = transactionRepository.findByStatus(TransactionStatus.COMPLETED, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void findByTransactionType_ShouldReturnTypeFilteredTransactions() {
        // Given
        Transaction transaction = createTestTransaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setReferenceNumber("REF-1234-9");
        entityManager.persistAndFlush(transaction);

        // When
        Page<Transaction> result = transactionRepository.findByTransactionType(TransactionType.DEPOSIT, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void findByUserIdAndDateRange_ShouldReturnDateFilteredTransactions() {
        // Given
        Transaction transaction = createTestTransaction();
        transaction.setUserId(10L);
        transaction.setFromAccountId(10L); // Set the account to match the user query logic
        transaction.setProcessedAt(LocalDateTime.now()); // Set processedAt instead of createdAt
        transaction.setReferenceNumber("REF-1234-10");
        entityManager.persistAndFlush(transaction);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        Page<Transaction> result = transactionRepository.findByUserIdAndDateRange(10L, startDate, endDate, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void findAllByAccountId_ShouldReturnAllAccountTransactions() {
        // Given
        Transaction transaction1 = createTestTransaction();
        transaction1.setFromAccountId(1L);
        transaction1.setReferenceNumber("REF-1234-11");
        Transaction transaction2 = createTestTransaction();
        transaction2.setTransactionId("TXN-87654321");
        transaction2.setToAccountId(1L);
        transaction2.setReferenceNumber("REF-1234-12");
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);

        // When
        List<Transaction> result = transactionRepository.findAllByAccountId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(t -> t.getFromAccountId().equals(1L));
        assertThat(result).anyMatch(t -> t.getToAccountId().equals(1L));
    }

    @Test
    void countByUserIdAndStatus_ShouldReturnCorrectCount() {
        // Given
        Transaction transaction1 = createTestTransaction();
        transaction1.setUserId(10L);
        transaction1.setStatus(TransactionStatus.COMPLETED);
        transaction1.setReferenceNumber("REF-1234-13");
        Transaction transaction2 = createTestTransaction();
        transaction2.setTransactionId("TXN-87654321");
        transaction2.setUserId(10L);
        transaction2.setStatus(TransactionStatus.COMPLETED);
        transaction2.setReferenceNumber("REF-1234-14");
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);

        // When
        long result = transactionRepository.countByUserIdAndStatus(10L, TransactionStatus.COMPLETED);

        // Then
        assertThat(result).isEqualTo(2);
    }

    @Test
    void getTotalDebitsByAccountId_ShouldReturnCorrectTotal() {
        // Given
        Transaction transaction1 = createTestTransaction();
        transaction1.setFromAccountId(1L);
        transaction1.setStatus(TransactionStatus.COMPLETED);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setReferenceNumber("REF-1234-15");
        Transaction transaction2 = createTestTransaction();
        transaction2.setTransactionId("TXN-87654321");
        transaction2.setFromAccountId(1L);
        transaction2.setStatus(TransactionStatus.COMPLETED);
        transaction2.setAmount(new BigDecimal("200.00"));
        transaction2.setReferenceNumber("REF-1234-16");
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);

        // When
        BigDecimal result = transactionRepository.getTotalDebitsByAccountId(1L, LocalDateTime.now().minusDays(1));

        // Then
        assertThat(result).isEqualTo(new BigDecimal("300.00"));
    }

    @Test
    void getTotalCreditsByAccountId_ShouldReturnCorrectTotal() {
        // Given
        Transaction transaction1 = createTestTransaction();
        transaction1.setToAccountId(1L);
        transaction1.setStatus(TransactionStatus.COMPLETED);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setReferenceNumber("REF-1234-17");
        Transaction transaction2 = createTestTransaction();
        transaction2.setTransactionId("TXN-87654321");
        transaction2.setToAccountId(1L);
        transaction2.setStatus(TransactionStatus.COMPLETED);
        transaction2.setAmount(new BigDecimal("200.00"));
        transaction2.setReferenceNumber("REF-1234-18");
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);

        // When
        BigDecimal result = transactionRepository.getTotalCreditsByAccountId(1L, LocalDateTime.now().minusDays(1));

        // Then
        assertThat(result).isEqualTo(new BigDecimal("300.00"));
    }

    private Transaction createTestTransaction() {
        return Transaction.builder()
                .transactionId("TXN-12345678")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transaction")
                .referenceNumber("REF-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId())
                .userId(10L)
                .feeAmount(new BigDecimal("1.00"))
                .totalAmount(new BigDecimal("101.00"))
                .processedAt(LocalDateTime.now())
                .build();
    }
} 