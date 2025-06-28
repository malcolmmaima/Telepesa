package com.maelcolium.telepesa.transaction.dto;

import com.maelcolium.telepesa.models.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTransactionRequestTest {

    @Test
    void builder_ShouldCreateValidRequest() {
        // Given
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(1L)
                .fromAccountId(100L)
                .toAccountId(200L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .description("Test transfer")
                .build();

        // Then
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getFromAccountId()).isEqualTo(100L);
        assertThat(request.getToAccountId()).isEqualTo(200L);
        assertThat(request.getAmount()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(request.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(request.getDescription()).isEqualTo("Test transfer");
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest();

        // When
        request.setUserId(2L);
        request.setFromAccountId(300L);
        request.setToAccountId(400L);
        request.setAmount(BigDecimal.valueOf(1000.00));
        request.setTransactionType(TransactionType.DEPOSIT);
        request.setDescription("Test deposit");

        // Then
        assertThat(request.getUserId()).isEqualTo(2L);
        assertThat(request.getFromAccountId()).isEqualTo(300L);
        assertThat(request.getToAccountId()).isEqualTo(400L);
        assertThat(request.getAmount()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(request.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(request.getDescription()).isEqualTo("Test deposit");
    }

    @Test
    void equals_ShouldReturnTrueForSameValues() {
        // Given
        CreateTransactionRequest request1 = CreateTransactionRequest.builder()
                .userId(1L)
                .fromAccountId(100L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .description("Test")
                .build();

        CreateTransactionRequest request2 = CreateTransactionRequest.builder()
                .userId(1L)
                .fromAccountId(100L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .description("Test")
                .build();

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        CreateTransactionRequest request1 = CreateTransactionRequest.builder()
                .userId(1L)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        CreateTransactionRequest request2 = CreateTransactionRequest.builder()
                .userId(2L)
                .amount(BigDecimal.valueOf(600.00))
                .build();

        // Then
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Given
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(1L)
                .fromAccountId(100L)
                .toAccountId(200L)
                .amount(BigDecimal.valueOf(500.00))
                .transactionType(TransactionType.TRANSFER)
                .description("Test transfer")
                .build();

        // When
        String toString = request.toString();

        // Then
        assertThat(toString).contains("userId=1");
        assertThat(toString).contains("fromAccountId=100");
        assertThat(toString).contains("toAccountId=200");
        assertThat(toString).contains("amount=500.0");
        assertThat(toString).contains("transactionType=TRANSFER");
        assertThat(toString).contains("description=Test transfer");
    }

    @Test
    void builderPattern_ShouldAllowPartialBuilding() {
        // Given & When
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(1L)
                .amount(BigDecimal.valueOf(100.00))
                .transactionType(TransactionType.DEPOSIT)
                .build();

        // Then
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(request.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(request.getFromAccountId()).isNull();
        assertThat(request.getToAccountId()).isNull();
        assertThat(request.getDescription()).isNull();
    }
} 