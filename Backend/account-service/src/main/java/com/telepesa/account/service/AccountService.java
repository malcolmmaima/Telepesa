package com.telepesa.account.service;

import com.telepesa.account.dto.AccountDto;
import com.telepesa.account.entity.Account;
import com.telepesa.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;


    public Optional<AccountDto> getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<AccountDto> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(this::convertToDto);
    }

    public List<AccountDto> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AccountDto createAccount(Long userId, String accountType) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUserId(userId);
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setMinimumBalance(BigDecimal.ZERO);
        account.setCurrencyCode("KES");
        account.setStatus("ACTIVE");
        account.setIsFrozen(false);
        account.setKycVerified(false);
        account.setOverdraftAllowed(false);
        account.setVerificationLevel(1);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);
        log.info("Created new account: {}", savedAccount.getAccountNumber());
        
        return convertToDto(savedAccount);
    }

    public Optional<AccountDto> updateAccountBalance(String accountNumber, BigDecimal newBalance) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> {
                    account.setBalance(newBalance);
                    account.setAvailableBalance(newBalance); // Update available balance too
                    account.setUpdatedAt(LocalDateTime.now());
                    Account savedAccount = accountRepository.save(account);
                    log.info("Updated balance for account {}: {}", accountNumber, newBalance);
                    return convertToDto(savedAccount);
                });
    }

    private AccountDto convertToDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getAccountNumber(),
                account.getUserId(),
                account.getAccountType(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.getMinimumBalance(),
                account.getCurrencyCode(),
                account.getStatus(),
                account.getIsFrozen(),
                account.getKycVerified(),
                account.getOverdraftAllowed(),
                account.getVerificationLevel(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private String generateAccountNumber() {
        // Generate a 15-character account number to fit in VARCHAR(20)
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp).substring(7); // Last 6 digits
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
        return timestampStr + randomStr; // 6 + 9 = 15 characters
    }
}
