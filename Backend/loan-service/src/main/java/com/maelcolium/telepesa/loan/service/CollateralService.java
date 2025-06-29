package com.maelcolium.telepesa.loan.service;

import com.maelcolium.telepesa.loan.dto.CollateralDto;
import com.maelcolium.telepesa.loan.dto.CreateCollateralRequest;
import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for collateral management operations
 */
public interface CollateralService {

    /**
     * Create a new collateral
     */
    CollateralDto createCollateral(CreateCollateralRequest request);

    /**
     * Get collateral by ID
     */
    CollateralDto getCollateral(Long collateralId);

    /**
     * Get collateral by collateral number
     */
    CollateralDto getCollateralByNumber(String collateralNumber);

    /**
     * Get all collaterals with pagination
     */
    Page<CollateralDto> getAllCollaterals(Pageable pageable);

    /**
     * Get collaterals by loan ID
     */
    Page<CollateralDto> getCollateralsByLoanId(Long loanId, Pageable pageable);

    /**
     * Get collaterals by owner ID
     */
    Page<CollateralDto> getCollateralsByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Get collaterals by status
     */
    Page<CollateralDto> getCollateralsByStatus(CollateralStatus status, Pageable pageable);

    /**
     * Get collaterals by type
     */
    Page<CollateralDto> getCollateralsByType(CollateralType collateralType, Pageable pageable);

    /**
     * Get active collaterals for a loan
     */
    List<CollateralDto> getActiveCollateralsByLoanId(Long loanId);

    /**
     * Update collateral status
     */
    CollateralDto updateCollateralStatus(Long collateralId, CollateralStatus status);

    /**
     * Release collateral
     */
    CollateralDto releaseCollateral(Long collateralId, Long releasedBy);

    /**
     * Update appraisal information
     */
    CollateralDto updateAppraisalInfo(Long collateralId, BigDecimal appraisedValue, 
                                     LocalDate appraisalDate, String appraiserName);

    /**
     * Get total collateral value for a loan
     */
    BigDecimal getTotalCollateralValueByLoanId(Long loanId);

    /**
     * Get collaterals with expired insurance
     */
    List<CollateralDto> getCollateralsWithExpiredInsurance();

    /**
     * Search collaterals with multiple criteria
     */
    Page<CollateralDto> searchCollaterals(Long loanId, Long ownerId, CollateralStatus status,
                                         CollateralType collateralType, LocalDate fromDate, 
                                         LocalDate toDate, Pageable pageable);

    /**
     * Generate unique collateral number
     */
    String generateCollateralNumber(CollateralType collateralType);
} 