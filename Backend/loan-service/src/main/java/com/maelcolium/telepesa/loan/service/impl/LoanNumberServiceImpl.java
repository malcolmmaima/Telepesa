package com.maelcolium.telepesa.loan.service.impl;

import com.maelcolium.telepesa.loan.repository.LoanRepository;
import com.maelcolium.telepesa.loan.service.LoanNumberService;
import com.maelcolium.telepesa.models.enums.LoanType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of LoanNumberService
 */
@Service
@Slf4j
public class LoanNumberServiceImpl implements LoanNumberService {

    private final LoanRepository loanRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public LoanNumberServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public String generateLoanNumber(LoanType loanType) {
        String prefix = getLoanTypePrefix(loanType);
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        
        String loanNumber;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
            loanNumber = prefix + dateStr + randomSuffix;
            attempts++;
            
            if (attempts >= maxAttempts) {
                log.error("Failed to generate unique loan number after {} attempts", maxAttempts);
                throw new RuntimeException("Unable to generate unique loan number");
            }
            
        } while (loanRepository.existsByLoanNumber(loanNumber));
        
        log.info("Generated loan number: {} for loan type: {}", loanNumber, loanType);
        return loanNumber;
    }

    private String getLoanTypePrefix(LoanType loanType) {
        return switch (loanType) {
            case PERSONAL -> "PL";
            case BUSINESS -> "BL";
            case MORTGAGE -> "ML";
            case AUTO -> "AL";
            case EDUCATION -> "EL";
            default -> "LN";
        };
    }
}
