package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.PaymentStatus;
import com.maelcolium.telepesa.models.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for loan payment information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Loan payment information")
public class LoanPaymentDto {

    @Schema(description = "Payment ID", example = "1")
    private Long id;

    @Schema(description = "Loan ID", example = "1")
    private Long loanId;

    @Schema(description = "Payment reference number", example = "PAY202412001")
    private String paymentReference;

    @Schema(description = "Payment amount", example = "2347.50")
    private BigDecimal amount;

    @Schema(description = "Principal portion", example = "2000.00")
    private BigDecimal principalAmount;

    @Schema(description = "Interest portion", example = "347.50")
    private BigDecimal interestAmount;

    @Schema(description = "Payment date", example = "2024-01-15")
    private LocalDate paymentDate;

    @Schema(description = "Due date", example = "2024-01-15")
    private LocalDate dueDate;

    @Schema(description = "Payment status", example = "COMPLETED")
    private PaymentStatus status;

    @Schema(description = "Payment type", example = "REGULAR")
    private PaymentType paymentType;

    @Schema(description = "Payment method", example = "BANK_TRANSFER")
    private String paymentMethod;

    @Schema(description = "Transaction ID", example = "TXN123456789")
    private String transactionId;

    @Schema(description = "Late fee amount", example = "0.00")
    private BigDecimal lateFee;

    @Schema(description = "Penalty amount", example = "0.00")
    private BigDecimal penaltyAmount;

    @Schema(description = "Payment notes", example = "On-time payment")
    private String notes;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Total payment amount including fees", example = "2347.50")
    public BigDecimal getTotalAmount() {
        BigDecimal total = amount != null ? amount : BigDecimal.ZERO;
        if (lateFee != null) total = total.add(lateFee);
        if (penaltyAmount != null) total = total.add(penaltyAmount);
        return total;
    }

    @Schema(description = "Whether payment was made late", example = "false")
    public boolean isLate() {
        return paymentDate != null && dueDate != null && paymentDate.isAfter(dueDate);
    }
}
