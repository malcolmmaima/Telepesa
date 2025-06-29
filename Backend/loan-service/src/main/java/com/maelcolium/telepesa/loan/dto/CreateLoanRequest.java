package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a new loan application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Loan application request")
public class CreateLoanRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID", example = "100", required = true)
    private Long userId;

    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    @Schema(description = "Account number for loan disbursement", example = "ACC001234567890", required = true)
    private String accountNumber;

    @NotNull(message = "Loan type is required")
    @Schema(description = "Type of loan", example = "PERSONAL", required = true)
    private LoanType loanType;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Principal amount must be at least 1000")
    @DecimalMax(value = "10000000.00", message = "Principal amount must not exceed 10,000,000")
    @Digits(integer = 13, fraction = 2, message = "Invalid principal amount format")
    @Schema(description = "Principal loan amount", example = "50000.00", required = true)
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0001", message = "Interest rate must be greater than 0")
    @DecimalMax(value = "100.0000", message = "Interest rate must not exceed 100%")
    @Digits(integer = 3, fraction = 4, message = "Invalid interest rate format")
    @Schema(description = "Annual interest rate", example = "12.5000", required = true)
    private BigDecimal interestRate;

    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 360, message = "Term must not exceed 360 months")
    @Schema(description = "Loan term in months", example = "24", required = true)
    private Integer termMonths;

    @Size(max = 500, message = "Purpose must not exceed 500 characters")
    @Schema(description = "Purpose of the loan", example = "Business expansion")
    private String purpose;

    @DecimalMin(value = "0.00", message = "Monthly income must be non-negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid monthly income format")
    @Schema(description = "Applicant's monthly income", example = "80000.00")
    private BigDecimal monthlyIncome;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Existing customer with good credit history")
    private String notes;
}
