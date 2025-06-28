package com.maelcolium.telepesa.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for account statistics and analytics.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account statistics and analytics")
public class AccountStatisticsDto {

    /**
     * Total number of accounts
     */
    @Schema(description = "Total number of accounts", example = "1500")
    private Long totalAccounts;

    /**
     * Number of active accounts
     */
    @Schema(description = "Number of active accounts", example = "1350")
    private Long activeAccounts;

    /**
     * Number of inactive accounts
     */
    @Schema(description = "Number of inactive accounts", example = "100")
    private Long inactiveAccounts;

    /**
     * Number of frozen accounts
     */
    @Schema(description = "Number of frozen accounts", example = "25")
    private Long frozenAccounts;

    /**
     * Number of pending accounts
     */
    @Schema(description = "Number of pending accounts", example = "50")
    private Long pendingAccounts;

    /**
     * Number of closed accounts
     */
    @Schema(description = "Number of closed accounts", example = "25")
    private Long closedAccounts;

    /**
     * Total balance across all accounts
     */
    @Schema(description = "Total balance across all accounts", example = "50000000.00")
    private BigDecimal totalBalance;

    /**
     * Average account balance
     */
    @Schema(description = "Average account balance", example = "33333.33")
    private BigDecimal averageBalance;

    /**
     * Account counts by type
     */
    @Schema(description = "Account counts by type")
    private Map<String, Long> accountsByType;

    /**
     * Balance distribution by account type
     */
    @Schema(description = "Balance distribution by account type")
    private Map<String, BigDecimal> balancesByType;

    /**
     * Number of accounts below minimum balance
     */
    @Schema(description = "Number of accounts below minimum balance", example = "45")
    private Long accountsBelowMinBalance;

    /**
     * Number of dormant accounts
     */
    @Schema(description = "Number of dormant accounts", example = "75")
    private Long dormantAccounts;

    /**
     * Number of accounts with overdraft
     */
    @Schema(description = "Number of accounts with overdraft facility", example = "200")
    private Long accountsWithOverdraft;
} 