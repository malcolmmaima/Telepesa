package com.maelcolium.telepesa.loan.service;

import com.maelcolium.telepesa.loan.dto.CollateralDto;
import com.maelcolium.telepesa.loan.dto.CreateCollateralRequest;
import com.maelcolium.telepesa.loan.exception.CollateralNotFoundException;
import com.maelcolium.telepesa.loan.exception.CollateralOperationException;
import com.maelcolium.telepesa.loan.mapper.CollateralMapper;
import com.maelcolium.telepesa.loan.model.Collateral;
import com.maelcolium.telepesa.loan.repository.CollateralRepository;
import com.maelcolium.telepesa.loan.service.impl.CollateralServiceImpl;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollateralServiceTest {

    @Mock
    private CollateralRepository collateralRepository;

    @Mock
    private CollateralMapper collateralMapper;

    @InjectMocks
    private CollateralServiceImpl collateralService;

    private CreateCollateralRequest createCollateralRequest;
    private Collateral collateral;
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
            .createdAt(java.time.LocalDateTime.now())
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
    void createCollateral_WithValidRequest_ShouldReturnCollateralDto() {
        // Given
        when(collateralRepository.save(any(Collateral.class))).thenReturn(collateral);
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        CollateralDto result = collateralService.createCollateral(createCollateralRequest);

        // Then
        assertThat(result).isEqualTo(collateralDto);
        verify(collateralRepository).save(any(Collateral.class));
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void getCollateral_WithValidId_ShouldReturnCollateralDto() {
        // Given
        when(collateralRepository.findById(1L)).thenReturn(Optional.of(collateral));
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        CollateralDto result = collateralService.getCollateral(1L);

        // Then
        assertThat(result).isEqualTo(collateralDto);
        verify(collateralRepository).findById(1L);
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void getCollateral_WithInvalidId_ShouldThrowException() {
        // Given
        when(collateralRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> collateralService.getCollateral(999L))
            .isInstanceOf(CollateralNotFoundException.class)
            .hasMessage("Collateral not found with id: 999");
        verify(collateralRepository).findById(999L);
        verify(collateralMapper, never()).toDto(any());
    }

    @Test
    void getCollateralByNumber_WithValidNumber_ShouldReturnCollateralDto() {
        // Given
        when(collateralRepository.findByCollateralNumber("RE202412001234")).thenReturn(Optional.of(collateral));
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        CollateralDto result = collateralService.getCollateralByNumber("RE202412001234");

        // Then
        assertThat(result).isEqualTo(collateralDto);
        verify(collateralRepository).findByCollateralNumber("RE202412001234");
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void getCollateralByNumber_WithInvalidNumber_ShouldThrowException() {
        // Given
        when(collateralRepository.findByCollateralNumber("INVALID123")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> collateralService.getCollateralByNumber("INVALID123"))
            .isInstanceOf(CollateralNotFoundException.class)
            .hasMessage("Collateral not found with number: INVALID123");
        verify(collateralRepository).findByCollateralNumber("INVALID123");
        verify(collateralMapper, never()).toDto(any());
    }

    @Test
    void getAllCollaterals_ShouldReturnPageOfCollateralDtos() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Collateral> collateralPage = new PageImpl<>(Arrays.asList(collateral));
        Page<CollateralDto> expectedPage = new PageImpl<>(Arrays.asList(collateralDto));

        when(collateralRepository.findAll(pageable)).thenReturn(collateralPage);
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        Page<CollateralDto> result = collateralService.getAllCollaterals(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        verify(collateralRepository).findAll(pageable);
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void getCollateralsByLoanId_ShouldReturnPageOfCollateralDtos() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Collateral> collateralPage = new PageImpl<>(Arrays.asList(collateral));
        Page<CollateralDto> expectedPage = new PageImpl<>(Arrays.asList(collateralDto));

        when(collateralRepository.findByLoanId(1L, pageable)).thenReturn(collateralPage);
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        Page<CollateralDto> result = collateralService.getCollateralsByLoanId(1L, pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        verify(collateralRepository).findByLoanId(1L, pageable);
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void getActiveCollateralsByLoanId_ShouldReturnListOfCollateralDtos() {
        // Given
        List<Collateral> collaterals = Arrays.asList(collateral);
        List<CollateralDto> expectedDtos = Arrays.asList(collateralDto);

        when(collateralRepository.findActiveCollateralsByLoanId(1L)).thenReturn(collaterals);
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        List<CollateralDto> result = collateralService.getActiveCollateralsByLoanId(1L);

        // Then
        assertThat(result).isEqualTo(expectedDtos);
        verify(collateralRepository).findActiveCollateralsByLoanId(1L);
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void updateCollateralStatus_WithValidId_ShouldReturnUpdatedCollateralDto() {
        // Given
        Collateral updatedCollateral = collateral.toBuilder().status(CollateralStatus.ACTIVE).build();
        CollateralDto updatedDto = collateralDto.toBuilder().status(CollateralStatus.ACTIVE).build();

        when(collateralRepository.findById(1L)).thenReturn(Optional.of(collateral));
        when(collateralRepository.save(any(Collateral.class))).thenReturn(updatedCollateral);
        when(collateralMapper.toDto(updatedCollateral)).thenReturn(updatedDto);

        // When
        CollateralDto result = collateralService.updateCollateralStatus(1L, CollateralStatus.ACTIVE);

        // Then
        assertThat(result).isEqualTo(updatedDto);
        verify(collateralRepository).findById(1L);
        verify(collateralRepository).save(any(Collateral.class));
        verify(collateralMapper).toDto(updatedCollateral);
    }

    @Test
    void updateCollateralStatus_WithInvalidId_ShouldThrowException() {
        // Given
        when(collateralRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> collateralService.updateCollateralStatus(999L, CollateralStatus.ACTIVE))
            .isInstanceOf(CollateralNotFoundException.class)
            .hasMessage("Collateral not found with id: 999");
        verify(collateralRepository).findById(999L);
        verify(collateralRepository, never()).save(any());
    }

    @Test
    void releaseCollateral_WithActiveCollateral_ShouldReturnReleasedCollateralDto() {
        // Given
        Collateral releasedCollateral = collateral.toBuilder()
            .status(CollateralStatus.RELEASED)
            .releaseDate(LocalDate.now())
            .releasedBy(200L)
            .build();
        CollateralDto releasedDto = collateralDto.toBuilder()
            .status(CollateralStatus.RELEASED)
            .releaseDate(LocalDate.now())
            .releasedBy(200L)
            .isReleased(true)
            .build();

        when(collateralRepository.findById(1L)).thenReturn(Optional.of(collateral));
        when(collateralRepository.save(any(Collateral.class))).thenReturn(releasedCollateral);
        when(collateralMapper.toDto(releasedCollateral)).thenReturn(releasedDto);

        // When
        CollateralDto result = collateralService.releaseCollateral(1L, 200L);

        // Then
        assertThat(result).isEqualTo(releasedDto);
        verify(collateralRepository).findById(1L);
        verify(collateralRepository).save(any(Collateral.class));
        verify(collateralMapper).toDto(releasedCollateral);
    }

    @Test
    void releaseCollateral_WithInactiveCollateral_ShouldThrowException() {
        // Given
        Collateral inactiveCollateral = collateral.toBuilder().status(CollateralStatus.RELEASED).build();
        when(collateralRepository.findById(1L)).thenReturn(Optional.of(inactiveCollateral));

        // When & Then
        assertThatThrownBy(() -> collateralService.releaseCollateral(1L, 200L))
            .isInstanceOf(CollateralOperationException.class)
            .hasMessage("Collateral is not active for release");
        verify(collateralRepository).findById(1L);
        verify(collateralRepository, never()).save(any());
    }

    @Test
    void updateAppraisalInfo_WithValidId_ShouldReturnUpdatedCollateralDto() {
        // Given
        BigDecimal newAppraisedValue = new BigDecimal("5200000.00");
        LocalDate newAppraisalDate = LocalDate.now().plusDays(1);
        String newAppraiserName = "Jane Smith Appraisals";

        Collateral updatedCollateral = collateral.toBuilder()
            .appraisedValue(newAppraisedValue)
            .appraisalDate(newAppraisalDate)
            .appraiserName(newAppraiserName)
            .build();
        CollateralDto updatedDto = collateralDto.toBuilder()
            .appraisedValue(newAppraisedValue)
            .appraisalDate(newAppraisalDate)
            .appraiserName(newAppraiserName)
            .currentValue(newAppraisedValue)
            .build();

        when(collateralRepository.findById(1L)).thenReturn(Optional.of(collateral));
        when(collateralRepository.save(any(Collateral.class))).thenReturn(updatedCollateral);
        when(collateralMapper.toDto(updatedCollateral)).thenReturn(updatedDto);

        // When
        CollateralDto result = collateralService.updateAppraisalInfo(1L, newAppraisedValue, newAppraisalDate, newAppraiserName);

        // Then
        assertThat(result).isEqualTo(updatedDto);
        verify(collateralRepository).findById(1L);
        verify(collateralRepository).save(any(Collateral.class));
        verify(collateralMapper).toDto(updatedCollateral);
    }

    @Test
    void getTotalCollateralValueByLoanId_ShouldReturnTotalValue() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("10000000.00");
        when(collateralRepository.calculateTotalCollateralValueByLoanId(1L)).thenReturn(expectedTotal);

        // When
        BigDecimal result = collateralService.getTotalCollateralValueByLoanId(1L);

        // Then
        assertThat(result).isEqualTo(expectedTotal);
        verify(collateralRepository).calculateTotalCollateralValueByLoanId(1L);
    }

    @Test
    void getCollateralsWithExpiredInsurance_ShouldReturnListOfCollateralDtos() {
        // Given
        List<Collateral> collaterals = Arrays.asList(collateral);
        List<CollateralDto> expectedDtos = Arrays.asList(collateralDto);

        when(collateralRepository.findCollateralsWithExpiredInsurance(LocalDate.now())).thenReturn(collaterals);
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        List<CollateralDto> result = collateralService.getCollateralsWithExpiredInsurance();

        // Then
        assertThat(result).isEqualTo(expectedDtos);
        verify(collateralRepository).findCollateralsWithExpiredInsurance(LocalDate.now());
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void searchCollaterals_ShouldReturnPageOfCollateralDtos() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Collateral> collateralPage = new PageImpl<>(Arrays.asList(collateral));
        Page<CollateralDto> expectedPage = new PageImpl<>(Arrays.asList(collateralDto));

        when(collateralRepository.searchCollaterals(1L, 100L, CollateralStatus.ACTIVE, 
            CollateralType.REAL_ESTATE, LocalDate.now().minusDays(30), LocalDate.now(), pageable))
            .thenReturn(collateralPage);
        when(collateralMapper.toDto(collateral)).thenReturn(collateralDto);

        // When
        Page<CollateralDto> result = collateralService.searchCollaterals(1L, 100L, CollateralStatus.ACTIVE,
            CollateralType.REAL_ESTATE, LocalDate.now().minusDays(30), LocalDate.now(), pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        verify(collateralRepository).searchCollaterals(1L, 100L, CollateralStatus.ACTIVE,
            CollateralType.REAL_ESTATE, LocalDate.now().minusDays(30), LocalDate.now(), pageable);
        verify(collateralMapper).toDto(collateral);
    }

    @Test
    void generateCollateralNumber_ShouldReturnValidNumber() {
        // When
        String result = collateralService.generateCollateralNumber(CollateralType.REAL_ESTATE);

        // Then
        assertThat(result).startsWith("RE");
        assertThat(result).hasSize(10); // RE + 3 digits timestamp + 3 digits random
    }

    @Test
    void generateCollateralNumber_WithDifferentTypes_ShouldReturnCorrectPrefix() {
        // When & Then
        assertThat(collateralService.generateCollateralNumber(CollateralType.VEHICLE)).startsWith("VE");
        assertThat(collateralService.generateCollateralNumber(CollateralType.EQUIPMENT)).startsWith("EQ");
        assertThat(collateralService.generateCollateralNumber(CollateralType.INVENTORY)).startsWith("IN");
        assertThat(collateralService.generateCollateralNumber(CollateralType.SECURITIES)).startsWith("SE");
        assertThat(collateralService.generateCollateralNumber(CollateralType.CASH_DEPOSIT)).startsWith("CD");
        assertThat(collateralService.generateCollateralNumber(CollateralType.INSURANCE_POLICY)).startsWith("IP");
        assertThat(collateralService.generateCollateralNumber(CollateralType.GOLD_JEWELRY)).startsWith("GJ");
        assertThat(collateralService.generateCollateralNumber(CollateralType.LAND)).startsWith("LA");
        assertThat(collateralService.generateCollateralNumber(CollateralType.BUILDING)).startsWith("BU");
        assertThat(collateralService.generateCollateralNumber(CollateralType.MACHINERY)).startsWith("MA");
        assertThat(collateralService.generateCollateralNumber(CollateralType.ACCOUNTS_RECEIVABLE)).startsWith("AR");
        assertThat(collateralService.generateCollateralNumber(CollateralType.OTHER)).startsWith("OT");
    }
} 