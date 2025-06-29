package com.maelcolium.telepesa.loan.exception;

/**
 * Exception thrown when a collateral operation fails
 */
public class CollateralOperationException extends RuntimeException {
    
    public CollateralOperationException(String message) {
        super(message);
    }
    
    public CollateralOperationException(String message, Throwable cause) {
        super(message, cause);
    }
} 