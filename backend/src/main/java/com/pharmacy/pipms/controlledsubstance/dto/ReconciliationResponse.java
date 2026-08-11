package com.pharmacy.pipms.controlledsubstance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReconciliationResponse {
    private Long id;
    private Long drugId;
    private String drugGenericName;
    private BigDecimal expectedQuantity;
    private BigDecimal countedQuantity;
    private BigDecimal variance;
    private boolean discrepancyFlagged;
    private boolean manuallyReported;
    private String performedByName;
    private String notes;
    private LocalDateTime createdAt;
}