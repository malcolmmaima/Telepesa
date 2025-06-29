package com.maelcolium.telepesa.loan.model;

import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
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

/**
 * Entity representing collateral for loans in the Telepesa system.
 */
@Entity
@Table(name = "collaterals", indexes = {
    @Index(name = "idx_collateral_loan_id", columnList = "loan_id"),
    @Index(name = "idx_collateral_status", columnList = "status"),
    @Index(name = "idx_collateral_type", columnList = "collateral_type"),
    @Index(name = "idx_collateral_owner", columnList = "owner_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Collateral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collateral_number", unique = true, nullable = false, length = 50)
    private String collateralNumber;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "collateral_type", nullable = false, length = 30)
    private CollateralType collateralType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CollateralStatus status;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "estimated_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "appraised_value", precision = 15, scale = 2)
    private BigDecimal appraisedValue;

    @Column(name = "appraisal_date")
    private LocalDate appraisalDate;

    @Column(name = "appraiser_name", length = 100)
    private String appraiserName;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "document_reference", length = 100)
    private String documentReference;

    @Column(name = "insurance_policy_number", length = 50)
    private String insurancePolicyNumber;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "insurance_amount", precision = 15, scale = 2)
    private BigDecimal insuranceAmount;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "released_by")
    private Long releasedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", insertable = false, updatable = false)
    private Loan loan;

    public boolean isActive() {
        return status == CollateralStatus.ACTIVE || status == CollateralStatus.REGISTERED;
    }

    public boolean isReleased() {
        return status == CollateralStatus.RELEASED;
    }

    public boolean isInsuranceExpired() {
        return insuranceExpiryDate != null && insuranceExpiryDate.isBefore(LocalDate.now());
    }

    public BigDecimal getCurrentValue() {
        return appraisedValue != null ? appraisedValue : estimatedValue;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
        if (status == null) {
            status = CollateralStatus.REGISTERED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 