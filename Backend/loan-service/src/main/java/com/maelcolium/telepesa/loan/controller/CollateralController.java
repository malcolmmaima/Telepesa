package com.maelcolium.telepesa.loan.controller;

import com.maelcolium.telepesa.loan.dto.CollateralDto;
import com.maelcolium.telepesa.loan.dto.CreateCollateralRequest;
import com.maelcolium.telepesa.loan.service.CollateralService;
import com.maelcolium.telepesa.models.enums.CollateralStatus;
import com.maelcolium.telepesa.models.enums.CollateralType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for collateral operations
 */
@RestController
@RequestMapping("/api/v1/collaterals")
@Validated
@Slf4j
@Tag(name = "Collateral Management", description = "APIs for managing loan collaterals")
public class CollateralController {

    private final CollateralService collateralService;

    public CollateralController(CollateralService collateralService) {
        this.collateralService = collateralService;
    }

    @Operation(
        summary = "Create collateral",
        description = "Register a new collateral for a loan",
        responses = {
            @ApiResponse(responseCode = "201", description = "Collateral created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
        }
    )
    @PostMapping
    public ResponseEntity<CollateralDto> createCollateral(@Valid @RequestBody CreateCollateralRequest request) {
        log.info("Creating collateral for loan: {}", request.getLoanId());
        CollateralDto collateral = collateralService.createCollateral(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(collateral);
    }

    @Operation(
        summary = "Get collateral by ID",
        description = "Retrieve collateral details by collateral ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collateral found"),
            @ApiResponse(responseCode = "404", description = "Collateral not found")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CollateralDto> getCollateral(
        @Parameter(description = "Collateral ID", example = "1")
        @PathVariable Long id) {
        
        CollateralDto collateral = collateralService.getCollateral(id);
        return ResponseEntity.ok(collateral);
    }

    @Operation(
        summary = "Get collateral by number",
        description = "Retrieve collateral details by collateral number",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collateral found"),
            @ApiResponse(responseCode = "404", description = "Collateral not found")
        }
    )
    @GetMapping("/number/{collateralNumber}")
    public ResponseEntity<CollateralDto> getCollateralByNumber(
        @Parameter(description = "Collateral number", example = "RE202412001234")
        @PathVariable String collateralNumber) {
        
        CollateralDto collateral = collateralService.getCollateralByNumber(collateralNumber);
        return ResponseEntity.ok(collateral);
    }

    @Operation(
        summary = "Get all collaterals",
        description = "Retrieve all collaterals with pagination",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collaterals retrieved successfully")
        }
    )
    @GetMapping
    public ResponseEntity<Page<CollateralDto>> getAllCollaterals(
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        
        @Parameter(description = "Sort field", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sortBy,
        
        @Parameter(description = "Sort direction", example = "desc")
        @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CollateralDto> collaterals = collateralService.getAllCollaterals(pageable);
        return ResponseEntity.ok(collaterals);
    }

    @Operation(
        summary = "Get collaterals by loan",
        description = "Retrieve all collaterals for a specific loan",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan collaterals retrieved successfully")
        }
    )
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<Page<CollateralDto>> getCollateralsByLoanId(
        @Parameter(description = "Loan ID", example = "100")
        @PathVariable Long loanId,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CollateralDto> collaterals = collateralService.getCollateralsByLoanId(loanId, pageable);
        return ResponseEntity.ok(collaterals);
    }

    @Operation(
        summary = "Get active collaterals by loan",
        description = "Retrieve all active collaterals for a specific loan",
        responses = {
            @ApiResponse(responseCode = "200", description = "Active collaterals retrieved successfully")
        }
    )
    @GetMapping("/loan/{loanId}/active")
    public ResponseEntity<List<CollateralDto>> getActiveCollateralsByLoanId(
        @Parameter(description = "Loan ID", example = "100")
        @PathVariable Long loanId) {
        
        List<CollateralDto> collaterals = collateralService.getActiveCollateralsByLoanId(loanId);
        return ResponseEntity.ok(collaterals);
    }

    @Operation(
        summary = "Get collaterals by owner",
        description = "Retrieve all collaterals for a specific owner",
        responses = {
            @ApiResponse(responseCode = "200", description = "Owner collaterals retrieved successfully")
        }
    )
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<CollateralDto>> getCollateralsByOwnerId(
        @Parameter(description = "Owner ID", example = "200")
        @PathVariable Long ownerId,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CollateralDto> collaterals = collateralService.getCollateralsByOwnerId(ownerId, pageable);
        return ResponseEntity.ok(collaterals);
    }

    @Operation(
        summary = "Get collaterals by status",
        description = "Retrieve collaterals filtered by status",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collaterals retrieved successfully")
        }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<CollateralDto>> getCollateralsByStatus(
        @Parameter(description = "Collateral status", example = "ACTIVE")
        @PathVariable CollateralStatus status,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CollateralDto> collaterals = collateralService.getCollateralsByStatus(status, pageable);
        return ResponseEntity.ok(collaterals);
    }

    @Operation(
        summary = "Get collaterals by type",
        description = "Retrieve collaterals filtered by type",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collaterals retrieved successfully")
        }
    )
    @GetMapping("/type/{collateralType}")
    public ResponseEntity<Page<CollateralDto>> getCollateralsByType(
        @Parameter(description = "Collateral type", example = "REAL_ESTATE")
        @PathVariable CollateralType collateralType,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CollateralDto> collaterals = collateralService.getCollateralsByType(collateralType, pageable);
        return ResponseEntity.ok(collaterals);
    }

    @Operation(
        summary = "Update collateral status",
        description = "Update the status of a collateral",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collateral status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Collateral not found")
        }
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<CollateralDto> updateCollateralStatus(
        @Parameter(description = "Collateral ID", example = "1")
        @PathVariable Long id,
        
        @Parameter(description = "New status", example = "ACTIVE")
        @RequestParam CollateralStatus status) {
        
        CollateralDto collateral = collateralService.updateCollateralStatus(id, status);
        return ResponseEntity.ok(collateral);
    }

    @Operation(
        summary = "Release collateral",
        description = "Release a collateral (mark as released)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collateral released successfully"),
            @ApiResponse(responseCode = "400", description = "Collateral cannot be released"),
            @ApiResponse(responseCode = "404", description = "Collateral not found")
        }
    )
    @PostMapping("/{id}/release")
    public ResponseEntity<CollateralDto> releaseCollateral(
        @Parameter(description = "Collateral ID", example = "1")
        @PathVariable Long id,
        
        @Parameter(description = "User ID who is releasing", example = "300")
        @RequestParam Long releasedBy) {
        
        CollateralDto collateral = collateralService.releaseCollateral(id, releasedBy);
        return ResponseEntity.ok(collateral);
    }

    @Operation(
        summary = "Update appraisal information",
        description = "Update appraisal details for a collateral",
        responses = {
            @ApiResponse(responseCode = "200", description = "Appraisal info updated successfully"),
            @ApiResponse(responseCode = "404", description = "Collateral not found")
        }
    )
    @PutMapping("/{id}/appraisal")
    public ResponseEntity<CollateralDto> updateAppraisalInfo(
        @Parameter(description = "Collateral ID", example = "1")
        @PathVariable Long id,
        
        @Parameter(description = "Appraised value", example = "4800000.00")
        @RequestParam BigDecimal appraisedValue,
        
        @Parameter(description = "Appraisal date", example = "2024-01-15")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appraisalDate,
        
        @Parameter(description = "Appraiser name", example = "John Doe Appraisals")
        @RequestParam String appraiserName) {
        
        CollateralDto collateral = collateralService.updateAppraisalInfo(id, appraisedValue, appraisalDate, appraiserName);
        return ResponseEntity.ok(collateral);
    }

    @Operation(
        summary = "Get total collateral value",
        description = "Get total collateral value for a loan",
        responses = {
            @ApiResponse(responseCode = "200", description = "Total value retrieved successfully")
        }
    )
    @GetMapping("/loan/{loanId}/total-value")
    public ResponseEntity<BigDecimal> getTotalCollateralValueByLoanId(
        @Parameter(description = "Loan ID", example = "100")
        @PathVariable Long loanId) {
        
        BigDecimal totalValue = collateralService.getTotalCollateralValueByLoanId(loanId);
        return ResponseEntity.ok(totalValue);
    }

    @Operation(
        summary = "Get collaterals with expired insurance",
        description = "Retrieve all collaterals with expired insurance",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collaterals with expired insurance retrieved successfully")
        }
    )
    @GetMapping("/expired-insurance")
    public ResponseEntity<List<CollateralDto>> getCollateralsWithExpiredInsurance() {
        List<CollateralDto> collaterals = collateralService.getCollateralsWithExpiredInsurance();
        return ResponseEntity.ok(collaterals);
    }

    @Operation(
        summary = "Search collaterals",
        description = "Search collaterals with multiple criteria",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
        }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<CollateralDto>> searchCollaterals(
        @RequestParam(required = false) Long loanId,
        @RequestParam(required = false) Long ownerId,
        @RequestParam(required = false) CollateralStatus status,
        @RequestParam(required = false) CollateralType collateralType,
        
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,
        
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CollateralDto> collaterals = collateralService.searchCollaterals(
            loanId, ownerId, status, collateralType, fromDate, toDate, pageable);
        return ResponseEntity.ok(collaterals);
    }
} 