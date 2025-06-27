package com.maelcolium.telepesa.account.service.impl;

import com.maelcolium.telepesa.account.dto.*;
import com.maelcolium.telepesa.account.exception.AccountNotFoundException;
import com.maelcolium.telepesa.account.exception.AccountOperationException;
import com.maelcolium.telepesa.account.exception.InsufficientBalanceException;
import com.maelcolium.telepesa.account.mapper.AccountMapper;
import com.maelcolium.telepesa.account.model.Account;
import com.maelcolium.telepesa.account.repository.AccountRepository;
import com.maelcolium.telepesa.account.service.AccountNumberService;
import com.maelcolium.telepesa.account.service.AccountService;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AccountService providing core account management functionality.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Service
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountNumberService accountNumberService;

    // Business rules constants
    private static final int MAX_ACCOUNTS_PER_USER = 10;
    private static final int DORMANT_DAYS_THRESHOLD = 90;
    private static final BigDecimal DEFAULT_MINIMUM_BALANCE = new BigDecimal("1000.00");
    private static final Map<AccountType, BigDecimal> MINIMUM_BALANCES = Map.of(
        AccountType.SAVINGS, new BigDecimal("1000.00"),
        AccountType.CHECKING, new BigDecimal("500.00"),
        AccountType.BUSINESS, new BigDecimal("5000.00"),
        AccountType.FIXED_DEPOSIT, new BigDecimal("10000.00")
    );

    public AccountServiceImpl(AccountRepository accountRepository, 
                            AccountMapper accountMapper,
                            AccountNumberService accountNumberService) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.accountNumberService = accountNumberService;
    }

    @Override
    public AccountDto createAccount(CreateAccountRequest request) {
        log.info("Creating account for user: {} with type: {}", request.getUserId(), request.getAccountType());

        // Validate user can create account
        if (!canUserCreateAccount(request.getUserId(), request.getAccountType())) {
            throw new AccountOperationException("User has reached maximum account limit or cannot create this account type");
        }

        // Create account entity
        Account account = accountMapper.toEntity(request);
        
        // Generate unique account number
        String accountNumber = accountNumberService.generateAccountNumber(request.getAccountType());
        account.setAccountNumber(accountNumber);
        
        // Set default values based on account type
        setDefaultAccountValues(account);
        
        // Validate initial deposit meets minimum balance requirement
        if (account.getBalance().compareTo(account.getMinimumBalance()) < 0) {
            throw new AccountOperationException("Initial deposit must meet minimum balance requirement of " + account.getMinimumBalance());
        }

        // Save account
        Account savedAccount = accountRepository.save(account);
        
        log.info("Successfully created account: {} for user: {}", savedAccount.getAccountNumber(), savedAccount.getUserId());
        return accountMapper.toDto(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccount(Long accountId) {
        log.debug("Retrieving account with ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountByNumber(String accountNumber) {
        log.debug("Retrieving account with number: {}", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber, true));
        
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getUserAccounts(Long userId) {
        log.debug("Retrieving all accounts for user: {}", userId);
        
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accountMapper.toDtoList(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDto> getUserAccounts(Long userId, Pageable pageable) {
        log.debug("Retrieving accounts for user: {} with pagination", userId);
        
        Page<Account> accounts = accountRepository.findByUserId(userId, pageable);
        return accounts.map(accountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDto> getAccountsByStatus(AccountStatus status, Pageable pageable) {
        log.debug("Retrieving accounts with status: {}", status);
        
        Page<Account> accounts = accountRepository.findByStatus(status, pageable);
        return accounts.map(accountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDto> getAccountsByType(AccountType accountType, Pageable pageable) {
        log.debug("Retrieving accounts with type: {}", accountType);
        
        Page<Account> accounts = accountRepository.findByAccountType(accountType, pageable);
        return accounts.map(accountMapper::toDto);
    }

    @Override
    public AccountDto updateAccount(Long accountId, UpdateAccountRequest request) {
        log.info("Updating account: {} with request: {}", accountId, request);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        // Update account with request data
        accountMapper.updateEntityFromRequest(account, request);
        
        Account updatedAccount = accountRepository.save(account);
        log.info("Successfully updated account: {}", updatedAccount.getAccountNumber());
        
        return accountMapper.toDto(updatedAccount);
    }

    @Override
    public AccountDto activateAccount(Long accountId) {
        log.info("Activating account: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new AccountOperationException("Account is already active");
        }
        
        account.activate();
        Account updatedAccount = accountRepository.save(account);
        
        log.info("Successfully activated account: {}", updatedAccount.getAccountNumber());
        return accountMapper.toDto(updatedAccount);
    }

    @Override
    public AccountDto freezeAccount(Long accountId) {
        log.info("Freezing account: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        if (account.getIsFrozen()) {
            throw new AccountOperationException("Account is already frozen");
        }
        
        account.freeze();
        Account updatedAccount = accountRepository.save(account);
        
        log.info("Successfully froze account: {}", updatedAccount.getAccountNumber());
        return accountMapper.toDto(updatedAccount);
    }

    @Override
    public AccountDto unfreezeAccount(Long accountId) {
        log.info("Unfreezing account: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        if (!account.getIsFrozen()) {
            throw new AccountOperationException("Account is not frozen");
        }
        
        account.unfreeze();
        Account updatedAccount = accountRepository.save(account);
        
        log.info("Successfully unfroze account: {}", updatedAccount.getAccountNumber());
        return accountMapper.toDto(updatedAccount);
    }

    @Override
    public AccountDto closeAccount(Long accountId) {
        log.info("Closing account: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountOperationException("Account is already closed");
        }
        
        // Check if account has balance
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountOperationException("Cannot close account with positive balance. Please withdraw all funds first.");
        }
        
        account.close();
        Account updatedAccount = accountRepository.save(account);
        
        log.info("Successfully closed account: {}", updatedAccount.getAccountNumber());
        return accountMapper.toDto(updatedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceDto getAccountBalance(String accountNumber) {
        log.debug("Getting balance for account: {}", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber, true));
        
        return accountMapper.toBalanceDto(account);
    }

    @Override
    public AccountDto creditAccount(String accountNumber, BigDecimal amount, String description) {
        log.info("Crediting account: {} with amount: {} - {}", accountNumber, amount, description);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber, true));
        
        if (!account.canCredit(amount)) {
            throw new AccountOperationException("Account cannot be credited: " + accountNumber);
        }
        
        account.credit(amount);
        Account updatedAccount = accountRepository.save(account);
        
        log.info("Successfully credited account: {} with amount: {}", accountNumber, amount);
        return accountMapper.toDto(updatedAccount);
    }

    @Override
    public AccountDto debitAccount(String accountNumber, BigDecimal amount, String description) {
        log.info("Debiting account: {} with amount: {} - {}", accountNumber, amount, description);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber, true));
        
        if (!account.canDebit(amount)) {
            throw new InsufficientBalanceException(accountNumber, amount, account.getAvailableBalance());
        }
        
        account.debit(amount);
        Account updatedAccount = accountRepository.save(account);
        
        log.info("Successfully debited account: {} with amount: {}", accountNumber, amount);
        return accountMapper.toDto(updatedAccount);
    }

    @Override
    public void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        log.info("Transferring {} from account: {} to account: {} - {}", amount, fromAccountNumber, toAccountNumber, description);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        // Get both accounts
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(fromAccountNumber, true));
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(toAccountNumber, true));
        
        // Validate transfer
        if (!fromAccount.canDebit(amount)) {
            throw new InsufficientBalanceException(fromAccountNumber, amount, fromAccount.getAvailableBalance());
        }
        
        if (!toAccount.canCredit(amount)) {
            throw new AccountOperationException("Destination account cannot receive funds: " + toAccountNumber);
        }
        
        // Perform transfer
        fromAccount.debit(amount);
        toAccount.credit(amount);
        
        // Save both accounts
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        log.info("Successfully transferred {} from {} to {}", amount, fromAccountNumber, toAccountNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUserTotalBalance(Long userId) {
        log.debug("Calculating total balance for user: {}", userId);
        
        BigDecimal totalBalance = accountRepository.calculateTotalBalanceForUser(userId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getUserActiveAccounts(Long userId) {
        log.debug("Retrieving active accounts for user: {}", userId);
        
        List<Account> accounts = accountRepository.findActiveAccountsByUserId(userId);
        return accountMapper.toDtoList(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDto> searchAccounts(String searchTerm, Pageable pageable) {
        log.debug("Searching accounts with term: {}", searchTerm);
        
        Page<Account> accounts = accountRepository.searchAccountsByNameOrDescription(searchTerm, pageable);
        return accounts.map(accountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsBelowMinimumBalance() {
        log.debug("Retrieving accounts below minimum balance");
        
        List<Account> accounts = accountRepository.findAccountsBelowMinimumBalance();
        return accountMapper.toDtoList(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getDormantAccounts(int daysInactive) {
        log.debug("Retrieving dormant accounts with {} days inactivity", daysInactive);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        List<Account> accounts = accountRepository.findDormantAccounts(cutoffDate);
        return accountMapper.toDtoList(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserCreateAccount(Long userId, AccountType accountType) {
        long userAccountCount = accountRepository.countByUserId(userId);
        return userAccountCount < MAX_ACCOUNTS_PER_USER;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStatisticsDto getAccountStatistics() {
        log.debug("Generating account statistics");
        
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByStatus(AccountStatus.ACTIVE);
        long pendingAccounts = accountRepository.countByStatus(AccountStatus.PENDING);
        long closedAccounts = accountRepository.countByStatus(AccountStatus.CLOSED);
        
        // Calculate frozen accounts (active but frozen)
        List<Account> allActiveAccounts = accountRepository.findByUserIdAndStatus(null, AccountStatus.ACTIVE);
        long frozenAccounts = allActiveAccounts.stream()
                .mapToLong(account -> account.getIsFrozen() ? 1L : 0L)
                .sum();
        
        // Get account counts by type
        Map<String, Long> accountsByType = new HashMap<>();
        for (AccountType type : AccountType.values()) {
            accountsByType.put(type.name(), accountRepository.countByAccountType(type));
        }
        
        // Get balance distribution by type
        Map<String, BigDecimal> balancesByType = new HashMap<>();
        for (AccountType type : AccountType.values()) {
            BigDecimal balance = accountRepository.calculateTotalBalanceForAccountType(type);
            balancesByType.put(type.name(), balance != null ? balance : BigDecimal.ZERO);
        }
        
        BigDecimal totalBalance = balancesByType.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageBalance = totalAccounts > 0 ? 
                totalBalance.divide(BigDecimal.valueOf(totalAccounts), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        long accountsBelowMinBalance = accountRepository.findAccountsBelowMinimumBalance().size();
        long dormantAccounts = getDormantAccounts(DORMANT_DAYS_THRESHOLD).size();
        long accountsWithOverdraft = accountRepository.findAccountsWithOverdraft().size();
        
        return AccountStatisticsDto.builder()
                .totalAccounts(totalAccounts)
                .activeAccounts(activeAccounts)
                .inactiveAccounts(pendingAccounts)
                .frozenAccounts(frozenAccounts)
                .closedAccounts(closedAccounts)
                .totalBalance(totalBalance)
                .averageBalance(averageBalance)
                .accountsByType(accountsByType)
                .balancesByType(balancesByType)
                .accountsBelowMinBalance(accountsBelowMinBalance)
                .dormantAccounts(dormantAccounts)
                .accountsWithOverdraft(accountsWithOverdraft)
                .build();
    }

    /**
     * Set default values for account based on type
     */
    private void setDefaultAccountValues(Account account) {
        // Set minimum balance based on account type
        BigDecimal minimumBalance = MINIMUM_BALANCES.getOrDefault(account.getAccountType(), DEFAULT_MINIMUM_BALANCE);
        if (account.getMinimumBalance() == null || account.getMinimumBalance().compareTo(BigDecimal.ZERO) == 0) {
            account.setMinimumBalance(minimumBalance);
        }
        
        // Set default currency if not provided
        if (account.getCurrencyCode() == null || account.getCurrencyCode().isEmpty()) {
            account.setCurrencyCode("KES");
        }
        
        // Set default status
        if (account.getStatus() == null) {
            account.setStatus(AccountStatus.PENDING);
        }
        
        // Set default boolean values
        if (account.getIsFrozen() == null) {
            account.setIsFrozen(false);
        }
        
        if (account.getOverdraftAllowed() == null) {
            account.setOverdraftAllowed(false);
        }
    }
} 