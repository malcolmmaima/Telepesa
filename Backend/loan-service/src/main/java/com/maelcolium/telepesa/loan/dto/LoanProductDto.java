package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanProductDto {
    private Long id;
    private String name;
    private LoanType loanType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal minInterestRate;
    private BigDecimal maxInterestRate;
    private Integer minTermMonths;
    private Integer maxTermMonths;
    private String description;
    private List<String> requirements;
    private List<String> features;
    private boolean isActive;
    private String currency;
}
