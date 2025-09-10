package com.maelcolium.telepesa.bill.payment.service.impl;

import com.maelcolium.telepesa.bill.payment.dto.BillPaymentResponse;
import com.maelcolium.telepesa.bill.payment.dto.CreateBillPaymentRequest;
import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import com.maelcolium.telepesa.bill.payment.provider.PaymentProvider;
import com.maelcolium.telepesa.bill.payment.repository.BillPaymentRepository;
import com.maelcolium.telepesa.bill.payment.service.BillPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillPaymentServiceImpl implements BillPaymentService {
    
    private final BillPaymentRepository billPaymentRepository;
    private final List<PaymentProvider> paymentProviders;
    
    @Override
    public BillPaymentResponse createPayment(String accountId, CreateBillPaymentRequest request) {
        log.info("Creating bill payment for account: {}, bill: {}", accountId, request.getBillNumber());
        
        // Find appropriate payment provider
        PaymentProvider provider = findProvider(request.getBillType(), request.getServiceProvider());
        if (provider == null) {
            throw new IllegalArgumentException("No payment provider available for: " + 
                request.getBillType() + " - " + request.getServiceProvider());
        }
        
        // Validate bill details
        PaymentProvider.BillValidationResult validation = provider.validateBill(
            new PaymentProvider.BillValidationRequest(
                request.getBillNumber(),
                request.getServiceProvider(),
                request.getBillType(),
                request.getCustomerName(),
                request.getMeterNumber(),
                request.getAccountNumber()
            )
        );
        
        if (!validation.valid()) {
            throw new IllegalArgumentException("Bill validation failed: " + validation.errorMessage());
        }
        
        // Calculate service fee
        BigDecimal serviceFee = provider.getServiceFee(request.getAmount(), request.getBillType());
        BigDecimal totalAmount = request.getAmount().add(serviceFee);
        
        // Create bill payment entity
        BillPayment payment = new BillPayment();
        payment.setPaymentReference(generatePaymentReference());
        payment.setAccountId(accountId);
        payment.setBillNumber(request.getBillNumber());
        payment.setCustomerName(validation.customerName());
        payment.setBillType(request.getBillType());
        payment.setServiceProvider(request.getServiceProvider());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setServiceFee(serviceFee);
        payment.setTotalAmount(totalAmount);
        payment.setStatus(BillPayment.PaymentStatus.PENDING);
        payment.setDescription(request.getDescription());
        payment.setDueDate(request.getDueDate());
        payment.setMeterNumber(request.getMeterNumber());
        payment.setAccountNumber(request.getAccountNumber());
        payment.setPhoneNumber(request.getPhoneNumber());
        
        // Save payment
        BillPayment savedPayment = billPaymentRepository.save(payment);
        
        // Process payment immediately
        return processPaymentWithProvider(savedPayment, provider);
    }
    
    @Override
    @Cacheable(value = "billPayments", key = "#paymentId")
    public BillPaymentResponse getPaymentById(String paymentId) {
        BillPayment payment = billPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }
    
    @Override
    @Cacheable(value = "billPayments", key = "#paymentReference")
    public BillPaymentResponse getPaymentByReference(String paymentReference) {
        BillPayment payment = billPaymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentReference));
        return mapToResponse(payment);
    }
    
    @Override
    @Cacheable(value = "accountBillPayments", key = "#accountId + '_' + #pageable.pageNumber")
    public Page<BillPaymentResponse> getPaymentsByAccount(String accountId, Pageable pageable) {
        return billPaymentRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
            .map(this::mapToResponse);
    }
    
    @Override
    @Cacheable(value = "billTypePayments", key = "#billType + '_' + #pageable.pageNumber")
    public Page<BillPaymentResponse> getPaymentsByType(BillPayment.BillType billType, Pageable pageable) {
        return billPaymentRepository.findByBillTypeOrderByCreatedAtDesc(billType, pageable)
            .map(this::mapToResponse);
    }
    
    @Override
    public BillPaymentResponse cancelPayment(String paymentId, String reason) {
        BillPayment payment = billPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (payment.getStatus() == BillPayment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed payment");
        }
        
        payment.setStatus(BillPayment.PaymentStatus.CANCELLED);
        payment.setFailureReason(reason);
        payment.setProcessedAt(LocalDateTime.now());
        
        BillPayment updatedPayment = billPaymentRepository.save(payment);
        return mapToResponse(updatedPayment);
    }
    
    @Override
    public BigDecimal calculateServiceFee(BigDecimal amount, BillPayment.BillType billType, String provider) {
        PaymentProvider paymentProvider = findProvider(billType, provider);
        if (paymentProvider != null) {
            return paymentProvider.getServiceFee(amount, billType);
        }
        
        // Default fee calculation if no specific provider found
        return calculateDefaultServiceFee(amount, billType);
    }
    
    @Override
    public BillPaymentResponse retryPayment(String paymentId) {
        BillPayment payment = billPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (payment.getStatus() != BillPayment.PaymentStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed payments");
        }
        
        PaymentProvider provider = findProvider(payment.getBillType(), payment.getServiceProvider());
        if (provider == null) {
            throw new IllegalStateException("No payment provider available");
        }
        
        payment.setStatus(BillPayment.PaymentStatus.PENDING);
        payment.setFailureReason(null);
        billPaymentRepository.save(payment);
        
        return processPaymentWithProvider(payment, provider);
    }
    
    private BillPaymentResponse processPaymentWithProvider(BillPayment payment, PaymentProvider provider) {
        log.info("Processing payment {} with provider {}", payment.getId(), provider.getProviderName());
        
        payment.setStatus(BillPayment.PaymentStatus.PROCESSING);
        payment.setProcessedAt(LocalDateTime.now());
        billPaymentRepository.save(payment);
        
        try {
            // Process payment with provider
            PaymentProvider.PaymentResult result = provider.processPayment(
                new PaymentProvider.PaymentRequest(
                    payment.getBillNumber(),
                    payment.getCustomerName(),
                    payment.getAmount(),
                    payment.getServiceProvider(),
                    payment.getBillType(),
                    payment.getPaymentReference(),
                    payment.getMeterNumber(),
                    payment.getAccountNumber(),
                    payment.getPhoneNumber()
                )
            );
            
            if (result.successful()) {
                payment.setStatus(BillPayment.PaymentStatus.COMPLETED);
                payment.setProviderTransactionId(result.providerTransactionId());
                payment.setProviderReference(result.providerReference());
            } else {
                payment.setStatus(BillPayment.PaymentStatus.FAILED);
                payment.setFailureReason(result.errorMessage());
            }
            
        } catch (Exception e) {
            log.error("Payment processing failed for {}: {}", payment.getId(), e.getMessage(), e);
            payment.setStatus(BillPayment.PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
        }
        
        BillPayment updatedPayment = billPaymentRepository.save(payment);
        return mapToResponse(updatedPayment);
    }
    
    private PaymentProvider findProvider(BillPayment.BillType billType, String serviceProvider) {
        return paymentProviders.stream()
            .filter(provider -> provider.supports(billType, serviceProvider))
            .findFirst()
            .orElse(null);
    }
    
    private String generatePaymentReference() {
        return "BILL" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    private BigDecimal calculateDefaultServiceFee(BigDecimal amount, BillPayment.BillType billType) {
        // Default service fee logic
        switch (billType) {
            case ELECTRICITY:
                return new BigDecimal("25.00");
            case WATER:
                return new BigDecimal("20.00");
            case INTERNET:
                return new BigDecimal("15.00");
            case TV_SUBSCRIPTION:
                return new BigDecimal("10.00");
            default:
                return new BigDecimal("30.00");
        }
    }
    
    private BillPaymentResponse mapToResponse(BillPayment payment) {
        BillPaymentResponse response = new BillPaymentResponse();
        response.setId(payment.getId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setAccountId(payment.getAccountId());
        response.setBillNumber(payment.getBillNumber());
        response.setCustomerName(payment.getCustomerName());
        response.setBillType(payment.getBillType());
        response.setServiceProvider(payment.getServiceProvider());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setServiceFee(payment.getServiceFee());
        response.setTotalAmount(payment.getTotalAmount());
        response.setStatus(payment.getStatus());
        response.setDescription(payment.getDescription());
        response.setProviderTransactionId(payment.getProviderTransactionId());
        response.setProviderReference(payment.getProviderReference());
        response.setProcessedAt(payment.getProcessedAt());
        response.setFailureReason(payment.getFailureReason());
        response.setDueDate(payment.getDueDate());
        response.setMeterNumber(payment.getMeterNumber());
        response.setAccountNumber(payment.getAccountNumber());
        response.setPhoneNumber(payment.getPhoneNumber());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}
