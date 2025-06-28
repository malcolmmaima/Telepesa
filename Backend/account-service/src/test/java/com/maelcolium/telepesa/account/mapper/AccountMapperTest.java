package com.maelcolium.telepesa.account.mapper;

import com.maelcolium.telepesa.account.dto.AccountBalanceDto;
import com.maelcolium.telepesa.account.dto.AccountDto;
import com.maelcolium.telepesa.account.dto.CreateAccountRequest;
import com.maelcolium.telepesa.account.dto.UpdateAccountRequest;
import com.maelcolium.telepesa.account.model.Account;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for AccountMapper.
 * Tests all mapping methods including edge cases and null handling.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
class AccountMapperTest {

    private AccountMapper accountMapper;
    private Account testAccount;
    private CreateAccountRequest createAccountRequest;
    private UpdateAccountRequest updateAccountRequest;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();

        testAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC001")
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("4800.00"))
                .minimumBalance(new BigDecimal("1000.00"))
                .dailyLimit(new BigDecimal("50000.00"))
                .monthlyLimit(new BigDecimal("500000.00"))
                .currencyCode("KES")
                .accountName("Test Savings Account")
                .description("Test account description")
                .interestRate(new BigDecimal("2.5"))
                .isFrozen(false)
                .overdraftAllowed(false)
                .overdraftLimit(BigDecimal.ZERO)
                .lastTransactionDate(LocalDateTime.now().minusDays(1))
                .activatedAt(LocalDateTime.now().minusDays(30))
                .closedAt(null)
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        createAccountRequest = CreateAccountRequest.builder()
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("2000.00"))
                .currencyCode("KES")
                .accountName("New Test Account")
                .description("New account description")
                .overdraftAllowed(false)
                .overdraftLimit(BigDecimal.ZERO)
                .dailyLimit(new BigDecimal("30000.00"))
                .monthlyLimit(new BigDecimal("300000.00"))
                .interestRate(new BigDecimal("2.0"))
                .build();

        updateAccountRequest = UpdateAccountRequest.builder()
                .accountName("Updated Account Name")
                .description("Updated description")
                .dailyLimit(new BigDecimal("60000.00"))
                .monthlyLimit(new BigDecimal("600000.00"))
                .overdraftAllowed(true)
                .overdraftLimit(new BigDecimal("10000.00"))
                .interestRate(new BigDecimal("3.0"))
                .build();
    }

    @Test
    void toDto_WithValidAccount_ShouldReturnAccountDto() {
        // When
        AccountDto result = accountMapper.toDto(testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testAccount.getId());
        assertThat(result.getAccountNumber()).isEqualTo(testAccount.getAccountNumber());
        assertThat(result.getUserId()).isEqualTo(testAccount.getUserId());
        assertThat(result.getAccountType()).isEqualTo(testAccount.getAccountType());
        assertThat(result.getStatus()).isEqualTo(testAccount.getStatus());
        assertThat(result.getBalance()).isEqualTo(testAccount.getBalance());
        assertThat(result.getAvailableBalance()).isEqualTo(testAccount.getAvailableBalance());
        assertThat(result.getMinimumBalance()).isEqualTo(testAccount.getMinimumBalance());
        assertThat(result.getCurrencyCode()).isEqualTo(testAccount.getCurrencyCode());
        assertThat(result.getAccountName()).isEqualTo(testAccount.getAccountName());
        assertThat(result.getDescription()).isEqualTo(testAccount.getDescription());
        assertThat(result.getIsFrozen()).isEqualTo(testAccount.getIsFrozen());
    }

    @Test
    void toDto_WithNullAccount_ShouldReturnNull() {
        // When
        AccountDto result = accountMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithValidCreateRequest_ShouldReturnAccount() {
        // When
        Account result = accountMapper.toEntity(createAccountRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(createAccountRequest.getUserId());
        assertThat(result.getAccountType()).isEqualTo(createAccountRequest.getAccountType());
        assertThat(result.getCurrencyCode()).isEqualTo(createAccountRequest.getCurrencyCode());
        assertThat(result.getBalance()).isEqualTo(createAccountRequest.getInitialDeposit());
        assertThat(result.getAvailableBalance()).isEqualTo(createAccountRequest.getInitialDeposit());
        assertThat(result.getAccountName()).isEqualTo(createAccountRequest.getAccountName());
        assertThat(result.getDescription()).isEqualTo(createAccountRequest.getDescription());
    }

    @Test
    void toEntity_WithNullCreateRequest_ShouldReturnNull() {
        // When
        Account result = accountMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void updateEntityFromRequest_WithValidRequest_ShouldUpdateAccount() {
        // Given
        Account originalAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC001")
                .accountName("Original Name")
                .description("Original description")
                .dailyLimit(new BigDecimal("20000.00"))
                .monthlyLimit(new BigDecimal("200000.00"))
                .overdraftAllowed(false)
                .overdraftLimit(BigDecimal.ZERO)
                .interestRate(new BigDecimal("1.5"))
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();

        // When
        accountMapper.updateEntityFromRequest(originalAccount, updateAccountRequest);

        // Then
        assertThat(originalAccount.getAccountName()).isEqualTo("Updated Account Name");
        assertThat(originalAccount.getDescription()).isEqualTo("Updated description");
        assertThat(originalAccount.getDailyLimit()).isEqualTo(new BigDecimal("60000.00"));
        assertThat(originalAccount.getMonthlyLimit()).isEqualTo(new BigDecimal("600000.00"));
        assertThat(originalAccount.getOverdraftAllowed()).isTrue();
        assertThat(originalAccount.getOverdraftLimit()).isEqualTo(new BigDecimal("10000.00"));
        assertThat(originalAccount.getInterestRate()).isEqualTo(new BigDecimal("3.0"));
        assertThat(originalAccount.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
        
        // Verify unchanged fields
        assertThat(originalAccount.getId()).isEqualTo(1L);
        assertThat(originalAccount.getAccountNumber()).isEqualTo("ACC001");
    }

    @Test
    void updateEntityFromRequest_WithNullAccount_ShouldNotThrowException() {
        // When & Then
        assertThatCode(() -> accountMapper.updateEntityFromRequest(null, updateAccountRequest))
                .doesNotThrowAnyException();
    }

    @Test
    void updateEntityFromRequest_WithNullRequest_ShouldNotThrowException() {
        // Given
        Account account = Account.builder().accountName("Original Name").build();

        // When & Then
        assertThatCode(() -> accountMapper.updateEntityFromRequest(account, null))
                .doesNotThrowAnyException();
        
        assertThat(account.getAccountName()).isEqualTo("Original Name");
    }

    @Test
    void toBalanceDto_WithValidAccount_ShouldReturnAccountBalanceDto() {
        // When
        AccountBalanceDto result = accountMapper.toBalanceDto(testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo(testAccount.getAccountNumber());
        assertThat(result.getBalance()).isEqualTo(testAccount.getBalance());
        assertThat(result.getAvailableBalance()).isEqualTo(testAccount.getAvailableBalance());
        assertThat(result.getMinimumBalance()).isEqualTo(testAccount.getMinimumBalance());
        assertThat(result.getCurrencyCode()).isEqualTo(testAccount.getCurrencyCode());
        assertThat(result.getLastTransactionDate()).isEqualTo(testAccount.getLastTransactionDate());
        assertThat(result.getIsActive()).isEqualTo(testAccount.isActive());
    }

    @Test
    void toBalanceDto_WithNullAccount_ShouldReturnNull() {
        // When
        AccountBalanceDto result = accountMapper.toBalanceDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDtoList_WithValidAccountList_ShouldReturnAccountDtoList() {
        // Given
        Account account2 = Account.builder()
                .id(2L)
                .accountNumber("ACC002")
                .userId(101L)
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("3000.00"))
                .build();
        
        List<Account> accounts = Arrays.asList(testAccount, account2);

        // When
        List<AccountDto> result = accountMapper.toDtoList(accounts);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("ACC001");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getAccountNumber()).isEqualTo("ACC002");
    }

    @Test
    void toDtoList_WithNullList_ShouldReturnNull() {
        // When
        List<AccountDto> result = accountMapper.toDtoList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDtoList_WithEmptyList_ShouldReturnEmptyList() {
        // When
        List<AccountDto> result = accountMapper.toDtoList(Arrays.asList());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
