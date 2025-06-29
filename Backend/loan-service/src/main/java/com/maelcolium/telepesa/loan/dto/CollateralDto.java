package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for collateral information
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Collateral information")
public class CollateralDto {

    @Schema(description = "Collateral ID", example = "1")
    private Long id;

    @Schema(description = "Unique collateral number", example = "COL202412001234")
    private String collateralNumber;

    @Schema(description = "Associated loan ID", example = "100")
    private Long loanId;

    @Schema(description = "Collateral owner ID", example = "200")
    private Long ownerId;

    @Schema(description = "Type of collateral", example = "REAL_ESTATE")
    private CollateralType collateralType;

    @Schema(description = "Current status of collateral", example = "ACTIVE")
    private CollateralStatus status;

    @Schema(description = "Description of the collateral", example = "Residential property in Nairobi")
    private String description;

    @Schema(description = "Estimated value of collateral", example = "5000000.00")
    private BigDecimal estimatedValue;

    @Schema(description = "Appraised value of collateral", example = "4800000.00")
    private BigDecimal appraisedValue;

    @Schema(description = "Date of appraisal", example = "2024-01-15")
    private LocalDate appraisalDate;

    @Schema(description = "Name of the appraiser", example = "John Doe Appraisals")
    private String appraiserName;

    @Schema(description = "Location of the collateral", example = "Westlands, Nairobi")
    private String location;

    @Schema(description = "Document reference number", example = "DOC123456")
    private String documentReference;

    @Schema(description = "Insurance policy number", example = "INS789012")
    private String insurancePolicyNumber;

    @Schema(description = "Insurance expiry date", example = "2025-01-15")
    private LocalDate insuranceExpiryDate;

    @Schema(description = "Insurance coverage amount", example = "5000000.00")
    private BigDecimal insuranceAmount;

    @Schema(description = "Additional notes", example = "Property is in good condition")
    private String notes;

    @Schema(description = "Registration date", example = "2024-01-10")
    private LocalDate registrationDate;

    @Schema(description = "Release date", example = "2026-01-10")
    private LocalDate releaseDate;

    @Schema(description = "User ID who released the collateral", example = "300")
    private Long releasedBy;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Current value (appraised or estimated)", example = "4800000.00")
    private BigDecimal currentValue;

    @Schema(description = "Whether collateral is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether collateral is released", example = "false")
    private Boolean isReleased;

    @Schema(description = "Whether insurance is expired", example = "false")
    private Boolean isInsuranceExpired;
} 