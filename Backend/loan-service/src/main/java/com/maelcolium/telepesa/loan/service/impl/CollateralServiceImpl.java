package com.maelcolium.telepesa.loan.service.impl;

import com.maelcolium.telepesa.loan.dto.CollateralDto;
import com.maelcolium.telepesa.loan.dto.CreateCollateralRequest;
import com.maelcolium.telepesa.loan.exception.CollateralNotFoundException;
import com.maelcolium.telepesa.loan.exception.CollateralOperationException;
import com.maelcolium.telepesa.loan.mapper.CollateralMapper;
import com.maelcolium.telepesa.loan.model.Collateral;
import com.maelcolium.telepesa.loan.repository.CollateralRepository;
import com.maelcolium.telepesa.loan.service.CollateralService;
import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CollateralService
 */
@Service
@Transactional
@Slf4j
public class CollateralServiceImpl implements CollateralService {

    private final CollateralRepository collateralRepository;
    private final CollateralMapper collateralMapper;

    public CollateralServiceImpl(CollateralRepository collateralRepository, 
                               CollateralMapper collateralMapper) {
        this.collateralRepository = collateralRepository;
        this.collateralMapper = collateralMapper;
    }

    @Override
    public CollateralDto createCollateral(CreateCollateralRequest request) {
        log.info("Creating collateral for loan: {} with type: {}", 
                request.getLoanId(), request.getCollateralType());

        // Generate unique collateral number
        String collateralNumber = generateCollateralNumber(request.getCollateralType());

        // Create collateral entity
        Collateral collateral = Collateral.builder()
            .collateralNumber(collateralNumber)
            .loanId(request.getLoanId())
            .ownerId(request.getOwnerId())
            .collateralType(request.getCollateralType())
            .status(CollateralStatus.REGISTERED)
            .description(request.getDescription())
            .estimatedValue(request.getEstimatedValue())
            .appraisedValue(request.getAppraisedValue())
            .appraisalDate(request.getAppraisalDate())
            .appraiserName(request.getAppraiserName())
            .location(request.getLocation())
            .documentReference(request.getDocumentReference())
            .insurancePolicyNumber(request.getInsurancePolicyNumber())
            .insuranceExpiryDate(request.getInsuranceExpiryDate())
            .insuranceAmount(request.getInsuranceAmount())
            .notes(request.getNotes())
            .registrationDate(LocalDate.now())
            .build();

        Collateral savedCollateral = collateralRepository.save(collateral);
        log.info("Successfully created collateral: {} for loan: {}", 
                savedCollateral.getCollateralNumber(), request.getLoanId());
        
        return collateralMapper.toDto(savedCollateral);
    }

    @Override
    @Transactional(readOnly = true)
    public CollateralDto getCollateral(Long collateralId) {
        log.info("Retrieving collateral with ID: {}", collateralId);
        
        Collateral collateral = collateralRepository.findById(collateralId)
            .orElseThrow(() -> new CollateralNotFoundException("Collateral not found with id: " + collateralId));
        
        return collateralMapper.toDto(collateral);
    }

    @Override
    @Transactional(readOnly = true)
    public CollateralDto getCollateralByNumber(String collateralNumber) {
        log.info("Retrieving collateral with number: {}", collateralNumber);
        
        Collateral collateral = collateralRepository.findByCollateralNumber(collateralNumber)
            .orElseThrow(() -> new CollateralNotFoundException("Collateral not found with number: " + collateralNumber));
        
        return collateralMapper.toDto(collateral);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollateralDto> getAllCollaterals(Pageable pageable) {
        log.info("Retrieving all collaterals with pagination");
        
        Page<Collateral> collaterals = collateralRepository.findAll(pageable);
        return collaterals.map(collateralMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollateralDto> getCollateralsByLoanId(Long loanId, Pageable pageable) {
        log.info("Retrieving collaterals for loan: {}", loanId);
        
        Page<Collateral> collaterals = collateralRepository.findByLoanId(loanId, pageable);
        return collaterals.map(collateralMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollateralDto> getCollateralsByOwnerId(Long ownerId, Pageable pageable) {
        log.info("Retrieving collaterals for owner: {}", ownerId);
        
        Page<Collateral> collaterals = collateralRepository.findByOwnerId(ownerId, pageable);
        return collaterals.map(collateralMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollateralDto> getCollateralsByStatus(CollateralStatus status, Pageable pageable) {
        log.info("Retrieving collaterals with status: {}", status);
        
        Page<Collateral> collaterals = collateralRepository.findByStatus(status, pageable);
        return collaterals.map(collateralMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollateralDto> getCollateralsByType(CollateralType collateralType, Pageable pageable) {
        log.info("Retrieving collaterals with type: {}", collateralType);
        
        Page<Collateral> collaterals = collateralRepository.findByCollateralType(collateralType, pageable);
        return collaterals.map(collateralMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollateralDto> getActiveCollateralsByLoanId(Long loanId) {
        log.info("Retrieving active collaterals for loan: {}", loanId);
        
        List<Collateral> collaterals = collateralRepository.findActiveCollateralsByLoanId(loanId);
        return collaterals.stream()
            .map(collateralMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public CollateralDto updateCollateralStatus(Long collateralId, CollateralStatus status) {
        log.info("Updating collateral status: {} to {}", collateralId, status);
        
        Collateral collateral = collateralRepository.findById(collateralId)
            .orElseThrow(() -> new CollateralNotFoundException("Collateral not found with id: " + collateralId));
        
        collateral.setStatus(status);
        
        Collateral savedCollateral = collateralRepository.save(collateral);
        log.info("Successfully updated collateral status: {}", savedCollateral.getCollateralNumber());
        
        return collateralMapper.toDto(savedCollateral);
    }

    @Override
    public CollateralDto releaseCollateral(Long collateralId, Long releasedBy) {
        log.info("Releasing collateral: {} by user: {}", collateralId, releasedBy);
        
        Collateral collateral = collateralRepository.findById(collateralId)
            .orElseThrow(() -> new CollateralNotFoundException("Collateral not found with id: " + collateralId));
        
        if (!collateral.isActive()) {
            throw new CollateralOperationException("Collateral is not active for release");
        }
        
        collateral.setStatus(CollateralStatus.RELEASED);
        collateral.setReleaseDate(LocalDate.now());
        collateral.setReleasedBy(releasedBy);
        
        Collateral savedCollateral = collateralRepository.save(collateral);
        log.info("Successfully released collateral: {}", savedCollateral.getCollateralNumber());
        
        return collateralMapper.toDto(savedCollateral);
    }

    @Override
    public CollateralDto updateAppraisalInfo(Long collateralId, BigDecimal appraisedValue, 
                                           LocalDate appraisalDate, String appraiserName) {
        log.info("Updating appraisal info for collateral: {}", collateralId);
        
        Collateral collateral = collateralRepository.findById(collateralId)
            .orElseThrow(() -> new CollateralNotFoundException("Collateral not found with id: " + collateralId));
        
        collateral.setAppraisedValue(appraisedValue);
        collateral.setAppraisalDate(appraisalDate);
        collateral.setAppraiserName(appraiserName);
        
        Collateral savedCollateral = collateralRepository.save(collateral);
        log.info("Successfully updated appraisal info for collateral: {}", savedCollateral.getCollateralNumber());
        
        return collateralMapper.toDto(savedCollateral);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCollateralValueByLoanId(Long loanId) {
        log.info("Calculating total collateral value for loan: {}", loanId);
        
        return collateralRepository.calculateTotalCollateralValueByLoanId(loanId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollateralDto> getCollateralsWithExpiredInsurance() {
        log.info("Retrieving collaterals with expired insurance");
        
        List<Collateral> collaterals = collateralRepository.findCollateralsWithExpiredInsurance(LocalDate.now());
        return collaterals.stream()
            .map(collateralMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollateralDto> searchCollaterals(Long loanId, Long ownerId, CollateralStatus status,
                                               CollateralType collateralType, LocalDate fromDate, 
                                               LocalDate toDate, Pageable pageable) {
        log.info("Searching collaterals with criteria - loanId: {}, ownerId: {}, status: {}, type: {}", 
                loanId, ownerId, status, collateralType);
        
        Page<Collateral> collaterals = collateralRepository.searchCollaterals(
            loanId, ownerId, status, collateralType, fromDate, toDate, pageable);
        return collaterals.map(collateralMapper::toDto);
    }

    @Override
    public String generateCollateralNumber(CollateralType collateralType) {
        String prefix = getCollateralTypePrefix(collateralType);
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String random = String.format("%03d", (int) (Math.random() * 1000));
        
        return prefix + timestamp + random;
    }

    private String getCollateralTypePrefix(CollateralType collateralType) {
        switch (collateralType) {
            case REAL_ESTATE:
                return "RE";
            case VEHICLE:
                return "VE";
            case EQUIPMENT:
                return "EQ";
            case INVENTORY:
                return "IN";
            case SECURITIES:
                return "SE";
            case CASH_DEPOSIT:
                return "CD";
            case INSURANCE_POLICY:
                return "IP";
            case GOLD_JEWELRY:
                return "GJ";
            case LAND:
                return "LA";
            case BUILDING:
                return "BU";
            case MACHINERY:
                return "MA";
            case ACCOUNTS_RECEIVABLE:
                return "AR";
            default:
                return "OT";
        }
    }
} 