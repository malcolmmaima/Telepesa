package com.maelcolium.telepesa.models.request;

import com.maelcolium.telepesa.models.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "Loan product ID is required")
    private Long loanProductId;
    
    @NotNull(message = "Loan type is required")
    private LoanType loanType;
    
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.0", message = "Principal amount must be at least 1000")
    @DecimalMax(value = "10000000.0", message = "Principal amount cannot exceed 10,000,000")
    private BigDecimal principalAmount;
    
    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 360, message = "Term cannot exceed 360 months")
    private Integer termInMonths;
    
    @NotBlank(message = "Purpose is required")
    @Size(max = 500, message = "Purpose cannot exceed 500 characters")
    private String purpose;
    
    // Optional collateral information
    private String collateralDescription;
    
    @DecimalMin(value = "0.0", message = "Collateral value cannot be negative")
    private BigDecimal collateralValue;
    
    // Additional application details
    @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
    private String additionalNotes;
    
    private Boolean agreeToTerms = false;
}
