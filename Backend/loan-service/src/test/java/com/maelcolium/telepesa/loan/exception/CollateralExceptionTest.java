package com.maelcolium.telepesa.loan.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollateralExceptionTest {

    @Test
    void CollateralNotFoundException_WithMessage_ShouldCreateException() {
        // When
        CollateralNotFoundException exception = new CollateralNotFoundException("Collateral not found");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Collateral not found");
    }

    @Test
    void CollateralNotFoundException_WithId_ShouldCreateException() {
        // When
        CollateralNotFoundException exception = new CollateralNotFoundException(999L);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Collateral not found with id: 999");
    }

    @Test
    void CollateralNotFoundException_WithMessageAndCause_ShouldCreateException() {
        // Given
        RuntimeException cause = new RuntimeException("Database error");

        // When
        CollateralNotFoundException exception = new CollateralNotFoundException("Collateral not found", cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Collateral not found");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void CollateralOperationException_WithMessage_ShouldCreateException() {
        // When
        CollateralOperationException exception = new CollateralOperationException("Operation failed");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Operation failed");
    }

    @Test
    void CollateralOperationException_WithMessageAndCause_ShouldCreateException() {
        // Given
        RuntimeException cause = new RuntimeException("Validation error");

        // When
        CollateralOperationException exception = new CollateralOperationException("Operation failed", cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Operation failed");
        assertThat(exception.getCause()).isEqualTo(cause);
    }
} 