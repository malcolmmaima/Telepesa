package com.maelcolium.telepesa.account.dto;

import com.maelcolium.telepesa.models.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new account.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new bank account")
public class CreateAccountRequest {

    /**
     * Type of account to create
     */
    @Schema(description = "Type of account", example = "SAVINGS", required = true)
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    /**
     * User ID who will own this account
     */
    @Schema(description = "User ID who will own this account", example = "1", required = true)
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    /**
     * Currency code for the account
     */
    @Schema(description = "Currency code", example = "KES", required = true)
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    @Builder.Default
    private String currencyCode = "KES";

    /**
     * Initial deposit amount
     */
    @Schema(description = "Initial deposit amount", example = "1000.00")
    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    @Builder.Default
    private BigDecimal initialDeposit = BigDecimal.ZERO;

    /**
     * Account nickname or alias
     */
    @Schema(description = "Account nickname or alias", example = "My Savings Account")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    /**
     * Account description
     */
    @Schema(description = "Account description", example = "Primary savings account for daily use")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /**
     * Whether overdraft is allowed (for checking accounts)
     */
    @Schema(description = "Whether overdraft is allowed", example = "false")
    @Builder.Default
    private Boolean overdraftAllowed = false;

    /**
     * Overdraft limit if overdraft is allowed
     */
    @Schema(description = "Overdraft limit if overdraft is allowed", example = "5000.00")
    @DecimalMin(value = "0.00", message = "Overdraft limit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal overdraftLimit;

    /**
     * Daily transaction limit
     */
    @Schema(description = "Daily transaction limit", example = "50000.00")
    @DecimalMin(value = "0.00", message = "Daily limit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal dailyLimit;

    /**
     * Monthly transaction limit
     */
    @Schema(description = "Monthly transaction limit", example = "500000.00")
    @DecimalMin(value = "0.00", message = "Monthly limit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal monthlyLimit;

    /**
     * Interest rate for savings accounts
     */
    @Schema(description = "Interest rate for savings accounts (percentage)", example = "3.5")
    @DecimalMin(value = "0.0000", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0000", message = "Interest rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 4, message = "Invalid interest rate format")
    private BigDecimal interestRate;
} 