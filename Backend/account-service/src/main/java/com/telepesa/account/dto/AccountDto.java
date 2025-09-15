package com.telepesa.account.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private Long userId;
    private String accountType;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal minimumBalance;
    private String currencyCode;
    private String status;
    private Boolean isFrozen;
    private Boolean kycVerified;
    private Boolean overdraftAllowed;
    private Integer verificationLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
