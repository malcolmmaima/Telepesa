package com.maelcolium.telepesa.transfer.controller;

import com.maelcolium.telepesa.transfer.dto.BankResponse;
import com.maelcolium.telepesa.transfer.dto.RecipientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bank and Recipients Management", description = "APIs for managing banks and recipients")
public class BankController {

    @GetMapping("/banks")
    @Operation(summary = "Get supported banks by country")
    public ResponseEntity<List<BankResponse>> getSupportedBanks(
            @Parameter(description = "Country code (e.g., KE, UG, TZ)")
            @RequestParam String country) {
        
        log.info("Getting supported banks for country: {}", country);
        List<BankResponse> banks = getSampleBanks(country);
        return ResponseEntity.ok(banks);
    }

    @GetMapping("/user/{userId}/recipients")
    @Operation(summary = "Get saved recipients for a user")
    public ResponseEntity<List<RecipientResponse>> getSavedRecipients(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting saved recipients for user: {}", userId);
        List<RecipientResponse> recipients = getSampleRecipients(userId);
        return ResponseEntity.ok(recipients);
    }

    @PostMapping("/user/{userId}/recipients")
    @Operation(summary = "Add a new recipient for a user")
    public ResponseEntity<RecipientResponse> addRecipient(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId,
            @RequestBody RecipientResponse recipientRequest) {
        
        log.info("Adding new recipient for user: {}", userId);
        RecipientResponse recipient = RecipientResponse.builder()
                .id("recipient-" + System.currentTimeMillis())
                .userId(userId)
                .recipientName(recipientRequest.getRecipientName())
                .recipientEmail(recipientRequest.getRecipientEmail())
                .recipientPhone(recipientRequest.getRecipientPhone())
                .bankCode(recipientRequest.getBankCode())
                .bankName(recipientRequest.getBankName())
                .accountNumber(recipientRequest.getAccountNumber())
                .accountType(recipientRequest.getAccountType())
                .currency("KES")
                .isVerified(false)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .transferCount(0)
                .build();
        
        return ResponseEntity.ok(recipient);
    }

    // Sample data methods
    private List<BankResponse> getSampleBanks(String country) {
        if ("KE".equalsIgnoreCase(country)) {
            return List.of(
                BankResponse.builder()
                    .id("bank-1")
                    .bankCode("KCB")
                    .bankName("Kenya Commercial Bank")
                    .country("KE")
                    .swift("KCBLKENX")
                    .active(true)
                    .supportedCurrencies(List.of("KES", "USD"))
                    .features(BankResponse.BankFeatures.builder()
                        .instantTransfer(true)
                        .scheduledTransfer(true)
                        .internationalTransfer(true)
                        .maxTransferAmount("1000000")
                        .minTransferAmount("10")
                        .build())
                    .build(),
                BankResponse.builder()
                    .id("bank-2")
                    .bankCode("EQB")
                    .bankName("Equity Bank")
                    .country("KE")
                    .swift("EQBLKENA")
                    .active(true)
                    .supportedCurrencies(List.of("KES", "USD"))
                    .features(BankResponse.BankFeatures.builder()
                        .instantTransfer(true)
                        .scheduledTransfer(true)
                        .internationalTransfer(false)
                        .maxTransferAmount("500000")
                        .minTransferAmount("1")
                        .build())
                    .build(),
                BankResponse.builder()
                    .id("bank-3")
                    .bankCode("COOP")
                    .bankName("Co-operative Bank")
                    .country("KE")
                    .swift("COOPKENX")
                    .active(true)
                    .supportedCurrencies(List.of("KES"))
                    .features(BankResponse.BankFeatures.builder()
                        .instantTransfer(true)
                        .scheduledTransfer(false)
                        .internationalTransfer(false)
                        .maxTransferAmount("200000")
                        .minTransferAmount("50")
                        .build())
                    .build(),
                BankResponse.builder()
                    .id("bank-4")
                    .bankCode("ABSA")
                    .bankName("Absa Bank Kenya")
                    .country("KE")
                    .swift("BARCKENX")
                    .active(true)
                    .supportedCurrencies(List.of("KES", "USD", "EUR"))
                    .features(BankResponse.BankFeatures.builder()
                        .instantTransfer(true)
                        .scheduledTransfer(true)
                        .internationalTransfer(true)
                        .maxTransferAmount("2000000")
                        .minTransferAmount("100")
                        .build())
                    .build(),
                BankResponse.builder()
                    .id("bank-5")
                    .bankCode("STDCHRT")
                    .bankName("Standard Chartered Bank")
                    .country("KE")
                    .swift("SCBLKENX")
                    .active(true)
                    .supportedCurrencies(List.of("KES", "USD", "GBP", "EUR"))
                    .features(BankResponse.BankFeatures.builder()
                        .instantTransfer(true)
                        .scheduledTransfer(true)
                        .internationalTransfer(true)
                        .maxTransferAmount("5000000")
                        .minTransferAmount("500")
                        .build())
                    .build()
            );
        }
        
        // Return empty list for other countries for now
        return List.of();
    }

    private List<RecipientResponse> getSampleRecipients(String userId) {
        return IntStream.range(1, 6)
                .mapToObj(i -> RecipientResponse.builder()
                    .id("recipient-" + i)
                    .userId(userId)
                    .recipientName("Recipient " + i)
                    .recipientEmail("recipient" + i + "@example.com")
                    .recipientPhone("+254700000" + String.format("%03d", i))
                    .bankCode(i % 2 == 0 ? "KCB" : "EQB")
                    .bankName(i % 2 == 0 ? "Kenya Commercial Bank" : "Equity Bank")
                    .accountNumber("123456789" + i)
                    .accountType("SAVINGS")
                    .currency("KES")
                    .isVerified(i % 3 == 0)
                    .isFavorite(i <= 2)
                    .createdAt(LocalDateTime.now().minusDays(i * 10))
                    .lastUsed(i <= 3 ? LocalDateTime.now().minusDays(i) : null)
                    .transferCount(i * 2)
                    .build())
                .toList();
    }
}
