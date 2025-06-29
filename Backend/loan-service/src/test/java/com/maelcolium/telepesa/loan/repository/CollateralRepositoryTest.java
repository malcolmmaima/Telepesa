package com.maelcolium.telepesa.loan.repository;

import com.maelcolium.telepesa.loan.model.Collateral;
import com.maelcolium.telepesa.loan.model.Loan;
import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CollateralRepository.class))
@ActiveProfiles("test")
class CollateralRepositoryTest {

    @Autowired
    private CollateralRepository collateralRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private Collateral collateral;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        // Create a loan first to satisfy foreign key constraint
        testLoan = Loan.builder()
            .loanNumber("PL202412001234")
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.ACTIVE)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("0.1250")) // 12.50% as decimal
            .termMonths(24)
            .monthlyPayment(new BigDecimal("2347.50"))
            .outstandingBalance(new BigDecimal("50000.00"))
            .totalPaid(BigDecimal.ZERO)
            .applicationDate(LocalDate.now())
            .build();
        
        // Persist the loan first
        testLoan = entityManager.persistAndFlush(testLoan);

        collateral = Collateral.builder()
            .collateralNumber("RE202412001234")
            .loanId(testLoan.getId()) // Use the actual loan ID
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
            .build();
    }

    @Test
    void save_ShouldPersistCollateral() {
        // When
        Collateral savedCollateral = collateralRepository.save(collateral);

        // Then
        assertThat(savedCollateral.getId()).isNotNull();
        assertThat(savedCollateral.getCollateralNumber()).isEqualTo("RE202412001234");
        assertThat(savedCollateral.getStatus()).isEqualTo(CollateralStatus.REGISTERED);
    }

    @Test
    void findByCollateralNumber_WithExistingCollateral_ShouldReturnCollateral() {
        // Given
        entityManager.persistAndFlush(collateral);

        // When
        Optional<Collateral> found = collateralRepository.findByCollateralNumber("RE202412001234");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Residential property in Nairobi");
    }

    @Test
    void findByCollateralNumber_WithNonExistingCollateral_ShouldReturnEmpty() {
        // When
        Optional<Collateral> found = collateralRepository.findByCollateralNumber("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByCollateralNumber_WithExistingCollateral_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(collateral);

        // When
        boolean exists = collateralRepository.existsByCollateralNumber("RE202412001234");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCollateralNumber_WithNonExistingCollateral_ShouldReturnFalse() {
        // When
        boolean exists = collateralRepository.existsByCollateralNumber("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByLoanId_ShouldReturnCollateralsForLoan() {
        // Given
        Collateral collateral2 = collateral.toBuilder()
            .collateralNumber("VE202412001235")
            .collateralType(CollateralType.VEHICLE)
            .description("Toyota Land Cruiser")
            .estimatedValue(new BigDecimal("3000000.00"))
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(collateral2);

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Collateral> result = collateralRepository.findByLoanId(testLoan.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("collateralNumber")
            .containsExactlyInAnyOrder("RE202412001234", "VE202412001235");
    }

    @Test
    void findByStatus_ShouldReturnCollateralsWithStatus() {
        // Given
        Collateral activeCollateral = collateral.toBuilder()
            .collateralNumber("RE202412001235")
            .status(CollateralStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(activeCollateral);

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Collateral> result = collateralRepository.findByStatus(CollateralStatus.REGISTERED, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(CollateralStatus.REGISTERED);
    }

    @Test
    void findByCollateralType_ShouldReturnCollateralsWithType() {
        // Given
        Collateral vehicleCollateral = collateral.toBuilder()
            .collateralNumber("VE202412001235")
            .collateralType(CollateralType.VEHICLE)
            .description("Toyota Land Cruiser")
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(vehicleCollateral);

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Collateral> result = collateralRepository.findByCollateralType(CollateralType.REAL_ESTATE, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
    }

    @Test
    void findByOwnerId_ShouldReturnCollateralsForOwner() {
        // Given
        Collateral owner2Collateral = collateral.toBuilder()
            .collateralNumber("RE202412001235")
            .ownerId(200L)
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(owner2Collateral);

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Collateral> result = collateralRepository.findByOwnerId(100L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOwnerId()).isEqualTo(100L);
    }

    @Test
    void findActiveCollateralsByLoanId_ShouldReturnActiveCollaterals() {
        // Given
        Collateral activeCollateral = collateral.toBuilder()
            .collateralNumber("RE202412001235")
            .status(CollateralStatus.ACTIVE)
            .build();
        Collateral releasedCollateral = collateral.toBuilder()
            .collateralNumber("RE202412001236")
            .status(CollateralStatus.RELEASED)
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(activeCollateral);
        entityManager.persistAndFlush(releasedCollateral);

        // When
        List<Collateral> result = collateralRepository.findActiveCollateralsByLoanId(testLoan.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("status")
            .containsExactlyInAnyOrder(CollateralStatus.REGISTERED, CollateralStatus.ACTIVE);
    }

    @Test
    void calculateTotalCollateralValueByLoanId_ShouldReturnTotalValue() {
        // Given
        Collateral collateral2 = collateral.toBuilder()
            .collateralNumber("VE202412001235")
            .collateralType(CollateralType.VEHICLE)
            .estimatedValue(new BigDecimal("3000000.00"))
            .appraisedValue(new BigDecimal("2800000.00"))
            .status(CollateralStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(collateral2);

        // When
        BigDecimal totalValue = collateralRepository.calculateTotalCollateralValueByLoanId(testLoan.getId());

        // Then
        assertThat(totalValue).isEqualTo(new BigDecimal("7600000.00")); // 4800000 + 2800000
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // Given
        Collateral activeCollateral = collateral.toBuilder()
            .collateralNumber("RE202412001235")
            .status(CollateralStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(activeCollateral);

        // When
        long registeredCount = collateralRepository.countByStatus(CollateralStatus.REGISTERED);
        long activeCount = collateralRepository.countByStatus(CollateralStatus.ACTIVE);

        // Then
        assertThat(registeredCount).isEqualTo(1);
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    void countActiveCollateralsByLoanId_ShouldReturnCorrectCount() {
        // Given
        Collateral activeCollateral = collateral.toBuilder()
            .collateralNumber("RE202412001235")
            .status(CollateralStatus.ACTIVE)
            .build();
        Collateral releasedCollateral = collateral.toBuilder()
            .collateralNumber("RE202412001236")
            .status(CollateralStatus.RELEASED)
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(activeCollateral);
        entityManager.persistAndFlush(releasedCollateral);

        // When
        long activeCount = collateralRepository.countActiveCollateralsByLoanId(testLoan.getId());

        // Then
        assertThat(activeCount).isEqualTo(2); // REGISTERED and ACTIVE
    }

    @Test
    void searchCollaterals_ShouldReturnFilteredResults() {
        // Given
        Collateral vehicleCollateral = collateral.toBuilder()
            .collateralNumber("VE202412001235")
            .collateralType(CollateralType.VEHICLE)
            .description("Toyota Land Cruiser")
            .estimatedValue(new BigDecimal("3000000.00"))
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(vehicleCollateral);

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Collateral> result = collateralRepository.searchCollaterals(
            testLoan.getId(), 100L, CollateralStatus.REGISTERED, CollateralType.REAL_ESTATE, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCollateralType()).isEqualTo(CollateralType.REAL_ESTATE);
        assertThat(result.getContent().get(0).getOwnerId()).isEqualTo(100L);
    }

    @Test
    void findCollateralsWithExpiredInsurance_ShouldReturnExpiredCollaterals() {
        // Given
        Collateral expiredCollateral = collateral.toBuilder()
            .collateralNumber("RE202412001235")
            .insuranceExpiryDate(LocalDate.now().minusDays(1)) // Expired yesterday
            .status(CollateralStatus.INSURED)
            .build();
        entityManager.persistAndFlush(collateral);
        entityManager.persistAndFlush(expiredCollateral);

        // When
        List<Collateral> result = collateralRepository.findCollateralsWithExpiredInsurance(LocalDate.now());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCollateralNumber()).isEqualTo("RE202412001235");
    }
} 