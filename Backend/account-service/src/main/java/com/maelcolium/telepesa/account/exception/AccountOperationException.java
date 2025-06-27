package com.maelcolium.telepesa.account.exception;

import com.maelcolium.telepesa.exceptions.BusinessException;

/**
 * Exception thrown when an account operation cannot be performed.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
public class AccountOperationException extends BusinessException {

    public AccountOperationException(String message) {
        super(message);
    }

    public AccountOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AccountOperationException accountFrozen(String accountNumber) {
        return new AccountOperationException("Account " + accountNumber + " is frozen and cannot perform transactions");
    }

    public static AccountOperationException accountInactive(String accountNumber) {
        return new AccountOperationException("Account " + accountNumber + " is not active");
    }

    public static AccountOperationException accountClosed(String accountNumber) {
        return new AccountOperationException("Account " + accountNumber + " is closed");
    }

    public static AccountOperationException invalidOperation(String operation, String reason) {
        return new AccountOperationException("Cannot perform " + operation + ": " + reason);
    }
} 