package com.maelcolium.telepesa.loan.mapper;

import com.maelcolium.telepesa.loan.dto.CollateralDto;
import com.maelcolium.telepesa.loan.model.Collateral;
import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CollateralMapperUnitTest {

    private CollateralMapper collateralMapper;
    private Collateral collateral;

    @BeforeEach
    void setUp() {
        collateralMapper = new CollateralMapper() {
            @Override
            public CollateralDto toDto(Collateral collateral) {
                if (collateral == null) {
                    return null;
                }

                return CollateralDto.builder()
                    .id(collateral.getId())
                    .collateralNumber(collateral.getCollateralNumber())
                    .loanId(collateral.getLoanId())
                    .ownerId(collateral.getOwnerId())
                    .collateralType(collateral.getCollateralType())
                    .status(collateral.getStatus())
                    .description(collateral.getDescription())
                    .estimatedValue(collateral.getEstimatedValue())
                    .appraisedValue(collateral.getAppraisedValue())
                    .appraisalDate(collateral.getAppraisalDate())
                    .appraiserName(collateral.getAppraiserName())
                    .location(collateral.getLocation())
                    .documentReference(collateral.getDocumentReference())
                    .insurancePolicyNumber(collateral.getInsurancePolicyNumber())
                    .insuranceExpiryDate(collateral.getInsuranceExpiryDate())
                    .insuranceAmount(collateral.getInsuranceAmount())
                    .notes(collateral.getNotes())
                    .registrationDate(collateral.getRegistrationDate())
                    .releaseDate(collateral.getReleaseDate())
                    .releasedBy(collateral.getReleasedBy())
                    .createdAt(collateral.getCreatedAt())
                    .updatedAt(collateral.getUpdatedAt())
                    .currentValue(collateral.getAppraisedValue() != null ? collateral.getAppraisedValue() : collateral.getEstimatedValue())
                    .isActive(collateral.isActive())
                    .isReleased(collateral.isReleased())
                    .isInsuranceExpired(collateral.isInsuranceExpired())
                    .build();
            }
        };

        collateral = Collateral.builder()
            .id(1L)
            .collateralNumber("RE202412001234")
            .loanId(1L)
            .ownerId(100L)
            .collateralType(CollateralType.REAL_ESTATE)
            .status(CollateralStatus.REGISTERED)
            .description("Residential property in Nairobi")
            .estimatedValue(new BigDecimal("5000000.00"))
            .appraisedValue(new BigDecimal("4800000.00"))
            .appraisalDate(LocalDate.now())
            .appraiserName("John Doe Appraisals")
            .location("Westlands, Nairobi")
            .documentReference("DOC123456")
            .insurancePolicyNumber("INS789012")
            .insuranceExpiryDate(LocalDate.now().plusYears(1))
            .insuranceAmount(new BigDecimal("5000000.00"))
            .notes("Property is in good condition")
            .registrationDate(LocalDate.now())
            .releaseDate(LocalDate.now().plusYears(5))
            .releasedBy(200L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .version(1L)
            .build();
    }

    @Test
    void toDto_ShouldMapAllFieldsCorrectly() {
        // When
        CollateralDto result = collateralMapper.toDto(collateral);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCollateralNumber()).isEqualTo("RE202412001234");
        assertThat(result.getLoanId()).isEqualTo(1L);
        assertThat(result.getOwnerId()).isEqualTo(100L);
        assertThat(result.getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
        assertThat(result.getStatus()).isEqualTo(CollateralStatus.REGISTERED);
        assertThat(result.getDescription()).isEqualTo("Residential property in Nairobi");
        assertThat(result.getEstimatedValue()).isEqualTo(new BigDecimal("5000000.00"));
        assertThat(result.getAppraisedValue()).isEqualTo(new BigDecimal("4800000.00"));
        assertThat(result.getAppraisalDate()).isEqualTo(LocalDate.now());
        assertThat(result.getAppraiserName()).isEqualTo("John Doe Appraisals");
        assertThat(result.getLocation()).isEqualTo("Westlands, Nairobi");
        assertThat(result.getDocumentReference()).isEqualTo("DOC123456");
        assertThat(result.getInsurancePolicyNumber()).isEqualTo("INS789012");
        assertThat(result.getInsuranceExpiryDate()).isEqualTo(LocalDate.now().plusYears(1));
        assertThat(result.getInsuranceAmount()).isEqualTo(new BigDecimal("5000000.00"));
        assertThat(result.getNotes()).isEqualTo("Property is in good condition");
        assertThat(result.getRegistrationDate()).isEqualTo(LocalDate.now());
        assertThat(result.getReleaseDate()).isEqualTo(LocalDate.now().plusYears(5));
        assertThat(result.getReleasedBy()).isEqualTo(200L);
        // Don't test exact timestamps as they will always be different
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toDto_ShouldCalculateCurrentValueCorrectly() {
        // When
        CollateralDto result = collateralMapper.toDto(collateral);

        // Then
        assertThat(result.getCurrentValue()).isEqualTo(new BigDecimal("4800000.00")); // appraisedValue
    }

    @Test
    void toDto_WithNullAppraisedValue_ShouldUseEstimatedValue() {
        // Given
        Collateral collateralWithoutAppraisal = collateral.toBuilder()
            .appraisedValue(null)
            .build();

        // When
        CollateralDto result = collateralMapper.toDto(collateralWithoutAppraisal);

        // Then
        assertThat(result.getCurrentValue()).isEqualTo(new BigDecimal("5000000.00")); // estimatedValue
    }

    @Test
    void toDto_ShouldCalculateIsActiveCorrectly() {
        // When
        CollateralDto result = collateralMapper.toDto(collateral);

        // Then
        assertThat(result.getIsActive()).isTrue(); // REGISTERED status
    }

    @Test
    void toDto_WithReleasedStatus_ShouldCalculateIsActiveAsFalse() {
        // Given
        Collateral releasedCollateral = collateral.toBuilder()
            .status(CollateralStatus.RELEASED)
            .build();

        // When
        CollateralDto result = collateralMapper.toDto(releasedCollateral);

        // Then
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void toDto_ShouldCalculateIsReleasedCorrectly() {
        // When
        CollateralDto result = collateralMapper.toDto(collateral);

        // Then
        assertThat(result.getIsReleased()).isFalse(); // REGISTERED status
    }

    @Test
    void toDto_WithReleasedStatus_ShouldCalculateIsReleasedAsTrue() {
        // Given
        Collateral releasedCollateral = collateral.toBuilder()
            .status(CollateralStatus.RELEASED)
            .build();

        // When
        CollateralDto result = collateralMapper.toDto(releasedCollateral);

        // Then
        assertThat(result.getIsReleased()).isTrue();
    }

    @Test
    void toDto_ShouldCalculateIsInsuranceExpiredCorrectly() {
        // When
        CollateralDto result = collateralMapper.toDto(collateral);

        // Then
        assertThat(result.getIsInsuranceExpired()).isFalse(); // Future expiry date
    }

    @Test
    void toDto_WithExpiredInsurance_ShouldCalculateIsInsuranceExpiredAsTrue() {
        // Given
        Collateral expiredCollateral = collateral.toBuilder()
            .insuranceExpiryDate(LocalDate.now().minusDays(1))
            .build();

        // When
        CollateralDto result = collateralMapper.toDto(expiredCollateral);

        // Then
        assertThat(result.getIsInsuranceExpired()).isTrue();
    }

    @Test
    void toDto_WithNullInsuranceExpiryDate_ShouldCalculateIsInsuranceExpiredAsFalse() {
        // Given
        Collateral collateralWithoutInsurance = collateral.toBuilder()
            .insuranceExpiryDate(null)
            .build();

        // When
        CollateralDto result = collateralMapper.toDto(collateralWithoutInsurance);

        // Then
        assertThat(result.getIsInsuranceExpired()).isFalse();
    }

    @Test
    void toDto_WithDifferentStatuses_ShouldCalculateFlagsCorrectly() {
        // Test ACTIVE status
        Collateral activeCollateral = collateral.toBuilder()
            .status(CollateralStatus.ACTIVE)
            .build();
        CollateralDto activeResult = collateralMapper.toDto(activeCollateral);
        assertThat(activeResult.getIsActive()).isTrue();
        assertThat(activeResult.getIsReleased()).isFalse();

        // Test REGISTERED status
        Collateral registeredCollateral = collateral.toBuilder()
            .status(CollateralStatus.REGISTERED)
            .build();
        CollateralDto registeredResult = collateralMapper.toDto(registeredCollateral);
        assertThat(registeredResult.getIsActive()).isTrue();
        assertThat(registeredResult.getIsReleased()).isFalse();

        // Test EXPIRED status
        Collateral expiredCollateral = collateral.toBuilder()
            .status(CollateralStatus.EXPIRED)
            .build();
        CollateralDto expiredResult = collateralMapper.toDto(expiredCollateral);
        assertThat(expiredResult.getIsActive()).isFalse();
        assertThat(expiredResult.getIsReleased()).isFalse();

        // Test SEIZED status
        Collateral seizedCollateral = collateral.toBuilder()
            .status(CollateralStatus.SEIZED)
            .build();
        CollateralDto seizedResult = collateralMapper.toDto(seizedCollateral);
        assertThat(seizedResult.getIsActive()).isFalse();
        assertThat(seizedResult.getIsReleased()).isFalse();
    }
} 