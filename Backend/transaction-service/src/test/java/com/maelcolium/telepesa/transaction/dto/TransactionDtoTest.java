package com.maelcolium.telepesa.transaction.dto;

import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionDtoTest {

    @Test
    void builder_ShouldCreateValidDto() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TransactionDto dto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-123")
                .userId(10L)
                .fromAccountId(100L)
                .toAccountId(200L)
                .amount(BigDecimal.valueOf(500.00))
                .feeAmount(BigDecimal.valueOf(5.00))
                .totalAmount(BigDecimal.valueOf(505.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description("Test transfer")
                .referenceNumber("REF-123")
                .createdAt(now)
                .processedAt(now.plusMinutes(1))
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTransactionId()).isEqualTo("TXN-123");
        assertThat(dto.getUserId()).isEqualTo(10L);
        assertThat(dto.getFromAccountId()).isEqualTo(100L);
        assertThat(dto.getToAccountId()).isEqualTo(200L);
        assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(dto.getFeeAmount()).isEqualTo(BigDecimal.valueOf(5.00));
        assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(505.00));
        assertThat(dto.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(dto.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(dto.getDescription()).isEqualTo("Test transfer");
        assertThat(dto.getReferenceNumber()).isEqualTo("REF-123");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getProcessedAt()).isEqualTo(now.plusMinutes(1));
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        TransactionDto dto = new TransactionDto();
        LocalDateTime now = LocalDateTime.now();

        // When
        dto.setId(2L);
        dto.setTransactionId("TXN-456");
        dto.setUserId(20L);
        dto.setFromAccountId(300L);
        dto.setToAccountId(400L);
        dto.setAmount(BigDecimal.valueOf(1000.00));
        dto.setFeeAmount(BigDecimal.valueOf(10.00));
        dto.setTotalAmount(BigDecimal.valueOf(1010.00));
        dto.setTransactionType(TransactionType.DEPOSIT);
        dto.setStatus(TransactionStatus.PENDING);
        dto.setDescription("Test deposit");
        dto.setReferenceNumber("REF-456");
        dto.setCreatedAt(now);
        dto.setProcessedAt(now.plusMinutes(2));

        // Then
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getTransactionId()).isEqualTo("TXN-456");
        assertThat(dto.getUserId()).isEqualTo(20L);
        assertThat(dto.getFromAccountId()).isEqualTo(300L);
        assertThat(dto.getToAccountId()).isEqualTo(400L);
        assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(dto.getFeeAmount()).isEqualTo(BigDecimal.valueOf(10.00));
        assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1010.00));
        assertThat(dto.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(dto.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(dto.getDescription()).isEqualTo("Test deposit");
        assertThat(dto.getReferenceNumber()).isEqualTo("REF-456");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getProcessedAt()).isEqualTo(now.plusMinutes(2));
    }

    @Test
    void equals_ShouldReturnTrueForSameValues() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TransactionDto dto1 = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-123")
                .userId(10L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .createdAt(now)
                .build();

        TransactionDto dto2 = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-123")
                .userId(10L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .createdAt(now)
                .build();

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        TransactionDto dto1 = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-123")
                .amount(BigDecimal.valueOf(500.00))
                .build();

        TransactionDto dto2 = TransactionDto.builder()
                .id(2L)
                .transactionId("TXN-456")
                .amount(BigDecimal.valueOf(600.00))
                .build();

        // Then
        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TransactionDto dto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-123")
                .userId(10L)
                .fromAccountId(100L)
                .toAccountId(200L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description("Test transfer")
                .createdAt(now)
                .build();

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("transactionId=TXN-123");
        assertThat(toString).contains("userId=10");
        assertThat(toString).contains("fromAccountId=100");
        assertThat(toString).contains("toAccountId=200");
        assertThat(toString).contains("amount=500.0");
        assertThat(toString).contains("transactionType=TRANSFER");
        assertThat(toString).contains("status=COMPLETED");
        assertThat(toString).contains("description=Test transfer");
    }

    @Test
    void builderPattern_ShouldAllowPartialBuilding() {
        // Given & When
        TransactionDto dto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-123")
                .amount(BigDecimal.valueOf(100.00))
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTransactionId()).isEqualTo("TXN-123");
        assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(dto.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(dto.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(dto.getUserId()).isNull();
        assertThat(dto.getFromAccountId()).isNull();
        assertThat(dto.getToAccountId()).isNull();
        assertThat(dto.getDescription()).isNull();
    }

    @Test
    void equalsAndHashCode_ShouldHandleNullValues() {
        // Given
        TransactionDto dto1 = new TransactionDto();
        TransactionDto dto2 = new TransactionDto();

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void equals_ShouldHandleNullComparison() {
        // Given
        TransactionDto dto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-123")
                .build();

        // Then
        assertThat(dto).isNotEqualTo(null);
        assertThat(dto).isEqualTo(dto);
    }
} 