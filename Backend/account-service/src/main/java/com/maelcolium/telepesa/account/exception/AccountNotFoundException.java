package com.maelcolium.telepesa.account.exception;

import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;

/**
 * Exception thrown when an account is not found.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
public class AccountNotFoundException extends ResourceNotFoundException {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(Long accountId) {
        super("Account not found with id: " + accountId);
    }

    public AccountNotFoundException(String accountNumber, boolean isAccountNumber) {
        super("Account not found with account number: " + accountNumber);
    }
} 