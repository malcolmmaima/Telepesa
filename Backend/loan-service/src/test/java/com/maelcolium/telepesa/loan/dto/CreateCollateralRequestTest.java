package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.CollateralType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateCollateralRequest Tests")
class CreateCollateralRequestTest {

    @Test
    @DisplayName("Should create CreateCollateralRequest with builder")
    void shouldCreateCreateCollateralRequestWithBuilder() {
        // Given
        LocalDate appraisalDate = LocalDate.now();
        LocalDate insuranceExpiryDate = LocalDate.now().plusDays(365);

        // When
        CreateCollateralRequest request = CreateCollateralRequest.builder()
                .loanId(1L)
                .ownerId(100L)
                .collateralType(CollateralType.REAL_ESTATE)
                .description("Residential property in Nairobi")
                .estimatedValue(new BigDecimal("5000000.00"))
                .appraisedValue(new BigDecimal("5500000.00"))
                .insuranceAmount(new BigDecimal("5000000.00"))
                .appraisalDate(appraisalDate)
                .insuranceExpiryDate(insuranceExpiryDate)
                .appraiserName("John Doe Appraisals")
                .insurancePolicyNumber("POL123456789")
                .documentReference("DOC123456789")
                .location("Nairobi, Kenya")
                .notes("Well-maintained property in prime location")
                .build();

        // Then
        assertThat(request.getLoanId()).isEqualTo(1L);
        assertThat(request.getOwnerId()).isEqualTo(100L);
        assertThat(request.getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
        assertThat(request.getDescription()).isEqualTo("Residential property in Nairobi");
        assertThat(request.getEstimatedValue()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(request.getAppraisedValue()).isEqualByComparingTo(new BigDecimal("5500000.00"));
        assertThat(request.getInsuranceAmount()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(request.getAppraisalDate()).isEqualTo(appraisalDate);
        assertThat(request.getInsuranceExpiryDate()).isEqualTo(insuranceExpiryDate);
        assertThat(request.getAppraiserName()).isEqualTo("John Doe Appraisals");
        assertThat(request.getInsurancePolicyNumber()).isEqualTo("POL123456789");
        assertThat(request.getDocumentReference()).isEqualTo("DOC123456789");
        assertThat(request.getLocation()).isEqualTo("Nairobi, Kenya");
        assertThat(request.getNotes()).isEqualTo("Well-maintained property in prime location");
    }

    @Test
    @DisplayName("Should create CreateCollateralRequest with no-args constructor and setters")
    void shouldCreateCreateCollateralRequestWithNoArgsConstructorAndSetters() {
        // Given
        CreateCollateralRequest request = new CreateCollateralRequest();
        LocalDate appraisalDate = LocalDate.now();
        LocalDate insuranceExpiryDate = LocalDate.now().plusDays(365);

        // When
        request.setLoanId(1L);
        request.setOwnerId(100L);
        request.setCollateralType(CollateralType.REAL_ESTATE);
        request.setDescription("Residential property in Nairobi");
        request.setEstimatedValue(new BigDecimal("5000000.00"));
        request.setAppraisedValue(new BigDecimal("5500000.00"));
        request.setInsuranceAmount(new BigDecimal("5000000.00"));
        request.setAppraisalDate(appraisalDate);
        request.setInsuranceExpiryDate(insuranceExpiryDate);
        request.setAppraiserName("John Doe Appraisals");
        request.setInsurancePolicyNumber("POL123456789");
        request.setDocumentReference("DOC123456789");
        request.setLocation("Nairobi, Kenya");
        request.setNotes("Well-maintained property in prime location");

        // Then
        assertThat(request.getLoanId()).isEqualTo(1L);
        assertThat(request.getOwnerId()).isEqualTo(100L);
        assertThat(request.getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
        assertThat(request.getDescription()).isEqualTo("Residential property in Nairobi");
        assertThat(request.getEstimatedValue()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(request.getAppraisedValue()).isEqualByComparingTo(new BigDecimal("5500000.00"));
        assertThat(request.getInsuranceAmount()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(request.getAppraisalDate()).isEqualTo(appraisalDate);
        assertThat(request.getInsuranceExpiryDate()).isEqualTo(insuranceExpiryDate);
        assertThat(request.getAppraiserName()).isEqualTo("John Doe Appraisals");
        assertThat(request.getInsurancePolicyNumber()).isEqualTo("POL123456789");
        assertThat(request.getDocumentReference()).isEqualTo("DOC123456789");
        assertThat(request.getLocation()).isEqualTo("Nairobi, Kenya");
        assertThat(request.getNotes()).isEqualTo("Well-maintained property in prime location");
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        LocalDate appraisalDate = LocalDate.now();
        LocalDate insuranceExpiryDate = LocalDate.now().plusDays(365);

        CreateCollateralRequest request1 = CreateCollateralRequest.builder()
                .loanId(1L)
                .ownerId(100L)
                .collateralType(CollateralType.REAL_ESTATE)
                .description("Residential property in Nairobi")
                .estimatedValue(new BigDecimal("5000000.00"))
                .appraisedValue(new BigDecimal("5500000.00"))
                .insuranceAmount(new BigDecimal("5000000.00"))
                .appraisalDate(appraisalDate)
                .insuranceExpiryDate(insuranceExpiryDate)
                .appraiserName("John Doe Appraisals")
                .insurancePolicyNumber("POL123456789")
                .documentReference("DOC123456789")
                .location("Nairobi, Kenya")
                .notes("Well-maintained property in prime location")
                .build();

        CreateCollateralRequest request2 = CreateCollateralRequest.builder()
                .loanId(1L)
                .ownerId(100L)
                .collateralType(CollateralType.REAL_ESTATE)
                .description("Residential property in Nairobi")
                .estimatedValue(new BigDecimal("5000000.00"))
                .appraisedValue(new BigDecimal("5500000.00"))
                .insuranceAmount(new BigDecimal("5000000.00"))
                .appraisalDate(appraisalDate)
                .insuranceExpiryDate(insuranceExpiryDate)
                .appraiserName("John Doe Appraisals")
                .insurancePolicyNumber("POL123456789")
                .documentReference("DOC123456789")
                .location("Nairobi, Kenya")
                .notes("Well-maintained property in prime location")
                .build();

        CreateCollateralRequest request3 = CreateCollateralRequest.builder()
                .loanId(2L)
                .ownerId(101L)
                .collateralType(CollateralType.VEHICLE)
                .description("Toyota Land Cruiser 2020")
                .estimatedValue(new BigDecimal("8000000.00"))
                .appraisedValue(new BigDecimal("8500000.00"))
                .insuranceAmount(new BigDecimal("8000000.00"))
                .appraisalDate(appraisalDate.plusDays(1))
                .insuranceExpiryDate(insuranceExpiryDate.plusDays(1))
                .appraiserName("Vehicle Appraisals Ltd")
                .insurancePolicyNumber("POL987654321")
                .documentReference("DOC987654321")
                .location("Mombasa, Kenya")
                .notes("Luxury vehicle in excellent condition")
                .build();

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1).isNotEqualTo(null);
        assertThat(request1).isNotEqualTo("string");

        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    @DisplayName("Should test edge cases with null values")
    void shouldTestEdgeCasesWithNullValues() {
        // Given
        CreateCollateralRequest request = new CreateCollateralRequest();

        // When & Then
        assertThat(request.getLoanId()).isNull();
        assertThat(request.getOwnerId()).isNull();
        assertThat(request.getCollateralType()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getEstimatedValue()).isNull();
        assertThat(request.getAppraisedValue()).isNull();
        assertThat(request.getInsuranceAmount()).isNull();
        assertThat(request.getAppraisalDate()).isNull();
        assertThat(request.getInsuranceExpiryDate()).isNull();
        assertThat(request.getAppraiserName()).isNull();
        assertThat(request.getInsurancePolicyNumber()).isNull();
        assertThat(request.getDocumentReference()).isNull();
        assertThat(request.getLocation()).isNull();
        assertThat(request.getNotes()).isNull();
    }

    @Test
    @DisplayName("Should test all collateral types")
    void shouldTestAllCollateralTypes() {
        // Given & When & Then
        for (CollateralType collateralType : CollateralType.values()) {
            CreateCollateralRequest request = CreateCollateralRequest.builder()
                    .collateralType(collateralType)
                    .build();
            
            assertThat(request.getCollateralType()).isEqualTo(collateralType);
        }
    }
} 