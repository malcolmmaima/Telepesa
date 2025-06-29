package com.maelcolium.telepesa.loan.mapper;

import com.maelcolium.telepesa.loan.dto.CollateralDto;
import com.maelcolium.telepesa.loan.model.Collateral;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

/**
 * Mapper for converting between Collateral entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface CollateralMapper {

    @Mapping(target = "currentValue", source = ".", qualifiedByName = "calculateCurrentValue")
    @Mapping(target = "isActive", source = ".", qualifiedByName = "calculateIsActive")
    @Mapping(target = "isReleased", source = ".", qualifiedByName = "calculateIsReleased")
    @Mapping(target = "isInsuranceExpired", source = ".", qualifiedByName = "calculateIsInsuranceExpired")
    CollateralDto toDto(Collateral collateral);

    @Named("calculateCurrentValue")
    default BigDecimal calculateCurrentValue(Collateral collateral) {
        return collateral.getCurrentValue();
    }

    @Named("calculateIsActive")
    default Boolean calculateIsActive(Collateral collateral) {
        return collateral.isActive();
    }

    @Named("calculateIsReleased")
    default Boolean calculateIsReleased(Collateral collateral) {
        return collateral.isReleased();
    }

    @Named("calculateIsInsuranceExpired")
    default Boolean calculateIsInsuranceExpired(Collateral collateral) {
        return collateral.isInsuranceExpired();
    }
} 