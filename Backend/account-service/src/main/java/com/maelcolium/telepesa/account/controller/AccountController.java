package com.maelcolium.telepesa.account.controller;

import com.maelcolium.telepesa.account.dto.*;
import com.maelcolium.telepesa.account.exception.AccountOperationException;
import com.maelcolium.telepesa.account.service.AccountService;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Account operations.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Validated
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create a new account", description = "Create a new bank account for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Account limit reached or business rule violation")
    })
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Creating account for user: {}", request.getUserId());
        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @Operation(summary = "Get account by ID", description = "Retrieve account details by account ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId) {
        
        log.debug("Getting account: {}", accountId);
        AccountDto account = accountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Get account by account number", description = "Retrieve account details by account number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByNumber(
        @Parameter(description = "Account number", example = "SAV20240612345678")
        @PathVariable String accountNumber) {
        
        log.debug("Getting account by number: {}", accountNumber);
        AccountDto account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Get user accounts", description = "Retrieve all accounts for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AccountDto>> getUserAccounts(
        @Parameter(description = "User ID", example = "1")
        @PathVariable Long userId,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "Sort direction", example = "desc")
        @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("Getting accounts for user: {}", userId);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AccountDto> accounts = accountService.getUserAccounts(userId, pageable);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get user active accounts", description = "Retrieve only active accounts for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active accounts retrieved successfully")
    })
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<AccountDto>> getUserActiveAccounts(
        @Parameter(description = "User ID", example = "1")
        @PathVariable Long userId) {
        
        log.debug("Getting active accounts for user: {}", userId);
        List<AccountDto> accounts = accountService.getUserActiveAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Update account", description = "Update account details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountDto> updateAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId,
        @Valid @RequestBody UpdateAccountRequest request) {
        
        log.info("Updating account: {}", accountId);
        AccountDto account = accountService.updateAccount(accountId, request);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Activate account", description = "Activate a pending account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account activated successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Account already active")
    })
    @PostMapping("/{accountId}/activate")
    public ResponseEntity<AccountDto> activateAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId) {
        
        log.info("Activating account: {}", accountId);
        AccountDto account = accountService.activateAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Freeze account", description = "Freeze an active account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account frozen successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Account already frozen")
    })
    @PostMapping("/{accountId}/freeze")
    public ResponseEntity<AccountDto> freezeAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId) {
        
        log.info("Freezing account: {}", accountId);
        AccountDto account = accountService.freezeAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Unfreeze account", description = "Unfreeze a frozen account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account unfrozen successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Account not frozen")
    })
    @PostMapping("/{accountId}/unfreeze")
    public ResponseEntity<AccountDto> unfreezeAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId) {
        
        log.info("Unfreezing account: {}", accountId);
        AccountDto account = accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Close account", description = "Close an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account closed successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Account has positive balance or already closed")
    })
    @PostMapping("/{accountId}/close")
    public ResponseEntity<AccountDto> closeAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId) {
        
        log.info("Closing account: {}", accountId);
        AccountDto account = accountService.closeAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Get account balance", description = "Get account balance information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceDto> getAccountBalance(
        @Parameter(description = "Account number", example = "SAV20240612345678")
        @PathVariable String accountNumber) {
        
        log.debug("Getting balance for account: {}", accountNumber);
        AccountBalanceDto balance = accountService.getAccountBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Get user total balance", description = "Get total balance across all user accounts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total balance calculated successfully")
    })
    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<BigDecimal> getUserTotalBalance(
        @Parameter(description = "User ID", example = "1")
        @PathVariable Long userId) {
        
        log.debug("Getting total balance for user: {}", userId);
        BigDecimal totalBalance = accountService.getUserTotalBalance(userId);
        return ResponseEntity.ok(totalBalance);
    }

    @Operation(summary = "Get accounts by status", description = "Retrieve accounts by status with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AccountDto>> getAccountsByStatus(
        @Parameter(description = "Account status", example = "ACTIVE")
        @PathVariable AccountStatus status,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting accounts with status: {}", status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AccountDto> accounts = accountService.getAccountsByStatus(status, pageable);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get accounts by type", description = "Retrieve accounts by type with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    @GetMapping("/type/{accountType}")
    public ResponseEntity<Page<AccountDto>> getAccountsByType(
        @Parameter(description = "Account type", example = "SAVINGS")
        @PathVariable AccountType accountType,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting accounts with type: {}", accountType);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AccountDto> accounts = accountService.getAccountsByType(accountType, pageable);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Search accounts", description = "Search accounts by name or description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<AccountDto>> searchAccounts(
        @Parameter(description = "Search term", example = "savings")
        @RequestParam String q,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Searching accounts with term: {}", q);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AccountDto> accounts = accountService.searchAccounts(q, pageable);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get accounts below minimum balance", description = "Get accounts with balance below minimum requirement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    @GetMapping("/below-minimum-balance")
    public ResponseEntity<List<AccountDto>> getAccountsBelowMinimumBalance() {
        log.debug("Getting accounts below minimum balance");
        List<AccountDto> accounts = accountService.getAccountsBelowMinimumBalance();
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get dormant accounts", description = "Get accounts with no activity for specified days")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dormant accounts retrieved successfully")
    })
    @GetMapping("/dormant")
    public ResponseEntity<List<AccountDto>> getDormantAccounts(
        @Parameter(description = "Days of inactivity", example = "90")
        @RequestParam(defaultValue = "90") int days) {
        
        log.debug("Getting dormant accounts with {} days inactivity", days);
        List<AccountDto> accounts = accountService.getDormantAccounts(days);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get account statistics", description = "Get comprehensive account statistics and analytics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/statistics")
    public ResponseEntity<AccountStatisticsDto> getAccountStatistics() {
        log.debug("Getting account statistics");
        AccountStatisticsDto statistics = accountService.getAccountStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Credit account", description = "Add funds to an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account credited successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Invalid amount or account not active")
    })
    @PostMapping("/{accountId}/credit")
    public ResponseEntity<AccountDto> creditAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId,
        @Valid @RequestBody CreditDebitRequest request) {
        
        log.info("Crediting account: {} with amount: {}", accountId, request.getAmount());
        // First get the account to get the account number
        AccountDto account = accountService.getAccount(accountId);
        AccountDto updatedAccount = accountService.creditAccount(account.getAccountNumber(), request.getAmount(), request.getDescription());
        return ResponseEntity.ok(updatedAccount);
    }

    @Operation(summary = "Debit account", description = "Withdraw funds from an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account debited successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance or account not active")
    })
    @PostMapping("/{accountId}/debit")
    public ResponseEntity<AccountDto> debitAccount(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId,
        @Valid @RequestBody CreditDebitRequest request) {
        
        log.info("Debiting account: {} with amount: {}", accountId, request.getAmount());
        // First get the account to get the account number
        AccountDto account = accountService.getAccount(accountId);
        AccountDto updatedAccount = accountService.debitAccount(account.getAccountNumber(), request.getAmount(), request.getDescription());
        return ResponseEntity.ok(updatedAccount);
    }

    @Operation(summary = "Transfer between accounts", description = "Transfer funds from one account to another")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance, invalid amount, or same account")
    })
    @PostMapping("/{fromAccountId}/transfer")
    public ResponseEntity<AccountDto> transferBetweenAccounts(
        @Parameter(description = "Source account ID", example = "1")
        @PathVariable Long fromAccountId,
        @Valid @RequestBody TransferRequest request) {
        
        log.info("Transferring from account: {} to account: {} amount: {}", 
                fromAccountId, request.getToAccountId(), request.getAmount());
        
        // Check if transferring to same account
        if (fromAccountId.equals(request.getToAccountId())) {
            throw new AccountOperationException("Cannot transfer to the same account");
        }
        
        // Get both accounts to get their account numbers
        AccountDto fromAccount = accountService.getAccount(fromAccountId);
        AccountDto toAccount = accountService.getAccount(request.getToAccountId());
        
        accountService.transferFunds(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), 
                                   request.getAmount(), request.getDescription());
        
        // Return the updated source account
        return ResponseEntity.ok(accountService.getAccount(fromAccountId));
    }

    @Operation(summary = "Get account balance by ID", description = "Get account balance information by account ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceDto> getAccountBalanceById(
        @Parameter(description = "Account ID", example = "1")
        @PathVariable Long accountId) {
        
        log.debug("Getting balance for account ID: {}", accountId);
        // First get the account to get the account number
        AccountDto account = accountService.getAccount(accountId);
        AccountBalanceDto balance = accountService.getAccountBalance(account.getAccountNumber());
        return ResponseEntity.ok(balance);
    }
} 