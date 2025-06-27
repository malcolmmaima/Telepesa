package com.maelcolium.telepesa.account.mapper;

import com.maelcolium.telepesa.account.dto.AccountBalanceDto;
import com.maelcolium.telepesa.account.dto.AccountDto;
import com.maelcolium.telepesa.account.dto.CreateAccountRequest;
import com.maelcolium.telepesa.account.dto.UpdateAccountRequest;
import com.maelcolium.telepesa.account.model.Account;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Account entities and DTOs.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Component
public class AccountMapper {

    /**
     * Convert Account entity to AccountDto
     */
    public AccountDto toDto(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .minimumBalance(account.getMinimumBalance())
                .dailyLimit(account.getDailyLimit())
                .monthlyLimit(account.getMonthlyLimit())
                .currencyCode(account.getCurrencyCode())
                .accountName(account.getAccountName())
                .description(account.getDescription())
                .interestRate(account.getInterestRate())
                .isFrozen(account.getIsFrozen())
                .overdraftAllowed(account.getOverdraftAllowed())
                .overdraftLimit(account.getOverdraftLimit())
                .lastTransactionDate(account.getLastTransactionDate())
                .activatedAt(account.getActivatedAt())
                .closedAt(account.getClosedAt())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateAccountRequest to Account entity
     */
    public Account toEntity(CreateAccountRequest request) {
        if (request == null) {
            return null;
        }

        return Account.builder()
                .userId(request.getUserId())
                .accountType(request.getAccountType())
                .currencyCode(request.getCurrencyCode())
                .balance(request.getInitialDeposit())
                .availableBalance(request.getInitialDeposit())
                .accountName(request.getAccountName())
                .description(request.getDescription())
                .overdraftAllowed(request.getOverdraftAllowed())
                .overdraftLimit(request.getOverdraftLimit())
                .dailyLimit(request.getDailyLimit())
                .monthlyLimit(request.getMonthlyLimit())
                .interestRate(request.getInterestRate())
                .build();
    }

    /**
     * Update Account entity with UpdateAccountRequest data
     */
    public void updateEntityFromRequest(Account account, UpdateAccountRequest request) {
        if (account == null || request == null) {
            return;
        }

        if (request.getAccountName() != null) {
            account.setAccountName(request.getAccountName());
        }
        if (request.getDescription() != null) {
            account.setDescription(request.getDescription());
        }
        if (request.getDailyLimit() != null) {
            account.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            account.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getOverdraftAllowed() != null) {
            account.setOverdraftAllowed(request.getOverdraftAllowed());
        }
        if (request.getOverdraftLimit() != null) {
            account.setOverdraftLimit(request.getOverdraftLimit());
        }
        if (request.getInterestRate() != null) {
            account.setInterestRate(request.getInterestRate());
        }
        
        account.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Convert Account entity to AccountBalanceDto
     */
    public AccountBalanceDto toBalanceDto(Account account) {
        if (account == null) {
            return null;
        }

        return AccountBalanceDto.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .minimumBalance(account.getMinimumBalance())
                .currencyCode(account.getCurrencyCode())
                .lastTransactionDate(account.getLastTransactionDate())
                .isActive(account.isActive())
                .build();
    }

    /**
     * Convert list of Account entities to list of AccountDto
     */
    public List<AccountDto> toDtoList(List<Account> accounts) {
        if (accounts == null) {
            return null;
        }

        return accounts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Account entities to list of AccountBalanceDto
     */
    public List<AccountBalanceDto> toBalanceDtoList(List<Account> accounts) {
        if (accounts == null) {
            return null;
        }

        return accounts.stream()
                .map(this::toBalanceDto)
                .collect(Collectors.toList());
    }
} 