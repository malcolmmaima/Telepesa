package com.maelcolium.telepesa.account.dto;

import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Account information.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account information response object")
public class AccountDto {

    /**
     * Account ID
     */
    @Schema(description = "Unique account identifier", example = "1")
    private Long id;

    /**
     * Unique account number
     */
    @Schema(description = "Unique account number", example = "ACC001234567890")
    private String accountNumber;

    /**
     * User ID who owns this account
     */
    @Schema(description = "User ID who owns this account", example = "1")
    private Long userId;

    /**
     * Type of account
     */
    @Schema(description = "Type of account", example = "SAVINGS")
    private AccountType accountType;

    /**
     * Current account status
     */
    @Schema(description = "Current account status", example = "ACTIVE")
    private AccountStatus status;

    /**
     * Current account balance
     */
    @Schema(description = "Current account balance", example = "15000.00")
    private BigDecimal balance;

    /**
     * Available balance for transactions
     */
    @Schema(description = "Available balance for transactions", example = "14500.00")
    private BigDecimal availableBalance;

    /**
     * Minimum balance required
     */
    @Schema(description = "Minimum balance required", example = "1000.00")
    private BigDecimal minimumBalance;

    /**
     * Daily transaction limit
     */
    @Schema(description = "Daily transaction limit", example = "50000.00")
    private BigDecimal dailyLimit;

    /**
     * Monthly transaction limit
     */
    @Schema(description = "Monthly transaction limit", example = "500000.00")
    private BigDecimal monthlyLimit;

    /**
     * Currency code
     */
    @Schema(description = "Currency code", example = "KES")
    private String currencyCode;

    /**
     * Account nickname or alias
     */
    @Schema(description = "Account nickname or alias", example = "My Savings Account")
    private String accountName;

    /**
     * Account description
     */
    @Schema(description = "Account description", example = "Primary savings account")
    private String description;

    /**
     * Interest rate for savings accounts
     */
    @Schema(description = "Interest rate (percentage)", example = "3.50")
    private BigDecimal interestRate;

    /**
     * Whether the account is frozen
     */
    @Schema(description = "Whether the account is frozen", example = "false")
    private Boolean isFrozen;

    /**
     * Whether overdraft is allowed
     */
    @Schema(description = "Whether overdraft is allowed", example = "false")
    private Boolean overdraftAllowed;

    /**
     * Overdraft limit if allowed
     */
    @Schema(description = "Overdraft limit if allowed", example = "5000.00")
    private BigDecimal overdraftLimit;

    /**
     * Last transaction date
     */
    @Schema(description = "Last transaction date", example = "2024-06-27T14:30:00")
    private LocalDateTime lastTransactionDate;

    /**
     * Account activation date
     */
    @Schema(description = "Account activation date", example = "2024-06-01T10:00:00")
    private LocalDateTime activatedAt;

    /**
     * Account closure date
     */
    @Schema(description = "Account closure date", example = "null")
    private LocalDateTime closedAt;

    /**
     * Account creation timestamp
     */
    @Schema(description = "Account creation timestamp", example = "2024-06-01T09:30:00")
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Schema(description = "Last update timestamp", example = "2024-06-27T14:30:00")
    private LocalDateTime updatedAt;

    /**
     * Computed property indicating if account is active
     */
    @Schema(description = "Whether the account is active and can perform transactions", example = "true")
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status) && !Boolean.TRUE.equals(this.isFrozen);
    }
} 