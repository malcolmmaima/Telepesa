package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CollateralDto Tests")
class CollateralDtoTest {

    @Test
    @DisplayName("Should create CollateralDto with builder")
    void shouldCreateCollateralDtoWithBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDate registrationDate = LocalDate.now();
        LocalDate appraisalDate = LocalDate.now().plusDays(1);
        LocalDate insuranceExpiryDate = LocalDate.now().plusDays(365);

        // When
        CollateralDto collateralDto = CollateralDto.builder()
                .id(1L)
                .collateralNumber("RE202412001234")
                .loanId(1L)
                .ownerId(100L)
                .collateralType(CollateralType.REAL_ESTATE)
                .status(CollateralStatus.ACTIVE)
                .description("Residential property in Nairobi")
                .estimatedValue(new BigDecimal("5000000.00"))
                .appraisedValue(new BigDecimal("5500000.00"))
                .insuranceAmount(new BigDecimal("5000000.00"))
                .registrationDate(registrationDate)
                .appraisalDate(appraisalDate)
                .insuranceExpiryDate(insuranceExpiryDate)
                .releaseDate(null)
                .releasedBy(null)
                .appraiserName("John Doe Appraisals")
                .insurancePolicyNumber("POL123456789")
                .documentReference("DOC123456789")
                .location("Nairobi, Kenya")
                .notes("Well-maintained property in prime location")
                .createdAt(now)
                .updatedAt(now)
                .currentValue(new BigDecimal("5500000.00"))
                .isActive(true)
                .isReleased(false)
                .isInsuranceExpired(false)
                .build();

        // Then
        assertThat(collateralDto.getId()).isEqualTo(1L);
        assertThat(collateralDto.getCollateralNumber()).isEqualTo("RE202412001234");
        assertThat(collateralDto.getLoanId()).isEqualTo(1L);
        assertThat(collateralDto.getOwnerId()).isEqualTo(100L);
        assertThat(collateralDto.getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
        assertThat(collateralDto.getStatus()).isEqualTo(CollateralStatus.ACTIVE);
        assertThat(collateralDto.getDescription()).isEqualTo("Residential property in Nairobi");
        assertThat(collateralDto.getEstimatedValue()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(collateralDto.getAppraisedValue()).isEqualByComparingTo(new BigDecimal("5500000.00"));
        assertThat(collateralDto.getInsuranceAmount()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(collateralDto.getRegistrationDate()).isEqualTo(registrationDate);
        assertThat(collateralDto.getAppraisalDate()).isEqualTo(appraisalDate);
        assertThat(collateralDto.getInsuranceExpiryDate()).isEqualTo(insuranceExpiryDate);
        assertThat(collateralDto.getReleaseDate()).isNull();
        assertThat(collateralDto.getReleasedBy()).isNull();
        assertThat(collateralDto.getAppraiserName()).isEqualTo("John Doe Appraisals");
        assertThat(collateralDto.getInsurancePolicyNumber()).isEqualTo("POL123456789");
        assertThat(collateralDto.getDocumentReference()).isEqualTo("DOC123456789");
        assertThat(collateralDto.getLocation()).isEqualTo("Nairobi, Kenya");
        assertThat(collateralDto.getNotes()).isEqualTo("Well-maintained property in prime location");
        assertThat(collateralDto.getCreatedAt()).isEqualTo(now);
        assertThat(collateralDto.getUpdatedAt()).isEqualTo(now);
        assertThat(collateralDto.getCurrentValue()).isEqualByComparingTo(new BigDecimal("5500000.00"));
        assertThat(collateralDto.getIsActive()).isTrue();
        assertThat(collateralDto.getIsReleased()).isFalse();
        assertThat(collateralDto.getIsInsuranceExpired()).isFalse();
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDate registrationDate = LocalDate.now();
        LocalDate appraisalDate = LocalDate.now().plusDays(1);
        LocalDate insuranceExpiryDate = LocalDate.now().plusDays(365);

        CollateralDto collateralDto1 = CollateralDto.builder()
                .id(1L)
                .collateralNumber("RE202412001234")
                .loanId(1L)
                .ownerId(100L)
                .collateralType(CollateralType.REAL_ESTATE)
                .status(CollateralStatus.ACTIVE)
                .description("Residential property in Nairobi")
                .estimatedValue(new BigDecimal("5000000.00"))
                .appraisedValue(new BigDecimal("5500000.00"))
                .insuranceAmount(new BigDecimal("5000000.00"))
                .registrationDate(registrationDate)
                .appraisalDate(appraisalDate)
                .insuranceExpiryDate(insuranceExpiryDate)
                .releaseDate(null)
                .releasedBy(null)
                .appraiserName("John Doe Appraisals")
                .insurancePolicyNumber("POL123456789")
                .documentReference("DOC123456789")
                .location("Nairobi, Kenya")
                .notes("Well-maintained property in prime location")
                .createdAt(now)
                .updatedAt(now)
                .currentValue(new BigDecimal("5500000.00"))
                .isActive(true)
                .isReleased(false)
                .isInsuranceExpired(false)
                .build();

        CollateralDto collateralDto2 = CollateralDto.builder()
                .id(1L)
                .collateralNumber("RE202412001234")
                .loanId(1L)
                .ownerId(100L)
                .collateralType(CollateralType.REAL_ESTATE)
                .status(CollateralStatus.ACTIVE)
                .description("Residential property in Nairobi")
                .estimatedValue(new BigDecimal("5000000.00"))
                .appraisedValue(new BigDecimal("5500000.00"))
                .insuranceAmount(new BigDecimal("5000000.00"))
                .registrationDate(registrationDate)
                .appraisalDate(appraisalDate)
                .insuranceExpiryDate(insuranceExpiryDate)
                .releaseDate(null)
                .releasedBy(null)
                .appraiserName("John Doe Appraisals")
                .insurancePolicyNumber("POL123456789")
                .documentReference("DOC123456789")
                .location("Nairobi, Kenya")
                .notes("Well-maintained property in prime location")
                .createdAt(now)
                .updatedAt(now)
                .currentValue(new BigDecimal("5500000.00"))
                .isActive(true)
                .isReleased(false)
                .isInsuranceExpired(false)
                .build();

        CollateralDto collateralDto3 = CollateralDto.builder()
                .id(2L)
                .collateralNumber("VE202412001235")
                .loanId(2L)
                .ownerId(101L)
                .collateralType(CollateralType.VEHICLE)
                .status(CollateralStatus.REGISTERED)
                .description("Toyota Land Cruiser 2020")
                .estimatedValue(new BigDecimal("8000000.00"))
                .appraisedValue(new BigDecimal("8500000.00"))
                .insuranceAmount(new BigDecimal("8000000.00"))
                .registrationDate(registrationDate)
                .appraisalDate(appraisalDate)
                .insuranceExpiryDate(insuranceExpiryDate)
                .releaseDate(null)
                .releasedBy(null)
                .appraiserName("Vehicle Appraisals Ltd")
                .insurancePolicyNumber("POL987654321")
                .documentReference("DOC987654321")
                .location("Mombasa, Kenya")
                .notes("Luxury vehicle in excellent condition")
                .createdAt(now)
                .updatedAt(now)
                .currentValue(new BigDecimal("8500000.00"))
                .isActive(true)
                .isReleased(false)
                .isInsuranceExpired(false)
                .build();

        // Then
        assertThat(collateralDto1).isEqualTo(collateralDto2);
        assertThat(collateralDto1).isNotEqualTo(collateralDto3);
        assertThat(collateralDto1).isNotEqualTo(null);
        assertThat(collateralDto1).isNotEqualTo("string");

        assertThat(collateralDto1.hashCode()).isEqualTo(collateralDto2.hashCode());
        assertThat(collateralDto1.hashCode()).isNotEqualTo(collateralDto3.hashCode());
    }

    @Test
    @DisplayName("Should test edge cases with null values")
    void shouldTestEdgeCasesWithNullValues() {
        // Given
        CollateralDto collateralDto = new CollateralDto();

        // When & Then
        assertThat(collateralDto.getId()).isNull();
        assertThat(collateralDto.getCollateralNumber()).isNull();
        assertThat(collateralDto.getLoanId()).isNull();
        assertThat(collateralDto.getOwnerId()).isNull();
        assertThat(collateralDto.getCollateralType()).isNull();
        assertThat(collateralDto.getStatus()).isNull();
        assertThat(collateralDto.getDescription()).isNull();
        assertThat(collateralDto.getEstimatedValue()).isNull();
        assertThat(collateralDto.getAppraisedValue()).isNull();
        assertThat(collateralDto.getInsuranceAmount()).isNull();
        assertThat(collateralDto.getRegistrationDate()).isNull();
        assertThat(collateralDto.getAppraisalDate()).isNull();
        assertThat(collateralDto.getInsuranceExpiryDate()).isNull();
        assertThat(collateralDto.getReleaseDate()).isNull();
        assertThat(collateralDto.getReleasedBy()).isNull();
        assertThat(collateralDto.getAppraiserName()).isNull();
        assertThat(collateralDto.getInsurancePolicyNumber()).isNull();
        assertThat(collateralDto.getDocumentReference()).isNull();
        assertThat(collateralDto.getLocation()).isNull();
        assertThat(collateralDto.getNotes()).isNull();
        assertThat(collateralDto.getCreatedAt()).isNull();
        assertThat(collateralDto.getUpdatedAt()).isNull();
        assertThat(collateralDto.getCurrentValue()).isNull();
        assertThat(collateralDto.getIsActive()).isNull();
        assertThat(collateralDto.getIsReleased()).isNull();
        assertThat(collateralDto.getIsInsuranceExpired()).isNull();
    }

    @Test
    @DisplayName("Should test all collateral types")
    void shouldTestAllCollateralTypes() {
        // Given & When & Then
        for (CollateralType collateralType : CollateralType.values()) {
            CollateralDto collateralDto = CollateralDto.builder()
                    .collateralType(collateralType)
                    .build();
            
            assertThat(collateralDto.getCollateralType()).isEqualTo(collateralType);
        }
    }

    @Test
    @DisplayName("Should test all collateral statuses")
    void shouldTestAllCollateralStatuses() {
        // Given & When & Then
        for (CollateralStatus status : CollateralStatus.values()) {
            CollateralDto collateralDto = CollateralDto.builder()
                    .status(status)
                    .build();
            
            assertThat(collateralDto.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("Should test boolean flags combinations")
    void shouldTestBooleanFlagsCombinations() {
        // Test active, not released, not expired
        CollateralDto activeCollateral = CollateralDto.builder()
                .isActive(true)
                .isReleased(false)
                .isInsuranceExpired(false)
                .build();
        
        assertThat(activeCollateral.getIsActive()).isTrue();
        assertThat(activeCollateral.getIsReleased()).isFalse();
        assertThat(activeCollateral.getIsInsuranceExpired()).isFalse();

        // Test inactive, released, not expired
        CollateralDto releasedCollateral = CollateralDto.builder()
                .isActive(false)
                .isReleased(true)
                .isInsuranceExpired(false)
                .build();
        
        assertThat(releasedCollateral.getIsActive()).isFalse();
        assertThat(releasedCollateral.getIsReleased()).isTrue();
        assertThat(releasedCollateral.getIsInsuranceExpired()).isFalse();

        // Test inactive, not released, expired
        CollateralDto expiredCollateral = CollateralDto.builder()
                .isActive(false)
                .isReleased(false)
                .isInsuranceExpired(true)
                .build();
        
        assertThat(expiredCollateral.getIsActive()).isFalse();
        assertThat(expiredCollateral.getIsReleased()).isFalse();
        assertThat(expiredCollateral.getIsInsuranceExpired()).isTrue();

        // Test all true
        CollateralDto allTrueCollateral = CollateralDto.builder()
                .isActive(true)
                .isReleased(true)
                .isInsuranceExpired(true)
                .build();
        
        assertThat(allTrueCollateral.getIsActive()).isTrue();
        assertThat(allTrueCollateral.getIsReleased()).isTrue();
        assertThat(allTrueCollateral.getIsInsuranceExpired()).isTrue();
    }
} 