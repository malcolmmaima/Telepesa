package com.maelcolium.telepesa.account.service;

import com.maelcolium.telepesa.account.dto.*;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Account operations.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
public interface AccountService {

    /**
     * Create a new account
     */
    AccountDto createAccount(CreateAccountRequest request);

    /**
     * Get account by ID
     */
    AccountDto getAccount(Long accountId);

    /**
     * Get account by account number
     */
    AccountDto getAccountByNumber(String accountNumber);

    /**
     * Get all accounts for a user
     */
    List<AccountDto> getUserAccounts(Long userId);

    /**
     * Get user accounts with pagination
     */
    Page<AccountDto> getUserAccounts(Long userId, Pageable pageable);

    /**
     * Get accounts by status
     */
    Page<AccountDto> getAccountsByStatus(AccountStatus status, Pageable pageable);

    /**
     * Get accounts by type
     */
    Page<AccountDto> getAccountsByType(AccountType accountType, Pageable pageable);

    /**
     * Update account details
     */
    AccountDto updateAccount(Long accountId, UpdateAccountRequest request);

    /**
     * Activate an account
     */
    AccountDto activateAccount(Long accountId);

    /**
     * Freeze an account
     */
    AccountDto freezeAccount(Long accountId);

    /**
     * Unfreeze an account
     */
    AccountDto unfreezeAccount(Long accountId);

    /**
     * Close an account
     */
    AccountDto closeAccount(Long accountId);

    /**
     * Get account balance
     */
    AccountBalanceDto getAccountBalance(String accountNumber);

    /**
     * Credit account
     */
    AccountDto creditAccount(String accountNumber, BigDecimal amount, String description);

    /**
     * Debit account
     */
    AccountDto debitAccount(String accountNumber, BigDecimal amount, String description);

    /**
     * Transfer between accounts
     */
    void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description);

    /**
     * Get user's total balance across all accounts
     */
    BigDecimal getUserTotalBalance(Long userId);

    /**
     * Get active accounts for user
     */
    List<AccountDto> getUserActiveAccounts(Long userId);

    /**
     * Search accounts by name or description
     */
    Page<AccountDto> searchAccounts(String searchTerm, Pageable pageable);

    /**
     * Get accounts with low balance
     */
    List<AccountDto> getAccountsBelowMinimumBalance();

    /**
     * Get dormant accounts
     */
    List<AccountDto> getDormantAccounts(int daysInactive);

    /**
     * Check if user can create account of specific type
     */
    boolean canUserCreateAccount(Long userId, AccountType accountType);

    /**
     * Get account statistics
     */
    AccountStatisticsDto getAccountStatistics();
} 