package com.pharmacy.pipms.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RefundResponse {
    private Long id;
    private BigDecimal amount;
    private String reason;
    private Long medicationReturnId;
    private String processedByName;
    private LocalDateTime refundDate;
}