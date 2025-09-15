package com.maelcolium.telepesa.transfer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "account-service", fallback = AccountServiceFallback.class, configuration = com.maelcolium.telepesa.transfer.config.FeignConfig.class)
public interface AccountServiceClient {
    
    @GetMapping("/api/v1/accounts/{accountId}")
    AccountResponse getAccount(@PathVariable("accountId") String accountId);
    
    @GetMapping("/api/v1/accounts/by-number/{accountNumber}")
    AccountResponse getAccountByNumber(@PathVariable("accountNumber") String accountNumber);
    
    @PostMapping("/api/v1/accounts/{accountId}/debit")
    TransactionResponse debitAccount(@PathVariable("accountId") String accountId, 
                                   @RequestBody DebitRequest request);
    
    @PostMapping("/api/v1/accounts/{accountId}/credit")
    TransactionResponse creditAccount(@PathVariable("accountId") String accountId, 
                                    @RequestBody CreditRequest request);
    
    // DTOs for Feign communication
    record AccountResponse(
        String id,
        String accountNumber,
        BigDecimal balance,
        String currency,
        String status,
        String userId,
        String accountType,
        String accountName
    ) {
        public String getAccountName() {
            return accountName != null ? accountName : "Account Holder";
        }
        
        public String getId() {
            return id;
        }
    }
    
    record DebitRequest(
        BigDecimal amount,
        String currency,
        String reference,
        String description
    ) {}
    
    record CreditRequest(
        BigDecimal amount,
        String currency,
        String reference,
        String description
    ) {}
    
    record TransactionResponse(
        String transactionId,
        String accountId,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String status,
        String reference
    ) {}
}
