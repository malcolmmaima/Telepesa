package com.maelcolium.telepesa.loan.exception;

/**
 * Exception thrown when a collateral is not found
 */
public class CollateralNotFoundException extends RuntimeException {
    
    public CollateralNotFoundException(String message) {
        super(message);
    }
    
    public CollateralNotFoundException(Long collateralId) {
        super("Collateral not found with id: " + collateralId);
    }
    
    public CollateralNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 