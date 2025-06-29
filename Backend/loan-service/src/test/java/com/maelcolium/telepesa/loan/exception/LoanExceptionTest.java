package com.maelcolium.telepesa.loan.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for loan exception classes
 */
class LoanExceptionTest {

    @Test
    void loanNotFoundException_WithMessage_ShouldCreateExceptionWithMessage() {
        // Given
        String message = "Loan not found";

        // When
        LoanNotFoundException exception = new LoanNotFoundException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void loanNotFoundException_WithLoanId_ShouldCreateExceptionWithIdMessage() {
        // Given
        Long loanId = 123L;

        // When
        LoanNotFoundException exception = new LoanNotFoundException(loanId);

        // Then
        assertThat(exception.getMessage()).contains("123");
        assertThat(exception.getMessage()).contains("Loan not found with id:");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void loanNotFoundException_WithMessageAndCause_ShouldCreateExceptionWithBoth() {
        // Given
        String message = "Loan not found";
        Throwable cause = new RuntimeException("Database error");

        // When
        LoanNotFoundException exception = new LoanNotFoundException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void loanOperationException_WithMessage_ShouldCreateExceptionWithMessage() {
        // Given
        String message = "Loan operation failed";

        // When
        LoanOperationException exception = new LoanOperationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void loanOperationException_WithMessageAndCause_ShouldCreateExceptionWithBoth() {
        // Given
        String message = "Loan operation failed";
        Throwable cause = new IllegalStateException("Invalid state");

        // When
        LoanOperationException exception = new LoanOperationException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
