package com.maelcolium.telepesa.account.service;

import com.maelcolium.telepesa.account.dto.*;
import com.maelcolium.telepesa.account.exception.AccountNotFoundException;
import com.maelcolium.telepesa.account.exception.AccountOperationException;
import com.maelcolium.telepesa.account.exception.InsufficientBalanceException;
import com.maelcolium.telepesa.account.model.Account;
import com.maelcolium.telepesa.account.repository.AccountRepository;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AccountService
 * Tests complete account workflows with database interactions
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private CreateAccountRequest baseCreateRequest;
    private UpdateAccountRequest baseUpdateRequest;

    @BeforeEach
    void setUp() {
        // Clean up database
        accountRepository.deleteAll();

        // Setup base requests - using higher initial deposit to meet minimum balance requirements
        baseCreateRequest = CreateAccountRequest.builder()
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("10000.00"))
                .build();

        baseUpdateRequest = UpdateAccountRequest.builder()
                .accountName("Updated Test Account")
                .description("Updated description")
                .dailyLimit(new BigDecimal("10000.00"))
                .build();
    }

    @Test
    void createAccount_ShouldPersistInDatabase() {
        // When
        AccountDto result = accountService.createAccount(baseCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isNotNull();
        
        // Verify database persistence
        List<Account> allAccounts = accountRepository.findAll();
        assertThat(allAccounts).hasSize(1);
        
        Account savedAccount = allAccounts.get(0);
        assertThat(savedAccount.getBalance()).isEqualTo(new BigDecimal("10000.00"));
        assertThat(savedAccount.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(savedAccount.getStatus()).isEqualTo(AccountStatus.PENDING); // Accounts start as PENDING
    }

    @Test
    void createMultipleAccountsForUser_ShouldAllBePersisted() {
        // Given
        CreateAccountRequest savingsRequest = createAccountRequest(AccountType.SAVINGS, new BigDecimal("10000.00"));
        CreateAccountRequest checkingRequest = createAccountRequest(AccountType.CHECKING, new BigDecimal("10000.00"));
        CreateAccountRequest businessRequest = createAccountRequest(AccountType.BUSINESS, new BigDecimal("15000.00"));

        // When
        AccountDto savings = accountService.createAccount(savingsRequest);
        AccountDto checking = accountService.createAccount(checkingRequest);
        AccountDto business = accountService.createAccount(businessRequest);

        // Then
        List<Account> userAccounts = accountRepository.findByUserId(100L);
        assertThat(userAccounts).hasSize(3);
        
        BigDecimal totalBalance = userAccounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalBalance).isEqualTo(new BigDecimal("35000.00"));
    }

    @Test
    void getUserAccounts_ShouldReturnUserAccountsOnly() {
        // Given - Create accounts for different users
        createAccountForUser(100L, AccountType.SAVINGS, new BigDecimal("10000.00"));
        createAccountForUser(100L, AccountType.CHECKING, new BigDecimal("12000.00"));
        createAccountForUser(200L, AccountType.SAVINGS, new BigDecimal("15000.00"));

        // When
        List<AccountDto> user100Accounts = accountService.getUserAccounts(100L);
        List<AccountDto> user200Accounts = accountService.getUserAccounts(200L);

        // Then
        assertThat(user100Accounts).hasSize(2);
        assertThat(user200Accounts).hasSize(1);
        
        BigDecimal user100Total = user100Accounts.stream()
                .map(AccountDto::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(user100Total).isEqualTo(new BigDecimal("22000.00"));
    }

    @Test
    void getUserAccountsWithPagination_ShouldReturnPagedResults() {
        // Given - Create 3 accounts for user (within reasonable limits)
        createAccountForUser(100L, AccountType.SAVINGS, new BigDecimal("10000.00"));
        createAccountForUser(100L, AccountType.CHECKING, new BigDecimal("10000.00"));
        createAccountForUser(100L, AccountType.BUSINESS, new BigDecimal("15000.00"));

        // When
        Page<AccountDto> firstPage = accountService.getUserAccounts(100L, PageRequest.of(0, 2));
        Page<AccountDto> secondPage = accountService.getUserAccounts(100L, PageRequest.of(1, 2));

        // Then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void creditAndDebitOperations_ShouldUpdateBalanceCorrectly() {
        // Given
        AccountDto account = accountService.createAccount(baseCreateRequest);
        // Activate account first
        accountService.activateAccount(account.getId());
        String accountNumber = account.getAccountNumber();

        // When - Perform credit and debit operations
        AccountDto creditedAccount = accountService.creditAccount(accountNumber, new BigDecimal("1000.00"), "Test credit");
        AccountDto debitedAccount = accountService.debitAccount(accountNumber, new BigDecimal("500.00"), "Test debit");

        // Then
        assertThat(creditedAccount.getBalance()).isEqualTo(new BigDecimal("11000.00")); // 10000 + 1000
        assertThat(debitedAccount.getBalance()).isEqualTo(new BigDecimal("10500.00")); // 11000 - 500
        
        // Verify in database
        Account dbAccount = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        assertThat(dbAccount.getBalance()).isEqualTo(new BigDecimal("10500.00"));
    }

    @Test
    void transferFunds_ShouldUpdateBothAccountBalances() {
        // Given
        AccountDto fromAccount = accountService.createAccount(baseCreateRequest);
        AccountDto toAccount = accountService.createAccount(createAccountRequest(AccountType.CHECKING, new BigDecimal("12000.00")));
        
        // Activate both accounts
        accountService.activateAccount(fromAccount.getId());
        accountService.activateAccount(toAccount.getId());

        // When
        accountService.transferFunds(
                fromAccount.getAccountNumber(), 
                toAccount.getAccountNumber(), 
                new BigDecimal("1500.00"), 
                "Test transfer"
        );

        // Then
        Account fromAccountDb = accountRepository.findByAccountNumber(fromAccount.getAccountNumber()).orElseThrow();
        Account toAccountDb = accountRepository.findByAccountNumber(toAccount.getAccountNumber()).orElseThrow();
        
        assertThat(fromAccountDb.getBalance()).isEqualTo(new BigDecimal("8500.00")); // 10000 - 1500
        assertThat(toAccountDb.getBalance()).isEqualTo(new BigDecimal("13500.00")); // 12000 + 1500
    }

    @Test
    void transferFunds_WithInsufficientBalance_ShouldThrowException() {
        // Given
        AccountDto fromAccount = accountService.createAccount(createAccountRequest(AccountType.SAVINGS, new BigDecimal("10000.00")));
        AccountDto toAccount = accountService.createAccount(createAccountRequest(AccountType.CHECKING, new BigDecimal("12000.00")));
        
        // Activate both accounts
        accountService.activateAccount(fromAccount.getId());
        accountService.activateAccount(toAccount.getId());

        // When & Then - Try to transfer more than available
        assertThatThrownBy(() -> accountService.transferFunds(
                fromAccount.getAccountNumber(), 
                toAccount.getAccountNumber(), 
                new BigDecimal("15000.00"), // More than available balance
                "Test transfer"
        )).isInstanceOf(InsufficientBalanceException.class);
        
        // Verify balances unchanged
        Account fromAccountDb = accountRepository.findByAccountNumber(fromAccount.getAccountNumber()).orElseThrow();
        Account toAccountDb = accountRepository.findByAccountNumber(toAccount.getAccountNumber()).orElseThrow();
        
        assertThat(fromAccountDb.getBalance()).isEqualTo(new BigDecimal("10000.00"));
        assertThat(toAccountDb.getBalance()).isEqualTo(new BigDecimal("12000.00"));
    }

    @Test
    void accountStatusOperations_ShouldPersistStatusChanges() {
        // Given
        AccountDto account = accountService.createAccount(baseCreateRequest);
        
        // When - Freeze and unfreeze account
        AccountDto frozenAccount = accountService.freezeAccount(account.getId());
        AccountDto unfrozenAccount = accountService.unfreezeAccount(account.getId());

        // Then
        Account dbAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(dbAccount.getIsFrozen()).isFalse(); // Final state should be unfrozen
        
        // Verify the operations were logged
        assertThat(frozenAccount.getIsFrozen()).isTrue();
        assertThat(unfrozenAccount.getIsFrozen()).isFalse();
    }

    @Test
    void getAccountsByStatus_ShouldReturnFilteredResults() {
        // Given
        AccountDto account1 = accountService.createAccount(baseCreateRequest);
        AccountDto account2 = accountService.createAccount(createAccountRequest(AccountType.CHECKING, new BigDecimal("10000.00")));
        
        // Activate one account
        accountService.activateAccount(account1.getId());

        // When
        Page<AccountDto> activeAccounts = accountService.getAccountsByStatus(AccountStatus.ACTIVE, PageRequest.of(0, 10));
        Page<AccountDto> pendingAccounts = accountService.getAccountsByStatus(AccountStatus.PENDING, PageRequest.of(0, 10));

        // Then
        assertThat(activeAccounts.getContent()).hasSize(1);
        assertThat(pendingAccounts.getContent()).hasSize(1);
    }

    @Test
    void getAccountsByType_ShouldReturnFilteredResults() {
        // Given
        accountService.createAccount(createAccountRequest(AccountType.SAVINGS, new BigDecimal("10000.00")));
        accountService.createAccount(createAccountRequest(AccountType.SAVINGS, new BigDecimal("12000.00")));
        accountService.createAccount(createAccountRequest(AccountType.CHECKING, new BigDecimal("15000.00")));

        // When
        Page<AccountDto> savingsAccounts = accountService.getAccountsByType(AccountType.SAVINGS, PageRequest.of(0, 10));
        Page<AccountDto> checkingAccounts = accountService.getAccountsByType(AccountType.CHECKING, PageRequest.of(0, 10));

        // Then
        assertThat(savingsAccounts.getContent()).hasSize(2);
        assertThat(checkingAccounts.getContent()).hasSize(1);
        
        BigDecimal savingsTotal = savingsAccounts.getContent().stream()
                .map(AccountDto::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(savingsTotal).isEqualTo(new BigDecimal("22000.00"));
    }

    @Test
    void getUserTotalBalance_ShouldCalculateCorrectTotal() {
        // Given
        createAccountForUser(100L, AccountType.SAVINGS, new BigDecimal("12000.00"));
        createAccountForUser(100L, AccountType.CHECKING, new BigDecimal("15000.00"));
        createAccountForUser(100L, AccountType.BUSINESS, new BigDecimal("20000.00"));
        createAccountForUser(200L, AccountType.SAVINGS, new BigDecimal("18000.00")); // Different user

        // When
        BigDecimal user100Total = accountService.getUserTotalBalance(100L);
        BigDecimal user200Total = accountService.getUserTotalBalance(200L);

        // Then
        assertThat(user100Total).isEqualTo(new BigDecimal("47000.00"));
        assertThat(user200Total).isEqualTo(new BigDecimal("18000.00"));
    }

    @Test
    void canUserCreateAccount_ShouldRespectAccountLimits() {
        // Given - Create one account
        createAccountForUser(100L, AccountType.SAVINGS, new BigDecimal("10000.00"));

        // When
        boolean canCreateMore = accountService.canUserCreateAccount(100L, AccountType.CHECKING);

        // Then
        assertThat(canCreateMore).isTrue();
    }

    @Test
    void updateAccount_ShouldPersistChanges() {
        // Given
        AccountDto account = accountService.createAccount(baseCreateRequest);

        // When
        AccountDto updatedAccount = accountService.updateAccount(account.getId(), baseUpdateRequest);

        // Then
        assertThat(updatedAccount.getAccountName()).isEqualTo("Updated Test Account");
        assertThat(updatedAccount.getDescription()).isEqualTo("Updated description");
        
        // Verify in database
        Account dbAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(dbAccount.getAccountName()).isEqualTo("Updated Test Account");
        assertThat(dbAccount.getDescription()).isEqualTo("Updated description");
    }

    // Helper methods
    private CreateAccountRequest createAccountRequest(AccountType type, BigDecimal initialDeposit) {
        return CreateAccountRequest.builder()
                .userId(100L)
                .accountType(type)
                .initialDeposit(initialDeposit)
                .build();
    }

    private void createAccountForUser(Long userId, AccountType type, BigDecimal initialDeposit) {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(userId)
                .accountType(type)
                .initialDeposit(initialDeposit)
                .build();
        accountService.createAccount(request);
    }
}
