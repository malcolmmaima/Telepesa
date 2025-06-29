package com.maelcolium.telepesa.loan.repository;

import com.maelcolium.telepesa.loan.model.LoanPayment;
import com.maelcolium.telepesa.models.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LoanPayment entity operations
 */
@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    /**
     * Find payment by reference number
     */
    Optional<LoanPayment> findByPaymentReference(String paymentReference);

    /**
     * Check if payment reference exists
     */
    boolean existsByPaymentReference(String paymentReference);

    /**
     * Find all payments for a specific loan
     */
    Page<LoanPayment> findByLoanId(Long loanId, Pageable pageable);

    /**
     * Find payments by status
     */
    Page<LoanPayment> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Find payments by loan and status
     */
    List<LoanPayment> findByLoanIdAndStatus(Long loanId, PaymentStatus status);

    /**
     * Find payments made between dates
     */
    @Query("SELECT p FROM LoanPayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Page<LoanPayment> findPaymentsBetweenDates(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              Pageable pageable);

    /**
     * Calculate total payments for a loan
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM LoanPayment p WHERE p.loan.id = :loanId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsByLoanId(@Param("loanId") Long loanId);

    /**
     * Find overdue payments
     */
    @Query("SELECT p FROM LoanPayment p WHERE p.dueDate < :currentDate AND p.status = 'PENDING'")
    List<LoanPayment> findOverduePayments(@Param("currentDate") LocalDate currentDate);

    /**
     * Count payments by status for a loan
     */
    long countByLoanIdAndStatus(Long loanId, PaymentStatus status);

    /**
     * Find last payment for a loan
     */
    @Query("SELECT p FROM LoanPayment p WHERE p.loan.id = :loanId ORDER BY p.paymentDate DESC")
    Page<LoanPayment> findLastPaymentByLoanId(@Param("loanId") Long loanId, Pageable pageable);

    /**
     * Find payments with late fees
     */
    @Query("SELECT p FROM LoanPayment p WHERE p.lateFee > 0")
    List<LoanPayment> findPaymentsWithLateFees();
}
