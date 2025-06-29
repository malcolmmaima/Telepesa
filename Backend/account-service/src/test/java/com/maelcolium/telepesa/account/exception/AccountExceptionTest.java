package com.maelcolium.telepesa.account.exception;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Account exception classes
 */
class AccountExceptionTest {

    @Test
    void accountNotFoundException_WithAccountId_ShouldCreateExceptionWithMessage() {
        // Given
        Long accountId = 123L;

        // When
        AccountNotFoundException exception = new AccountNotFoundException(accountId);

        // Then
        assertThat(exception.getMessage()).contains("123");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void accountOperationException_WithMessage_ShouldCreateExceptionWithMessage() {
        // Given
        String message = "Account operation failed";

        // When
        AccountOperationException exception = new AccountOperationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void insufficientBalanceException_WithAccountAndAmount_ShouldCreateExceptionWithDetails() {
        // Given
        String accountNumber = "ACC123456789";
        BigDecimal requestedAmount = new BigDecimal("1000.00");
        BigDecimal availableBalance = new BigDecimal("500.00");

        // When
        InsufficientBalanceException exception = new InsufficientBalanceException(
                accountNumber, requestedAmount, availableBalance);

        // Then
        assertThat(exception.getMessage()).contains("ACC123456789");
        assertThat(exception.getMessage()).contains("1000.00");
        assertThat(exception.getMessage()).contains("500.00");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
