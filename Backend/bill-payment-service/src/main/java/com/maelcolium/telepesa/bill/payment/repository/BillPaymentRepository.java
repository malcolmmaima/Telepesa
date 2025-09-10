package com.maelcolium.telepesa.bill.payment.repository;

import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, String> {
    
    Optional<BillPayment> findByPaymentReference(String paymentReference);
    
    Page<BillPayment> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);
    
    Page<BillPayment> findByBillTypeOrderByCreatedAtDesc(BillPayment.BillType billType, Pageable pageable);
    
    List<BillPayment> findByBillNumberAndServiceProvider(String billNumber, String serviceProvider);
    
    List<BillPayment> findByStatusInAndCreatedAtBefore(List<BillPayment.PaymentStatus> statuses, LocalDateTime cutoffTime);
    
    @Query("SELECT bp FROM BillPayment bp WHERE bp.status = :status AND bp.createdAt BETWEEN :startDate AND :endDate")
    List<BillPayment> findByStatusAndDateRange(
        @Param("status") BillPayment.PaymentStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(bp.amount) FROM BillPayment bp WHERE bp.accountId = :accountId AND bp.status = 'COMPLETED' AND bp.createdAt >= :since")
    BigDecimal getTotalPaidAmount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(bp) FROM BillPayment bp WHERE bp.accountId = :accountId AND bp.status = 'COMPLETED' AND bp.createdAt >= :since")
    Long getPaymentCountByAccount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT bp FROM BillPayment bp WHERE bp.billType = :billType AND bp.serviceProvider = :provider AND bp.status = :status ORDER BY bp.createdAt DESC")
    List<BillPayment> findByBillTypeAndProviderAndStatus(
        @Param("billType") BillPayment.BillType billType,
        @Param("provider") String serviceProvider,
        @Param("status") BillPayment.PaymentStatus status,
        Pageable pageable
    );
    
    @Query("SELECT DISTINCT bp.serviceProvider FROM BillPayment bp WHERE bp.billType = :billType")
    List<String> findServiceProvidersByBillType(@Param("billType") BillPayment.BillType billType);
    
    boolean existsByPaymentReference(String paymentReference);
    
    @Query("SELECT bp FROM BillPayment bp WHERE bp.accountId = :accountId AND bp.billType = :billType ORDER BY bp.createdAt DESC")
    Page<BillPayment> findByAccountIdAndBillType(
        @Param("accountId") String accountId, 
        @Param("billType") BillPayment.BillType billType, 
        Pageable pageable
    );
}
