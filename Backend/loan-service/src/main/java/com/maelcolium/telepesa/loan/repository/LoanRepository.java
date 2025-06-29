package com.maelcolium.telepesa.loan.repository;

import com.maelcolium.telepesa.loan.model.Loan;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Loan entity operations
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Find loan by loan number
     */
    Optional<Loan> findByLoanNumber(String loanNumber);

    /**
     * Check if loan number exists
     */
    boolean existsByLoanNumber(String loanNumber);

    /**
     * Find all loans for a specific user
     */
    Page<Loan> findByUserId(Long userId, Pageable pageable);

    /**
     * Find loans by status
     */
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * Find loans by type
     */
    Page<Loan> findByLoanType(LoanType loanType, Pageable pageable);

    /**
     * Find loans by user and status
     */
    Page<Loan> findByUserIdAndStatus(Long userId, LoanStatus status, Pageable pageable);

    /**
     * Find active loans for a user
     */
    @Query("SELECT l FROM Loan l WHERE l.userId = :userId AND l.status IN ('ACTIVE', 'CURRENT')")
    List<Loan> findActiveLoansByUserId(@Param("userId") Long userId);

    /**
     * Find overdue loans
     */
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate < :currentDate AND l.status IN ('ACTIVE', 'CURRENT')")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDate currentDate);

    /**
     * Find loans with outstanding balance greater than specified amount
     */
    @Query("SELECT l FROM Loan l WHERE l.outstandingBalance > :amount")
    Page<Loan> findLoansWithOutstandingBalanceGreaterThan(@Param("amount") BigDecimal amount, Pageable pageable);

    /**
     * Calculate total outstanding balance for a user
     */
    @Query("SELECT COALESCE(SUM(l.outstandingBalance), 0) FROM Loan l WHERE l.userId = :userId AND l.status IN ('ACTIVE', 'CURRENT')")
    BigDecimal calculateTotalOutstandingBalanceByUserId(@Param("userId") Long userId);

    /**
     * Count loans by status
     */
    long countByStatus(LoanStatus status);

    /**
     * Count active loans for a user
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.userId = :userId AND l.status IN ('ACTIVE', 'CURRENT')")
    long countActiveLoansByUserId(@Param("userId") Long userId);

    /**
     * Find loans disbursed within date range
     */
    @Query("SELECT l FROM Loan l WHERE l.disbursementDate BETWEEN :startDate AND :endDate")
    Page<Loan> findLoansDisbursedBetween(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate, 
                                        Pageable pageable);

    /**
     * Update loan status
     */
    @Modifying
    @Query("UPDATE Loan l SET l.status = :status WHERE l.id = :loanId")
    int updateLoanStatus(@Param("loanId") Long loanId, @Param("status") LoanStatus status);

    /**
     * Update outstanding balance
     */
    @Modifying
    @Query("UPDATE Loan l SET l.outstandingBalance = :balance, l.totalPaid = :totalPaid WHERE l.id = :loanId")
    int updateLoanBalance(@Param("loanId") Long loanId, 
                         @Param("balance") BigDecimal balance, 
                         @Param("totalPaid") BigDecimal totalPaid);

    /**
     * Update next payment date
     */
    @Modifying
    @Query("UPDATE Loan l SET l.nextPaymentDate = :nextPaymentDate WHERE l.id = :loanId")
    int updateNextPaymentDate(@Param("loanId") Long loanId, @Param("nextPaymentDate") LocalDate nextPaymentDate);

    /**
     * Find loans by account number
     */
    List<Loan> findByAccountNumber(String accountNumber);

    /**
     * Search loans by multiple criteria
     */
    @Query("SELECT l FROM Loan l WHERE " +
           "(:userId IS NULL OR l.userId = :userId) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:loanType IS NULL OR l.loanType = :loanType) AND " +
           "(:fromDate IS NULL OR l.applicationDate >= :fromDate) AND " +
           "(:toDate IS NULL OR l.applicationDate <= :toDate)")
    Page<Loan> searchLoans(@Param("userId") Long userId,
                          @Param("status") LoanStatus status,
                          @Param("loanType") LoanType loanType,
                          @Param("fromDate") LocalDate fromDate,
                          @Param("toDate") LocalDate toDate,
                          Pageable pageable);
}
