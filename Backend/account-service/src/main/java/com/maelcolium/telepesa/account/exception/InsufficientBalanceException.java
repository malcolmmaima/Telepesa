package com.maelcolium.telepesa.account.exception;

import com.maelcolium.telepesa.exceptions.BusinessException;

import java.math.BigDecimal;

/**
 * Exception thrown when an account has insufficient balance for a transaction.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String accountNumber, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format("Insufficient balance in account %s. Requested: %s, Available: %s", 
                accountNumber, requestedAmount, availableBalance));
    }

    public InsufficientBalanceException(BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format("Insufficient balance. Requested: %s, Available: %s", 
                requestedAmount, availableBalance));
    }
} 