package com.pharmacy.pipms.controlledsubstance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

// Backing GET /reports — a mocked regulatory report using real register
// data, not a live CDSCO submission format. See module notes, Assumption 8.
@Getter
@AllArgsConstructor
public class CsDrugSummaryResponse {
    private Long drugId;
    private String drugGenericName;
    private String schedule;
    private BigDecimal totalReceived;
    private BigDecimal totalDispensed;
    private BigDecimal totalReturned;
    private BigDecimal totalDisposed;
    private BigDecimal currentBalance;
    private long transactionCount;
}