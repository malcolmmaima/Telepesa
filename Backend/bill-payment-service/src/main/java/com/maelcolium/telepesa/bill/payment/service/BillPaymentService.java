package com.maelcolium.telepesa.bill.payment.service;

import com.maelcolium.telepesa.bill.payment.dto.BillPaymentResponse;
import com.maelcolium.telepesa.bill.payment.dto.CreateBillPaymentRequest;
import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BillPaymentService {

    BillPaymentResponse createPayment(String accountId, CreateBillPaymentRequest request);

    BillPaymentResponse getPaymentById(String paymentId);

    BillPaymentResponse getPaymentByReference(String paymentReference);

    Page<BillPaymentResponse> getPaymentsByAccount(String accountId, Pageable pageable);

    Page<BillPaymentResponse> getPaymentsByType(BillPayment.BillType billType, Pageable pageable);

    BillPaymentResponse cancelPayment(String paymentId, String reason);

    BigDecimal calculateServiceFee(BigDecimal amount, BillPayment.BillType billType, String provider);

    BillPaymentResponse retryPayment(String paymentId);
}

