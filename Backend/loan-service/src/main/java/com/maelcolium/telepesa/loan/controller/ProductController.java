package com.maelcolium.telepesa.loan.controller;

import com.maelcolium.telepesa.loan.dto.LoanProductDto;
import com.maelcolium.telepesa.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product operations
 * This controller provides a dedicated endpoint for loan products
 * to match frontend expectations at /api/v1/products
 */
@RestController
@RequestMapping("/api/v1/products")
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing loan products")
public class ProductController {

    private final LoanService loanService;

    public ProductController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Operation(
        summary = "Get loan products",
        description = "Retrieve all available loan products",
        responses = {
            @ApiResponse(responseCode = "200", description = "Loan products retrieved successfully")
        }
    )
    @GetMapping
    public ResponseEntity<List<LoanProductDto>> getLoanProducts() {
        log.info("Retrieving all loan products via products endpoint");
        List<LoanProductDto> products = loanService.getAllLoanProducts();
        return ResponseEntity.ok(products);
    }
}
