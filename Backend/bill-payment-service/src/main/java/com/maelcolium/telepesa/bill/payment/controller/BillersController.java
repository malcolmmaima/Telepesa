package com.maelcolium.telepesa.bill.payment.controller;

import com.maelcolium.telepesa.bill.payment.dto.BillerDto;
import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/billers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billers Management", description = "APIs for managing billing service providers")
public class BillersController {
    
    // Mock billers data - In production, this would come from a database
    private static final List<BillerDto> AVAILABLE_BILLERS = Arrays.asList(
        new BillerDto("kplc", "Kenya Power (KPLC)", "Utilities", "⚡", true, 
            "Account Number", "Enter your KPLC account number", 
            BillPayment.BillType.ELECTRICITY, "KPLC", true, "Pay electricity bills"),
            
        new BillerDto("nairobi-water", "Nairobi Water", "Utilities", "💧", true, 
            "Account Number", "Enter your water account number", 
            BillPayment.BillType.WATER, "NAIROBI_WATER", true, "Pay water bills"),
            
        new BillerDto("safaricom", "Safaricom Postpaid", "Telecom", "📱", true, 
            "Phone Number", "Enter phone number", 
            BillPayment.BillType.MOBILE_POSTPAID, "SAFARICOM", true, "Pay postpaid bills"),
            
        new BillerDto("airtel", "Airtel", "Telecom", "📶", true, 
            "Phone Number", "Enter phone number", 
            BillPayment.BillType.MOBILE_POSTPAID, "AIRTEL", true, "Pay Airtel postpaid bills"),
            
        new BillerDto("gotv", "GOtv", "Entertainment", "📺", true, 
            "Smart Card Number", "Enter smart card number", 
            BillPayment.BillType.TV_SUBSCRIPTION, "GOTV", true, "Pay GOtv subscription"),
            
        new BillerDto("dstv", "DStv", "Entertainment", "📡", true, 
            "Smart Card Number", "Enter smart card number", 
            BillPayment.BillType.TV_SUBSCRIPTION, "DSTV", true, "Pay DStv subscription"),
            
        new BillerDto("zuku", "Zuku", "Internet", "🌐", true, 
            "Account Number", "Enter account number", 
            BillPayment.BillType.INTERNET, "ZUKU", true, "Pay Zuku internet bills"),
            
        new BillerDto("liquid-telecom", "Liquid Telecom", "Internet", "📡", true, 
            "Account Number", "Enter account number", 
            BillPayment.BillType.INTERNET, "LIQUID_TELECOM", true, "Pay Liquid Telecom internet bills"),
            
        new BillerDto("nhif", "NHIF", "Insurance", "🏥", true, 
            "Member Number", "Enter NHIF member number", 
            BillPayment.BillType.INSURANCE, "NHIF", true, "Pay NHIF contributions"),
            
        new BillerDto("kra", "Kenya Revenue Authority", "Government", "🏛️", true, 
            "PIN Number", "Enter KRA PIN", 
            BillPayment.BillType.GOVERNMENT, "KRA", true, "Pay KRA taxes")
    );
    
    @GetMapping
    @Operation(summary = "Get all available billers")
    public ResponseEntity<List<BillerDto>> getAllBillers(
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) String category,
            @Parameter(description = "Filter by bill type")
            @RequestParam(required = false) BillPayment.BillType billType,
            @Parameter(description = "Search by name")
            @RequestParam(required = false) String search) {
        
        List<BillerDto> billers = AVAILABLE_BILLERS.stream()
            .filter(biller -> biller.isActive())
            .filter(biller -> category == null || biller.getCategory().equalsIgnoreCase(category))
            .filter(biller -> billType == null || biller.getBillType() == billType)
            .filter(biller -> search == null || 
                biller.getName().toLowerCase().contains(search.toLowerCase()) ||
                biller.getCategory().toLowerCase().contains(search.toLowerCase()))
            .collect(Collectors.toList());
        
        log.info("Returning {} billers", billers.size());
        return ResponseEntity.ok(billers);
    }
    
    @GetMapping("/{billerId}")
    @Operation(summary = "Get biller by ID")
    public ResponseEntity<BillerDto> getBillerById(
            @Parameter(description = "Biller ID", required = true)
            @PathVariable String billerId) {
        
        BillerDto biller = AVAILABLE_BILLERS.stream()
            .filter(b -> b.getId().equals(billerId) && b.isActive())
            .findFirst()
            .orElse(null);
        
        if (biller == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(biller);
    }
    
    @GetMapping("/categories")
    @Operation(summary = "Get all biller categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = AVAILABLE_BILLERS.stream()
            .filter(BillerDto::isActive)
            .map(BillerDto::getCategory)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular billers (most commonly used)")
    public ResponseEntity<List<BillerDto>> getPopularBillers(
            @Parameter(description = "Maximum number of billers to return")
            @RequestParam(defaultValue = "8") int limit) {
        
        // For now, return the first 'limit' billers as popular ones
        // In production, this would be based on usage statistics
        List<BillerDto> popularBillers = AVAILABLE_BILLERS.stream()
            .filter(BillerDto::isActive)
            .limit(limit)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(popularBillers);
    }
}
