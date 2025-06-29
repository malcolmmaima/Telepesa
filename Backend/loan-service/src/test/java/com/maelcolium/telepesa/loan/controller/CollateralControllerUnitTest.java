package com.maelcolium.telepesa.loan.controller;

import com.maelcolium.telepesa.loan.dto.CollateralDto;
import com.maelcolium.telepesa.loan.dto.CreateCollateralRequest;
import com.maelcolium.telepesa.loan.service.CollateralService;
import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollateralControllerUnitTest {

    @Mock
    private CollateralService collateralService;

    @InjectMocks
    private CollateralController collateralController;

    private CreateCollateralRequest createCollateralRequest;
    private CollateralDto collateralDto;

    @BeforeEach
    void setUp() {
        createCollateralRequest = CreateCollateralRequest.builder()
            .loanId(1L)
            .ownerId(100L)
            .collateralType(CollateralType.REAL_ESTATE)
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
            .build();

        collateralDto = CollateralDto.builder()
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
            .currentValue(new BigDecimal("4800000.00"))
            .isActive(true)
            .isReleased(false)
            .isInsuranceExpired(false)
            .build();
    }

    @Test
    void createCollateral_WithValidRequest_ShouldReturnCreatedCollateral() {
        // Given
        when(collateralService.createCollateral(any(CreateCollateralRequest.class))).thenReturn(collateralDto);

        // When
        ResponseEntity<CollateralDto> response = collateralController.createCollateral(createCollateralRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getCollateralNumber()).isEqualTo("RE202412001234");
        assertThat(response.getBody().getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
        assertThat(response.getBody().getStatus()).isEqualTo(CollateralStatus.REGISTERED);
    }

    @Test
    void getCollateral_WithValidId_ShouldReturnCollateral() {
        // Given
        when(collateralService.getCollateral(1L)).thenReturn(collateralDto);

        // When
        ResponseEntity<CollateralDto> response = collateralController.getCollateral(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getCollateralNumber()).isEqualTo("RE202412001234");
        assertThat(response.getBody().getDescription()).isEqualTo("Residential property in Nairobi");
    }

    @Test
    void getCollateralByNumber_WithValidNumber_ShouldReturnCollateral() {
        // Given
        when(collateralService.getCollateralByNumber("RE202412001234")).thenReturn(collateralDto);

        // When
        ResponseEntity<CollateralDto> response = collateralController.getCollateralByNumber("RE202412001234");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCollateralNumber()).isEqualTo("RE202412001234");
    }

    @Test
    void getAllCollaterals_ShouldReturnPageOfCollaterals() {
        // Given
        Page<CollateralDto> collateralPage = new PageImpl<>(Arrays.asList(collateralDto));
        when(collateralService.getAllCollaterals(any(Pageable.class))).thenReturn(collateralPage);

        // When
        ResponseEntity<Page<CollateralDto>> response = collateralController.getAllCollaterals(0, 20, "createdAt", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getCollateralNumber()).isEqualTo("RE202412001234");
    }

    @Test
    void getCollateralsByLoanId_ShouldReturnCollateralsForLoan() {
        // Given
        Page<CollateralDto> collateralPage = new PageImpl<>(Arrays.asList(collateralDto));
        when(collateralService.getCollateralsByLoanId(eq(1L), any(Pageable.class))).thenReturn(collateralPage);

        // When
        ResponseEntity<Page<CollateralDto>> response = collateralController.getCollateralsByLoanId(1L, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getLoanId()).isEqualTo(1L);
    }

    @Test
    void getActiveCollateralsByLoanId_ShouldReturnActiveCollaterals() {
        // Given
        List<CollateralDto> activeCollaterals = Arrays.asList(collateralDto);
        when(collateralService.getActiveCollateralsByLoanId(1L)).thenReturn(activeCollaterals);

        // When
        ResponseEntity<List<CollateralDto>> response = collateralController.getActiveCollateralsByLoanId(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getLoanId()).isEqualTo(1L);
    }

    @Test
    void getCollateralsByOwnerId_ShouldReturnCollateralsForOwner() {
        // Given
        Page<CollateralDto> collateralPage = new PageImpl<>(Arrays.asList(collateralDto));
        when(collateralService.getCollateralsByOwnerId(eq(100L), any(Pageable.class))).thenReturn(collateralPage);

        // When
        ResponseEntity<Page<CollateralDto>> response = collateralController.getCollateralsByOwnerId(100L, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getOwnerId()).isEqualTo(100L);
    }

    @Test
    void getCollateralsByStatus_ShouldReturnCollateralsWithStatus() {
        // Given
        Page<CollateralDto> collateralPage = new PageImpl<>(Arrays.asList(collateralDto));
        when(collateralService.getCollateralsByStatus(eq(CollateralStatus.ACTIVE), any(Pageable.class))).thenReturn(collateralPage);

        // When
        ResponseEntity<Page<CollateralDto>> response = collateralController.getCollateralsByStatus(CollateralStatus.ACTIVE, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(CollateralStatus.REGISTERED);
    }

    @Test
    void getCollateralsByType_ShouldReturnCollateralsWithType() {
        // Given
        Page<CollateralDto> collateralPage = new PageImpl<>(Arrays.asList(collateralDto));
        when(collateralService.getCollateralsByType(eq(CollateralType.REAL_ESTATE), any(Pageable.class))).thenReturn(collateralPage);

        // When
        ResponseEntity<Page<CollateralDto>> response = collateralController.getCollateralsByType(CollateralType.REAL_ESTATE, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
    }

    @Test
    void updateCollateralStatus_WithValidRequest_ShouldReturnUpdatedCollateral() {
        // Given
        when(collateralService.updateCollateralStatus(1L, CollateralStatus.ACTIVE)).thenReturn(collateralDto);

        // When
        ResponseEntity<CollateralDto> response = collateralController.updateCollateralStatus(1L, CollateralStatus.ACTIVE);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void releaseCollateral_WithValidRequest_ShouldReturnReleasedCollateral() {
        // Given
        when(collateralService.releaseCollateral(1L, 200L)).thenReturn(collateralDto);

        // When
        ResponseEntity<CollateralDto> response = collateralController.releaseCollateral(1L, 200L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void updateAppraisalInfo_WithValidRequest_ShouldReturnUpdatedCollateral() {
        // Given
        when(collateralService.updateAppraisalInfo(eq(1L), any(BigDecimal.class), any(LocalDate.class), anyString())).thenReturn(collateralDto);

        // When
        ResponseEntity<CollateralDto> response = collateralController.updateAppraisalInfo(1L, new BigDecimal("5000000.00"), LocalDate.now(), "New Appraiser");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getTotalCollateralValueByLoanId_ShouldReturnTotalValue() {
        // Given
        BigDecimal totalValue = new BigDecimal("10000000.00");
        when(collateralService.getTotalCollateralValueByLoanId(1L)).thenReturn(totalValue);

        // When
        ResponseEntity<BigDecimal> response = collateralController.getTotalCollateralValueByLoanId(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(totalValue);
    }

    @Test
    void getCollateralsWithExpiredInsurance_ShouldReturnExpiredCollaterals() {
        // Given
        List<CollateralDto> expiredCollaterals = Arrays.asList(collateralDto);
        when(collateralService.getCollateralsWithExpiredInsurance()).thenReturn(expiredCollaterals);

        // When
        ResponseEntity<List<CollateralDto>> response = collateralController.getCollateralsWithExpiredInsurance();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void searchCollaterals_ShouldReturnFilteredResults() {
        // Given
        Page<CollateralDto> collateralPage = new PageImpl<>(Arrays.asList(collateralDto));
        when(collateralService.searchCollaterals(any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(collateralPage);

        // When
        ResponseEntity<Page<CollateralDto>> response = collateralController.searchCollaterals(1L, 100L, CollateralStatus.ACTIVE, CollateralType.REAL_ESTATE, LocalDate.now().minusDays(30), LocalDate.now(), 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }
} 