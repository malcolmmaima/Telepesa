package com.maelcolium.telepesa.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maelcolium.telepesa.account.dto.*;
import com.maelcolium.telepesa.account.exception.AccountNotFoundException;
import com.maelcolium.telepesa.account.exception.AccountOperationException;
import com.maelcolium.telepesa.account.service.AccountService;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for AccountController.
 * Tests all REST endpoints, validation, and error handling.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountDto testAccountDto;
    private CreateAccountRequest createAccountRequest;
    private UpdateAccountRequest updateAccountRequest;
    private AccountBalanceDto accountBalanceDto;

    @BeforeEach
    void setUp() {
        testAccountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("ACC001")
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("5000.00"))
                .minimumBalance(new BigDecimal("1000.00"))
                .currencyCode("KES")
                .accountName("Test Savings Account")
                .isFrozen(false)
                .createdAt(LocalDateTime.now())
                .build();

        createAccountRequest = CreateAccountRequest.builder()
                .userId(100L)
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("2000.00"))
                .currencyCode("KES")
                .accountName("Test Account")
                .build();

        updateAccountRequest = UpdateAccountRequest.builder()
                .accountName("Updated Account Name")
                .description("Updated description")
                .dailyLimit(new BigDecimal("10000.00"))
                .build();

        accountBalanceDto = AccountBalanceDto.builder()
                .accountNumber("ACC001")
                .balance(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("5000.00"))
                .minimumBalance(new BigDecimal("1000.00"))
                .currencyCode("KES")
                .isActive(true)
                .build();
    }

    @Test
    void createAccount_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    void createAccount_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - invalid request with missing required fields
        CreateAccountRequest invalidRequest = CreateAccountRequest.builder().build();

        // When & Then
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_WhenServiceThrowsException_ShouldReturnConflict() throws Exception {
        // Given
        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenThrow(new AccountOperationException("User has reached maximum account limit"));

        // When & Then
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User has reached maximum account limit"));
    }

    @Test
    void getAccount_WithValidId_ShouldReturnAccount() throws Exception {
        // Given
        when(accountService.getAccount(1L)).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.userId").value(100L));
    }

    @Test
    void getAccount_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(accountService.getAccount(999L)).thenThrow(new AccountNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccountByNumber_WithValidNumber_ShouldReturnAccount() throws Exception {
        // Given
        when(accountService.getAccountByNumber("ACC001")).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/number/ACC001"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.userId").value(100L));
    }

    @Test
    void getUserAccounts_WithValidUserId_ShouldReturnAccountList() throws Exception {
        // Given
        List<AccountDto> accounts = Arrays.asList(testAccountDto);
        when(accountService.getUserAccounts(100L)).thenReturn(accounts);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(100L));
    }

    @Test
    void getUserAccountsWithPagination_ShouldReturnPagedResults() throws Exception {
        // Given
        Page<AccountDto> accountPage = new PageImpl<>(Arrays.asList(testAccountDto), PageRequest.of(0, 10), 1);
        when(accountService.getUserAccounts(eq(100L), any())).thenReturn(accountPage);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/user/100/paged")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpected(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updateAccount_WithValidRequest_ShouldReturnUpdatedAccount() throws Exception {
        // Given
        AccountDto updatedAccount = testAccountDto.toBuilder()
                .accountName("Updated Account Name")
                .description("Updated description")
                .build();
        when(accountService.updateAccount(eq(1L), any(UpdateAccountRequest.class))).thenReturn(updatedAccount);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpected(jsonPath("$.accountName").value("Updated Account Name"));
    }

    @Test
    void activateAccount_WithValidId_ShouldReturnActivatedAccount() throws Exception {
        // Given
        when(accountService.activateAccount(1L)).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void freezeAccount_WithValidId_ShouldReturnFrozenAccount() throws Exception {
        // Given
        AccountDto frozenAccount = testAccountDto.toBuilder().isFrozen(true).build();
        when(accountService.freezeAccount(1L)).thenReturn(frozenAccount);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/1/freeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpected(jsonPath("$.isFrozen").value(true));
    }

    @Test
    void unfreezeAccount_WithValidId_ShouldReturnUnfrozenAccount() throws Exception {
        // Given
        when(accountService.unfreezeAccount(1L)).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/1/unfreeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpected(jsonPath("$.isFrozen").value(false));
    }

    @Test
    void closeAccount_WithValidId_ShouldReturnClosedAccount() throws Exception {
        // Given
        AccountDto closedAccount = testAccountDto.toBuilder().status(AccountStatus.CLOSED).build();
        when(accountService.closeAccount(1L)).thenReturn(closedAccount);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/1/close"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void getAccountBalance_WithValidAccountNumber_ShouldReturnBalance() throws Exception {
        // Given
        when(accountService.getAccountBalance("ACC001")).thenReturn(accountBalanceDto);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/ACC001/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpected(jsonPath("$.availableBalance").value(5000.00));
    }

    @Test
    void creditAccount_WithValidRequest_ShouldReturnUpdatedAccount() throws Exception {
        // Given
        AccountBalanceUpdateRequest creditRequest = AccountBalanceUpdateRequest.builder()
                .amount(new BigDecimal("1000.00"))
                .description("Test credit")
                .build();
        
        AccountDto creditedAccount = testAccountDto.toBuilder()
                .balance(new BigDecimal("6000.00"))
                .availableBalance(new BigDecimal("6000.00"))
                .build();
        
        when(accountService.creditAccount(eq("ACC001"), eq(new BigDecimal("1000.00")), eq("Test credit")))
                .thenReturn(creditedAccount);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/ACC001/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditRequest)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.balance").value(6000.00));
    }

    @Test
    void debitAccount_WithValidRequest_ShouldReturnUpdatedAccount() throws Exception {
        // Given
        AccountBalanceUpdateRequest debitRequest = AccountBalanceUpdateRequest.builder()
                .amount(new BigDecimal("500.00"))
                .description("Test debit")
                .build();
        
        AccountDto debitedAccount = testAccountDto.toBuilder()
                .balance(new BigDecimal("4500.00"))
                .availableBalance(new BigDecimal("4500.00"))
                .build();
        
        when(accountService.debitAccount(eq("ACC001"), eq(new BigDecimal("500.00")), eq("Test debit")))
                .thenReturn(debitedAccount);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/ACC001/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(debitRequest)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.balance").value(4500.00));
    }

    @Test
    void transferFunds_WithValidRequest_ShouldReturnOk() throws Exception {
        // Given
        TransferFundsRequest transferRequest = TransferFundsRequest.builder()
                .fromAccountNumber("ACC001")
                .toAccountNumber("ACC002")
                .amount(new BigDecimal("1000.00"))
                .description("Test transfer")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpected(status().isOk());
    }

    @Test
    void getAccountsByStatus_ShouldReturnFilteredAccounts() throws Exception {
        // Given
        Page<AccountDto> accountPage = new PageImpl<>(Arrays.asList(testAccountDto), PageRequest.of(0, 10), 1);
        when(accountService.getAccountsByStatus(eq(AccountStatus.ACTIVE), any())).thenReturn(accountPage);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/status/ACTIVE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    void getAccountsByType_ShouldReturnFilteredAccounts() throws Exception {
        // Given
        Page<AccountDto> accountPage = new PageImpl<>(Arrays.asList(testAccountDto), PageRequest.of(0, 10), 1);
        when(accountService.getAccountsByType(eq(AccountType.SAVINGS), any())).thenReturn(accountPage);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/type/SAVINGS")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.content[0].accountType").value("SAVINGS"));
    }

    @Test
    void searchAccounts_WithValidSearchTerm_ShouldReturnFilteredAccounts() throws Exception {
        // Given
        Page<AccountDto> accountPage = new PageImpl<>(Arrays.asList(testAccountDto), PageRequest.of(0, 10), 1);
        when(accountService.searchAccounts(eq("test"), any())).thenReturn(accountPage);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/search")
                .param("searchTerm", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.content").isArray());
    }

    @Test
    void getUserTotalBalance_WithValidUserId_ShouldReturnTotalBalance() throws Exception {
        // Given
        when(accountService.getUserTotalBalance(100L)).thenReturn(new BigDecimal("15000.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/user/100/total-balance"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.totalBalance").value(15000.00));
    }

    @Test
    void getAccountStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        AccountStatisticsDto statistics = AccountStatisticsDto.builder()
                .totalAccounts(100L)
                .activeAccounts(80L)
                .pendingAccounts(15L)
                .closedAccounts(5L)
                .frozenAccounts(2L)
                .totalBalance(new BigDecimal("1000000.00"))
                .build();
        
        when(accountService.getAccountStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/statistics"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.totalAccounts").value(100))
                .andExpected(jsonPath("$.activeAccounts").value(80))
                .andExpected(jsonPath("$.totalBalance").value(1000000.00));
    }
} 