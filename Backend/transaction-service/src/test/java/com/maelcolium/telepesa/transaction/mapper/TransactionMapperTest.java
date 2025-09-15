package com.maelcolium.telepesa.transaction.mapper;

import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.transaction.model.Transaction;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionMapperTest {

    @InjectMocks
    private TransactionMapper transactionMapper;

    private Transaction transaction;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
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
                .processedAt(now)
                .build();
        transaction.setId(1L);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

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
                .processedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void toDto_WithValidTransaction_ShouldReturnCorrectDto() {
        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transaction.getId());
        assertThat(result.getTransactionId()).isEqualTo(transaction.getTransactionId());
        assertThat(result.getFromAccountId()).isEqualTo(transaction.getFromAccountId());
        assertThat(result.getToAccountId()).isEqualTo(transaction.getToAccountId());
        assertThat(result.getAmount()).isEqualTo(transaction.getAmount());
        assertThat(result.getTransactionType()).isEqualTo(transaction.getTransactionType());
        assertThat(result.getStatus()).isEqualTo(transaction.getStatus());
        assertThat(result.getDescription()).isEqualTo(transaction.getDescription());
        assertThat(result.getReferenceNumber()).isEqualTo(transaction.getReferenceNumber());
        assertThat(result.getUserId()).isEqualTo(transaction.getUserId());
        assertThat(result.getFeeAmount()).isEqualTo(transaction.getFeeAmount());
        assertThat(result.getTotalAmount()).isEqualTo(transaction.getTotalAmount());
        assertThat(result.getProcessedAt()).isEqualTo(transaction.getProcessedAt());
        assertThat(result.getCreatedAt()).isEqualTo(transaction.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(transaction.getUpdatedAt());
    }

    @Test
    void toDto_WithNullTransaction_ShouldReturnNull() {
        // When
        TransactionDto result = transactionMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDto_WithNullValues_ShouldHandleNullsCorrectly() {
        // Given
        Transaction transactionWithNulls = Transaction.builder()
                .transactionId("TXN-12345678")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(null)
                .referenceNumber(null)
                .userId(10L)
                .feeAmount(null)
                .totalAmount(null)
                .processedAt(null)
                .build();
        transactionWithNulls.setId(1L);

        // When
        TransactionDto result = transactionMapper.toDto(transactionWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTransactionId()).isEqualTo("TXN-12345678");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getReferenceNumber()).isNull();
        assertThat(result.getFeeAmount()).isNull();
        assertThat(result.getTotalAmount()).isNull();
        assertThat(result.getProcessedAt()).isNull();
    }

    @Test
    void toDto_WithDifferentTransactionTypes_ShouldMapCorrectly() {
        // Given
        transaction.setTransactionType(TransactionType.DEPOSIT);

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void toDto_WithDifferentStatuses_ShouldMapCorrectly() {
        // Given
        transaction.setStatus(TransactionStatus.COMPLETED);

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void toDto_WithZeroAmounts_ShouldMapCorrectly() {
        // Given
        transaction.setAmount(BigDecimal.ZERO);
        transaction.setFeeAmount(BigDecimal.ZERO);
        transaction.setTotalAmount(BigDecimal.ZERO);

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getFeeAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void toDto_WithLargeAmounts_ShouldMapCorrectly() {
        // Given
        transaction.setAmount(new BigDecimal("999999.99"));
        transaction.setFeeAmount(new BigDecimal("9999.99"));
        transaction.setTotalAmount(new BigDecimal("1009999.98"));

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("999999.99"));
        assertThat(result.getFeeAmount()).isEqualTo(new BigDecimal("9999.99"));
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("1009999.98"));
    }

    @Test
    void toDto_WithSpecialCharacters_ShouldMapCorrectly() {
        // Given
        transaction.setDescription("Test transaction with special chars: @#$%^&*()");
        transaction.setReferenceNumber("REF-1234@#$%");

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getDescription()).isEqualTo("Test transaction with special chars: @#$%^&*()");
        assertThat(result.getReferenceNumber()).isEqualTo("REF-1234@#$%");
    }

    @Test
    void toDto_WithUnicodeCharacters_ShouldMapCorrectly() {
        // Given
        transaction.setDescription("Test transaction with unicode: 测试交易 🚀 💰");
        transaction.setReferenceNumber("REF-测试-1234");

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getDescription()).isEqualTo("Test transaction with unicode: 测试交易 🚀 💰");
        assertThat(result.getReferenceNumber()).isEqualTo("REF-测试-1234");
    }

    @Test
    void toDto_WithLongValues_ShouldMapCorrectly() {
        // Given
        transaction.setFromAccountId(Long.MAX_VALUE);
        transaction.setToAccountId(Long.MAX_VALUE - 1);
        transaction.setUserId(Long.MAX_VALUE - 2);

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getFromAccountId()).isEqualTo(Long.MAX_VALUE);
        assertThat(result.getToAccountId()).isEqualTo(Long.MAX_VALUE - 1);
        assertThat(result.getUserId()).isEqualTo(Long.MAX_VALUE - 2);
    }

    @Test
    void toDto_WithNegativeValues_ShouldMapCorrectly() {
        // Given
        transaction.setFromAccountId(-1L);
        transaction.setToAccountId(-2L);
        transaction.setUserId(-10L);
        transaction.setAmount(new BigDecimal("-100.00"));

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getFromAccountId()).isEqualTo(-1L);
        assertThat(result.getToAccountId()).isEqualTo(-2L);
        assertThat(result.getUserId()).isEqualTo(-10L);
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("-100.00"));
    }

    @Test
    void toDto_WithAllTransactionTypes_ShouldMapCorrectly() {
        // Test all transaction types
        TransactionType[] types = TransactionType.values();
        
        for (TransactionType type : types) {
            // Given
            transaction.setTransactionType(type);
            
            // When
            TransactionDto result = transactionMapper.toDto(transaction);
            
            // Then
            assertThat(result.getTransactionType()).isEqualTo(type);
        }
    }

    @Test
    void toDto_WithAllTransactionStatuses_ShouldMapCorrectly() {
        // Test all transaction statuses
        TransactionStatus[] statuses = TransactionStatus.values();
        
        for (TransactionStatus status : statuses) {
            // Given
            transaction.setStatus(status);
            
            // When
            TransactionDto result = transactionMapper.toDto(transaction);
            
            // Then
            assertThat(result.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void toDto_WithDateTimePrecision_ShouldMapCorrectly() {
        // Given
        LocalDateTime preciseTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 123456789);
        transaction.setCreatedAt(preciseTime);
        transaction.setUpdatedAt(preciseTime);
        transaction.setProcessedAt(preciseTime);

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getCreatedAt()).isEqualTo(preciseTime);
        assertThat(result.getUpdatedAt()).isEqualTo(preciseTime);
        assertThat(result.getProcessedAt()).isEqualTo(preciseTime);
    }

    @Test
    void toDto_WithEmptyStrings_ShouldMapCorrectly() {
        // Given
        transaction.setDescription("");
        transaction.setReferenceNumber("");

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getDescription()).isEqualTo("");
        assertThat(result.getReferenceNumber()).isEqualTo("");
    }

    @Test
    void toDto_WithWhitespaceStrings_ShouldMapCorrectly() {
        // Given
        transaction.setDescription("   ");
        transaction.setReferenceNumber("  REF-1234  ");

        // When
        TransactionDto result = transactionMapper.toDto(transaction);

        // Then
        assertThat(result.getDescription()).isEqualTo("   ");
        assertThat(result.getReferenceNumber()).isEqualTo("  REF-1234  ");
    }

    // Tests for toEntity method
    @Test
    void toEntity_WithValidTransactionDto_ShouldReturnCorrectEntity() {
        // When
        Transaction result = transactionMapper.toEntity(transactionDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(transactionDto.getTransactionId());
        assertThat(result.getFromAccountId()).isEqualTo(transactionDto.getFromAccountId());
        assertThat(result.getToAccountId()).isEqualTo(transactionDto.getToAccountId());
        assertThat(result.getAmount()).isEqualTo(transactionDto.getAmount());
        assertThat(result.getTransactionType()).isEqualTo(transactionDto.getTransactionType());
        assertThat(result.getStatus()).isEqualTo(transactionDto.getStatus());
        assertThat(result.getDescription()).isEqualTo(transactionDto.getDescription());
        assertThat(result.getReferenceNumber()).isEqualTo(transactionDto.getReferenceNumber());
        assertThat(result.getUserId()).isEqualTo(transactionDto.getUserId());
        assertThat(result.getFeeAmount()).isEqualTo(transactionDto.getFeeAmount());
        assertThat(result.getTotalAmount()).isEqualTo(transactionDto.getTotalAmount());
        assertThat(result.getProcessedAt()).isEqualTo(transactionDto.getProcessedAt());
    }

    @Test
    void toEntity_WithNullTransactionDto_ShouldReturnNull() {
        // When
        Transaction result = transactionMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithNullValues_ShouldHandleNullsCorrectly() {
        // Given
        TransactionDto dtoWithNulls = TransactionDto.builder()
                .transactionId("TXN-12345678")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(null)
                .referenceNumber(null)
                .userId(10L)
                .feeAmount(null)
                .totalAmount(null)
                .processedAt(null)
                .build();

        // When
        Transaction result = transactionMapper.toEntity(dtoWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("TXN-12345678");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getReferenceNumber()).isNull();
        assertThat(result.getFeeAmount()).isNull();
        assertThat(result.getTotalAmount()).isNull();
        assertThat(result.getProcessedAt()).isNull();
    }

    @Test
    void toEntity_WithAllTransactionTypes_ShouldMapCorrectly() {
        // Test all transaction types
        TransactionType[] types = TransactionType.values();
        
        for (TransactionType type : types) {
            // Given
            TransactionDto updatedDto = TransactionDto.builder()
                    .id(transactionDto.getId())
                    .transactionId(transactionDto.getTransactionId())
                    .fromAccountId(transactionDto.getFromAccountId())
                    .toAccountId(transactionDto.getToAccountId())
                    .amount(transactionDto.getAmount())
                    .transactionType(type)
                    .status(transactionDto.getStatus())
                    .description(transactionDto.getDescription())
                    .referenceNumber(transactionDto.getReferenceNumber())
                    .userId(transactionDto.getUserId())
                    .feeAmount(transactionDto.getFeeAmount())
                    .totalAmount(transactionDto.getTotalAmount())
                    .processedAt(transactionDto.getProcessedAt())
                    .createdAt(transactionDto.getCreatedAt())
                    .updatedAt(transactionDto.getUpdatedAt())
                    .build();
            
            // When
            Transaction result = transactionMapper.toEntity(updatedDto);
            
            // Then
            assertThat(result.getTransactionType()).isEqualTo(type);
        }
    }

    @Test
    void toEntity_WithAllTransactionStatuses_ShouldMapCorrectly() {
        // Test all transaction statuses
        TransactionStatus[] statuses = TransactionStatus.values();
        
        for (TransactionStatus status : statuses) {
            // Given
            TransactionDto updatedDto = TransactionDto.builder()
                    .id(transactionDto.getId())
                    .transactionId(transactionDto.getTransactionId())
                    .fromAccountId(transactionDto.getFromAccountId())
                    .toAccountId(transactionDto.getToAccountId())
                    .amount(transactionDto.getAmount())
                    .transactionType(transactionDto.getTransactionType())
                    .status(status)
                    .description(transactionDto.getDescription())
                    .referenceNumber(transactionDto.getReferenceNumber())
                    .userId(transactionDto.getUserId())
                    .feeAmount(transactionDto.getFeeAmount())
                    .totalAmount(transactionDto.getTotalAmount())
                    .processedAt(transactionDto.getProcessedAt())
                    .createdAt(transactionDto.getCreatedAt())
                    .updatedAt(transactionDto.getUpdatedAt())
                    .build();
            
            // When
            Transaction result = transactionMapper.toEntity(updatedDto);
            
            // Then
            assertThat(result.getStatus()).isEqualTo(status);
        }
    }
} 