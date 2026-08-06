package com.pharmacy.pipms.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdjustmentResponse {
    private Long id;
    private Long batchId;
    private String batchNumber;
    private BigDecimal previousQuantity;
    private BigDecimal adjustmentQuantity;
    private BigDecimal newQuantity;
    private String reasonCode;
    private String notes;
    private String requestedByName;
    private String status;
    private String approvedByName;
    private LocalDateTime approvalDate;
}