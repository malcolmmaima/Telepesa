package com.maelcolium.telepesa.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating account details.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating account details")
public class UpdateAccountRequest {

    /**
     * Account nickname or alias
     */
    @Schema(description = "Account nickname or alias", example = "Updated Savings Account")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    /**
     * Account description
     */
    @Schema(description = "Account description", example = "Updated primary savings account")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /**
     * Daily transaction limit
     */
    @Schema(description = "Daily transaction limit", example = "75000.00")
    @DecimalMin(value = "0.00", message = "Daily limit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal dailyLimit;

    /**
     * Monthly transaction limit
     */
    @Schema(description = "Monthly transaction limit", example = "750000.00")
    @DecimalMin(value = "0.00", message = "Monthly limit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal monthlyLimit;

    /**
     * Whether overdraft is allowed
     */
    @Schema(description = "Whether overdraft is allowed", example = "true")
    private Boolean overdraftAllowed;

    /**
     * Overdraft limit if overdraft is allowed
     */
    @Schema(description = "Overdraft limit if overdraft is allowed", example = "10000.00")
    @DecimalMin(value = "0.00", message = "Overdraft limit cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal overdraftLimit;

    /**
     * Interest rate for savings accounts
     */
    @Schema(description = "Interest rate for savings accounts (percentage)", example = "4.0")
    @DecimalMin(value = "0.0000", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0000", message = "Interest rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 4, message = "Invalid interest rate format")
    private BigDecimal interestRate;
} 