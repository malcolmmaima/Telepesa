package com.telepesa.account.controller;

import com.telepesa.account.dto.AccountDto;
import com.telepesa.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;


    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getAccountsByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        log.info("Fetching accounts for user ID: {} with pagination - page: {}, size: {}", userId, page, size);
        
        List<AccountDto> allAccounts = accountService.getAccountsByUserId(userId);
        
        // Simple pagination logic
        int start = page * size;
        int end = Math.min(start + size, allAccounts.size());
        
        List<AccountDto> pagedAccounts = start < allAccounts.size() 
            ? allAccounts.subList(start, end) 
            : List.of();
        
        // Create paginated response
        Map<String, Object> response = Map.of(
            "content", pagedAccounts,
            "totalElements", allAccounts.size(),
            "totalPages", (int) Math.ceil((double) allAccounts.size() / size),
            "currentPage", page,
            "pageSize", size,
            "hasNext", end < allAccounts.size(),
            "hasPrevious", page > 0
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String accountType = request.get("accountType").toString();
        
        log.info("Creating new account with request: {}", request);
        
        AccountDto account = accountService.createAccount(userId, accountType);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<Map<String, Object>> getUserTotalBalance(@PathVariable("userId") Long userId) {
        log.info("Getting total balance for user ID: {}", userId);
        
        List<AccountDto> userAccounts = accountService.getAccountsByUserId(userId);
        BigDecimal totalBalance = userAccounts.stream()
                .map(AccountDto::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAvailableBalance = userAccounts.stream()
                .map(AccountDto::getAvailableBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ResponseEntity.ok(Map.of(
                "totalBalance", totalBalance,
                "totalAvailableBalance", totalAvailableBalance,
                "currencyCode", "KES",
                "accountCount", userAccounts.size()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "account-service-minimal",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
