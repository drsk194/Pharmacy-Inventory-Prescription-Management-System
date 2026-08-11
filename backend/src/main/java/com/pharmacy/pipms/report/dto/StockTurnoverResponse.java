package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

// currentStockAsAverageProxy: see Module 17's Assumption 4 — no historical
// snapshot table exists, so today's stock level stands in for a true
// period average.
@Getter
@AllArgsConstructor
public class StockTurnoverResponse {
    private Long drugId;
    private String drugGenericName;
    private BigDecimal quantityDispensedInPeriod;
    private BigDecimal currentStockAsAverageProxy;
    private BigDecimal turnoverRatio;
}