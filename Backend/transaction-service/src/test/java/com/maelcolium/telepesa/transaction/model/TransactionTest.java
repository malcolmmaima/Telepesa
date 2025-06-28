package com.maelcolium.telepesa.transaction.model;

import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void builder_ShouldCreateValidTransaction() {
        // Given
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-123")
                .userId(10L)
                .fromAccountId(100L)
                .toAccountId(200L)
                .amount(BigDecimal.valueOf(500.00))
                .feeAmount(BigDecimal.valueOf(5.00))
                .totalAmount(BigDecimal.valueOf(505.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transfer")
                .referenceNumber("REF-123")
                .build();

        // Then
        assertThat(transaction.getTransactionId()).isEqualTo("TXN-123");
        assertThat(transaction.getUserId()).isEqualTo(10L);
        assertThat(transaction.getFromAccountId()).isEqualTo(100L);
        assertThat(transaction.getToAccountId()).isEqualTo(200L);
        assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(transaction.getFeeAmount()).isEqualTo(BigDecimal.valueOf(5.00));
        assertThat(transaction.getTotalAmount()).isEqualTo(BigDecimal.valueOf(505.00));
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getDescription()).isEqualTo("Test transfer");
        assertThat(transaction.getReferenceNumber()).isEqualTo("REF-123");
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        Transaction transaction = new Transaction();
        LocalDateTime now = LocalDateTime.now();

        // When
        transaction.setId(1L);
        transaction.setTransactionId("TXN-456");
        transaction.setUserId(20L);
        transaction.setFromAccountId(300L);
        transaction.setToAccountId(400L);
        transaction.setAmount(BigDecimal.valueOf(1000.00));
        transaction.setFeeAmount(BigDecimal.valueOf(10.00));
        transaction.setTotalAmount(BigDecimal.valueOf(1010.00));
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription("Test deposit");
        transaction.setReferenceNumber("REF-456");
        transaction.setCreatedAt(now);
        transaction.setProcessedAt(now.plusMinutes(1));
        transaction.setUpdatedAt(now.plusMinutes(2));
        transaction.setVersion(1L);

        // Then
        assertThat(transaction.getId()).isEqualTo(1L);
        assertThat(transaction.getTransactionId()).isEqualTo("TXN-456");
        assertThat(transaction.getUserId()).isEqualTo(20L);
        assertThat(transaction.getFromAccountId()).isEqualTo(300L);
        assertThat(transaction.getToAccountId()).isEqualTo(400L);
        assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(transaction.getFeeAmount()).isEqualTo(BigDecimal.valueOf(10.00));
        assertThat(transaction.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1010.00));
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(transaction.getDescription()).isEqualTo("Test deposit");
        assertThat(transaction.getReferenceNumber()).isEqualTo("REF-456");
        assertThat(transaction.getCreatedAt()).isEqualTo(now);
        assertThat(transaction.getProcessedAt()).isEqualTo(now.plusMinutes(1));
        assertThat(transaction.getUpdatedAt()).isEqualTo(now.plusMinutes(2));
        assertThat(transaction.getVersion()).isEqualTo(1L);
    }

    @Test
    void equals_ShouldReturnTrueForSameValues() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN-123")
                .userId(10L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN-123")
                .userId(10L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .build();

        // Then
        assertThat(transaction1).isEqualTo(transaction2);
        assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN-123")
                .amount(BigDecimal.valueOf(500.00))
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN-456")
                .amount(BigDecimal.valueOf(600.00))
                .build();

        // Then
        assertThat(transaction1).isNotEqualTo(transaction2);
    }

    @Test
    void toString_ShouldContainKeyFields() {
        // Given
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-123")
                .userId(10L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transfer")
                .build();

        // When
        String toString = transaction.toString();

        // Then
        assertThat(toString).contains("transactionId=TXN-123");
        assertThat(toString).contains("userId=10");
        assertThat(toString).contains("amount=500.0");
        assertThat(toString).contains("transactionType=TRANSFER");
        assertThat(toString).contains("status=PENDING");
    }

    @Test
    void builderPattern_ShouldAllowPartialBuilding() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-123")
                .userId(10L)
                .amount(BigDecimal.valueOf(100.00))
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .build();

        // Then
        assertThat(transaction.getTransactionId()).isEqualTo("TXN-123");
        assertThat(transaction.getUserId()).isEqualTo(10L);
        assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getId()).isNull();
        assertThat(transaction.getFromAccountId()).isNull();
        assertThat(transaction.getToAccountId()).isNull();
        assertThat(transaction.getDescription()).isNull();
    }

    @Test
    void equalsAndHashCode_ShouldHandleNullValues() {
        // Given
        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        // Then
        assertThat(transaction1).isEqualTo(transaction2);
        assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
    }

    @Test
    void equals_ShouldHandleNullComparison() {
        // Given
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-123")
                .build();

        // Then
        assertThat(transaction).isNotEqualTo(null);
        assertThat(transaction).isEqualTo(transaction);
    }

    @Test
    void transactionStatusEnum_ShouldHaveAllValues() {
        // Given & When & Then
        assertThat(TransactionStatus.PENDING).isNotNull();
        assertThat(TransactionStatus.PROCESSING).isNotNull();
        assertThat(TransactionStatus.COMPLETED).isNotNull();
        assertThat(TransactionStatus.FAILED).isNotNull();
        assertThat(TransactionStatus.CANCELLED).isNotNull();
        assertThat(TransactionStatus.REVERSED).isNotNull();
    }

    @Test
    void transactionTypeEnum_ShouldHaveAllValues() {
        // Given & When & Then
        assertThat(TransactionType.DEPOSIT).isNotNull();
        assertThat(TransactionType.WITHDRAWAL).isNotNull();
        assertThat(TransactionType.TRANSFER).isNotNull();
        assertThat(TransactionType.PAYMENT).isNotNull();
        assertThat(TransactionType.LOAN_DISBURSEMENT).isNotNull();
        assertThat(TransactionType.LOAN_REPAYMENT).isNotNull();
        assertThat(TransactionType.FEE).isNotNull();
        assertThat(TransactionType.REFUND).isNotNull();
        assertThat(TransactionType.REVERSAL).isNotNull();
    }

    @Test
    void builderToString_ShouldWork() {
        // Given & When
        Transaction.TransactionBuilder builder = Transaction.builder()
                .transactionId("TXN-123")
                .userId(10L);

        String builderString = builder.toString();

        // Then
        assertThat(builderString).contains("TransactionBuilder");
    }

    @Test
    void canEqual_ShouldReturnCorrectValue() {
        // Given
        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();
        Object other = new Object();

        // Then
        assertThat(transaction1.canEqual(transaction2)).isTrue();
        assertThat(transaction1.canEqual(other)).isFalse();
    }
} 