package com.maelcolium.telepesa.account.service;

import com.maelcolium.telepesa.account.repository.AccountRepository;
import com.maelcolium.telepesa.models.enums.AccountType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating unique account numbers.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class AccountNumberService {

    private final AccountRepository accountRepository;
    private final SecureRandom random;
    
    // Account type prefixes
    private static final String SAVINGS_PREFIX = "SAV";
    private static final String CHECKING_PREFIX = "CHK";
    private static final String BUSINESS_PREFIX = "BUS";
    private static final String FIXED_DEPOSIT_PREFIX = "FD";
    private static final String DEFAULT_PREFIX = "ACC";

    public AccountNumberService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.random = new SecureRandom();
    }

    /**
     * Generate a unique account number based on account type
     */
    public String generateAccountNumber(AccountType accountType) {
        String prefix = getAccountTypePrefix(accountType);
        String accountNumber;
        int attempts = 0;
        final int maxAttempts = 100;

        do {
            accountNumber = generateAccountNumberWithPrefix(prefix);
            attempts++;
            
            if (attempts >= maxAttempts) {
                log.error("Failed to generate unique account number after {} attempts", maxAttempts);
                throw new RuntimeException("Unable to generate unique account number");
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));

        log.info("Generated unique account number: {} for account type: {} in {} attempts", 
                accountNumber, accountType, attempts);
        
        return accountNumber;
    }

    /**
     * Get account type prefix
     */
    private String getAccountTypePrefix(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> SAVINGS_PREFIX;
            case CHECKING -> CHECKING_PREFIX;
            case BUSINESS -> BUSINESS_PREFIX;
            case FIXED_DEPOSIT -> FIXED_DEPOSIT_PREFIX;
            default -> DEFAULT_PREFIX;
        };
    }

    /**
     * Generate account number with given prefix
     * Format: PREFIX + YYYYMM + 8_DIGIT_RANDOM_NUMBER
     * Example: SAV20240610123456
     */
    private String generateAccountNumberWithPrefix(String prefix) {
        // Get current year and month
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        // Generate 8-digit random number
        int randomNumber = 10000000 + random.nextInt(90000000);
        
        return prefix + yearMonth + randomNumber;
    }

    /**
     * Validate account number format
     */
    public boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 15 || accountNumber.length() > 20) {
            return false;
        }

        // Check if it starts with known prefix
        return accountNumber.startsWith(SAVINGS_PREFIX) ||
               accountNumber.startsWith(CHECKING_PREFIX) ||
               accountNumber.startsWith(BUSINESS_PREFIX) ||
               accountNumber.startsWith(FIXED_DEPOSIT_PREFIX) ||
               accountNumber.startsWith(DEFAULT_PREFIX);
    }

    /**
     * Extract account type from account number
     */
    public AccountType getAccountTypeFromNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }

        if (accountNumber.startsWith(SAVINGS_PREFIX)) {
            return AccountType.SAVINGS;
        } else if (accountNumber.startsWith(CHECKING_PREFIX)) {
            return AccountType.CHECKING;
        } else if (accountNumber.startsWith(BUSINESS_PREFIX)) {
            return AccountType.BUSINESS;
        } else if (accountNumber.startsWith(FIXED_DEPOSIT_PREFIX)) {
            return AccountType.FIXED_DEPOSIT;
        }

        return null;
    }

    /**
     * Generate multiple unique account numbers (for testing purposes)
     */
    public String[] generateMultipleAccountNumbers(AccountType accountType, int count) {
        String[] accountNumbers = new String[count];
        
        for (int i = 0; i < count; i++) {
            accountNumbers[i] = generateAccountNumber(accountType);
        }
        
        return accountNumbers;
    }
} 