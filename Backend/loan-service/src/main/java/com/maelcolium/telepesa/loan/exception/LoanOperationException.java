package com.maelcolium.telepesa.loan.exception;

/**
 * Exception thrown when a loan operation cannot be performed
 */
public class LoanOperationException extends RuntimeException {
    
    public LoanOperationException(String message) {
        super(message);
    }
    
    public LoanOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
