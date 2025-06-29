package com.maelcolium.telepesa.loan.controller;

import com.maelcolium.telepesa.loan.dto.CreateLoanRequest;
import com.maelcolium.telepesa.loan.dto.LoanDto;
import com.maelcolium.telepesa.loan.service.LoanService;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
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
 * REST controller for loan operations
 */
@RestController
@RequestMapping("/api/v1/loans")
@Validated
@Slf4j
@Tag(name = "Loan Management", description = "APIs for managing loans and loan applications")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
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
    @GetMapping("/{id}")
    public ResponseEntity<LoanDto> getLoan(
        @Parameter(description = "Loan ID", example = "1")
        @PathVariable Long id) {
        
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
        @PathVariable String loanNumber) {
        
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
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        
        @Parameter(description = "Sort field", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sortBy,
        
        @Parameter(description = "Sort direction", example = "desc")
        @RequestParam(defaultValue = "desc") String sortDir) {
        
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
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LoanDto>> getLoansByUserId(
        @Parameter(description = "User ID", example = "100")
        @PathVariable Long userId,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LoanDto> loans = loanService.getLoansByUserId(userId, pageable);
        return ResponseEntity.ok(loans);
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
        @PathVariable Long userId) {
        
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
        @PathVariable LoanStatus status,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
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
        @PathVariable Long id,
        
        @Parameter(description = "Approver user ID", example = "200")
        @RequestParam Long approvedBy) {
        
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
        @PathVariable Long id,
        
        @Parameter(description = "Rejection reason", example = "Insufficient credit score")
        @RequestParam String rejectionReason) {
        
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
        @PathVariable Long id) {
        
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
        @PathVariable Long id,
        
        @Parameter(description = "Payment amount", example = "2347.50")
        @RequestParam BigDecimal amount,
        
        @Parameter(description = "Payment method", example = "BANK_TRANSFER")
        @RequestParam(defaultValue = "BANK_TRANSFER") String paymentMethod) {
        
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
        @PathVariable Long userId) {
        
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
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) LoanStatus status,
        @RequestParam(required = false) LoanType loanType,
        
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,
        
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate,
        
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LoanDto> loans = loanService.searchLoans(userId, status, loanType, fromDate, toDate, pageable);
        return ResponseEntity.ok(loans);
    }
}
