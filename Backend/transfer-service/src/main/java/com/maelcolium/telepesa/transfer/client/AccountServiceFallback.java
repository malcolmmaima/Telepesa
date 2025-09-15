package com.maelcolium.telepesa.transfer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class AccountServiceFallback implements AccountServiceClient {
    
    @Override
    public AccountResponse getAccount(String accountId) {
        log.error("Account service is unavailable. Returning fallback for account: {}", accountId);
        return new AccountResponse(
            accountId, 
            "UNAVAILABLE", 
            BigDecimal.ZERO, 
            "KES", 
            "UNAVAILABLE", 
            "", 
            "UNKNOWN",
            "Account Holder"
        );
    }
    
    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.error("Account service is unavailable. Returning fallback for account number: {}", accountNumber);
        return new AccountResponse(
            "fallback-id", 
            accountNumber, 
            BigDecimal.ZERO, 
            "KES", 
            "UNAVAILABLE", 
            "", 
            "UNKNOWN",
            "Account Holder"
        );
    }
    
    @Override
    public TransactionResponse debitAccount(String accountId, DebitRequest request) {
        log.error("Account service is unavailable. Cannot debit account: {}", accountId);
        return new TransactionResponse(
            "",
            accountId,
            request.amount(),
            BigDecimal.ZERO,
            "FAILED",
            request.reference()
        );
    }
    
    @Override
    public TransactionResponse creditAccount(String accountId, CreditRequest request) {
        log.error("Account service is unavailable. Cannot credit account: {}", accountId);
        return new TransactionResponse(
            "",
            accountId,
            request.amount(),
            BigDecimal.ZERO,
            "FAILED",
            request.reference()
        );
    }
}
