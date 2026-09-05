package com.pharmacy.pipms.dispensing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BalanceOrderResponse {
    private Long id;
    private Long prescriptionItemId;
    private Long patientId;
    private String patientName;
    private String drugGenericName;
    private BigDecimal quantityPending;
    private String status;
    private LocalDateTime createdAt;
}
