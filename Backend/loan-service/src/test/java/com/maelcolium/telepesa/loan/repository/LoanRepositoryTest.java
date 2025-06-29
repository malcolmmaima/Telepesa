package com.maelcolium.telepesa.loan.repository;

import com.maelcolium.telepesa.loan.model.Loan;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LoanRepository
 */
@DataJpaTest
class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    private Loan testLoan1;
    private Loan testLoan2;

    @BeforeEach
    void setUp() {
        testLoan1 = Loan.builder()
            .loanNumber("PL202412001234")
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.ACTIVE)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("0.1250"))
            .termMonths(24)
            .monthlyPayment(new BigDecimal("2347.50"))
            .outstandingBalance(new BigDecimal("40000.00"))
            .totalPaid(new BigDecimal("10000.00"))
            .applicationDate(LocalDate.now().minusDays(30))
            .nextPaymentDate(LocalDate.now().plusDays(5))
            .build();

        testLoan2 = Loan.builder()
            .loanNumber("BL202412005678")
            .userId(200L)
            .accountNumber("ACC987654321098")
            .loanType(LoanType.BUSINESS)
            .status(LoanStatus.PENDING)
            .principalAmount(new BigDecimal("100000.00"))
            .interestRate(new BigDecimal("0.1500"))
            .termMonths(36)
            .monthlyPayment(new BigDecimal("3466.67"))
            .outstandingBalance(new BigDecimal("100000.00"))
            .totalPaid(BigDecimal.ZERO)
            .applicationDate(LocalDate.now().minusDays(5))
            .build();

        entityManager.persistAndFlush(testLoan1);
        entityManager.persistAndFlush(testLoan2);
    }

    @Test
    void findByLoanNumber_WithExistingNumber_ShouldReturnLoan() {
        // When
        Optional<Loan> found = loanRepository.findByLoanNumber("PL202412001234");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(100L);
        assertThat(found.get().getLoanType()).isEqualTo(LoanType.PERSONAL);
    }

    @Test
    void findByLoanNumber_WithNonExistingNumber_ShouldReturnEmpty() {
        // When
        Optional<Loan> found = loanRepository.findByLoanNumber("INVALID123");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByLoanNumber_WithExistingNumber_ShouldReturnTrue() {
        // When
        boolean exists = loanRepository.existsByLoanNumber("PL202412001234");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByLoanNumber_WithNonExistingNumber_ShouldReturnFalse() {
        // When
        boolean exists = loanRepository.existsByLoanNumber("INVALID123");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByUserId_ShouldReturnUserLoans() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Loan> loans = loanRepository.findByUserId(100L, pageable);

        // Then
        assertThat(loans.getContent()).hasSize(1);
        assertThat(loans.getContent().get(0).getUserId()).isEqualTo(100L);
    }

    @Test
    void findByStatus_ShouldReturnLoansWithStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE, pageable);
        Page<Loan> pendingLoans = loanRepository.findByStatus(LoanStatus.PENDING, pageable);

        // Then
        assertThat(activeLoans.getContent()).hasSize(1);
        assertThat(activeLoans.getContent().get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
        
        assertThat(pendingLoans.getContent()).hasSize(1);
        assertThat(pendingLoans.getContent().get(0).getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void findByLoanType_ShouldReturnLoansWithType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Loan> personalLoans = loanRepository.findByLoanType(LoanType.PERSONAL, pageable);
        Page<Loan> businessLoans = loanRepository.findByLoanType(LoanType.BUSINESS, pageable);

        // Then
        assertThat(personalLoans.getContent()).hasSize(1);
        assertThat(personalLoans.getContent().get(0).getLoanType()).isEqualTo(LoanType.PERSONAL);
        
        assertThat(businessLoans.getContent()).hasSize(1);
        assertThat(businessLoans.getContent().get(0).getLoanType()).isEqualTo(LoanType.BUSINESS);
    }

    @Test
    void findByUserIdAndStatus_ShouldReturnFilteredLoans() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Loan> userActiveLoans = loanRepository.findByUserIdAndStatus(100L, LoanStatus.ACTIVE, pageable);
        Page<Loan> userPendingLoans = loanRepository.findByUserIdAndStatus(100L, LoanStatus.PENDING, pageable);

        // Then
        assertThat(userActiveLoans.getContent()).hasSize(1);
        assertThat(userActiveLoans.getContent().get(0).getUserId()).isEqualTo(100L);
        assertThat(userActiveLoans.getContent().get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
        
        assertThat(userPendingLoans.getContent()).hasSize(0);
    }

    @Test
    void findActiveLoansByUserId_ShouldReturnActiveLoans() {
        // When
        List<Loan> activeLoans = loanRepository.findActiveLoansByUserId(100L);

        // Then
        assertThat(activeLoans).hasSize(1);
        assertThat(activeLoans.get(0).getUserId()).isEqualTo(100L);
        assertThat(activeLoans.get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void findOverdueLoans_ShouldReturnOverdueLoans() {
        // Given
        LocalDate currentDate = LocalDate.now();

        // When
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(currentDate);

        // Then
        // testLoan1 has nextPaymentDate 5 days from now, so should not be overdue
        assertThat(overdueLoans).hasSize(0);
    }

    @Test
    void findOverdueLoans_WithPastDueDate_ShouldReturnOverdueLoans() {
        // Given
        testLoan1.setNextPaymentDate(LocalDate.now().minusDays(5));
        entityManager.merge(testLoan1);
        entityManager.flush();

        // When
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDate.now());

        // Then
        assertThat(overdueLoans).hasSize(1);
        assertThat(overdueLoans.get(0).getId()).isEqualTo(testLoan1.getId());
    }

    @Test
    void calculateTotalOutstandingBalanceByUserId_ShouldReturnTotalBalance() {
        // When
        BigDecimal totalBalance = loanRepository.calculateTotalOutstandingBalanceByUserId(100L);

        // Then
        assertThat(totalBalance).isEqualTo(new BigDecimal("40000.00"));
    }

    @Test
    void calculateTotalOutstandingBalanceByUserId_WithNoActiveLoans_ShouldReturnZero() {
        // When
        BigDecimal totalBalance = loanRepository.calculateTotalOutstandingBalanceByUserId(999L);

        // Then
        assertThat(totalBalance).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // When
        long activeCount = loanRepository.countByStatus(LoanStatus.ACTIVE);
        long pendingCount = loanRepository.countByStatus(LoanStatus.PENDING);
        long rejectedCount = loanRepository.countByStatus(LoanStatus.REJECTED);

        // Then
        assertThat(activeCount).isEqualTo(1);
        assertThat(pendingCount).isEqualTo(1);
        assertThat(rejectedCount).isEqualTo(0);
    }

    @Test
    void countActiveLoansByUserId_ShouldReturnCorrectCount() {
        // When
        long activeCount = loanRepository.countActiveLoansByUserId(100L);
        long noLoansCount = loanRepository.countActiveLoansByUserId(999L);

        // Then
        assertThat(activeCount).isEqualTo(1);
        assertThat(noLoansCount).isEqualTo(0);
    }

    @Test
    void findByAccountNumber_ShouldReturnLoansForAccount() {
        // When
        List<Loan> loans = loanRepository.findByAccountNumber("ACC001234567890");

        // Then
        assertThat(loans).hasSize(1);
        assertThat(loans.get(0).getAccountNumber()).isEqualTo("ACC001234567890");
    }

    @Test
    void searchLoans_WithAllCriteria_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate fromDate = LocalDate.now().minusDays(40);
        LocalDate toDate = LocalDate.now();

        // When
        Page<Loan> results = loanRepository.searchLoans(
            100L, LoanStatus.ACTIVE, LoanType.PERSONAL, fromDate, toDate, pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getUserId()).isEqualTo(100L);
        assertThat(results.getContent().get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(results.getContent().get(0).getLoanType()).isEqualTo(LoanType.PERSONAL);
    }

    @Test
    void searchLoans_WithNullCriteria_ShouldReturnAllLoans() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Loan> results = loanRepository.searchLoans(null, null, null, null, null, pageable);

        // Then
        assertThat(results.getContent()).hasSize(2);
    }

    @Test
    void searchLoans_WithPartialCriteria_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Loan> results = loanRepository.searchLoans(null, LoanStatus.PENDING, null, null, null, pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getStatus()).isEqualTo(LoanStatus.PENDING);
    }
}
