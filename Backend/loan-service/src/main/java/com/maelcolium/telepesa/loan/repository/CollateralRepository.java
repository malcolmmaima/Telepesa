package com.maelcolium.telepesa.loan.repository;

import com.maelcolium.telepesa.loan.model.Collateral;
import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
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
 * Repository interface for Collateral entity operations
 */
@Repository
public interface CollateralRepository extends JpaRepository<Collateral, Long> {

    /**
     * Find collateral by collateral number
     */
    Optional<Collateral> findByCollateralNumber(String collateralNumber);

    /**
     * Check if collateral number exists
     */
    boolean existsByCollateralNumber(String collateralNumber);

    /**
     * Find all collaterals for a specific loan
     */
    Page<Collateral> findByLoanId(Long loanId, Pageable pageable);

    /**
     * Find collaterals by status
     */
    Page<Collateral> findByStatus(CollateralStatus status, Pageable pageable);

    /**
     * Find collaterals by type
     */
    Page<Collateral> findByCollateralType(CollateralType collateralType, Pageable pageable);

    /**
     * Find collaterals by owner
     */
    Page<Collateral> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Find active collaterals for a loan
     */
    @Query("SELECT c FROM Collateral c WHERE c.loanId = :loanId AND c.status IN ('ACTIVE', 'REGISTERED', 'INSURED')")
    List<Collateral> findActiveCollateralsByLoanId(@Param("loanId") Long loanId);

    /**
     * Find collaterals with expired insurance
     */
    @Query("SELECT c FROM Collateral c WHERE c.insuranceExpiryDate < :currentDate AND c.status IN ('ACTIVE', 'INSURED')")
    List<Collateral> findCollateralsWithExpiredInsurance(@Param("currentDate") LocalDate currentDate);

    /**
     * Find collaterals with value greater than specified amount
     */
    @Query("SELECT c FROM Collateral c WHERE c.appraisedValue > :amount OR (c.appraisedValue IS NULL AND c.estimatedValue > :amount)")
    Page<Collateral> findCollateralsWithValueGreaterThan(@Param("amount") BigDecimal amount, Pageable pageable);

    /**
     * Calculate total collateral value for a loan
     */
    @Query("SELECT COALESCE(SUM(COALESCE(c.appraisedValue, c.estimatedValue)), 0) FROM Collateral c WHERE c.loanId = :loanId AND c.status IN ('ACTIVE', 'REGISTERED', 'INSURED')")
    BigDecimal calculateTotalCollateralValueByLoanId(@Param("loanId") Long loanId);

    /**
     * Count collaterals by status
     */
    long countByStatus(CollateralStatus status);

    /**
     * Count active collaterals for a loan
     */
    @Query("SELECT COUNT(c) FROM Collateral c WHERE c.loanId = :loanId AND c.status IN ('ACTIVE', 'REGISTERED', 'INSURED')")
    long countActiveCollateralsByLoanId(@Param("loanId") Long loanId);

    /**
     * Find collaterals registered within date range
     */
    @Query("SELECT c FROM Collateral c WHERE c.registrationDate BETWEEN :startDate AND :endDate")
    Page<Collateral> findCollateralsRegisteredBetween(@Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate, 
                                                     Pageable pageable);

    /**
     * Update collateral status
     */
    @Modifying
    @Query("UPDATE Collateral c SET c.status = :status WHERE c.id = :collateralId")
    int updateCollateralStatus(@Param("collateralId") Long collateralId, @Param("status") CollateralStatus status);

    /**
     * Release collateral
     */
    @Modifying
    @Query("UPDATE Collateral c SET c.status = 'RELEASED', c.releaseDate = :releaseDate, c.releasedBy = :releasedBy WHERE c.id = :collateralId")
    int releaseCollateral(@Param("collateralId") Long collateralId, 
                         @Param("releaseDate") LocalDate releaseDate, 
                         @Param("releasedBy") Long releasedBy);

    /**
     * Update appraisal information
     */
    @Modifying
    @Query("UPDATE Collateral c SET c.appraisedValue = :appraisedValue, c.appraisalDate = :appraisalDate, c.appraiserName = :appraiserName WHERE c.id = :collateralId")
    int updateAppraisalInfo(@Param("collateralId") Long collateralId,
                           @Param("appraisedValue") BigDecimal appraisedValue,
                           @Param("appraisalDate") LocalDate appraisalDate,
                           @Param("appraiserName") String appraiserName);

    /**
     * Search collaterals by multiple criteria
     */
    @Query("SELECT c FROM Collateral c WHERE " +
           "(:loanId IS NULL OR c.loanId = :loanId) AND " +
           "(:ownerId IS NULL OR c.ownerId = :ownerId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:collateralType IS NULL OR c.collateralType = :collateralType) AND " +
           "(:fromDate IS NULL OR c.registrationDate >= :fromDate) AND " +
           "(:toDate IS NULL OR c.registrationDate <= :toDate)")
    Page<Collateral> searchCollaterals(@Param("loanId") Long loanId,
                                      @Param("ownerId") Long ownerId,
                                      @Param("status") CollateralStatus status,
                                      @Param("collateralType") CollateralType collateralType,
                                      @Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate,
                                      Pageable pageable);
} 