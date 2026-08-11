package com.pharmacy.pipms.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BillResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private LocalDateTime billDate;
    private BigDecimal subtotal;
    private BigDecimal dispensingFee;
    private BigDecimal discountPercent;
    private String discountReason;
    private BigDecimal discountAmount;
    private BigDecimal gstPercent;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String insuranceProvider;
    private String insuranceClaimNumber;
    private String insuranceClaimStatus;
    private BigDecimal insuranceAmount;
    private BigDecimal coPayment;
    private BigDecimal amountPaid;
    private BigDecimal amountRefunded;
    private BigDecimal outstandingAmount;
    private String status;
    private List<BillItemResponse> items;
    private List<PaymentResponse> payments;
    private List<RefundResponse> refunds;
}