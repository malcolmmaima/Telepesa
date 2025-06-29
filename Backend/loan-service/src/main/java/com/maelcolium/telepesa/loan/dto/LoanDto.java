package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for loan information
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Loan information response object")
public class LoanDto {

    @Schema(description = "Loan ID", example = "1")
    private Long id;

    @Schema(description = "Unique loan number", example = "LN202412001")
    private String loanNumber;

    @Schema(description = "User ID", example = "100")
    private Long userId;

    @Schema(description = "Account number", example = "ACC001234567890")
    private String accountNumber;

    @Schema(description = "Type of loan", example = "PERSONAL")
    private LoanType loanType;

    @Schema(description = "Current status of the loan", example = "ACTIVE")
    private LoanStatus status;

    @Schema(description = "Principal loan amount", example = "50000.00")
    private BigDecimal principalAmount;

    @Schema(description = "Interest rate (annual)", example = "12.5000")
    private BigDecimal interestRate;

    @Schema(description = "Loan term in months", example = "24")
    private Integer termMonths;

    @Schema(description = "Monthly payment amount", example = "2347.50")
    private BigDecimal monthlyPayment;

    @Schema(description = "Outstanding balance", example = "35000.00")
    private BigDecimal outstandingBalance;

    @Schema(description = "Total amount paid", example = "15000.00")
    private BigDecimal totalPaid;

    @Schema(description = "Next payment due date", example = "2024-01-15")
    private LocalDate nextPaymentDate;

    @Schema(description = "Loan application date", example = "2024-01-01")
    private LocalDate applicationDate;

    @Schema(description = "Loan approval date", example = "2024-01-03")
    private LocalDate approvalDate;

    @Schema(description = "Loan disbursement date", example = "2024-01-05")
    private LocalDate disbursementDate;

    @Schema(description = "ID of approving officer", example = "200")
    private Long approvedBy;

    @Schema(description = "Credit score at application", example = "750")
    private Integer creditScore;

    @Schema(description = "Purpose of the loan", example = "Business expansion")
    private String purpose;

    @Schema(description = "Monthly income", example = "80000.00")
    private BigDecimal monthlyIncome;

    @Schema(description = "Additional notes", example = "Good payment history")
    private String notes;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T14:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Total repayment amount", example = "56340.00")
    public BigDecimal getTotalRepaymentAmount() {
        return monthlyPayment != null && termMonths != null ? 
            monthlyPayment.multiply(BigDecimal.valueOf(termMonths)) : BigDecimal.ZERO;
    }

    @Schema(description = "Total interest amount", example = "6340.00")
    public BigDecimal getTotalInterestAmount() {
        return getTotalRepaymentAmount().subtract(principalAmount != null ? principalAmount : BigDecimal.ZERO);
    }

    @Schema(description = "Whether loan is overdue", example = "false")
    public boolean isOverdue() {
        return nextPaymentDate != null && nextPaymentDate.isBefore(LocalDate.now());
    }
}
