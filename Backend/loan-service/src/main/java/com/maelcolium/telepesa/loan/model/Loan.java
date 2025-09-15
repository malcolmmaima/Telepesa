package com.maelcolium.telepesa.loan.model;

import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a loan in the Telepesa system.
 */
@Entity
@Table(name = "loans", indexes = {
    @Index(name = "idx_loan_user_id", columnList = "user_id"),
    @Index(name = "idx_loan_status", columnList = "status"),
    @Index(name = "idx_loan_type", columnList = "loan_type"),
    @Index(name = "idx_loan_account_number", columnList = "account_number")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_number", unique = true, nullable = false, length = 50)
    private String loanNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 20)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "monthly_payment", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "outstanding_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(name = "total_paid", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Column(name = "purpose", length = 500)
    private String purpose;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LoanPayment> payments = new ArrayList<>();

    public BigDecimal getTotalRepaymentAmount() {
        return monthlyPayment.multiply(BigDecimal.valueOf(termMonths));
    }

    public BigDecimal getTotalInterestAmount() {
        return getTotalRepaymentAmount().subtract(principalAmount);
    }

    public boolean isOverdue() {
        return nextPaymentDate != null && nextPaymentDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE || status == LoanStatus.CURRENT;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (applicationDate == null) {
            applicationDate = LocalDate.now();
        }
        if (status == null) {
            status = LoanStatus.PENDING;
        }
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
