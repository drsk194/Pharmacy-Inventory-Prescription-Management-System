package com.pharmacy.pipms.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private String paymentMode;
    private String transactionReference;
    private String receivedByName;
    private LocalDateTime paymentDate;
}