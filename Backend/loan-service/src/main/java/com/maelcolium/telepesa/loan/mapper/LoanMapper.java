package com.maelcolium.telepesa.loan.mapper;

import com.maelcolium.telepesa.loan.dto.LoanDto;
import com.maelcolium.telepesa.loan.model.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for converting between Loan entities and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanMapper {

    /**
     * Convert Loan entity to LoanDto
     */
    LoanDto toDto(Loan loan);

    /**
     * Convert LoanDto to Loan entity
     */
    Loan toEntity(LoanDto loanDto);
}
