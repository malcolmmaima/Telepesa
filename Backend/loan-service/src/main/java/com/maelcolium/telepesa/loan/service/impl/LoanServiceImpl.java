package com.maelcolium.telepesa.loan.service.impl;

import com.maelcolium.telepesa.models.request.CreateLoanRequest;
import com.maelcolium.telepesa.models.dto.LoanDto;
import com.maelcolium.telepesa.models.dto.LoanProductDto;
import com.maelcolium.telepesa.loan.exception.LoanNotFoundException;
import com.maelcolium.telepesa.loan.exception.LoanOperationException;
import com.maelcolium.telepesa.loan.mapper.LoanMapper;
import com.maelcolium.telepesa.loan.model.Loan;
import com.maelcolium.telepesa.loan.repository.LoanRepository;
import com.maelcolium.telepesa.loan.service.LoanNumberService;
import com.maelcolium.telepesa.loan.service.LoanService;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of LoanService with comprehensive caching
 */
@Service
@Transactional
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final LoanNumberService loanNumberService;

    public LoanServiceImpl(LoanRepository loanRepository, 
                          LoanMapper loanMapper,
                          LoanNumberService loanNumberService) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.loanNumberService = loanNumberService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanProductDto> getAllLoanProducts() {
        log.info("Retrieving all loan products");
        
        // Return sample loan products - in production, this would come from database
        return List.of(
            LoanProductDto.builder()
                .id(1L)
                .name("Personal Loan")
                .loanType(LoanType.PERSONAL)
                .minAmount(new BigDecimal("10000"))
                .maxAmount(new BigDecimal("500000"))
                .minInterestRate(new BigDecimal("12.0"))
                .maxInterestRate(new BigDecimal("18.0"))
                .minTermMonths(6)
                .maxTermMonths(36)
                .description("Quick personal loans for your immediate needs")
                .requirements(List.of("Valid ID", "Proof of income", "Bank statements"))
                .features(List.of("Quick approval", "Flexible terms", "No collateral required"))
                .isActive(true)
                .currency("KES")
                .build(),
            LoanProductDto.builder()
                .id(2L)
                .name("Business Loan")
                .loanType(LoanType.BUSINESS)
                .minAmount(new BigDecimal("50000"))
                .maxAmount(new BigDecimal("2000000"))
                .minInterestRate(new BigDecimal("14.0"))
                .maxInterestRate(new BigDecimal("20.0"))
                .minTermMonths(12)
                .maxTermMonths(60)
                .description("Grow your business with our competitive business loans")
                .requirements(List.of("Business registration", "Financial statements", "Business plan"))
                .features(List.of("Higher amounts", "Longer terms", "Business support"))
                .isActive(true)
                .currency("KES")
                .build(),
            LoanProductDto.builder()
                .id(3L)
                .name("Emergency Loan")
                .loanType(LoanType.EMERGENCY)
                .minAmount(new BigDecimal("5000"))
                .maxAmount(new BigDecimal("100000"))
                .minInterestRate(new BigDecimal("10.0"))
                .maxInterestRate(new BigDecimal("15.0"))
                .minTermMonths(3)
                .maxTermMonths(12)
                .description("Fast cash for unexpected expenses")
                .requirements(List.of("Valid ID", "Active account"))
                .features(List.of("Same day approval", "Quick disbursement", "Lower rates"))
                .isActive(true)
                .currency("KES")
                .build()
        );
    }

    @Override
    public LoanDto createLoan(CreateLoanRequest request) {
        log.info("Creating loan application for user: {} with amount: {}", 
                request.getUserId(), request.getPrincipalAmount());

        // Generate unique loan number
        String loanNumber = loanNumberService.generateLoanNumber(request.getLoanType());

        // Calculate monthly payment
        BigDecimal monthlyPayment = calculateMonthlyPayment(
            request.getPrincipalAmount(), 
            request.getInterestRate(), 
            request.getTermMonths()
        );

        // Generate account number if not provided
        String accountNumber = request.getAccountNumber();
        if (accountNumber == null && request.getAccountId() != null) {
            accountNumber = "ACC" + String.format("%06d", request.getAccountId());
        }
        
        // Create loan entity
        Loan loan = Loan.builder()
            .loanNumber(loanNumber)
            .userId(request.getUserId())
            .accountNumber(accountNumber)
            .loanType(request.getLoanType())
            .status(LoanStatus.PENDING)
            .principalAmount(request.getPrincipalAmount())
            .interestRate(request.getInterestRate())
            .termMonths(request.getTermMonths())
            .monthlyPayment(monthlyPayment)
            .outstandingBalance(request.getPrincipalAmount())
            .totalPaid(BigDecimal.ZERO)
            .purpose(request.getPurpose())
            .monthlyIncome(request.getMonthlyIncome())
            .notes(request.getNotes())
            .applicationDate(LocalDate.now())
            .build();

        Loan savedLoan = loanRepository.save(loan);
        log.info("Successfully created loan: {} for user: {}", savedLoan.getLoanNumber(), request.getUserId());
        
        return loanMapper.toDto(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDto getLoan(Long loanId) {
        log.info("Retrieving loan with ID: {}", loanId);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        
        return loanMapper.toDto(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDto getLoanByNumber(String loanNumber) {
        log.info("Retrieving loan with number: {}", loanNumber);
        
        Loan loan = loanRepository.findByLoanNumber(loanNumber)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with number: " + loanNumber));
        
        return loanMapper.toDto(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDto> getAllLoans(Pageable pageable) {
        log.info("Retrieving all loans with pagination");
        
        Page<Loan> loans = loanRepository.findAll(pageable);
        return loans.map(loanMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDto> getLoansByUserId(Long userId, Pageable pageable) {
        log.info("Retrieving loans for user: {}", userId);
        
        // Ensure pageable is not null to avoid SpEL cache errors
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        }
        
        Page<Loan> loans = loanRepository.findByUserId(userId, pageable);
        return loans.map(loanMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDto> getUserLoansWithPagination(Long userId, Pageable pageable) {
        // Ensure pageable is not null to avoid SpEL cache errors
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        }
        
        log.info("Getting user loans with pagination for user: {}, page: {}, size: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Loan> loans = loanRepository.findByUserId(userId, pageable);
            return loans.map(loanMapper::toDto);
        } catch (Exception e) {
            log.error("Error retrieving user loans: {}", e.getMessage(), e);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDto> getLoansByStatus(LoanStatus status, Pageable pageable) {
        log.info("Retrieving loans with status: {}", status);
        
        // Ensure pageable is not null to avoid SpEL cache errors
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        }
        
        Page<Loan> loans = loanRepository.findByStatus(status, pageable);
        return loans.map(loanMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDto> getLoansByType(LoanType loanType, Pageable pageable) {
        log.info("Retrieving loans with type: {}", loanType);
        
        // Ensure pageable is not null to avoid SpEL cache errors
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        }
        
        Page<Loan> loans = loanRepository.findByLoanType(loanType, pageable);
        return loans.map(loanMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDto> getActiveLoansByUserId(Long userId) {
        log.info("Retrieving active loans for user: {}", userId);
        
        List<Loan> loans = loanRepository.findActiveLoansByUserId(userId);
        return loans.stream()
            .map(loanMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public LoanDto approveLoan(Long loanId, Long approvedBy) {
        log.info("Approving loan: {} by user: {}", loanId, approvedBy);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new LoanOperationException("Loan is not in pending status for approval");
        }
        
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(approvedBy);
        loan.setApprovalDate(LocalDate.now());
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Successfully approved loan: {}", savedLoan.getLoanNumber());
        
        return loanMapper.toDto(savedLoan);
    }

    @Override
    public LoanDto rejectLoan(Long loanId, String rejectionReason) {
        log.info("Rejecting loan: {} with reason: {}", loanId, rejectionReason);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new LoanOperationException("Loan is not in pending status for rejection");
        }
        
        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(rejectionReason);
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Successfully rejected loan: {}", savedLoan.getLoanNumber());
        
        return loanMapper.toDto(savedLoan);
    }

    @Override
    public LoanDto disburseLoan(Long loanId) {
        log.info("Disbursing loan: {}", loanId);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new LoanOperationException("Loan must be approved before disbursement");
        }
        
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Successfully disbursed loan: {}", savedLoan.getLoanNumber());
        
        return loanMapper.toDto(savedLoan);
    }

    @Override
    public LoanDto makePayment(Long loanId, BigDecimal amount, String paymentMethod) {
        log.info("Making payment of {} for loan: {}", amount, loanId);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        
        if (!loan.isActive()) {
            throw new LoanOperationException("Cannot make payment on inactive loan");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanOperationException("Payment amount must be positive");
        }
        
        // Update loan balances
        BigDecimal newOutstandingBalance = loan.getOutstandingBalance().subtract(amount);
        BigDecimal newTotalPaid = loan.getTotalPaid().add(amount);
        
        loan.setOutstandingBalance(newOutstandingBalance.max(BigDecimal.ZERO));
        loan.setTotalPaid(newTotalPaid);
        
        // Check if loan is paid off
        if (newOutstandingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.PAID_OFF);
            loan.setNextPaymentDate(null);
        } else {
            loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        }
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Successfully processed payment for loan: {}", savedLoan.getLoanNumber());
        
        return loanMapper.toDto(savedLoan);
    }

    @Override
    public BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal interestRate, Integer termMonths) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0 || termMonths <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }
        
        // Monthly interest rate
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(1200), 6, RoundingMode.HALF_UP);
        
        // (1 + r)^n
        BigDecimal onePlusRatePowN = monthlyRate.add(BigDecimal.ONE).pow(termMonths);
        
        // Monthly payment = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowN);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstandingBalance(Long userId) {
        log.info("Calculating total outstanding balance for user: {}", userId);
        
        return loanRepository.calculateTotalOutstandingBalanceByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDto> getOverdueLoans() {
        log.info("Retrieving overdue loans");
        
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDate.now());
        return overdueLoans.stream()
            .map(loanMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDto> searchLoans(Long userId, LoanStatus status, LoanType loanType, 
                                    LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        log.info("Searching loans with criteria - userId: {}, status: {}, type: {}", userId, status, loanType);
        
        Page<Loan> loans = loanRepository.searchLoans(userId, status, loanType, fromDate, toDate, pageable);
        return loans.map(loanMapper::toDto);
    }

    @Override
    public LoanDto updateLoanStatus(Long loanId, LoanStatus status) {
        log.info("Updating loan status: {} to {}", loanId, status);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        
        loan.setStatus(status);
        Loan savedLoan = loanRepository.save(loan);
        
        log.info("Successfully updated loan status: {}", savedLoan.getLoanNumber());
        return loanMapper.toDto(savedLoan);
    }
}
