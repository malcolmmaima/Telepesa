package com.maelcolium.telepesa.account.service;

import com.maelcolium.telepesa.account.dto.*;
import com.maelcolium.telepesa.account.exception.AccountNotFoundException;
import com.maelcolium.telepesa.account.exception.AccountOperationException;
import com.maelcolium.telepesa.account.exception.InsufficientBalanceException;
import com.maelcolium.telepesa.account.mapper.AccountMapper;
import com.maelcolium.telepesa.account.model.Account;
import com.maelcolium.telepesa.account.repository.AccountRepository;
import com.maelcolium.telepesa.account.service.impl.AccountServiceImpl;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Comprehensive unit tests for AccountService implementation.
 * Tests all business logic paths including edge cases and error scenarios.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountNumberService accountNumberService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private AccountDto testAccountDto;
    private CreateAccountRequest createAccountRequest;
    private UpdateAccountRequest updateAccountRequest;

    @BeforeEach
    void setUp() {
        // Test data setup with proper available balance
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC001")
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("5000.00")) // Important: set available balance
                .minimumBalance(new BigDecimal("1000.00"))
                .isFrozen(false)
                .createdAt(LocalDateTime.now())
                .build();

        testAccountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("ACC001")
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("5000.00"))
                .minimumBalance(new BigDecimal("1000.00"))
                .isFrozen(false)
                .build();

        createAccountRequest = CreateAccountRequest.builder()
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("2000.00"))
                .build();

        updateAccountRequest = UpdateAccountRequest.builder()
                .accountName("Updated Test Account")
                .description("Updated description")
                .dailyLimit(new BigDecimal("10000.00"))
                .build();
    }

    // CREATE ACCOUNT TESTS
    @Test
    void createAccount_WithValidRequest_ShouldReturnAccountDto() {
        // Given
        when(accountRepository.countByUserId(100L)).thenReturn(1L); // User has 1 account (within limit)
        when(accountMapper.toEntity(createAccountRequest)).thenReturn(testAccount);
        when(accountNumberService.generateAccountNumber(AccountType.SAVINGS)).thenReturn("ACC001");
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toDto(testAccount)).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.createAccount(createAccountRequest);

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).save(any(Account.class));
        verify(accountNumberService).generateAccountNumber(AccountType.SAVINGS);
    }

    @Test
    void createAccount_WhenUserExceedsAccountLimit_ShouldThrowException() {
        // Given
        when(accountRepository.countByUserId(100L)).thenReturn(10L); // User has max accounts

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(createAccountRequest))
                .isInstanceOf(AccountOperationException.class)
                .hasMessageContaining("maximum account limit");

        verify(accountRepository, never()).save(any(Account.class));
    }

    // GET ACCOUNT TESTS
    @Test
    void getAccount_WithValidId_ShouldReturnAccountDto() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountMapper.toDto(testAccount)).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.getAccount(1L);

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccount_WithInvalidId_ShouldThrowAccountNotFoundException() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.getAccount(999L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAccountByNumber_WithValidNumber_ShouldReturnAccountDto() {
        // Given
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(accountMapper.toDto(testAccount)).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.getAccountByNumber("ACC001");

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).findByAccountNumber("ACC001");
    }

    @Test
    void getAccountByNumber_WithInvalidNumber_ShouldThrowAccountNotFoundException() {
        // Given
        when(accountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.getAccountByNumber("INVALID"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // USER ACCOUNTS TESTS
    @Test
    void getUserAccounts_WithValidUserId_ShouldReturnAccountList() {
        // Given
        List<Account> accounts = Arrays.asList(testAccount);
        List<AccountDto> accountDtos = Arrays.asList(testAccountDto);
        when(accountRepository.findByUserId(100L)).thenReturn(accounts);
        when(accountMapper.toDtoList(accounts)).thenReturn(accountDtos);

        // When
        List<AccountDto> result = accountService.getUserAccounts(100L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAccountDto);
        verify(accountRepository).findByUserId(100L);
    }

    @Test
    void getUserAccounts_WithPagination_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount));
        when(accountRepository.findByUserId(100L, pageable)).thenReturn(accountPage);
        when(accountMapper.toDto(testAccount)).thenReturn(testAccountDto);

        // When
        Page<AccountDto> result = accountService.getUserAccounts(100L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testAccountDto);
        verify(accountRepository).findByUserId(100L, pageable);
    }

    // ACCOUNT STATUS OPERATIONS TESTS
    @Test
    void activateAccount_WithValidId_ShouldReturnActivatedAccount() {
        // Given
        Account inactiveAccount = Account.builder()
                .id(1L)
                .status(AccountStatus.PENDING)
                .build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(inactiveAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(inactiveAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.activateAccount(1L);

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void activateAccount_WhenAlreadyActive_ShouldThrowException() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.activateAccount(1L))
                .isInstanceOf(AccountOperationException.class)
                .hasMessageContaining("already active");
    }

    @Test
    void freezeAccount_WithValidId_ShouldReturnFrozenAccount() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.freezeAccount(1L);

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void freezeAccount_WhenAlreadyFrozen_ShouldThrowException() {
        // Given
        Account frozenAccount = Account.builder()
                .id(1L)
                .isFrozen(true)
                .build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(frozenAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.freezeAccount(1L))
                .isInstanceOf(AccountOperationException.class)
                .hasMessageContaining("already frozen");
    }

    // BALANCE OPERATIONS TESTS
    @Test
    void getAccountBalance_WithValidAccountNumber_ShouldReturnBalance() {
        // Given
        AccountBalanceDto balanceDto = AccountBalanceDto.builder()
                .accountNumber("ACC001")
                .balance(new BigDecimal("5000.00"))
                .build();
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(accountMapper.toBalanceDto(testAccount)).thenReturn(balanceDto);

        // When
        AccountBalanceDto result = accountService.getAccountBalance("ACC001");

        // Then
        assertThat(result.getBalance()).isEqualTo(new BigDecimal("5000.00"));
        verify(accountRepository).findByAccountNumber("ACC001");
    }

    @Test
    void creditAccount_WithValidAmount_ShouldIncreaseBalance() {
        // Given
        BigDecimal creditAmount = new BigDecimal("1000.00");
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.creditAccount("ACC001", creditAmount, "Test credit");

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void debitAccount_WithValidAmount_ShouldDecreaseBalance() {
        // Given
        BigDecimal debitAmount = new BigDecimal("500.00");
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.debitAccount("ACC001", debitAmount, "Test debit");

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void debitAccount_WithInsufficientBalance_ShouldThrowException() {
        // Given
        Account lowBalanceAccount = Account.builder()
                .accountNumber("ACC001")
                .balance(new BigDecimal("100.00"))
                .availableBalance(new BigDecimal("100.00"))
                .build();
        BigDecimal largeDebitAmount = new BigDecimal("500.00");
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(lowBalanceAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.debitAccount("ACC001", largeDebitAmount, "Test debit"))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    // TRANSFER OPERATIONS TESTS
    @Test
    void transferFunds_WithValidAccounts_ShouldTransferSuccessfully() {
        // Given
        Account toAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC002")
                .status(AccountStatus.ACTIVE) // Important: set status to ACTIVE
                .balance(new BigDecimal("3000.00"))
                .availableBalance(new BigDecimal("3000.00"))
                .isFrozen(false) // Important: not frozen
                .build();
        
        BigDecimal transferAmount = new BigDecimal("500.00");
        
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(toAccount));

        // When
        accountService.transferFunds("ACC001", "ACC002", transferAmount, "Test transfer");

        // Then
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferFunds_WithInsufficientBalance_ShouldThrowException() {
        // Given
        Account lowBalanceAccount = Account.builder()
                .accountNumber("ACC001")
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .availableBalance(new BigDecimal("100.00"))
                .isFrozen(false)
                .build();
        Account toAccount = Account.builder()
                .accountNumber("ACC002")
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("3000.00"))
                .availableBalance(new BigDecimal("3000.00"))
                .isFrozen(false)
                .build();
        
        BigDecimal largeTransferAmount = new BigDecimal("500.00");
        
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(lowBalanceAccount));
        when(accountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(toAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.transferFunds("ACC001", "ACC002", largeTransferAmount, "Test transfer"))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    // UPDATE ACCOUNT TESTS
    @Test
    void updateAccount_WithValidRequest_ShouldReturnUpdatedAccount() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.updateAccount(1L, updateAccountRequest);

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountMapper).updateEntityFromRequest(testAccount, updateAccountRequest);
        verify(accountRepository).save(testAccount);
    }

    // SEARCH AND FILTER TESTS
    @Test
    void getAccountsByStatus_WithValidStatus_ShouldReturnFilteredAccounts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount));
        when(accountRepository.findByStatus(AccountStatus.ACTIVE, pageable)).thenReturn(accountPage);
        when(accountMapper.toDto(testAccount)).thenReturn(testAccountDto);

        // When
        Page<AccountDto> result = accountService.getAccountsByStatus(AccountStatus.ACTIVE, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(accountRepository).findByStatus(AccountStatus.ACTIVE, pageable);
    }

    @Test
    void getAccountsByType_WithValidType_ShouldReturnFilteredAccounts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount));
        when(accountRepository.findByAccountType(AccountType.SAVINGS, pageable)).thenReturn(accountPage);
        when(accountMapper.toDto(testAccount)).thenReturn(testAccountDto);

        // When
        Page<AccountDto> result = accountService.getAccountsByType(AccountType.SAVINGS, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(accountRepository).findByAccountType(AccountType.SAVINGS, pageable);
    }

    // BUSINESS LOGIC TESTS
    @Test
    void getUserTotalBalance_WithValidUserId_ShouldReturnTotalBalance() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("7000.00");
        when(accountRepository.calculateTotalBalanceForUser(100L)).thenReturn(expectedTotal);

        // When
        BigDecimal result = accountService.getUserTotalBalance(100L);

        // Then
        assertThat(result).isEqualTo(expectedTotal);
        verify(accountRepository).calculateTotalBalanceForUser(100L);
    }

    @Test
    void canUserCreateAccount_WithinLimit_ShouldReturnTrue() {
        // Given
        when(accountRepository.countByUserId(100L)).thenReturn(1L); // Only 1 account

        // When
        boolean result = accountService.canUserCreateAccount(100L, AccountType.CHECKING);

        // Then
        assertThat(result).isTrue();
        verify(accountRepository).countByUserId(100L);
    }

    @Test
    void canUserCreateAccount_ExceedsLimit_ShouldReturnFalse() {
        // Given
        when(accountRepository.countByUserId(100L)).thenReturn(10L); // At limit

        // When
        boolean result = accountService.canUserCreateAccount(100L, AccountType.CHECKING);

        // Then
        assertThat(result).isFalse();
        verify(accountRepository).countByUserId(100L);
    }

    // EDGE CASES AND ERROR SCENARIOS
    @Test
    void closeAccount_WithZeroBalance_ShouldCloseAccount() {
        // Given
        Account zeroBalanceAccount = Account.builder()
                .id(1L)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(zeroBalanceAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(zeroBalanceAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.closeAccount(1L);

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void closeAccount_WithPositiveBalance_ShouldThrowException() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount)); // Has positive balance

        // When & Then
        assertThatThrownBy(() -> accountService.closeAccount(1L))
                .isInstanceOf(AccountOperationException.class)
                .hasMessageContaining("positive balance");
    }

    @Test
    void getUserActiveAccounts_WithValidUserId_ShouldReturnActiveAccounts() {
        // Given
        List<Account> activeAccounts = Arrays.asList(testAccount);
        List<AccountDto> activeAccountDtos = Arrays.asList(testAccountDto);
        when(accountRepository.findActiveAccountsByUserId(100L)).thenReturn(activeAccounts);
        when(accountMapper.toDtoList(activeAccounts)).thenReturn(activeAccountDtos);

        // When
        List<AccountDto> result = accountService.getUserActiveAccounts(100L);

        // Then
        assertThat(result).hasSize(1);
        verify(accountRepository).findActiveAccountsByUserId(100L);
    }

    @Test
    void unfreezeAccount_WithValidId_ShouldReturnUnfrozenAccount() {
        // Given
        Account frozenAccount = Account.builder()
                .id(1L)
                .isFrozen(true)
                .build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(frozenAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(frozenAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(testAccountDto);

        // When
        AccountDto result = accountService.unfreezeAccount(1L);

        // Then
        assertThat(result).isEqualTo(testAccountDto);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void searchAccounts_WithValidSearchTerm_ShouldReturnFilteredAccounts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount));
        when(accountRepository.searchAccountsByNameOrDescription("test", pageable)).thenReturn(accountPage);
        when(accountMapper.toDto(testAccount)).thenReturn(testAccountDto);

        // When
        Page<AccountDto> result = accountService.searchAccounts("test", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(accountRepository).searchAccountsByNameOrDescription("test", pageable);
    }

    @Test
    void getAccountsBelowMinimumBalance_ShouldReturnFilteredAccounts() {
        // Given
        List<Account> lowBalanceAccounts = Arrays.asList(testAccount);
        List<AccountDto> lowBalanceAccountDtos = Arrays.asList(testAccountDto);
        when(accountRepository.findAccountsBelowMinimumBalance()).thenReturn(lowBalanceAccounts);
        when(accountMapper.toDtoList(lowBalanceAccounts)).thenReturn(lowBalanceAccountDtos);

        // When
        List<AccountDto> result = accountService.getAccountsBelowMinimumBalance();

        // Then
        assertThat(result).hasSize(1);
        verify(accountRepository).findAccountsBelowMinimumBalance();
    }

    @Test
    void getDormantAccounts_WithValidDays_ShouldReturnDormantAccounts() {
        // Given
        List<Account> dormantAccounts = Arrays.asList(testAccount);
        List<AccountDto> dormantAccountDtos = Arrays.asList(testAccountDto);
        when(accountRepository.findDormantAccounts(any(LocalDateTime.class))).thenReturn(dormantAccounts);
        when(accountMapper.toDtoList(dormantAccounts)).thenReturn(dormantAccountDtos);

        // When
        List<AccountDto> result = accountService.getDormantAccounts(90);

        // Then
        assertThat(result).hasSize(1);
        verify(accountRepository).findDormantAccounts(any(LocalDateTime.class));
    }
} 