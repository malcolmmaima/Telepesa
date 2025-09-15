package com.maelcolium.telepesa.loan.controller;

import com.maelcolium.telepesa.loan.mapper.LoanMapper;
import com.maelcolium.telepesa.loan.model.Loan;
import com.maelcolium.telepesa.loan.repository.LoanRepository;
import com.maelcolium.telepesa.loan.service.LoanService;
import com.maelcolium.telepesa.models.dto.LoanDto;
import com.maelcolium.telepesa.models.dto.LoanProductDto;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import com.maelcolium.telepesa.models.request.CreateLoanRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.Map;
import java.util.Collections;
import jakarta.validation.Valid;

/**
 * REST controller for loan operations
 */
@RestController
@RequestMapping("/api/v1/loans")
@Validated
@Slf4j
@Tag(name = "Loan Management", description = "APIs for managing loans and loan applications")
public class LoanController {

    private final LoanService loanService;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    public LoanController(LoanService loanService, LoanRepository loanRepository, LoanMapper loanMapper) {
        this.loanService = loanService;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    @Operation(
        summary = "Get loan products",
        description = "Retrieve all available loan products",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan products retrieved successfully")
        }
    )
    @GetMapping("/products")
    public ResponseEntity<List<LoanProductDto>> getAllLoanProducts() {
        log.info("Retrieving all loan products");
        List<LoanProductDto> products = loanService.getAllLoanProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/simple-test")
    public ResponseEntity<String> simpleTest() {
        return ResponseEntity.ok("Test endpoint working");
    }

    @PostMapping("/get-user-loans")
    public ResponseEntity<Page<LoanDto>> getUserLoansPost(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        int page = request.containsKey("page") ? Integer.parseInt(request.get("page").toString()) : 0;
        int size = request.containsKey("size") ? Integer.parseInt(request.get("size").toString()) : 20;
        
        log.info("Getting loans for user: {} with page: {}, size: {}", userId, page, size);
        
        try {
            // Use service method without cache annotations
            Page<LoanDto> loans = loanService.getUserLoansWithPagination(userId, page, size);
            return ResponseEntity.ok(loans);
            
        } catch (Exception e) {
            log.error("Error getting user loans: {}", e.getMessage(), e);
            Page<LoanDto> emptyPage = new PageImpl<>(Collections.emptyList(), 
                PageRequest.of(page, size), 0);
            return ResponseEntity.ok(emptyPage);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<LoanDto>> searchUserLoans(
            @RequestParam("userId") Long userId) {
        
        log.info("Searching loans for user: {}", userId);
        
        try {
            // Use simple findAll without pagination to avoid any cache issues
            List<Loan> allLoans = loanRepository.findAll();
            List<Loan> userLoans = allLoans.stream()
                .filter(loan -> loan.getUserId().equals(userId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(20)
                .collect(java.util.stream.Collectors.toList());
            
            List<LoanDto> loanDtos = userLoans.stream()
                .map(loanMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
            
            log.info("Successfully retrieved {} loans for user: {}", loanDtos.size(), userId);
            return ResponseEntity.ok(loanDtos);
            
        } catch (Exception e) {
            log.error("Error searching user loans: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(
        summary = "Create loan application",
        description = "Submit a new loan application",
        responses = {
            @ApiResponse(responseCode = "201", description = "Loan application created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
        }
    )
    @PostMapping
    public ResponseEntity<LoanDto> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("Creating loan application for user: {}", request.getUserId());
        LoanDto loan = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @Operation(
        summary = "Get loan by ID",
        description = "Retrieve loan details by loan ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
        }
    )
    @GetMapping("/loan/{id}")
    public ResponseEntity<LoanDto> getLoan(
        @Parameter(description = "Loan ID", example = "1")
        @PathVariable("id") Long id) {
        
        LoanDto loan = loanService.getLoan(id);
        return ResponseEntity.ok(loan);
    }

    @Operation(
        summary = "Get loan by number",
        description = "Retrieve loan details by loan number",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
        }
    )
    @GetMapping("/number/{loanNumber}")
    public ResponseEntity<LoanDto> getLoanByNumber(
        @Parameter(description = "Loan number", example = "PL202412001234")
        @PathVariable("loanNumber") String loanNumber) {
        
        LoanDto loan = loanService.getLoanByNumber(loanNumber);
        return ResponseEntity.ok(loan);
    }

    @Operation(
        summary = "Get all loans",
        description = "Retrieve all loans with pagination",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
        }
    )
    @GetMapping
    public ResponseEntity<Page<LoanDto>> getAllLoans(
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(name = "page", defaultValue = "0") int page,
        
        @Parameter(description = "Page size", example = "20")
        @RequestParam(name = "size", defaultValue = "20") int size,
        
        @Parameter(description = "Sort field", example = "createdAt")
        @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
        
        @Parameter(description = "Sort direction", example = "desc")
        @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<LoanDto> loans = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(loans);
    }

    @Operation(
        summary = "Get loans by user",
        description = "Retrieve all loans for a specific user",
        responses = {
            @ApiResponse(responseCode = "200", description = "User loans retrieved successfully")
        }
    )
    @GetMapping("/by-user-id")
    public ResponseEntity<Page<LoanDto>> getUserLoans(
        @Parameter(description = "User ID", example = "100")
        @RequestParam("userId") Long userId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "sort", defaultValue = "createdAt") String sortBy,
        @RequestParam(name = "direction", defaultValue = "desc") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<LoanDto> loans = loanService.getUserLoansWithPagination(userId, page, size);
        return ResponseEntity.ok(loans);
    }

    @Operation(
        summary = "Get user loans - clean endpoint",
        description = "Retrieve loans for a user without cache complications"
    )
    @GetMapping("/user/{userId}/clean")
    public ResponseEntity<Page<LoanDto>> getUserLoansClean(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        log.info("Clean endpoint: Retrieving loans for user: {} with page: {}, size: {}", userId, page, size);
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Loan> loans = loanRepository.findByUserId(userId, pageable);
            Page<LoanDto> loanDtos = loans.map(loanMapper::toDto);
            
            log.info("Successfully retrieved {} loans for user: {}", loanDtos.getTotalElements(), userId);
            return ResponseEntity.ok(loanDtos);
            
        } catch (Exception e) {
            log.error("Error retrieving loans for user {}: {}", userId, e.getMessage(), e);
            // Return empty page on error
            Page<LoanDto> emptyPage = new PageImpl<>(Collections.emptyList());
            return ResponseEntity.ok(emptyPage);
        }
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<LoanDto>> getUserLoansPaginated(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        log.info("Getting paginated loans for user: {} with page: {}, size: {}", userId, page, size);
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        
        try {
            // Direct repository call to bypass any cache issues
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Loan> loans = loanRepository.findByUserId(userId, pageable);
            Page<LoanDto> loanDtos = loans.map(loanMapper::toDto);
            
            log.info("Successfully retrieved {} loans for user: {}", loanDtos.getTotalElements(), userId);
            return ResponseEntity.ok(loanDtos);
            
        } catch (Exception e) {
            log.error("Error getting paginated loans for user {}: {}", userId, e.getMessage(), e);
            // Return empty page on error
            Page<LoanDto> emptyPage = new PageImpl<>(Collections.emptyList(), 
                PageRequest.of(page, size), 0);
            return ResponseEntity.ok(emptyPage);
        }
    }

    @Operation(
        summary = "Get active loans by user",
        description = "Retrieve all active loans for a specific user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Active loans retrieved successfully")
        }
    )
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<LoanDto>> getActiveLoansByUserId(
        @Parameter(description = "User ID", example = "100")
        @PathVariable("userId") Long userId) {
        
        List<LoanDto> loans = loanService.getActiveLoansByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @Operation(
        summary = "Get loans by status",
        description = "Retrieve loans filtered by status",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
        }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LoanDto>> getLoansByStatus(
        @Parameter(description = "Loan status", example = "ACTIVE")
        @PathVariable("status") LoanStatus status,
        
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LoanDto> loans = loanService.getLoansByStatus(status, pageable);
        return ResponseEntity.ok(loans);
    }

    @Operation(
        summary = "Approve loan",
        description = "Approve a pending loan application",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan approved successfully"),
            @ApiResponse(responseCode = "400", description = "Loan cannot be approved"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
        }
    )
    @PostMapping("/{id}/approve")
    public ResponseEntity<LoanDto> approveLoan(
        @Parameter(description = "Loan ID", example = "1")
        @PathVariable("id") Long id,
        
        @Parameter(description = "Approver user ID", example = "200")
        @RequestParam(name = "approvedBy") Long approvedBy) {
        
        LoanDto loan = loanService.approveLoan(id, approvedBy);
        return ResponseEntity.ok(loan);
    }

    @Operation(
        summary = "Reject loan",
        description = "Reject a pending loan application",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Loan cannot be rejected"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
        }
    )
    @PostMapping("/{id}/reject")
    public ResponseEntity<LoanDto> rejectLoan(
        @Parameter(description = "Loan ID", example = "1")
        @PathVariable("id") Long id,
        
        @Parameter(description = "Rejection reason", example = "Insufficient credit score")
        @RequestParam(name = "rejectionReason") String rejectionReason) {
        
        LoanDto loan = loanService.rejectLoan(id, rejectionReason);
        return ResponseEntity.ok(loan);
    }

    @Operation(
        summary = "Disburse loan",
        description = "Disburse an approved loan",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan disbursed successfully"),
            @ApiResponse(responseCode = "400", description = "Loan cannot be disbursed"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
        }
    )
    @PostMapping("/{id}/disburse")
    public ResponseEntity<LoanDto> disburseLoan(
        @Parameter(description = "Loan ID", example = "1")
        @PathVariable("id") Long id) {
        
        LoanDto loan = loanService.disburseLoan(id);
        return ResponseEntity.ok(loan);
    }

    @Operation(
        summary = "Make loan payment",
        description = "Make a payment towards a loan",
        responses = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
        }
    )
    @PostMapping("/{id}/payment")
    public ResponseEntity<LoanDto> makePayment(
        @Parameter(description = "Loan ID", example = "1")
        @PathVariable("id") Long id,
        
        @Parameter(description = "Payment amount", example = "2347.50")
        @RequestParam(name = "amount") BigDecimal amount,
        
        @Parameter(description = "Payment method", example = "BANK_TRANSFER")
        @RequestParam(name = "paymentMethod", defaultValue = "BANK_TRANSFER") String paymentMethod) {
        
        LoanDto loan = loanService.makePayment(id, amount, paymentMethod);
        return ResponseEntity.ok(loan);
    }

    @Operation(
        summary = "Get total outstanding balance",
        description = "Get total outstanding balance for a user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully")
        }
    )
    @GetMapping("/user/{userId}/outstanding-balance")
    public ResponseEntity<BigDecimal> getTotalOutstandingBalance(
        @Parameter(description = "User ID", example = "100")
        @PathVariable("userId") Long userId) {
        
        BigDecimal balance = loanService.getTotalOutstandingBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @Operation(
        summary = "Get overdue loans",
        description = "Retrieve all overdue loans",
        responses = {
            @ApiResponse(responseCode = "200", description = "Overdue loans retrieved successfully")
        }
    )
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanDto>> getOverdueLoans() {
        List<LoanDto> loans = loanService.getOverdueLoans();
        return ResponseEntity.ok(loans);
    }

    @Operation(
        summary = "Search loans",
        description = "Search loans with multiple criteria",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
        }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<LoanDto>> searchLoans(
        @RequestParam(name = "userId", required = false) Long userId,
        @RequestParam(name = "status", required = false) LoanStatus status,
        @RequestParam(name = "loanType", required = false) LoanType loanType,
        
        @RequestParam(name = "fromDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,
        
        @RequestParam(name = "toDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate,
        
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LoanDto> loans = loanService.searchLoans(userId, status, loanType, fromDate, toDate, pageable);
        return ResponseEntity.ok(loans);
    }

    @Operation(
        summary = "Get loans by type",
        description = "Retrieve loans filtered by loan type",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
        }
    )
    @GetMapping("/type/{loanType}")
    public ResponseEntity<Page<LoanDto>> getLoansByType(
        @Parameter(description = "Loan type", example = "PERSONAL")
        @PathVariable("loanType") LoanType loanType,
        
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LoanDto> loans = loanService.getLoansByType(loanType, pageable);
        return ResponseEntity.ok(loans);
    }

    @Operation(
        summary = "Update loan status",
        description = "Update the status of a loan",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status update"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
        }
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<LoanDto> updateLoanStatus(
        @Parameter(description = "Loan ID", example = "1")
        @PathVariable("id") Long id,
        
        @Parameter(description = "New loan status", example = "ACTIVE")
        @RequestParam(name = "status") LoanStatus status) {
        
        LoanDto loan = loanService.updateLoanStatus(id, status);
        return ResponseEntity.ok(loan);
    }
}
