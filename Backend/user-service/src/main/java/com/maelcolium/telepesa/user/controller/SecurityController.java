package com.maelcolium.telepesa.user.controller;

import com.maelcolium.telepesa.user.dto.ChangePinRequest;
import com.maelcolium.telepesa.user.dto.CreatePinRequest;
import com.maelcolium.telepesa.user.dto.TransactionPinResponse;
import com.maelcolium.telepesa.user.dto.VerifyPinRequest;
import com.maelcolium.telepesa.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/security")
@Tag(name = "Security", description = "Security endpoints including transaction PIN")
@RequiredArgsConstructor
public class SecurityController {

    private final UserService userService;

    private Long getCurrentUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getName())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.maelcolium.telepesa.user.security.UserPrincipal userPrincipal) {
                return userPrincipal.getId();
            }
            if (principal instanceof UserDetails userDetails) {
                String username = userDetails.getUsername();
                return userService.getUserByUsername(username).getId();
            }
            String name = authentication.getName();
            if (name != null && !name.isBlank()) {
                return userService.getUserByUsername(name).getId();
            }
        }
        String gatewayUser = request.getHeader("X-User-Name");
        if (gatewayUser != null && !gatewayUser.isBlank()) {
            return userService.getUserByUsername(gatewayUser).getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    @Operation(summary = "Create transaction PIN")
    @PostMapping("/transaction-pin")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionPinResponse> createPin(@Valid @RequestBody CreatePinRequest request,
                                                            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TransactionPinResponse response = userService.createTransactionPin(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify transaction PIN")
    @PostMapping("/transaction-pin/verify")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> verifyPin(@Valid @RequestBody VerifyPinRequest request,
                                                          HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Map<String, Boolean> result = userService.verifyTransactionPin(userId, request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Change transaction PIN")
    @PutMapping("/transaction-pin")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionPinResponse> changePin(@Valid @RequestBody ChangePinRequest request,
                                                            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TransactionPinResponse response = userService.changeTransactionPin(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get transaction PIN status")
    @GetMapping("/transaction-pin/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionPinResponse> getPinStatus(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TransactionPinResponse response = userService.getTransactionPinStatus(userId);
        return ResponseEntity.ok(response);
    }
}


