package com.maelcolium.telepesa.loan.exception;

/**
 * Exception thrown when a loan is not found
 */
public class LoanNotFoundException extends RuntimeException {
    
    public LoanNotFoundException(String message) {
        super(message);
    }
    
    public LoanNotFoundException(Long loanId) {
        super("Loan not found with id: " + loanId);
    }
    
    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
