package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.CollateralType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new collateral
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Collateral creation request")
public class CreateCollateralRequest {

    @NotNull(message = "Loan ID is required")
    @Schema(description = "Associated loan ID", example = "100", required = true)
    private Long loanId;

    @NotNull(message = "Owner ID is required")
    @Schema(description = "Collateral owner ID", example = "200", required = true)
    private Long ownerId;

    @NotNull(message = "Collateral type is required")
    @Schema(description = "Type of collateral", example = "REAL_ESTATE", required = true)
    private CollateralType collateralType;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Description of the collateral", example = "Residential property in Nairobi", required = true)
    private String description;

    @NotNull(message = "Estimated value is required")
    @DecimalMin(value = "1000.00", message = "Estimated value must be at least 1000")
    @DecimalMax(value = "1000000000.00", message = "Estimated value must not exceed 1,000,000,000")
    @Digits(integer = 15, fraction = 2, message = "Invalid estimated value format")
    @Schema(description = "Estimated value of collateral", example = "5000000.00", required = true)
    private BigDecimal estimatedValue;

    @DecimalMin(value = "1000.00", message = "Appraised value must be at least 1000")
    @DecimalMax(value = "1000000000.00", message = "Appraised value must not exceed 1,000,000,000")
    @Digits(integer = 15, fraction = 2, message = "Invalid appraised value format")
    @Schema(description = "Appraised value of collateral", example = "4800000.00")
    private BigDecimal appraisedValue;

    @PastOrPresent(message = "Appraisal date cannot be in the future")
    @Schema(description = "Date of appraisal", example = "2024-01-15")
    private LocalDate appraisalDate;

    @Size(max = 100, message = "Appraiser name must not exceed 100 characters")
    @Schema(description = "Name of the appraiser", example = "John Doe Appraisals")
    private String appraiserName;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Location of the collateral", example = "Westlands, Nairobi")
    private String location;

    @Size(max = 100, message = "Document reference must not exceed 100 characters")
    @Schema(description = "Document reference number", example = "DOC123456")
    private String documentReference;

    @Size(max = 50, message = "Insurance policy number must not exceed 50 characters")
    @Schema(description = "Insurance policy number", example = "INS789012")
    private String insurancePolicyNumber;

    @Future(message = "Insurance expiry date must be in the future")
    @Schema(description = "Insurance expiry date", example = "2025-01-15")
    private LocalDate insuranceExpiryDate;

    @DecimalMin(value = "0.00", message = "Insurance amount must be non-negative")
    @DecimalMax(value = "1000000000.00", message = "Insurance amount must not exceed 1,000,000,000")
    @Digits(integer = 15, fraction = 2, message = "Invalid insurance amount format")
    @Schema(description = "Insurance coverage amount", example = "5000000.00")
    private BigDecimal insuranceAmount;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Property is in good condition")
    private String notes;
} 