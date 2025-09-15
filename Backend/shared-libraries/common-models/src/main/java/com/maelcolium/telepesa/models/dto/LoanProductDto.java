package com.maelcolium.telepesa.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maelcolium.telepesa.models.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanProductDto {
    private Long id;
    private String name;
    private String description;
    private LoanType loanType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private BigDecimal minInterestRate;
    private BigDecimal maxInterestRate;
    private Integer minTermMonths;
    private Integer maxTermMonths;
    private BigDecimal processingFee;
    private BigDecimal processingFeePercentage;
    private Boolean requiresCollateral;
    private BigDecimal maxLtvRatio; // Loan-to-Value ratio
    private String eligibilityCriteria;
    private String requiredDocuments;
    private List<String> requirements;
    private List<String> features;
    private String currency;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Calculated fields
    private BigDecimal effectiveRate;
    private String termRange;
    private String amountRange;
}
