package com.maelcolium.telepesa.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for account balance information.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account balance information")
public class AccountBalanceDto {

    /**
     * Account number
     */
    @Schema(description = "Account number", example = "ACC001234567890")
    private String accountNumber;

    /**
     * Current balance
     */
    @Schema(description = "Current account balance", example = "15000.00")
    private BigDecimal balance;

    /**
     * Available balance
     */
    @Schema(description = "Available balance for transactions", example = "14500.00")
    private BigDecimal availableBalance;

    /**
     * Minimum balance required
     */
    @Schema(description = "Minimum balance required", example = "1000.00")
    private BigDecimal minimumBalance;

    /**
     * Currency code
     */
    @Schema(description = "Currency code", example = "KES")
    private String currencyCode;

    /**
     * Last transaction date
     */
    @Schema(description = "Last transaction date", example = "2024-06-27T14:30:00")
    private LocalDateTime lastTransactionDate;

    /**
     * Whether account is active
     */
    @Schema(description = "Whether account is active", example = "true")
    private Boolean isActive;
} 